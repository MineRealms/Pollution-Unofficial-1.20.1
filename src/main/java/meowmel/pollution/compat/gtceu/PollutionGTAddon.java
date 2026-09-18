package meowmel.pollution.compat.gtceu;

import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import meowmel.pollution.Pollution;
import meowmel.pollution.api.unification.PollutionElements;
import meowmel.pollution.loaders.recipes.PollutionRecipes;
import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

/**
 * GregTech CEu Modern addon entry point. Discovered automatically through the
 * {@link GTAddon} annotation scan.
 */
@GTAddon
public final class PollutionGTAddon implements IGTAddon {

    /**
     * Shared registrate. {@code registerRegistrate()} must be called from the
     * Pollution mod constructor (own mod bus). Calling it from
     * {@code initializeAddon()} attaches the listener to GregTech's bus instead
     * (the FMLJavaModLoadingContext thread-local still points at GT there), which
     * makes addon ore blocks flush before GT's stone blocks exist and crashes
     * with {@code Registry entry not present: gtceu:red_granite}.
     */
    public static final GTRegistrate REGISTRATE = GTRegistrate.create(Pollution.MOD_ID);

    @Override
    public GTRegistrate getRegistrate() {
        return REGISTRATE;
    }

    @Override
    public void initializeAddon() {
        Pollution.LOGGER.debug("Pollution GregTech CEu addon initialized");
    }

    @Override
    public String addonModId() {
        return Pollution.MOD_ID;
    }

    @Override
    public void registerElements() {
        PollutionElements.init();
    }

    // Materials are registered through {@code PollutionMaterialEvents}
    // (MaterialRegistryEvent + MaterialEvent), the replacement for the
    // deprecated IGTAddon#registerMaterials() hook.

    @Override
    public void addRecipes(Consumer<FinishedRecipe> provider) {
        PollutionRecipes.init(provider);
    }
}
