package meowmel.pollution.common.machine.multiblock.astral;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.api.recipe.GTRecipeSerializer;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import meowmel.pollution.loaders.recipes.AstralNativeRecipes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

/** Recipe types retained under their 1.12 Pollution IDs for data compatibility. */
public final class AstralRecipeMaps {
    public static final GTRecipeType INDUSTRIAL_STARLIGHT_INFUSER_RECIPES = nativeType("industrial_starlight_infuser_recipes", 2, 1, 2, 0);
    public static final GTRecipeType INDUSTRIAL_LIGHTWELL_RECIPES = nativeType("industrial_lightwell_recipes", 1, 0, 0, 1);
    public static final GTRecipeType CELESTIAL_OBSERVATION_RECIPES = type("celestial_observation", 3, 1, 2, 0);
    public static final GTRecipeType CELESTIAL_CALIBRATION_RECIPES = type("celestial_calibration", 6, 1, 2, 0);
    public static final GTRecipeType CELESTIAL_CRYSTAL_GROWTH_RECIPES = type("celestial_crystal_growth", 5, 1, 3, 0);

    private static GTRecipeType type(String id, int itemIn, int itemOut, int fluidIn, int fluidOut) {
        return GTRecipeTypes.register(id, "pollution")
                .setMaxIOSize(itemIn, itemOut, fluidIn, fluidOut).setEUIO(IO.IN);
    }

    private static GTRecipeType nativeType(String id, int itemIn, int itemOut, int fluidIn, int fluidOut) {
        var type = new AstralNativeRecipes.NativeRecipeType(ResourceLocation.fromNamespaceAndPath("gtceu", id));
        GTRegistries.register(BuiltInRegistries.RECIPE_TYPE, type.registryName, type);
        GTRegistries.register(BuiltInRegistries.RECIPE_SERIALIZER, type.registryName, new GTRecipeSerializer());
        GTRegistries.RECIPE_TYPES.register(type.registryName, type);
        return type.setMaxIOSize(itemIn, itemOut, fluidIn, fluidOut).setEUIO(IO.IN);
    }

    public static void init() {}

    private AstralRecipeMaps() {}
}
