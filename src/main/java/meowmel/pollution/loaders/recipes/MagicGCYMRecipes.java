package meowmel.pollution.loaders.recipes;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.machines.GCYMMachines;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.tterrag.registrate.util.entry.ItemEntry;
import dev.arbor.gtnn.data.GTNNMaterials;
import dev.tc4port.thaumcraft.api.ThaumcraftContent;
import dev.tc4port.thaumcraft.registry.TCBlocks;
import dev.tc4port.thaumcraft.registry.TCItems;
import meowmel.pollution.Pollution;
import meowmel.pollution.api.recipes.PORecipeMaps;
import meowmel.pollution.api.unification.PollutionMaterials;
import meowmel.pollution.common.block.PollutionMagicBlocks;
import meowmel.pollution.common.item.PollutionItems;
import meowmel.pollution.common.machine.PollutionMachines;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.item.BotaniaItems;

import java.util.function.Consumer;

/**
 * GCYM-era magic recipes, port of the portable subset of upstream
 * {@code meowmel.pollution.loaders.recipes.MagicGCYMRecipes} (38 of 250+
 * recipes).
 *
 * <p><b>Material substitutions</b>: Manasteel -&gt; {@code GTNNMaterials.ManaSteel},
 * Thaumium -&gt; StainlessSteel, Mansussteel -&gt; HSSG, KQGold -&gt; TungstenSteel,
 * HyperdimensionalSilver -&gt; NaquadahAlloy, Terrasteel -&gt; TungstenSteel,
 * ElvenElementium -&gt; NaquadahAlloy, BloodOfAvernus -&gt; TungstenSteel.
 * {@code Impuremana} is a real port material now and is used directly by the
 * mana mixers (see the task substitution table).</p>
 *
 * <p><b>Modern API substitutions</b></p>
 * <ul>
 *   <li>The six aspect alloys are registered with {@code ingot/fluid} only
 *       (no dust, plate or frame shapes), so the upstream dust inputs/outputs
 *       use ingots instead; the casing recipes that need plates/frames are
 *       skipped.</li>
 *   <li>Upstream {@code MetaTileEntities.PARALLEL_HATCH[0..3]} (IV..UV) maps
 *       to {@link GCYMMachines#PARALLEL_HATCH} at LuV..UHV, the tiers GTCEu
 *       7.5.3 registers.</li>
 *   <li>Upstream {@code LARGE_GAS_COLLECTOR} does not exist in GTCEu 7.5.3;
 *       the Terra gas collector uses the single-block
 *       {@link GTMachines#GAS_COLLECTOR} as its machine input.</li>
 *   <li>Tiered {@code circuit} items map to the modern {@code circuitMeta}
 *       configuration input.</li>
 * </ul>
 *
 * <p><b>Newly ported (previously skipped)</b></p>
 * <ul>
 *   <li>Six macerator + six autoclave element-crystal recipes using the TC4R
 *       crystal clusters. // 上游: BlocksTC.crystalAir/Fire/Water/Earth/Order/
 *       Entropy -> 本移植版: thaumcraft:*_crystal_cluster</li>
 *   <li>Nine spell-prism casings, six Terra casings and five Mana casings.
 *       // 上游: plate/frameGt of the six aspect alloys -> 本移植版: the same
 *       alloy ingot + HSSG frame（合金只有 ingot/fluid）；Terrasteel/
 *       ElvenElementium/Manasteel -> GTNN TerraSteel/Elementium/ManaSteel；
 *       GTQT Mana -> InfusedAura；Botania rune meta 1/3/2/4/6 -> 命名符文</li>
 *   <li>Greatwood/silverwood greenhouse recipes (TC4R ships the trees).</li>
 *   <li>Node blast furnace, small chemical plant and GT essence smelter
 *       (DimensionalTransformingAgent now exists; BlocksTC.smelterThaumium ->
 *       TC4R alchemical furnace; GTQT chemical plant -> GTCEu large chemical
 *       reactor).</li>
 *   <li>Eight upgrade items (// 上游: ItemsTC.morphicResonator ->
 *       TC4R NODE_TRANSDUCER).</li>
 *   <li>Five advanced components + the central vis tower as magic assembler
 *       recipes without the Astral condition (Astral Sorcery is not in the
 *       pack). // 上游: IizunamaruElectrum -> Electrum, AethericDarkSteel ->
 *       HSSG, BloodOfAvernus -> TungstenSteel, GTQT VoidMetal -> TC4R void
 *       ingot, ItemsTC.causalityCollapser -> PRIMORDIAL_PEARL,
 *       Starrymansus/BlackMansus/WhiteMansus -> InfusedAura, ErichAura ->
 *       InfusedAura.</li>
 *   <li>The arcane-crafting half (glass, pipes, gearboxes, battery casing,
 *       filters) as GT assembler recipes. // 上游: plateMansussteel -> HSSG
 *       plate, plateThaumium -> StainlessSteel plate, ItemsTC.visResonator ->
 *       ESSENTIA_RESONATOR, ItemsTC.morphicResonator -> NODE_TRANSDUCER,
 *       BlocksTC.visBattery -> VIS_CHARGE_RELAY</li>
 *   <li>Enchanted soil / infused grass. // 上游: Botania altGrass（1.20.1 已移除）
 *       -> 原版草方块输入 + Botania enchantedSoil 输出；BlackMansus /
 *       WhiteMansus -> InfusedAura；dust Terrasteel -> GTNN TerraSteel ingot</li>
 * </ul>
 *
 * <p><b>Still skipped</b></p>
 * <ul>
 *   <li>The six element extractor recipes are already ported by
 *       {@code InfusedProcessingRecipes}; not duplicated.</li>
 *   <li>// 跳过: GTQT Orichalcum 未移植且机器未注册（Muti Dan De Life On 装配线）。</li>
 *   <li>// 跳过: 整合包无 Blood Magic，且 BMHPCA 机器未注册（整个 HPCA 组）。</li>
 *   <li>// 跳过: 整合包无 GTFO（魔法温室主方块注魔；温室配方本身已移植）。</li>
 *   <li>// 跳过: 魔法涡轮机（小）机器未注册（turbine_1..3 奥术配方）。</li>
 * </ul>
 */
public final class MagicGCYMRecipes {

    private MagicGCYMRecipes() {}

    public static void init(Consumer<FinishedRecipe> provider) {
        alloyChemistry(provider);
        manasteelChain(provider);
        casings(provider);
        machineRecipes(provider);
        botaniaRecipes(provider);
        elementCrystals(provider);
        spellPrismCasings(provider);
        terraManaCasings(provider);
        greenhouseRecipes(provider);
        advancedMachines(provider);
        upgrades(provider);
        advancedComponents(provider);
        arcaneCasings(provider);
        enchantedSoil(provider);
    }

    // ////////////////////////////////////
    // ***** enchanted soil *****//
    // ////////////////////////////////////

