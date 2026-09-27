package meowmel.pollution.common.machine.multiblock.astral;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

/** Recipe types retained under their 1.12 Pollution IDs for data compatibility. */
public final class AstralRecipeMaps {
    public static final GTRecipeType INDUSTRIAL_STARLIGHT_INFUSER_RECIPES = type("industrial_starlight_infuser_recipes", 2, 1, 2, 0);
    public static final GTRecipeType INDUSTRIAL_LIGHTWELL_RECIPES = type("industrial_lightwell_recipes", 1, 0, 0, 1);
    public static final GTRecipeType CELESTIAL_OBSERVATION_RECIPES = type("celestial_observation", 3, 1, 2, 0);
    public static final GTRecipeType CELESTIAL_CALIBRATION_RECIPES = type("celestial_calibration", 6, 1, 2, 0);
    public static final GTRecipeType CELESTIAL_CRYSTAL_GROWTH_RECIPES = type("celestial_crystal_growth", 4, 1, 3, 0);

    private static GTRecipeType type(String id, int itemIn, int itemOut, int fluidIn, int fluidOut) {
        return GTRecipeTypes.register(id, "pollution")
                .setMaxIOSize(itemIn, itemOut, fluidIn, fluidOut).setEUIO(IO.IN);
    }

    public static void init() {}

    private AstralRecipeMaps() {}
}
