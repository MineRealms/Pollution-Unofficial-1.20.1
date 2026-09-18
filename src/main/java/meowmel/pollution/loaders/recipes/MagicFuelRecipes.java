package meowmel.pollution.loaders.recipes;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import meowmel.pollution.Pollution;
import meowmel.pollution.api.recipes.PORecipeMaps;
import meowmel.pollution.api.unification.PollutionMaterials;
import meowmel.pollution.common.item.PollutionItems;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.ModList;

import java.util.function.Consumer;

/**
 * Magic fuel chain.
 *
 * <p>Upstream produced the fuels from the GTQT chain (MethylFormate,
 * BlazingPyrotheum, hydrazine sulfate, TetraethylLead, ...). Those materials do
 * not exist in GTCEu Modern, so the port re-routes the same fuel values through
 * GTCEu-native intermediates:</p>
 * <ul>
 *   <li>MethylFormate -&gt; AmmoniumFormate</li>
 *   <li>BlazingPyrotheum + hydrazine sulfate -&gt; RocketFuel + Dimethylhydrazine</li>
 *   <li>ChlorineTrifluoride -&gt; HydrofluoricAcid (AntimonyTrifluoride has no
 *       fluid in GTCEu 7.5.3 and is only kept as a fallback when a fluid exists)</li>
 *   <li>TetraethylLead -&gt; LeadZincSolution</li>
 *   <li>ROCKET_ENGINE_RECIPES -&gt; MAGIC_TURBINE_FUELS (GregTech 7.5.3 has no
 *       rocket engine map) plus, when the optional GTNN/GCYR mods are loaded,
 *       the real rocket maps: {@code GTNNRecipeTypes.ROCKET_ENGINE_RECIPES} and
 *       {@code GCYRRecipeTypes.ROCKET_FUEL_RECIPES} (see
 *       {@link GTNNRocketFuels} / {@link GCYRRocketFuels})</li>
 * </ul>
 *
 * <p>Every recipe is registered defensively: GTCEu materials may exist without
 * a fluid property (e.g. dust-only intermediates), and {@code Material#getFluid}
 * throws in that case. Recipes whose fluids are unavailable are skipped with a
 * log line instead of crashing the recipe-loading phase.</p>
 */
public final class MagicFuelRecipes {

    private MagicFuelRecipes() {}

    public static void init(Consumer<FinishedRecipe> provider) {
        combustionGenerator(provider);
        propellants(provider);
        rocketEngines(provider);
    }

    /**
     * Rocket fuel registration on GTNN/GCYR. Both mods are optional
     * dependencies, so the implementation classes are only resolved when the
     * mod is loaded (lazy class resolution keeps the references in
     * {@link #rocketEngines} safe when the mods are absent).
     *
     * <p>The upstream 1.12.2 {@code ROCKET_ENGINE_RECIPES} entries could not be
     * ported directly because GTCEu Modern has no rocket engine recipe map; the
     * initial port substituted {@code MAGIC_TURBINE_FUELS} (kept, it is the
     * Pollution magic turbine's own fuel map). GTNN restores the real map, so
     * the two upstream rocket recipes are registered there, and GCYR receives
     * the same propellants in its own {@code ROCKET_FUEL_RECIPES} registry.</p>
     */
    private static void rocketEngines(Consumer<FinishedRecipe> provider) {
        if (ModList.get().isLoaded("gtnn")) {
            GTNNRocketFuels.init(provider);
        }
        if (ModList.get().isLoaded("gcyr")) {
            GCYRRocketFuels.init(provider);
        }
    }

