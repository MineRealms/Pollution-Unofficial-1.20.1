package meowmel.pollution.api.pollution;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Sparse per-chunk industrial pollution storage. Only polluted chunks are kept.
 */
public final class PollutionData extends SavedData {

    public static final String DATA_NAME = "pollution";

    private static final String TAG_CHUNKS = "Chunks";
    private static final String TAG_POS = "Pos";
    private static final String TAG_AMOUNT = "Amount";

    private final Long2DoubleOpenHashMap chunkPollution = new Long2DoubleOpenHashMap();

    public static PollutionData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(PollutionData::load, PollutionData::new, DATA_NAME);
    }

    public static PollutionData load(CompoundTag tag) {
        PollutionData data = new PollutionData();
        ListTag chunks = tag.getList(TAG_CHUNKS, Tag.TAG_COMPOUND);
        for (int index = 0; index < chunks.size(); index++) {
            CompoundTag entry = chunks.getCompound(index);
            double amount = entry.getDouble(TAG_AMOUNT);
            if (amount > 0.0D) {
                data.chunkPollution.put(entry.getLong(TAG_POS), amount);
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag chunks = new ListTag();
        for (Long2DoubleMap.Entry entry : chunkPollution.long2DoubleEntrySet()) {
            if (entry.getDoubleValue() <= 0.0D) {
                continue;
            }
            CompoundTag entryTag = new CompoundTag();
            entryTag.putLong(TAG_POS, entry.getLongKey());
            entryTag.putDouble(TAG_AMOUNT, entry.getDoubleValue());
            chunks.add(entryTag);
        }
        tag.put(TAG_CHUNKS, chunks);
        return tag;
    }

    public double get(ChunkPos pos) {
        return chunkPollution.get(pos.toLong());
    }

    public double add(ChunkPos pos, double amount) {
        if (amount <= 0.0D) {
            return get(pos);
        }
        double updated = chunkPollution.get(pos.toLong()) + amount;
        chunkPollution.put(pos.toLong(), updated);
        setDirty();
        return updated;
    }

    public double set(ChunkPos pos, double amount) {
        if (amount <= 0.0D) {
            if (chunkPollution.remove(pos.toLong()) != 0.0D) {
                setDirty();
            }
            return 0.0D;
        }
        chunkPollution.put(pos.toLong(), amount);
        setDirty();
        return amount;
    }

    public double scrub(ChunkPos pos, double amount) {
        double current = chunkPollution.get(pos.toLong());
        if (current <= 0.0D || amount <= 0.0D) {
            return 0.0D;
        }
        double removed = Math.min(current, amount);
        double left = current - removed;
        if (left <= 0.0D) {
            chunkPollution.remove(pos.toLong());
        } else {
            chunkPollution.put(pos.toLong(), left);
        }
        setDirty();
        return removed;
    }

    public void decayAll(double amount) {
        if (amount <= 0.0D || chunkPollution.isEmpty()) {
            return;
        }
        LongIterator iterator = chunkPollution.keySet().iterator();
        while (iterator.hasNext()) {
            long key = iterator.nextLong();
            double left = chunkPollution.get(key) - amount;
            if (left <= 0.0D) {
                iterator.remove();
            } else {
                chunkPollution.put(key, left);
            }
        }
        setDirty();
    }

    public int pollutedChunkCount() {
        return chunkPollution.size();
    }
}
