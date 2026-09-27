package meowmel.pollution.loaders.recipes;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import hellfirepvp.astralsorcery.common.crafting.LiquidInfusion;
import hellfirepvp.astralsorcery.common.crafting.RecipeTypesAS;
import hellfirepvp.astralsorcery.common.crafting.WellLiquefaction;
import meowmel.pollution.common.machine.multiblock.astral.AstralRecipeMaps;
import meowmel.pollution.common.machine.multiblock.astral.AstralRecipeOutputs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;

/** GT proxies import the final Astral datapack recipes on each server/client recipe reload. */
public final class AstralNativeRecipes {
    private AstralNativeRecipes() {}

    public static void init() {
        AstralRecipeMaps.INDUSTRIAL_STARLIGHT_INFUSER_RECIPES.getProxyRecipes()
                .putIfAbsent(RecipeTypesAS.INFUSION.get(), new ArrayList<>());
        AstralRecipeMaps.INDUSTRIAL_LIGHTWELL_RECIPES.getProxyRecipes()
                .putIfAbsent(RecipeTypesAS.WELL.get(), new ArrayList<>());
    }

    public static final class NativeRecipeType extends GTRecipeType {
        public NativeRecipeType(ResourceLocation id) {
            super(id, "pollution");
        }

        @Override
        public GTRecipe toGTrecipe(ResourceLocation nativeId, Recipe<?> nativeRecipe) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath("pollution",
                    "astral/native/" + registryName.getPath() + "/" + nativeId.getNamespace() + "/" + nativeId.getPath());
            var builder = recipeBuilder(id).EUt(GTValues.VA[GTValues.IV]);
            if (nativeRecipe instanceof LiquidInfusion infusion) {
                builder.inputItems(infusion.getItemInput()).outputItems(infusion.getOutput(ItemStack.EMPTY))
                        .duration(Math.max(1, infusion.getCraftingTickTime()));
                // Preserve Pollution 1.12's expected twelve-position consumption, discounted sixfold.
                int amount = Math.round(2000 * infusion.getConsumptionChance());
                if (infusion.getConsumptionChance() > 0) {
                    builder.inputFluids(FluidIngredient.of(infusion.getLiquidInput(), Math.max(1, amount)));
                }
                if (infusion.doesCopyNBTToOutputs()) builder.addData(AstralRecipeOutputs.TRANSFORM, "copy_nbt");
            } else if (nativeRecipe instanceof WellLiquefaction well) {
                builder.notConsumable(well.getInput())
                        .outputFluids(new FluidStack(well.getFluidOutput(), Math.max(1, Math.round(well.getProductionMultiplier() * 1000))))
                        .duration(200);
            } else {
                return super.toGTrecipe(id, nativeRecipe);
            }
            return builder.buildRawRecipe();
        }
    }
}
