package meowmel.pollution.loaders.recipes;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import meowmel.pollution.api.recipes.PORecipeMaps;
import meowmel.pollution.api.unification.PollutionMaterials;
import meowmel.pollution.common.block.PollutionMagicBlocks;
import meowmel.pollution.common.item.PollutionItems;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

/**
 * Node fusion recipes.
 *
 * <p>Upstream built the node reactors and the fusion fuel chain from GTQT
 * materials and the {@code POHyper} casing family, none of which are part of
 * the port. Substitutions (kept consistent with the tracker table):</p>
 * <ul>
 *   <li>HyperdimensionalSilver -&gt; NaquadahAlloy, KQGold -&gt; TungstenSteel,
 *       SentientMetal/ExistingNexus -&gt; Tritanium, FadingNexus -&gt; NaquadahAlloy,
 *       BindingMetal/BloodOfAvernus -&gt; TungstenSteel, AethericDarkSteel -&gt; NaquadahAlloy,
 *       IizunamaruElectrum -&gt; Electrum, Terrasteel -&gt; TungstenSteel</li>
 *   <li>ErichAura -&gt; InfusedAura, DimensionalTransformingAgent -&gt; InfusedSpatio,
 *       StarmetalAlloy -&gt; NaquadahAlloy, BlockLifeEssence -&gt; InfusedLife,
 *       VoidMetal -&gt; InfusedVoid, plasma outputs -&gt; fluid outputs</li>
 *   <li>POHyper casings -&gt; Void Prism; TC morphic resonator -&gt; vis checker;
 *       GT control components -&gt; magic circuits</li>
 *   <li>Fusion start cost ({@code EUToStart}) is not enforced by the ported
 *       reactors (documented in the tracker)</li>
 * </ul>
 */
public final class NodeFusionRecipes {

    private NodeFusionRecipes() {}

    public static void init(Consumer<FinishedRecipe> provider) {
        nodeReactorAssembly(provider);
        fusionFuels(provider);
    }

