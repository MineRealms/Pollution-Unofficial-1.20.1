package meowmel.pollution.loaders.recipes;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.data.recipe.misc.MetaTileEntityLoader;
import hellfirepvp.astralsorcery.common.lib.FluidsAS;
import meowmel.pollution.Pollution;
import meowmel.pollution.api.unification.PollutionMaterials;
import meowmel.pollution.common.item.PollutionItems;
import meowmel.pollution.common.machine.PollutionMachines;
import meowmel.pollution.common.machine.multiblock.astral.AstralRecipeMaps;
import meowmel.pollution.common.machine.multiblock.astral.ConstellationTowerDefinition;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.data.recipe.GTCraftingComponents.CIRCUIT;
import static com.gregtechceu.gtceu.data.recipe.GTCraftingComponents.HULL;
import static com.gregtechceu.gtceu.data.recipe.GTCraftingComponents.SENSOR;
import static com.gregtechceu.gtceu.data.recipe.GTCraftingComponents.MOTOR;

/** Obtainable Astral Sorcery integration chain and machine controllers. */
public final class AstralIntegrationRecipes {

    private AstralIntegrationRecipes() {}

    public static void init(Consumer<FinishedRecipe> provider) {
        registerComponents(provider);
        registerProcessing(provider);
        registerMachines(provider);
    }

    private static void registerComponents(Consumer<FinishedRecipe> provider) {
        // Pollution intermediates keep the Astral chain in the GT recipe system.
        GTRecipeBuilder.of(id("components/astral_resonance_coil"), GTRecipeTypes.ASSEMBLER_RECIPES)
                .inputItems(PollutionItems.MANA_RESONANCE_COIL.asStack())
                .inputItems(PollutionItems.STARRY_RUNE.asStack(2))
                .inputFluids(PollutionMaterials.InfusedAura.getFluid(500))
                .outputItems(PollutionItems.ASTRAL_RESONANCE_COIL.asStack())
                .duration(240).EUt(GTValues.VA[GTValues.EV]).save(provider);

        GTRecipeBuilder.of(id("components/astral_lens_basic"), GTRecipeTypes.ASSEMBLER_RECIPES)
                .inputItems(PollutionItems.SILVERED_GLASS_LENS.asStack())
                .inputItems(PollutionItems.ASTRAL_RESONANCE_COIL.asStack())
                .inputItems(PollutionItems.STARRY_RUNE.asStack(2))
                .outputItems(PollutionItems.ASTRAL_LENS_BASIC.asStack())
                .duration(200).EUt(GTValues.VA[GTValues.HV]).save(provider);

        GTRecipeBuilder.of(id("components/astral_lens_advanced"), GTRecipeTypes.ASSEMBLER_RECIPES)
                .inputItems(PollutionItems.ASTRAL_LENS_BASIC.asStack())
                .inputItems(PollutionItems.CELESTIAL_CALIBRATION_CORE.asStack())
                .inputItems(PollutionItems.ASTRAL_RESONANCE_COIL.asStack())
                .inputFluids(PollutionMaterials.InfusedLight.getFluid(1000))
                .outputItems(PollutionItems.ASTRAL_LENS_ADVANCED.asStack())
                .duration(400).EUt(GTValues.VA[GTValues.LuV]).save(provider);

        GTRecipeBuilder.of(id("components/constellation_data_wafer"), GTRecipeTypes.ASSEMBLER_RECIPES)
                .inputItems(PollutionItems.STERILE_SLATE_BLANK.asStack())
                .inputItems(PollutionItems.ARCANE_INK_CAPSULE.asStack())
                .inputItems(PollutionItems.ASTRAL_LENS_BASIC.asStack())
                .inputFluids(PollutionMaterials.InfusedAura.getFluid(1000))
                .outputItems(PollutionItems.CONSTELLATION_DATA_WAFER.asStack())
                .duration(300).EUt(GTValues.VA[GTValues.IV]).save(provider);
    }

