package meowmel.pollution.client;

import meowmel.pollution.Pollution;
import meowmel.pollution.api.pollution.PollutionEngine;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Client copy of the last server-authoritative local chunk pollution value. */
@Mod.EventBusSubscriber(modid = Pollution.MOD_ID, value = Dist.CLIENT)
public final class PollutionClientState {

    private static double amount;
    private static double threshold;
    private static ResourceLocation dimension;
    private static long chunk;

    private PollutionClientState() {}

    public static void update(ResourceLocation newDimension, long newChunk, double newAmount, double newThreshold) {
        dimension = newDimension;
        chunk = newChunk;
        amount = Double.isFinite(newAmount) ? Math.max(0.0D, newAmount) : 0.0D;
        threshold = Double.isFinite(newThreshold) ? Math.max(0.0D, newThreshold) : 0.0D;
    }

    public static boolean isCurrent() {
        var minecraft = Minecraft.getInstance();
        return minecraft.level != null && minecraft.player != null
                && minecraft.level.dimension().location().equals(dimension)
                && minecraft.player.chunkPosition().toLong() == chunk;
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        dimension = null;
        amount = 0.0D;
        threshold = 0.0D;
    }

    public static double amount() {
        return amount;
    }

    public static double threshold() {
        return threshold;
    }

    public static double ratio() {
        return PollutionEngine.exposureRatio(amount, threshold);
    }
}
