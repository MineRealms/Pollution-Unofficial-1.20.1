package meowmel.pollution.compat.kubejs;

import meowmel.pollution.api.pollution.PollutionData;
import meowmel.pollution.api.pollution.PollutionEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

/**
 * Script-facing pollution helpers, bound to scripts as {@code Pollution}
 * (server scripts only).
 *
 * <p>All methods are static and operate on the server level passed in from the
 * script; client levels return {@code 0}. Mutating helpers call through to
 * {@link PollutionEngine} and post
 * {@link PollutionEvents#postChunkChanged} so script-driven changes are
 * observable by other scripts.</p>
 */
public final class PollutionJS {

    private PollutionJS() {}

    /** Pollution of the chunk containing {@code (x, z)}. */
    public static double get(Level level, int x, int z) {
        if (!(level instanceof ServerLevel server)) {
            return 0.0D;
        }
        return PollutionEngine.get(server, new BlockPos(x, 0, z));
    }

    /** Adds {@code amount} pollution to the chunk containing {@code (x, z)}. Returns the new value. */
    public static double add(Level level, int x, int z, double amount) {
        if (!(level instanceof ServerLevel server)) {
            return 0.0D;
        }
        BlockPos blockPos = new BlockPos(x, 0, z);
        ChunkPos chunkPos = new ChunkPos(blockPos);
        double previous = PollutionData.get(server).get(chunkPos);
        double updated = PollutionEngine.add(server, blockPos, amount);
        PollutionEvents.postChunkChanged(server, chunkPos, previous, updated, "script");
        return updated;
    }

    /** Sets the chunk pollution to {@code amount}. Returns the new value. */
    public static double set(Level level, int x, int z, double amount) {
        if (!(level instanceof ServerLevel server)) {
            return 0.0D;
        }
        BlockPos blockPos = new BlockPos(x, 0, z);
        ChunkPos chunkPos = new ChunkPos(blockPos);
        double previous = PollutionData.get(server).get(chunkPos);
        double updated = PollutionEngine.set(server, blockPos, amount);
        PollutionEvents.postChunkChanged(server, chunkPos, previous, updated, "script");
        return updated;
    }

    /** Scrubs up to {@code amount} pollution from the chunk. Returns the amount removed. */
    public static double scrub(Level level, int x, int z, double amount) {
        if (!(level instanceof ServerLevel server)) {
            return 0.0D;
        }
        BlockPos blockPos = new BlockPos(x, 0, z);
        ChunkPos chunkPos = new ChunkPos(blockPos);
        double previous = PollutionData.get(server).get(chunkPos);
        double removed = PollutionEngine.scrub(server, blockPos, amount);
        PollutionEvents.postChunkChanged(server, chunkPos, previous, previous - removed, "script");
        return removed;
    }

    /** Number of chunks currently holding a non-zero pollution value in this level. */
    public static int pollutedChunkCount(Level level) {
        if (!(level instanceof ServerLevel server)) {
            return 0;
        }
        return PollutionData.get(server).pollutedChunkCount();
    }
}
