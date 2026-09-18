package meowmel.pollution.loaders.recipes;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.data.recipe.misc.MetaTileEntityLoader;
import meowmel.pollution.Pollution;
import meowmel.pollution.common.machine.PollutionMachines;
import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.data.recipe.GTCraftingComponents.CIRCUIT;
import static com.gregtechceu.gtceu.data.recipe.GTCraftingComponents.CONVEYOR;
import static com.gregtechceu.gtceu.data.recipe.GTCraftingComponents.EMITTER;
import static com.gregtechceu.gtceu.data.recipe.GTCraftingComponents.HULL;
import static com.gregtechceu.gtceu.data.recipe.GTCraftingComponents.MOTOR;
import static com.gregtechceu.gtceu.data.recipe.GTCraftingComponents.PISTON;
import static com.gregtechceu.gtceu.data.recipe.GTCraftingComponents.ROTOR;
import static com.gregtechceu.gtceu.data.recipe.GTCraftingComponents.SENSOR;

/**
 * GregTech recipe datagen entry point, invoked from
 * {@code PollutionGTAddon#addRecipes(Consumer)}.
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
 * runtime.</p>
 */
public final class PollutionRecipes {

    private PollutionRecipes() {}

    public static void init(Consumer<FinishedRecipe> provider) {
        registerVisGenerator(provider);
        registerVisProvider(provider);
        registerMagicEnergyAbsorber(provider);
        registerFluxScrubber(provider);
        registerFluxFuelCell(provider);
        registerVisHatch(provider);
        CompoundAspectRecipes.init(provider);
        MagicFuelRecipes.init(provider);

        Pollution.LOGGER.info("Registered vis generator crafting recipes for {} tiers", count(PollutionMachines.VIS_GENERATOR));
        Pollution.LOGGER.info("Registered vis provider crafting recipes for {} tiers", count(PollutionMachines.VIS_PROVIDER));
        Pollution.LOGGER.info("Registered magic energy absorber crafting recipes for {} tiers", count(PollutionMachines.MAGIC_ENERGY_ABSORBER));
        Pollution.LOGGER.info("Registered flux scrubber crafting recipes for {} tiers", count(PollutionMachines.FLUX_SCRUBBER));
        Pollution.LOGGER.info("Registered flux fuel cell crafting recipes for {} tiers", count(PollutionMachines.FLUX_FUEL_CELL));
        Pollution.LOGGER.info("Registered vis hatch crafting recipes for {} tiers", count(PollutionMachines.VIS_HATCH));
    }

    private static int count(MachineDefinition[] machines) {
        int tiers = 0;
        for (MachineDefinition definition : machines) {
            if (definition != null) {
                tiers++;
            }
        }
        return tiers;
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

    /** Upstream: same pattern as the generator, B = emitter instead of piston. */
    private static void registerVisProvider(Consumer<FinishedRecipe> provider) {
        MetaTileEntityLoader.registerMachineRecipe(provider, PollutionMachines.VIS_PROVIDER,
                "ABA", "CHC", "ABA",
                'H', HULL,
                'A', MOTOR,
                'B', EMITTER,
                'C', ROTOR);
    }

    /**
     * Upstream: pattern {@code "CSC" / "HBH" / "MEM"}. The upstream center piece
     * {@code BLANKCORE} is a Pollution meta item that is not ported yet, so the
     * port substitutes a circuit and documents the change.
     */
    private static void registerMagicEnergyAbsorber(Consumer<FinishedRecipe> provider) {
        MetaTileEntityLoader.registerMachineRecipe(provider, PollutionMachines.MAGIC_ENERGY_ABSORBER,
                "CSC", "HBH", "MEM",
                'H', HULL,
                'S', SENSOR,
                'B', CIRCUIT,
                'C', CIRCUIT,
                'M', MOTOR,
                'E', EMITTER);
    }

    /** Upstream: same pattern as the absorber, B = sensor, C = rotor. */
    private static void registerFluxScrubber(Consumer<FinishedRecipe> provider) {
        MetaTileEntityLoader.registerMachineRecipe(provider, PollutionMachines.FLUX_SCRUBBER,
                "ABA", "CHC", "ABA",
                'H', HULL,
                'A', MOTOR,
                'B', SENSOR,
                'C', ROTOR);
    }

    /**
     * Upstream: pattern {@code "PBP" / "EHE" / "MCM"}. Center piece
     * {@code BLANKCORE} substituted by a circuit, same as the absorber.
     */
    private static void registerFluxFuelCell(Consumer<FinishedRecipe> provider) {
        MetaTileEntityLoader.registerMachineRecipe(provider, PollutionMachines.FLUX_FUEL_CELL,
                "PBP", "EHE", "MCM",
                'H', HULL,
                'P', PISTON,
                'B', CIRCUIT,
                'C', CIRCUIT,
                'M', MOTOR,
                'E', EMITTER);
    }

    /**
     * Upstream: {@code "ABA" / "CHC" / "ABA"} with H = hull, A = conveyor,
     * B = blank magic core (substituted by a circuit), C = emitter.
     */
    private static void registerVisHatch(Consumer<FinishedRecipe> provider) {
        MetaTileEntityLoader.registerMachineRecipe(provider, PollutionMachines.VIS_HATCH,
                "ABA", "CHC", "ABA",
                'H', HULL,
                'A', CONVEYOR,
                'B', CIRCUIT,
                'C', EMITTER);
    }
}
