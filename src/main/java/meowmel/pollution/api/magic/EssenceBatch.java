package meowmel.pollution.api.magic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.ToLongBiFunction;

/** One consumed input batch, including output the receiving containers have not accepted yet. */
public final class EssenceBatch {
    private final Map<String, Long> remaining = new LinkedHashMap<>();
    private int progress;
    private int duration;

    public boolean active() { return !remaining.isEmpty(); }
    public boolean ready() { return active() && progress >= duration; }
    public int progress() { return progress; }
    public int duration() { return duration; }
    public Map<String, Long> remaining() { return Map.copyOf(remaining); }

    public void start(Map<String, Long> outputs, int ticks) {
        if (active()) throw new IllegalStateException("Previous essence batch is still pending");
        outputs.forEach((id, amount) -> { if (amount > 0) remaining.put(id, amount); });
        duration = Math.max(1, ticks);
        progress = 0;
    }

    /** Called only after this tick's processing energy and fuel have been paid. */
    public void advance() { if (active() && progress < duration) progress++; }

    /** Receiver returns the actual amount accepted, never a simulated capacity. */
    public boolean offer(ToLongBiFunction<String, Long> receiver) {
        if (!ready()) return false;
        boolean changed = false;
        var iterator = remaining.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            long accepted = Math.max(0L, Math.min(entry.getValue(), receiver.applyAsLong(entry.getKey(), entry.getValue())));
            if (accepted == 0) continue;
            long left = entry.getValue() - accepted;
            if (left == 0) iterator.remove(); else entry.setValue(left);
            changed = true;
        }
        if (!active()) { progress = 0; duration = 0; }
        return changed;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Progress", progress);
        tag.putInt("Duration", duration);
        CompoundTag outputs = new CompoundTag();
        remaining.forEach(outputs::putLong);
        tag.put("Remaining", outputs);
        return tag;
    }

    public void load(CompoundTag tag) {
        remaining.clear();
        CompoundTag outputs = tag.getCompound("Remaining");
        for (String id : outputs.getAllKeys()) {
            if (outputs.contains(id, Tag.TAG_ANY_NUMERIC) && outputs.getLong(id) > 0) remaining.put(id, outputs.getLong(id));
        }
        duration = Math.max(1, tag.getInt("Duration"));
        progress = Math.max(0, Math.min(duration, tag.getInt("Progress")));
        if (!active()) { progress = 0; duration = 0; }
    }
}
