package meowmel.pollution.loaders.recipes;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import meowmel.pollution.api.recipes.PORecipeMaps;
import meowmel.pollution.api.unification.PollutionMaterials;

/**
 * Magic turbine fuel recipes.
 *
 * <p>Upstream fed the magic turbines from the GTQT fuel chain
 * (MagicNitrobenzene, InfernalBlazePropellant, DragonPulseFuel), which depends
 * on GTQT materials that are not part of the port. The port therefore fuels
 * the magic turbines directly with the ported infused fluids, at a rate that
 * mirrors the upstream energy density ordering (aura &gt; order &gt; fire).</p>
 */
public final class MagicTurbineRecipes {

    private MagicTurbineRecipes() {}

    public static void init(java.util.function.Consumer<net.minecraft.data.recipes.FinishedRecipe> provider) {
        fuel(provider, PollutionMaterials.InfusedEnergy, 1, GTValues.V[GTValues.HV] / 2, 20);
        fuel(provider, PollutionMaterials.InfusedFire, 1, GTValues.V[GTValues.HV] / 4, 20);
        fuel(provider, PollutionMaterials.InfusedAura, 1, GTValues.V[GTValues.EV], 20);
        fuel(provider, PollutionMaterials.InfusedOrder, 1, GTValues.V[GTValues.HV], 20);
    }

    private static void fuel(java.util.function.Consumer<net.minecraft.data.recipes.FinishedRecipe> provider,
                             com.gregtechceu.gtceu.api.data.chemical.material.Material material,
                             int amount, long euPerTick, int duration) {
        if (material == null || !material.hasFluid()) {
            return;
        }
        GTRecipeBuilder.of(
                        net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("pollution",
                                "magic_turbine/" + material.getName()),
                        PORecipeMaps.MAGIC_TURBINE_FUELS)
                .inputFluids(material.getFluid(amount))
                .duration(duration)
                .EUt(euPerTick)
                .save(provider);
    }
}
