package meowmel.pollution.loaders.recipes;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import dev.arbor.gtnn.data.GTNNMaterials;
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
 *   <li>SentientMetal / BindingMetal / ExistingNexus / FadingNexus /
 *       AethericDarkSteel / IizunamaruElectrum / HyperdimensionalSilver /
 *       KQGold are ported and used directly; BloodOfAvernus -&gt; TungstenSteel,
 *       Terrasteel -&gt; GTNN TerraSteel</li>
 *   <li>ErichAura -&gt; InfusedAura, StarmetalAlloy -&gt; NaquadahAlloy,
 *       BlockLifeEssence -&gt; InfusedLife, VoidMetal -&gt; InfusedVoid,
 *       plasma outputs -&gt; fluid outputs</li>
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
        // // 上游: screw BloodOfAvernus -> 本移植版: TungstenSteel screw
        GTRecipeBuilder.of(id("node_reactor_luv"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(new ItemStack(PollutionMagicBlocks.BEAM_CORE_1.get()))
                .inputItems(PollutionItems.get("core_of_idea").get(), 2)
                .inputItems(PollutionItems.VIS_CHECKER.get(), 32)
                .inputItems(PollutionItems.MAGIC_CIRCUIT_LUV.get())
                .inputItems(ChemicalHelper.get(TagPrefix.screw, GTMaterials.TungstenSteel, 8))
                .inputItems(ChemicalHelper.get(TagPrefix.frameGt, PollutionMaterials.AethericDarkSteel, 4))
                .inputItems(ChemicalHelper.get(TagPrefix.frameGt, PollutionMaterials.IizunamaruElectrum, 4))
                .inputFluids(PollutionMaterials.KQGold.getFluid(576))
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
                .inputFluids(PollutionMaterials.BindingMetal.getFluid(576))
                .inputFluids(PollutionMaterials.SentientMetal.getFluid(576))
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
                .inputFluids(PollutionMaterials.ExistingNexus.getFluid(576))
                .inputFluids(PollutionMaterials.FadingNexus.getFluid(576))
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
                .inputFluids(PollutionMaterials.AethericDarkSteel.getFluid(36000))
                .inputFluids(PollutionMaterials.KQGold.getFluid(36000))
                .inputFluids(PollutionMaterials.InfusedAura.getFluid(10000))
                .EUt(GTValues.VA[GTValues.ZPM])
                .duration(40000)
                .save(provider);
    }

    private static void fusionFuels(Consumer<FinishedRecipe> provider) {
        // 感知金属：InfusedSense + KQGold -> SentientMetal
        fusion(provider, "sentient_metal", PollutionMaterials.InfusedSense,
                PollutionMaterials.KQGold, PollutionMaterials.SentientMetal, GTValues.LuV);
        // 缚束金属：InfusedSoul + HyperdimensionalSilver -> BindingMetal
        fusion(provider, "binding_metal", PollutionMaterials.InfusedSoul,
                PollutionMaterials.HyperdimensionalSilver, PollutionMaterials.BindingMetal, GTValues.LuV);
        // 超次元秘银：ErichAura -> InfusedAura；输出真实 HyperdimensionalSilver
        fusion(provider, "hyperdimensional_silver", PollutionMaterials.InfusedAura,
                GTMaterials.Silver, PollutionMaterials.HyperdimensionalSilver, GTValues.LuV);
        // 刻金：ErichAura -> InfusedAura；输出真实 KQGold
        fusion(provider, "kq_gold", PollutionMaterials.InfusedAura,
                GTMaterials.Gold, PollutionMaterials.KQGold, GTValues.LuV);
        // 次元改造剂：真实材料
        fusion(provider, "dimensional_transforming_agent", PollutionMaterials.InfusedAura,
                GTMaterials.Water, PollutionMaterials.DimensionalTransformingAgent, GTValues.IV);

        GTRecipeBuilder.of(id("erich_aura"), PORecipeMaps.NODE_MAGIC_FUSION_RECIPES)
                .inputFluids(PollutionMaterials.InfusedMagic.getFluid(144))
                .inputFluids(GTMaterials.Water.getFluid(1000))
                .outputFluids(PollutionMaterials.InfusedAura.getFluid(1000))
                .EUt(GTValues.VA[GTValues.EV])
                .duration(200)
                .save(provider);

        // 既存之枢：生命源质 -> InfusedLife；Terrasteel -> GTNN TerraSteel
        fusion(provider, "existing_nexus", PollutionMaterials.InfusedLife,
                GTNNMaterials.TerraSteel, PollutionMaterials.ExistingNexus, GTValues.ZPM);
        // 消逝之枢：VoidMetal -> InfusedVoid
        fusion(provider, "fading_nexus", PollutionMaterials.InfusedLife,
                PollutionMaterials.InfusedVoid, PollutionMaterials.FadingNexus, GTValues.ZPM);
        fusion(provider, "tritanium", PollutionMaterials.InfusedInstrument,
                GTMaterials.Titanium, GTMaterials.Tritanium, GTValues.ZPM);
        fusion(provider, "neutronium", GTMaterials.Naquadria,
                PollutionMaterials.InfusedEnergy, GTMaterials.Neutronium, GTValues.ZPM);

        // 上游: StarmetalAlloy -> 本移植版: NaquadahAlloy（StarmetalAlloy 未移植）
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
