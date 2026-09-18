package meowmel.pollution.loaders.recipes;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.machines.GCYMMachines;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.tterrag.registrate.util.entry.ItemEntry;
import dev.arbor.gtnn.data.GTNNMaterials;
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
 * ElvenElementium -&gt; NaquadahAlloy, BloodOfAvernus -&gt; TungstenSteel,
 * Impuremana -&gt; InfusedAura (see the task substitution table).</p>
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
 * <p><b>Skipped (210+ recipes)</b></p>
 * <ul>
 *   <li>The six element extractor recipes are already ported by
 *       {@code InfusedProcessingRecipes}; not duplicated.</li>
 *   <li>Six macerator + six autoclave element-crystal recipes: the Thaumcraft
 *       crystal blocks are not exposed by the TC4R API.</li>
 *   <li>Nine spell-prism casings, six Terra casings and five Mana casings:
 *       they need plate/frame shapes of the six aspect alloys, which the port
 *       materials do not generate.</li>
 *   <li>All Thaumcraft infusion / arcane recipes (machine controllers, beam
 *       cores, wire coils, laminated glass, vis hatches, pipes, gearboxes,
 *       battery casing, filters, fusion frames and reactor cores): the 1.12
 *       {@code ThaumcraftApi} infusion/arcane API does not exist in TC4R.</li>
 *   <li>Greatwood/silverwood greenhouse recipes: Thaumcraft blocks absent.</li>
 *   <li>Node blast furnace, small chemical plant and GT essence smelter:
 *       DimensionalTransformingAgent and the Thaumcraft smelter blocks are
 *       unported.</li>
 *   <li>Muti Dan De Life On (assembly line): GTQT Orichalcum unported.</li>
 *   <li>Enchanted soil / alt grass: Botania removed the alt-grass block in
 *       1.20.1.</li>
 *   <li>Eight upgrade items, five advanced components, the central vis tower
 *       and the whole HPCA group: Thaumcraft resonators,
 *       BloodOfAvernus/SentientMetal/BindingMetal/VoidMetal,
 *       Existing/FadingNexus and the BMHPCA machines are unported.</li>
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
    }

    // ////////////////////////////////////
    // ***** six aspect alloys *****//
    // ////////////////////////////////////

    private static void alloyChemistry(Consumer<FinishedRecipe> provider) {
        alloy(provider, "aertitanium", PollutionMaterials.InfusedAir, 20,
                dust(GTMaterials.Bauxite, 2), dust(GTMaterials.Aluminium, 1),
                dust(GTMaterials.Manganese, 1), dust(PollutionMaterials.InfusedAir, 5));
        alloy(provider, "ignissteel", PollutionMaterials.InfusedFire, 20,
                dust(GTMaterials.Steel, 2), dust(GTMaterials.Magnesium, 1),
                dust(GTMaterials.Lithium, 1), dust(PollutionMaterials.InfusedFire, 5));
        alloy(provider, "aquasilver", PollutionMaterials.InfusedWater, 20,
                dust(GTMaterials.Silver, 2), dust(GTMaterials.Tin, 1),
                dust(PollutionMaterials.InfusedWater, 5));
        alloy(provider, "terracopper", PollutionMaterials.InfusedEarth, 20,
                dust(GTMaterials.Copper, 2), dust(GTMaterials.Boron, 1),
                dust(GTMaterials.Carbon, 1), dust(PollutionMaterials.InfusedEarth, 5));
        alloy(provider, "ordolead", PollutionMaterials.InfusedOrder, 20,
                dust(GTMaterials.Lead, 2), dust(GTMaterials.Silicon, 1),
                dust(GTMaterials.Gold, 1), dust(PollutionMaterials.InfusedOrder, 5));
        alloy(provider, "perditioaluminium", PollutionMaterials.InfusedEntropy, 0,
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

        // 不纯魔力搅拌（Impuremana -> InfusedAura）
        FluidStack impureMana = fluid(PollutionMaterials.InfusedAura, 48);
        if (impureMana != null) {
            impureMana(provider, "order_entropy", PollutionMaterials.InfusedOrder,
                    PollutionMaterials.InfusedEntropy, impureMana);
            impureMana(provider, "air_earth", PollutionMaterials.InfusedAir,
                    PollutionMaterials.InfusedEarth, impureMana);
            impureMana(provider, "fire_water", PollutionMaterials.InfusedFire,
                    PollutionMaterials.InfusedWater, impureMana);
        } else {
            Pollution.LOGGER.warn("Skipping the magic_gcym Impuremana mixer group: InfusedAura has no fluid");
        }

        // 不纯魔力 + 铁粉 -> 魔力钢锭
        FluidStack mana = fluid(PollutionMaterials.InfusedAura, 144);
        if (mana != null) {
            GTRecipeBuilder.of(id("manasteel_ingot"), GTRecipeTypes.BLAST_RECIPES)
                    .inputItems(dust(GTMaterials.Iron, 1))
                    .inputFluids(mana)
                    .outputItems(ChemicalHelper.get(TagPrefix.ingot, GTNNMaterials.ManaSteel, 1))
                    .blastFurnaceTemp(1800)
                    .duration(400)
                    .EUt(GTValues.VA[GTValues.LV])
                    .save(provider);
        } else {
            Pollution.LOGGER.warn("Skipping magic_gcym/manasteel_ingot: InfusedAura has no fluid");
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
