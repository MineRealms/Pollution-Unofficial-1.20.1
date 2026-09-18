package meowmel.pollution.loaders.recipes;

import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

/**
 * GregTech recipe datagen entry point, invoked from
 * {@code PollutionGTAddon#addRecipes(Consumer)} during {@code runData}.
 *
 * <p>TODO(port): the 1.12.2 project registered recipes imperatively from
 * {@code meowmel.pollution.loaders.recipes.*} (MaterialsLine, MagicIntegrationRecipes,
 * MagicHatchRecipes, ...). Those are ported line by line in later phases; this class is
 * the wired, verified hook they will plug into.</p>
 */
public final class PollutionRecipes {

    private PollutionRecipes() {}

    public static void init(Consumer<FinishedRecipe> provider) {
        // No recipes ported yet.
    }
}