    private static void registerProcessing(Consumer<FinishedRecipe> provider) {
        FluidStack starlight = new FluidStack(FluidsAS.LIQUID_STARLIGHT_SOURCE.get(), 1000);

        GTRecipeBuilder.of(id("processing/industrial_starlight_infuser"),
                        AstralRecipeMaps.INDUSTRIAL_STARLIGHT_INFUSER_RECIPES)
                .inputItems(PollutionItems.ASTRAL_LENS_BASIC.asStack())
                .inputItems(PollutionItems.ASTRAL_RESONANCE_COIL.asStack())
                .inputFluids(starlight)
                .outputItems(PollutionItems.ASTRAL_LENS_ADVANCED.asStack())
                .duration(400).EUt(GTValues.VA[GTValues.IV]).save(provider);

        GTRecipeBuilder.of(id("processing/industrial_lightwell"),
                        AstralRecipeMaps.INDUSTRIAL_LIGHTWELL_RECIPES)
                .inputItems(PollutionItems.ASTRAL_RESONANCE_COIL.asStack())
                .outputFluids(new FluidStack(FluidsAS.LIQUID_STARLIGHT_SOURCE.get(), 1000))
                .duration(200).EUt(GTValues.VA[GTValues.HV]).save(provider);

        GTRecipeBuilder.of(id("processing/celestial_observation"),
                        AstralRecipeMaps.CELESTIAL_OBSERVATION_RECIPES)
                .inputItems(PollutionItems.ASTRAL_LENS_BASIC.asStack())
                .inputItems(PollutionItems.ATTUNED_CRYSTAL_WAFER.asStack())
                .inputItems(PollutionItems.STARRY_RUNE.asStack())
                .outputItems(PollutionItems.CONSTELLATION_DATA_WAFER.asStack())
                .duration(600).EUt(GTValues.VA[GTValues.LuV]).save(provider);

        GTRecipeBuilder.of(id("processing/celestial_calibration"),
                        AstralRecipeMaps.CELESTIAL_CALIBRATION_RECIPES)
                .inputItems(PollutionItems.ASTRAL_LENS_ADVANCED.asStack())
                .inputItems(PollutionItems.CONSTELLATION_DATA_WAFER.asStack())
                .inputItems(PollutionItems.CELESTIAL_CALIBRATION_CORE.asStack())
                .inputItems(PollutionItems.ATTUNED_CRYSTAL_WAFER.asStack())
                .inputItems(PollutionItems.ASTRAL_RESONANCE_COIL.asStack())
                .inputItems(PollutionItems.STARRY_RUNE.asStack())
                .outputItems(PollutionItems.CELESTIAL_CALIBRATION_CORE.asStack())
                .duration(800).EUt(GTValues.VA[GTValues.ZPM]).save(provider);

        GTRecipeBuilder.of(id("processing/celestial_crystal_growth"),
                        AstralRecipeMaps.CELESTIAL_CRYSTAL_GROWTH_RECIPES)
                .inputItems(PollutionItems.ROCK_CRYSTAL_SEED.asStack())
                .inputItems(PollutionItems.CELESTIAL_CRYSTAL_EMBRYO.asStack())
                .inputItems(PollutionItems.ASTRAL_LENS_ADVANCED.asStack())
                .inputItems(PollutionItems.CONSTELLATION_DATA_WAFER.asStack())
                .inputFluids(starlight, PollutionMaterials.InfusedAura.getFluid(1000),
                        PollutionMaterials.InfusedCrystal.getFluid(500))
                .outputItems(PollutionItems.CULTIVATED_CRYSTAL.asStack())
                .duration(1200).EUt(GTValues.VA[GTValues.UV]).save(provider);
    }

    private static void registerMachines(Consumer<FinishedRecipe> provider) {
        MetaTileEntityLoader.registerMachineRecipe(provider,
                new com.gregtechceu.gtceu.api.machine.MachineDefinition[] {
                        PollutionMachines.INDUSTRIAL_STARLIGHT_INFUSER },
                "SCS", "CHC", "SCS", 'H', HULL, 'S', SENSOR, 'C', CIRCUIT);
        MetaTileEntityLoader.registerMachineRecipe(provider,
                new com.gregtechceu.gtceu.api.machine.MachineDefinition[] {
                        PollutionMachines.INDUSTRIAL_LIGHTWELL },
                "CCC", "CHC", "CCC", 'H', HULL, 'C', MOTOR);
        MetaTileEntityLoader.registerMachineRecipe(provider,
                new com.gregtechceu.gtceu.api.machine.MachineDefinition[] {
                        PollutionMachines.CELESTIAL_OBSERVATION_ARRAY },
                "SCS", "CHC", "SCS", 'H', HULL, 'S', SENSOR, 'C', CIRCUIT);
        MetaTileEntityLoader.registerMachineRecipe(provider,
                new com.gregtechceu.gtceu.api.machine.MachineDefinition[] {
                        PollutionMachines.CELESTIAL_CALIBRATION_MATRIX },
                "SCS", "CHC", "SCS", 'H', HULL, 'S', SENSOR, 'C', CIRCUIT);
        MetaTileEntityLoader.registerMachineRecipe(provider,
                new com.gregtechceu.gtceu.api.machine.MachineDefinition[] {
                        PollutionMachines.CELESTIAL_CRYSTAL_GROWTH_ARRAY },
                "CCC", "CHC", "CCC", 'H', HULL, 'C', MOTOR);
        for (ConstellationTowerDefinition tower : ConstellationTowerDefinition.values()) {
            var definition = PollutionMachines.CONSTELLATION_TOWER[tower.ordinal()];
            if (definition != null) {
                MetaTileEntityLoader.registerMachineRecipe(provider,
                        new com.gregtechceu.gtceu.api.machine.MachineDefinition[] { definition },
                        "SCS", "CHC", "SCS", 'H', HULL, 'S', SENSOR, 'C', CIRCUIT);
            }
        }
        Pollution.LOGGER.info("Registered Astral component, processing and machine recipes");
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("pollution", "astral/" + path);
    }
}
