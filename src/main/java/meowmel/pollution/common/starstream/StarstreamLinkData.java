package meowmel.pollution.common.starstream;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.*;

/** Durable unlink delivery, including targets in unloaded chunks or other dimensions. */
public final class StarstreamLinkData extends SavedData {
    private final Map<UUID, Set<UUID>> pending = new HashMap<>();

    public static StarstreamLinkData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(
                StarstreamLinkData::load, StarstreamLinkData::new, "pollution_starstream_links");
    }

    public void unlink(UUID target, UUID source) {
        if (pending.computeIfAbsent(target, ignored -> new HashSet<>()).add(source)) setDirty();
    }

    public Set<UUID> takeUnlinks(UUID target) {
        Set<UUID> sources = pending.remove(target);
        if (sources == null) return Set.of();
        setDirty();
        return sources;
    }

    public static StarstreamLinkData load(CompoundTag tag) {
        StarstreamLinkData data = new StarstreamLinkData();
        ListTag entries = tag.getList("Unlinks", Tag.TAG_COMPOUND);
        for (int i = 0; i < entries.size(); i++) {
            CompoundTag entry = entries.getCompound(i);
            if (entry.hasUUID("Target") && entry.hasUUID("Source")) {
                data.pending.computeIfAbsent(entry.getUUID("Target"), ignored -> new HashSet<>()).add(entry.getUUID("Source"));
            }
        }
        return data;
    }

    @Override public CompoundTag save(CompoundTag tag) {
        ListTag entries = new ListTag();
        pending.forEach((target, sources) -> sources.forEach(source -> {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("Target", target);
            entry.putUUID("Source", source);
            entries.add(entry);
        }));
        tag.put("Unlinks", entries);
        return tag;
    }
}
