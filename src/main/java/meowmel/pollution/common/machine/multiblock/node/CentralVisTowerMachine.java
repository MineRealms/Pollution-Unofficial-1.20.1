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
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.List;

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
 *
 * <p>Starry Mansus: upstream produced it once per 400 tick cleaning cycle when
 * every controlled chunk was balanced ({@code flux + (vis - base) <= 4.2}),
 * at {@code goodChunkAmount × manaConsumptionSpeed} mB, where the coverage is
 * {@code (2 × frameLevel - 1)²} chunks and
 * {@code manaConsumptionSpeed = 4 × 2^(hatchLevel - 2)}. The port keeps that
 * rate and condition: the tower's control area is the containment frame
 * coverage (frame level 1..4 -> 9/9/25/49 chunks), the aura node supplies the
 * vis difference and the scrubbed flux the pollution term, and the mana hatch
 * level becomes the energy hatch tier.</p>
 */
public class CentralVisTowerMachine extends AbstractDisplayMultiblockMachine {

    /** Match-context key written by {@link CentralVisTowerPatterns#frameGroup()}. */
    public static final String FRAME_LEVEL_KEY = "CentralVisTowerFrameLevel";

    private static final int NODE_RADIUS = 8;
    /** Upstream cleaning period: one Starry Mansus batch every 20 seconds. */
    private static final int CLEANING_PERIOD = 400;

    private TickableSubscription tickSubscription;
    private int starryTimer;

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
            tickStarryProduction(level, energy, input, voltage, upkeep, nodePos);
            return;
        }

        starryTimer = 0;
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

    /**
     * Upstream Starry Mansus bonus ("Tian Xuan essence"): no Light/Dark work
     * was available, the tower still consumed mana and, once per cleaning
     * period, emitted Starry Mansus if every controlled chunk was balanced.
     * The port advances the 400 tick timer only while the node/area is clean
     * (node vis at most 4.2 above base, no scrubable flux) and pays the usual
     * EU + {@code InfusedAura} upkeep for the cycle.
     */
    private void tickStarryProduction(ServerLevel level, EnergyHatchPartMachine energy,
                                      FluidHatchPartMachine input, long voltage,
                                      FluidStack upkeep, BlockPos nodePos) {
        if (!isAreaBalanced(level, nodePos)) {
            starryTimer = 0;
            return;
        }
        energy.energyContainer.changeEnergy(-voltage);
        if (!upkeep.isEmpty()) {
            input.tank.drain(upkeep, IFluidHandler.FluidAction.EXECUTE);
        }
        starryTimer += 20;
        if (starryTimer < CLEANING_PERIOD) {
            return;
        }
        starryTimer = 0;
        int manaConsumptionSpeed = (int) (4 * Math.pow(2, Math.max(0, energyTier(voltage) - 2)));
        int goodChunks = coveredChunks(getFrameLevel());
        fillOutputFluid(PollutionMaterials.Starrymansus.getFluid(goodChunks * manaConsumptionSpeed));
    }

    /**
     * Port equivalent of upstream's good-chunk test {@code flux + (vis - base)
     * <= 4.2}: the scrubable flux quanta around the tower plus the node's vis
     * above its base level.
     */
    private boolean isAreaBalanced(ServerLevel level, BlockPos nodePos) {
        float amount = 0;
        if (nodePos != null && level.getBlockEntity(nodePos) instanceof AuraNodeBlockEntity node) {
            AuraNodeState state = node.nodeState();
            amount = total(state.currentVis()) - total(state.baseVis());
        }
        amount += TC4RBridge.scrubFlux(level, getPos(), 1, VisAction.SIMULATE);
        return amount <= 4.2F;
    }

    /** Containment frame level (1..4) recorded when the structure formed. */
    private int getFrameLevel() {
        Integer level = getMultiblockState().getMatchContext().get(FRAME_LEVEL_KEY);
        return level == null ? 1 : Math.max(1, level);
    }

    /** Covered chunks of a frame level: radius {@code max(1, level - 1)}. */
    private static int coveredChunks(int frameLevel) {
        int radius = Math.max(1, frameLevel - 1);
        return (2 * radius + 1) * (2 * radius + 1);
    }

    private static int energyTier(long voltage) {
        return Math.max(1, (int) Math.ceil(Math.log((double) voltage / 32) / Math.log(4) + 1));
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

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (isFormed()) {
            textList.add(Component.literal("Containment Frame Level: " + getFrameLevel()));
            textList.add(Component.literal("Starry Mansus Progress: " + starryTimer + " / " + CLEANING_PERIOD));
        }
    }

    public static BlockPattern createPattern(MultiblockMachineDefinition definition) {
        return CentralVisTowerPatterns.create(definition);
    }
}
