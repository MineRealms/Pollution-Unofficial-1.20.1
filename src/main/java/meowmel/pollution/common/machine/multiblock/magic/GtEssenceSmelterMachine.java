package meowmel.pollution.common.machine.multiblock.magic;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.machine.multiblock.part.EnergyHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import dev.tc4port.thaumcraft.api.aspect.AspectAmounts;
import dev.tc4port.thaumcraft.api.aspect.AspectApi;
import dev.tc4port.thaumcraft.api.aspect.AspectId;
import meowmel.pollution.api.magic.PollutionAspectMapping;
import meowmel.pollution.api.unification.PollutionMaterials;
import meowmel.pollution.common.block.PollutionMagicBlocks;
import meowmel.pollution.common.machine.multiblock.MagicStructureElements;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.Map;

/**
 * GT essence smelter: like the essence smelter, but instead of filling nearby
 * jars it converts the dissolved aspects into their mapped infused fluids
 * (144 mB per aspect unit, the upstream {@code POAspectToGtFluidList} rate).
 */
public class GtEssenceSmelterMachine extends MultiblockControllerMachine {

    private TickableSubscription tickSubscription;

    private int progress;
    private int duration;
    private int infusedCost = 1;
    private int euTier = 1;
    private boolean working;
    private AspectAmounts pending = AspectAmounts.EMPTY;

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
            reset();
            return;
        }
        EnergyHatchPartMachine energy = findPart(EnergyHatchPartMachine.class);
        FluidHatchPartMachine fluids = findPart(FluidHatchPartMachine.class);
        ItemBusPartMachine items = findPart(ItemBusPartMachine.class);
        if (energy == null || fluids == null || items == null) {
            reset();
            return;
        }

        long inputVoltage = Math.max(1, energy.energyContainer.getInputVoltage());
        euTier = Math.max(1, (int) Math.ceil(Math.log((double) inputVoltage / 32) / Math.log(4) + 1));
        infusedCost = euTier <= 1 ? 1 : Math.max(1, (int) (Math.log(1024) / Math.log(euTier)));

        if (!working && !tryStartRecipe(items)) {
            return;
        }

        if (energy.energyContainer.getEnergyStored() < inputVoltage) {
            return;
        }
        FluidStack infusedFire = PollutionMaterials.InfusedFire.getFluid(infusedCost);
        if (!infusedFire.isEmpty() &&
                fluids.tank.drain(infusedFire, IFluidHandler.FluidAction.SIMULATE).getAmount() < infusedCost) {
            return;
        }
        energy.energyContainer.changeEnergy(-inputVoltage);
        if (!infusedFire.isEmpty()) {
            fluids.tank.drain(infusedFire, IFluidHandler.FluidAction.EXECUTE);
        }
        progress++;
        if (progress >= duration) {
            transportEssence(fluids, pending);
            reset();
        }
    }

    private boolean tryStartRecipe(ItemBusPartMachine items) {
        var inventory = items.getInventory().storage;
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            AspectAmounts aspects = AspectApi.getItemAspects(stack);
            if (aspects.amounts().isEmpty()) {
                continue;
            }
            int total = 0;
            for (Map.Entry<AspectId, Integer> entry : aspects.amounts().entrySet()) {
                total += (entry.getValue() == null ? 0 : entry.getValue()) * stack.getCount();
            }
            duration = Math.max(20, total * 10 / (int) Math.pow(4, Math.max(0, euTier - 1)));
            pending = scale(aspects, stack.getCount());
            inventory.extractItem(slot, stack.getCount(), false);
            progress = 0;
            working = true;
            return true;
        }
        return false;
    }

    private static AspectAmounts scale(AspectAmounts aspects, int multiplier) {
        if (multiplier <= 1) {
            return aspects;
        }
        java.util.Map<AspectId, Integer> scaled = new java.util.HashMap<>();
        aspects.amounts().forEach((aspect, amount) -> scaled.put(aspect, amount * multiplier));
        return new AspectAmounts(scaled);
    }

    private void transportEssence(FluidHatchPartMachine fluids, AspectAmounts aspects) {
        for (Map.Entry<AspectId, Integer> entry : aspects.amounts().entrySet()) {
            Material material = PollutionAspectMapping.materialOf(entry.getKey()).orElse(null);
            if (material == null || !material.hasFluid()) {
                continue;
            }
            int amount = entry.getValue() == null ? 0 : entry.getValue();
            if (amount <= 0) {
                continue;
            }
            FluidStack fluid = material.getFluid(144 * amount);
            if (!fluid.isEmpty()) {
                fluids.tank.fill(fluid, IFluidHandler.FluidAction.EXECUTE);
            }
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

    private void reset() {
        progress = 0;
        duration = 0;
        working = false;
        pending = AspectAmounts.EMPTY;
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
                        .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(1))
                        .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setMaxGlobalLimited(1))
                        .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setMaxGlobalLimited(6))
                        .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(2))
                        .or(Predicates.abilities(PartAbility.MAINTENANCE).setMaxGlobalLimited(1)))
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
