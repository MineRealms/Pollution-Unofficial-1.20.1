package meowmel.pollution.loaders.recipes;

import argent_matter.gcyr.common.data.GCYRRecipeTypes;
import meowmel.pollution.api.unification.PollutionMaterials;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

/**
 * GCYR (Gregicality Rocketry) rocket fuel integration (only loaded when
 * {@code gcyr} is present; see {@link MagicFuelRecipes#rocketEngines}).
 *
 * <p>Verified with javap against {@code gcyr-0.2.9}: GCYR exposes
 * {@code GCYRRecipeTypes.ROCKET_FUEL_RECIPES} and
 * {@code RocketEntity#reinitializeFluidStorage} accepts any recipe of that type
 * whose first fluid input ingredient matches the tank fluid, using the recipe
 * duration as the burn efficiency factor
 * ({@code RocketEntity#consumeFuel}: burn = 2 * (thrusters + tier) /
 * (1 + duration / 20), so a longer duration means less fuel per tick). GCYR's
 * own entries (gasoline 25, diesel 18, rocket_fuel 75, hydrogen 10,
 * hydrogen_plasma 18) all use {@code .inputFluids(...).duration(...).EUt(0)}.</p>
 *
 * <p>The magic propellants are registered with the upstream rocket engine
 * durations (4 s and 8 s, i.e. 80 and 160 ticks). Since GCYR durations are in
 * ticks, both entries are deliberately more efficient than GCYR's best
 * conventional fuel (rocket fuel, 75 ticks), matching their endgame tier.</p>
 */
public final class GCYRRocketFuels {

    private GCYRRocketFuels() {}

    public static void init(Consumer<FinishedRecipe> provider) {
        // 焚天烈焰推进剂: upstream rocket engine duration 4 s.
        GCYRRecipeTypes.ROCKET_FUEL_RECIPES
                .recipeBuilder(id("infernal_blaze_propellant"))
                .inputFluids(PollutionMaterials.InfernalBlazePropellant.getFluid(1))
                .duration(4 * 20)
                .save(provider);

        // 龙脉星轨燃剂: upstream rocket engine duration 8 s.
        GCYRRecipeTypes.ROCKET_FUEL_RECIPES
                .recipeBuilder(id("dragon_pulse_fuel"))
                .inputFluids(PollutionMaterials.DragonPulseFuel.getFluid(1))
                .duration(8 * 20)
                .save(provider);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("pollution", "gcyr_rocket_fuel/" + path);
    }
}
