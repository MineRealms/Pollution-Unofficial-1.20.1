package meowmel.pollution.compat.jei;

import meowmel.pollution.Pollution;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import net.minecraft.resources.ResourceLocation;

/**
 * JEI plugin skeleton. Recipe categories and machine information pages are
 * ported in later phases; the plugin is registered from phase 1 so the JEI
 * classpath requirement stays verified.
 */
@JeiPlugin
public final class PollutionJeiPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID, "jei_plugin");
    }
}
