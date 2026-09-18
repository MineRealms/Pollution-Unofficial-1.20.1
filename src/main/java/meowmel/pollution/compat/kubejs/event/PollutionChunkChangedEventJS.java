package meowmel.pollution.compat.kubejs.event;

import dev.latvian.mods.kubejs.event.EventJS;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

/**
 * Script event payload for {@code PollutionEvents.chunkChanged}.
 *
 * <p>Fired when the industrial pollution value of a chunk changes. Getters are
 * exposed to Rhino as bean properties, e.g.
 * {@code event.newValue}, {@code event.chunkX}, {@code event.level}.</p>
 */
public class PollutionChunkChangedEventJS extends EventJS {

    private final ServerLevel level;
    private final ChunkPos pos;
    private final double previousValue;
    private final double newValue;
    private final String cause;

    public PollutionChunkChangedEventJS(ServerLevel level, ChunkPos pos, double previousValue, double newValue,
                                        String cause) {
        this.level = level;
        this.pos = pos;
        this.previousValue = previousValue;
        this.newValue = newValue;
        this.cause = cause;
    }

    public ServerLevel getLevel() {
        return level;
    }

    public ChunkPos getChunkPos() {
        return pos;
    }

    public int getChunkX() {
        return pos.x;
    }

    public int getChunkZ() {
        return pos.z;
    }

    public double getPreviousValue() {
        return previousValue;
    }

    public double getNewValue() {
        return newValue;
    }

    public double getDelta() {
        return newValue - previousValue;
    }

    /** Free-form origin tag: {@code "script"} for the binding helpers, else the producer's label. */
    public String getCause() {
        return cause;
    }
}
