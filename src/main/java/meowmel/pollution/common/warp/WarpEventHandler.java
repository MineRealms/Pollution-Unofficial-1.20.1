package meowmel.pollution.common.warp;

import meowmel.pollution.PollutionConfig;
import meowmel.pollution.api.pollution.PollutionEngine;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;

/**
 * Server-side warp event scheduler.
 *
 * <p>At the configured interval each player rolls for a warp event; the chance is
 * {@code min(25%, warp / 100)} where warp is permanent + sticky + temporary
 * from the TC4R warp view (the same source the upstream queue used).</p>
 */
public final class WarpEventHandler {

    private WarpEventHandler() {}

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        var server = event.getServer();
        PollutionWarpEvents.tickCountdowns(server);
        int interval = PollutionConfig.WARP_EVENT_INTERVAL_TICKS.get();
        if (interval > 0 && server.getTickCount() % interval == 0) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                PollutionWarpEvents.tick(player);
            }
        }
        // Warp exposure is the TC4R input and remains on the upstream 10-second cadence.
        if (server.getTickCount() % 200 != 0) return;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            FluxWarpManager.expose(player);
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        PollutionWarpEvents.clearPendingEvents();
        PollutionEngine.resetCaches();
    }
}
