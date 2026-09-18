package meowmel.pollution.compat.kubejs;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import meowmel.pollution.compat.kubejs.event.PollutionChunkChangedEventJS;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

/**
 * KubeJS event group exposed to scripts as {@code PollutionEvents}.
 *
 * <p>Verified against kubejs-forge 2001.6.5-build.16 (javap): the plugin API
 * provides {@code EventGroup.of(name)}, {@code EventGroup#server(name,
 * supplier)} and {@code EventGroup#register()}. Publishing to script bindings
 * is automatic: {@code BuiltinKubeJSPlugin#registerBindings} iterates
 * {@code EventGroup.getGroups()} and binds every registered group under its
 * {@code name}, so scripts see {@code PollutionEvents.chunkChanged(event =>
 * ...)} without extra binding code.</p>
 *
 * <p>Event firing: {@code EventHandler#post(EventJS)} requires the handler to
 * carry a {@code ScriptTypeHolder} predicate, which {@code server(...)}
 * provides. {@link #postChunkChanged} guards with {@code hasListeners()} so
 * posting from mod code before any script has loaded is a no-op.</p>
 *
 * <p><b>Known gap:</b> the canonical call site for pollution mutations is
 * {@code api/pollution/PollutionEngine} ({@code add}/{@code set}/{@code scrub}),
 * which is outside this change's file ownership. Those methods therefore do
 * not post yet; {@link PollutionEventBridge} is the KubeJS-safe one-line hook
 * for that call site (see the port report). Scripts that mutate pollution
 * through the {@code Pollution} binding do post, because {@link PollutionJS} is
 * owned by this package.</p>
 */
public interface PollutionEvents {

    EventGroup GROUP = EventGroup.of("PollutionEvents");

    EventHandler CHUNK_CHANGED = GROUP.server("chunkChanged", () -> PollutionChunkChangedEventJS.class);

    static void register() {
        GROUP.register();
    }

    static void postChunkChanged(ServerLevel level, ChunkPos pos, double previousValue, double newValue, String cause) {
        if (CHUNK_CHANGED.hasListeners()) {
            CHUNK_CHANGED.post(new PollutionChunkChangedEventJS(level, pos, previousValue, newValue, cause));
        }
    }
}
