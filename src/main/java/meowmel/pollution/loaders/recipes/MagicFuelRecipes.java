package meowmel.pollution.loaders.recipes;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import meowmel.pollution.api.recipes.PORecipeMaps;
import meowmel.pollution.api.unification.PollutionMaterials;
import meowmel.pollution.common.item.PollutionItems;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;

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
 *   <li>ChlorineTrifluoride -&gt; AntimonyTrifluoride</li>
 *   <li>TetraethylLead -&gt; LeadZincSolution</li>
 *   <li>ROCKET_ENGINE_RECIPES -&gt; COMBUSTION_GENERATOR_FUELS + MAGIC_TURBINE_FUELS
 *       (GregTech 7.5.3 has no rocket engine map)</li>
 * </ul>
 */
public final class MagicFuelRecipes {

    private MagicFuelRecipes() {}

    public static void init(Consumer<FinishedRecipe> provider) {
        combustionGenerator(provider);
        propellants(provider);
    }

    private static void combustionGenerator(Consumer<FinishedRecipe> provider) {
        // 魔力抗爆焦化硝基苯
        GTRecipeBuilder.of(id("magic_nitrobenzene"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                .inputFluids(GTMaterials.AmmoniumFormate.getFluid(1000))
                .inputFluids(GTMaterials.Ethanol.getFluid(1000))
                .inputFluids(GTMaterials.Nitrobenzene.getFluid(10000))
                .inputFluids(PollutionMaterials.InfusedEnergy.getFluid(1152))
                .notConsumable(PollutionItems.COKING_CATALYST_CORE.get())
                .outputFluids(PollutionMaterials.MagicNitrobenzene.getFluid(16000))
                .duration(200)
                .EUt(GTValues.VA[GTValues.HV])
                .save(provider);

        GTRecipeBuilder.of(id("magic_nitrobenzene_fuel"), GTRecipeTypes.COMBUSTION_GENERATOR_FUELS)
                .inputFluids(PollutionMaterials.MagicNitrobenzene.getFluid(1))
                .duration(90)
                .EUt(GTValues.VA[GTValues.HV] / 4)
                .save(provider);
    }

    private static void propellants(Consumer<FinishedRecipe> provider) {
        // 焚天烈焰推进剂
        GTRecipeBuilder.of(id("infernal_blaze_propellant"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                .inputFluids(GTMaterials.RocketFuel.getFluid(1000))
                .inputFluids(GTMaterials.Dimethylhydrazine.getFluid(1000))
                .inputFluids(GTMaterials.NitricAcid.getFluid(10000))
                .inputFluids(PollutionMaterials.InfusedEnergy.getFluid(1152))
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
                .EUt(GTValues.VA[GTValues.EV])
                .save(provider);

        // 龙脉星轨燃剂
        GTRecipeBuilder.of(id("dragon_pulse_fuel"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                .inputFluids(GTMaterials.AntimonyTrifluoride.getFluid(1000))
                .inputFluids(GTMaterials.LeadZincSolution.getFluid(1000))
                .inputFluids(GTMaterials.Dimethylhydrazine.getFluid(10000))
                .inputItems(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DRAGON_BREATH, 4))
                .inputFluids(PollutionMaterials.InfusedEnergy.getFluid(1152))
                .notConsumable(PollutionItems.COKING_CATALYST_CORE.get())
                .outputFluids(PollutionMaterials.DragonPulseFuel.getFluid(16000))
                .duration(800)
                .EUt(GTValues.VA[GTValues.IV])
                .save(provider);

        GTRecipeBuilder.of(id("dragon_pulse_fuel_burn"), PORecipeMaps.MAGIC_TURBINE_FUELS)
                .inputFluids(PollutionMaterials.DragonPulseFuel.getFluid(1))
                .duration(8 * 20)
                .EUt(GTValues.VA[GTValues.IV])
                .save(provider);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("pollution", "magic_fuel/" + path);
    }
}
