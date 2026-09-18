package meowmel.pollution.loaders.recipes;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.tterrag.registrate.util.entry.ItemEntry;
import dev.arbor.gtnn.data.GTNNMaterials;
import dev.tc4port.thaumcraft.registry.TCItems;
import meowmel.pollution.Pollution;
import meowmel.pollution.api.recipes.PORecipeMaps;
import meowmel.pollution.api.unification.PollutionMaterials;
import meowmel.pollution.common.item.PollutionItems;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import vazkii.botania.common.item.BotaniaItems;

import java.util.function.Consumer;

/**
 * Forge alchemy recipes ({@code pollution:forge_alchemy} map).
 *
 * <p>Port of the portable subset of upstream
 * {@code meowmel.pollution.loaders.recipes.ForgeAlchemyRecipes} (1.12.2). The
 * upstream file uses Pollution's own recipe map and no Forbidden Magic class;
 * the addon's alchemy content is datapack JSON, so no addon API call is needed
 * here (see {@code docs/PHASE6C_API.md} §2.3).</p>
 *
 * <p>Ported (26 recipes): the nine basic metal triads, the three advanced
 * noble/rare-earth triads, the advanced-substrate
 * Syrmorite/Octine/Valonite transmutation, the two six-aspect-alloy
 * transmutations, the three philosopher-stone upgrades, the three catalyst
 * metal fluids (Manasteel/Thaumium/Mansussteel), the
 * DimensionalTransformingAgent production, the HyperdimensionalSilver / KQGold
 * / Terrasteel / ElvenElementium / IizunamaruElectrum / AethericDarkSteel
 * transmutations and the helium transmutation.</p>
 *
 * <p>Documented substitutions (task table plus the port's established
 * mappings):</p>
 * <ul>
 *   <li>// 上游: GTQT Mana -&gt; 本移植版: InfusedAura（同 {@code ThaumcraftRecipes}）</li>
 *   <li>// 上游: BlackMansus / WhiteMansus / Starrymansus -&gt; 本移植版: InfusedAura</li>
 *   <li>// 上游: Terrasteel -&gt; 本移植版: GTNN TerraSteel</li>
 *   <li>// 上游: ElvenElementium -&gt; 本移植版: GTNN Elementium</li>
 *   <li>// 上游: Manasteel -&gt; 本移植版: GTNN ManaSteel</li>
 *   <li>// 上游: GTQT Thaumium -&gt; 本移植版: StainlessSteel</li>
 *   <li>// 上游: Mansussteel -&gt; 本移植版: HSSG</li>
 *   <li>// 上游: BloodOfAvernus -&gt; 本移植版: TungstenSteel</li>
 *   <li>// 上游: GTQT VoidMetal -&gt; 本移植版: TC4R void ingot</li>
 *   <li>Upstream meta-item philosopher stones (damage 150/151/152/153) are the
 *       port's plain items {@code stone_of_philosopher_1..4}; recipes are
 *       skipped when the item is absent.</li>
 *   <li>GTNN ManaSteel/TerraSteel/Elementium only carry {@code ingot/fluid}, so
 *       the upstream dust inputs use ingots.</li>
 * </ul>
 *
 * <p>Real materials (no longer substituted): {@code SentientMetal},
 * {@code BindingMetal}, {@code ExistingNexus}, {@code FadingNexus},
 * {@code AethericDarkSteel}, {@code IizunamaruElectrum},
 * {@code HyperdimensionalSilver} and {@code KQGold} are ported and used
 * directly by the stone upgrades and the catalyst transmutations.</p>
 *
 * <p>Still skipped: the BloodOfAvernus transmutation needs Blood Magic life
 * essence, which is not in the pack.</p>
 */
public final class ForgeAlchemyRecipes {

    private static final int CHANCE = 3333;
    private static final int BOOST = 500;
    private static final int FLUID_AMOUNT = 2304;
    private static final int MANA_AMOUNT = 1000;

