package meowmel.pollution.loaders.recipes;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import dev.arbor.gtnn.data.GTNNMaterials;
import dev.arbor.gtnn.data.GTNNRecipeTypes;
import meowmel.pollution.api.recipes.PORecipeMaps;
import meowmel.pollution.api.unification.PollutionMaterials;
import meowmel.pollution.common.item.PollutionItems;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

/**
 * GTNN rocket integration (only loaded when {@code gtnn} is present; see
 * {@link MagicFuelRecipes#rocketEngines}).
 *
 * <p>GTNN provides the {@code gtnn:rocket_engine} {@code GTRecipeType} that
 * GTCEu Modern itself lacks, verified with javap against
 * {@code gtnn-1.3.10}: {@code GTNNRecipeTypes.INSTANCE.getROCKET_ENGINE_RECIPES()}
 * and its own fuels use {@code .inputFluids(...).EUt(-V[EV]).duration(...)}
 * (negative EUt = generator-style recipe, duration in ticks).</p>
 *
 * <p>The two upstream ({@code ROCKET_ENGINE_RECIPES}, 1.12.2) entries are
 * transplanted 1:1 onto that map:</p>
 * <ul>
 *   <li>{@code InfernalBlazePropellant} 9 mB, 2048 EU/t, 80 ticks</li>
 *   <li>{@code DragonPulseFuel} 6 mB, 2048 EU/t, 160 ticks</li>
 * </ul>
 *
 * <p>In addition, GTNN-flavoured variants of the two magic propellant
 * synthesis recipes are added to {@code PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES}.
 * They are additive, not replacements: the GTCEu-native chain in
 * {@link MagicFuelRecipes} must keep working when GTNN is absent. Documented
 * substitutions against the upstream GTQT inputs:</p>
 * <ul>
 *   <li>BlazingPyrotheum -&gt; {@code GTNNMaterials.RP1} (RP-1 kerosene is the
 *       closest GTNN rocket fuel base; GTNN has no pyrotheum)</li>
 *   <li>hydrazine sulfate -&gt; {@code GTNNMaterials.Hydrazine} (upstream built
 *       hydrazine sulfate from Hydrazine; GTNN ships plain Hydrazine)</li>
 *   <li>Dimethylhydrazine -&gt; {@code GTNNMaterials.UDMH} (unsymmetrical
 *       dimethylhydrazine, the same compound under GTNN's canonical name)</li>
 *   <li>ChlorineTrifluoride -&gt; {@code GTNNMaterials.BromineTrifluoride}
 *       (GTNN's interhalogen fluoride oxidizer, the closest analogue of ClF3)</li>
 *   <li>TetraethylLead -&gt; {@code GTNNMaterials.MethylhydrazineNitrateRocketFuel}
 *       (a storable hypergolic propellant; GTNN has no tetraethyl lead)</li>
 * </ul>
 */
public final class GTNNRocketFuels {

    private GTNNRocketFuels() {}

    public static void init(Consumer<FinishedRecipe> provider) {
        rocketEngines(provider);
        gtnnPropellantVariants(provider);
    }

    private static void rocketEngines(Consumer<FinishedRecipe> provider) {
        // 焚天烈焰推进剂: upstream 9 mB, EUt 2048, 4 s.
        GTNNRecipeTypes.INSTANCE.getROCKET_ENGINE_RECIPES()
                .recipeBuilder(id("rocket_engine/infernal_blaze_propellant"))
                .inputFluids(PollutionMaterials.InfernalBlazePropellant.getFluid(9))
                .EUt(-GTValues.V[GTValues.EV])
                .duration(4 * 20)
                .save(provider);

        // 龙脉星轨燃剂: upstream 6 mB, EUt 2048, 8 s.
        GTNNRecipeTypes.INSTANCE.getROCKET_ENGINE_RECIPES()
                .recipeBuilder(id("rocket_engine/dragon_pulse_fuel"))
                .inputFluids(PollutionMaterials.DragonPulseFuel.getFluid(6))
                .EUt(-GTValues.V[GTValues.EV])
                .duration(8 * 20)
                .save(provider);
    }

    private static void gtnnPropellantVariants(Consumer<FinishedRecipe> provider) {
        // 焚天烈焰推进剂 (GTNN materials): RP1 + UDMH + nitric acid + aluminium.
        GTRecipeBuilder.of(id("magic_fuel/infernal_blaze_propellant_gtnn"),
                        PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                .inputFluids(GTNNMaterials.RP1.getFluid(1000))
                .inputFluids(GTNNMaterials.UDMH.getFluid(1000))
                .inputFluids(GTMaterials.NitricAcid.getFluid(10000))
                .inputFluids(PollutionMaterials.InfusedEnergy.getFluid(1152))
                .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Aluminium, 8))
                .notConsumable(PollutionItems.COKING_CATALYST_CORE.get())
                .outputFluids(PollutionMaterials.InfernalBlazePropellant.getFluid(16000))
                .duration(800)
                .EUt(GTValues.VA[GTValues.HV])
                .save(provider);

        // 龙脉星轨燃剂 (GTNN materials): BrF3 + methylhydrazine nitrate rocket
        // fuel + UDMH + dragon breath.
        GTRecipeBuilder.of(id("magic_fuel/dragon_pulse_fuel_gtnn"),
                        PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                .inputFluids(GTNNMaterials.BromineTrifluoride.getFluid(1000))
                .inputFluids(GTNNMaterials.MethylhydrazineNitrateRocketFuel.getFluid(1000))
                .inputFluids(GTNNMaterials.UDMH.getFluid(10000))
                .inputItems(new net.minecraft.world.item.ItemStack(Items.DRAGON_BREATH, 4))
                .inputFluids(PollutionMaterials.InfusedEnergy.getFluid(1152))
                .notConsumable(PollutionItems.COKING_CATALYST_CORE.get())
                .outputFluids(PollutionMaterials.DragonPulseFuel.getFluid(16000))
                .duration(800)
                .EUt(GTValues.VA[GTValues.IV])
                .save(provider);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("pollution", path);
    }
}
