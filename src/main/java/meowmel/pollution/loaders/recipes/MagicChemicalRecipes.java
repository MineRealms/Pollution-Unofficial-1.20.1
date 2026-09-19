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
 * {@code meowmel.pollution.loaders.recipes.MagicChemicalRecipes}.
 *
 * <p><b>Material substitutions</b> (task substitution table plus the ones
 * already established by {@code ForgeAlchemyRecipes}/{@code BotaniaRecipes}):</p>
 * <ul>
 *   <li>GTQT Mana / BlackMansus / WhiteMansus -&gt; InfusedAura</li>
 *   <li>GTQT Thaumium -&gt; StainlessSteel, Mansussteel -&gt; HSSG</li>
 *   <li>Manasteel -&gt; GTNN ManaSteel (ingot; GTNN carries ingot/fluid only)</li>
 *   <li>GTQT VoidMetal -&gt; TC4R void ingot</li>
 *   <li>GTCEu CarbonTetrachloride / Acetylene / SodiumNitrate / HafniumTetrachloride
 *       and GTQT ZirconiumTetrachloride (all absent from GTCEu 7.5.3) -&gt;
 *       Chloroform / Ethylene / GTNN SodiumNitrate / TitaniumTetrachloride /
 *       TitaniumTetrachloride; every substitution is annotated at its recipe.</li>
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
 *   <li>Upstream {@code dust EnderEye} is the modern gem form
 *       ({@code EnderEye} has no dust prefix in 7.5.3).</li>
 * </ul>
 *
 * <p><b>Newly ported (previously skipped)</b></p>
 * <ul>
 *   <li>Philosopher stone 2/3/4 duplication (IizunamaruElectrum /
 *       SentientMetal / ExistingNexus blocks now exist).</li>
 *   <li>Evolution core and the three ultimate-catalyst plastic recipes
 *       (DimensionalTransformingAgent now exists). // 上游: GTQT Mana /
 *       BlackMansus / WhiteMansus -> InfusedAura；GTQT Polystyrene ->
 *       StyreneButadieneRubber；GTQT Zylon -> Polybenzimidazole；GTQT
 *       Polyetheretherketone -> Epoxy；GTQT Kevlar / KaptonE / KaptonK ->
 *       Polybenzimidazole / Epoxy / PolyphenyleneSulfide。</li>
 *   <li>The LLP chain (8 recipes). // 上游: SiliconTetrachloride ->
 *       本移植版: Silicon dust + Chlorine。</li>
 *   <li>Paradox matter. // 上游: ItemsTC.causalityCollapser ->
 *       PRIMORDIAL_PEARL（alumentum 保留）。</li>
 *   <li>Sugar -&gt; HMF and the two HMF -&gt; MethylFormate recipes.
 *       // 上游: ZirconiumTetrachloride -> TitaniumTetrachloride（GTCEu 7.5.3
 *       的 Zirconium 无任何物品形态）；Crotonaldehyde -> Butyraldehyde；
 *       MethylFormate -> MethylAcetate。</li>
 *   <li>Zombie brain. // 上游: ItemsTC.brain -> TC4R ZOMBIE_BRAIN。</li>
 *   <li>Valonite/Octine/Syrmorite philosopher-stone transmutations.</li>
 *   <li>The two wood-coking recipes. // 上游: BlocksTC.logGreatwood /
 *       logSilverwood -> TC4R GREATWOOD_LOG / SILVERWOOD_LOG。</li>
 *   <li>The whole battery chain (20 recipes) on the real
 *       Basic/AdvancedBatteryHullAlloy and Basic/AdvancedBatteryContent
 *       (// 上游: Mansussteel -> HSSG；GTQT Thaumium -> StainlessSteel；
 *       上游进阶内容物配方误输出基础内容物，此处按语义修正为进阶内容物).</li>
 *   <li>The kqt chain (30 remaining recipes), the superconductor chain (5),
 *       the filth chain (6), the hachimi chain (5) and magic distillation
 *       (Impuremana -> InfusedAura + Water).</li>
 * </ul>
 *
 * <p><b>Still skipped</b></p>
 * <ul>
 *   <li>// 跳过: 整合包无 GTFO（工业制香蕉彩蛋）。</li>
 *   <li>// 跳过: 整合包无 Blood Magic（血链 6 条：life essence 与血液培养液缺失）。</li>
 * </ul>
 */
public final class MagicChemicalRecipes {

    private MagicChemicalRecipes() {}

    public static void init(Consumer<FinishedRecipe> provider) {
        philosopherStones(provider);
        catalystCores(provider);
        generalChemistry(provider);
        magicDistillation(provider);
        slimeBreeding(provider);
        beamCores(provider);
        kqtChain(provider);
        superconductorChain(provider);
        batteryChain(provider);
        filthChain(provider);
        hachimiChain(provider);
        evolutionCatalysts(provider);
        llpChain(provider);
        paradoxAndHmf(provider);
        thaumcraftTransmutations(provider);
        woodCoking(provider);
    }

    // ////////////////////////////////////
    // ***** battery chain (20 recipes) *****//
    // ////////////////////////////////////

