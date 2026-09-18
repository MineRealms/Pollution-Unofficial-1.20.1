package meowmel.pollution.common.machine.multiblock.node;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.machine.multiblock.part.EnergyHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import meowmel.pollution.api.pollution.PollutionEngine;
import meowmel.pollution.compat.tc4r.TC4RBridge;
import meowmel.pollution.api.unification.PollutionMaterials;
import meowmel.pollution.common.block.PollutionMagicBlocks;
import meowmel.pollution.common.item.PackagedAuraNode;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.Random;

/**
 * Large node generator: burns packaged aura nodes as fuel and outputs EU.
 *
 * <p>Capacity multipliers (tier/type/essence), the infused aura + order upkeep
 * costs and the special node behaviours follow upstream. The ambient-aura
 * effects of TC6 are mapped onto TC4R: "Pure" scrubs flux around the machine,
 * "Ominous" adds industrial pollution instead of polluting the aura, and
 * Concussive/Voracious nodes may burn out when their upkeep fluid is
 * missing.</p>
 */
public class LargeNodeGeneratorMachine extends MultiblockControllerMachine {

    private static final int BASIC_CAPACITY = 2048;
    private static final double BURNOUT_CHANCE = 0.001;

    private final Random random = new Random();

    private TickableSubscription tickSubscription;

    public LargeNodeGeneratorMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            tickSubscription = subscribeServerTick(this::tickGenerator);
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

