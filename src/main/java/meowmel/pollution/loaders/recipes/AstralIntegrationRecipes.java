package meowmel.pollution.loaders.recipes;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.item.ItemConstellationPaper;
import hellfirepvp.astralsorcery.common.lib.FluidsAS;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import meowmel.pollution.api.astral.AstralNbtHelper;
import meowmel.pollution.api.recipes.PORecipeMaps;
import meowmel.pollution.api.recipes.properties.AstralCondition;
import meowmel.pollution.api.recipes.properties.MagicRecipeProperties;
import meowmel.pollution.api.unification.PollutionMaterials;
import meowmel.pollution.common.item.PollutionItems;
import meowmel.pollution.common.machine.PollutionMachines;
import meowmel.pollution.common.machine.multiblock.astral.AstralRecipeMaps;
import meowmel.pollution.common.machine.multiblock.astral.AstralRecipeOutputs;
import meowmel.pollution.common.machine.multiblock.astral.ConstellationTowerDefinition;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fluids.FluidStack;

import java.util.function.Consumer;

/** Astral native inputs, persistent crystal lineage and the celestial industrial progression. */
public final class AstralIntegrationRecipes {
    private AstralIntegrationRecipes() {}

    public static void init(Consumer<FinishedRecipe> provider) {
        AstralNativeRecipes.init();
        registerMaterials(provider);
        registerComponents(provider);
        registerCrystalLineage(provider);
        registerProcessing(provider);
        registerMachines(provider);
    }

    private static void registerMaterials(Consumer<FinishedRecipe> provider) {
        GTRecipeBuilder.of(id("materials/optical_aquamarine"), GTRecipeTypes.CHEMICAL_RECIPES)
                .inputItems(new ItemStack(ItemsAS.AQUAMARINE.get(), 4))
                .inputFluids(GTMaterials.DistilledWater.getFluid(1000), PollutionMaterials.InfusedOrder.getFluid(144))
                .outputItems(TagPrefix.dust, PollutionMaterials.OpticalGradeAquamarine, 4)
                .duration(300).EUt(GTValues.VA[GTValues.HV]).save(provider);
        GTRecipeBuilder.of(id("materials/optical_aquamarine_gem"), GTRecipeTypes.SIFTER_RECIPES)
                .inputItems(TagPrefix.dust, PollutionMaterials.OpticalGradeAquamarine, 4)
                .outputItems(TagPrefix.gem, PollutionMaterials.OpticalGradeAquamarine, 3)
                .chancedOutput(TagPrefix.gem, PollutionMaterials.OpticalGradeAquamarine, 1, 5000, 0)
                .duration(240).EUt(GTValues.VA[GTValues.HV]).save(provider);
        GTRecipeBuilder.of(id("materials/starlight_pollen"), GTRecipeTypes.MACERATOR_RECIPES)
                .inputItems(BlocksAS.GLOW_FLOWER.get().asItem()).outputItems(TagPrefix.dust, PollutionMaterials.StarlightPollen, 2)
                .duration(160).EUt(GTValues.VA[GTValues.MV]).save(provider);
        GTRecipeBuilder.of(id("materials/moonlight_resin"), GTRecipeTypes.CHEMICAL_RECIPES)
                .inputItems(SafeItems.byId("thaumcraft", "silverwood_log", 4)).inputFluids(starlight(250), GTMaterials.Water.getFluid(1000))
                .outputFluids(PollutionMaterials.MoonlightResin.getFluid(500)).duration(400).EUt(GTValues.VA[GTValues.HV]).save(provider);
        GTRecipeBuilder.of(id("materials/arcane_ink"), GTRecipeTypes.MIXER_RECIPES)
                .inputItems(TagPrefix.dust, PollutionMaterials.StarlightPollen, 2).inputItems(TagPrefix.dust, PollutionMaterials.Salisundus)
                .inputItems(new ItemStack(Items.INK_SAC, 2))
                .inputFluids(PollutionMaterials.MoonlightResin.getFluid(250), PollutionMaterials.InfusedMagic.getFluid(144))
                .outputFluids(PollutionMaterials.ArcaneInk.getFluid(500)).duration(300).EUt(GTValues.VA[GTValues.HV]).save(provider);
    }

