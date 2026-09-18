package meowmel.pollution.loaders.recipes;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.tterrag.registrate.util.entry.ItemEntry;
import dev.arbor.gtnn.data.GTNNMaterials;
import meowmel.pollution.Pollution;
import meowmel.pollution.api.recipes.PORecipeMaps;
import meowmel.pollution.api.recipes.properties.MagicRecipeProperties;
import meowmel.pollution.api.unification.PollutionMaterials;
import meowmel.pollution.common.item.PollutionItems;
import meowmel.pollution.common.machine.PollutionMachines;
import meowmel.pollution.common.machine.multiblock.botania.BotaniaRecipeMaps;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.block.BotaniaFlowerBlocks;
import vazkii.botania.common.item.BotaniaItems;

import java.util.function.Consumer;

/**
 * Botania integration recipes, port of upstream {@code BotaniaRecipes}.
 *
 * <p>Upstream held three sections: the three custom rune-altar recipes, the
 * flower-breeding/petal chain and the mana hatch crafting recipes.</p>
 *
 * <p><b>Material substitutions</b> (kept consistent with
 * {@code NodeFusionRecipes}, {@code AERecipes} and {@code MagicStructureElements}):
 * Mansussteel -&gt; HSSG, Terrasteel -&gt; TungstenSteel,
 * ElvenElementium -&gt; NaquadahAlloy,
 * BloodOfAvernus/VoidMetal -&gt; TungstenSteel,
 * GTQT Mana/ErichAura -&gt; InfusedAura,
 * the unported {@code Llp} dust -&gt; SiliconDioxide. The white/starry rune
 * blocks use the real {@code AethericDarkSteel}, {@code HyperdimensionalSilver},
 * {@code IizunamaruElectrum} and {@code KQGold}.</p>
 *
 * <p><b>Modern API substitutions</b></p>
 * <ul>
 *   <li>{@code BACTERIAL_VAT_RECIPES} (GTQT) does not exist in the port; the
 *       flower-breeding and petal recipes run on the port's magic greenhouse
 *       ({@link PORecipeMaps#MAGIC_GREENHOUSE_RECIPES}), the closest ported
 *       item+fluid vat with chanced item output. The upstream cleanroom gate
 *       has no ported machine to enforce it and is dropped.</li>
 *   <li>1.12.2 {@code Blocks.RED_FLOWER}/{@code YELLOW_FLOWER} flatten to
 *       {@link Items#POPPY}/{@link Items#DANDELION}; Botania's meta-16
 *       {@code petal}/{@code dye}/{@code mushroom} items are the named
 *       {@code BotaniaItems#getPetal} / {@code BotaniaBlocks#getMushroom}
 *       accessors and vanilla dye items.</li>
 *   <li>{@code ItemBlockSpecialFlower.ofType(name)} is the named
 *       {@code BotaniaFlowerBlocks} field ({@code pureDaisy}, {@code endoflame},
 *       {@code hydroangeas}); {@code manaResource} meta 15 is
 *       {@code BotaniaItems.enderAirBottle}, meta 8 is
 *       {@code BotaniaItems.pixieDust}, rune meta 8 is
 *       {@code BotaniaItems.runeMana} (metadata order verified against the
 *       1.12-final {@code ItemManaResource}/{@code ItemRune} sources).</li>
 * </ul>
 *
 * <p><b>Skipped (30 recipes)</b></p>
 * <ul>
 *   <li>3 custom rune-altar recipes: they require
 *       {@code PollutionMetaItems.PRECISION_RUNE_BLANK}, which the port has not
 *       registered yet. The recipes are kept in {@link #rune} behind a lookup
 *       guard and activate as soon as the item pass lands.</li>
 *   <li>7 mana resonance coil recipes (coil + six wireless hatch upgrades):
 *       they require the unported {@code MANA_RESONANCE_COIL} item; the
 *       wireless hatches themselves are ported and only wait for that item.</li>
 *   <li>16 {@code ORDINARY_ALGAE} petal variants: the GTQT algae item does not
 *       exist in the port; the other four sources per colour are ported.</li>
 *   <li>4 mana input hatches (UEV/UIV/UXV/OpV): the port's hatch arrays stop at
 *       UHV, matching the machine registration window.</li>
 * </ul>
 */
