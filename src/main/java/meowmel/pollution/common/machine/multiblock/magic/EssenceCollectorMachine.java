package meowmel.pollution.common.machine.multiblock.magic;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.common.machine.multiblock.part.EnergyHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import dev.tc4port.thaumcraft.api.node.AuraNodeState;
import dev.tc4port.thaumcraft.api.node.NodeVis;
import dev.tc4port.thaumcraft.block.entity.AuraNodeBlockEntity;
import meowmel.pollution.api.pollution.PollutionEngine;
import meowmel.pollution.api.unification.PollutionMaterials;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Essence collector: condenses the local Thaumcraft energy field into pure
 * infused fluids.
 *
 * <p>Upstream read the TC6 ambient chunk aura: it needed {@code flux < vis}
 * and produced six infused fluids each tick at
 * {@code ceil(0.025 × (1+coil) × 2^tier × (ln(vis) − ln(1+flux/vis)))}.
 * TC4R has no ambient aura, so the port substitutes the aura node's current vis
 * for {@code vis} and the industrial pollution of the chunk for {@code flux};
 * the focused crystal mode of upstream has no TC4R crystal equivalent yet and
 * is dropped (documented deviation).</p>
 */
public class EssenceCollectorMachine extends MultiblockControllerMachine {

    private static final float BASIC_SPEED_PER_TICK = 0.025F;
    private static final int NODE_RADIUS = 8;

    private TickableSubscription tickSubscription;

    public EssenceCollectorMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            tickSubscription = subscribeServerTick(this::tickCollector);
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

    private void tickCollector() {
        if (!(getLevel() instanceof ServerLevel level) || !isFormed()) {
            return;
        }
        EnergyHatchPartMachine energy = findPart(EnergyHatchPartMachine.class);
        if (energy == null) {
            return;
        }
        List<FluidHatchPartMachine> outputs = new ArrayList<>();
        for (IMultiPart part : getParts()) {
            if (part.self() instanceof FluidHatchPartMachine hatch) {
                outputs.add(hatch);
            }
        }
        if (outputs.isEmpty()) {
            return;
        }

        long voltage = Math.max(1, energy.energyContainer.getInputVoltage());
        if (energy.energyContainer.getEnergyStored() < voltage) {
            return;
        }
        int euTier = Math.max(1, (int) Math.ceil(Math.log((double) voltage / 32) / Math.log(4) + 1));

        float vis = 0;
        BlockPos nodePos = findNode(level);
        if (nodePos != null && level.getBlockEntity(nodePos) instanceof AuraNodeBlockEntity node) {
            vis = total(node.nodeState().currentVis());
        }
        float flux = (float) PollutionEngine.get(level, getPos());
        int speed = calculateSpeed(vis, flux, euTier);
        if (speed <= 0) {
            return;
        }

        energy.energyContainer.changeEnergy(-voltage);
        writeFluids(outputs, speed);
    }

    private int calculateSpeed(float vis, float flux, int euTier) {
        if (vis <= 0 || flux >= vis) {
            return 0;
        }
        double ratio = flux / vis;
        double speedFactor = Math.log(vis) - Math.log(1 + ratio);
        if (speedFactor <= 0) {
            return 0;
        }
        int coilLevel = hasCoil() ? Math.max(1, getCoilLevel()) : 0;
        return (int) Math.ceil(BASIC_SPEED_PER_TICK * (1 + coilLevel) * Math.pow(2, euTier) * speedFactor);
    }

    private void writeFluids(List<FluidHatchPartMachine> outputs, int amount) {
        FluidStack[] fluids = {
                PollutionMaterials.InfusedAir.getFluid(amount),
                PollutionMaterials.InfusedFire.getFluid(amount),
                PollutionMaterials.InfusedEarth.getFluid(amount),
                PollutionMaterials.InfusedWater.getFluid(amount),
                PollutionMaterials.InfusedOrder.getFluid(amount),
                PollutionMaterials.InfusedEntropy.getFluid(amount),
        };
        for (FluidStack fluid : fluids) {
            if (fluid.isEmpty()) {
                continue;
            }
            for (FluidHatchPartMachine hatch : outputs) {
                if (hatch.tank.fill(fluid, IFluidHandler.FluidAction.EXECUTE) > 0) {
                    break;
                }
            }
        }
    }

    private BlockPos findNode(ServerLevel level) {
        for (BlockPos pos : BlockPos.betweenClosed(
                getPos().offset(-NODE_RADIUS, -NODE_RADIUS, -NODE_RADIUS),
                getPos().offset(NODE_RADIUS, NODE_RADIUS, NODE_RADIUS))) {
            if (level.getBlockEntity(pos) instanceof AuraNodeBlockEntity) {
                return pos.immutable();
            }
        }
        return null;
    }

    private static int total(NodeVis vis) {
        int sum = 0;
        for (Integer amount : vis.amounts().values()) {
            if (amount != null && amount > 0) {
                sum += amount;
            }
        }
        return sum;
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
        return EssenceCollectorPatterns.create(definition);
    }
}