    private static void combustionGenerator(Consumer<FinishedRecipe> provider) {
        // 魔力抗爆焦化硝基苯
        FluidStack formate = fluid(GTMaterials.AmmoniumFormate, 1000);
        FluidStack ethanol = fluid(GTMaterials.Ethanol, 1000);
        FluidStack nitrobenzene = fluid(GTMaterials.Nitrobenzene, 10000);
        FluidStack infusedEnergy = fluid(PollutionMaterials.InfusedEnergy, 1152);
        if (formate != null && ethanol != null && nitrobenzene != null && infusedEnergy != null) {
            GTRecipeBuilder.of(id("magic_nitrobenzene"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                    .inputFluids(formate)
                    .inputFluids(ethanol)
                    .inputFluids(nitrobenzene)
                    .inputFluids(infusedEnergy)
                    .notConsumable(PollutionItems.COKING_CATALYST_CORE.get())
                    .outputFluids(PollutionMaterials.MagicNitrobenzene.getFluid(16000))
                    .duration(200)
                    .EUt(GTValues.VA[GTValues.HV])
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping magic_nitrobenzene recipe: a required fluid is missing");
        }

        GTRecipeBuilder.of(id("magic_nitrobenzene_fuel"), GTRecipeTypes.COMBUSTION_GENERATOR_FUELS)
                .inputFluids(PollutionMaterials.MagicNitrobenzene.getFluid(1))
                .duration(90)
                .EUt(-GTValues.VA[GTValues.HV] / 4)
                .save(provider);
    }

    private static void propellants(Consumer<FinishedRecipe> provider) {
        // 焚天烈焰推进剂
        FluidStack rocketFuel = fluid(GTMaterials.RocketFuel, 1000);
        FluidStack dimethylhydrazine = fluid(GTMaterials.Dimethylhydrazine, 1000);
        FluidStack nitricAcid = fluid(GTMaterials.NitricAcid, 10000);
        FluidStack infusedEnergy = fluid(PollutionMaterials.InfusedEnergy, 1152);
        if (rocketFuel != null && dimethylhydrazine != null && nitricAcid != null && infusedEnergy != null) {
            GTRecipeBuilder.of(id("infernal_blaze_propellant"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                    .inputFluids(rocketFuel)
                    .inputFluids(dimethylhydrazine)
                    .inputFluids(nitricAcid)
                    .inputFluids(infusedEnergy)
                    .inputItems(com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper.get(
                            com.gregtechceu.gtceu.api.data.tag.TagPrefix.dust, GTMaterials.Aluminium, 8))
                    .notConsumable(PollutionItems.COKING_CATALYST_CORE.get())
                    .outputFluids(PollutionMaterials.InfernalBlazePropellant.getFluid(16000))
                    .duration(800)
                    .EUt(GTValues.VA[GTValues.HV])
                    .save(provider);

            GTRecipeBuilder.of(id("infernal_blaze_propellant_fuel"), PORecipeMaps.MAGIC_TURBINE_FUELS)
                    .inputFluids(PollutionMaterials.InfernalBlazePropellant.getFluid(1))
                    .duration(4 * 20)
                    .EUt(-GTValues.VA[GTValues.EV])
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping infernal_blaze_propellant recipe: a required fluid is missing");
        }

        // 龙脉星轨燃剂 (ChlorineTrifluoride -> HydrofluoricAcid; LeadZincSolution -> TetraethylLead slot)
        FluidStack hydrofluoricAcid = fluid(GTMaterials.HydrofluoricAcid, 1000);
        FluidStack leadZincSolution = fluid(GTMaterials.LeadZincSolution, 1000);
        if (hydrofluoricAcid != null && leadZincSolution != null && dimethylhydrazine != null && infusedEnergy != null) {
            GTRecipeBuilder.of(id("dragon_pulse_fuel"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                    .inputFluids(hydrofluoricAcid)
                    .inputFluids(leadZincSolution)
                    .inputFluids(fluid(GTMaterials.Dimethylhydrazine, 10000))
                    .inputItems(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DRAGON_BREATH, 4))
                    .inputFluids(infusedEnergy)
                    .notConsumable(PollutionItems.COKING_CATALYST_CORE.get())
                    .outputFluids(PollutionMaterials.DragonPulseFuel.getFluid(16000))
                    .duration(800)
                    .EUt(GTValues.VA[GTValues.IV])
                    .save(provider);

            GTRecipeBuilder.of(id("dragon_pulse_fuel_burn"), PORecipeMaps.MAGIC_TURBINE_FUELS)
                    .inputFluids(PollutionMaterials.DragonPulseFuel.getFluid(1))
                    .duration(8 * 20)
                    .EUt(-GTValues.VA[GTValues.IV])
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping dragon_pulse_fuel recipe: a required fluid is missing");
        }
    }

    /** @return the fluid stack, or null when the material has no fluid in this GTCEu build */
    private static FluidStack fluid(Material material, int amount) {
        if (material == null || !material.hasFluid()) {
            return null;
        }
        return material.getFluid(amount);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("pollution", "magic_fuel/" + path);
    }
}
