package meowmel.pollution.common.machine.single;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
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
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import java.util.LinkedHashMap;
import java.util.List;
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

    public VisProviderMachine(IMachineBlockEntity info, int tier) {
        super(info, tier);
    }

    @Override
    protected void pollutionTick() {
        if (!(getLevel() instanceof ServerLevel level)) {
            return;
        }
        long cost = GTValues.V[getTier()];
        if (energyContainer.getEnergyStored() < cost || PollutionConfig.VIS_PROVIDER_MULTIPLIER.get() <= 0.0D) {
            return;
        }
        if (--scanTimer <= 0) {
            nodePos = findNode(level);
            scanTimer = SCAN_INTERVAL;
        }
        if (nodePos == null) {
            return;
        }
        AuraNodeState state = readState(level, nodePos);
        if (state == null) {
            nodePos = null;
            scanTimer = 0;
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
            nodePos = null;
            scanTimer = 0;
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
        BlockPos origin = getPos();
        BlockPos nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(
                origin.offset(-SCAN_RADIUS, -SCAN_RADIUS, -SCAN_RADIUS),
                origin.offset(SCAN_RADIUS, SCAN_RADIUS, SCAN_RADIUS))) {
            if (!level.hasChunkAt(pos)) {
                continue;
            }
            if (level.getBlockState(pos).is(TCBlocks.AURA_NODE.get()) && origin.distSqr(pos) < nearestDistance) {
                AuraNodeState state = readState(level, pos);
                if (state != null && state.baseVis().amounts().entrySet().stream()
                        .anyMatch(entry -> entry.getValue() > state.currentVis().amount(entry.getKey()))) {
                    nearest = pos.immutable();
                    nearestDistance = origin.distSqr(pos);
                }
            }
        }
        return nearest;
    }

    private static AuraNodeState readState(ServerLevel level, BlockPos pos) {
        if (level.hasChunkAt(pos) && level.getBlockEntity(pos) instanceof AuraNodeBlockEntity node) {
            return node.nodeState();
        }
        return null;
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        textList.add(Component.literal("Target Node: "
                + (nodePos == null ? "none" : nodePos.toShortString())));
    }
}
