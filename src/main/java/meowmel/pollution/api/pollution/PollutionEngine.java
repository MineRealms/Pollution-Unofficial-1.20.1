package meowmel.pollution.api.pollution;

import meowmel.pollution.PollutionConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;

/**
 * Server side facade for reading, adding and scrubbing industrial pollution.
 */
public final class PollutionEngine {

    private static final int DECAY_INTERVAL_TICKS = 200;

    private static int decayTimer;

    private PollutionEngine() {}

    public static double get(ServerLevel level, BlockPos pos) {
        return PollutionData.get(level).get(new net.minecraft.world.level.ChunkPos(pos));
    }

    public static double add(ServerLevel level, BlockPos pos, double amount) {
        if (!PollutionConfig.ENABLE_POLLUTION.get() || amount <= 0.0D) {
            return get(level, pos);
        }
        return PollutionData.get(level).add(new net.minecraft.world.level.ChunkPos(pos), amount);
    }

    public static double set(ServerLevel level, BlockPos pos, double amount) {
        return PollutionData.get(level).set(new net.minecraft.world.level.ChunkPos(pos), amount);
    }

    public static double scrub(ServerLevel level, BlockPos pos, double amount) {
        return PollutionData.get(level).scrub(new net.minecraft.world.level.ChunkPos(pos), amount);
    }

    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (++decayTimer < DECAY_INTERVAL_TICKS) {
            return;
        }
        decayTimer = 0;
        applyPlayerEffects(event.getServer());
        double decay = PollutionConfig.POLLUTION_DECAY_PER_TICK.get() * DECAY_INTERVAL_TICKS;
        if (decay <= 0.0D) {
            return;
        }
        for (ServerLevel level : event.getServer().getAllLevels()) {
            PollutionData.get(level).decayAll(decay);
        }
    }

    /**
     * Applies the configured harmful effect to players standing in chunks whose
     * pollution exceeds {@link PollutionConfig#EFFECT_THRESHOLD}.
     */
    public static void applyPlayerEffects(net.minecraft.server.MinecraftServer server) {
        if (!PollutionConfig.ENABLE_POLLUTION.get()) {
            return;
        }
        double threshold = PollutionConfig.EFFECT_THRESHOLD.get();
        for (ServerLevel level : server.getAllLevels()) {
            for (net.minecraft.server.level.ServerPlayer player : level.players()) {
                boolean polluted = get(level, player.blockPosition()) > threshold;
                if (polluted) {
                    player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                            net.minecraft.world.effect.MobEffects.CONFUSION, 100, 0, true, false));
                    player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                            net.minecraft.world.effect.MobEffects.HUNGER, 100, 0, true, false));
                    if (!player.getPersistentData().getBoolean("pollution.warned")) {
                        player.displayClientMessage(
                                net.minecraft.network.chat.Component.translatable("pollution.effect.warning"), true);
                        player.getPersistentData().putBoolean("pollution.warned", true);
                    }
                } else {
                    player.getPersistentData().remove("pollution.warned");
                }
            }
        }
    }
}
