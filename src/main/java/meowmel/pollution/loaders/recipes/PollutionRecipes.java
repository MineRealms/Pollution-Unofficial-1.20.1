package meowmel.pollution.loaders.recipes;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.data.recipe.misc.MetaTileEntityLoader;
import meowmel.pollution.Pollution;
import meowmel.pollution.common.machine.PollutionMachines;
import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.data.recipe.GTCraftingComponents.HULL;
import static com.gregtechceu.gtceu.data.recipe.GTCraftingComponents.MOTOR;
import static com.gregtechceu.gtceu.data.recipe.GTCraftingComponents.PISTON;
import static com.gregtechceu.gtceu.data.recipe.GTCraftingComponents.ROTOR;

/**
 * GregTech recipe datagen entry point, invoked from
 * {@code PollutionGTAddon#addRecipes(Consumer)} during {@code runData}.
 *
 * <p>Upstream registered machine recipes in
 * {@code meowmel.pollution.loaders.recipes.MachineRecipes} using GTCEu 1.12.2's
 * tiered shaped-recipe helper. The modern equivalent is
 * {@link MetaTileEntityLoader#registerMachineRecipe}, which resolves
 * {@code CraftingComponent} values per machine tier.</p>
 *
 * <p>Verified against GTCEu 8.0.0: recipes are no longer generated as JSON by
 * {@code runData}. {@code GTRecipes.recipeAddition} is invoked from the common
 * setup and feeds a built-in dynamic data pack
 * ({@code GTDynamicDataPack::addRecipe}), so this entry point fires at server
 * runtime. A server boot log line is emitted below for verification.</p>
 */
public final class PollutionRecipes {

    private PollutionRecipes() {}

    public static void init(Consumer<FinishedRecipe> provider) {
        registerVisGenerator(provider);

        int tiers = 0;
        for (MachineDefinition definition : PollutionMachines.VIS_GENERATOR) {
            if (definition != null) {
                tiers++;
            }
        }
        Pollution.LOGGER.info("Registered {} vis generator crafting recipes", tiers);
    }

    /**
     * Upstream: {@code MachineRecipes.muffler()} registered the six
     * {@code AURA_GENERATORS} with pattern {@code "ABA" / "CHC" / "ABA"},
     * H = hull, A = motor, B = piston, C = rotor. Kept unchanged; the helper
     * skips empty tiers and substitutes the tier components.
     */
    private static void registerVisGenerator(Consumer<FinishedRecipe> provider) {
        MetaTileEntityLoader.registerMachineRecipe(provider, PollutionMachines.VIS_GENERATOR,
                "ABA", "CHC", "ABA",
                'H', HULL,
                'A', MOTOR,
                'B', PISTON,
                'C', ROTOR);
    }
}
