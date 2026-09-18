package meowmel.pollution.loaders.recipes;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import meowmel.pollution.api.recipes.PORecipeMaps;
import meowmel.pollution.api.unification.PollutionMaterials;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.function.Consumer;

/**
 * Compound aspect chemistry: two infused fluids combine in the mixer into a
 * compound aspect fluid, and every infused fluid burns in the magic turbines.
 *
 * <p>Port of upstream {@code CompoundAspectRecipes}; the recipe table and the
 * nested-component duration/EU scaling are unchanged. The turbine fuel values
 * (80 mB, 80 ticks for primals; component-count scaled for compounds) are the
 * upstream values, replacing the earlier interim {@code MagicTurbineRecipes}
 * adaptation.</p>
 */
public final class CompoundAspectRecipes {

    private CompoundAspectRecipes() {}

    public static void init(Consumer<FinishedRecipe> provider) {
        aspect(provider);
    }

    private static void aspect(Consumer<FinishedRecipe> provider) {
        registerGenerator(provider, PollutionMaterials.InfusedAir);
        registerGenerator(provider, PollutionMaterials.InfusedFire);
        registerGenerator(provider, PollutionMaterials.InfusedWater);
        registerGenerator(provider, PollutionMaterials.InfusedEarth);
        registerGenerator(provider, PollutionMaterials.InfusedEntropy);
        registerGenerator(provider, PollutionMaterials.InfusedOrder);

        registerAspect(provider, PollutionMaterials.InfusedAir, PollutionMaterials.InfusedEarth,
                PollutionMaterials.InfusedCrystal);
        registerAspect(provider, PollutionMaterials.InfusedEarth, PollutionMaterials.InfusedWater,
                PollutionMaterials.InfusedLife);
        registerAspect(provider, PollutionMaterials.InfusedWater, PollutionMaterials.InfusedEntropy,
                PollutionMaterials.InfusedDeath);
        registerAspect(provider, PollutionMaterials.InfusedLife, PollutionMaterials.InfusedDeath,
                PollutionMaterials.InfusedSoul);
        registerAspect(provider, PollutionMaterials.InfusedSoul, PollutionMaterials.InfusedEntropy,
                PollutionMaterials.InfusedWeapon);
        registerAspect(provider, PollutionMaterials.InfusedEarth, PollutionMaterials.InfusedOrder,
                PollutionMaterials.InfusedMetal);
        registerAspect(provider, PollutionMaterials.InfusedOrder, PollutionMaterials.InfusedFire,
                PollutionMaterials.InfusedEnergy);
        registerAspect(provider, PollutionMaterials.InfusedMetal, PollutionMaterials.InfusedEnergy,
                PollutionMaterials.InfusedInstrument);
        registerAspect(provider, PollutionMaterials.InfusedOrder, PollutionMaterials.InfusedEntropy,
                PollutionMaterials.InfusedExchange);
        registerAspect(provider, PollutionMaterials.InfusedAir, PollutionMaterials.InfusedEnergy,
                PollutionMaterials.InfusedMagic);
        registerAspect(provider, PollutionMaterials.InfusedMagic, PollutionMaterials.InfusedWater,
                PollutionMaterials.InfusedAlchemy);
        registerAspect(provider, PollutionMaterials.InfusedFire, PollutionMaterials.InfusedAir,
                PollutionMaterials.InfusedLight);
        registerAspect(provider, PollutionMaterials.InfusedInstrument, PollutionMaterials.InfusedExchange,
                PollutionMaterials.InfusedCraft);
        registerAspect(provider, PollutionMaterials.InfusedEntropy, PollutionMaterials.InfusedAir,
                PollutionMaterials.InfusedVoid);
        registerAspect(provider, PollutionMaterials.InfusedOrder, PollutionMaterials.InfusedAir,
                PollutionMaterials.InfusedMotion);
        registerAspect(provider, PollutionMaterials.InfusedEntropy, PollutionMaterials.InfusedMagic,
                PollutionMaterials.InfusedTaint);
        registerAspect(provider, PollutionMaterials.InfusedEntropy, PollutionMaterials.InfusedLight,
                PollutionMaterials.InfusedDark);
        registerAspect(provider, PollutionMaterials.InfusedVoid, PollutionMaterials.InfusedDark,
                PollutionMaterials.InfusedAlien);
        registerAspect(provider, PollutionMaterials.InfusedAir, PollutionMaterials.InfusedMotion,
                PollutionMaterials.InfusedFly);
        registerAspect(provider, PollutionMaterials.InfusedEarth, PollutionMaterials.InfusedLife,
                PollutionMaterials.InfusedPlant);
        registerAspect(provider, PollutionMaterials.InfusedMotion, PollutionMaterials.InfusedInstrument,
                PollutionMaterials.InfusedMechanics);
        registerAspect(provider, PollutionMaterials.InfusedEntropy, PollutionMaterials.InfusedMotion,
                PollutionMaterials.InfusedTrap);
        registerAspect(provider, PollutionMaterials.InfusedFire, PollutionMaterials.InfusedSoul,
                PollutionMaterials.InfusedThought);
        registerAspect(provider, PollutionMaterials.InfusedAir, PollutionMaterials.InfusedSoul,
                PollutionMaterials.InfusedSense);
        registerAspect(provider, PollutionMaterials.InfusedMotion, PollutionMaterials.InfusedLife,
                PollutionMaterials.InfusedAnimal);
        registerAspect(provider, PollutionMaterials.InfusedLife, PollutionMaterials.InfusedSoul,
                PollutionMaterials.InfusedHuman);
        registerAspect(provider, PollutionMaterials.InfusedSoul, PollutionMaterials.InfusedVoid,
                PollutionMaterials.InfusedGreed);
        registerAspect(provider, PollutionMaterials.InfusedSoul, PollutionMaterials.InfusedEarth,
                PollutionMaterials.InfusedArmor);
        registerAspect(provider, PollutionMaterials.InfusedFire, PollutionMaterials.InfusedEntropy,
                PollutionMaterials.InfusedCold);
        registerAspect(provider, PollutionMaterials.InfusedMotion, PollutionMaterials.InfusedDeath,
                PollutionMaterials.InfusedUndead);
        registerAspect(provider, PollutionMaterials.InfusedMagic, PollutionMaterials.InfusedAir,
                PollutionMaterials.InfusedAura);
        registerAspect(provider, PollutionMaterials.InfusedVoid, PollutionMaterials.InfusedEntropy,
                PollutionMaterials.InfusedSpatio);
        registerAspect(provider, PollutionMaterials.InfusedSpatio, PollutionMaterials.InfusedExchange,
                PollutionMaterials.InfusedTempus);
        registerAspect(provider, PollutionMaterials.InfusedSense, PollutionMaterials.InfusedExchange,
                PollutionMaterials.InfusedTinctura);
    }