    /**
     * 魔法电池外壳与电池（上游全 20 条）。真实材料：
     * Basic/AdvancedBatteryHullAlloy、Basic/AdvancedBatteryContent 与
     * Basic/AdvancedThaumicSuperconductor 均已移植。
     * // 上游: Mansussteel -> 本移植版: HSSG（基础外壳合金组分）；
     * GTQT Thaumium -> 本移植版: StainlessSteel（基础内容物组分）。
     * 上游进阶内容物配方误输出基础内容物，此处按语义修正为进阶内容物。
     */
    private static void batteryChain(Consumer<FinishedRecipe> provider) {
        // 外壳合金与内容物（上游 MIXER 四条）
        GTRecipeBuilder.of(id("basic_battery_hull_alloy"), GTRecipeTypes.MIXER_RECIPES)
                .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.HSSG, 4))
                // 上游: dust Ordolead -> 本移植版: ingot Ordolead（合金只有锭/流体形态）
                .inputItems(ChemicalHelper.get(TagPrefix.ingot, PollutionMaterials.Ordolead, 1))
                .outputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.BasicBatteryHullAlloy, 5))
                .circuitMeta(2)
                .duration(400)
                .EUt(120)
                .save(provider);

        GTRecipeBuilder.of(id("advanced_battery_hull_alloy"), GTRecipeTypes.MIXER_RECIPES)
                .inputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.HyperdimensionalSilver, 4))
                // 上游: dust Valonite -> 本移植版: gem Valonite（Valonite 只有宝石/流体形态）
                .inputItems(ChemicalHelper.get(TagPrefix.gem, PollutionMaterials.Valonite, 1))
                .outputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.AdvancedBatteryHullAlloy, 5))
                .circuitMeta(2)
                .duration(400)
                .EUt(7680)
                .save(provider);

        FluidStack energy1000 = fluid(PollutionMaterials.InfusedEnergy, 1000);
        FluidStack motion1000 = fluid(PollutionMaterials.InfusedMotion, 1000);
        if (energy1000 != null && motion1000 != null) {
            GTRecipeBuilder.of(id("basic_battery_content"), GTRecipeTypes.MIXER_RECIPES)
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Lithium, 6))
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.StainlessSteel, 1))
                    .inputFluids(energy1000)
                    .inputFluids(motion1000)
                    .outputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.BasicBatteryContent, 9))
                    .duration(800)
                    .EUt(120)
                    .save(provider);

            // 上游: 进阶内容物配方误输出 BasicBatteryContent，此处修正为 AdvancedBatteryContent
            GTRecipeBuilder.of(id("advanced_battery_content"), GTRecipeTypes.MIXER_RECIPES)
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.KQGold, 6))
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Caesium, 1))
                    .inputFluids(fluid(PollutionMaterials.InfusedEnergy, 1000))
                    .inputFluids(fluid(PollutionMaterials.InfusedMotion, 1000))
                    .outputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.AdvancedBatteryContent, 9))
                    .duration(800)
                    .EUt(7680)
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping the magic_chemical battery-content mixers: a required fluid is missing");
        }

        ItemStack basicHull = ChemicalHelper.get(TagPrefix.plate,
                PollutionMaterials.BasicBatteryHullAlloy, 1);
        ItemStack advancedHull = ChemicalHelper.get(TagPrefix.plate,
                PollutionMaterials.AdvancedBatteryHullAlloy, 1);
        ItemStack basicSuperconductor = ChemicalHelper.get(TagPrefix.plate,
                PollutionMaterials.BasicThaumicSuperconductor, 1);
        ItemStack advancedSuperconductor = ChemicalHelper.get(TagPrefix.plate,
                PollutionMaterials.AdvancedThaumicSuperconductor, 1);
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

        if (basicSuperconductor.isEmpty() || advancedSuperconductor.isEmpty()) {
            Pollution.LOGGER.warn("Skipping the magic_chemical IV..UV battery hulls: superconductor plate missing");
            return;
        }
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
                // 上游: cableGtSingle Osmiridium -> 本移植版: cableGtSingle Trinium
                // （GTCEu 7.5.3 的 Osmiridium 无线缆属性，ZPM 级线缆为 Trinium）
                .inputItems(ChemicalHelper.get(TagPrefix.cableGtSingle, GTMaterials.Trinium, 4))
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

        // 电池内容物
        ItemStack basicContent = ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.BasicBatteryContent, 1);
        ItemStack advancedContent = ChemicalHelper.get(TagPrefix.dust,
                PollutionMaterials.AdvancedBatteryContent, 1);
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
    // ***** filth chain (6) *****//
    // ////////////////////////////////////

    /**
     * 污秽链。// 上游: GTQT Mana -> 本移植版: InfusedAura；GTQT VoidMetal ->
     * 本移植版: TC4R void ingot；ItemsTC.voidSeed -> 本移植版: ELDRITCH_OBJECT；
     * ErichAura -> 本移植版: InfusedAura。
     */
    private static void filthChain(Consumer<FinishedRecipe> provider) {
        // 魔导催化 污秽
        FluidStack taint1000 = fluid(PollutionMaterials.InfusedTaint, 1000);
        if (taint1000 != null) {
            GTRecipeBuilder.of(id("filth"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Netherrack, 6))
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Endstone, 1))
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Stone, 1))
                    .inputFluids(taint1000)
                    .outputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.Filth, 9))
                    .duration(800)
                    .EUt(GTValues.VA[GTValues.IV])
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping magic_chemical/filth: InfusedTaint has no fluid");
        }

        // 污秽之水
        ItemStack stone2 = philosopherStone(2);
        FluidStack death1000 = fluid(PollutionMaterials.InfusedDeath, 1000);
        FluidStack dark1000 = fluid(PollutionMaterials.InfusedDark, 1000);
        FluidStack filthWater = fluid(PollutionMaterials.FilthWater, 11000);
        if (!stone2.isEmpty() && death1000 != null && dark1000 != null && filthWater != null) {
            GTRecipeBuilder.of(id("filth_water"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                    .notConsumable(stone2.copy())
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.Filth, 9))
                    .inputFluids(death1000)
                    .inputFluids(dark1000)
                    .outputFluids(filthWater)
                    .duration(800)
                    .EUt(GTValues.VA[GTValues.IV])
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping magic_chemical/filth_water: a required input is missing");
        }

        // 污秽之水蒸馏 // 上游: GTQT Mana -> InfusedAura
        FluidStack filthWater1000 = fluid(PollutionMaterials.FilthWater, 1000);
        FluidStack mana800 = fluid(PollutionMaterials.InfusedAura, 800);
        FluidStack taint100 = fluid(PollutionMaterials.InfusedTaint, 100);
        FluidStack voidWater100 = fluid(PollutionMaterials.VoidWater, 100);
        if (filthWater1000 != null && mana800 != null && taint100 != null && voidWater100 != null) {
            GTRecipeBuilder.of(id("filth_water_distillation"), GTRecipeTypes.DISTILLATION_RECIPES)
                    .inputFluids(filthWater1000)
                    .outputFluids(mana800)
                    .outputFluids(taint100)
                    .outputFluids(voidWater100)
                    .duration(180)
                    .EUt(GTValues.VA[GTValues.LuV])
                    .save(provider);
        } else {
            Pollution.LOGGER.warn(
                    "Skipping magic_chemical/filth_water_distillation: a required fluid is missing");
        }

        // 虚空之水蒸馏 // 上游: ErichAura -> InfusedAura
        FluidStack voidWater1000 = fluid(PollutionMaterials.VoidWater, 1000);
        FluidStack erichAura800 = fluid(PollutionMaterials.InfusedAura, 800);
        FluidStack taint100B = fluid(PollutionMaterials.InfusedTaint, 100);
        FluidStack water100 = fluid(GTMaterials.Water, 100);
        if (voidWater1000 != null && erichAura800 != null && taint100B != null && water100 != null) {
            GTRecipeBuilder.of(id("void_water_distillation"), GTRecipeTypes.DISTILLATION_RECIPES)
                    .inputFluids(voidWater1000)
                    .outputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.VoidMaterial, 1))
                    .outputFluids(erichAura800)
                    .outputFluids(taint100B)
                    .outputFluids(water100)
                    .duration(1980)
                    .EUt(GTValues.VA[GTValues.LuV])
                    .save(provider);
        } else {
            Pollution.LOGGER.warn(
                    "Skipping magic_chemical/void_water_distillation: a required fluid is missing");
        }

        // 虚空金属 // 上游: GTQT VoidMetal -> 本移植版: TC4R void ingot；
        // ItemsTC.voidSeed -> 本移植版: ELDRITCH_OBJECT
        FluidStack iron576 = fluid(GTMaterials.Iron, 576);
        FluidStack infusedVoid576 = fluid(PollutionMaterials.InfusedVoid, 576);
        ItemStack eldritchObject4 = SafeItems.byId("thaumcraft", "eldritch_object", 4);
        ItemStack voidIngot = SafeItems.byId("thaumcraft", "void_ingot", 4);
        if (!stone2.isEmpty() && iron576 != null && infusedVoid576 != null
                && !eldritchObject4.isEmpty() && !voidIngot.isEmpty()) {
            GTRecipeBuilder.of(id("void_metal"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                    .notConsumable(stone2.copy())
                    .inputFluids(iron576)
                    .inputFluids(infusedVoid576)
                    .inputItems(eldritchObject4)
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.VoidMaterial, 1))
                    .outputItems(voidIngot)
                    .duration(2400)
                    .EUt(GTValues.VA[GTValues.LuV])
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping magic_chemical/void_metal: a required input is missing");
        }

        // 虚空种子增殖 // 上游: ItemsTC.voidSeed -> 本移植版: ELDRITCH_OBJECT
        FluidStack infusedVoid2304 = fluid(PollutionMaterials.InfusedVoid, 2304);
        if (infusedVoid2304 != null && !eldritchObject4.isEmpty()) {
            GTRecipeBuilder.of(id("void_seed"), PORecipeMaps.MAGIC_GREENHOUSE_RECIPES)
                    .notConsumable(PollutionItems.EVOLUTION_CATALYST_CORE.asStack())
                    .inputFluids(infusedVoid2304)
                    .inputItems(net.minecraft.world.item.Items.WHEAT_SEEDS, 64)
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.VoidMaterial, 1))
                    .outputItems(eldritchObject4)
                    .duration(2400)
                    .EUt(GTValues.VA[GTValues.LuV])
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping magic_chemical/void_seed: a required input is missing");
        }
    }

    // ////////////////////////////////////
    // ***** hachimi chain (5) *****//
    // ////////////////////////////////////

    /**
     * 耄耋哈基米链。// 上游: GTQT SodiumNitrate -> 本移植版: GTNN SodiumNitrate。
     * GTQT Acetylene -> 本移植版: Ethylene；GTQT HafniumTetrachloride ->
     * 本移植版: TitaniumTetrachloride（同族四氯化物；GTCEu 7.5.3 。Hafnium
     * 无任何物品/流体形态）。
     */
    private static void hachimiChain(Consumer<FinishedRecipe> provider) {
        // 叠氮酸
        FluidStack ammonia1000 = fluid(GTMaterials.Ammonia, 1000);
        FluidStack hydrochloricAcid1000 = fluid(GTMaterials.HydrochloricAcid, 1000);
        FluidStack hydrazoicAcid = fluid(PollutionMaterials.HydrazoicAcid, 1000);
        FluidStack water2000 = fluid(GTMaterials.Water, 2000);
        if (ammonia1000 != null && hydrochloricAcid1000 != null && hydrazoicAcid != null
                && water2000 != null) {
            GTRecipeBuilder.of(id("hydrazoic_acid"), GTRecipeTypes.CHEMICAL_RECIPES)
                    .inputFluids(ammonia1000)
                    .inputFluids(hydrochloricAcid1000)
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, GTNNMaterials.SodiumNitrate, 1))
                    .outputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Salt, 1))
                    .outputFluids(hydrazoicAcid)
                    .outputFluids(water2000)
                    .duration(100)
                    .EUt(GTValues.VA[GTValues.EV])
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping magic_chemical/hydrazoic_acid: a required fluid is missing");
        }

        // 中和
        FluidStack hydrazoicAcid1000 = fluid(PollutionMaterials.HydrazoicAcid, 1000);
        FluidStack water1000 = fluid(GTMaterials.Water, 1000);
        if (hydrazoicAcid1000 != null && water1000 != null) {
            GTRecipeBuilder.of(id("sodium_azide"), GTRecipeTypes.CHEMICAL_RECIPES)
                    .inputFluids(hydrazoicAcid1000)
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.SodiumHydroxide, 1))
                    .outputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.SodiumAzide, 1))
                    .outputFluids(water1000)
                    .duration(40)
                    .EUt(GTValues.VA[GTValues.LV])
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping magic_chemical/sodium_azide: a required fluid is missing");
        }

        // 环戊二烯基钠合成 // 上游: GTQT Acetylene -> 本移植版: Ethylene
        FluidStack ethylene2000 = fluid(GTMaterials.Ethylene, 2000);
        FluidStack hydrogen1000 = fluid(GTMaterials.Hydrogen, 1000);
        if (ethylene2000 != null && hydrogen1000 != null) {
            GTRecipeBuilder.of(id("sodium_cyclopentadienide"), GTRecipeTypes.CHEMICAL_RECIPES)
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Sodium, 2))
                    .inputFluids(ethylene2000)
                    .outputItems(ChemicalHelper.get(TagPrefix.dust,
                            PollutionMaterials.SodiumCyclopentadienide, 2))
                    .outputFluids(hydrogen1000)
                    .duration(400)
                    .EUt(GTValues.VA[GTValues.EV])
                    .save(provider);
        } else {
            Pollution.LOGGER.warn(
                    "Skipping magic_chemical/sodium_cyclopentadienide: a required fluid is missing");
        }

        // 二氯二茂铪 // 上游: HafniumTetrachloride -> 本移植版: TitaniumTetrachloride
        FluidStack titaniumTetrachloride1000 = fluid(GTMaterials.TitaniumTetrachloride, 1000);
        if (titaniumTetrachloride1000 != null) {
            GTRecipeBuilder.of(id("hafnocene_dichloride"), GTRecipeTypes.CHEMICAL_RECIPES)
                    .inputItems(ChemicalHelper.get(TagPrefix.dust,
                            PollutionMaterials.SodiumCyclopentadienide, 2))
                    .inputFluids(titaniumTetrachloride1000)
                    .outputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.HafnoceneDichloride, 1))
                    .outputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Salt, 2))
                    .duration(400)
                    .EUt(GTValues.VA[GTValues.EV])
                    .save(provider);
        } else {
            Pollution.LOGGER.warn(
                    "Skipping magic_chemical/hafnocene_dichloride: TitaniumTetrachloride has no fluid");
        }

        // 茂叠铪基醚
        ItemStack stone3 = philosopherStone(3);
        FluidStack water1000B = fluid(GTMaterials.Water, 1000);
        FluidStack hydrochloricAcid2000 = fluid(GTMaterials.HydrochloricAcid, 2000);
        if (!stone3.isEmpty() && water1000B != null && hydrochloricAcid2000 != null) {
            GTRecipeBuilder.of(id("u_oxo_bis_hafnocene_azide"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                    .notConsumable(stone3.copy())
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.HafnoceneDichloride, 2))
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.SodiumAzide, 2))
                    .inputFluids(water1000B)
                    .outputItems(ChemicalHelper.get(TagPrefix.dust,
                            PollutionMaterials.uOxoBisHafnoceneAzide, 1))
                    .outputFluids(hydrochloricAcid2000)
                    .outputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Salt, 2))
                    .duration(400)
                    .EUt(GTValues.VA[GTValues.EV])
                    .save(provider);
        } else {
            Pollution.LOGGER.warn(
                    "Skipping magic_chemical/u_oxo_bis_hafnocene_azide: a required input is missing");
        }
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

        // 二级贤者之石复制（上游: block IizunamaruElectrum；BlackMansus/WhiteMansus -> InfusedAura）
        FluidStack aura = fluid(PollutionMaterials.InfusedAura, 20000);
        if (!stone2.isEmpty() && aura != null) {
            GTRecipeBuilder.of(id("philosopher_stone_2"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                    .notConsumable(stone2)
                    .inputItems(ChemicalHelper.get(TagPrefix.block, PollutionMaterials.IizunamaruElectrum, 1))
                    .inputFluids(aura)
                    .outputItems(stone2.copy())
                    .duration(10000)
                    .EUt(7680)
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping magic_chemical/philosopher_stone_2: stone item or InfusedAura missing");
        }

        // 三级贤者之石复制（上游: block SentientMetal；Starrymansus -> InfusedAura）
        ItemStack stone3 = philosopherStone(3);
        FluidStack aura100k = fluid(PollutionMaterials.InfusedAura, 100000);
        if (!stone3.isEmpty() && aura100k != null) {
            GTRecipeBuilder.of(id("philosopher_stone_3"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                    .notConsumable(stone3)
                    .inputItems(ChemicalHelper.get(TagPrefix.block, PollutionMaterials.SentientMetal, 1))
                    .inputFluids(aura100k)
                    .outputItems(stone3.copy())
                    .duration(10000)
                    .EUt(122880)
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping magic_chemical/philosopher_stone_3: stone item or InfusedAura missing");
        }

        // 四级贤者之石复制（上游: block ExistingNexus + GTNN Infinity 流体；
        // 上游输出三级石，此处按语义修正为四级石）
        ItemStack stone4 = philosopherStone(4);
        FluidStack infinity = fluid(GTNNMaterials.Infinity, 100000);
        if (!stone4.isEmpty() && infinity != null) {
            GTRecipeBuilder.of(id("philosopher_stone_4"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                    .notConsumable(stone4)
                    .inputItems(ChemicalHelper.get(TagPrefix.block, PollutionMaterials.ExistingNexus, 1))
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
    // ***** magic distillation *****//
    // ////////////////////////////////////

    /**
     * 魔力蒸馏。// 上游: GTQTMaterials.Mana -> 本移植版: InfusedAura。
     * Impuremana 现为真实材料，蒸馏不再退化为流体自我转换。
     */
    private static void magicDistillation(Consumer<FinishedRecipe> provider) {
        FluidStack impureMana = fluid(PollutionMaterials.Impuremana, 1000);
        FluidStack mana = fluid(PollutionMaterials.InfusedAura, 500);
        FluidStack water = fluid(GTMaterials.Water, 500);
        if (impureMana == null || mana == null || water == null) {
            Pollution.LOGGER.warn("Skipping magic_chemical/impuremana_distillation: a required fluid is missing");
            return;
        }
        GTRecipeBuilder.of(id("impuremana_distillation"), GTRecipeTypes.DISTILLATION_RECIPES)
                .inputFluids(impureMana)
                .outputFluids(mana)
                .outputFluids(water)
                .duration(400)
                .EUt(7680)
                .save(provider);
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
    // ***** kqt chain (31 recipes) *****//
    // ////////////////////////////////////

    /**
     * kqt 链。替换项：
     * // 上游: CarbonTetrachloride -> 本移植版: Chloroform（GTCEu 7.5.3 已移除四氯化碳）
     * // 上游: GTQT Mana / BlackMansus / WhiteMansus -> 本移植版: InfusedAura
     * // 上游: Manasteel -> 本移植版: GTNN ManaSteel（锭，GTNN 无粉尘形态）
     * // 上游: dust EnderEye -> 本移植版: gem EnderEye（7.5.3 无粉尘形态）
     * // 上游: ItemsTC.voidSeed -> 本移植版: TC4R ELDRITCH_OBJECT
     * KQGold 与 HyperdimensionalSilver 现为真实材料。
     */
    private static void kqtChain(Consumer<FinishedRecipe> provider) {
        // 方铅矿矿粉+世界盐 搅拌 硫铅盐
        GTRecipeBuilder.of(id("sulfo_plumbic_salt"), GTRecipeTypes.MIXER_RECIPES)
                .inputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.Salisundus, 1))
                .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Galena, 2))
                .outputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.SulfoPlumbicSalt, 3))
                .duration(300)
                .EUt(GTValues.VA[GTValues.HV])
                .save(provider);

        // 硫铅盐+不纯魔力 流体固化 蕴魔硫铅盐
        FluidStack impureMana48 = fluid(PollutionMaterials.Impuremana, 48);
        if (impureMana48 != null) {
            GTRecipeBuilder.of(id("magical_sulfo_plumbic_salt"), GTRecipeTypes.FLUID_SOLIDFICATION_RECIPES)
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.SulfoPlumbicSalt, 1))
                    .inputFluids(impureMana48)
                    .outputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.MagicalSulfoPlumbicSalt, 1))
                    .duration(100)
                    .EUt(120)
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping magic_chemical/magical_sulfo_plumbic_salt: Impuremana has no fluid");
        }

        // 蕴魔硫铅盐+液态序 工业高炉 一次炼金残渣+一次升华蒸汽
        FluidStack order432 = fluid(PollutionMaterials.InfusedOrder, 432);
        FluidStack vapor1 = fluid(PollutionMaterials.AlchemicalVapor1, 1000);
        if (order432 != null && vapor1 != null) {
            GTRecipeBuilder.of(id("alchemical_residue_1"), GTRecipeTypes.BLAST_RECIPES)
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.MagicalSulfoPlumbicSalt, 3))
                    .inputFluids(order432)
                    .outputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.AlchemicalResidue1, 3))
                    .outputFluids(vapor1)
                    .blastFurnaceTemp(1600)
                    .duration(1200)
                    .EUt(120)
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping magic_chemical/alchemical_residue_1: a required fluid is missing");
        }

        // 一次炼金残渣 离心 硫+铅+概率银
        GTRecipeBuilder.of(id("alchemical_residue_1_centrifuge"), GTRecipeTypes.CENTRIFUGE_RECIPES)
                .inputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.AlchemicalResidue1, 3))
                .outputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Sulfur, 1))
                .outputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Lead, 1))
                .chancedOutput(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Silver, 1), 7000, 200)
                .duration(100)
                .EUt(30)
                .save(provider);

        // 一次升华蒸汽+锡粉+四氯化碳 化反 神秘锡溶液
        // // 上游: CarbonTetrachloride -> 本移植版: Chloroform
        FluidStack chloroform3000 = fluid(GTMaterials.Chloroform, 3000);
        FluidStack tinSolution = fluid(PollutionMaterials.MagicalTinSolution, 3000);
        if (vapor1 != null && chloroform3000 != null && tinSolution != null) {
            GTRecipeBuilder.of(id("magical_tin_solution"), GTRecipeTypes.CHEMICAL_RECIPES)
                    .inputFluids(vapor1)
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Tin, 3))
                    .inputFluids(chloroform3000)
                    .outputFluids(tinSolution)
                    .duration(300)
                    .EUt(480)
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping magic_chemical/magical_tin_solution: a required fluid is missing");
        }

        // 神秘锡溶液+硫酸 化反 硫酸亚锡神秘溶液
        FluidStack sulfuricAcid1000 = fluid(GTMaterials.SulfuricAcid, 1000);
        FluidStack stannousSulfate = fluid(PollutionMaterials.MagicalStannousSulfateSolution, 1000);
        if (tinSolution != null && sulfuricAcid1000 != null && stannousSulfate != null) {
            GTRecipeBuilder.of(id("magical_stannous_sulfate_solution"), GTRecipeTypes.CHEMICAL_RECIPES)
                    .inputFluids(tinSolution)
                    .inputFluids(sulfuricAcid1000)
                    .outputFluids(stannousSulfate)
                    .duration(100)
                    .EUt(480)
                    .save(provider);
        } else {
            Pollution.LOGGER.warn(
                    "Skipping magic_chemical/magical_stannous_sulfate_solution: a required fluid is missing");
        }

        // 硫酸亚锡神秘溶液 高压釜 高魔素硫酸亚锡+四氯化碳
        // // 上游: CarbonTetrachloride -> 本移植版: Chloroform
        FluidStack chloroform1000 = fluid(GTMaterials.Chloroform, 1000);
        if (stannousSulfate != null && chloroform1000 != null) {
            GTRecipeBuilder.of(id("highmana_stannous_sulfate"), GTRecipeTypes.AUTOCLAVE_RECIPES)
                    .inputFluids(stannousSulfate)
                    .outputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.HighmanaStannousSulfate, 1))
                    .outputFluids(chloroform1000)
                    .duration(100)
                    .EUt(480)
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping magic_chemical/highmana_stannous_sulfate: a required fluid is missing");
        }

        // 高魔素硫酸亚锡+液态水 工业高炉 二次炼金残渣+二次升华蒸汽
        FluidStack infusedWater432 = fluid(PollutionMaterials.InfusedWater, 432);
        FluidStack vapor2 = fluid(PollutionMaterials.AlchemicalVapor2, 1000);
        if (infusedWater432 != null && vapor2 != null) {
            GTRecipeBuilder.of(id("alchemical_residue_2"), GTRecipeTypes.BLAST_RECIPES)
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.HighmanaStannousSulfate, 3))
                    .inputFluids(infusedWater432)
                    .outputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.AlchemicalResidue2, 3))
                    .outputFluids(vapor2)
                    .blastFurnaceTemp(1600)
                    .duration(1800)
                    .EUt(120)
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping magic_chemical/alchemical_residue_2: a required fluid is missing");
        }

        // 二次炼金残渣 离心 锡+概率硫+不纯魔力
        FluidStack impureMana18 = fluid(PollutionMaterials.Impuremana, 18);
        if (impureMana18 != null) {
            GTRecipeBuilder.of(id("alchemical_residue_2_centrifuge"), GTRecipeTypes.CENTRIFUGE_RECIPES)
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.AlchemicalResidue2, 1))
                    .outputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Tin, 1))
                    .chancedOutput(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Sulfur, 1), 2000, 200)
                    .outputFluids(impureMana18)
                    .duration(200)
                    .EUt(30)
                    .save(provider);
        } else {
            Pollution.LOGGER.warn(
                    "Skipping magic_chemical/alchemical_residue_2_centrifuge: Impuremana has no fluid");
        }

        // 二次升华蒸汽+水银+世界盐 搅拌 含杂汞盐溶液
        FluidStack mercury9000 = fluid(GTMaterials.Mercury, 9000);
        FluidStack impureMercuricSalt = fluid(PollutionMaterials.ImpureMercuricSaltSolution, 10000);
        if (vapor2 != null && mercury9000 != null && impureMercuricSalt != null) {
            GTRecipeBuilder.of(id("impure_mercuric_salt_solution"), GTRecipeTypes.MIXER_RECIPES)
                    .inputFluids(vapor2)
                    .inputFluids(mercury9000)
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.Salisundus, 10))
                    .outputFluids(impureMercuricSalt)
                    .duration(300)
                    .EUt(480)
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping magic_chemical/impure_mercuric_salt_solution: a required fluid is missing");
        }

        // 含杂汞盐溶液 蒸馏塔 神秘汞盐溶液+世界盐+水
        FluidStack impureMercuricSalt1000 = fluid(PollutionMaterials.ImpureMercuricSaltSolution, 1000);
        FluidStack mercuricSalt = fluid(PollutionMaterials.MercuricSaltSolution, 100);
        FluidStack water900 = fluid(GTMaterials.Water, 900);
        if (impureMercuricSalt1000 != null && mercuricSalt != null && water900 != null) {
            GTRecipeBuilder.of(id("mercuric_salt_solution"), GTRecipeTypes.DISTILLATION_RECIPES)
                    .inputFluids(impureMercuricSalt1000)
                    .outputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.Salisundus, 1))
                    .outputFluids(mercuricSalt)
                    .outputFluids(water900)
                    .duration(30)
                    .EUt(480)
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping magic_chemical/mercuric_salt_solution: a required fluid is missing");
        }

        // 神秘汞盐溶液+火粉 工业高炉 三次炼金残渣+三次升华蒸汽
        FluidStack mercuricSalt1000 = fluid(PollutionMaterials.MercuricSaltSolution, 1000);
        FluidStack vapor3 = fluid(PollutionMaterials.AlchemicalVapor3, 1000);
        if (mercuricSalt1000 != null && vapor3 != null) {
            GTRecipeBuilder.of(id("alchemical_residue_3"), GTRecipeTypes.BLAST_RECIPES)
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.InfusedFire, 3))
                    .inputFluids(mercuricSalt1000)
                    .outputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.AlchemicalResidue3, 10))
                    .outputFluids(vapor3)
                    .blastFurnaceTemp(2700)
                    .duration(600)
                    .EUt(480)
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping magic_chemical/alchemical_residue_3: a required fluid is missing");
        }

        // 三次炼金残渣 离心 汞+概率盐
        FluidStack mercury9000Out = fluid(GTMaterials.Mercury, 9000);
        if (mercury9000Out != null) {
            GTRecipeBuilder.of(id("alchemical_residue_3_centrifuge"), GTRecipeTypes.CENTRIFUGE_RECIPES)
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.AlchemicalResidue3, 10))
                    .chancedOutput(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Salt, 1), 1500, 200)
                    .outputFluids(mercury9000Out)
                    .duration(400)
                    .EUt(30)
                    .save(provider);
        } else {
            Pollution.LOGGER.warn(
                    "Skipping magic_chemical/alchemical_residue_3_centrifuge: Mercury has no fluid");
        }

        // 三次升华蒸汽+氯化铁+水 化反 魔力激活氯化铁溶液
        FluidStack iron3Chloride = fluid(GTMaterials.Iron3Chloride, 1000);
        FluidStack water1000 = fluid(GTMaterials.Water, 1000);
        FluidStack activatedIronChloride = fluid(PollutionMaterials.MagicActivatedIronChlorideSolution, 3000);
        if (vapor3 != null && iron3Chloride != null && water1000 != null && activatedIronChloride != null) {
            GTRecipeBuilder.of(id("magic_activated_iron_chloride_solution"), GTRecipeTypes.CHEMICAL_RECIPES)
                    .inputFluids(vapor3)
                    .inputFluids(iron3Chloride)
                    .inputFluids(water1000)
                    .outputFluids(activatedIronChloride)
                    .duration(600)
                    .EUt(480)
                    .save(provider);
        } else {
            Pollution.LOGGER.warn(
                    "Skipping magic_chemical/magic_activated_iron_chloride_solution: a required fluid is missing");
        }

        // 魔力激活氯化铁溶液+小撮炽焰铁粉+铁粉+甲醇 大化反 魔力激活氯化亚铁甲醇溶液
        FluidStack activatedIronChloride3000 = fluid(PollutionMaterials.MagicActivatedIronChlorideSolution, 3000);
        FluidStack methanol1000 = fluid(GTMaterials.Methanol, 1000);
        FluidStack activatedFerrousChloride = fluid(
                PollutionMaterials.MagicActivatedFerrousChlorideEthanolSolution, 6000);
        if (activatedIronChloride3000 != null && methanol1000 != null && activatedFerrousChloride != null) {
            GTRecipeBuilder.of(id("magic_activated_ferrous_chloride_ethanol_solution"),
                            GTRecipeTypes.LARGE_CHEMICAL_RECIPES)
                    .inputFluids(activatedIronChloride3000)
                    .inputItems(ChemicalHelper.get(TagPrefix.dustTiny, PollutionMaterials.Octine, 1))
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Iron, 3))
                    .inputFluids(methanol1000)
                    .outputFluids(activatedFerrousChloride)
                    .duration(600)
                    .EUt(480)
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping magic_chemical/magic_activated_ferrous_chloride_ethanol_solution: "
                    + "a required fluid is missing");
        }

        // 魔力激活氯化亚铁甲醇溶液+磁化钕杆 电解机 除杂激活氯化亚铁甲醇溶液+四次炼金残渣
        FluidStack activatedFerrousChloride1000 = fluid(
                PollutionMaterials.MagicActivatedFerrousChlorideEthanolSolution, 1000);
        FluidStack purifiedSolution = fluid(
                PollutionMaterials.PurifiedActivatedFerrousChlorideEthanolSolution, 1000);
        if (activatedFerrousChloride1000 != null && purifiedSolution != null) {
            GTRecipeBuilder.of(id("purified_activated_ferrous_chloride_ethanol_solution"),
                            GTRecipeTypes.ELECTROLYZER_RECIPES)
                    .inputFluids(activatedFerrousChloride1000)
                    .notConsumable(ChemicalHelper.get(TagPrefix.rod, GTMaterials.NeodymiumMagnetic, 1))
                    .outputFluids(purifiedSolution)
                    .outputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.AlchemicalResidue4, 1))
                    .duration(100)
                    .EUt(480)
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping magic_chemical/purified_activated_ferrous_chloride_ethanol_solution: "
                    + "a required fluid is missing");
        }

        // 四次炼金残渣 离心 概率火+概率魔力钢粉+概率世界盐
        // // 上游: Manasteel dust -> 本移植版: GTNN ManaSteel ingot（GTNN 无粉尘形态）
        GTRecipeBuilder.of(id("alchemical_residue_4_centrifuge"), GTRecipeTypes.CENTRIFUGE_RECIPES)
                .inputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.AlchemicalResidue4, 2))
                .chancedOutput(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.InfusedFire, 1), 1500, 100)
                .chancedOutput(ingot(GTNNMaterials.ManaSteel, 1), 2500, 100)
                .chancedOutput(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.Salisundus, 1), 500, 100)
                .duration(300)
                .EUt(30)
                .save(provider);

        // 除杂激活氯化亚铁甲醇溶液 电弧炉 除杂激活氯化亚铁
        FluidStack purifiedSolution1000 = fluid(
                PollutionMaterials.PurifiedActivatedFerrousChlorideEthanolSolution, 1000);
        if (purifiedSolution1000 != null) {
            GTRecipeBuilder.of(id("purified_activated_ferrous_chloride"), GTRecipeTypes.ARC_FURNACE_RECIPES)
                    .inputFluids(purifiedSolution1000)
                    .outputItems(ChemicalHelper.get(TagPrefix.dust,
                            PollutionMaterials.PurifiedActivatedFerrousChloride, 1))
                    .duration(100)
                    .EUt(480)
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping magic_chemical/purified_activated_ferrous_chloride: "
                    + "PurifiedActivatedFerrousChlorideEthanolSolution has no fluid");
        }

        // 除杂激活氯化亚铁+地粉 工业高炉 四次升华蒸汽+氯化亚铁
        FluidStack vapor4 = fluid(PollutionMaterials.AlchemicalVapor4, 1000);
        if (vapor4 != null) {
            GTRecipeBuilder.of(id("ferrous_chloride"), GTRecipeTypes.BLAST_RECIPES)
                    .inputItems(ChemicalHelper.get(TagPrefix.dust,
                            PollutionMaterials.PurifiedActivatedFerrousChloride, 6))
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.InfusedEarth, 3))
                    .outputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.FerrousChloride, 6))
                    .outputFluids(vapor4)
                    .blastFurnaceTemp(2700)
                    .duration(900)
                    .EUt(480)
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping magic_chemical/ferrous_chloride: AlchemicalVapor4 has no fluid");
        }

        // 四次升华蒸汽+小撮赛摩铜粉+蒸馏水 搅拌 赛摩铜掺杂魔水溶液
        FluidStack vapor4In = fluid(PollutionMaterials.AlchemicalVapor4, 1000);
        FluidStack distilledWater520 = fluid(GTMaterials.DistilledWater, 520);
        FluidStack dopedSolution = fluid(PollutionMaterials.SyrmoriteDopedMagicWaterSolution, 1520);
        if (vapor4In != null && distilledWater520 != null && dopedSolution != null) {
            GTRecipeBuilder.of(id("syrmorite_doped_magic_water_solution"), GTRecipeTypes.MIXER_RECIPES)
                    .inputFluids(vapor4In)
                    .inputItems(ChemicalHelper.get(TagPrefix.dustTiny, PollutionMaterials.Syrmorite, 1))
                    .inputFluids(distilledWater520)
                    .outputFluids(dopedSolution)
                    .duration(300)
                    .EUt(1920)
                    .save(provider);
        } else {
            Pollution.LOGGER.warn(
                    "Skipping magic_chemical/syrmorite_doped_magic_water_solution: a required fluid is missing");
        }

        // 赛摩铜掺杂魔水溶液+世界盐 大化反 铜粉+未成形胚胎魔水+水
        FluidStack dopedSolution15200 = fluid(PollutionMaterials.SyrmoriteDopedMagicWaterSolution, 15200);
        FluidStack unformedEmbryo = fluid(PollutionMaterials.UnformedEmbryoMagicWater, 1000);
        FluidStack water14200 = fluid(GTMaterials.Water, 14200);
        if (dopedSolution15200 != null && unformedEmbryo != null && water14200 != null) {
            GTRecipeBuilder.of(id("unformed_embryo_magic_water"), GTRecipeTypes.LARGE_CHEMICAL_RECIPES)
                    .inputFluids(dopedSolution15200)
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.Salisundus, 1))
                    .outputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Copper, 1))
                    .outputFluids(unformedEmbryo)
                    .outputFluids(water14200)
                    .duration(3000)
                    .EUt(1920)
                    .save(provider);
        } else {
            Pollution.LOGGER.warn(
                    "Skipping magic_chemical/unformed_embryo_magic_water: a required fluid is missing");
        }

        // 未成形胚胎魔水+悖论物质nc+黑土贤者之石nc 魔导催化 胚胎魔水
        // // 上游: ItemsTC.causalityCollapser -> 本移植版: PRIMORDIAL_PEARL
        FluidStack unformedEmbryo1000 = fluid(PollutionMaterials.UnformedEmbryoMagicWater, 1000);
        FluidStack embryoMagicWater = fluid(PollutionMaterials.EmbryoMagicWater, 1000);
        ItemStack stone1 = philosopherStone(1);
        ItemStack primordialPearl = SafeItems.byId("thaumcraft", "primordial_pearl", 1);
        if (unformedEmbryo1000 != null && embryoMagicWater != null && !stone1.isEmpty()
                && !primordialPearl.isEmpty()) {
            GTRecipeBuilder.of(id("embryo_magic_water"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                    .inputFluids(unformedEmbryo1000)
                    .notConsumable(primordialPearl)
                    .notConsumable(stone1.copy())
                    .outputFluids(embryoMagicWater)
                    .duration(120)
                    .EUt(1920)
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping magic_chemical/embryo_magic_water: a required input is missing");
        }

        // 胚胎魔水+风粉 工业高炉 五次升华蒸汽+五次炼金残渣
        FluidStack embryoMagicWater100 = fluid(PollutionMaterials.EmbryoMagicWater, 100);
        FluidStack vapor5 = fluid(PollutionMaterials.AlchemicalVapor5, 1000);
        if (embryoMagicWater100 != null && vapor5 != null) {
            GTRecipeBuilder.of(id("alchemical_residue_5"), GTRecipeTypes.BLAST_RECIPES)
                    .inputFluids(embryoMagicWater100)
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.InfusedAir, 3))
                    .outputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.AlchemicalResidue5, 1))
                    .outputFluids(vapor5)
                    .blastFurnaceTemp(3600)
                    .duration(600)
                    .EUt(1920)
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping magic_chemical/alchemical_residue_5: a required fluid is missing");
        }

        // 五次炼金残渣 离心 白漫宿+概率地
        // // 上游: WhiteMansus -> 本移植版: InfusedAura
        FluidStack aura10 = fluid(PollutionMaterials.InfusedAura, 10);
        if (aura10 != null) {
            GTRecipeBuilder.of(id("alchemical_residue_5_centrifuge"), GTRecipeTypes.CENTRIFUGE_RECIPES)
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.AlchemicalResidue5, 1))
                    .chancedOutput(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.InfusedEarth, 1), 1000, 100)
                    .outputFluids(aura10)
                    .duration(1600)
                    .EUt(30)
                    .save(provider);
        } else {
            Pollution.LOGGER.warn(
                    "Skipping magic_chemical/alchemical_residue_5_centrifuge: InfusedAura has no fluid");
        }

        // 五次升华蒸汽+银粉+蓝石粉 搅拌 不稳次元银
        FluidStack vapor5In = fluid(PollutionMaterials.AlchemicalVapor5, 1000);
        if (vapor5In != null) {
            GTRecipeBuilder.of(id("unstable_dimensional_silver"), GTRecipeTypes.MIXER_RECIPES)
                    .inputFluids(vapor5In)
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Silver, 1))
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Electrotine, 4))
                    .outputItems(ChemicalHelper.get(TagPrefix.dust,
                            PollutionMaterials.UnstableDimensionalSilver, 6))
                    .duration(300)
                    .EUt(1920)
                    .save(provider);
        } else {
            Pollution.LOGGER.warn(
                    "Skipping magic_chemical/unstable_dimensional_silver: AlchemicalVapor5 has no fluid");
        }

        // 不稳次元银+末影珍珠粉+氡 魔法炖屎 超次元含杂秘银流
        FluidStack radon1000 = fluid(GTMaterials.Radon, 1000);
        FluidStack impureHyperSilver = fluid(PollutionMaterials.ImpureHyperdimensionalSilver, 1440);
        if (radon1000 != null && impureHyperSilver != null) {
            GTRecipeBuilder.of(id("impure_hyperdimensional_silver"), PORecipeMaps.MAGIC_ALLOY_BLAST_RECIPES)
                    .inputItems(ChemicalHelper.get(TagPrefix.dust,
                            PollutionMaterials.UnstableDimensionalSilver, 6))
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.EnderPearl, 3))
                    .inputFluids(radon1000)
                    .outputFluids(impureHyperSilver)
                    .blastFurnaceTemp(3600)
                    .duration(1200)
                    .EUt(1920)
                    .save(provider);
        } else {
            Pollution.LOGGER.warn(
                    "Skipping magic_chemical/impure_hyperdimensional_silver: a required fluid is missing");
        }

        // 超次元含杂秘银流+世界盐 化学浸洗 超次元秘银（粉）+六次炼金残渣
        FluidStack impureHyperSilver1440 = fluid(PollutionMaterials.ImpureHyperdimensionalSilver, 1440);
        if (impureHyperSilver1440 != null) {
            GTRecipeBuilder.of(id("hyperdimensional_silver"), GTRecipeTypes.CHEMICAL_BATH_RECIPES)
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.Salisundus, 1))
                    .inputFluids(impureHyperSilver1440)
                    .outputItems(ChemicalHelper.get(TagPrefix.dust,
                            PollutionMaterials.HyperdimensionalSilver, 1))
                    .outputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.AlchemicalResidue6, 10))
                    .duration(300)
                    .EUt(1920)
                    .save(provider);
        } else {
            Pollution.LOGGER.warn(
                    "Skipping magic_chemical/hyperdimensional_silver: ImpureHyperdimensionalSilver has no fluid");
        }

        // 六次炼金残渣 离心 黑漫宿+红石+末影之眼粉+概率虚空种子
        // // 上游: BlackMansus -> 本移植版: InfusedAura；
        // // 上游: dust EnderEye -> 本移植版: gem EnderEye；
        // // 上游: ItemsTC.voidSeed -> 本移植版: TC4R ELDRITCH_OBJECT
        FluidStack aura10Out = fluid(PollutionMaterials.InfusedAura, 10);
        ItemStack eldritchObject = SafeItems.byId("thaumcraft", "eldritch_object", 1);
        if (aura10Out != null && !eldritchObject.isEmpty()) {
            GTRecipeBuilder.of(id("alchemical_residue_6_centrifuge"), GTRecipeTypes.CENTRIFUGE_RECIPES)
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.AlchemicalResidue6, 10))
                    .outputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Redstone, 4))
                    .outputItems(ChemicalHelper.get(TagPrefix.gem, GTMaterials.EnderEye, 3))
                    .chancedOutput(eldritchObject, 100, 10)
                    .outputFluids(aura10Out)
                    .duration(6400)
                    .EUt(30)
                    .save(provider);
        } else {
            Pollution.LOGGER.warn(
                    "Skipping magic_chemical/alchemical_residue_6_centrifuge: a required input is missing");
        }

        // 超次元秘银+液态熵 工业高炉 六次升华蒸汽
        FluidStack entropy432 = fluid(PollutionMaterials.InfusedEntropy, 432);
        FluidStack vapor6 = fluid(PollutionMaterials.AlchemicalVapor6, 890);
        if (entropy432 != null && vapor6 != null) {
            GTRecipeBuilder.of(id("alchemical_vapor_6"), GTRecipeTypes.BLAST_RECIPES)
                    .inputItems(ChemicalHelper.get(TagPrefix.dust,
                            PollutionMaterials.HyperdimensionalSilver, 1))
                    .inputFluids(entropy432)
                    .outputFluids(vapor6)
                    .blastFurnaceTemp(4500)
                    .duration(600)
                    .EUt(1920)
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping magic_chemical/alchemical_vapor_6: a required fluid is missing");
        }

        // 六次升华蒸汽+魔力+黑漫宿+白漫宿 大化反 次元改造剂
        // // 上游: GTQT Mana / BlackMansus / WhiteMansus -> 本移植版: InfusedAura
        // （三种上游流体合并为 110 mB InfusedAura）
        FluidStack vapor6In = fluid(PollutionMaterials.AlchemicalVapor6, 890);
        FluidStack aura110 = fluid(PollutionMaterials.InfusedAura, 110);
        FluidStack dta1000 = fluid(PollutionMaterials.DimensionalTransformingAgent, 1000);
        if (vapor6In != null && aura110 != null && dta1000 != null) {
            GTRecipeBuilder.of(id("dimensional_transforming_agent"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                    .inputFluids(vapor6In)
                    .inputFluids(aura110)
                    .outputFluids(dta1000)
                    .duration(100)
                    .EUt(7680)
                    .save(provider);
        } else {
            Pollution.LOGGER.warn(
                    "Skipping magic_chemical/dimensional_transforming_agent: a required fluid is missing");
        }

        // 次元改造剂+金粉 高炉 刻金
        FluidStack dta42 = fluid(PollutionMaterials.DimensionalTransformingAgent, 42);
        if (dta42 != null) {
            GTRecipeBuilder.of(id("kq_gold"), GTRecipeTypes.BLAST_RECIPES)
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Gold, 1))
                    .inputFluids(dta42)
                    .outputItems(ChemicalHelper.get(TagPrefix.ingotHot, PollutionMaterials.KQGold, 1))
                    .blastFurnaceTemp(5400)
                    .duration(2000)
                    .EUt(7680)
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping magic_chemical/kq_gold: DimensionalTransformingAgent has no fluid");
        }
    }

    // ////////////////////////////////////
    // ***** superconductor chain (5) *****//
    // ////////////////////////////////////

    /**
     * 超导链。// 上游: Mansussteel -> 本移植版: HSSG；GTQT Thaumium ->
     * 本移植版: StainlessSteel（基础超导体的两种合金粉）。CrudeLk99、
     * MagicalSuperconductiveLiquid 与两种神秘超导体现为真实材料。
     */
    private static void superconductorChain(Consumer<FinishedRecipe> provider) {
        // 烧lk99（基础）
        FluidStack oxygen1000 = fluid(GTMaterials.Oxygen, 1000);
        FluidStack crudeLk99 = fluid(PollutionMaterials.CrudeLk99, 2448);
        if (oxygen1000 != null && crudeLk99 != null) {
            GTRecipeBuilder.of(id("crude_lk_99"), PORecipeMaps.MAGIC_ALLOY_BLAST_RECIPES)
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Lead, 6))
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Copper, 4))
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Phosphate, 6))
                    .inputFluids(oxygen1000)
                    .outputFluids(crudeLk99)
                    .blastFurnaceTemp(2700)
                    .circuitMeta(4)
                    .duration(1700)
                    .EUt(480)
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping magic_chemical/crude_lk_99: a required fluid is missing");
        }

        // 烧lk99（氮气加速）
        FluidStack oxygen1000B = fluid(GTMaterials.Oxygen, 1000);
        FluidStack nitrogen17000 = fluid(GTMaterials.Nitrogen, 17000);
        FluidStack crudeLk99B = fluid(PollutionMaterials.CrudeLk99, 2448);
        if (oxygen1000B != null && nitrogen17000 != null && crudeLk99B != null) {
            GTRecipeBuilder.of(id("crude_lk_99_nitrogen"), PORecipeMaps.MAGIC_ALLOY_BLAST_RECIPES)
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Lead, 6))
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Copper, 4))
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Phosphate, 6))
                    .inputFluids(oxygen1000B)
                    .inputFluids(nitrogen17000)
                    .outputFluids(crudeLk99B)
                    .blastFurnaceTemp(2700)
                    .circuitMeta(14)
                    .duration(1120)
                    .EUt(480)
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping magic_chemical/crude_lk_99_nitrogen: a required fluid is missing");
        }

        // 做液体
        FluidStack infusedMetal = fluid(PollutionMaterials.InfusedMetal, 1296);
        FluidStack infusedEnergy = fluid(PollutionMaterials.InfusedEnergy, 1296);
        FluidStack superconductiveLiquid = fluid(PollutionMaterials.MagicalSuperconductiveLiquid, 2000);
        if (infusedMetal != null && infusedEnergy != null && superconductiveLiquid != null) {
            GTRecipeBuilder.of(id("magical_superconductive_liquid"),
                            PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                    .inputFluids(infusedMetal)
                    .inputFluids(infusedEnergy)
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.CrudeLk99, 4))
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.Syrmorite, 4))
                    .outputFluids(superconductiveLiquid)
                    .circuitMeta(1)
                    .duration(400)
                    .EUt(480)
                    .save(provider);
        } else {
            Pollution.LOGGER.warn(
                    "Skipping magic_chemical/magical_superconductive_liquid: a required fluid is missing");
        }

        // 初阶超导 // 上游: Mansussteel / GTQT Thaumium -> HSSG / StainlessSteel
        FluidStack basicSubstrate576 = fluid(PollutionMaterials.BasicSubstrate, 576);
        FluidStack superconductiveLiquid500 = fluid(PollutionMaterials.MagicalSuperconductiveLiquid, 500);
        FluidStack basicSuperconductor = fluid(PollutionMaterials.BasicThaumicSuperconductor, 2304);
        if (basicSubstrate576 != null && superconductiveLiquid500 != null && basicSuperconductor != null) {
            GTRecipeBuilder.of(id("basic_thaumic_superconductor"),
                            PORecipeMaps.MAGIC_ALLOY_BLAST_RECIPES)
                    .inputFluids(basicSubstrate576)
                    .inputFluids(superconductiveLiquid500)
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.HSSG, 4))
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.StainlessSteel, 4))
                    .outputFluids(basicSuperconductor)
                    .blastFurnaceTemp(2700)
                    .circuitMeta(4)
                    .duration(2000)
                    .EUt(480)
                    .save(provider);
        } else {
            Pollution.LOGGER.warn(
                    "Skipping magic_chemical/basic_thaumic_superconductor: a required fluid is missing");
        }

        // 高阶超导
        FluidStack advancedSubstrate576 = fluid(PollutionMaterials.AdvancedSubstrate, 576);
        FluidStack superconductiveLiquid500B = fluid(PollutionMaterials.MagicalSuperconductiveLiquid, 500);
        FluidStack advancedSuperconductor = fluid(PollutionMaterials.AdvancedThaumicSuperconductor, 2304);
        if (advancedSubstrate576 != null && superconductiveLiquid500B != null
                && advancedSuperconductor != null) {
            GTRecipeBuilder.of(id("advanced_thaumic_superconductor"),
                            PORecipeMaps.FORGE_ALCHEMY_RECIPES)
                    .inputFluids(advancedSubstrate576)
                    .inputFluids(superconductiveLiquid500B)
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.KQGold, 4))
                    .inputItems(ChemicalHelper.get(TagPrefix.dust,
                            PollutionMaterials.HyperdimensionalSilver, 4))
                    .outputFluids(advancedSuperconductor)
                    .blastFurnaceTemp(7200)
                    .circuitMeta(4)
                    .duration(2000)
                    .EUt(30720)
                    .save(provider);
        } else {
            Pollution.LOGGER.warn(
                    "Skipping magic_chemical/advanced_thaumic_superconductor: a required fluid is missing");
        }
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
                .inputItems(Items.LILY_PAD)
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
        ItemStack alumentum = SafeItems.byId("thaumcraft", "alumentum", 1);
        ItemStack primordialPearl = SafeItems.byId("thaumcraft", "primordial_pearl", 1);
        if (entropy != null && energy != null && !alumentum.isEmpty() && !primordialPearl.isEmpty()) {
            GTRecipeBuilder.of(id("paradox_matter"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                    .inputFluids(entropy)
                    .inputFluids(energy)
                    .inputItems(alumentum)
                    .outputItems(primordialPearl)
                    .duration(200)
                    .EUt(120)
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping magic_chemical/paradox_matter: a required input is missing");
        }

        // 糖 -> HMF（// 上游: ZirconiumTetrachloride -> 本移植版:
        // TitaniumTetrachloride；GTCEu 7.5.3 的 Zirconium 无任何物品形态；
        // Crotonaldehyde -> Butyraldehyde）
        FluidStack dilutedSulfuricAcid = fluid(GTMaterials.DilutedSulfuricAcid, 1000);
        FluidStack butyraldehyde = fluid(GTMaterials.Butyraldehyde, 1000);
        FluidStack titaniumTetrachloride = fluid(GTMaterials.TitaniumTetrachloride, 1);
        if (dilutedSulfuricAcid != null && butyraldehyde != null && titaniumTetrachloride != null) {
            GTRecipeBuilder.of(id("hmf"), GTRecipeTypes.CHEMICAL_RECIPES)
                    .inputItems(Items.SUGAR, 8)
                    .notConsumableFluid(dilutedSulfuricAcid)
                    .notConsumableFluid(titaniumTetrachloride)
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
            ItemStack zombieBrain = SafeItems.byId("thaumcraft", "zombie_brain", 1);
            if (death != null && soul != null && !zombieBrain.isEmpty()) {
                GTRecipeBuilder.of(id("zombie_brain"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                        .notConsumable(stone1.copy())
                        .inputItems(Items.ROTTEN_FLESH)
                        .inputFluids(death)
                        .inputFluids(soul)
                        .outputItems(zombieBrain)
                        .duration(120)
                        .EUt(120)
                        .save(provider);
            } else {
                Pollution.LOGGER.warn("Skipping magic_chemical/zombie_brain: a required input is missing");
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
        ItemStack greatwoodLog = SafeItems.byId("thaumcraft", "greatwood_log", 16);
        if (greatwoodLog.isEmpty()) {
            Pollution.LOGGER.warn("Skipping magic_chemical/greatwood_coking: thaumcraft:greatwood_log is missing");
        } else {
            GTRecipeBuilder.of(id("greatwood_coking"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                    .notConsumable(cokingCore.copy())
                    .inputItems(greatwoodLog)
                    .notConsumable(hotCore.copy())
                    .outputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Ash, 4))
                    .outputFluids(fluid(PollutionMaterials.InfusedAura, 576))
                    .duration(400)
                    .EUt(120)
                    .save(provider);
        }

        ItemStack silverwoodLog = SafeItems.byId("thaumcraft", "silverwood_log", 8);
        if (silverwoodLog.isEmpty()) {
            Pollution.LOGGER.warn("Skipping magic_chemical/silverwood_coking: thaumcraft:silverwood_log is missing");
        } else {
            GTRecipeBuilder.of(id("silverwood_coking"), PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES)
                    .notConsumable(cokingCore.copy())
                    .inputItems(silverwoodLog)
                    .notConsumable(hotCore.copy())
                    .outputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Ash, 4))
                    .outputFluids(fluid(PollutionMaterials.InfusedAura, 576))
                    .duration(400)
                    .EUt(120)
                    .save(provider);
        }
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
