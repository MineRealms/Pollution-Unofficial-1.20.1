package meowmel.pollution.compat.jei;

/** Connects the optional JEI runtime only for the opt-in development client check. */
@mezz.jei.api.JeiPlugin
public final class ClientSmokeJeiHook implements mezz.jei.api.IModPlugin {
    @Override public net.minecraft.resources.ResourceLocation getPluginUid() {
        return net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("pollution", "client_smoke");
    }
    @Override public void onRuntimeAvailable(mezz.jei.api.runtime.IJeiRuntime runtime) {
        if (!net.minecraftforge.fml.loading.FMLEnvironment.production && Boolean.getBoolean("pollution.clientSmokeTest")) {
            meowmel.pollution.gametest.ClientSmokeTests.jeiRuntime = runtime;
        }
    }
}