    public static void registerAspect(Consumer<FinishedRecipe> provider, Material input1, Material input2,
                                      Material output) {
        if (!hasFluid(input1) || !hasFluid(input2) || !hasFluid(output)) {
            return;
        }
        int amount = countAllNestedComponents(output);
        int tier = Math.max(1, Math.min(amount / 4, GTValues.VA.length - 1));

        GTRecipeBuilder.of(id("compound/" + output.getName()), GTRecipeTypes.MIXER_RECIPES)
                .inputFluids(input1.getFluid(1000))
                .inputFluids(input2.getFluid(1000))
                .outputFluids(output.getFluid(2000))
                .duration(100 * amount)
                .EUt(GTValues.VA[tier])
                .save(provider);

        registerGenerator(provider, output, 80, 40 * amount, amount / 3);
    }

    private static void registerGenerator(Consumer<FinishedRecipe> provider, Material input) {
        registerGenerator(provider, input, 80, 80, 1);
    }

    private static void registerGenerator(Consumer<FinishedRecipe> provider, Material input, int amount,
                                          int duration, int tier) {
        if (!hasFluid(input)) {
            return;
        }
        GTRecipeBuilder.of(id("turbine/" + input.getName()), PORecipeMaps.MAGIC_TURBINE_FUELS)
                .inputFluids(input.getFluid(amount))
                .duration(duration)
                .EUt(-GTValues.V[Math.max(1, Math.min(tier, GTValues.V.length - 1))])
                .save(provider);
    }

    public static int countAllNestedComponents(Material material) {
        return Math.max(1, countNestedComponents(material.getMaterialComponents()));
    }

    private static int countNestedComponents(List<MaterialStack> components) {
        if (components == null) {
            return 0;
        }
        int count = 0;
        for (MaterialStack stack : components) {
            if (stack == null || stack.material() == null) {
                continue;
            }
            count += 1;
            count += countNestedComponents(stack.material().getMaterialComponents());
        }
        return count;
    }

    private static boolean hasFluid(Material material) {
        return material != null && material.hasFluid();
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("pollution", "compound_aspect/" + path);
    }
}
