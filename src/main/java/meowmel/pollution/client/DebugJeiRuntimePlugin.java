package meowmel.pollution.client;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;

/**
 * TEMPORARY diagnostic: captures the JEI runtime so {@link DebugRecipeDump}
 * can query the recipes JEI actually registered. Must be deleted after use.
 */
@JeiPlugin
public final class DebugJeiRuntimePlugin implements IModPlugin {

    static IJeiRuntime RUNTIME;

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath("pollution", "debug_jei_runtime");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        RUNTIME = jeiRuntime;
    }
}