    /**
     * 蕴魔草地/注魔草地。// 上游: Botania altGrass 在 1.20.1 已移除 ->
     * 本移植版: 原版草方块作为输入、Botania enchantedSoil 作为输出；
     * BlackMansus/WhiteMansus -> InfusedAura；dust Terrasteel ->
     * GTNN TerraSteel ingot。
     */
    private static void enchantedSoil(Consumer<FinishedRecipe> provider) {
        FluidStack aura = fluid(PollutionMaterials.InfusedAura, 12800);
        ItemStack terraSteel = ChemicalHelper.get(TagPrefix.ingot, GTNNMaterials.TerraSteel, 64);
        if (aura == null || terraSteel.isEmpty()) {
            Pollution.LOGGER.warn("Skipping magic_gcym enchanted soil: a required input is missing");
            return;
        }
        GTRecipeBuilder.of(id("enchanted_soil"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(BotaniaItems.overgrowthSeed, 64)
                .inputItems(net.minecraft.world.item.Items.GRASS_BLOCK, 128)
                .inputItems(terraSteel)
                .inputFluids(aura)
                .outputItems(new ItemStack(BotaniaBlocks.enchantedSoil, 128))
                .duration(500)
                .EUt(30720)
                .save(provider);

        FluidStack auraSmall = fluid(PollutionMaterials.InfusedAura, 6400);
        if (auraSmall == null) {
            return;
        }
        GTRecipeBuilder.of(id("infused_grass"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(BotaniaItems.grassSeeds, 64)
                .inputItems(net.minecraft.world.item.Items.GRASS_BLOCK, 128)
                .inputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.Salisundus, 64))
                .inputFluids(auraSmall)
                .outputItems(new ItemStack(BotaniaBlocks.enchantedSoil, 128))
                .duration(500)
                .EUt(8192)
                .save(provider);
    }

    // ////////////////////////////////////
    // ***** six aspect alloys *****//
    // ////////////////////////////////////

    private static void alloyChemistry(Consumer<FinishedRecipe> provider) {
        // 上游: dust <alloy> -> 本移植版: ingot <alloy>（六种元素合金只有 ingot/fluid 形态）
        alloy(provider, "aertitanium", PollutionMaterials.Aertitanium, 20,
                dust(GTMaterials.Bauxite, 2), dust(GTMaterials.Aluminium, 1),
                dust(GTMaterials.Manganese, 1), dust(PollutionMaterials.InfusedAir, 5));
        alloy(provider, "ignissteel", PollutionMaterials.IgnisSteel, 20,
                dust(GTMaterials.Steel, 2), dust(GTMaterials.Magnesium, 1),
                dust(GTMaterials.Lithium, 1), dust(PollutionMaterials.InfusedFire, 5));
        alloy(provider, "aquasilver", PollutionMaterials.Aquasilver, 20,
                dust(GTMaterials.Silver, 2), dust(GTMaterials.Tin, 1),
                dust(PollutionMaterials.InfusedWater, 5));
        alloy(provider, "terracopper", PollutionMaterials.Terracopper, 20,
                dust(GTMaterials.Copper, 2), dust(GTMaterials.Boron, 1),
                dust(GTMaterials.Carbon, 1), dust(PollutionMaterials.InfusedEarth, 5));
        alloy(provider, "ordolead", PollutionMaterials.Ordolead, 20,
                dust(GTMaterials.Lead, 2), dust(GTMaterials.Silicon, 1),
                dust(GTMaterials.Gold, 1), dust(PollutionMaterials.InfusedOrder, 5));
        alloy(provider, "perditioaluminium", PollutionMaterials.Perditioaluminium, 0,
                dust(GTMaterials.Aluminium, 2), dust(GTMaterials.Thorium, 1),
                dust(PollutionMaterials.InfusedEntropy, 5));

        hotIngot(provider, "aertitanium", PollutionMaterials.Aertitanium, PollutionMaterials.InfusedAir);
        hotIngot(provider, "ignissteel", PollutionMaterials.IgnisSteel, PollutionMaterials.InfusedFire);
        hotIngot(provider, "aquasilver", PollutionMaterials.Aquasilver, PollutionMaterials.InfusedWater);
        hotIngot(provider, "terracopper", PollutionMaterials.Terracopper, PollutionMaterials.InfusedEarth);
        hotIngot(provider, "ordolead", PollutionMaterials.Ordolead, PollutionMaterials.InfusedOrder);
        hotIngot(provider, "perditioaluminium", PollutionMaterials.Perditioaluminium,
                PollutionMaterials.InfusedEntropy);
    }

    private static void alloy(Consumer<FinishedRecipe> provider, String name, Material output, int circuit,
                              ItemStack... inputs) {
        FluidStack molten = fluid(output, 9 * 144);
        if (molten == null) {
            Pollution.LOGGER.warn("Skipping magic_gcym alloy {}: material has no fluid", name);
            return;
        }
        GTRecipeBuilder mixer = GTRecipeBuilder.of(id("alloy_mixer/" + name), GTRecipeTypes.MIXER_RECIPES);
        for (ItemStack input : inputs) {
            mixer.inputItems(input);
        }
        if (circuit > 0) {
            mixer.circuitMeta(circuit);
        }
        mixer.outputItems(ChemicalHelper.get(TagPrefix.ingot, output, 9))
                .duration(900)
                .EUt(GTValues.VA[GTValues.LV])
                .save(provider);

        GTRecipeBuilder blast = GTRecipeBuilder.of(id("alloy_blast/" + name),
                PORecipeMaps.MAGIC_ALLOY_BLAST_RECIPES);
        for (ItemStack input : inputs) {
            blast.inputItems(input);
        }
        blast.outputFluids(molten)
                .circuitMeta(20)
                .duration(300)
                .blastFurnaceTemp(2700)
                .EUt(GTValues.VA[GTValues.HV])
                .save(provider);
    }

    private static void hotIngot(Consumer<FinishedRecipe> provider, String name, Material alloy, Material aspect) {
        FluidStack essence = fluid(aspect, 144);
        if (essence == null) {
            Pollution.LOGGER.warn("Skipping magic_gcym hot ingot {}: {} has no fluid", name, aspect);
            return;
        }
        GTRecipeBuilder.of(id("hot_ingot/" + name), GTRecipeTypes.BLAST_RECIPES)
                .inputItems(ChemicalHelper.get(TagPrefix.ingot, alloy, 1))
                .inputFluids(essence)
                .outputItems(ChemicalHelper.get(TagPrefix.ingotHot, alloy, 1))
                .circuitMeta(11)
                .duration(500)
                .blastFurnaceTemp(2700)
                .EUt(GTValues.VA[GTValues.HV])
                .save(provider);
    }

    // ////////////////////////////////////
    // ***** mana / mansus chain *****//
    // ////////////////////////////////////

    private static void manasteelChain(Consumer<FinishedRecipe> provider) {
        // 魔力钢简化配方（Manasteel -> GTNN ManaSteel）
        FluidStack manaSteel = fluid(GTNNMaterials.ManaSteel, 576);
        if (manaSteel != null) {
            GTRecipeBuilder.of(id("manasteel_simplified"), PORecipeMaps.MAGIC_ALLOY_BLAST_RECIPES)
                    .inputItems(dust(PollutionMaterials.InfusedAir, 1))
                    .inputItems(dust(PollutionMaterials.InfusedFire, 1))
                    .inputItems(dust(PollutionMaterials.InfusedWater, 1))
                    .inputItems(dust(PollutionMaterials.InfusedEarth, 1))
                    .inputItems(dust(PollutionMaterials.InfusedOrder, 1))
                    .inputItems(dust(PollutionMaterials.InfusedEntropy, 1))
                    .inputItems(dust(GTMaterials.Iron, 4))
                    .outputFluids(manaSteel)
                    .circuitMeta(1)
                    .duration(400)
                    .blastFurnaceTemp(2700)
                    .EUt(GTValues.VA[GTValues.HV])
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping magic_gcym/manasteel_simplified: GTNN ManaSteel has no fluid");
        }

        // 神秘锭简化配方（Thaumium -> StainlessSteel）
        FluidStack stainlessSteel = fluid(GTMaterials.StainlessSteel, 3024);
        if (stainlessSteel != null) {
            GTRecipeBuilder.of(id("thaumium_simplified"), PORecipeMaps.MAGIC_ALLOY_BLAST_RECIPES)
                    .inputItems(dust(GTMaterials.Iron, 1))
                    .inputItems(dust(PollutionMaterials.InfusedEarth, 10))
                    .inputItems(dust(PollutionMaterials.InfusedFire, 5))
                    .inputItems(dust(PollutionMaterials.InfusedAir, 5))
                    .outputFluids(stainlessSteel)
                    .circuitMeta(2)
                    .duration(1200)
                    .blastFurnaceTemp(2700)
                    .EUt(GTValues.VA[GTValues.HV])
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping magic_gcym/thaumium_simplified: StainlessSteel has no fluid");
        }

        // 不纯魔力搅拌（Impuremana 为真实材料）
        FluidStack impureMana = fluid(PollutionMaterials.Impuremana, 48);
        if (impureMana != null) {
            impureMana(provider, "order_entropy", PollutionMaterials.InfusedOrder,
                    PollutionMaterials.InfusedEntropy, impureMana);
            impureMana(provider, "air_earth", PollutionMaterials.InfusedAir,
                    PollutionMaterials.InfusedEarth, impureMana);
            impureMana(provider, "fire_water", PollutionMaterials.InfusedFire,
                    PollutionMaterials.InfusedWater, impureMana);
        } else {
            Pollution.LOGGER.warn("Skipping the magic_gcym Impuremana mixer group: Impuremana has no fluid");
        }

        // 不纯魔力 + 铁粉 -> 魔力钢锭（上游: Manasteel -> GTNN ManaSteel）
        FluidStack impureMana144 = fluid(PollutionMaterials.Impuremana, 144);
        if (impureMana144 != null) {
            GTRecipeBuilder.of(id("manasteel_ingot"), GTRecipeTypes.BLAST_RECIPES)
                    .inputItems(dust(GTMaterials.Iron, 1))
                    .inputFluids(impureMana144)
                    .outputItems(ChemicalHelper.get(TagPrefix.ingot, GTNNMaterials.ManaSteel, 1))
                    .blastFurnaceTemp(1800)
                    .duration(400)
                    .EUt(GTValues.VA[GTValues.LV])
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping magic_gcym/manasteel_ingot: Impuremana has no fluid");
        }

        // 世界盐搅拌
        FluidStack redstone = fluid(GTMaterials.Redstone, 288);
        if (redstone == null) {
            Pollution.LOGGER.warn("Skipping magic_gcym/salisundus: Redstone has no fluid");
        } else {
            GTRecipeBuilder.of(id("salisundus"), GTRecipeTypes.MIXER_RECIPES)
                    .inputItems(dust(PollutionMaterials.InfusedAir, 1))
                    .inputItems(dust(PollutionMaterials.InfusedFire, 1))
                    .inputItems(dust(PollutionMaterials.InfusedWater, 1))
                    .inputItems(dust(PollutionMaterials.InfusedEarth, 1))
                    .inputItems(dust(PollutionMaterials.InfusedOrder, 1))
                    .inputItems(dust(PollutionMaterials.InfusedEntropy, 1))
                    .inputFluids(redstone)
                    .outputItems(dust(PollutionMaterials.Salisundus, 6))
                    .duration(600)
                    .EUt(GTValues.VA[GTValues.LV])
                    .save(provider);
        }

        // 漫宿钢（Mansussteel -> HSSG；上游 Manasteel 粉尘在本移植中改用锭）
        ItemStack manaSteelIngot = ChemicalHelper.get(TagPrefix.ingot, GTNNMaterials.ManaSteel, 3);
        if (!manaSteelIngot.isEmpty()) {
            GTRecipeBuilder.of(id("mansussteel"), GTRecipeTypes.MIXER_RECIPES)
                    .inputItems(manaSteelIngot)
                    .inputItems(dust(GTMaterials.StainlessSteel, 2))
                    .inputItems(dust(PollutionMaterials.Salisundus, 1))
                    .outputItems(dust(GTMaterials.HSSG, 6))
                    .duration(600)
                    .EUt(GTValues.VA[GTValues.LV])
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping magic_gcym/mansussteel: GTNN ManaSteel has no ingot");
        }
    }

    private static void impureMana(Consumer<FinishedRecipe> provider, String name, Material first, Material second,
                                   FluidStack output) {
        GTRecipeBuilder.of(id("impure_mana/" + name), GTRecipeTypes.MIXER_RECIPES)
                .inputItems(dust(first, 1))
                .inputItems(dust(second, 1))
                .outputFluids(output)
                .circuitMeta(1)
                .duration(100)
                .EUt(GTValues.VA[GTValues.LV])
                .save(provider);
    }

    // ////////////////////////////////////
    // ***** casings *****//
    // ////////////////////////////////////

    private static void casings(Consumer<FinishedRecipe> provider) {
        // 泰拉防水外壳（Terrasteel -> TungstenSteel，Mansussteel -> HSSG）
        GTRecipeBuilder.of(id("terra_watertight_casing"), GTRecipeTypes.ASSEMBLER_RECIPES)
                .inputItems(ChemicalHelper.get(TagPrefix.plate, GTNNMaterials.ManaSteel, 5))
                .inputItems(ChemicalHelper.get(TagPrefix.plate, GTMaterials.TungstenSteel, 1))
                .inputItems(ChemicalHelper.get(TagPrefix.frameGt, GTMaterials.HSSG, 1))
                .outputItems(PollutionMagicBlocks.TERRA_WATERTIGHT_CASING.asStack())
                .circuitMeta(16)
                .duration(300)
                .EUt(GTValues.VA[GTValues.EV])
                .save(provider);

        // 魔力底板（Thaumium -> StainlessSteel，HyperdimensionalSilver -> NaquadahAlloy）
        GTRecipeBuilder.of(id("mana_basic"), GTRecipeTypes.ASSEMBLER_RECIPES)
                .inputItems(ChemicalHelper.get(TagPrefix.plate, GTMaterials.StainlessSteel, 2))
                .inputItems(ChemicalHelper.get(TagPrefix.plate, GTMaterials.NaquadahAlloy, 4))
                .inputItems(ChemicalHelper.get(TagPrefix.frameGt, GTMaterials.HSSG, 1))
                .outputItems(PollutionMagicBlocks.MANA_BASIC.asStack())
                .circuitMeta(16)
                .duration(300)
                .EUt(GTValues.VA[GTValues.EV])
                .save(provider);
    }

    // ////////////////////////////////////
    // ***** machines *****//
    // ////////////////////////////////////

    private static void machineRecipes(Consumer<FinishedRecipe> provider) {
        FluidStack aura = fluid(PollutionMaterials.InfusedAura, 1000);
        if (aura == null) {
            Pollution.LOGGER.warn("Skipping the magic_gcym machine group: InfusedAura has no fluid");
            return;
        }

        // 并行控制仓（上游 IV..UV -> 7.5.3 的 LuV..UHV）
        int[] tiers = { GTValues.LuV, GTValues.ZPM, GTValues.UV, GTValues.UHV };
        String[] tierNames = { "luv", "zpm", "uv", "uhv" };
        ItemStack[] generators = {
                GTItems.FIELD_GENERATOR_LuV.asStack(), GTItems.FIELD_GENERATOR_ZPM.asStack(),
                GTItems.FIELD_GENERATOR_UV.asStack(), GTItems.FIELD_GENERATOR_UHV.asStack(),
        };
        for (int index = 0; index < tiers.length; index++) {
            int tier = tiers[index];
            if (GCYMMachines.PARALLEL_HATCH[tier] == null) {
                continue;
            }
            GTRecipeBuilder.of(id("parallel_hatch/" + tierNames[index]), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                    .inputItems(GTMachines.HULL[tier], 4)
                    .inputItems(generators[index])
                    .inputItems(ChemicalHelper.get(TagPrefix.frameGt, GTMaterials.HSSG, 4))
                    .inputFluids(fluid(PollutionMaterials.InfusedAura, 1000))
                    .outputItems(GCYMMachines.PARALLEL_HATCH[tier], 4)
                    .duration(800)
                    .EUt(GTValues.VA[tier])
                    .save(provider);
        }

        FluidStack turbineFuel = fluid(PollutionMaterials.InfusedAura, 16000);
        FluidStack lubricant = fluid(GTMaterials.Lubricant, 16000);
        if (turbineFuel != null && lubricant != null) {
            // 大型魔力轮机
            GTRecipeBuilder.of(id("large_mana_turbine"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                    .inputItems(PollutionMachines.MAGIC_ENERGY_ABSORBER[GTValues.EV], 8)
                    .circuitMeta(1)
                    .inputItems(GTItems.ELECTRIC_MOTOR_IV, 16)
                    .inputItems(GTItems.ELECTRIC_PUMP_IV, 16)
                    .inputItems(ChemicalHelper.get(TagPrefix.plate, GTMaterials.TungstenSteel, 32))
                    .inputItems(ChemicalHelper.get(TagPrefix.gear, GTMaterials.TungstenSteel, 4))
                    .inputFluids(turbineFuel)
                    .inputFluids(lubricant)
                    .outputItems(PollutionMachines.MAGIC_LARGE_TURBINE)
                    .duration(1000)
                    .EUt(30720)
                    .save(provider);

            // 巨型魔力轮机（装配线；上游的 84 个分级电路合并为配置电路）
            GTRecipeBuilder.of(id("mega_mana_turbine"), GTRecipeTypes.ASSEMBLY_LINE_RECIPES)
                    .inputItems(PollutionMachines.MAGIC_LARGE_TURBINE, 64)
                    .inputItems(ChemicalHelper.get(TagPrefix.frameGt, GTMaterials.NaquadahAlloy, 64))
                    .circuitMeta(4)
                    .inputItems(GTItems.ELECTRIC_PUMP_LuV, 64)
                    .inputItems(GTItems.FIELD_GENERATOR_LuV, 16)
                    .inputItems(ChemicalHelper.get(TagPrefix.plateDense, GTMaterials.TungstenSteel, 32))
                    .inputItems(ChemicalHelper.get(TagPrefix.cableGtHex, GTMaterials.TungstenSteel, 16))
                    .inputFluids(fluid(PollutionMaterials.InfusedAura, 64000))
                    .inputFluids(fluid(GTMaterials.Lubricant, 64000))
                    .outputItems(PollutionMachines.MEGA_MANA_TURBINE)
                    .duration(1600)
                    .EUt(GTValues.VA[GTValues.ZPM])
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping the magic_gcym mana turbine group: a required fluid is missing");
        }

        // 泰拉冰箱
        GTRecipeBuilder.of(id("bot_vacuum_freezer"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(GTMultiMachines.VACUUM_FREEZER, 16)
                .circuitMeta(16)
                .inputItems(GTItems.FIELD_GENERATOR_IV, 4)
                .inputItems(GTItems.ELECTRIC_PUMP_IV, 16)
                .inputItems(ChemicalHelper.get(TagPrefix.plate, GTMaterials.TungstenSteel, 32))
                .inputItems(ChemicalHelper.get(TagPrefix.frameGt, GTMaterials.NaquadahAlloy, 4))
                .inputItems(ChemicalHelper.get(TagPrefix.gear, GTMaterials.TungstenSteel, 4))
                .inputFluids(fluid(PollutionMaterials.InfusedAura, 10000))
                .outputItems(PollutionMachines.BOT_VACUUM_FREEZER)
                .duration(1000)
                .EUt(30720)
                .save(provider);

        // 泰拉集气室（上游大型集气室在 7.5.3 中不存在，改用单方块集气室）
        if (GTMachines.GAS_COLLECTOR.length > 1 && GTMachines.GAS_COLLECTOR[1] != null) {
            GTRecipeBuilder.of(id("bot_gas_collector"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                    .inputItems(GTMachines.GAS_COLLECTOR[1], 16)
                    .circuitMeta(16)
                    .inputItems(GTItems.FIELD_GENERATOR_IV, 4)
                    .inputItems(GTItems.ELECTRIC_PUMP_IV, 16)
                    .inputItems(ChemicalHelper.get(TagPrefix.plate, GTMaterials.NaquadahAlloy, 32))
                    .inputItems(ChemicalHelper.get(TagPrefix.frameGt, GTMaterials.TungstenSteel, 4))
                    .inputItems(ChemicalHelper.get(TagPrefix.gear, GTMaterials.NaquadahAlloy, 4))
                    .inputFluids(fluid(PollutionMaterials.InfusedAura, 10000))
                    .outputItems(PollutionMachines.BOT_GAS_COLLECTOR)
                    .duration(1000)
                    .EUt(30720)
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping magic_gcym/bot_gas_collector: no GTCEu gas collector registered");
        }

        // 泰拉电路组装机（上游 BloodOfAvernus -> TungstenSteel，ElvenElementium -> NaquadahAlloy）
        ItemStack autoElenchus = item("auto_elenchus_device");
        ItemStack elucidator = item("elucidator_of_four_causes");
        if (!autoElenchus.isEmpty() && !elucidator.isEmpty()) {
            GTRecipeBuilder.of(id("bot_circuit_assembler"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                    .inputItems(GTMachines.CIRCUIT_ASSEMBLER[GTValues.LuV], 4)
                    .inputItems(PollutionMagicBlocks.MANA_5.asStack(4))
                    .inputItems(GTItems.FIELD_GENERATOR_LuV, 4)
                    .inputItems(autoElenchus.copyWithCount(4))
                    .inputItems(elucidator.copyWithCount(2))
                    .inputItems(ChemicalHelper.get(TagPrefix.frameGt, GTMaterials.TungstenSteel, 16))
                    .inputItems(ChemicalHelper.get(TagPrefix.plate, GTMaterials.NaquadahAlloy, 16))
                    .inputFluids(fluid(PollutionMaterials.InfusedAura, 16000))
                    .outputItems(PollutionMachines.BOT_CIRCUIT_ASSEMBLER)
                    .duration(4000)
                    .EUt(30720)
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping magic_gcym/bot_circuit_assembler: component items are missing");
        }
    }

    // ////////////////////////////////////
    // ***** Botania *****//
    // ////////////////////////////////////

    private static void botaniaRecipes(Consumer<FinishedRecipe> provider) {
        // 增生之种（BlackMansus/WhiteMansus -> InfusedAura；manaResource meta 8 = pixie dust，
        // grassSeeds meta 7 = infused seeds）
        ItemStack infinity = ChemicalHelper.get(TagPrefix.dust, GTNNMaterials.Infinity, 1);
        FluidStack aura = fluid(PollutionMaterials.InfusedAura, 6400);
        FluidStack uuMatter = fluid(GTMaterials.UUMatter, 12800);
        if (infinity.isEmpty() || aura == null || uuMatter == null) {
            Pollution.LOGGER.warn("Skipping magic_gcym/overgrowth_seed: a required material or fluid is missing");
            return;
        }
        GTRecipeBuilder.of(id("overgrowth_seed"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(BotaniaItems.infusedSeeds, 4)
                .inputItems(BotaniaItems.pixieDust, 4)
                .inputItems(infinity)
                .inputFluids(aura)
                .inputFluids(uuMatter)
                .outputItems(BotaniaItems.overgrowthSeed, 4)
                .duration(5000)
                .EUt(32768)
                .save(provider);
    }

    // ////////////////////////////////////
    // ***** element crystals (TC4R crystal clusters) *****//
    // ////////////////////////////////////

    /**
     * 六要素粉的粉碎与重结晶。上游使用 TC6 的 {@code BlocksTC.crystal*} 方块；
     * TC4R 没有同名方块，改用 TC4R 的 {@code *_crystal_cluster} 方块
     * （// 上游: BlocksTC.crystalAir/Fire/Water/Earth/Order/Entropy ->
     * 本移植版: thaumcraft:*_crystal_cluster）。
     */
    private static void elementCrystals(Consumer<FinishedRecipe> provider) {
        Material[] aspects = { PollutionMaterials.InfusedAir, PollutionMaterials.InfusedFire,
                PollutionMaterials.InfusedWater, PollutionMaterials.InfusedEarth,
                PollutionMaterials.InfusedOrder, PollutionMaterials.InfusedEntropy };
        String[] names = { "air", "fire", "water", "earth", "order", "entropy" };
        for (int index = 0; index < aspects.length; index++) {
            ItemStack cluster = crystal(names[index]);
            if (cluster.isEmpty()) {
                Pollution.LOGGER.warn("Skipping magic_gcym element crystal {}: cluster block missing", names[index]);
                continue;
            }
            GTRecipeBuilder.of(id("element_crystal/" + names[index]), GTRecipeTypes.MACERATOR_RECIPES)
                    .inputItems(cluster)
                    .outputItems(dust(aspects[index], 1))
                    .duration(20)
                    .EUt(120)
                    .save(provider);

            FluidStack essence = fluid(aspects[index], 144);
            if (essence != null) {
                GTRecipeBuilder.of(id("element_crystal_reverse/" + names[index]), GTRecipeTypes.AUTOCLAVE_RECIPES)
                        .inputFluids(essence)
                        .outputItems(cluster)
                        .duration(200)
                        .EUt(120)
                        .save(provider);
            }
        }
    }

    // ////////////////////////////////////
    // ***** spell prism casings *****//
    // ////////////////////////////////////

    /**
     * 九个咒法棱镜外壳。// 上游: plate/frameGt 形态的六种魔法合金 ->
     * 本移植版: 同材料 ingot + HSSG frame（本移植版合金只有 ingot/fluid，
     * Mansussteel -> HSSG）。
     */
    private static void spellPrismCasings(Consumer<FinishedRecipe> provider) {
        prismCasing(provider, "air", PollutionMaterials.Aertitanium, PollutionMagicBlocks.SPELL_PRISM_AIR);
        prismCasing(provider, "hot", PollutionMaterials.IgnisSteel, PollutionMagicBlocks.SPELL_PRISM_HOT);
        prismCasing(provider, "water", PollutionMaterials.Aquasilver, PollutionMagicBlocks.SPELL_PRISM_WATER);
        prismCasing(provider, "earth", PollutionMaterials.Terracopper, PollutionMagicBlocks.SPELL_PRISM_EARTH);
        prismCasing(provider, "order", PollutionMaterials.Ordolead, PollutionMagicBlocks.SPELL_PRISM_ORDER);
        prismCasing(provider, "void", PollutionMaterials.Perditioaluminium, PollutionMagicBlocks.VOID_PRISM);
        prismCasing(provider, "blank", PollutionMaterials.Ordolead, PollutionMagicBlocks.SPELL_PRISM);
        prismCasing(provider, "spell_void", PollutionMaterials.Perditioaluminium,
                PollutionMagicBlocks.SPELL_PRISM_VOID);
        prismCasing(provider, "cold", PollutionMaterials.Aquasilver, PollutionMagicBlocks.SPELL_PRISM_COLD);
    }

    private static void prismCasing(Consumer<FinishedRecipe> provider, String name, Material alloy,
                                    com.tterrag.registrate.util.entry.BlockEntry<net.minecraft.world.level.block.Block> output) {
        ItemStack ingots = ChemicalHelper.get(TagPrefix.ingot, alloy, 6);
        ItemStack frame = ChemicalHelper.get(TagPrefix.frameGt, GTMaterials.HSSG, 1);
        if (ingots.isEmpty() || frame.isEmpty()) {
            Pollution.LOGGER.warn("Skipping magic_gcym spell prism {}: alloy ingot or HSSG frame missing", name);
            return;
        }
        GTRecipeBuilder.of(id("spell_prism/" + name), GTRecipeTypes.ASSEMBLER_RECIPES)
                .inputItems(ingots)
                .inputItems(frame)
                .outputItems(output.asStack())
                .circuitMeta(6)
                .duration(300)
                .EUt(120)
                .save(provider);
    }

    // ////////////////////////////////////
    // ***** terra / mana casings *****//
    // ////////////////////////////////////

    /**
     * 六个泰拉外壳与五个魔力底板。// 上游: plate Terrasteel/ElvenElementium/
     * Manasteel -> 本移植版: GTNN TerraSteel/Elementium/ManaSteel ingot；
     * GTQT Mana -> InfusedAura；Botania rune meta 1/3/2/4/6 -> 命名符文。
     */
    private static void terraManaCasings(Consumer<FinishedRecipe> provider) {
        Material[] terraAlloys = { PollutionMaterials.Aquasilver, PollutionMaterials.Terracopper,
                PollutionMaterials.Ordolead, PollutionMaterials.IgnisSteel, PollutionMaterials.Aertitanium,
                PollutionMaterials.Perditioaluminium };
        var terraBlocks = new com.tterrag.registrate.util.entry.BlockEntry[] {
                PollutionMagicBlocks.TERRA_1_CASING, PollutionMagicBlocks.TERRA_2_CASING,
                PollutionMagicBlocks.TERRA_3_CASING, PollutionMagicBlocks.TERRA_4_CASING,
                PollutionMagicBlocks.TERRA_5_CASING, PollutionMagicBlocks.TERRA_6_CASING };
        FluidStack aura = fluid(PollutionMaterials.InfusedAura, 1000);
        if (aura == null) {
            Pollution.LOGGER.warn("Skipping the magic_gcym terra/mana casing group: InfusedAura has no fluid");
            return;
        }
        for (int index = 0; index < terraAlloys.length; index++) {
            GTRecipeBuilder.of(id("terra_casing/" + (index + 1)), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                    .inputItems(ChemicalHelper.get(TagPrefix.ingot, terraAlloys[index], 3))
                    .inputItems(PollutionMagicBlocks.TERRA_WATERTIGHT_CASING.asStack())
                    .inputItems(ChemicalHelper.get(TagPrefix.ingot, GTNNMaterials.TerraSteel, 3))
                    .inputItems(ChemicalHelper.get(TagPrefix.frameGt, GTMaterials.TungstenSteel, 1))
                    .inputFluids(aura)
                    .outputItems(terraBlocks[index].asStack(2))
                    .circuitMeta(6)
                    .duration(400)
                    .EUt(7680)
                    .save(provider);
        }

        Material[] manaAlloys = { PollutionMaterials.IgnisSteel, PollutionMaterials.Aertitanium,
                PollutionMaterials.Terracopper, PollutionMaterials.Aquasilver, PollutionMaterials.Terracopper };
        Item[] manaRunes = { BotaniaItems.runeFire, BotaniaItems.runeAir, BotaniaItems.runeEarth,
                BotaniaItems.runeSpring, BotaniaItems.runeAutumn };
        var manaBlocks = new com.tterrag.registrate.util.entry.BlockEntry[] {
                PollutionMagicBlocks.MANA_1, PollutionMagicBlocks.MANA_2, PollutionMagicBlocks.MANA_3,
                PollutionMagicBlocks.MANA_4, PollutionMagicBlocks.MANA_5 };
        for (int index = 0; index < manaAlloys.length; index++) {
            GTRecipeBuilder.of(id("mana_casing/" + (index + 1)), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                    .inputItems(ChemicalHelper.get(TagPrefix.ingot, manaAlloys[index], 3))
                    .inputItems(PollutionMagicBlocks.MANA_BASIC.asStack())
                    .inputItems(ChemicalHelper.get(TagPrefix.ingot, GTNNMaterials.Elementium, 3))
                    .inputItems(manaRunes[index])
                    .inputFluids(aura)
                    .outputItems(manaBlocks[index].asStack(2))
                    .circuitMeta(6)
                    .duration(400)
                    .EUt(7680)
                    .save(provider);
        }
    }

    // ////////////////////////////////////
    // ***** greenhouse trees (TC4R greatwood/silverwood) *****//
    // ////////////////////////////////////

    private static void greenhouseRecipes(Consumer<FinishedRecipe> provider) {
        FluidStack earth = fluid(PollutionMaterials.InfusedEarth, 144);
        if (earth == null) {
            Pollution.LOGGER.warn("Skipping the magic_gcym greenhouse group: InfusedEarth has no fluid");
            return;
        }
        GTRecipeBuilder.of(id("greenhouse/greatwood"), PORecipeMaps.MAGIC_GREENHOUSE_RECIPES)
                .inputItems(new ItemStack(TCBlocks.GREATWOOD_SAPLING.get()))
                .inputFluids(earth)
                .outputItems(new ItemStack(TCBlocks.GREATWOOD_LOG.get(), 8))
                .outputItems(new ItemStack(TCBlocks.GREATWOOD_SAPLING.get(), 2))
                .circuitMeta(1)
                .duration(200)
                .EUt(120)
                .save(provider);

        GTRecipeBuilder.of(id("greenhouse/greatwood_fertilized"), PORecipeMaps.MAGIC_GREENHOUSE_RECIPES)
                .inputItems(new ItemStack(TCBlocks.GREATWOOD_SAPLING.get()))
                .inputItems(GTItems.FERTILIZER.asStack())
                .inputFluids(fluid(PollutionMaterials.InfusedEarth, 144))
                .outputItems(new ItemStack(TCBlocks.GREATWOOD_LOG.get(), 16))
                .outputItems(new ItemStack(TCBlocks.GREATWOOD_SAPLING.get(), 4))
                .outputItems(new ItemStack(TCBlocks.GREATWOOD_LEAVES.get(), 16))
                .circuitMeta(2)
                .duration(200)
                .EUt(120)
                .save(provider);

        FluidStack earth288 = fluid(PollutionMaterials.InfusedEarth, 288);
        if (earth288 == null) {
            return;
        }
        GTRecipeBuilder.of(id("greenhouse/silverwood"), PORecipeMaps.MAGIC_GREENHOUSE_RECIPES)
                .inputItems(new ItemStack(TCBlocks.SILVERWOOD_SAPLING.get()))
                .inputFluids(earth288)
                .outputItems(new ItemStack(TCBlocks.SILVERWOOD_LOG.get(), 4))
                .outputItems(new ItemStack(TCBlocks.SILVERWOOD_SAPLING.get(), 1))
                .circuitMeta(1)
                .duration(400)
                .EUt(480)
                .save(provider);

        GTRecipeBuilder.of(id("greenhouse/silverwood_fertilized"), PORecipeMaps.MAGIC_GREENHOUSE_RECIPES)
                .inputItems(new ItemStack(TCBlocks.SILVERWOOD_SAPLING.get()))
                .inputItems(GTItems.FERTILIZER.asStack())
                .inputFluids(fluid(PollutionMaterials.InfusedEarth, 288))
                .outputItems(new ItemStack(TCBlocks.SILVERWOOD_LOG.get(), 8))
                .outputItems(new ItemStack(TCBlocks.SILVERWOOD_SAPLING.get(), 2))
                .outputItems(new ItemStack(TCBlocks.SILVERWOOD_LEAVES.get(), 8))
                .circuitMeta(2)
                .duration(400)
                .EUt(480)
                .save(provider);
    }

    // ////////////////////////////////////
    // ***** node blast furnace / chemical plant / gt essence smelter *****//
    // ////////////////////////////////////

    private static void advancedMachines(Consumer<FinishedRecipe> provider) {
        ItemStack valoniteBlock = ChemicalHelper.get(TagPrefix.block, PollutionMaterials.Valonite, 1);
        ItemStack valonite9 = ChemicalHelper.get(TagPrefix.gem, PollutionMaterials.Valonite, 9);
        FluidStack dta = fluid(PollutionMaterials.DimensionalTransformingAgent, 1000);
        if (dta == null) {
            Pollution.LOGGER.warn("Skipping the magic_gcym advanced machine group: DTA has no fluid");
            return;
        }
        // 节点高炉（上游 BlocksTC.smelterThaumium -> TC4R ALCHEMICAL_FURNACE，
        // frameGtTerrasteel -> TungstenSteel frame，gear HyperdimensionalSilver -> NaquadahAlloy gear）
        GTRecipeBuilder.of(id("node_blast_furnace"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(GTMultiMachines.ELECTRIC_BLAST_FURNACE.asStack(64))
                .inputItems(new ItemStack(TCBlocks.ALCHEMICAL_FURNACE.get(), 16))
                .inputItems(GTItems.FIELD_GENERATOR_IV.asStack(16))
                .inputItems(valonite9)
                .inputItems(ChemicalHelper.get(TagPrefix.gear, GTMaterials.NaquadahAlloy, 4))
                .inputItems(ChemicalHelper.get(TagPrefix.frameGt, GTMaterials.TungstenSteel, 4))
                .inputItems(philosopherStone(1))
                .inputFluids(dta)
                .outputItems(PollutionMachines.NODE_BLAST_FURNACE)
                .duration(10000)
                .EUt(7680)
                .save(provider);

        // 小化工厂（上游 GTQT CHEMICAL_PLANT -> GTCEu 大型化学反应釜）
        GTRecipeBuilder.of(id("small_chemical_plant"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(GTMachines.CHEMICAL_REACTOR[GTValues.IV], 4)
                .inputItems(GTMultiMachines.LARGE_CHEMICAL_REACTOR)
                .inputItems(GTItems.FIELD_GENERATOR_IV.asStack(4))
                .inputItems(valonite9)
                .inputItems(ChemicalHelper.get(TagPrefix.gear, GTMaterials.NaquadahAlloy, 4))
                .inputItems(ChemicalHelper.get(TagPrefix.frameGt, GTMaterials.TungstenSteel, 4))
                .inputItems(PollutionItems.EVOLUTION_CATALYST_CORE.asStack())
                .inputFluids(dta)
                .outputItems(PollutionMachines.SMALL_CHEMICAL_PLANT)
                .duration(10000)
                .EUt(7680)
                .save(provider);

        // GT 版炼金枢纽
        FluidStack aura = fluid(PollutionMaterials.InfusedAura, 1000);
        if (aura != null) {
            GTRecipeBuilder.of(id("gt_essence_smelter"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                    .inputItems(PollutionMachines.ESSENCE_SMELTER)
                    .inputItems(PollutionMachines.MAGIC_CHEMICAL_REACTOR)
                    .inputItems(GTItems.FIELD_GENERATOR_HV.asStack(8))
                    .inputItems(new ItemStack(TCBlocks.ALCHEMICAL_FURNACE.get(), 8))
                    .inputFluids(aura)
                    .outputItems(PollutionMachines.GT_ESSENCE_SMELTER)
                    .circuitMeta(1)
                    .duration(1000)
                    .EUt(1920)
                    .save(provider);
        }
    }

    // ////////////////////////////////////
    // ***** machine upgrades *****//
    // ////////////////////////////////////

    /** 八个升级（电路/电路板两套等价配方）。// 上游: ItemsTC.morphicResonator -> NODE_TRANSDUCER。 */
    private static void upgrades(Consumer<FinishedRecipe> provider) {
        ItemStack[] cores = { PollutionMagicBlocks.BEAM_CORE_0.asStack(), PollutionMagicBlocks.BEAM_CORE_1.asStack(),
                PollutionMagicBlocks.BEAM_CORE_2.asStack(), PollutionMagicBlocks.BEAM_CORE_3.asStack() };
        ItemEntry<?>[] outputs = { PollutionItems.ENERGY_REDUCE, PollutionItems.TIME_INCREASE,
                PollutionItems.PARALLEL_ENHANCE, PollutionItems.OVERCLOCKING_ENHANCE };
        ItemStack[] circuits = { PollutionItems.MAGIC_CIRCUIT_MV.asStack(),
                PollutionItems.MAGIC_CIRCUIT_BOARD_MV.asStack() };
        String[] circuitNames = { "circuit", "board" };
        for (int circuitIndex = 0; circuitIndex < circuits.length; circuitIndex++) {
            for (int index = 0; index < cores.length; index++) {
                GTRecipeBuilder.of(id("upgrade/" + circuitNames[circuitIndex] + "_" + index),
                                GTRecipeTypes.ASSEMBLER_RECIPES)
                        .inputItems(ChemicalHelper.get(TagPrefix.frameGt, GTMaterials.HSSG, 1))
                        .inputItems(cores[index])
                        .inputItems(circuits[circuitIndex])
                        .inputItems(new ItemStack(TCBlocks.NODE_TRANSDUCER.get(), 4))
                        .inputItems(ChemicalHelper.get(TagPrefix.gem, PollutionMaterials.Scabyst, 4))
                        .outputItems(outputs[index].asStack())
                        .duration(400)
                        .EUt(GTValues.VA[GTValues.MV])
                        .save(provider);
            }
        }
    }

    // ////////////////////////////////////
    // ***** advanced components *****//
    // ////////////////////////////////////

    /**
     * 高级魔导组件。上游带 Astral 条件；整合包无 Astral Sorcery，这里改为
     * 等价的魔导组装机配方（条件不生效）。IizunamaruElectrum /
     * AethericDarkSteel / SentientMetal / BindingMetal / HyperdimensionalSilver
     * 现为真实材料。// 上游: BloodOfAvernus -> TungstenSteel，
     * GTQT VoidMetal -> TC4R void ingot，ItemsTC.causalityCollapser ->
     * PRIMORDIAL_PEARL，Starrymansus/BlackMansus/WhiteMansus -> InfusedAura。
     */
    private static void advancedComponents(Consumer<FinishedRecipe> provider) {
        FluidStack aura = fluid(PollutionMaterials.InfusedAura, 1000);
        FluidStack dta = fluid(PollutionMaterials.DimensionalTransformingAgent, 1000);
        if (aura == null || dta == null) {
            Pollution.LOGGER.warn("Skipping the magic_gcym advanced component group: a required fluid is missing");
            return;
        }
        ItemStack coreOfIdea = item("core_of_idea");
        if (coreOfIdea.isEmpty()) {
            return;
        }

        // 理式核心
        GTRecipeBuilder.of(id("component/core_of_idea"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(ChemicalHelper.get(TagPrefix.rodLong, PollutionMaterials.AethericDarkSteel, 2))
                .inputItems(ChemicalHelper.get(TagPrefix.frameGt, PollutionMaterials.AethericDarkSteel, 1))
                .inputItems(ChemicalHelper.get(TagPrefix.plate, PollutionMaterials.IizunamaruElectrum, 8))
                .inputItems(ChemicalHelper.get(TagPrefix.gear, PollutionMaterials.IizunamaruElectrum, 4))
                .inputItems(new ItemStack(TCBlocks.NODE_TRANSDUCER.get(), 16))
                .inputItems(new ItemStack(TCItems.ESSENTIA_RESONATOR.get(), 16))
                .inputItems(GTItems.FIELD_GENERATOR_LuV.asStack())
                .inputFluids(aura)
                .outputItems(coreOfIdea.copy())
                .duration(400)
                .EUt(30720)
                .save(provider);

        // 自动反诘装置（BloodOfAvernus -> TungstenSteel）
        ItemStack autoElenchus = item("auto_elenchus_device");
        if (!autoElenchus.isEmpty()) {
            GTRecipeBuilder.of(id("component/auto_elenchus"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                    .inputItems(ChemicalHelper.get(TagPrefix.gear, GTMaterials.TungstenSteel, 6))
                    .inputItems(ChemicalHelper.get(TagPrefix.gearSmall, GTMaterials.TungstenSteel, 4))
                    .inputItems(ChemicalHelper.get(TagPrefix.rodLong, PollutionMaterials.IizunamaruElectrum, 4))
                    .inputItems(ChemicalHelper.get(TagPrefix.rod, PollutionMaterials.AethericDarkSteel, 4))
                    .inputItems(new ItemStack(TCItems.VOID_INGOT.get(), 8))
                    .inputItems(coreOfIdea.copyWithCount(2))
                    .inputItems(GTItems.ELECTRIC_PISTON_LuV.asStack(2))
                    .inputItems(GTItems.ROBOT_ARM_LuV.asStack())
                    .inputFluids(aura)
                    .outputItems(autoElenchus.copy())
                    .duration(400)
                    .EUt(30720)
                    .save(provider);
        }

        // 太一燃素瓶（BloodOfAvernus -> TungstenSteel）
        ItemStack bottle = item("bottle_of_phlogistonic_oneness");
        FluidStack fire = fluid(PollutionMaterials.InfusedFire, 64000);
        if (!bottle.isEmpty() && fire != null) {
            GTRecipeBuilder.of(id("component/phlogistonic_bottle"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                    .inputItems(ChemicalHelper.get(TagPrefix.rotor, PollutionMaterials.IizunamaruElectrum, 2))
                    .inputItems(ChemicalHelper.get(TagPrefix.gearSmall, GTMaterials.TungstenSteel, 4))
                    .inputItems(ChemicalHelper.get(TagPrefix.ring, PollutionMaterials.IizunamaruElectrum, 32))
                    .inputItems(ChemicalHelper.get(TagPrefix.screw, PollutionMaterials.AethericDarkSteel, 12))
                    .inputItems(new ItemStack(TCItems.VOID_INGOT.get(), 8))
                    .inputItems(coreOfIdea.copyWithCount(2))
                    .inputItems(new ItemStack(TCItems.PRIMORDIAL_PEARL.get(), 4))
                    .inputItems(GTItems.ELECTRIC_PUMP_LuV.asStack(2))
                    .inputFluids(fire)
                    .inputFluids(aura)
                    .outputItems(bottle.copy())
                    .duration(400)
                    .EUt(30720)
                    .save(provider);
        }

        // 四因阐释器（BloodOfAvernus -> TungstenSteel）
        ItemStack elucidator = item("elucidator_of_four_causes");
        if (!elucidator.isEmpty() && !autoElenchus.isEmpty()) {
            GTRecipeBuilder.of(id("component/elucidator"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                    .inputItems(ChemicalHelper.get(TagPrefix.plate, PollutionMaterials.IizunamaruElectrum, 16))
                    .inputItems(ChemicalHelper.get(TagPrefix.plate, GTMaterials.TungstenSteel, 16))
                    .inputItems(ChemicalHelper.get(TagPrefix.plate, PollutionMaterials.AethericDarkSteel, 16))
                    .inputItems(new ItemStack(TCItems.VOID_INGOT.get(), 16))
                    .inputItems(new ItemStack(TCItems.PRIMORDIAL_PEARL.get(), 4))
                    .inputItems(GTItems.ROBOT_ARM_LuV.asStack(2))
                    .inputItems(coreOfIdea.copyWithCount(2))
                    .inputItems(autoElenchus.copy())
                    .inputFluids(fluid(PollutionMaterials.DimensionalTransformingAgent, 8000))
                    .inputFluids(fluid(PollutionMaterials.InfusedAura, 8000))
                    .outputItems(elucidator.copy())
                    .duration(400)
                    .EUt(30720)
                    .save(provider);
        }

        // 意志数据链（SentientMetal/BindingMetal 为真实材料；
        // VoidMetal -> TC4R void ingot）
        ItemStack dataLink = item("symptomatic_vis_data_link");
        if (!dataLink.isEmpty() && !bottle.isEmpty()) {
            GTRecipeBuilder.of(id("component/vis_data_link"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                    .inputItems(ChemicalHelper.get(TagPrefix.plateDouble,
                            PollutionMaterials.IizunamaruElectrum, 16))
                    .inputItems(new ItemStack(TCItems.VOID_INGOT.get(), 16))
                    .inputItems(ChemicalHelper.get(TagPrefix.rodLong, PollutionMaterials.AethericDarkSteel, 8))
                    .inputItems(GTItems.SENSOR_ZPM.asStack())
                    .inputItems(GTItems.EMITTER_ZPM.asStack())
                    .inputItems(coreOfIdea.copyWithCount(2))
                    .inputItems(bottle.copy())
                    .inputFluids(fluid(PollutionMaterials.DimensionalTransformingAgent, 8000))
                    .inputFluids(fluid(PollutionMaterials.SentientMetal, 1440))
                    .inputFluids(fluid(PollutionMaterials.BindingMetal, 1440))
                    .outputItems(dataLink.copy())
                    .duration(400)
                    .EUt(122880)
                    .save(provider);
        }

        // 中控塔（上游 ErichAura -> InfusedAura，HyperdimensionalSilver 为真实材料，
        // ItemsTC.morphicResonator -> NODE_TRANSDUCER）
        if (!dataLink.isEmpty() && PollutionMachines.BOT_GAS_COLLECTOR != null
                && PollutionMachines.FLUX_SCRUBBER != null && PollutionMachines.FLUX_SCRUBBER.length > 1
                && PollutionMachines.FLUX_SCRUBBER[1] != null) {
            GTRecipeBuilder.of(id("central_vis_tower"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                    .inputItems(PollutionMachines.BOT_GAS_COLLECTOR)
                    .inputItems(PollutionMachines.FLUX_SCRUBBER[1])
                    .inputItems(GTMultiMachines.CENTRAL_MONITOR)
                    .inputItems(PollutionMagicBlocks.MANA_BASIC.asStack(4))
                    .inputItems(new ItemStack(TCBlocks.NODE_TRANSDUCER.get(), 64))
                    .inputItems(ChemicalHelper.get(TagPrefix.frameGt,
                            PollutionMaterials.HyperdimensionalSilver, 16))
                    .inputItems(GTItems.EMITTER_LuV.asStack(8))
                    .inputItems(GTItems.SENSOR_LuV.asStack(8))
                    .inputItems(GTItems.FIELD_GENERATOR_LuV.asStack(4))
                    .inputFluids(fluid(PollutionMaterials.InfusedAura, 16000))
                    .outputItems(PollutionMachines.CENTRAL_VIS_TOWER)
                    .duration(4000)
                    .EUt(30720)
                    .save(provider);
        }
    }

    // ////////////////////////////////////
    // ***** arcane casings (upstream arcane crafting -> GT assembler) *****//
    // ////////////////////////////////////

    /**
     * 上游奥术工作台配方改为 GT 组装机配方。
     * // 上游: plateMansussteel -> HSSG plate，plateThaumium -> StainlessSteel
     * plate，ItemsTC.visResonator -> ESSENTIA_RESONATOR，ItemsTC.morphicResonator
     * -> NODE_TRANSDUCER，BlocksTC.visBattery -> VIS_CHARGE_RELAY。
     */
    private static void arcaneCasings(Consumer<FinishedRecipe> provider) {
        FluidStack aura500 = fluid(PollutionMaterials.InfusedAura, 500);
        if (aura500 == null) {
            return;
        }
        // 玻璃 5 种
        var glasses = new com.tterrag.registrate.util.entry.BlockEntry[] {
                PollutionMagicBlocks.LAMINATED_GLASS, PollutionMagicBlocks.AAMINATED_GLASS,
                PollutionMagicBlocks.BAMINATED_GLASS, PollutionMagicBlocks.CAMINATED_GLASS,
                PollutionMagicBlocks.DAMINATED_GLASS };
        for (int index = 0; index < glasses.length; index++) {
            GTRecipeBuilder.of(id("glass/" + index), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                    .inputItems(net.minecraft.world.item.Items.GLASS, 2)
                    .inputItems(new ItemStack(TCBlocks.NODE_TRANSDUCER.get()))
                    .inputFluids(aura500)
                    .outputItems(glasses[index].asStack())
                    .circuitMeta(index + 1)
                    .duration(100)
                    .EUt(1920)
                    .save(provider);
        }

        // 管道 4 种
        pipe(provider, "bronze", GTMaterials.Bronze, GTBlocks.CASING_BRONZE_PIPE, PollutionMagicBlocks.BRONZE_PIPE);
        pipe(provider, "steel", GTMaterials.Steel, GTBlocks.CASING_STEEL_PIPE, PollutionMagicBlocks.STEEL_PIPE);
        pipe(provider, "titanium", GTMaterials.Titanium, GTBlocks.CASING_TITANIUM_PIPE,
                PollutionMagicBlocks.TITANIUM_PIPE);
        pipe(provider, "tungstensteel", GTMaterials.TungstenSteel, GTBlocks.CASING_TUNGSTENSTEEL_PIPE,
                PollutionMagicBlocks.TUNGSTENSTEEL_PIPE);

        // 齿轮箱 5 种
        gearbox(provider, "bronze", GTMaterials.Bronze, GTBlocks.CASING_BRONZE_GEARBOX,
                PollutionMagicBlocks.BRONZE_GEARBOX);
        gearbox(provider, "steel", GTMaterials.Steel, GTBlocks.CASING_STEEL_GEARBOX,
                PollutionMagicBlocks.STEEL_GEARBOX);
        gearbox(provider, "stainless", GTMaterials.StainlessSteel, GTBlocks.CASING_STAINLESS_STEEL_GEARBOX,
                PollutionMagicBlocks.STAINLESS_STEEL_GEARBOX);
        gearbox(provider, "titanium", GTMaterials.Titanium, GTBlocks.CASING_TITANIUM_GEARBOX,
                PollutionMagicBlocks.TITANIUM_GEARBOX);
        gearbox(provider, "tungstensteel", GTMaterials.TungstenSteel, GTBlocks.CASING_TUNGSTENSTEEL_GEARBOX,
                PollutionMagicBlocks.TUNGSTENSTEEL_GEARBOX);

        // 电池外壳
        GTRecipeBuilder.of(id("battery_casing"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(ChemicalHelper.get(TagPrefix.plate, GTMaterials.HSSG, 1))
                .inputItems(ChemicalHelper.get(TagPrefix.plate, GTMaterials.StainlessSteel, 2))
                .inputItems(ChemicalHelper.get(TagPrefix.frameGt, GTMaterials.HSSG, 1))
                .inputItems(GTItems.FIELD_GENERATOR_MV.asStack())
                .outputItems(PollutionMagicBlocks.MAGIC_BATTERY_CASING.asStack(16))
                .duration(100)
                .EUt(1920)
                .save(provider);

        // 过滤器 1..5
        var filters = new com.tterrag.registrate.util.entry.BlockEntry[] {
                PollutionMagicBlocks.FILTER_1, PollutionMagicBlocks.FILTER_2, PollutionMagicBlocks.FILTER_3,
                PollutionMagicBlocks.FILTER_4, PollutionMagicBlocks.FILTER_5 };
        var generators = new ItemStack[] { GTItems.FIELD_GENERATOR_LV.asStack(), GTItems.FIELD_GENERATOR_MV.asStack(),
                GTItems.FIELD_GENERATOR_HV.asStack(), GTItems.FIELD_GENERATOR_EV.asStack() };
        GTRecipeBuilder.of(id("filter/1"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(ChemicalHelper.get(TagPrefix.plate, GTMaterials.HSSG, 3))
                .inputItems(ChemicalHelper.get(TagPrefix.frameGt, GTMaterials.HSSG, 1))
                .inputItems(new ItemStack(TCBlocks.VIS_CHARGE_RELAY.get()))
                .outputItems(filters[0].asStack(4))
                .duration(200)
                .EUt(480)
                .save(provider);
        for (int index = 1; index < filters.length; index++) {
            GTRecipeBuilder.of(id("filter/" + (index + 1)), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                    .inputItems(filters[index - 1].asStack(8))
                    .inputItems(generators[index - 1])
                    .outputItems(filters[index].asStack(8))
                    .duration(200)
                    .EUt(480 << index)
                    .save(provider);
        }
    }

    private static void pipe(Consumer<FinishedRecipe> provider, String name, Material material,
                             com.tterrag.registrate.util.entry.BlockEntry<net.minecraft.world.level.block.Block> boilerCasing,
                             com.tterrag.registrate.util.entry.BlockEntry<net.minecraft.world.level.block.Block> output) {
        GTRecipeBuilder.of(id("pipe/" + name), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(ChemicalHelper.get(TagPrefix.plate, material, 6))
                .inputItems(ChemicalHelper.get(TagPrefix.plate, GTMaterials.HSSG, 2))
                .inputItems(boilerCasing.asStack())
                .outputItems(output.asStack(3))
                .duration(100)
                .EUt(480)
                .save(provider);
    }

    private static void gearbox(Consumer<FinishedRecipe> provider, String name, Material material,
                                com.tterrag.registrate.util.entry.BlockEntry<net.minecraft.world.level.block.Block> turbineCasing,
                                com.tterrag.registrate.util.entry.BlockEntry<net.minecraft.world.level.block.Block> output) {
        GTRecipeBuilder.of(id("gearbox/" + name), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(ChemicalHelper.get(TagPrefix.plate, material, 6))
                .inputItems(ChemicalHelper.get(TagPrefix.plate, GTMaterials.HSSG, 2))
                .inputItems(turbineCasing.asStack())
                .outputItems(output.asStack(3))
                .duration(100)
                .EUt(480)
                .save(provider);
    }

    private static ItemStack crystal(String aspect) {
        var block = ThaumcraftContent.block(aspect + "_crystal_cluster");
        return block == null ? ItemStack.EMPTY : new ItemStack(block.asItem());
    }

    private static ItemStack philosopherStone(int tier) {
        ItemEntry<Item> entry = PollutionItems.get("stone_of_philosopher_" + tier);
        return entry == null ? ItemStack.EMPTY : entry.asStack();
    }

    // ////////////////////////////////////
    // ***** helpers *****//
    // ////////////////////////////////////

    private static ItemStack dust(Material material, int amount) {
        return ChemicalHelper.get(TagPrefix.dust, material, amount);
    }

    private static ItemStack item(String name) {
        ItemEntry<Item> entry = PollutionItems.get(name);
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
        return ResourceLocation.fromNamespaceAndPath("pollution", "magic_gcym/" + path);
    }
}
