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
import com.gregtechceu.gtceu.common.machine.multiblock.part.EnergyHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import meowmel.pollution.api.unification.PollutionMaterials;
import meowmel.pollution.compat.tc4r.TC4RBridge;
import meowmel.pollution.common.block.PollutionMagicBlocks;
import meowmel.pollution.common.item.PackagedAuraNode;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

/**
 * Node washer.
 *
 * <p>Upstream left this machine half-implemented (empty type decision, no
 * output path, dependency on the unported packaged-node item). The port
 * completes the semantics with the data the upstream class already carried:
 * the machine washes packed aura nodes in place, consuming EU and infused
 * water to remove one batch of {@code EssenceEntropy} per operation
 * ({@code coil tier × energy tier × 25}, matching the upstream
 * {@code maxInfusedValue} formula) and scrubbing a little flux around
 * itself.</p>
 */
public class NodeWasherMachine extends MultiblockControllerMachine {

    private static final int WATER_PER_WASH = 144;

    private TickableSubscription tickSubscription;

    public NodeWasherMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            tickSubscription = subscribeServerTick(this::tickWasher);
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

    private void tickWasher() {
        if (!(getLevel() instanceof ServerLevel level) || !isFormed()) {
            return;
        }
        if (getOffsetTimer() % 20 != 0) {
            return;
        }
        EnergyHatchPartMachine energy = findPart(EnergyHatchPartMachine.class);
        FluidHatchPartMachine fluids = findPart(FluidHatchPartMachine.class);
        ItemBusPartMachine items = findPart(ItemBusPartMachine.class);
        if (energy == null || fluids == null || items == null) {
            return;
        }

        long voltage = Math.max(1, energy.energyContainer.getInputVoltage());
        int euTier = Math.max(1, (int) Math.ceil(Math.log((double) voltage / 32) / Math.log(4) + 1));
        int coilLevel = hasCoil() ? Math.max(1, getCoilLevel()) : 1;
        int maxWash = Math.max(25, coilLevel * euTier * 25);

        FluidStack water = PollutionMaterials.InfusedWater.getFluid(WATER_PER_WASH);
        if (water.isEmpty() ||
                fluids.tank.drain(water, IFluidHandler.FluidAction.SIMULATE).getAmount() < WATER_PER_WASH) {
            return;
        }

        var inventory = items.getInventory().storage;
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!PackagedAuraNode.isNode(stack)) {
                continue;
            }
            int entropy = PackagedAuraNode.essence(stack, PackagedAuraNode.ESSENCE_ENTROPY);
            if (entropy <= 0) {
                continue;
            }
            if (energy.energyContainer.getEnergyStored() < voltage) {
                return;
            }
            energy.energyContainer.changeEnergy(-voltage);
            fluids.tank.drain(water, IFluidHandler.FluidAction.EXECUTE);
            PackagedAuraNode.setEssence(stack, PackagedAuraNode.ESSENCE_ENTROPY, entropy - maxWash);
            TC4RBridge.scrubFlux(level, getPos(), 1);
            return;
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
                .aisle("XXXXXXX", "XXXXXXX", "XXXXXXX", "##XXXXX")
                .aisle("XXXXXXX", "XAXCCCX", "XXXAAAX", "##XXXXX")
                .aisle("XXXXXXX", "XAXCCCX", "XXXAAAX", "##XXXXX")
                .aisle("XXXXXXX", "XSXDDDX", "XEXDDDX", "##XXXXX")
                .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                .where('X', Predicates.blocks(PollutionMagicBlocks.SPELL_PRISM_HOT.get())
                        .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(1))
                        .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setMaxGlobalLimited(1))
                        .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(1))
                        .or(Predicates.abilities(PartAbility.MAINTENANCE).setMaxGlobalLimited(1)))
                .where('A', Predicates.blocks(PollutionMagicBlocks.POLYTETRAFLUOROETHYLENE_PIPE.get()))
                .where('C', Predicates.blocks(PollutionMagicBlocks.BEAM_CORE_4.get()))
                .where('D', Predicates.blocks(PollutionMagicBlocks.AAMINATED_GLASS.get()))
                .where('E', Predicates.heatingCoils())
                .where('#', Predicates.any())
                .build();
    }
}
