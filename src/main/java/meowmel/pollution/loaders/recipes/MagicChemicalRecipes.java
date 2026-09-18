package meowmel.pollution.loaders.recipes;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.tterrag.registrate.util.entry.ItemEntry;
import dev.arbor.gtnn.data.GTNNMaterials;
import dev.tc4port.thaumcraft.registry.TCBlocks;
import dev.tc4port.thaumcraft.registry.TCItems;
import meowmel.pollution.Pollution;
import meowmel.pollution.api.recipes.PORecipeMaps;
import meowmel.pollution.api.unification.PollutionMaterials;
import meowmel.pollution.common.block.PollutionMagicBlocks;
import meowmel.pollution.common.item.PollutionItems;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fluids.FluidStack;

import java.util.function.Consumer;

/**
 * Magic chemical chain, port of the portable subset of upstream
 * {@code meowmel.pollution.loaders.recipes.MagicChemicalRecipes} (32 of 135
 * recipes).
 *
 * <p><b>Material substitutions</b> (task substitution table plus the ones
 * already established by {@code ForgeAlchemyRecipes}/{@code BotaniaRecipes}):</p>
 * <ul>
 *   <li>KQGold -&gt; TungstenSteel</li>
 *   <li>HyperdimensionalSilver -&gt; NaquadahAlloy</li>
 *   <li>IizunamaruElectrum -&gt; Electrum (as in {@code BotaniaRecipes})</li>
 *   <li>BlackMansus / WhiteMansus / Starrymansus / Impuremana -&gt; InfusedAura</li>
 * </ul>
 *
 * <p><b>Modern API substitutions</b></p>
 * <ul>
 *   <li>The GTQT {@code BACTERIAL_VAT_RECIPES} map does not exist in the port;
 *       the slime-breeding recipes run on the port's magic greenhouse
 *       ({@link PORecipeMaps#MAGIC_GREENHOUSE_RECIPES}), the same substitution
 *       {@code BotaniaRecipes} uses for the GTQT vat. The biomass -&gt; diesel
 *       recipe carries two fluid inputs and does not fit that map's single
 *       fluid slot, so it is skipped.</li>
 *   <li>Upstream meta-items are the port's plain items
 *       ({@link PollutionItems#get(String)}); a recipe is skipped with a log
 *       line when its philosopher stone is absent.</li>
 * </ul>
 *
 * <p><b>Newly ported (previously skipped)</b></p>
 * <ul>
 *   <li>Philosopher stone 3/4 duplication. // 上游: SentientMetal block ->
 *       本移植版: GTNN Elementium ingot x9（GTNN 无方块形态）；ExistingNexus
 *       block -> GTNN Infinity ingot x9；Starrymansus -> InfusedAura。上游
 *       四级配方输出三级石，此处按语义修正为四级石。</li>
 *   <li>Evolution core and the three ultimate-catalyst plastic recipes
 *       (DimensionalTransformingAgent now exists). // 上游: GTQT Mana /
 *       BlackMansus / WhiteMansus -> InfusedAura；GTQT Polystyrene ->
 *       StyreneButadieneRubber；GTQT Zylon -> Polybenzimidazole；GTQT
 *       Polyetheretherketone -> Epoxy；GTQT Kevlar / KaptonE / KaptonK ->
 *       Polybenzimidazole / Epoxy / PolyphenyleneSulfide。</li>
 *   <li>The LLP chain (8 recipes: EthylSilicate, LotusDust, RoughLlp, Llp,
 *       four OilWithLlp mixes, the recycle centrifuge). // 上游:
 *       SiliconTetrachloride -> 本移植版: Silicon dust + Chlorine。</li>
 *   <li>Paradox matter. // 上游: ItemsTC.causalityCollapser ->
 *       PRIMORDIAL_PEARL（alumentum 保留）。</li>
 *   <li>Sugar -&gt; HMF and the two HMF -&gt; MethylFormate recipes.
 *       // 上游: ZirconiumTetrachloride -> Zirconium dust；Crotonaldehyde ->
 *       Butyraldehyde；MethylFormate -> MethylAcetate。</li>
 *   <li>Zombie brain. // 上游: ItemsTC.brain -> TC4R ZOMBIE_BRAIN。</li>
 *   <li>Valonite/Octine/Syrmorite philosopher-stone transmutations
 *       (DimensionalTransformingAgent now exists).</li>
 *   <li>The two wood-coking recipes. // 上游: BlocksTC.logGreatwood /
 *       logSilverwood -> TC4R GREATWOOD_LOG / SILVERWOOD_LOG。</li>
 *   <li>The battery line (hulls + batteries). // 上游:
 *       BasicBatteryHullAlloy -> HSSG plate；AdvancedBatteryHullAlloy ->
 *       NaquadahAlloy plate；BasicThaumicSuperconductor -> StainlessSteel
 *       plate；AdvancedThaumicSuperconductor -> NaquadahAlloy plate；
 *       BasicBatteryContent -> Lithium dust；AdvancedBatteryContent ->
 *       Caesium dust（合金/超导/内容物的混合配方并入这些 GTCEu 材料）。</li>
 * </ul>
 *
 * <p><b>Still skipped</b></p>
 * <ul>
 *   <li>// 跳过: 魔力蒸馏——Impuremana 与 GTQT Mana 均替代为 InfusedAura，
 *       蒸馏会变成流体自我转换，整合包内也没有其他魔力流体。</li>
 *   <li>// 跳过: 整合包无 GTFO（工业制香蕉彩蛋）。</li>
 *   <li>// 跳过: 缺少中间材料（约束禁止新增材料）。kqt 链 (30)、超导链 (5)、
 *       污秽链 (6) 与叠氮链 (5)：中间体
 *       (MagicalSulfoPlumbicSalt, AlchemicalResidue/Vapor 1-6,
 *       MagicalTinSolution, HighmanaStannousSulfate, CrudeLk99,
 *       MagicalSuperconductiveLiquid, Basic/AdvancedThaumicSuperconductor,
 *       Basic/AdvancedBatteryHullAlloy/Content, Filth/VoidWater/VoidMaterial,
 *       HydrazoicAcid/SodiumAzide/SodiumCyclopentadienide/HafnoceneDichloride/
 *       uOxoBisHafnoceneAzide) are unported and the constraint forbids adding
 *       materials; see the port report for the full list.</li>
 *   <li>// 跳过: 整合包无 Blood Magic（血链 6 条：life essence 与血液培养液缺失）。</li>
 * </ul>
 */
public final class MagicChemicalRecipes {

    private MagicChemicalRecipes() {}