    private static void registerComponents(Consumer<FinishedRecipe> provider) {
        assembler("astral_resonance_coil").inputItems(TagPrefix.wireFine, GTMaterials.Silver, 8)
                .inputItems(ItemsAS.RESONATING_GEM.get()).inputItems(PollutionItems.RUBBER_SLIME.asStack())
                .inputFluids(PollutionMaterials.MoonlightResin.getFluid(250))
                .outputItems(PollutionItems.ASTRAL_RESONANCE_COIL.asStack()).duration(300).EUt(GTValues.VA[GTValues.HV]).save(provider);
        assembler("astral_lens_basic").inputItems(PollutionItems.SILVERED_GLASS_LENS.asStack(2))
                .inputItems(PollutionItems.ASTRAL_RESONANCE_COIL.asStack()).inputItems(TagPrefix.gem, PollutionMaterials.OpticalGradeAquamarine, 2)
                .inputItems(ItemsAS.GLASS_LENS.get()).inputFluids(starlight(500))
                .outputItems(PollutionItems.ASTRAL_LENS_BASIC.asStack()).duration(400).EUt(GTValues.VA[GTValues.HV]).save(provider);
        // Native tuned crystals bootstrap the chain before the industrial growth array exists.
        GTRecipeBuilder.of(id("components/attuned_crystal_wafer"), GTRecipeTypes.CUTTER_RECIPES)
                .inputItems(ItemsAS.ATTUNED_ROCK_CRYSTAL.get()).inputFluids(GTMaterials.Lubricant.getFluid(250))
                .outputItems(PollutionItems.ATTUNED_CRYSTAL_WAFER.asStack(4)).duration(500).EUt(GTValues.VA[GTValues.IV]).save(provider);
        assembler("astral_lens_advanced").inputItems(PollutionItems.ASTRAL_LENS_BASIC.asStack())
                .inputItems(PollutionItems.ATTUNED_CRYSTAL_WAFER.asStack()).inputItems(ItemsAS.CELESTIAL_CRYSTAL.get())
                .inputItems(GTItems.ROBOT_ARM_LuV.asStack()).inputItems(GTItems.SENSOR_LuV.asStack())
                .inputItems(TagPrefix.gearSmall, GTMaterials.Titanium, 4).inputFluids(starlight(1000))
                .outputItems(PollutionItems.ASTRAL_LENS_ADVANCED.asStack()).duration(600).EUt(GTValues.VA[GTValues.LuV]).save(provider);
        GTRecipeBuilder.of(id("components/celestial_calibration_core"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(PollutionItems.ASTRAL_LENS_ADVANCED.asStack()).inputItems(PollutionItems.ATTUNED_CRYSTAL_WAFER.asStack(2))
                .inputItems(ItemsAS.RESONATOR.get()).inputItems(PollutionItems.MAGIC_CIRCUIT_BOARD_LUV.asStack())
                .inputItems(GTItems.FIELD_GENERATOR_LuV.asStack()).inputFluids(starlight(1000), PollutionMaterials.InfusedOrder.getFluid(576))
                .outputItems(PollutionItems.CELESTIAL_CALIBRATION_CORE.asStack()).duration(800).EUt(GTValues.VA[GTValues.LuV]).save(provider);
        // A blank substrate can build the advanced hatch, but cannot authorize a constellation.
        assembler("constellation_data_wafer").inputItems(PollutionItems.STERILE_SLATE_BLANK.asStack())
                .inputItems(PollutionItems.ARCANE_INK_CAPSULE.asStack()).inputItems(PollutionItems.ASTRAL_LENS_BASIC.asStack())
                .inputFluids(PollutionMaterials.InfusedAura.getFluid(1000)).outputItems(PollutionItems.CONSTELLATION_DATA_WAFER.asStack())
                .duration(300).EUt(GTValues.VA[GTValues.IV]).save(provider);
        assembler("astral_lens_hatch").inputItems(GTItems.BATTERY_HULL_MV.asStack()).inputItems(GTItems.FIELD_GENERATOR_LV.asStack())
                .inputItems(GTItems.SENSOR_MV.asStack()).inputItems(PollutionItems.ASTRAL_LENS_BASIC.asStack())
                .inputItems(ItemsAS.RESONATOR.get()).inputItems(ItemsAS.CONSTELLATION_PAPER.get())
                .inputItems(PollutionItems.MAGIC_CIRCUIT_BOARD_MV.asStack()).outputItems(PollutionMachines.ASTRAL_LENS_HATCH.asStack())
                .duration(300).EUt(GTValues.VA[GTValues.MV]).save(provider);
        assembler("astral_lens_hatch_advanced").inputItems(GTMachines.HULL[GTValues.LuV].asStack())
                .inputItems(GTItems.FIELD_GENERATOR_LuV.asStack()).inputItems(GTItems.SENSOR_LuV.asStack())
                .inputItems(PollutionItems.ASTRAL_LENS_ADVANCED.asStack()).inputItems(PollutionItems.CONSTELLATION_DATA_WAFER.asStack())
                .inputItems(PollutionItems.CELESTIAL_CALIBRATION_CORE.asStack()).inputItems(PollutionItems.MAGIC_CIRCUIT_BOARD_LUV.asStack())
                .outputItems(PollutionMachines.ASTRAL_LENS_HATCH_ADVANCED.asStack()).duration(600).EUt(GTValues.VA[GTValues.LuV]).save(provider);
    }

    private static void registerCrystalLineage(Consumer<FinishedRecipe> provider) {
        GTRecipeBuilder.of(id("crystals/seed"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(ItemsAS.ROCK_CRYSTAL.get()).inputItems(PollutionItems.SILVERED_GLASS_LENS.asStack())
                .inputItems(TagPrefix.dust, PollutionMaterials.OpticalGradeAquamarine, 2)
                .inputFluids(PollutionMaterials.MoonlightResin.getFluid(250)).outputItems(PollutionItems.ROCK_CRYSTAL_SEED.asStack())
                .addData(AstralRecipeOutputs.TRANSFORM, "seed")
                .addData(MagicRecipeProperties.INFUSED_FLUID_PER_TICK, 2).addData(MagicRecipeProperties.MANA_PER_TICK, 20L)
                .addData(MagicRecipeProperties.VIS_PER_CRAFT, 32).duration(400).EUt(GTValues.VA[GTValues.HV]).save(provider);
        // The port's magic assembler hosts upstream crystal cultivation's embryo preparation.
        GTRecipeBuilder.of(id("crystals/embryo"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(PollutionItems.ROCK_CRYSTAL_SEED.asStack()).inputItems(ItemsAS.SHIFTING_STAR.get())
                .inputItems(TagPrefix.dust, PollutionMaterials.OpticalGradeAquamarine, 8)
                .inputFluids(PollutionMaterials.MoonlightResin.getFluid(250), starlight(500))
                .outputItems(PollutionItems.CELESTIAL_CRYSTAL_EMBRYO.asStack()).addData(AstralRecipeOutputs.TRANSFORM, "embryo")
                .addData(MagicRecipeProperties.INFUSED_FLUID_PER_TICK, 4).addData(MagicRecipeProperties.MANA_PER_TICK, 50L)
                .addData(MagicRecipeProperties.VIS_PER_CRAFT, 64)
                .duration(600).EUt(GTValues.VA[GTValues.LuV]).save(provider);
    }

    private static void registerProcessing(Consumer<FinishedRecipe> provider) {
        for (IConstellation constellation : ConstellationRegistry.getAllConstellations()) {
            String name = constellation.getSimpleName();
            ItemStack paper = new ItemStack(ItemsAS.CONSTELLATION_PAPER.get());
            ((ItemConstellationPaper) paper.getItem()).setConstellation(paper, constellation);
            var observation = GTRecipeBuilder.of(id("observation/" + name), AstralRecipeMaps.CELESTIAL_OBSERVATION_RECIPES)
                    .inputItems(PollutionItems.ATTUNED_CRYSTAL_WAFER.asStack()).inputItems(GTItems.TOOL_DATA_STICK.asStack())
                    .notConsumable(paper).inputFluids(PollutionMaterials.ArcaneInk.getFluid(100), starlight(250))
                    .outputItems(AstralNbtHelper.createDataWafer(constellation)).duration(300).EUt(GTValues.VA[GTValues.LuV]);
            MagicRecipeProperties.astralCondition(observation, AstralCondition.night(name, 0.10F)).save(provider);
            var calibration = GTRecipeBuilder.of(id("calibration/" + name), AstralRecipeMaps.CELESTIAL_CALIBRATION_RECIPES)
                    .inputItems(PollutionItems.CELESTIAL_CALIBRATION_CORE.asStack()).inputItems(AstralNbtHelper.createDataWafer(constellation))
                    .notConsumable(PollutionItems.ASTRAL_LENS_ADVANCED.asStack()).notConsumable(PollutionItems.CULTIVATED_CRYSTAL.asStack())
                    .inputFluids(starlight(1000), PollutionMaterials.InfusedOrder.getFluid(576))
                    .outputItems(AstralNbtHelper.createCalibratedCore(constellation)).addData(AstralRecipeOutputs.MIN_QUALITY, 70)
                    .duration(600).EUt(GTValues.VA[GTValues.LuV]);
            MagicRecipeProperties.astralCondition(calibration, AstralCondition.night(name, 0.15F)).save(provider);
            var growth = GTRecipeBuilder.of(id("growth/" + name), AstralRecipeMaps.CELESTIAL_CRYSTAL_GROWTH_RECIPES)
                    .inputItems(PollutionItems.CELESTIAL_CRYSTAL_EMBRYO.asStack()).inputItems(PollutionItems.SILVERED_GLASS_LENS.asStack())
                    .inputItems(ItemsAS.STARDUST.get()).inputItems(TagPrefix.dust, PollutionMaterials.OpticalGradeAquamarine, 2)
                    .notConsumable(AstralNbtHelper.createDataWafer(constellation))
                    .inputFluids(PollutionMaterials.MoonlightResin.getFluid(500), starlight(1000), PollutionMaterials.InfusedCrystal.getFluid(500))
                    .outputItems(PollutionItems.CULTIVATED_CRYSTAL.asStack()).addData(AstralRecipeOutputs.TRANSFORM, "growth")
                    .addData(AstralRecipeOutputs.TARGET, name).addData(MagicRecipeProperties.INFUSED_FLUID_PER_TICK, 6)
                    .addData(MagicRecipeProperties.MANA_PER_TICK, 100L).addData(MagicRecipeProperties.VIS_PER_CRAFT, 128)
                    .duration(1200).EUt(GTValues.VA[GTValues.LuV]);
            MagicRecipeProperties.astralCondition(growth, AstralCondition.night(name, 0.15F)).save(provider);
        }
    }

    private static void registerMachines(Consumer<FinishedRecipe> provider) {
        assembler("industrial_starlight_infuser").inputItems(GTMachines.HULL[GTValues.IV].asStack())
                .inputItems(BlocksAS.INFUSER.get().asItem()).inputItems(PollutionItems.ASTRAL_LENS_BASIC.asStack(2))
                .inputItems(GTItems.SENSOR_IV.asStack(2)).inputItems(PollutionItems.ASTRAL_RESONANCE_COIL.asStack(4))
                .outputItems(PollutionMachines.INDUSTRIAL_STARLIGHT_INFUSER.asStack()).duration(800).EUt(GTValues.VA[GTValues.IV]).save(provider);
        assembler("industrial_lightwell").inputItems(GTMachines.HULL[GTValues.IV].asStack())
                .inputItems(BlocksAS.WELL.get().asItem()).inputItems(PollutionItems.ASTRAL_RESONANCE_COIL.asStack(4))
                .inputItems(GTItems.ELECTRIC_PUMP_IV.asStack(2)).outputItems(PollutionMachines.INDUSTRIAL_LIGHTWELL.asStack())
                .duration(800).EUt(GTValues.VA[GTValues.IV]).save(provider);
        assembler("celestial_observation_array").inputItems(GTMachines.HULL[GTValues.LuV].asStack())
                .inputItems(PollutionItems.ASTRAL_LENS_ADVANCED.asStack(2)).inputItems(GTItems.SENSOR_LuV.asStack(2))
                .inputItems(GTItems.EMITTER_LuV.asStack()).inputItems(GTItems.TOOL_DATA_STICK.asStack(4))
                .inputItems(PollutionItems.MAGIC_CIRCUIT_BOARD_LUV.asStack(4)).inputItems(ItemsAS.RESONATOR.get())
                .inputFluids(starlight(2000)).outputItems(PollutionMachines.CELESTIAL_OBSERVATION_ARRAY.asStack())
                .duration(800).EUt(GTValues.VA[GTValues.LuV]).save(provider);
        assembler("celestial_calibration_matrix").inputItems(GTMachines.HULL[GTValues.LuV].asStack())
                .inputItems(PollutionItems.ASTRAL_LENS_ADVANCED.asStack(4)).inputItems(GTItems.FIELD_GENERATOR_LuV.asStack(2))
                .inputItems(GTItems.ROBOT_ARM_LuV.asStack(2)).inputItems(PollutionItems.CELESTIAL_CALIBRATION_CORE.asStack())
                .inputItems(PollutionItems.MAGIC_CIRCUIT_BOARD_LUV.asStack(4)).inputItems(PollutionItems.CONSTELLATION_DATA_WAFER.asStack())
                .inputFluids(starlight(4000)).outputItems(PollutionMachines.CELESTIAL_CALIBRATION_MATRIX.asStack())
                .duration(1200).EUt(GTValues.VA[GTValues.LuV]).save(provider);
        assembler("celestial_crystal_growth_array").inputItems(GTMachines.HULL[GTValues.LuV].asStack())
                .inputItems(PollutionItems.ASTRAL_LENS_ADVANCED.asStack(4)).inputItems(PollutionItems.CELESTIAL_CALIBRATION_CORE.asStack())
                .inputItems(PollutionItems.ATTUNED_CRYSTAL_WAFER.asStack(2)).inputItems(PollutionItems.MAGIC_CIRCUIT_BOARD_LUV.asStack(4))
                .inputItems(GTItems.FIELD_GENERATOR_LuV.asStack(2)).inputItems(ItemsAS.RESONATOR.get()).inputFluids(starlight(4000))
                .outputItems(PollutionMachines.CELESTIAL_CRYSTAL_GROWTH_ARRAY.asStack()).duration(1200).EUt(GTValues.VA[GTValues.LuV]).save(provider);
        for (ConstellationTowerDefinition tower : ConstellationTowerDefinition.values()) {
            IConstellation constellation = AstralNbtHelper.findConstellation(tower.getId());
            GTRecipeBuilder.of(id("towers/" + tower.getId()), GTRecipeTypes.ASSEMBLY_LINE_RECIPES)
                    .inputItems(GTMachines.HULL[GTValues.UHV].asStack()).inputItems(PollutionItems.MAGIC_CIRCUIT_BOARD_UHV.asStack(4))
                    .inputItems(PollutionItems.ASTRAL_LENS_ADVANCED.asStack(8)).inputItems(PollutionItems.CELESTIAL_CALIBRATION_CORE.asStack(4))
                    .inputItems(PollutionItems.HARMONIZING_RUNE_CORE.asStack(2)).inputItems(PollutionItems.ASTRAL_RESONANCE_COIL.asStack(16))
                    .inputItems((GTItems.FIELD_GENERATOR_UHV != null ? GTItems.FIELD_GENERATOR_UHV : GTItems.FIELD_GENERATOR_UV).asStack(4))
                    .inputItems((GTItems.EMITTER_UHV != null ? GTItems.EMITTER_UHV : GTItems.EMITTER_UV).asStack(8))
                    .inputItems((GTItems.SENSOR_UHV != null ? GTItems.SENSOR_UHV : GTItems.SENSOR_UV).asStack(8))
                    .inputItems(ItemsAS.RESONATOR.get()).inputItems(AstralNbtHelper.createDataWafer(constellation))
                    .inputFluids(starlight(32000), PollutionMaterials.Starrymansus.getFluid(8000), PollutionMaterials.DimensionalTransformingAgent.getFluid(2000))
                    .outputItems(PollutionMachines.CONSTELLATION_TOWER[tower.ordinal()].asStack())
                    .duration(2400).EUt(GTValues.VA[GTValues.UHV]).save(provider);
        }
    }

    private static GTRecipeBuilder assembler(String name) { return GTRecipeBuilder.of(id("components/" + name), GTRecipeTypes.ASSEMBLER_RECIPES); }
    private static FluidStack starlight(int amount) { return new FluidStack(FluidsAS.LIQUID_STARLIGHT_SOURCE.get(), amount); }
    private static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath("pollution", "astral/" + path); }
}
