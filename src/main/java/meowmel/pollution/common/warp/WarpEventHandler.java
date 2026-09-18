package meowmel.pollution.common.warp;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Server-side warp event scheduler.
 *
 * <p>Every ten seconds each player rolls for a warp event; the chance is
 * {@code min(25%, warp / 100)} where warp is permanent + sticky + temporary
 * from the TC4R warp view (the same source the upstream queue used).</p>
 */
public final class WarpEventHandler {

    private static final int INTERVAL_TICKS = 200;

    private WarpEventHandler() {}

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        var server = event.getServer();
        if (server.getTickCount() % INTERVAL_TICKS != 0) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            PollutionWarpEvents.tick(player);
        }
    }
}