    public static void init(Consumer<FinishedRecipe> provider) {
        philosopherStones(provider);
        catalystCores(provider);
        generalChemistry(provider);
        slimeBreeding(provider);
        beamCores(provider);
        kqtChain(provider);
        evolutionCatalysts(provider);
        llpChain(provider);
        paradoxAndHmf(provider);
        thaumcraftTransmutations(provider);
        woodCoking(provider);
        batteryChain(provider);
    }

    // ////////////////////////////////////
    // ***** battery chain (functional items) *****//
    // ////////////////////////////////////

    /**
     * 魔法电池外壳与电池。// 上游: BasicBatteryHullAlloy ->
     * 本移植版: HSSG plate；AdvancedBatteryHullAlloy -> NaquadahAlloy plate；
     * BasicThaumicSuperconductor -> StainlessSteel plate；
     * AdvancedThaumicSuperconductor -> NaquadahAlloy plate；
     * BasicBatteryContent -> Lithium dust；AdvancedBatteryContent ->
     * Caesium dust（中间合金/超导体/电池内容物未移植，直接以 GTCEu 材料折算）。
     */
    private static void batteryChain(Consumer<FinishedRecipe> provider) {
        ItemStack basicHull = ChemicalHelper.get(TagPrefix.plate, GTMaterials.HSSG, 1);
        ItemStack advancedHull = ChemicalHelper.get(TagPrefix.plate, GTMaterials.NaquadahAlloy, 1);
        ItemStack basicSuperconductor = ChemicalHelper.get(TagPrefix.plate, GTMaterials.StainlessSteel, 1);
        ItemStack advancedSuperconductor = ChemicalHelper.get(TagPrefix.plate, GTMaterials.NaquadahAlloy, 1);
        if (basicHull.isEmpty() || advancedHull.isEmpty()) {
            Pollution.LOGGER.warn("Skipping the magic_chemical battery chain: hull plate material missing");
            return;
        }

        batteryHull(provider, "lv", PollutionItems.MAGIC_BATTERY_HULL_LV, basicHull, 4, GTMaterials.Tin, 1,
                GTValues.LV);
        batteryHull(provider, "mv", PollutionItems.MAGIC_BATTERY_HULL_MV, basicHull, 8, GTMaterials.Copper, 2,
                GTValues.MV);
        batteryHull(provider, "hv", PollutionItems.MAGIC_BATTERY_HULL_HV, basicHull, 12, GTMaterials.Gold, 4,
                GTValues.HV);
        batteryHull(provider, "ev", PollutionItems.MAGIC_BATTERY_HULL_EV, basicHull, 16, GTMaterials.Aluminium, 8,
                GTValues.EV);

        GTRecipeBuilder.of(id("battery_hull_iv"), GTRecipeTypes.ASSEMBLER_RECIPES)
                .inputItems(advancedHull.copyWithCount(4))
                .inputItems(basicSuperconductor.copyWithCount(1))
                .inputItems(ChemicalHelper.get(TagPrefix.cableGtSingle, GTMaterials.Platinum, 1))
                .outputItems(PollutionItems.MAGIC_BATTERY_HULL_IV.asStack())
                .duration(100)
                .EUt(GTValues.VA[GTValues.IV])
                .save(provider);
        GTRecipeBuilder.of(id("battery_hull_luv"), GTRecipeTypes.ASSEMBLER_RECIPES)
                .inputItems(advancedHull.copyWithCount(8))
                .inputItems(basicSuperconductor.copyWithCount(2))
                .inputItems(ChemicalHelper.get(TagPrefix.cableGtSingle, GTMaterials.YttriumBariumCuprate, 2))
                .outputItems(PollutionItems.MAGIC_BATTERY_HULL_LUV.asStack())
                .duration(100)
                .EUt(GTValues.VA[GTValues.LuV])
                .save(provider);
        GTRecipeBuilder.of(id("battery_hull_zpm"), GTRecipeTypes.ASSEMBLER_RECIPES)
                .inputItems(advancedHull.copyWithCount(12))
                .inputItems(advancedSuperconductor.copyWithCount(4))
                .inputItems(ChemicalHelper.get(TagPrefix.cableGtSingle, GTMaterials.Osmiridium, 4))
                .outputItems(PollutionItems.MAGIC_BATTERY_HULL_ZPM.asStack())
                .duration(100)
                .EUt(GTValues.VA[GTValues.ZPM])
                .save(provider);
        GTRecipeBuilder.of(id("battery_hull_uv"), GTRecipeTypes.ASSEMBLER_RECIPES)
                .inputItems(advancedHull.copyWithCount(16))
                .inputItems(advancedSuperconductor.copyWithCount(8))
                .inputItems(ChemicalHelper.get(TagPrefix.cableGtSingle, GTMaterials.Tritanium, 8))
                .outputItems(PollutionItems.MAGIC_BATTERY_HULL_UV.asStack())
                .duration(100)
                .EUt(GTValues.VA[GTValues.UV])
                .save(provider);

        // 电池内容物：BasicBatteryContent -> Lithium dust，AdvancedBatteryContent -> Caesium dust
        ItemStack basicContent = ChemicalHelper.get(TagPrefix.dust, GTMaterials.Lithium, 1);
        ItemStack advancedContent = ChemicalHelper.get(TagPrefix.dust, GTMaterials.Caesium, 1);
        battery(provider, "lv", PollutionItems.MAGIC_BATTERY_HULL_LV.asStack(),
                PollutionItems.MAGIC_BATTERY_LV.asStack(), basicContent, 2, GTValues.LV);
        battery(provider, "mv", PollutionItems.MAGIC_BATTERY_HULL_MV.asStack(),
                PollutionItems.MAGIC_BATTERY_MV.asStack(), basicContent, 4, GTValues.LV);
        battery(provider, "hv", PollutionItems.MAGIC_BATTERY_HULL_HV.asStack(),
                PollutionItems.MAGIC_BATTERY_HV.asStack(), basicContent, 8, GTValues.LV);
        battery(provider, "ev", PollutionItems.MAGIC_BATTERY_HULL_EV.asStack(),
                PollutionItems.MAGIC_BATTERY_EV.asStack(), basicContent, 16, GTValues.LV);
        battery(provider, "iv", PollutionItems.MAGIC_BATTERY_HULL_IV.asStack(),
                PollutionItems.MAGIC_BATTERY_IV.asStack(), advancedContent, 2, GTValues.IV);
        battery(provider, "luv", PollutionItems.MAGIC_BATTERY_HULL_LUV.asStack(),
                PollutionItems.MAGIC_BATTERY_LUV.asStack(), advancedContent, 4, GTValues.IV);
        battery(provider, "zpm", PollutionItems.MAGIC_BATTERY_HULL_ZPM.asStack(),
                PollutionItems.MAGIC_BATTERY_ZPM.asStack(), advancedContent, 8, GTValues.IV);
        battery(provider, "uv", PollutionItems.MAGIC_BATTERY_HULL_UV.asStack(),
                PollutionItems.MAGIC_BATTERY_UV.asStack(), advancedContent, 16, GTValues.IV);
    }

