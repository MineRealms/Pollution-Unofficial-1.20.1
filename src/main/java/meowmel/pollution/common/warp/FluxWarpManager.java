package meowmel.pollution.common.warp;

import meowmel.pollution.compat.tc4r.TC4RBridge;
import net.minecraft.server.level.ServerPlayer;

/**
 * Flux warp manager (semantic port).
 *
 * <p>Upstream converted long-term flux exposure into permanent warp. The TC4R
 * warp view is read-only, so the port cannot add warp points; instead players
 * standing in heavy flux are exposed to warp-like events directly: every
 * scheduler pass the local scrubbable flux is measured and, above the
 * threshold, a random warp event is rolled for the player.</p>
 */
public final class FluxWarpManager {

    private static final int FLUX_THRESHOLD = 8;

    private FluxWarpManager() {}

    /** @return true when the player was flux-exposed this pass */
    public static boolean expose(ServerPlayer player) {
        if (!(player.level() instanceof net.minecraft.server.level.ServerLevel level)) {
            return false;
        }
        int flux = TC4RBridge.scrubFlux(level, player.blockPosition(), 1,
                dev.tc4port.thaumcraft.api.aspect.VisAction.SIMULATE);
        if (flux < FLUX_THRESHOLD) {
            return false;
        }
        double chance = Math.min(0.25, flux / 100.0);
        if (player.getRandom().nextDouble() >= chance) {
            return false;
        }
        return PollutionWarpEvents.tick(player);
    }
}
