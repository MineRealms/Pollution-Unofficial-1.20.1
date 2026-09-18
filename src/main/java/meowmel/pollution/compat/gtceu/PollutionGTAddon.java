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

    private final GTRegistrate registrate = GTRegistrate.create(Pollution.MOD_ID);

    @Override
    public GTRegistrate getRegistrate() {
        return registrate;
    }

    @Override
    public void initializeAddon() {
        registrate.registerRegistrate();
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