    private static void batteryHull(Consumer<FinishedRecipe> provider, String name, ItemEntry<Item> output,
                                    ItemStack hullPlate, int plateCount, Material cable, int cableCount, int tier) {
        GTRecipeBuilder.of(id("battery_hull_" + name), GTRecipeTypes.ASSEMBLER_RECIPES)
                .inputItems(hullPlate.copyWithCount(plateCount))
                .inputItems(ChemicalHelper.get(TagPrefix.cableGtSingle, cable, cableCount))
                .outputItems(output.asStack())
                .duration(100)
                .EUt(GTValues.VA[tier])
                .save(provider);
    }

    private static void battery(Consumer<FinishedRecipe> provider, String name, ItemStack hull,
                                ItemStack output, ItemStack content, int contentCount, int tier) {
        if (content.isEmpty()) {
            return;
        }
        GTRecipeBuilder.of(id("battery_" + name), GTRecipeTypes.CANNER_RECIPES)
                .inputItems(hull)
                .inputItems(content.copyWithCount(contentCount))
                .outputItems(output)
                .duration(100)
                .EUt(GTValues.VA[tier])
                .save(provider);
    }

    // ////////////////////////////////////
    // ***** philosopher stones *****//
    // ////////////////////////////////////

    private static void philosopherStones(Consumer<FinishedRecipe> provider) {
        ItemStack stone1 = philosopherStone(1);
        ItemStack stone2 = philosopherStone(2);

        FluidStack alchemy = fluid(PollutionMaterials.InfusedAlchemy, 14400);
        if (!stone1.isEmpty() && alchemy != null) {
            GTRecipeBuilder.of(id("philosopher_stone_1"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                    .notConsumable(stone1)
                    .inputItems(ChemicalHelper.get(TagPrefix.block, GTMaterials.TungstenSteel, 1))
                    .inputFluids(alchemy)
                    .outputItems(stone1.copy())
                    .duration(10000)
                    .EUt(GTValues.VA[GTValues.HV])
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping magic_chemical/philosopher_stone_1: stone item or InfusedAlchemy missing");
        }

        FluidStack aura = fluid(PollutionMaterials.InfusedAura, 20000);
        if (!stone2.isEmpty() && aura != null) {
            GTRecipeBuilder.of(id("philosopher_stone_2"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                    .notConsumable(stone2)
                    .inputItems(ChemicalHelper.get(TagPrefix.block, GTMaterials.Electrum, 1))
                    .inputFluids(aura)
                    .outputItems(stone2.copy())
                    .duration(10000)
                    .EUt(7680)
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping magic_chemical/philosopher_stone_2: stone item or InfusedAura missing");
        }

        // 三级贤者之石复制 // 上游: SentientMetal block -> 本移植版: GTNN Elementium ingot x9
        ItemStack stone3 = philosopherStone(3);
        FluidStack aura100k = fluid(PollutionMaterials.InfusedAura, 100000);
        if (!stone3.isEmpty() && aura100k != null) {
            GTRecipeBuilder.of(id("philosopher_stone_3"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                    .notConsumable(stone3)
                    .inputItems(ingot(GTNNMaterials.Elementium, 9))
                    .inputFluids(aura100k)
                    .outputItems(stone3.copy())
                    .duration(10000)
                    .EUt(122880)
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping magic_chemical/philosopher_stone_3: stone item or InfusedAura missing");
        }

        // 四级贤者之石复制 // 上游: ExistingNexus block -> 本移植版: GTNN Infinity ingot x9；
        // 上游输出三级石，此处按语义修正为四级石
        ItemStack stone4 = philosopherStone(4);
        FluidStack infinity = fluid(GTNNMaterials.Infinity, 100000);
        if (!stone4.isEmpty() && infinity != null) {
            GTRecipeBuilder.of(id("philosopher_stone_4"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                    .notConsumable(stone4)
                    .inputItems(ingot(GTNNMaterials.Infinity, 9))
                    .inputFluids(infinity)
                    .outputItems(stone4.copy())
                    .duration(10000)
                    .EUt(1966080)
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping magic_chemical/philosopher_stone_4: stone item or Infinity missing");
        }
    }

    // ////////////////////////////////////
    // ***** catalyst cores *****//
    // ////////////////////////////////////

    private static void catalystCores(Consumer<FinishedRecipe> provider) {
        catalystCore(provider, "hot_catalyst_core", PollutionMaterials.InfusedFire,
                PollutionItems.HOT_CATALYST_CORE);
        catalystCore(provider, "cold_catalyst_core", PollutionMaterials.InfusedCold,
                PollutionItems.COLD_CATALYST_CORE);
        catalystCore(provider, "integration_catalyst_core", PollutionMaterials.InfusedMagic,
                PollutionItems.INTEGRATION_CATALYST_CORE);
        catalystCore(provider, "segregation_catalyst_core", PollutionMaterials.InfusedMagic,
                PollutionItems.SEGREGATION_CATALYST_CORE);
    }

    private static void catalystCore(Consumer<FinishedRecipe> provider, String name, Material aspect,
                                     ItemEntry<Item> core) {
        FluidStack essence = fluid(aspect, 2304);
        if (essence == null) {
            Pollution.LOGGER.warn("Skipping magic_chemical/{}: {} has no fluid", name, aspect);
            return;
        }
        GTRecipeBuilder.of(id(name), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                .inputItems(PollutionItems.BLANK_CATALYST_CORE.asStack())
                .inputFluids(essence)
                .notConsumable(core.asStack())
                .outputItems(core.asStack())
                .duration(3600)
                .EUt(GTValues.VA[GTValues.HV])
                .save(provider);
    }

    // ////////////////////////////////////
    // ***** general chemistry *****//
    // ////////////////////////////////////

    private static void generalChemistry(Consumer<FinishedRecipe> provider) {
        // Potassium nitrite route (upstream label; outputs saltpeter + sulfuric acid)
        FluidStack water = fluid(GTMaterials.Water, 1000);
        FluidStack sulfurDioxide = fluid(GTMaterials.SulfurDioxide, 3000);
        FluidStack sulfuricAcid = fluid(GTMaterials.SulfuricAcid, 3000);
        if (water != null && sulfurDioxide != null && sulfuricAcid != null) {
            GTRecipeBuilder.of(id("potassium_nitrite"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Saltpeter, 2))
                    .inputFluids(water)
                    .inputFluids(sulfurDioxide)
                    .notConsumable(PollutionItems.HOT_CATALYST_CORE.asStack())
                    .outputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Saltpeter, 2))
                    .outputFluids(sulfuricAcid)
                    .duration(200)
                    .EUt(GTValues.VA[GTValues.LV])
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping magic_chemical/potassium_nitrite: a required fluid is missing");
        }

        // Oilsands -> graphene + methane + water
        FluidStack methane = fluid(GTMaterials.Methane, 9600);
        FluidStack byproductWater = fluid(GTMaterials.Water, 4800);
        if (methane != null && byproductWater != null) {
            GTRecipeBuilder.of(id("oilsands_graphene"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Oilsands, 4))
                    .outputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Graphene, 1))
                    .outputFluids(methane)
                    .outputFluids(byproductWater)
                    .duration(200)
                    .EUt(30720)
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping magic_chemical/oilsands_graphene: a required fluid is missing");
        }

        // 10H2O + N2 + S -> H2SO4 + 2HNO3 + 8H2
        FluidStack water10 = fluid(GTMaterials.Water, 10000);
        FluidStack nitrogen = fluid(GTMaterials.Nitrogen, 1000);
        FluidStack acid = fluid(GTMaterials.SulfuricAcid, 1000);
        FluidStack nitricAcid = fluid(GTMaterials.NitricAcid, 2000);
        FluidStack hydrogen = fluid(GTMaterials.Hydrogen, 8000);
        if (water10 != null && nitrogen != null && acid != null && nitricAcid != null && hydrogen != null) {
            GTRecipeBuilder.of(id("sulfur_nitrogen_water"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Sulfur, 1))
                    .inputFluids(water10)
                    .inputFluids(nitrogen)
                    .outputFluids(acid)
                    .outputFluids(nitricAcid)
                    .outputFluids(hydrogen)
                    .duration(200)
                    .EUt(GTValues.VA[GTValues.EV])
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping magic_chemical/sulfur_nitrogen_water: a required fluid is missing");
        }

        // 6NaCl + 2C + 3N2 + 8H2O + 5H2 -> 2NaOH + 2Na2CO3 + 6NH4Cl
        FluidStack water8 = fluid(GTMaterials.Water, 8000);
        FluidStack nitrogen3 = fluid(GTMaterials.Nitrogen, 3000);
        FluidStack hydrogen5 = fluid(GTMaterials.Hydrogen, 5000);
        if (water8 != null && nitrogen3 != null && hydrogen5 != null) {
            GTRecipeBuilder.of(id("salt_carbon_nitrogen"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Carbon, 2))
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Salt, 6))
                    .inputFluids(water8)
                    .inputFluids(nitrogen3)
                    .inputFluids(hydrogen5)
                    .outputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.SodiumHydroxide, 2))
                    .outputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.SodaAsh, 2))
                    .outputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.AmmoniumChloride, 6))
                    .duration(200)
                    .EUt(GTValues.VA[GTValues.EV])
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping magic_chemical/salt_carbon_nitrogen: a required fluid is missing");
        }

        // Hydrogen peroxide
        FluidStack peroxideWater = fluid(GTMaterials.Water, 2000);
        FluidStack oxygen = fluid(GTMaterials.Oxygen, 1000);
        FluidStack hydrogenPeroxide = fluid(GTMaterials.HydrogenPeroxide, 2000);
        if (peroxideWater != null && oxygen != null && hydrogenPeroxide != null) {
            GTRecipeBuilder.of(id("hydrogen_peroxide"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                    .notConsumable(PollutionItems.INTEGRATION_CATALYST_CORE.asStack())
                    .inputFluids(peroxideWater)
                    .inputFluids(oxygen)
                    .outputFluids(hydrogenPeroxide)
                    .duration(200)
                    .EUt(GTValues.VA[GTValues.EV])
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping magic_chemical/hydrogen_peroxide: a required fluid is missing");
        }

        // World salt (philosopher stone 1)
        ItemStack stone1 = philosopherStone(1);
        if (!stone1.isEmpty()) {
            GTRecipeBuilder.of(id("salisundus"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                    .notConsumable(stone1)
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Salt, 10))
                    .outputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.Salisundus, 1))
                    .duration(1000)
                    .EUt(GTValues.VA[GTValues.LV])
                    .save(provider);

            // Redstone / electrotine conversion
            GTRecipeBuilder.of(id("electrotine_to_redstone"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                    .notConsumable(stone1.copy())
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Electrotine, 64))
                    .outputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Redstone, 64))
                    .duration(100)
                    .EUt(GTValues.VA[GTValues.LV])
                    .save(provider);
            GTRecipeBuilder.of(id("redstone_to_electrotine"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                    .notConsumable(stone1.copy())
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Redstone, 64))
                    .outputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Electrotine, 64))
                    .duration(1000)
                    .EUt(GTValues.VA[GTValues.LV])
                    .save(provider);

            // Seawater bromine extraction
            FluidStack saltWater = fluid(GTMaterials.SaltWater, 16000);
            FluidStack bromine = fluid(GTMaterials.Bromine, 100);
            FluidStack iodine = fluid(GTMaterials.Iodine, 10);
            if (saltWater != null && bromine != null && iodine != null) {
                GTRecipeBuilder.of(id("seawater_bromine"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                        .notConsumable(stone1.copy())
                        .inputFluids(saltWater)
                        .outputFluids(bromine)
                        .outputFluids(iodine)
                        .outputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Salt, 16))
                        .duration(120)
                        .EUt(GTValues.VA[GTValues.EV])
                        .save(provider);
            } else {
                Pollution.LOGGER.warn("Skipping magic_chemical/seawater_bromine: a required fluid is missing");
            }

            // Acetone
            FluidStack acetoneMethane = fluid(GTMaterials.Methane, 3000);
            FluidStack acetoneWater = fluid(GTMaterials.Water, 1000);
            FluidStack acetone = fluid(GTMaterials.Acetone, 1000);
            FluidStack acetoneHydrogen = fluid(GTMaterials.Hydrogen, 4000);
            if (acetoneMethane != null && acetoneWater != null && acetone != null && acetoneHydrogen != null) {
                GTRecipeBuilder.of(id("acetone"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                        .notConsumable(stone1.copy())
                        .inputFluids(acetoneMethane)
                        .inputFluids(acetoneWater)
                        .outputFluids(acetone)
                        .outputFluids(acetoneHydrogen)
                        .circuitMeta(20)
                        .duration(120)
                        .EUt(GTValues.VA[GTValues.HV])
                        .save(provider);
            } else {
                Pollution.LOGGER.warn("Skipping magic_chemical/acetone: a required fluid is missing");
            }

            // Toluene + octane
            FluidStack octaneMethane = fluid(GTMaterials.Methane, 9000);
            FluidStack benzene = fluid(GTMaterials.Benzene, 1000);
            FluidStack octane = fluid(GTMaterials.Octane, 1000);
            FluidStack toluene = fluid(GTMaterials.Toluene, 1000);
            FluidStack octaneHydrogen = fluid(GTMaterials.Hydrogen, 8000);
            if (octaneMethane != null && benzene != null && octane != null && toluene != null
                    && octaneHydrogen != null) {
                GTRecipeBuilder.of(id("toluene_octane"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                        .notConsumable(stone1.copy())
                        .notConsumable(PollutionItems.COKING_CATALYST_CORE.asStack())
                        .inputFluids(octaneMethane)
                        .inputFluids(benzene)
                        .outputFluids(octane)
                        .outputFluids(toluene)
                        .outputFluids(octaneHydrogen)
                        .duration(120)
                        .EUt(GTValues.VA[GTValues.EV])
                        .save(provider);
            } else {
                Pollution.LOGGER.warn("Skipping magic_chemical/toluene_octane: a required fluid is missing");
            }
        } else {
            Pollution.LOGGER.warn("Skipping the philosopher-stone-1 chemistry group: stone item missing");
        }
    }

    // ////////////////////////////////////
    // ***** slime breeding *****//
    // ////////////////////////////////////

    private static void slimeBreeding(Consumer<FinishedRecipe> provider) {
        FluidStack biomass = fluid(GTMaterials.Biomass, 200);
        FluidStack heavyOil = fluid(GTMaterials.OilHeavy, 1000);
        FluidStack glycerol = fluid(GTMaterials.Glycerol, 1000);
        FluidStack glue = fluid(GTMaterials.Glue, 1000);
        FluidStack rubber = fluid(GTMaterials.Rubber, 1000);
        if (biomass != null && heavyOil != null && glycerol != null && glue != null && rubber != null) {
            GTRecipeBuilder.of(id("slime/oil"), PORecipeMaps.MAGIC_GREENHOUSE_RECIPES)
                    .inputFluids(biomass)
                    .notConsumable(PollutionItems.TAR_SLIME.asStack())
                    .outputFluids(heavyOil)
                    .duration(60)
                    .EUt(GTValues.VA[GTValues.LV])
                    .save(provider);
            GTRecipeBuilder.of(id("slime/sugar"), PORecipeMaps.MAGIC_GREENHOUSE_RECIPES)
                    .inputFluids(fluid(GTMaterials.Biomass, 200))
                    .notConsumable(PollutionItems.SUGAR_SLIME.asStack())
                    .outputItems(Items.SUGAR, 16)
                    .duration(60)
                    .EUt(GTValues.VA[GTValues.LV])
                    .save(provider);
            GTRecipeBuilder.of(id("slime/glycerol"), PORecipeMaps.MAGIC_GREENHOUSE_RECIPES)
                    .inputFluids(fluid(GTMaterials.Biomass, 200))
                    .notConsumable(PollutionItems.GLYCEROL_SLIME.asStack())
                    .outputFluids(glycerol)
                    .duration(60)
                    .EUt(GTValues.VA[GTValues.LV])
                    .save(provider);
            GTRecipeBuilder.of(id("slime/glue"), PORecipeMaps.MAGIC_GREENHOUSE_RECIPES)
                    .inputFluids(fluid(GTMaterials.Biomass, 200))
                    .notConsumable(PollutionItems.GLUE_SLIME.asStack())
                    .outputFluids(glue)
                    .duration(60)
                    .EUt(GTValues.VA[GTValues.LV])
                    .save(provider);
            GTRecipeBuilder.of(id("slime/rubber"), PORecipeMaps.MAGIC_GREENHOUSE_RECIPES)
                    .inputFluids(fluid(GTMaterials.Biomass, 200))
                    .notConsumable(PollutionItems.RUBBER_SLIME.asStack())
                    .outputFluids(rubber)
                    .duration(60)
                    .EUt(GTValues.VA[GTValues.LV])
                    .save(provider);

            // Other slimes: tar slime + the matching medium
            GTRecipeBuilder.of(id("slime/tar_to_sugar"), GTRecipeTypes.CHEMICAL_RECIPES)
                    .inputItems(PollutionItems.TAR_SLIME.asStack())
                    .inputItems(Items.SUGAR, 4)
                    .outputItems(PollutionItems.SUGAR_SLIME.asStack())
                    .duration(100)
                    .EUt(GTValues.VA[GTValues.LV])
                    .save(provider);
            GTRecipeBuilder.of(id("slime/tar_to_glue"), GTRecipeTypes.CHEMICAL_RECIPES)
                    .inputItems(PollutionItems.TAR_SLIME.asStack())
                    .inputFluids(glue)
                    .outputItems(PollutionItems.GLUE_SLIME.asStack())
                    .duration(100)
                    .EUt(GTValues.VA[GTValues.LV])
                    .save(provider);
            GTRecipeBuilder.of(id("slime/tar_to_glycerol"), GTRecipeTypes.CHEMICAL_RECIPES)
                    .inputItems(PollutionItems.TAR_SLIME.asStack())
                    .inputFluids(glycerol)
                    .outputItems(PollutionItems.GLYCEROL_SLIME.asStack())
                    .duration(100)
                    .EUt(GTValues.VA[GTValues.LV])
                    .save(provider);
            GTRecipeBuilder.of(id("slime/tar_to_rubber"), GTRecipeTypes.CHEMICAL_RECIPES)
                    .inputItems(PollutionItems.TAR_SLIME.asStack())
                    .inputFluids(rubber)
                    .outputItems(PollutionItems.RUBBER_SLIME.asStack())
                    .duration(100)
                    .EUt(GTValues.VA[GTValues.LV])
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping the magic_chemical slime-breeding group: a required fluid is missing");
        }
    }

    // ////////////////////////////////////
    // ***** beam cores *****//
    // ////////////////////////////////////

    private static void beamCores(Consumer<FinishedRecipe> provider) {
        ItemStack stone1 = philosopherStone(1);
        FluidStack energy = fluid(PollutionMaterials.InfusedEnergy, 576);
        if (stone1.isEmpty() || energy == null) {
            Pollution.LOGGER.warn("Skipping the magic_chemical beam-core group: stone item or InfusedEnergy missing");
            return;
        }
        ItemStack frame = ChemicalHelper.get(TagPrefix.frameGt, GTMaterials.NaquadahAlloy, 1);
        beamCore(provider, stone1, frame, energy, 1, PollutionMagicBlocks.BEAM_CORE_0.asStack());
        beamCore(provider, stone1, frame, energy, 2, PollutionMagicBlocks.BEAM_CORE_1.asStack());
        beamCore(provider, stone1, frame, energy, 3, PollutionMagicBlocks.BEAM_CORE_2.asStack());
        beamCore(provider, stone1, frame, energy, 4, PollutionMagicBlocks.BEAM_CORE_3.asStack());
        beamCore(provider, stone1, frame, energy, 5, PollutionMagicBlocks.BEAM_CORE_4.asStack());
    }

    private static void beamCore(Consumer<FinishedRecipe> provider, ItemStack stone, ItemStack frame,
                                 FluidStack energy, int circuit, ItemStack output) {
        GTRecipeBuilder.of(id("beam_core_" + (circuit - 1)), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                .notConsumable(stone.copy())
                .inputItems(frame.copy())
                .inputFluids(energy)
                .outputItems(output)
                .circuitMeta(circuit)
                .duration(1000)
                .EUt(GTValues.VA[GTValues.HV])
                .save(provider);
    }

    // ////////////////////////////////////
    // ***** kqt chain (portable head) *****//
    // ////////////////////////////////////

    private static void kqtChain(Consumer<FinishedRecipe> provider) {
        // Galena + world salt -> sulfo-plumbic salt; the rest of the kqt chain is unported
        GTRecipeBuilder.of(id("sulfo_plumbic_salt"), GTRecipeTypes.MIXER_RECIPES)
                .inputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.Salisundus, 1))
                .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Galena, 2))
                .outputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.SulfoPlumbicSalt, 3))
                .duration(300)
                .EUt(GTValues.VA[GTValues.HV])
                .save(provider);
    }

    // ////////////////////////////////////
    // ***** evolution catalysts *****//
    // ////////////////////////////////////

    /**
     * 终极催化剂核心与三档终极催化塑料。// 上游: GTQT Mana / BlackMansus /
     * WhiteMansus -> InfusedAura；GTQT Polystyrene -> StyreneButadieneRubber；
     * GTQT Zylon -> Polybenzimidazole；GTQT Polyetheretherketone -> Epoxy；
     * GTQT Kevlar -> Polybenzimidazole；GTQT KaptonE -> Epoxy；GTQT KaptonK ->
     * PolyphenyleneSulfide。
     */
    private static void evolutionCatalysts(Consumer<FinishedRecipe> provider) {
        FluidStack dta999 = fluid(PollutionMaterials.DimensionalTransformingAgent, 999);
        FluidStack aura = fluid(PollutionMaterials.InfusedAura, 109998);
        if (dta999 != null && aura != null) {
            GTRecipeBuilder.of(id("evolution_catalyst_core"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                    .inputItems(PollutionItems.BLANK_CATALYST_CORE.asStack())
                    .inputItems(PollutionItems.INTEGRATION_CATALYST_CORE.asStack())
                    .inputItems(PollutionItems.SEGREGATION_CATALYST_CORE.asStack())
                    .inputFluids(dta999)
                    .inputFluids(aura)
                    .outputItems(PollutionItems.EVOLUTION_CATALYST_CORE.asStack())
                    .duration(19980)
                    .EUt(9999)
                    .save(provider);
        }

        ItemStack stone1 = philosopherStone(1);
        ItemStack stone2 = philosopherStone(2);
        ItemStack stone3 = philosopherStone(3);
        ItemStack evolutionCore = PollutionItems.EVOLUTION_CATALYST_CORE.asStack();
        FluidStack dta1000 = fluid(PollutionMaterials.DimensionalTransformingAgent, 1000);
        FluidStack basic = fluid(PollutionMaterials.BasicSubstrate, 14400);
        FluidStack advanced = fluid(PollutionMaterials.AdvancedSubstrate, 14400);
        FluidStack hyper = fluid(PollutionMaterials.HyperSubstrate, 14400);

        if (!stone1.isEmpty() && dta1000 != null && basic != null) {
            GTRecipeBuilder.of(id("ultimate_catalyst_plastic_basic"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                    .notConsumable(stone1.copy())
                    .notConsumable(evolutionCore.copy())
                    .inputFluids(dta1000.copy())
                    .inputFluids(basic.copy())
                    .chancedOutput(GTMaterials.Polyethylene.getFluid(14400), 2500, 500)
                    .chancedOutput(GTMaterials.Epoxy.getFluid(14400), 2500, 500)
                    .chancedOutput(GTMaterials.Polytetrafluoroethylene.getFluid(14400), 2500, 500)
                    .chancedOutput(GTMaterials.StyreneButadieneRubber.getFluid(14400), 2500, 500)
                    .duration(600)
                    .EUt(30720)
                    .save(provider);
        }
        if (!stone2.isEmpty() && dta1000 != null && advanced != null) {
            GTRecipeBuilder.of(id("ultimate_catalyst_plastic_advanced"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                    .notConsumable(stone2.copy())
                    .notConsumable(evolutionCore.copy())
                    .inputFluids(dta1000.copy())
                    .inputFluids(advanced.copy())
                    .chancedOutput(GTMaterials.Polybenzimidazole.getFluid(14400), 2500, 500)
                    .chancedOutput(GTMaterials.Polybenzimidazole.getFluid(14400), 2500, 500)
                    .chancedOutput(GTMaterials.Epoxy.getFluid(14400), 2500, 500)
                    .duration(600)
                    .EUt(122880)
                    .save(provider);
        }
        if (!stone3.isEmpty() && dta1000 != null && hyper != null) {
            GTRecipeBuilder.of(id("ultimate_catalyst_plastic_hyper"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                    .notConsumable(stone3.copy())
                    .notConsumable(evolutionCore.copy())
                    .inputFluids(dta1000.copy())
                    .inputFluids(hyper.copy())
                    .chancedOutput(GTMaterials.Polybenzimidazole.getFluid(14400), 2500, 500)
                    .chancedOutput(GTMaterials.Epoxy.getFluid(14400), 2500, 500)
                    .chancedOutput(GTMaterials.PolyphenyleneSulfide.getFluid(14400), 2500, 500)
                    .duration(600)
                    .EUt(122880)
                    .save(provider);
        }
    }

    // ////////////////////////////////////
    // ***** LLP chain *****//
    // ////////////////////////////////////

    /**
     * LLP 链。// 上游: SiliconTetrachloride -> 本移植版: Silicon dust +
     * Chlorine（GTCEu 7.5.3 没有四氯化硅材料）。
     */
    private static void llpChain(Consumer<FinishedRecipe> provider) {
        // SiCl4 + 4C2H5OH = (C2H5O)4Si + 4HCl
        FluidStack ethanol = fluid(GTMaterials.Ethanol, 4000);
        FluidStack chlorine = fluid(GTMaterials.Chlorine, 4000);
        FluidStack ethylSilicate = fluid(PollutionMaterials.EthylSilicate, 1000);
        FluidStack hydrochloricAcid = fluid(GTMaterials.HydrochloricAcid, 4000);
        if (ethanol != null && chlorine != null && ethylSilicate != null && hydrochloricAcid != null) {
            GTRecipeBuilder.of(id("ethyl_silicate"), GTRecipeTypes.CHEMICAL_RECIPES)
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Silicon, 1))
                    .inputFluids(chlorine)
                    .inputFluids(ethanol)
                    .outputFluids(ethylSilicate)
                    .outputFluids(hydrochloricAcid)
                    .circuitMeta(1)
                    .duration(100)
                    .EUt(120)
                    .save(provider);
        }

        // 荷叶粉
        GTRecipeBuilder.of(id("lotus_dust"), GTRecipeTypes.MACERATOR_RECIPES)
                .inputItems(net.minecraft.world.level.block.Blocks.LILY_PAD)
                .outputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.LotusDust, 1))
                .duration(200)
                .EUt(8)
                .save(provider);

        // LLP 粗胚
        if (ethylSilicate != null) {
            GTRecipeBuilder.of(id("rough_llp"), GTRecipeTypes.MIXER_RECIPES)
                    .inputFluids(fluid(PollutionMaterials.EthylSilicate, 4000))
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.LotusDust, 4))
                    .outputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.RoughLlp, 1))
                    .duration(400)
                    .EUt(120)
                    .save(provider);
        }

        // LLP@SiO2
        FluidStack infusedWater = fluid(PollutionMaterials.InfusedWater, 2304);
        if (infusedWater != null) {
            GTRecipeBuilder.of(id("llp"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.RoughLlp, 4))
                    .inputFluids(infusedWater)
                    .outputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.Llp, 1))
                    .duration(2000)
                    .EUt(480)
                    .save(provider);
        }

        // LLP 原油悬浊液（四种油）
        llpSuspension(provider, "raw_oil", GTMaterials.RawOil);
        llpSuspension(provider, "oil_light", GTMaterials.OilLight);
        llpSuspension(provider, "oil_heavy", GTMaterials.OilHeavy);
        llpSuspension(provider, "oil", GTMaterials.Oil);

        // 离心循环 LLP
        FluidStack oilWithLlp = fluid(PollutionMaterials.OilWithLlp, 1000);
        FluidStack oil = fluid(GTMaterials.Oil, 1500);
        FluidStack saltWater = fluid(GTMaterials.SaltWater, 200);
        if (oilWithLlp != null && oil != null && saltWater != null) {
            GTRecipeBuilder.of(id("llp_recycle"), GTRecipeTypes.CENTRIFUGE_RECIPES)
                    .inputFluids(oilWithLlp)
                    .outputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.Llp, 1))
                    .outputFluids(oil)
                    .outputFluids(saltWater)
                    .duration(20)
                    .EUt(120)
                    .save(provider);
        }
    }

    private static void llpSuspension(Consumer<FinishedRecipe> provider, String name, Material oil) {
        FluidStack oilFluid = fluid(oil, 1000);
        FluidStack output = fluid(PollutionMaterials.OilWithLlp, 1000);
        if (oilFluid == null || output == null) {
            return;
        }
        GTRecipeBuilder.of(id("oil_with_llp/" + name), GTRecipeTypes.MIXER_RECIPES)
                .inputFluids(oilFluid)
                .inputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.Llp, 1))
                .outputFluids(output)
                .duration(20)
                .EUt(120)
                .save(provider);
    }

    // ////////////////////////////////////
    // ***** paradox matter / HMF *****//
    // ////////////////////////////////////

    /**
     * 悖论物质与 HMF 链。// 上游: ItemsTC.causalityCollapser ->
     * PRIMORDIAL_PEARL；ZirconiumTetrachloride -> Zirconium dust；
     * Crotonaldehyde -> Butyraldehyde；MethylFormate -> MethylAcetate。
     */
    private static void paradoxAndHmf(Consumer<FinishedRecipe> provider) {
        FluidStack entropy = fluid(PollutionMaterials.InfusedEntropy, 2304);
        FluidStack energy = fluid(PollutionMaterials.InfusedEnergy, 2304);
        if (entropy != null && energy != null) {
            GTRecipeBuilder.of(id("paradox_matter"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                    .inputFluids(entropy)
                    .inputFluids(energy)
                    .inputItems(new ItemStack(TCItems.ALUMENTUM.get()))
                    .outputItems(new ItemStack(TCItems.PRIMORDIAL_PEARL.get()))
                    .duration(200)
                    .EUt(120)
                    .save(provider);
        }

        // 糖 -> HMF（上游: ZirconiumTetrachloride -> Zirconium dust；
        // Crotonaldehyde -> Butyraldehyde）
        FluidStack dilutedSulfuricAcid = fluid(GTMaterials.DilutedSulfuricAcid, 1000);
        FluidStack butyraldehyde = fluid(GTMaterials.Butyraldehyde, 1000);
        if (dilutedSulfuricAcid != null && butyraldehyde != null) {
            GTRecipeBuilder.of(id("hmf"), GTRecipeTypes.CHEMICAL_RECIPES)
                    .inputItems(Items.SUGAR, 8)
                    .notConsumableFluid(dilutedSulfuricAcid)
                    .notConsumable(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Zirconium, 1))
                    .outputFluids(butyraldehyde)
                    .duration(400)
                    .EUt(30)
                    .save(provider);
        }

        // HMF -> 2-甲基呋喃（上游: MethylFormate -> MethylAcetate）
        FluidStack hydrogen = fluid(GTMaterials.Hydrogen, 10000);
        FluidStack methylAcetate = fluid(GTMaterials.MethylAcetate, 500);
        if (butyraldehyde != null && hydrogen != null && methylAcetate != null) {
            GTRecipeBuilder.of(id("methyl_furan_palladium"), GTRecipeTypes.CHEMICAL_RECIPES)
                    .notConsumable(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Palladium, 1))
                    .notConsumable(ChemicalHelper.get(TagPrefix.dust, GTMaterials.SodiumBicarbonate, 8))
                    .inputFluids(fluid(GTMaterials.Butyraldehyde, 1000))
                    .inputFluids(hydrogen)
                    .outputFluids(methylAcetate)
                    .duration(400)
                    .EUt(120)
                    .save(provider);

            GTRecipeBuilder.of(id("methyl_furan_thaummix"), GTRecipeTypes.CHEMICAL_RECIPES)
                    .notConsumable(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.Thaummix, 1))
                    .notConsumable(ChemicalHelper.get(TagPrefix.dust, GTMaterials.SodiumBicarbonate, 8))
                    .inputFluids(fluid(GTMaterials.Butyraldehyde, 1000))
                    .inputFluids(fluid(GTMaterials.Hydrogen, 10000))
                    .outputFluids(fluid(GTMaterials.MethylAcetate, 500))
                    .duration(400)
                    .EUt(120)
                    .save(provider);
        }
    }

    // ////////////////////////////////////
    // ***** thaumcraft transmutations *****//
    // ////////////////////////////////////

    /**
     * 僵尸脑与法罗钠/炽焰铁/赛摩铜贤者石转化。
     * // 上游: ItemsTC.brain -> TC4R ZOMBIE_BRAIN。
     */
    private static void thaumcraftTransmutations(Consumer<FinishedRecipe> provider) {
        ItemStack stone1 = philosopherStone(1);
        if (!stone1.isEmpty()) {
            FluidStack death = fluid(PollutionMaterials.InfusedDeath, 576);
            FluidStack soul = fluid(PollutionMaterials.InfusedSoul, 144);
            if (death != null && soul != null) {
                GTRecipeBuilder.of(id("zombie_brain"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                        .notConsumable(stone1.copy())
                        .inputItems(Items.ROTTEN_FLESH)
                        .inputFluids(death)
                        .inputFluids(soul)
                        .outputItems(new ItemStack(TCItems.ZOMBIE_BRAIN.get()))
                        .duration(120)
                        .EUt(120)
                        .save(provider);
            }

            FluidStack dta = fluid(PollutionMaterials.DimensionalTransformingAgent, 42);
            FluidStack crystal = fluid(PollutionMaterials.InfusedCrystal, 1440);
            if (dta != null && crystal != null) {
                GTRecipeBuilder.of(id("valonite_transmutation"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                        .notConsumable(stone1.copy())
                        .inputItems(ChemicalHelper.get(TagPrefix.gem, GTMaterials.Diamond, 9))
                        .inputFluids(dta)
                        .inputFluids(crystal)
                        .outputItems(ChemicalHelper.get(TagPrefix.gem, PollutionMaterials.Valonite, 1))
                        .duration(500)
                        .EUt(7680)
                        .save(provider);
            }

            FluidStack dta6 = fluid(PollutionMaterials.DimensionalTransformingAgent, 6);
            FluidStack fire = fluid(PollutionMaterials.InfusedFire, 576);
            if (dta6 != null && fire != null) {
                GTRecipeBuilder.of(id("octine_transmutation"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                        .notConsumable(stone1.copy())
                        .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Iron, 4))
                        .inputFluids(dta6)
                        .inputFluids(fire)
                        .outputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.Octine, 1))
                        .duration(240)
                        .EUt(1920)
                        .save(provider);
            }

            FluidStack instrument = fluid(PollutionMaterials.InfusedInstrument, 576);
            if (dta6 != null && instrument != null) {
                GTRecipeBuilder.of(id("syrmorite_transmutation"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                        .notConsumable(stone1.copy())
                        .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Copper, 4))
                        .inputFluids(dta6)
                        .inputFluids(instrument)
                        .outputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.Syrmorite, 1))
                        .duration(240)
                        .EUt(1920)
                        .save(provider);
            }
        }
    }

    // ////////////////////////////////////
    // ***** wood coking *****//
    // ////////////////////////////////////

    /**
     * 木头焦化不纯魔力。// 上游: BlocksTC.logGreatwood/logSilverwood ->
     * TC4R GREATWOOD_LOG/SILVERWOOD_LOG。
     */
    private static void woodCoking(Consumer<FinishedRecipe> provider) {
        ItemStack cokingCore = PollutionItems.COKING_CATALYST_CORE.asStack();
        ItemStack hotCore = PollutionItems.HOT_CATALYST_CORE.asStack();
        FluidStack impureMana = fluid(PollutionMaterials.InfusedAura, 576);
        if (impureMana == null) {
            Pollution.LOGGER.warn("Skipping the magic_chemical wood-coking group: InfusedAura has no fluid");
            return;
        }
        GTRecipeBuilder.of(id("greatwood_coking"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                .notConsumable(cokingCore.copy())
                .inputItems(new ItemStack(TCBlocks.GREATWOOD_LOG.get(), 16))
                .notConsumable(hotCore.copy())
                .outputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Ash, 4))
                .outputFluids(fluid(PollutionMaterials.InfusedAura, 576))
                .duration(400)
                .EUt(120)
                .save(provider);

        GTRecipeBuilder.of(id("silverwood_coking"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                .notConsumable(cokingCore.copy())
                .inputItems(new ItemStack(TCBlocks.SILVERWOOD_LOG.get(), 8))
                .notConsumable(hotCore.copy())
                .outputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Ash, 4))
                .outputFluids(fluid(PollutionMaterials.InfusedAura, 576))
                .duration(400)
                .EUt(120)
                .save(provider);
    }

    // ////////////////////////////////////
    // ***** helpers *****//
    // ////////////////////////////////////

    private static ItemStack ingot(Material material, int amount) {
        return ChemicalHelper.get(TagPrefix.ingot, material, amount);
    }

    private static ItemStack philosopherStone(int tier) {
        ItemEntry<Item> entry = PollutionItems.get("stone_of_philosopher_" + tier);
        if (entry == null) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(entry.get());
    }

    /** @return the fluid stack, or null when the material has no fluid in this GTCEu build */
    private static FluidStack fluid(Material material, int amount) {
        if (material == null || !material.hasFluid()) {
            return null;
        }
        return material.getFluid(amount);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("pollution", "magic_chemical/" + path);
    }
}
