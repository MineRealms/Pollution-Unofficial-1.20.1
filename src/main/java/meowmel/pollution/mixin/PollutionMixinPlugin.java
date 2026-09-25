package meowmel.pollution.mixin;

import net.minecraftforge.fml.ModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Logs compatibility mixin application. Optional JEI targets use pseudo mixins so
 * clients without JEI do not load its classes. Development runs include JEI to exercise
 * the supported API. Common GT hooks and the client Alfheim sky use the same config.
 */
public final class PollutionMixinPlugin implements IMixinConfigPlugin {

    private static final String JEI_MOD_ID = "jei";
    private static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();

    @Override
    public void onLoad(String mixinPackage) {
        LOGGER.info("[pollution] mixin config loaded (package={}), jei present={}", mixinPackage, isJeiLoaded());
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        // No ModList gate: the mixin only runs when its target class is loaded,
        // and JEI's classes only exist when JEI is installed. Checking ModList
        // here fails because the config is evaluated before JEI registers.
        LOGGER.info("[pollution] mixin {} -> target {}", mixinClassName, targetClassName);
        return true;
    }

    private static boolean isJeiLoaded() {
        try {
            ModList modList = ModList.get();
            return modList != null && modList.isLoaded(JEI_MOD_ID);
        } catch (Throwable ignored) {
            return false;
        }
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName,
                         IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName,
                          IMixinInfo mixinInfo) {
        LOGGER.info("[pollution] mixin applied: {} -> {}", mixinClassName, targetClassName);
    }
}