    private void tickGenerator() {
        if (!(getLevel() instanceof ServerLevel level) || !isFormed()) {
            return;
        }
        EnergyHatchPartMachine energy = findPart(EnergyHatchPartMachine.class);
        ItemBusPartMachine items = findPart(ItemBusPartMachine.class);
        FluidHatchPartMachine fluids = findPart(FluidHatchPartMachine.class);
        if (energy == null || items == null || fluids == null) {
            return;
        }

        int coilLevel = hasCoil() ? Math.max(1, getCoilLevel()) : 1;
        var inventory = items.getInventory().storage;
        float expectedCapacity = 0;
        double speedMultiplier = 1.0;
        float variance = 0;
        int nodes = 0;

        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!PackagedAuraNode.isNode(stack)) {
                continue;
            }
            nodes++;
            expectedCapacity += BASIC_CAPACITY * nodeCapacityMultiplier(stack) * coilLevel;
            if (PackagedAuraNode.hasEssence(stack, PackagedAuraNode.ESSENCE_WATER)) {
                speedMultiplier = Math.min(speedMultiplier,
                        Math.max(0.15, 1 - PackagedAuraNode.essence(stack, PackagedAuraNode.ESSENCE_WATER) / 400.0));
            }
            if (PackagedAuraNode.hasEssence(stack, PackagedAuraNode.ESSENCE_AIR)) {
                variance += PackagedAuraNode.essence(stack, PackagedAuraNode.ESSENCE_AIR) / 1000.0F;
            }
        }

        if (getOffsetTimer() % 20 == 0 && nodes > 0) {
            int essenceCost = Math.max(1, (int) Math.ceil(200 * speedMultiplier));
            FluidStack aura = PollutionMaterials.InfusedAura.getFluid(essenceCost);
            FluidStack order = PollutionMaterials.InfusedOrder.getFluid(essenceCost);
            boolean hasAura = !aura.isEmpty() &&
                    fluids.tank.drain(aura, IFluidHandler.FluidAction.SIMULATE).getAmount() >= essenceCost;
            boolean hasOrder = !order.isEmpty() &&
                    fluids.tank.drain(order, IFluidHandler.FluidAction.SIMULATE).getAmount() >= essenceCost;
            if (hasAura) {
                fluids.tank.drain(aura, IFluidHandler.FluidAction.EXECUTE);
            }
            if (hasOrder) {
                fluids.tank.drain(order, IFluidHandler.FluidAction.EXECUTE);
            }
            for (int slot = 0; slot < inventory.getSlots(); slot++) {
                ItemStack stack = inventory.getStackInSlot(slot);
                if (PackagedAuraNode.isNode(stack)) {
                    specialNodeBehaviors(level, inventory, slot, stack, hasAura, hasOrder);
                }
            }
        }

        if (nodes == 0) {
            return;
        }
        float roll = 1 + random.nextFloat() * variance / nodes;
        long finalCapacity = (long) (expectedCapacity * roll);
        var container = energy.energyContainer;
        if (finalCapacity > 0 && container.getEnergyCapacity() - container.getEnergyStored() >= finalCapacity) {
            container.addEnergy(finalCapacity);
        }
    }

    private float nodeCapacityMultiplier(ItemStack node) {
        float multiplier = 1.0F;
        switch (PackagedAuraNode.tier(node)) {
            case "Withering" -> multiplier *= 0.25F;
            case "Pale" -> multiplier *= 0.5F;
            case "Bright" -> multiplier *= 4.0F;
            default -> {}
        }
        switch (PackagedAuraNode.type(node)) {
            case "Ominous", "Pure" -> multiplier *= 2.0F;
            case "Concussive" -> multiplier *= 4.0F;
            case "Voracious" -> multiplier *= 8.0F;
            default -> {}
        }
        multiplier *= Math.max((1.2F - 0.005F * PackagedAuraNode.essence(node, PackagedAuraNode.ESSENCE_ENTROPY)), 0);
        multiplier *= (float) (1 + 0.02 * Math.sqrt(
                PackagedAuraNode.essence(node, PackagedAuraNode.ESSENCE_FIRE) *
                        PackagedAuraNode.essence(node, PackagedAuraNode.ESSENCE_ORDER)));
        return multiplier;
    }

    private void specialNodeBehaviors(ServerLevel level, Object inventory, int slot, ItemStack node,
                                      boolean hasAura, boolean hasOrder) {
        switch (PackagedAuraNode.type(node)) {
            case "Ominous" -> PollutionEngine.add(level, getPos(), 0.1);
            case "Pure" -> TC4RBridge.scrubFlux(level, getPos(), 1);
            case "Concussive" -> {
                if (random.nextDouble() <= BURNOUT_CHANCE * 10) {
                    consumeNode(inventory, slot);
                }
            }
            case "Voracious" -> {
                if (!hasAura && !hasOrder && random.nextDouble() <= BURNOUT_CHANCE * 10) {
                    consumeNode(inventory, slot);
                }
            }
            default -> {}
        }
    }

    private void consumeNode(Object inventory, int slot) {
        if (inventory instanceof com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler handler) {
            handler.extractItem(slot, handler.getStackInSlot(slot).getCount(), false);
        }
    }

    private int getCoilLevel() {
        var coil = getMultiblockState().getMatchContext().get("CoilType");
        if (coil instanceof com.gregtechceu.gtceu.api.block.ICoilType coilType) {
            return coilType.getLevel();
        }
        return 0;
    }

    private boolean hasCoil() {
        return getCoilLevel() > 0;
    }

    private <T> T findPart(Class<T> type) {
        for (IMultiPart part : getParts()) {
            if (type.isInstance(part.self())) {
                return type.cast(part.self());
            }
        }
        return null;
    }

    public static BlockPattern createPattern(MultiblockMachineDefinition definition) {
        return FactoryBlockPattern.start()
                .aisle(" A ", " B ", "   ", "   ", "   ", "   ", "   ", "   ", "   ", "   ", "   ")
                .aisle("ABA", "BBB", "   ", "   ", "   ", "   ", "   ", "   ", "   ", "   ", "   ")
                .aisle("ABA", "BBB", "HAC", "   ", "   ", "   ", "   ", "   ", "   ", "   ", "   ")
                .aisle("ABA", "BBB", "HAC", "CAC", "   ", "   ", "DDD", "   ", "DDD", "   ", "   ")
                .aisle("ABA", "BBB", "HBC", "CBC", " B ", " B ", "DBD", " B ", "DBD", " E ", "   ")
                .aisle("ABA", "BBB", " A ", "C C", "C C", " A ", "DDD", "   ", "DDD", "   ", "   ")
                .aisle("ABA", "BBB", " A ", " A ", " A ", " A ", " A ", " A ", "   ", "   ", "   ")
                .aisle("ABA", "BBB", "   ", "   ", "   ", "   ", "BBB", "BBB", "BBB", "   ", "   ")
                .aisle(" B ", "   ", "   ", "   ", "   ", "   ", "   ", " F ", "   ", "   ", "   ")
                .aisle(" A ", " A ", " A ", " A ", " F ", "   ", " C ", " F ", " C ", "   ", " F ")
                .aisle(" B ", "   ", "   ", "   ", "   ", " F ", "   ", "   ", "   ", " F ", "   ")
                .aisle(" A ", " B ", "   ", "   ", " C ", "   ", "   ", " C ", "   ", "   ", " C ")
                .aisle(" A ", " G ", " B ", "   ", " C ", "   ", " C ", " S ", " C ", "   ", " C ")
                .aisle(" A ", " B ", "   ", "   ", " C ", "   ", "   ", " C ", "   ", "   ", " C ")
                .aisle(" B ", "   ", "   ", "   ", "   ", " F ", "   ", "   ", "   ", " F ", "   ")
                .aisle(" A ", " A ", " A ", " A ", " F ", "   ", " C ", " F ", " C ", "   ", " F ")
                .aisle(" B ", "   ", "   ", "   ", "   ", "   ", "   ", " F ", "   ", "   ", "   ")
                .aisle("ABA", "BBB", "   ", "   ", "   ", "   ", "BBB", "BBB", "BBB", "   ", "   ")
                .aisle("ABA", "BBB", " A ", " A ", " A ", " A ", " A ", " A ", "   ", "   ", "   ")
                .aisle("ABA", "BBB", " A ", "C C", "C C", " A ", "DDD", "   ", "DDD", "   ", "   ")
                .aisle("ABA", "BBB", "HBC", "CBC", " B ", " B ", "DBD", " B ", "DBD", " E ", "   ")
                .aisle("ABA", "BBB", "HAC", "CAC", "   ", "   ", "DDD", "   ", "DDD", "   ", "   ")
                .aisle("ABA", "BBB", "HAC", "   ", "   ", "   ", "   ", "   ", "   ", "   ", "   ")
                .aisle("ABA", "BBB", "   ", "   ", "   ", "   ", "   ", "   ", "   ", "   ", "   ")
                .aisle(" A ", " B ", "   ", "   ", "   ", "   ", "   ", "   ", "   ", "   ", "   ")
                .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                .where('A', meowmel.pollution.common.machine.multiblock.MagicStructureElements
                        .frame(GTMaterials.HSSG))
                .where('B', Predicates.blocks(PollutionMagicBlocks.VOID_PRISM.get()))
                .where('C', Predicates.blocks(GTBlocks.FUSION_CASING.get())
                        .or(Predicates.abilities(PartAbility.OUTPUT_ENERGY).setMaxGlobalLimited(1))
                        .or(Predicates.abilities(PartAbility.OUTPUT_LASER).setMaxGlobalLimited(1))
                        .or(Predicates.abilities(PartAbility.MAINTENANCE).setMaxGlobalLimited(1))
                        .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setMaxGlobalLimited(1))
                        .setMinGlobalLimited(25))
                .where('D', Predicates.heatingCoils())
                .where('E', Predicates.blocks(PollutionMagicBlocks.BEAM_CORE_4.get()))
                .where('F', Predicates.blocks(PollutionMagicBlocks.BAMINATED_GLASS.get()))
                .where('G', Predicates.blocks(PollutionMagicBlocks.VOID_PRISM.get()))
                .where('H', Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(1))
                .where(' ', Predicates.any())
                .build();
    }
}