public final class BotaniaRecipes {

    /** Dye order of the 16 Botania petal/mushroom colours. */
    private static final DyeColor[] COLORS = DyeColor.values();

    private BotaniaRecipes() {}

    public static void init(Consumer<FinishedRecipe> provider) {
        rune(provider);
        flowers(provider);
        manahatch(provider);
    }

    // ////////////////////////////////////
    // ***** custom rune altar recipes *****//
    // ////////////////////////////////////

    /**
     * The three Pollution rune recipes. Blocked on the unported
     * {@code precision_rune_blank} item; skipped with a log message until the
     * item pass registers it.
     */
    private static void rune(Consumer<FinishedRecipe> provider) {
        ItemEntry<Item> runeBlank = PollutionItems.get("precision_rune_blank");
        if (runeBlank == null) {
            Pollution.LOGGER.warn("[botania] skipping 3 custom rune altar recipes: "
                    + "pollution:precision_rune_blank is not registered yet");
            return;
        }
        ItemEntry<Item> coreOfIdea = PollutionItems.get("core_of_idea");

        runeAltar(provider, "white_rune", runeBlank, coreOfIdea,
                new ItemStack(BotaniaItems.runeFire), new ItemStack(BotaniaItems.runeSummer),
                new ItemStack(BotaniaItems.runeLust), new ItemStack(BotaniaItems.runeGluttony),
                ChemicalHelper.get(TagPrefix.block, PollutionMaterials.AethericDarkSteel),
                ChemicalHelper.get(TagPrefix.block, PollutionMaterials.HyperdimensionalSilver),
                PollutionItems.WHITE_RUNE.get());

        // // 上游: block BloodOfAvernus -> 本移植版: TungstenSteel block；
        // block VoidMetal -> 本移植版: TungstenSteel block
        runeAltar(provider, "black_rune", runeBlank, coreOfIdea,
                new ItemStack(BotaniaItems.runeEarth), new ItemStack(BotaniaItems.runeAutumn),
                new ItemStack(BotaniaItems.runeLust), new ItemStack(BotaniaItems.runeGluttony),
                ChemicalHelper.get(TagPrefix.block, GTMaterials.TungstenSteel),
                ChemicalHelper.get(TagPrefix.block, GTMaterials.TungstenSteel),
                PollutionItems.BLACK_RUNE.get());

        runeAltar(provider, "starry_rune", runeBlank, coreOfIdea,
                new ItemStack(BotaniaItems.runeSpring), new ItemStack(BotaniaItems.runeMana),
                new ItemStack(BotaniaItems.runeLust), new ItemStack(BotaniaItems.runeGluttony),
                ChemicalHelper.get(TagPrefix.block, PollutionMaterials.IizunamaruElectrum),
                ChemicalHelper.get(TagPrefix.block, PollutionMaterials.KQGold),
                PollutionItems.STARRY_RUNE.get());
    }

    private static void runeAltar(Consumer<FinishedRecipe> provider, String id,
                                  ItemEntry<Item> runeBlank, ItemEntry<Item> coreOfIdea,
                                  ItemStack rune1, ItemStack rune2, ItemStack rune3, ItemStack rune4,
                                  ItemStack block1, ItemStack block2, Item output) {
        GTRecipeBuilder builder = GTRecipeBuilder
                .of(id("rune_altar/" + id), BotaniaRecipeMaps.MANA_RUNE_ALTAR_RECIPES)
                .inputItems(rune1)
                .inputItems(rune2)
                .inputItems(rune3)
                .inputItems(rune4)
                .inputItems(block1)
                .inputItems(block2)
                .inputItems(new ItemStack(runeBlank.get()))
                .inputItems(new ItemStack(coreOfIdea.get()))
                .inputItems(ChemicalHelper.get(TagPrefix.frameGt, GTMaterials.TungstenSteel))
                .outputItems(output)
                .duration(100)
                .EUt(GTValues.VA[GTValues.EV]);
        MagicRecipeProperties.manaPerTick(builder, 1000L).save(provider);
    }

