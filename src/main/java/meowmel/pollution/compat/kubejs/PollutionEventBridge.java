package meowmel.pollution.compat.kubejs;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.fml.ModList;

/**
 * KubeJS-independent entry point for mod code that wants to report pollution
 * changes to scripts.
 *
 * <p>This class deliberately has no KubeJS types in its method signatures; the
 * only reference to {@link PollutionEvents} sits behind a {@code ModList} guard,
 * so calling it is safe when KubeJS is not installed (the JVM resolves the
 * constant lazily, only when the branch executes).</p>
 *
 * <p>Intended call site (owned by the api pass):
 * {@code PollutionEngine#add}, {@code #set} and {@code #scrub} should call
 * {@code PollutionEventBridge.chunkChanged(level, pos, previous, updated, "engine")}
 * right before returning. Nothing in this change wires that call because
 * {@code api/**} is outside its file ownership.</p>
 */
public final class PollutionEventBridge {

    private PollutionEventBridge() {}

    public static void chunkChanged(ServerLevel level, ChunkPos pos, double previousValue, double newValue,
                                    String cause) {
        if (ModList.get().isLoaded("kubejs")) {
            PollutionEvents.postChunkChanged(level, pos, previousValue, newValue, cause);
        }
    }
}
