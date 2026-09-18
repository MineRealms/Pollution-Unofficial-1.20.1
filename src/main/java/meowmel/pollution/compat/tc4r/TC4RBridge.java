package meowmel.pollution.compat.tc4r;

import dev.tc4port.thaumcraft.api.aspect.VisAction;
import dev.tc4port.thaumcraft.api.aspect.VisChannel;
import dev.tc4port.thaumcraft.api.flux.FluxApi;
import dev.tc4port.thaumcraft.api.flux.FluxConsumeContext;
import dev.tc4port.thaumcraft.api.flux.FluxConsumeResult;
import dev.tc4port.thaumcraft.api.node.VisNetworkApi;
import dev.tc4port.thaumcraft.api.player.PlayerWarpApi;
import dev.tc4port.thaumcraft.api.player.PlayerWarpView;
import meowmel.pollution.Pollution;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

/**
 * Single integration point for Thaumcraft 4R. Keep every TC4R class reference here
 * so API changes only require editing this file.
 */
public final class TC4RBridge {

    public static final int FLUX_RANGE = 16;

    private static final ResourceLocation MACHINE_SOURCE =
            ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID, "pollution_machine");

    private TC4RBridge() {}

    /** Drains vis from the TC4R network around the given position. Returns the amount actually drained. */
    public static int drainVis(ServerLevel level, BlockPos pos, VisChannel channel, int amount) {
        if (amount <= 0) {
            return 0;
        }
        return VisNetworkApi.drain(level, pos, channel, amount, VisAction.EXECUTE);
    }

    /** Consumes flux goo/gas around the given position. Returns the amount actually consumed. */
    public static int scrubFlux(ServerLevel level, BlockPos pos, int quanta) {
        if (quanta <= 0) {
            return 0;
        }
        FluxConsumeResult result = FluxApi.consumeNearby(level, pos, FLUX_RANGE, quanta,
                FluxConsumeContext.machine(MACHINE_SOURCE, pos), VisAction.EXECUTE);
        return result.consumedQuanta();
    }

    /** Read only view of a player's Thaumcraft 4R warp values. */
    public static PlayerWarpView warpOf(Player player) {
        return PlayerWarpApi.view(player);
    }
}