    // ////////////////////////////////////
    // ***** flower breeding and petals *****//
    // ////////////////////////////////////

    private static void flowers(Consumer<FinishedRecipe> provider) {
        // 白雏菊诱变改造
        flowerMutation(provider, "pure_daisy", Items.OXEYE_DAISY,
                ChemicalHelper.get(TagPrefix.dust, GTMaterials.NetherStar), BotaniaFlowerBlocks.pureDaisy);
        // 火红莲诱变改造
        flowerMutation(provider, "endoflame", Items.POPPY,
                ChemicalHelper.get(TagPrefix.dust, GTMaterials.Blaze), BotaniaFlowerBlocks.endoflame);
        // 水绣球诱变改造 (upstream Llp dust -> SiliconDioxide)
        flowerMutation(provider, "hydroangeas", Items.POPPY,
                ChemicalHelper.get(TagPrefix.dust, GTMaterials.SiliconDioxide), BotaniaFlowerBlocks.hydroangeas);

        // 瓶装末地空气
        GTRecipeBuilder.of(id("bottled_ender_air"), GTRecipeTypes.CANNER_RECIPES)
                .inputFluids(GTMaterials.LiquidEnderAir.getFluid(1000))
                .inputItems(Items.GLASS_BOTTLE)
                .outputItems(new ItemStack(BotaniaItems.enderAirBottle))
                .duration(200)
                .EUt(GTValues.VA[GTValues.HV])
                .save(provider);

        // 染料线：16 色花瓣
        for (int color = 0; color < COLORS.length; color++) {
            DyeColor dyeColor = COLORS[color];
            Item dye = DyeItem.byColor(dyeColor);
            Item petal = BotaniaItems.getPetal(dyeColor);

            petalSource(provider, "poppy", color, Items.POPPY, dye, petal);
            petalSource(provider, "dandelion", color, Items.DANDELION, dye, petal);
            petalSource(provider, "brown_mushroom", color, Items.BROWN_MUSHROOM, dye, petal);
            petalSource(provider, "red_mushroom", color, Items.RED_MUSHROOM, dye, petal);

            GTRecipeBuilder.of(id("petal_to_dye/" + color), GTRecipeTypes.MACERATOR_RECIPES)
                    .inputItems(petal)
                    .outputItems(dye, 2)
                    .duration(40)
                    .EUt(GTValues.VA[GTValues.LV])
                    .save(provider);

            GTRecipeBuilder.of(id("mushroom_to_dye/" + color), GTRecipeTypes.MACERATOR_RECIPES)
                    .inputItems(new ItemStack(BotaniaBlocks.getMushroom(dyeColor)))
                    .outputItems(dye, 2)
                    .duration(40)
                    .EUt(GTValues.VA[GTValues.LV])
                    .save(provider);
        }
    }

    private static void flowerMutation(Consumer<FinishedRecipe> provider, String id, Item host,
                                       ItemStack catalyst, Block flower) {
        GTRecipeBuilder.of(id("flower/" + id), PORecipeMaps.MAGIC_GREENHOUSE_RECIPES)
                .inputItems(host)
                .inputItems(catalyst)
                .inputFluids(GTMaterials.SterileGrowthMedium.getFluid(100))
                .chancedOutput(new ItemStack(flower), 5000, 0)
                .duration(200)
                .EUt(GTValues.VA[GTValues.HV])
                .save(provider);
    }