    private static void nodeReactorAssembly(Consumer<FinishedRecipe> provider) {
        GTRecipeBuilder.of(id("node_reactor_luv"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(new ItemStack(PollutionMagicBlocks.BEAM_CORE_1.get()))
                .inputItems(PollutionItems.get("core_of_idea").get(), 2)
                .inputItems(PollutionItems.VIS_CHECKER.get(), 32)
                .inputItems(PollutionItems.MAGIC_CIRCUIT_LUV.get())
                .inputItems(ChemicalHelper.get(TagPrefix.screw, GTMaterials.TungstenSteel, 8))
                .inputItems(ChemicalHelper.get(TagPrefix.frameGt, GTMaterials.NaquadahAlloy, 4))
                .inputItems(ChemicalHelper.get(TagPrefix.frameGt, GTMaterials.Electrum, 4))
                .inputFluids(GTMaterials.TungstenSteel.getFluid(576))
                .inputFluids(PollutionMaterials.InfusedLight.getFluid(8000))
                .inputFluids(PollutionMaterials.InfusedDark.getFluid(8000))
                .outputItems(new ItemStack(PollutionMagicBlocks.VOID_PRISM.get(), 4))
                .EUt(GTValues.VA[GTValues.LuV])
                .duration(400)
                .save(provider);

        GTRecipeBuilder.of(id("node_reactor_zpm"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(new ItemStack(PollutionMagicBlocks.VOID_PRISM.get()))
                .inputItems(PollutionItems.get("elucidator_of_four_causes").get())
                .inputItems(PollutionItems.STARRY_RUNE.get(), 2)
                .inputItems(PollutionItems.MAGIC_CIRCUIT_ZPM.get())
                .inputItems(ChemicalHelper.get(TagPrefix.screw, GTMaterials.TungstenSteel, 4))
                .inputItems(ChemicalHelper.get(TagPrefix.frameGt, GTMaterials.NaquadahAlloy, 2))
                .inputFluids(GTMaterials.TungstenSteel.getFluid(576))
                .inputFluids(GTMaterials.Tritanium.getFluid(576))
                .inputFluids(PollutionMaterials.InfusedAura.getFluid(2000))
                .outputItems(new ItemStack(PollutionMagicBlocks.VOID_PRISM.get(), 2))
                .EUt(GTValues.VA[GTValues.ZPM])
                .duration(400)
                .save(provider);

        GTRecipeBuilder.of(id("node_reactor_uv"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(new ItemStack(PollutionMagicBlocks.VOID_PRISM.get()))
                .inputItems(PollutionItems.get("symptomatic_vis_data_link").get())
                .inputItems(PollutionItems.STARRY_RUNE.get(), 4)
                .inputItems(PollutionItems.MAGIC_CIRCUIT_UV.get())
                .inputItems(ChemicalHelper.get(TagPrefix.screw, GTMaterials.Tritanium, 4))
                .inputItems(ChemicalHelper.get(TagPrefix.frameGt, GTMaterials.NaquadahAlloy, 2))
                .inputFluids(GTMaterials.Tritanium.getFluid(576))
                .inputFluids(PollutionMaterials.InfusedAura.getFluid(4000))
                .outputItems(new ItemStack(PollutionMagicBlocks.SPELL_PRISM_VOID.get(), 2))
                .EUt(GTValues.VA[GTValues.UV])
                .duration(400)
                .save(provider);

        GTRecipeBuilder.of(id("node_reactor_assembly"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(PollutionItems.get("bottle_of_phlogistonic_oneness").get(), 8)
                .inputItems(PollutionItems.get("auto_elenchus_device").get(), 4)
                .inputItems(ChemicalHelper.get(TagPrefix.plate, GTMaterials.NaquadahAlloy, 32))
                .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Neutronium, 16))
                .inputItems(new ItemStack(GTBlocks.SUPERCONDUCTING_COIL.get(), 4))
                .inputFluids(GTMaterials.NaquadahAlloy.getFluid(36000))
                .inputFluids(GTMaterials.TungstenSteel.getFluid(36000))
                .inputFluids(PollutionMaterials.InfusedAura.getFluid(10000))
                .EUt(GTValues.VA[GTValues.ZPM])
                .duration(40000)
                .save(provider);
    }

    private static void fusionFuels(Consumer<FinishedRecipe> provider) {
        fusion(provider, "sentient_metal", PollutionMaterials.InfusedSense,
                GTMaterials.TungstenSteel, GTMaterials.Tritanium, GTValues.LuV);
        fusion(provider, "binding_metal", PollutionMaterials.InfusedSoul,
                GTMaterials.NaquadahAlloy, GTMaterials.TungstenSteel, GTValues.LuV);
        fusion(provider, "hyperdimensional_silver", PollutionMaterials.InfusedAura,
                GTMaterials.Silver, GTMaterials.NaquadahAlloy, GTValues.LuV);
        fusion(provider, "kq_gold", PollutionMaterials.InfusedAura,
                GTMaterials.Gold, GTMaterials.TungstenSteel, GTValues.LuV);
        fusion(provider, "dimensional_transforming_agent", PollutionMaterials.InfusedAura,
                GTMaterials.Water, PollutionMaterials.InfusedSpatio, GTValues.IV);

        GTRecipeBuilder.of(id("erich_aura"), PORecipeMaps.NODE_MAGIC_FUSION_RECIPES)
                .inputFluids(PollutionMaterials.InfusedMagic.getFluid(144))
                .inputFluids(GTMaterials.Water.getFluid(1000))
                .outputFluids(PollutionMaterials.InfusedAura.getFluid(1000))
                .EUt(GTValues.VA[GTValues.EV])
                .duration(200)
                .save(provider);

        fusion(provider, "existing_nexus", PollutionMaterials.InfusedLife,
                GTMaterials.TungstenSteel, GTMaterials.Tritanium, GTValues.ZPM);
        fusion(provider, "fading_nexus", PollutionMaterials.InfusedLife,
                PollutionMaterials.InfusedVoid, GTMaterials.NaquadahAlloy, GTValues.ZPM);
        fusion(provider, "tritanium", PollutionMaterials.InfusedInstrument,
                GTMaterials.Titanium, GTMaterials.Tritanium, GTValues.ZPM);
        fusion(provider, "neutronium", GTMaterials.Naquadria,
                PollutionMaterials.InfusedEnergy, GTMaterials.Neutronium, GTValues.ZPM);

        GTRecipeBuilder.of(id("starmetal_alloy"), PORecipeMaps.NODE_MAGIC_FUSION_RECIPES)
                .inputFluids(PollutionMaterials.InfusedEntropy.getFluid(144))
                .inputFluids(PollutionMaterials.InfusedOrder.getFluid(144))
                .outputFluids(GTMaterials.NaquadahAlloy.getFluid(576))
                .EUt(GTValues.VA[GTValues.UHV])
                .duration(200)
                .save(provider);
    }

    private static void fusion(Consumer<FinishedRecipe> provider, String name,
                               com.gregtechceu.gtceu.api.data.chemical.material.Material first,
                               com.gregtechceu.gtceu.api.data.chemical.material.Material second,
                               com.gregtechceu.gtceu.api.data.chemical.material.Material output,
                               int tier) {
        GTRecipeBuilder.of(id(name), PORecipeMaps.NODE_MAGIC_FUSION_RECIPES)
                .inputFluids(first.getFluid(1000))
                .inputFluids(second.getFluid(288))
                .outputFluids(output.getFluid(144))
                .EUt(GTValues.VA[tier])
                .duration(200)
                .save(provider);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("pollution", "node_fusion/" + path);
    }
}
