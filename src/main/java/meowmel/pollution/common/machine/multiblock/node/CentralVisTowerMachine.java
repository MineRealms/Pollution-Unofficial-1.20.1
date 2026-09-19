package meowmel.pollution.common.machine.multiblock.node;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.common.machine.multiblock.part.EnergyHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import dev.tc4port.thaumcraft.api.aspect.VisAction;
import dev.tc4port.thaumcraft.api.node.AuraNodeState;
import dev.tc4port.thaumcraft.api.node.NodeApi;
import dev.tc4port.thaumcraft.api.node.NodeVis;
import dev.tc4port.thaumcraft.api.node.NodeStateChangeResult;
import dev.tc4port.thaumcraft.block.entity.AuraNodeBlockEntity;
import meowmel.pollution.api.unification.PollutionMaterials;
import meowmel.pollution.common.machine.multiblock.AbstractDisplayMultiblockMachine;
import meowmel.pollution.compat.tc4r.TC4RBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

/**
 * Central vis tower.
 *
 * <p>Upstream harvested TC6 ambient chunk aura: it scrubbed flux and drained
 * vis above the chunk's base level, turning both into Mansus fluids while
 * consuming Botania mana. TC4R has no ambient aura, so the port rewrites the
 * machine around an aura node inside its radius:</p>
 * <ul>
 *   <li>excess node vis (current above base) is drained via the
 *       {@code NodeApi} compare-and-set and emitted as {@code InfusedLight}
 *       (the White Mansus mapping);</li>
 *   <li>scrubbed flux is emitted as {@code InfusedDark} (Black Mansus);</li>
 *   <li>the Botania mana upkeep becomes EU plus a small
 *       {@code InfusedAura} cost.</li>
 * </ul>
 * <p>The Starry Mansus "all chunks clean" bonus has no direct equivalent and
 * is not produced (documented deviation).</p>
 */
public class CentralVisTowerMachine extends AbstractDisplayMultiblockMachine {

    private static final int NODE_RADIUS = 8;

    private TickableSubscription tickSubscription;

    public CentralVisTowerMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            tickSubscription = subscribeServerTick(this::tickTower);
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

    private void tickTower() {
        if (!(getLevel() instanceof ServerLevel level) || !isFormed()) {
            return;
        }
        if (getOffsetTimer() % 20 != 0) {
            return;
        }
        EnergyHatchPartMachine energy = findPart(EnergyHatchPartMachine.class);
        FluidHatchPartMachine input = findFluidHatch(PartAbility.IMPORT_FLUIDS);
        if (energy == null || input == null) {
            return;
        }
        long voltage = Math.max(1, energy.energyContainer.getInputVoltage());
        if (energy.energyContainer.getEnergyStored() < voltage) {
            return;
        }
        FluidStack upkeep = PollutionMaterials.InfusedAura.getFluid(4);
        if (!upkeep.isEmpty() &&
                input.tank.drain(upkeep, IFluidHandler.FluidAction.SIMULATE).getAmount() < 4) {
            return;
        }

        BlockPos nodePos = findNode(level);
        int excess = 0;
        if (nodePos != null) {
            excess = drainNodeExcess(level, nodePos);
        }
        int scrubbed = TC4RBridge.scrubFlux(level, getPos(), 1);
        if (excess <= 0 && scrubbed <= 0) {
            return;
        }

        energy.energyContainer.changeEnergy(-voltage);
        if (!upkeep.isEmpty()) {
            input.tank.drain(upkeep, IFluidHandler.FluidAction.EXECUTE);
        }
        if (excess > 0) {
            fillOutputFluid(PollutionMaterials.InfusedLight.getFluid(excess * 10));
        }
        if (scrubbed > 0) {
            fillOutputFluid(PollutionMaterials.InfusedDark.getFluid(scrubbed * 10));
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

    private int drainNodeExcess(ServerLevel level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof AuraNodeBlockEntity node)) {
            return 0;
        }
        AuraNodeState state = node.nodeState();
        int base = total(state.baseVis());
        int current = total(state.currentVis());
        if (current <= base) {
            return 0;
        }
        NodeStateChangeResult result = NodeApi.replaceLoadedState(level, pos, state,
                state.withCurrentVis(state.baseVis()), VisAction.EXECUTE);
        return result.successful() ? current - base : 0;
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

    private void fillOutputFluid(FluidStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        for (IMultiPart part : getParts()) {
            if (part.self() instanceof FluidHatchPartMachine hatch
                    && PartAbility.EXPORT_FLUIDS.isApplicable(hatch.getBlockState().getBlock())) {
                hatch.tank.fill(stack, IFluidHandler.FluidAction.EXECUTE);
            }
        }
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

    private <T> T findPart(Class<T> type) {
        for (IMultiPart part : getParts()) {
            if (type.isInstance(part.self())) {
                return type.cast(part.self());
            }
        }
        return null;
    }

    public static BlockPattern createPattern(MultiblockMachineDefinition definition) {
        return CentralVisTowerPatterns.create(definition);
    }
}