    private static void petalSource(Consumer<FinishedRecipe> provider, String source, int color,
                                    Item host, Item dye, Item petal) {
        GTRecipeBuilder.of(id("petal/" + source + "/" + color), PORecipeMaps.MAGIC_GREENHOUSE_RECIPES)
                .inputFluids(GTMaterials.Water.getFluid(500))
                .inputItems(host)
                .inputItems(dye)
                .outputItems(petal, 4)
                .duration(200)
                .EUt(GTValues.VA[GTValues.HV])
                .save(provider);
    }

    // ////////////////////////////////////
    // ***** mana hatches *****//
    // ////////////////////////////////////

    /**
     * Mana hatch crafting. The port covers the LV..UHV tier window of the
     * upstream LV..OpV arrays; the UEV..OpV recipes of the upstream arrays are
     * not ported because the corresponding machines are not registered yet.
     * The resonance coil block (1 + 6 wireless recipes) is skipped while
     * {@code mana_resonance_coil} is unported.
     */
    private static void manahatch(Consumer<FinishedRecipe> provider) {
        ItemEntry<?>[] sensors = {
                GTItems.SENSOR_LV, GTItems.SENSOR_MV, GTItems.SENSOR_HV, GTItems.SENSOR_EV,
                GTItems.SENSOR_IV, GTItems.SENSOR_LuV, GTItems.SENSOR_ZPM, GTItems.SENSOR_UV,
                GTItems.SENSOR_UHV,
        };
        ItemEntry<?>[] poolSensors = { GTItems.SENSOR_LV, GTItems.SENSOR_LuV, GTItems.SENSOR_UEV };
        ItemEntry<?>[] poolEmitters = { GTItems.EMITTER_LV, GTItems.EMITTER_LuV, GTItems.EMITTER_UEV };

        // GT's registerTieredMachines returns a tier-indexed array (index 0 = ULV
        // is empty, LV starts at 1), so iterate by tier and skip unregistered slots.
        for (int tier = 1; tier < PollutionMachines.MANA_INPUT_HATCH_1A.length; tier++) {
            if (PollutionMachines.MANA_INPUT_HATCH_1A[tier] == null || tier > sensors.length) {
                continue;
            }
            Material gear = tier <= 5 ? GTMaterials.HSSG : GTMaterials.TungstenSteel;
            GTRecipeBuilder.of(id("mana_input_hatch/" + GTValues.VN[tier].toLowerCase(java.util.Locale.ROOT)), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                    .inputItems(GTMachines.ENERGY_INPUT_HATCH[tier])
                    .inputItems(new ItemStack(BotaniaItems.runeMana))
                    .inputItems(ChemicalHelper.get(TagPrefix.gear, gear, 2))
                    .inputItems(sensors[tier - 1].asStack(2))
                    .inputFluids(PollutionMaterials.InfusedAura.getFluid(1000))
                    .outputItems(PollutionMachines.MANA_INPUT_HATCH_1A[tier])
                    .duration(100)
                    .EUt(GTValues.VA[tier])
                    .save(provider);
        }

        // 三档纯魔力池输出仓：稀释 / 普通 / 神话
        int[] poolTiers = { GTValues.LV, GTValues.LuV, GTValues.UEV };
        Material[] poolGears = { GTMaterials.HSSG, GTMaterials.TungstenSteel, GTMaterials.NaquadahAlloy };
        for (int index = 0; index < poolTiers.length; index++) {
            int tier = poolTiers[index];
            GTRecipeBuilder.of(id("mana_pool_output_hatch/" + GTValues.VN[tier].toLowerCase(java.util.Locale.ROOT)), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                    .inputItems(GTMachines.ENERGY_OUTPUT_HATCH[tier])
                    .inputItems(new ItemStack(BotaniaItems.runeMana))
                    .inputItems(ChemicalHelper.get(TagPrefix.gear, poolGears[index], 2))
                    .inputItems(poolEmitters[index].asStack(2))
                    .inputFluids(PollutionMaterials.InfusedAura.getFluid(1000))
                    .outputItems(PollutionMachines.MANA_POOL_OUTPUT_HATCH[index])
                    .duration(100)
                    .EUt(GTValues.VA[tier])
                    .save(provider);

            GTRecipeBuilder.of(id("mana_pool_input_hatch/" + GTValues.VN[tier].toLowerCase(java.util.Locale.ROOT)), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                    .inputItems(GTMachines.ENERGY_INPUT_HATCH[tier])
                    .inputItems(new ItemStack(BotaniaItems.runeMana))
                    .inputItems(ChemicalHelper.get(TagPrefix.gear, poolGears[index], 2))
                    .inputItems(poolSensors[index].asStack(2))
                    .inputFluids(PollutionMaterials.InfusedAura.getFluid(1000))
                    .outputItems(PollutionMachines.MANA_POOL_INPUT_HATCH[index])
                    .duration(100)
                    .EUt(GTValues.VA[tier])
                    .save(provider);
        }

        // 无线升级所需的魔力谐振线圈（上游 BotaniaRecipes: spark + 末影之眼 +
        // 魔力钢线 + 马努斯钢齿轮 + 魔力）
        GTRecipeBuilder.of(id("mana_resonance_coil"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(new ItemStack(BotaniaItems.spark))
                .inputItems(new ItemStack(Items.ENDER_EYE))
                // 上游: wireGtSingle ManaSteel x8 -> 本移植版: ingot ManaSteel x4
                // （GTNN 魔力钢只有 ingot/fluid 形态，无 WIRE 属性；8 线 = 4 锭）
                .inputItems(ChemicalHelper.get(TagPrefix.ingot, GTNNMaterials.ManaSteel, 4))
                .inputItems(ChemicalHelper.get(TagPrefix.gear, GTMaterials.HSSG, 1))
                .inputFluids(PollutionMaterials.InfusedAura.getFluid(1000))
                .outputItems(PollutionItems.MANA_RESONANCE_COIL.asStack())
                .duration(200)
                .EUt(GTValues.VA[GTValues.LV])
                .save(provider);

        // 三档无线输入/输出仓由对应有线仓升级（方向不变）
        String[] poolNames = { "diluted", "normal", "mythic" };
        int[] poolEuTiers = { GTValues.LV, GTValues.LuV, GTValues.UEV };
        int[] poolCoils = { 1, 2, 4 };
        for (int i = 0; i < poolNames.length; i++) {
            GTRecipeBuilder.of(id("wireless_mana_pool_input_hatch/" + poolNames[i]),
                            PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                    .inputItems(PollutionMachines.MANA_POOL_INPUT_HATCH[i])
                    .inputItems(PollutionItems.MANA_RESONANCE_COIL.asStack(poolCoils[i]))
                    .inputItems(poolSensors[i].asStack(2))
                    .inputFluids(PollutionMaterials.InfusedAura.getFluid(2000))
                    .outputItems(PollutionMachines.WIRELESS_MANA_POOL_INPUT_HATCH[i])
                    .duration(200 + i * 100)
                    .EUt(GTValues.VA[poolEuTiers[i]])
                    .save(provider);

            GTRecipeBuilder.of(id("wireless_mana_pool_output_hatch/" + poolNames[i]),
                            PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                    .inputItems(PollutionMachines.MANA_POOL_OUTPUT_HATCH[i])
                    .inputItems(PollutionItems.MANA_RESONANCE_COIL.asStack(poolCoils[i]))
                    .inputItems(poolEmitters[i].asStack(2))
                    .inputFluids(PollutionMaterials.InfusedAura.getFluid(2000))
                    .outputItems(PollutionMachines.WIRELESS_MANA_POOL_OUTPUT_HATCH[i])
                    .duration(200 + i * 100)
                    .EUt(GTValues.VA[poolEuTiers[i]])
                    .save(provider);
        }
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("pollution", "botania/" + path);
    }
}
