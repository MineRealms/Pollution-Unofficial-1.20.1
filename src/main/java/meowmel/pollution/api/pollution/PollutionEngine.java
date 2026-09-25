package meowmel.pollution.api.pollution;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import meowmel.pollution.PollutionConfig;
import meowmel.pollution.common.network.PollutionNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Server side facade for reading, adding and scrubbing industrial pollution.
 */
public final class PollutionEngine {

    private static final int DECAY_INTERVAL_TICKS = 200;
    private static final int EFFECT_INTERVAL_TICKS = 40;
    private static final int EFFECT_DURATION_TICKS = 100;
    /** Additional effect bands are expressed as multiples of effectThreshold. */
    private static final double WEAKNESS_BAND = 2.0D;
    private static final double MINING_FATIGUE_BAND = 3.0D;
    private static final double BLINDNESS_BAND = 4.0D;

    private static int decayTimer;

    /** Cached per-dimension candidate chunks for terrain conversion, refreshed on the decay interval. */
    private static final Map<ResourceKey<Level>, LongArrayList> CONVERSION_CANDIDATES = new HashMap<>();
    private static int conversionRefreshTimer;

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
        MinecraftServer server = event.getServer();
        convertTerrain(server);
        if (server.getTickCount() % EFFECT_INTERVAL_TICKS == 0) {
            applyPlayerEffects(server);
        }
        if (++decayTimer < DECAY_INTERVAL_TICKS) {
            return;
        }
        decayTimer = 0;
        double decay = PollutionConfig.POLLUTION_DECAY_PER_TICK.get() * DECAY_INTERVAL_TICKS;
        if (decay <= 0.0D) {
            return;
        }
        for (ServerLevel level : event.getServer().getAllLevels()) {
            PollutionData.get(level).decayAll(decay);
        }
    }

    /**
     * Environmental conversion of heavy pollution (tracker Phase 1: 草→沙、水→岩浆，
     * 带预算限流). Random sampling over a cached list of heavily polluted chunks,
     * capped by a per-dimension per-tick conversion budget (tracker §8 red lines).
     */
    private static void convertTerrain(MinecraftServer server) {
        if (!PollutionConfig.ENABLE_POLLUTION.get() || !PollutionConfig.ENABLE_TERRAIN_CONVERSION.get()) {
            return;
        }
        int budget = PollutionConfig.TERRAIN_CONVERSION_BUDGET_PER_TICK.get();
        if (budget <= 0) {
            return;
        }
        double threshold = PollutionConfig.TERRAIN_CONVERSION_THRESHOLD.get();
        if (++conversionRefreshTimer >= DECAY_INTERVAL_TICKS) {
            conversionRefreshTimer = 0;
            CONVERSION_CANDIDATES.clear();
            for (ServerLevel level : server.getAllLevels()) {
                LongArrayList chunks = PollutionData.get(level).collectChunksAbove(threshold);
                if (!chunks.isEmpty()) {
                    CONVERSION_CANDIDATES.put(level.dimension(), chunks);
                }
            }
        }
        if (CONVERSION_CANDIDATES.isEmpty()) {
            return;
        }
        for (ServerLevel level : server.getAllLevels()) {
            LongArrayList chunks = CONVERSION_CANDIDATES.get(level.dimension());
            if (chunks == null || chunks.isEmpty()) {
                continue;
            }
            RandomSource random = level.getRandom();
            int converted = 0;
            for (int attempt = 0; attempt < budget * 4 && converted < budget; attempt++) {
                if (convertOne(level, new ChunkPos(chunks.getLong(random.nextInt(chunks.size()))), random)) {
                    converted++;
                }
            }
        }
    }

    /** Samples one random surface column in the chunk; returns true if a block was converted. */
    private static boolean convertOne(ServerLevel level, ChunkPos chunkPos, RandomSource random) {
        if (PollutionData.get(level).get(chunkPos) < PollutionConfig.TERRAIN_CONVERSION_THRESHOLD.get()) {
            return false;
        }
        if (level.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z) == null) {
            return false;
        }
        int x = chunkPos.getMinBlockX() + random.nextInt(16);
        int z = chunkPos.getMinBlockZ() + random.nextInt(16);
        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z) - 1;
        if (surfaceY <= level.getMinBuildHeight()) {
            return false;
        }
        BlockPos pos = new BlockPos(x, surfaceY, z);
        var state = level.getBlockState(pos);
        if (state.is(Blocks.GRASS_BLOCK)) {
            // Kill the vegetation above first, then desertify the grass block.
            BlockPos above = pos.above();
            var aboveState = level.getBlockState(above);
            if (aboveState.is(Blocks.TALL_GRASS) || aboveState.is(Blocks.FERN)) {
                level.setBlockAndUpdate(above, Blocks.DEAD_BUSH.defaultBlockState());
            }
            level.setBlockAndUpdate(pos, Blocks.SAND.defaultBlockState());
            return true;
        }
        if (state.getFluidState().is(FluidTags.WATER) && state.getFluidState().isSource()
                && PollutionData.get(level).get(chunkPos) >= 2.0D * PollutionConfig.TERRAIN_CONVERSION_THRESHOLD.get()) {
            level.setBlockAndUpdate(pos, Blocks.LAVA.defaultBlockState());
            return true;
        }
        return false;
    }

    /** Clears static scheduler state when a server instance ends (including integrated worlds). */
    public static void resetCaches() {
        decayTimer = 0;
        conversionRefreshTimer = 0;
        CONVERSION_CANDIDATES.clear();
    }

    /**
     * Applies the configured harmful effect to players standing in chunks whose
     * pollution exceeds {@link PollutionConfig#EFFECT_THRESHOLD}.
     */
    public static void applyPlayerEffects(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            for (var player : level.players()) applyPlayerEffects(player);
        }
    }

    /** Apply one server-authoritative exposure sample; also usable for simulated players. */
    public static void applyPlayerEffects(net.minecraft.server.level.ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        boolean enabled = PollutionConfig.ENABLE_POLLUTION.get();
        double threshold = PollutionConfig.EFFECT_THRESHOLD.get();
        double pollution = enabled ? get(level, player.blockPosition()) : 0.0D;
        PollutionNetwork.send(player, pollution, threshold);
        boolean polluted = pollution > threshold;
        if (polluted) {
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.CONFUSION, EFFECT_DURATION_TICKS, 0, true, false));
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.HUNGER, EFFECT_DURATION_TICKS, 0, true, false));

            // Preserve the upstream baseline nausea/hunger while
            // making the documented pollution gradient functional:
            // 2x adds weakness, 3x adds mining fatigue, and 4x adds
            // blindness. Amplifiers rise one step per additional band
            // and are capped so extreme chunks cannot create an
            // unbounded effect level.
            double ratio = exposureRatio(pollution, threshold);
            if (ratio >= WEAKNESS_BAND) {
                int amplifier = Math.min(2, Math.max(0, (int) (ratio / WEAKNESS_BAND) - 1));
                player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.WEAKNESS,
                        EFFECT_DURATION_TICKS, amplifier, true, false));
            }
            if (ratio >= MINING_FATIGUE_BAND) {
                int amplifier = Math.min(2, Math.max(0, (int) (ratio / MINING_FATIGUE_BAND) - 1));
                player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.DIG_SLOWDOWN,
                        EFFECT_DURATION_TICKS, amplifier, true, false));
            }
            if (ratio >= BLINDNESS_BAND) {
                int amplifier = Math.min(2, Math.max(0, (int) (ratio / BLINDNESS_BAND) - 1));
                player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.BLINDNESS,
                        EFFECT_DURATION_TICKS, amplifier, true, false));
            }
            if (!player.getPersistentData().getBoolean("pollution.warned")) {
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable("pollution.effect.warning"), true);
                player.getPersistentData().putBoolean("pollution.warned", true);
            }
        } else {
            player.getPersistentData().remove("pollution.warned");
        }
    }

    /** Shared by the server effects and HUD, including a configured zero threshold. */
    public static double exposureRatio(double pollution, double threshold) {
        if (pollution <= 0.0D) return 0.0D;
        return threshold <= 0.0D ? Double.POSITIVE_INFINITY : pollution / threshold;
    }
}
