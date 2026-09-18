package meowmel.pollution.common.machine.single;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import dev.tc4port.thaumcraft.api.aspect.AspectId;
import dev.tc4port.thaumcraft.api.aspect.VisAction;
import dev.tc4port.thaumcraft.api.node.AuraNodeState;
import dev.tc4port.thaumcraft.api.node.NodeApi;
import dev.tc4port.thaumcraft.api.node.NodeStateChangeResult;
import dev.tc4port.thaumcraft.api.node.NodeVis;
import dev.tc4port.thaumcraft.block.entity.AuraNodeBlockEntity;
import dev.tc4port.thaumcraft.registry.TCBlocks;
import meowmel.pollution.PollutionConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Vis provider: consumes EU to recharge the nearest loaded aura node.
 *
 * <p>Upstream ({@code MetaTileEntityVisProvider}) added vis to the ambient TC6
 * aura through {@code AuraHelper.addVis} while the aura was below 400. TC4R has
 * no ambient aura; vis lives inside aura nodes and can only be modified through
 * the compare-and-set API {@code NodeApi.replaceLoadedState}. The port therefore
 * scans for a loaded ordinary aura node in a small radius and adds
 * {@code V[tier] * visProviderMultiplier} centivis to the node aspect with the
 * most free base capacity, paying {@code V[tier]} EU per operation.</p>
 */
public class VisProviderMachine extends PollutionEnergyMachine {

    private static final int SCAN_RADIUS = 8;
    private static final int SCAN_INTERVAL = 60;

    private BlockPos nodePos;
    private int scanTimer;

    public VisProviderMachine(BlockEntityCreationInfo info, int tier) {
        super(info, tier);
    }

    @Override
    protected void pollutionTick() {
        if (!(getLevel() instanceof ServerLevel level)) {
            return;
        }
        long cost = GTValues.V[getTier()];
        if (energyContainer.getEnergyStored() < cost) {
            return;
        }
        if (nodePos == null || --scanTimer <= 0) {
            nodePos = findNode(level);
            scanTimer = SCAN_INTERVAL;
        }
        if (nodePos == null) {
            return;
        }
        AuraNodeState state = readState(level, nodePos);
        if (state == null) {
            nodePos = null;
            return;
        }

        AspectId target = null;
        int targetCurrent = 0;
        int targetRoom = 0;
        for (Map.Entry<AspectId, Integer> entry : state.baseVis().amounts().entrySet()) {
            int current = state.currentVis().amount(entry.getKey());
            int room = entry.getValue() - current;
            if (room > targetRoom) {
                target = entry.getKey();
                targetCurrent = current;
                targetRoom = room;
            }
        }
        if (target == null || targetRoom <= 0) {
            return;
        }

        int step = (int) Math.max(1.0D, cost * PollutionConfig.VIS_PROVIDER_MULTIPLIER.get());
        int added = Math.min(step, targetRoom);
        Map<AspectId, Integer> amounts = new LinkedHashMap<>(state.currentVis().amounts());
        amounts.put(target, targetCurrent + added);
        NodeStateChangeResult result = NodeApi.replaceLoadedState(level, nodePos, state,
                state.withCurrentVis(new NodeVis(amounts)), VisAction.EXECUTE);
        if (result.successful()) {
            energyContainer.removeEnergy(cost);
        }
    }

    private BlockPos findNode(ServerLevel level) {
        BlockPos origin = getBlockPos();
        for (BlockPos pos : BlockPos.betweenClosed(
                origin.offset(-SCAN_RADIUS, -SCAN_RADIUS, -SCAN_RADIUS),
                origin.offset(SCAN_RADIUS, SCAN_RADIUS, SCAN_RADIUS))) {
            if (!level.hasChunkAt(pos)) {
                continue;
            }
            if (level.getBlockState(pos).is(TCBlocks.AURA_NODE.get())) {
                return pos.immutable();
            }
        }
        return null;
    }

    private static AuraNodeState readState(ServerLevel level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof AuraNodeBlockEntity node) {
            return node.nodeState();
        }
        return null;
    }
}
