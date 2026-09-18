package meowmel.pollution.loaders.recipes;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import meowmel.pollution.api.unification.PollutionMaterials;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

/**
 * Infused material processing recipes (from {@code MagicGCYMRecipes}).
 *
 * <p>The infused dusts of the six primal aspects extract back into their
 * fluids. The remaining 100+ recipes of the upstream file are GCYM/GTQT
 * machine integrations and wait for the substrate/catalyst material batch
 * (see the tracker).</p>
 */
public final class InfusedProcessingRecipes {

    private InfusedProcessingRecipes() {}

    public static void init(Consumer<FinishedRecipe> provider) {
        extract(provider, "infused_air", PollutionMaterials.InfusedAir);
        extract(provider, "infused_fire", PollutionMaterials.InfusedFire);
        extract(provider, "infused_water", PollutionMaterials.InfusedWater);
        extract(provider, "infused_earth", PollutionMaterials.InfusedEarth);
        extract(provider, "infused_order", PollutionMaterials.InfusedOrder);
        extract(provider, "infused_entropy", PollutionMaterials.InfusedEntropy);
    }

    private static void extract(Consumer<FinishedRecipe> provider, String name,
                                com.gregtechceu.gtceu.api.data.chemical.material.Material material) {
        if (material == null || !material.hasFluid()) {
            return;
        }
        GTRecipeBuilder.of(id(name), GTRecipeTypes.EXTRACTOR_RECIPES)
                .inputItems(ChemicalHelper.get(TagPrefix.dust, material, 1))
                .outputFluids(material.getFluid(144))
                .duration(200)
                .EUt(30)
                .save(provider);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("pollution", "infused_processing/" + path);
    }
}
