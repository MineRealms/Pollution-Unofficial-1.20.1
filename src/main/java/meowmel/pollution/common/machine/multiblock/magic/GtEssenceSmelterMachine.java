package meowmel.pollution.common.machine.multiblock.magic;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.machine.multiblock.part.EnergyHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import dev.tc4port.thaumcraft.api.aspect.AspectAmounts;
import dev.tc4port.thaumcraft.api.aspect.AspectQueryApi;
import dev.tc4port.thaumcraft.api.aspect.AspectId;
import meowmel.pollution.api.magic.PollutionAspectMapping;
import meowmel.pollution.api.unification.PollutionMaterials;
import meowmel.pollution.api.magic.EssenceBatch;
import net.minecraft.nbt.CompoundTag;
import meowmel.pollution.common.block.PollutionMagicBlocks;
import meowmel.pollution.common.machine.multiblock.AbstractDisplayMultiblockMachine;
import meowmel.pollution.common.machine.multiblock.MagicStructureElements;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.List;
import java.util.Map;

/**
 * GT essence smelter: like the essence smelter, but instead of filling nearby
 * jars it converts the dissolved aspects into their mapped infused fluids
 * (144 mB per aspect unit, the upstream {@code POAspectToGtFluidList} rate).
 */
public class GtEssenceSmelterMachine extends AbstractDisplayMultiblockMachine {

    private TickableSubscription tickSubscription;

    private final EssenceBatch batch = new EssenceBatch();
    private int infusedCost = 1;
    private int euTier = 1;

