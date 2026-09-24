package meowmel.pollution.client.warp;

import meowmel.pollution.Pollution;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Upstream blue/red fake rain; local particles never change server weather. */
@Mod.EventBusSubscriber(modid = Pollution.MOD_ID, value = Dist.CLIENT)
public final class ClientWarpEffects {
    private static ResourceLocation dimension;
    private static int rainLevel;
    private static int ticksLeft;

    public static void rain(ResourceLocation targetDimension, int level, int ticks) {
        dimension = targetDimension;
        rainLevel = Math.max(0, Math.min(2, level));
        ticksLeft = Math.max(0, Math.min(1200, ticks));
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        var client = Minecraft.getInstance();
        var world = client.level;
        var player = client.player;
        if (world == null || player == null || !world.dimension().location().equals(dimension)) {
            ticksLeft = 0;
            dimension = null;
            return;
        }
        if (client.isPaused() || ticksLeft <= 0 || rainLevel == 0) return;
        ticksLeft--;
        var random = world.random;
        world.addParticle(rainLevel >= 2 ? ParticleTypes.DRIPPING_LAVA : ParticleTypes.RAIN,
                player.getX() + (random.nextDouble() - 0.5) * 12,
                player.getY() + random.nextDouble() * 6 + 1,
                player.getZ() + (random.nextDouble() - 0.5) * 12, 0, 0, 0);
    }

    private ClientWarpEffects() {}
}
