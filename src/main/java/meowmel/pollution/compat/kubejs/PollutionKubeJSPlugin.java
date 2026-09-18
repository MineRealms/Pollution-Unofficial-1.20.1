package meowmel.pollution.compat.kubejs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import meowmel.pollution.Pollution;

/**
 * KubeJS integration entry point. Registered through
 * {@code src/main/resources/kubejs.plugins.txt}.
 *
 * Recipe removal / addition hooks and pollution script bindings are ported in
 * later phases; see docs/MIGRATION_TRACKER.md section 7.
 */
public final class PollutionKubeJSPlugin extends KubeJSPlugin {

    @Override
    public void init() {
        Pollution.LOGGER.debug("Pollution KubeJS plugin loaded");
    }
}