    public GtEssenceSmelterMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            tickSubscription = subscribeServerTick(this::tickSmelter);
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (tickSubscription != null) {
            tickSubscription.unsubscribe();
            tickSubscription = null;
        }
    }

    private void tickSmelter() {
        if (!(getLevel() instanceof ServerLevel) || !isFormed()) {
            return;
        }
        if (batch.ready()) {
            transportEssence();
            return;
        }
        EnergyHatchPartMachine energy = findPart(EnergyHatchPartMachine.class);
        FluidHatchPartMachine fluids = findFluidHatch(PartAbility.IMPORT_FLUIDS);
        FluidHatchPartMachine outputFluids = findFluidHatch(PartAbility.EXPORT_FLUIDS);
        ItemBusPartMachine items = findPart(ItemBusPartMachine.class);
        if (energy == null || fluids == null || outputFluids == null || items == null) {
            return;
        }

        long inputVoltage = Math.max(1, energy.energyContainer.getInputVoltage());
        euTier = Math.max(1, (int) Math.ceil(Math.log((double) inputVoltage / 32) / Math.log(4) + 1));
        infusedCost = euTier <= 1 ? 1 : Math.max(1, (int) (Math.log(1024) / Math.log(euTier)));

        if (!batch.active() && !tryStartRecipe(items)) {
            return;
        }

        if (energy.energyContainer.getEnergyStored() < inputVoltage) {
            return;
        }
        FluidStack infusedFire = PollutionMaterials.InfusedFire.getFluid(infusedCost);
        if (!infusedFire.isEmpty() &&
                fluids.tank.drainInternal(infusedFire, IFluidHandler.FluidAction.SIMULATE).getAmount() < infusedCost) {
            return;
        }
        energy.energyContainer.changeEnergy(-inputVoltage);
        if (!infusedFire.isEmpty()) {
            fluids.tank.drainInternal(infusedFire, IFluidHandler.FluidAction.EXECUTE);
        }
        batch.advance();
        markDirty();
        if (batch.ready()) transportEssence();
    }

    private boolean tryStartRecipe(ItemBusPartMachine items) {
        var inventory = items.getInventory().storage;
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack candidate = inventory.extractItem(slot, Integer.MAX_VALUE, true);
            if (candidate.isEmpty()) continue;
            AspectAmounts aspects = AspectQueryApi.item(candidate);
            Map<String, Long> outputs = outputs(aspects, candidate.getCount());
            if (outputs.isEmpty()) continue;
            ItemStack extracted = inventory.extractItem(slot, candidate.getCount(), false);
            if (extracted.isEmpty()) continue;
            outputs = outputs(aspects, extracted.getCount());
            long total = aspects.amounts().values().stream().filter(java.util.Objects::nonNull)
                    .mapToLong(n -> Math.max(0, n)).sum() * extracted.getCount();
            int ticks = (int) Math.min(Integer.MAX_VALUE,
                    Math.max(20D, total * 10D / Math.pow(4, Math.max(0, euTier - 1))));
            batch.start(outputs, ticks);
            markDirty();
            return true;
        }
        return false;
    }

    /** Reject unmapped aspects before consuming any input; amounts are buffered in mB. */
    private static Map<String, Long> outputs(AspectAmounts aspects, int count) {
        Map<String, Long> outputs = new java.util.LinkedHashMap<>();
        for (var entry : aspects.amounts().entrySet()) {
            if (entry.getValue() == null || entry.getValue() <= 0) continue;
            Material material = PollutionAspectMapping.materialOf(entry.getKey()).orElse(null);
            if (material == null || !material.hasFluid() || material.getFluid(1).isEmpty()) return Map.of();
            outputs.put(entry.getKey().toString(), (long) entry.getValue() * count * 144L);
        }
        return outputs;
    }

    private void transportEssence() {
        // Try every export hatch. Partial fills retain their exact mB remainder, not rounded aspects.
        for (IMultiPart part : getParts()) {
            if (!(part.self() instanceof FluidHatchPartMachine hatch)
                    || !PartAbility.EXPORT_FLUIDS.isApplicable(hatch.getBlockState().getBlock())) continue;
            boolean changed = batch.offer((key, amount) -> {
                Material material = PollutionAspectMapping.materialOf(AspectId.parse(key)).orElse(null);
                if (material == null || !material.hasFluid()) return 0L;
                FluidStack fluid = material.getFluid((int) Math.min(Integer.MAX_VALUE, amount));
                return fluid.isEmpty() ? 0L : hatch.tank.fillInternal(fluid, IFluidHandler.FluidAction.EXECUTE);
            });
            if (changed) markDirty();
            if (!batch.active()) break;
        }
    }

    private <T> T findPart(Class<T> type) {
        for (IMultiPart part : getParts()) {
            if (type.isInstance(part.self())) {
                return type.cast(part.self());
            }
        }
        return null;
    }

    private FluidHatchPartMachine findFluidHatch(PartAbility ability) {
        for (IMultiPart part : getParts()) {
            if (part.self() instanceof FluidHatchPartMachine hatch
                    && ability.isApplicable(hatch.getBlockState().getBlock())) {
                return hatch;
            }
        }
        return null;
    }

    @Override
    public void saveCustomPersistedData(CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        tag.put("EssenceBatch", batch.save());
    }

    @Override
    public void loadCustomPersistedData(CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        batch.load(tag.getCompound("EssenceBatch"));
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (isFormed()) {
            textList.add(Component.translatable("pollution.ui.progress", batch.progress(), batch.duration()));
            textList.add(Component.translatable("pollution.ui.essence_work", Component.translatable(batch.active() ? "pollution.ui.yes" : "pollution.ui.no"), infusedCost));
        }
    }

    public static BlockPattern createPattern(MultiblockMachineDefinition definition) {
        return FactoryBlockPattern.start()
                .aisle(" ABBBBBA", " AACCCAA", " A AAA A", " D     D", "        ", "        ")
                .aisle("ABEEEEEB", "ABCCCCCA", " BBBBBB ", "  F   F ", "  FFFFF ", "  G   G ")
                .aisle("BBEEEEEB", "CCCHHHCC", " CBGGGBA", "   CCC  ", "  F   F ", "        ")
                .aisle("SBEEEEEB", "CCCHHHCC", " CBGGGBA", "   CCC  ", "  F   F ", "        ")
                .aisle("BBEEEEEB", "CCCHHHCC", " CBGGGBA", "   CCC  ", "  F   F ", "        ")
                .aisle("ABEEEEEB", "ABCCCCCA", " BBBBBB ", "  F   F ", "  FFFFF ", "  G   G ")
                .aisle(" ABBBBBA", " AACCCAA", " A AAA A", " D     D", "        ", "        ")
                .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                .where('B', Predicates.blocks(PollutionMagicBlocks.SPELL_PRISM_VOID.get())
                        .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMinGlobalLimited(1).setMaxGlobalLimited(27))
                        .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setExactLimit(1))
                        .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setMinGlobalLimited(6).setMaxGlobalLimited(27))
                        .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(2))
                        .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                .where('E', Predicates.blocks(PollutionMagicBlocks.MAGIC_BATTERY_CASING.get()))
                .where('A', MagicStructureElements.frame(GTMaterials.StainlessSteel))
                .where('F', MagicStructureElements.frame(GTMaterials.HSSG))
                .where('D', Predicates.blocks(PollutionMagicBlocks.BEAM_CORE_2.get()))
                .where('G', Predicates.blocks(PollutionMagicBlocks.BEAM_CORE_4.get()))
                .where('C', Predicates.blocks(PollutionMagicBlocks.CAMINATED_GLASS.get()))
                .where('H', Predicates.blocks(PollutionMagicBlocks.SPELL_PRISM.get()))
                .where(' ', Predicates.any())
                .build();
    }
}