    private ForgeAlchemyRecipes() {}

    public static void init(Consumer<FinishedRecipe> provider) {
        basicMetalTransmutations(provider);
        advancedMetalTransmutations(provider);
        substrateTransmutations(provider);
        philosopherStoneUpgrades(provider);
        catalystTransmutations(provider);
    }

    /** Nine triads from BasicSubstrate, stone tier 1, HV. */
    private static void basicMetalTransmutations(Consumer<FinishedRecipe> provider) {
        ItemStack stone = philosopherStone(1);
        if (stone.isEmpty()) {
            return;
        }
        basicTriad(provider, stone, 1, 3600, 2500, 1920,
                GTMaterials.Lead, GTMaterials.Tin, GTMaterials.Iron);
        basicTriad(provider, stone, 2, 3600, 3000, 1920,
                GTMaterials.Zinc, GTMaterials.Nickel, GTMaterials.Cobalt);
        basicTriad(provider, stone, 3, 3600, 3500, 1920,
                GTMaterials.Copper, GTMaterials.Silver, GTMaterials.Gold);
        basicTriad(provider, stone, 4, 4500, 4000, 1920,
                GTMaterials.Manganese, GTMaterials.Molybdenum, GTMaterials.Neodymium);
        basicTriad(provider, stone, 5, 4500, 4500, 1920,
                GTMaterials.Gallium, GTMaterials.Vanadium, GTMaterials.Chromium);
        basicTriad(provider, stone, 6, 4500, 5000, 1920,
                GTMaterials.Antimony, GTMaterials.Beryllium, GTMaterials.Bismuth);
        basicTriad(provider, stone, 7, 5400, 9000, 7680,
                GTMaterials.Aluminium, GTMaterials.Titanium, GTMaterials.Tungsten);
        basicTriad(provider, stone, 8, 5400, 9500, 7680,
                GTMaterials.Niobium, GTMaterials.Tantalum, GTMaterials.Yttrium);
        basicTriad(provider, stone, 9, 5400, 10000, 7680,
                GTMaterials.Thorium, GTMaterials.Uranium238, GTMaterials.Plutonium241);
    }

    private static void basicTriad(Consumer<FinishedRecipe> provider, ItemStack stone, int circuit,
                                   int temperature, int duration, int eu, Material... outputs) {
        GTRecipeBuilder builder = GTRecipeBuilder
                .of(id("metal/" + circuit), PORecipeMaps.FORGE_ALCHEMY_RECIPES)
                .inputFluids(PollutionMaterials.BasicSubstrate.getFluid(144))
                .inputFluids(PollutionMaterials.InfusedAura.getFluid(MANA_AMOUNT))
                .notConsumable(stone)
                .circuitMeta(circuit)
                .blastFurnaceTemp(temperature)
                .duration(duration)
                .EUt(eu);
        for (Material output : outputs) {
            builder.chancedOutput(output.getFluid(FLUID_AMOUNT), CHANCE, BOOST);
        }
        builder.save(provider);
    }

    /** Three triads from AdvancedSubstrate, stone tier 2, EV+. */
    private static void advancedMetalTransmutations(Consumer<FinishedRecipe> provider) {
        ItemStack stone = philosopherStone(2);
        if (stone.isEmpty()) {
            return;
        }
        advancedTriad(provider, stone, 2, 6300, 14000,
                GTMaterials.Platinum, GTMaterials.Palladium, GTMaterials.Ruthenium);
        advancedTriad(provider, stone, 3, 6300, 14500,
                GTMaterials.Rhodium, GTMaterials.Iridium, GTMaterials.Osmium);
        advancedTriad(provider, stone, 4, 6300, 15000,
                GTMaterials.Cerium, GTMaterials.Europium, GTMaterials.Samarium);
    }

    private static void advancedTriad(Consumer<FinishedRecipe> provider, ItemStack stone, int circuit,
                                      int temperature, int duration, Material... outputs) {
        GTRecipeBuilder builder = GTRecipeBuilder
                .of(id("metal_advanced/" + circuit), PORecipeMaps.FORGE_ALCHEMY_RECIPES)
                .inputFluids(PollutionMaterials.AdvancedSubstrate.getFluid(144))
                .inputFluids(PollutionMaterials.InfusedAura.getFluid(4000))
                .notConsumable(stone)
                .circuitMeta(circuit)
                .blastFurnaceTemp(temperature)
                .duration(duration)
                .EUt(30720);
        for (Material output : outputs) {
            builder.chancedOutput(output.getFluid(FLUID_AMOUNT), CHANCE, BOOST);
        }
        builder.save(provider);
    }

    /** Advanced-substrate specials and the two six-aspect-alloy transmutations. */
    private static void substrateTransmutations(Consumer<FinishedRecipe> provider) {
        ItemStack basicStone = philosopherStone(1);
        if (!basicStone.isEmpty()) {
            // 赛摩铜 / 炽焰铁 / 法罗钠
            GTRecipeBuilder.of(id("substrate/twilight"), PORecipeMaps.FORGE_ALCHEMY_RECIPES)
                    .inputFluids(PollutionMaterials.AdvancedSubstrate.getFluid(144))
                    .inputFluids(PollutionMaterials.InfusedAura.getFluid(MANA_AMOUNT))
                    .notConsumable(basicStone)
                    .chancedOutput(PollutionMaterials.Syrmorite.getFluid(FLUID_AMOUNT), CHANCE, BOOST)
                    .chancedOutput(PollutionMaterials.Octine.getFluid(FLUID_AMOUNT), CHANCE, BOOST)
                    .chancedOutput(ChemicalHelper.get(TagPrefix.gem, PollutionMaterials.Valonite, 16),
                            CHANCE, BOOST)
                    .circuitMeta(1)
                    .blastFurnaceTemp(5400)
                    .duration(10000)
                    .EUt(7680)
                    .save(provider);

            // 律动钛 / 定坤铜 / 司辰铅
            GTRecipeBuilder.of(id("substrate/aer_terra_ordo"), PORecipeMaps.FORGE_ALCHEMY_RECIPES)
                    .inputFluids(PollutionMaterials.BasicSubstrate.getFluid(144))
                    .inputFluids(PollutionMaterials.InfusedAura.getFluid(MANA_AMOUNT))
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Titanium, 4))
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Copper, 4))
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Lead, 4))
                    .notConsumable(basicStone)
                    .chancedOutput(PollutionMaterials.Aertitanium.getFluid(FLUID_AMOUNT), CHANCE, BOOST)
                    .chancedOutput(PollutionMaterials.Terracopper.getFluid(FLUID_AMOUNT), CHANCE, BOOST)
                    .chancedOutput(PollutionMaterials.Ordolead.getFluid(FLUID_AMOUNT), CHANCE, BOOST)
                    .circuitMeta(20)
                    .blastFurnaceTemp(3600)
                    .duration(2000)
                    .EUt(1920)
                    .save(provider);

            // 残日钢 / 捩花银 / 无极铝
            GTRecipeBuilder.of(id("substrate/ignis_aqua_perditio"), PORecipeMaps.FORGE_ALCHEMY_RECIPES)
                    .inputFluids(PollutionMaterials.BasicSubstrate.getFluid(144))
                    .inputFluids(PollutionMaterials.InfusedAura.getFluid(MANA_AMOUNT))
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Steel, 4))
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Silver, 4))
                    .inputItems(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Aluminium, 4))
                    .notConsumable(basicStone)
                    .chancedOutput(PollutionMaterials.IgnisSteel.getFluid(FLUID_AMOUNT), CHANCE, BOOST)
                    .chancedOutput(PollutionMaterials.Aquasilver.getFluid(FLUID_AMOUNT), CHANCE, BOOST)
                    .chancedOutput(PollutionMaterials.Perditioaluminium.getFluid(FLUID_AMOUNT), CHANCE, BOOST)
                    .circuitMeta(20)
                    .blastFurnaceTemp(3600)
                    .duration(2000)
                    .EUt(1920)
                    .save(provider);
        }
    }

    // ////////////////////////////////////
    // ***** philosopher stone upgrades *****//
    // ////////////////////////////////////

    /** 二/三/四级贤者之石升级。 */
    private static void philosopherStoneUpgrades(Consumer<FinishedRecipe> provider) {
        ItemStack stone1 = philosopherStone(1);
        ItemStack stone2 = philosopherStone(2);
        ItemStack stone3 = philosopherStone(3);
        ItemStack stone4 = philosopherStone(4);

        // 二级贤者之石（Terrasteel -> GTNN TerraSteel；HyperdimensionalSilver/KQGold 为真实材料）
        if (!stone1.isEmpty() && !stone2.isEmpty() && aura(299997) != null) {
            GTRecipeBuilder.of(id("stone_upgrade_2"), PORecipeMaps.FORGE_ALCHEMY_RECIPES)
                    .inputFluids(aura(299997))
                    .inputFluids(PollutionMaterials.DimensionalTransformingAgent.getFluid(9999))
                    .inputItems(stone1)
                    .inputItems(ingot(GTNNMaterials.TerraSteel, 64))
                    .inputItems(dust(PollutionMaterials.HyperdimensionalSilver, 64))
                    .inputItems(dust(PollutionMaterials.KQGold, 64))
                    .chancedOutput(stone2.copyWithCount(1), 5000, 0)
                    .blastFurnaceTemp(5400)
                    .duration(19980)
                    .EUt(9999)
                    .save(provider);
        }

        // 三级贤者之石（SentientMetal/BindingMetal 为真实材料；BloodOfAvernus -> TungstenSteel）
        if (!stone2.isEmpty() && !stone3.isEmpty() && aura(399998) != null
                && hasFluid(PollutionMaterials.SentientMetal) && hasFluid(PollutionMaterials.BindingMetal)) {
            GTRecipeBuilder.of(id("stone_upgrade_3"), PORecipeMaps.FORGE_ALCHEMY_RECIPES)
                    .inputFluids(aura(399998))
                    .inputFluids(PollutionMaterials.SentientMetal.getFluid(99999))
                    .inputFluids(PollutionMaterials.BindingMetal.getFluid(99999))
                    .inputItems(stone2)
                    .inputItems(dust(PollutionMaterials.IizunamaruElectrum, 64))
                    .inputItems(dust(PollutionMaterials.AethericDarkSteel, 64))
                    .inputItems(dust(GTMaterials.TungstenSteel, 64))
                    .chancedOutput(stone3.copyWithCount(1), 2500, 0)
                    .blastFurnaceTemp(7200)
                    .duration(19980)
                    .EUt(99999)
                    .save(provider);
        }

        // 四级贤者之石（ExistingNexus/FadingNexus 为真实材料）
        if (!stone3.isEmpty() && !stone4.isEmpty() && aura(1999998) != null
                && hasFluid(PollutionMaterials.ExistingNexus)
                && hasFluid(PollutionMaterials.FadingNexus)) {
            GTRecipeBuilder.of(id("stone_upgrade_4"), PORecipeMaps.FORGE_ALCHEMY_RECIPES)
                    .inputFluids(aura(1999998))
                    .inputFluids(PollutionMaterials.ExistingNexus.getFluid(99999))
                    .inputFluids(PollutionMaterials.FadingNexus.getFluid(99999))
                    .inputItems(stone3)
                    .inputItems(dust(PollutionMaterials.SentientMetal, 64))
                    .inputItems(dust(PollutionMaterials.BindingMetal, 64))
                    .inputItems(dust(GTMaterials.Neutronium, 64))
                    .chancedOutput(stone4.copyWithCount(1), 1000, 0)
                    .blastFurnaceTemp(10800)
                    .duration(29997)
                    .EUt(999999)
                    .save(provider);
        }
    }

    // ////////////////////////////////////
    // ***** catalyst transmutations *****//
    // ////////////////////////////////////

    /** 基础/进阶触媒金属：魔力钢、神秘、漫宿钢、次元改造剂、超次元秘银、刻金、泰拉钢、精灵元素、氦、光风霁月琥珀金、太虚玄钢。 */
    private static void catalystTransmutations(Consumer<FinishedRecipe> provider) {
        ItemStack stone1 = philosopherStone(1);
        ItemStack stone2 = philosopherStone(2);

        if (!stone1.isEmpty()) {
            // 魔力钢（Manasteel -> GTNN ManaSteel）
            if (hasFluid(GTNNMaterials.ManaSteel)) {
                GTRecipeBuilder.of(id("catalyst/manasteel"), PORecipeMaps.FORGE_ALCHEMY_RECIPES)
                        .inputFluids(PollutionMaterials.BasicSubstrate.getFluid(144))
                        .inputFluids(aura(MANA_AMOUNT))
                        .inputItems(dust(GTMaterials.Iron, 4))
                        .notConsumable(stone1.copy())
                        .outputFluids(GTNNMaterials.ManaSteel.getFluid(FLUID_AMOUNT))
                        .circuitMeta(20)
                        .blastFurnaceTemp(3600)
                        .duration(2500)
                        .EUt(1920)
                        .save(provider);
            }
            // 神秘（GTQT Thaumium -> StainlessSteel）
            GTRecipeBuilder.of(id("catalyst/thaumium"), PORecipeMaps.FORGE_ALCHEMY_RECIPES)
                    .inputFluids(PollutionMaterials.BasicSubstrate.getFluid(144))
                    .inputFluids(aura(MANA_AMOUNT))
                    .inputItems(dust(GTMaterials.Steel, 4))
                    .notConsumable(stone1.copy())
                    .outputFluids(GTMaterials.StainlessSteel.getFluid(FLUID_AMOUNT))
                    .circuitMeta(21)
                    .blastFurnaceTemp(3600)
                    .duration(3000)
                    .EUt(1920)
                    .save(provider);
            // 漫宿钢（Mansussteel -> HSSG）
            GTRecipeBuilder.of(id("catalyst/mansussteel"), PORecipeMaps.FORGE_ALCHEMY_RECIPES)
                    .inputFluids(PollutionMaterials.BasicSubstrate.getFluid(144))
                    .inputFluids(aura(MANA_AMOUNT))
                    .inputItems(dust(GTMaterials.StainlessSteel, 4))
                    .notConsumable(stone1.copy())
                    .outputFluids(GTMaterials.HSSG.getFluid(FLUID_AMOUNT))
                    .circuitMeta(20)
                    .blastFurnaceTemp(3600)
                    .duration(5000)
                    .EUt(1920)
                    .save(provider);
        }

        if (!stone2.isEmpty()) {
            // 次元改造剂
            GTRecipeBuilder.of(id("catalyst/dimensional_transforming_agent"), PORecipeMaps.FORGE_ALCHEMY_RECIPES)
                    .inputFluids(GTMaterials.Water.getFluid(16000))
                    .inputFluids(aura(4000))
                    .inputItems(dust(PollutionMaterials.Salisundus, 4))
                    .notConsumable(stone2.copy())
                    .outputFluids(PollutionMaterials.DimensionalTransformingAgent.getFluid(1000))
                    .blastFurnaceTemp(4500)
                    .duration(2000)
                    .EUt(7680)
                    .save(provider);

            // 超次元秘银（HyperdimensionalSilver 为真实材料）
            GTRecipeBuilder.of(id("catalyst/hyperdimensional_silver"), PORecipeMaps.FORGE_ALCHEMY_RECIPES)
                    .inputFluids(PollutionMaterials.AdvancedSubstrate.getFluid(144))
                    .inputFluids(PollutionMaterials.DimensionalTransformingAgent.getFluid(42))
                    .inputFluids(aura(4000))
                    .inputItems(dust(GTMaterials.Silver, 4))
                    .notConsumable(stone2.copy())
                    .outputFluids(PollutionMaterials.HyperdimensionalSilver.getFluid(FLUID_AMOUNT))
                    .circuitMeta(20)
                    .blastFurnaceTemp(4500)
                    .duration(10000)
                    .EUt(7680)
                    .save(provider);

            // 刻金（KQGold 为真实材料）
            GTRecipeBuilder.of(id("catalyst/kq_gold"), PORecipeMaps.FORGE_ALCHEMY_RECIPES)
                    .inputFluids(PollutionMaterials.AdvancedSubstrate.getFluid(144))
                    .inputFluids(PollutionMaterials.DimensionalTransformingAgent.getFluid(42))
                    .inputFluids(aura(4000))
                    .inputItems(dust(GTMaterials.Gold, 4))
                    .notConsumable(stone2.copy())
                    .outputFluids(PollutionMaterials.KQGold.getFluid(FLUID_AMOUNT))
                    .circuitMeta(20)
                    .blastFurnaceTemp(4500)
                    .duration(10000)
                    .EUt(7680)
                    .save(provider);

            // 泰拉钢（Terrasteel -> GTNN TerraSteel）
            if (hasFluid(GTNNMaterials.TerraSteel)) {
                GTRecipeBuilder.of(id("catalyst/terrasteel"), PORecipeMaps.FORGE_ALCHEMY_RECIPES)
                        .inputFluids(PollutionMaterials.AdvancedSubstrate.getFluid(144))
                        .inputFluids(PollutionMaterials.DimensionalTransformingAgent.getFluid(42))
                        .inputFluids(aura(4000))
                        .inputItems(dust(GTMaterials.HSSG, 2))
                        .inputItems(ingot(GTNNMaterials.ManaSteel, 2))
                        .notConsumable(stone2.copy())
                        .outputFluids(GTNNMaterials.TerraSteel.getFluid(FLUID_AMOUNT))
                        .circuitMeta(20)
                        .blastFurnaceTemp(4500)
                        .duration(15000)
                        .EUt(7680)
                        .save(provider);
            }

            // 精灵元素（ElvenElementium -> GTNN Elementium）
            if (hasFluid(GTNNMaterials.Elementium)) {
                GTRecipeBuilder.of(id("catalyst/elementium"), PORecipeMaps.FORGE_ALCHEMY_RECIPES)
                        .inputFluids(PollutionMaterials.AdvancedSubstrate.getFluid(144))
                        .inputFluids(PollutionMaterials.DimensionalTransformingAgent.getFluid(42))
                        .inputFluids(aura(4000))
                        .inputItems(ingot(GTNNMaterials.TerraSteel, 2))
                        .inputItems(ingot(GTNNMaterials.ManaSteel, 2))
                        .inputItems(BotaniaItems.runeMana)
                        .notConsumable(stone2.copy())
                        .outputFluids(GTNNMaterials.Elementium.getFluid(FLUID_AMOUNT))
                        .circuitMeta(20)
                        .blastFurnaceTemp(4500)
                        .duration(15000)
                        .EUt(7680)
                        .save(provider);
            }

            // 氦气
            GTRecipeBuilder.of(id("catalyst/helium"), PORecipeMaps.FORGE_ALCHEMY_RECIPES)
                    .inputFluids(PollutionMaterials.DimensionalTransformingAgent.getFluid(42))
                    .inputFluids(GTMaterials.Hydrogen.getFluid(4000))
                    .notConsumable(stone2.copy())
                    .chancedOutput(GTMaterials.Helium.getFluid(1000), CHANCE, BOOST)
                    .blastFurnaceTemp(4500)
                    .duration(1000)
                    .EUt(7680)
                    .save(provider);

            // 光风霁月琥珀金（IizunamaruElectrum 为真实材料；Starrymansus/Mana -> InfusedAura）
            GTRecipeBuilder.of(id("catalyst/iizunamaru_electrum"), PORecipeMaps.FORGE_ALCHEMY_RECIPES)
                    .inputFluids(PollutionMaterials.AdvancedSubstrate.getFluid(1440))
                    .inputFluids(PollutionMaterials.DimensionalTransformingAgent.getFluid(420))
                    .inputFluids(aura(11000))
                    .inputItems(dust(PollutionMaterials.HyperdimensionalSilver, 8))
                    .inputItems(dust(PollutionMaterials.KQGold, 8))
                    .notConsumable(stone2.copy())
                    .outputFluids(PollutionMaterials.IizunamaruElectrum.getFluid(1152))
                    .circuitMeta(21)
                    .blastFurnaceTemp(7200)
                    .duration(12000)
                    .EUt(30720)
                    .save(provider);

            // 太虚玄钢（AethericDarkSteel 为真实材料；VoidMetal -> TC4R void ingot，
            // ElvenElementium -> GTNN Elementium）
            GTRecipeBuilder.of(id("catalyst/aetheric_dark_steel"), PORecipeMaps.FORGE_ALCHEMY_RECIPES)
                    .inputFluids(PollutionMaterials.AdvancedSubstrate.getFluid(1440))
                    .inputFluids(PollutionMaterials.DimensionalTransformingAgent.getFluid(420))
                    .inputFluids(aura(20000))
                    .inputItems(new ItemStack(TCItems.VOID_INGOT.get(), 8))
                    .inputItems(ingot(GTNNMaterials.Elementium, 8))
                    .notConsumable(stone2.copy())
                    .outputFluids(PollutionMaterials.AethericDarkSteel.getFluid(1152))
                    .circuitMeta(21)
                    .blastFurnaceTemp(7200)
                    .duration(12000)
                    .EUt(30720)
                    .save(provider);

            // 阿弗纳斯之血：// 跳过: 整合包无 Blood Magic（life essence 缺失）
        }
    }

    // ////////////////////////////////////
    // ***** helpers *****//
    // ////////////////////////////////////

    /** @return InfusedAura fluid, or null when the material has no fluid in this GTCEu build */
    private static FluidStack aura(int amount) {
        return PollutionMaterials.InfusedAura != null && PollutionMaterials.InfusedAura.hasFluid()
                ? PollutionMaterials.InfusedAura.getFluid(amount) : null;
    }

    private static boolean hasFluid(Material material) {
        return material != null && material.hasFluid();
    }

    private static ItemStack dust(Material material, int amount) {
        if (material == null) {
            return ItemStack.EMPTY;
        }
        return ChemicalHelper.get(TagPrefix.dust, material, amount);
    }

    /** GTNN ManaSteel/TerraSteel/Elementium only carry ingot/fluid, so their dust inputs use ingots. */
    private static ItemStack ingot(Material material, int amount) {
        if (material == null) {
            return ItemStack.EMPTY;
        }
        return ChemicalHelper.get(TagPrefix.ingot, material, amount);
    }

    private static ItemStack philosopherStone(int tier) {
        ItemEntry<Item> entry = PollutionItems.get("stone_of_philosopher_" + tier);
        if (entry == null) {
            Pollution.LOGGER.warn("Forge alchemy: stone_of_philosopher_{} is not registered, "
                    + "skipping its recipes", tier);
            return ItemStack.EMPTY;
        }
        return new ItemStack(entry.get());
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("pollution", "forge_alchemy/" + path);
    }
}
