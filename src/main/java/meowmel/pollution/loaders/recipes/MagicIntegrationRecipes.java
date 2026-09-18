package meowmel.pollution.loaders.recipes;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.tterrag.registrate.util.entry.ItemEntry;
import meowmel.pollution.Pollution;
import meowmel.pollution.api.recipes.PORecipeMaps;
import meowmel.pollution.api.unification.PollutionMaterials;
import meowmel.pollution.common.item.PollutionItems;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.fluids.FluidStack;
import vazkii.botania.common.item.BotaniaItems;

import java.util.function.Consumer;

/**
 * Cross-mod integration layer, port of the portable subset of upstream
 * {@code meowmel.pollution.loaders.recipes.MagicIntegrationRecipes}
 * (35 of 90+ recipes).
 *
 * <p><b>Ported</b>: the sterile slate blank, the natural infused coil, the
 * advanced blood circuit, the tarot stock (blank card plus 22 Major Arcana),
 * the ULV/LV/MV/HV/LuV magic circuit boards, the arcane ink capsule and the
 * transform cores.</p>
 *
 * <p><b>Material substitutions</b>: GTQT Mana / Starrymansus -&gt;
 * InfusedAura; Sunnarium -&gt; Titanium; the compound infused dusts used by
 * upstream do not exist in the port (the materials only carry
 * {@code ore/gem/fluid}), so {@code gem} stacks are used instead.</p>
 *
 * <p><b>Newly ported (previously skipped)</b></p>
 * <ul>
 *   <li>Arcane ink capsule (ArcaneInk is registered).</li>
 *   <li>The Magician tarot. // 上游: ItemsTC.salisMundus -> 本移植版:
 *       Salisundus dust</li>
 *   <li>LV/MV/HV magic circuit boards. // 上游: BasicSubstrate ingot ->
 *       本移植版: BasicSubstrate fluid（本移植版材料只有流体）；
 *       plate Manasteel -> GTNN ManaSteel ingot；GTQT Mana -> InfusedAura；
 *       ItemsTC.visResonator -> ESSENTIA_RESONATOR；liquid starlight ->
 *       InfusedAura</li>
 *   <li>EV/IV circuit boards (registered as TC4R infusions in
 *       {@code InfusionRecipes}).</li>
 *   <li>LuV circuit board. // 上游: CelestialBiologicalMedium ->
 *       本移植版: InfusedAura；ASTRAL_LENS_ADVANCED 物品存在（其来源链属
 *       Astral Sorcery，仍跳过）</li>
 * </ul>
 *
 * <p><b>Skipped</b></p>
 * <ul>
 *   <li>The Astral Sorcery group (foundational materials, optics, starmetal
 *       alchemy, rock-crystal catalysis, attuned wafers, advanced astral
 *       components, the three celestial machines, the constellation-specific
 *       ZPM/UV/UHV+ circuit boards): Astral Sorcery is not in the pack and its
 *       items, liquid starlight and recipe maps are absent.
 *       // 跳过: 整合包无 Astral Sorcery</li>
 *   <li>Blood Magic bridge (blood altar slate) and the blood culture line
 *       (living biofilm, ultimate/supreme boards): Blood Magic life essence /
 *       BloodPlasma / CelestialBiologicalMedium / InfusedPurifiedBlood are not
 *       in the pack. // 跳过: 整合包无 Blood Magic</li>
 *   <li>// 跳过: 整合包无 Astral Sorcery / Blood Magic（星图晶圆、星辉晶核、
 *       血液培养链、终极/至高血液电路板、ZPM 及以上电路板、星空机器）。</li>
 * </ul>
 *
 * <p>The Thaumcraft infusion components (ball-in-itself, node stabilization
 * frame, magic control assembly) and the transform core are registered: the
 * infusions live in {@code InfusionRecipes} (with the TC4R resonator
 * substitutions), the transform core is the assembler recipe below.</p>
 */
public final class MagicIntegrationRecipes {

    private MagicIntegrationRecipes() {}

    public static void init(Consumer<FinishedRecipe> provider) {
        sterileSlate(provider);
        naturalInfusedCoil(provider);
        bloodCircuit(provider);
        tarotStock(provider);
        magicCircuitBoardUlv(provider);
        magicCircuitBoardsLvHv(provider);
        magicCircuitBoardLuv(provider);
        transformCores(provider);
    }

    // ////////////////////////////////////
    // ***** transform cores *****//
    // ////////////////////////////////////

    /**
     * 转换核心。// 上游: frameGtMansussteel -> HSSG frame，
     * ItemsTC.morphicResonator -> NODE_TRANSDUCER。
     */
    private static void transformCores(Consumer<FinishedRecipe> provider) {
        ItemStack[] circuits = { PollutionItems.MAGIC_CIRCUIT_EV.asStack(),
                PollutionItems.MAGIC_CIRCUIT_BOARD_EV.asStack() };
        String[] names = { "circuit", "board" };
        FluidStack exchange = fluid(PollutionMaterials.InfusedExchange, 576);
        if (exchange == null) {
            return;
        }
        for (int index = 0; index < circuits.length; index++) {
            GTRecipeBuilder.of(id("transform_core_" + names[index]), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                    .inputItems(ChemicalHelper.get(TagPrefix.frameGt, GTMaterials.HSSG, 1))
                    .inputItems(meowmel.pollution.common.block.PollutionMagicBlocks.BEAM_CORE_4.asStack())
                    .inputItems(circuits[index])
                    .inputItems(PollutionItems.NATURAL_INFUSED_COIL.asStack())
                    .inputItems(new ItemStack(dev.tc4port.thaumcraft.registry.TCBlocks.NODE_TRANSDUCER.get(), 4))
                    .inputFluids(fluid(PollutionMaterials.InfusedExchange, 576))
                    .outputItems(PollutionItems.TRANSFORM_ENHANCE.asStack())
                    .duration(500)
                    .EUt(GTValues.VA[GTValues.EV])
                    .save(provider);
        }
    }

    // ////////////////////////////////////
    // ***** blood / botania intermediates *****//
    // ////////////////////////////////////

    private static void sterileSlate(Consumer<FinishedRecipe> provider) {
        FluidStack chlorine = fluid(GTMaterials.Chlorine, 250);
        if (chlorine == null) {
            Pollution.LOGGER.warn("Skipping magic_integration/sterile_slate_blank: Chlorine has no fluid");
            return;
        }
        GTRecipeBuilder.of(id("sterile_slate_blank"), GTRecipeTypes.CHEMICAL_BATH_RECIPES)
                .inputItems(new ItemStack(Blocks.STONE_SLAB, 4))
                .inputFluids(chlorine)
                .outputItems(PollutionItems.STERILE_SLATE_BLANK.asStack())
                .duration(200)
                .EUt(GTValues.VA[GTValues.MV])
                .save(provider);
    }

    private static void naturalInfusedCoil(Consumer<FinishedRecipe> provider) {
        FluidStack mana = fluid(PollutionMaterials.InfusedAura, 1000);
        if (mana == null) {
            Pollution.LOGGER.warn("Skipping magic_integration/natural_infused_coil: InfusedAura has no fluid");
            return;
        }
        GTRecipeBuilder.of(id("natural_infused_coil"), GTRecipeTypes.ASSEMBLER_RECIPES)
                .inputItems(PollutionItems.MANA_RESONANCE_COIL.asStack())
                // 上游用 InfusedPlant 粉尘；复合要素材料在本移植中只有宝石形态
                .inputItems(ChemicalHelper.get(TagPrefix.gem, PollutionMaterials.InfusedPlant, 4))
                .inputItems(PollutionItems.RUBBER_SLIME.asStack())
                // 上游 Botania 符文 meta 3 = runeAir
                .inputItems(new ItemStack(BotaniaItems.runeAir))
                .inputFluids(mana)
                .outputItems(PollutionItems.NATURAL_INFUSED_COIL.asStack())
                .duration(400)
                .EUt(GTValues.VA[GTValues.HV])
                .save(provider);
    }

    private static void bloodCircuit(Consumer<FinishedRecipe> provider) {
        FluidStack growthMedium = fluid(GTMaterials.SterileGrowthMedium, 1000);
        FluidStack mana = fluid(PollutionMaterials.InfusedAura, 1000);
        if (growthMedium == null || mana == null) {
            Pollution.LOGGER.warn("Skipping magic_integration/blood_circuit_advanced: a required fluid is missing");
            return;
        }
        GTRecipeBuilder.of(id("blood_circuit_advanced"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(PollutionItems.BLOOD_CIRCUIT.asStack())
                .inputItems(PollutionItems.BLOOD_RATS_BRAIN.asStack())
                .inputItems(PollutionItems.BLOOD_MITOCHONDRION_POWER.asStack())
                .inputItems(PollutionItems.BLOOD_ENDORPHINS_STABILIZER.asStack())
                .inputFluids(growthMedium)
                .inputFluids(mana)
                .outputItems(PollutionItems.BLOOD_CIRCUIT_ADVANCED.asStack())
                .duration(500)
                .EUt(GTValues.VA[GTValues.IV])
                .save(provider);
    }

    // ////////////////////////////////////
    // ***** tarot stock *****//
    // ////////////////////////////////////

    private static void tarotStock(Consumer<FinishedRecipe> provider) {
        GTRecipeBuilder.of(id("blank_tarot_card"), GTRecipeTypes.FORMING_PRESS_RECIPES)
                .inputItems(Items.PAPER, 4)
                .inputItems(Items.STRING, 2)
                .outputItems(PollutionItems.BLANK_TAROT_CARD.asStack())
                .duration(120)
                .EUt(GTValues.VA[GTValues.ZPM])
                .save(provider);

        // 秘法墨囊 // 上游: ArcaneInk 流体（现已在移植版注册）
        FluidStack ink = fluid(PollutionMaterials.ArcaneInk, 250);
        if (ink != null) {
            GTRecipeBuilder.of(id("arcane_ink_capsule"), GTRecipeTypes.CANNER_RECIPES)
                    .inputItems(Items.GLASS_BOTTLE)
                    .inputFluids(ink)
                    .outputItems(PollutionItems.ARCANE_INK_CAPSULE.asStack())
                    .duration(100)
                    .EUt(GTValues.VA[GTValues.MV])
                    .save(provider);
        }

        // 22 张大阿卡纳
        tarot(provider, 1, "the_fool", PollutionItems.TAROT_THE_FOOL,
                new ItemStack(Items.ENDER_PEARL), GTMaterials.EnderPearl);
        // 上游: ItemsTC.salisMundus -> 本移植版: Salisundus dust
        tarot(provider, 2, "the_magician", PollutionItems.TAROT_THE_MAGICIAN,
                ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.Salisundus, 1), GTMaterials.Electrum);
        tarot(provider, 3, "the_high_priestess", PollutionItems.TAROT_THE_HIGH_PRIESTESS,
                new ItemStack(Items.ENDER_EYE), GTMaterials.CertusQuartz);
        tarot(provider, 4, "the_empress", PollutionItems.TAROT_THE_EMPRESS,
                new ItemStack(Items.GOLDEN_APPLE), GTMaterials.Titanium);
        tarot(provider, 5, "the_emperor", PollutionItems.TAROT_THE_EMPEROR,
                new ItemStack(Items.IRON_INGOT), GTMaterials.TungstenSteel);
        tarot(provider, 6, "the_highophant", PollutionItems.TAROT_THE_HIGHOPHANT,
                new ItemStack(Items.BOOK), GTMaterials.Palladium);
        tarot(provider, 7, "the_lovers", PollutionItems.TAROT_THE_LOVERS,
                new ItemStack(Items.RED_DYE), GTMaterials.RoseGold);
        tarot(provider, 8, "the_chariot", PollutionItems.TAROT_THE_CHARIOT,
                new ItemStack(Items.MINECART), GTMaterials.Aluminium);
        tarot(provider, 9, "the_strength", PollutionItems.TAROT_THE_STRENGTH,
                new ItemStack(Items.BLAZE_ROD), GTMaterials.Steel);
        tarot(provider, 10, "the_hermit", PollutionItems.TAROT_THE_HERMIT,
                new ItemStack(Items.GOLD_NUGGET), GTMaterials.Platinum);
        tarot(provider, 11, "the_wheel_of_fortune", PollutionItems.TAROT_THE_WHEEL_OF_FORTUNE,
                new ItemStack(Items.CLOCK), GTMaterials.Osmium);
        tarot(provider, 12, "the_justice", PollutionItems.TAROT_JUSTICE,
                new ItemStack(Items.IRON_SWORD), GTMaterials.SolderingAlloy);
        tarot(provider, 13, "the_hanged_man", PollutionItems.TAROT_THE_HANGED_MAN,
                new ItemStack(Items.LEAD), GTMaterials.Lead);
        tarot(provider, 14, "the_death", PollutionItems.TAROT_DEATH,
                new ItemStack(Items.BONE), GTMaterials.NetherStar);
        tarot(provider, 15, "the_temperance", PollutionItems.TAROT_TEMPERANCE,
                new ItemStack(Items.GLASS_BOTTLE), GTMaterials.StainlessSteel);
        tarot(provider, 16, "the_devil", PollutionItems.TAROT_THE_DEVIL,
                new ItemStack(Items.MAGMA_CREAM), GTMaterials.Naquadah);
        tarot(provider, 17, "the_tower", PollutionItems.TAROT_THE_TOWER,
                new ItemStack(Blocks.TNT), GTMaterials.NaquadahAlloy);
        tarot(provider, 18, "the_star", PollutionItems.TAROT_THE_STAR,
                new ItemStack(Items.NETHER_STAR), PollutionMaterials.InfusedLight);
        tarot(provider, 19, "the_moon", PollutionItems.TAROT_THE_MOON,
                new ItemStack(Items.GHAST_TEAR), PollutionMaterials.InfusedDark);
        tarot(provider, 20, "the_sun", PollutionItems.TAROT_THE_SUN,
                new ItemStack(Items.GLOWSTONE_DUST), GTMaterials.Titanium);
        tarot(provider, 21, "the_judgement", PollutionItems.TAROT_JUDGEMENT,
                new ItemStack(Items.TOTEM_OF_UNDYING), GTMaterials.Iridium);
        tarot(provider, 22, "the_world", PollutionItems.TAROT_THE_WORLD,
                new ItemStack(Items.DRAGON_BREATH), GTMaterials.NaquadahEnriched);
    }

    private static void tarot(Consumer<FinishedRecipe> provider, int circuit, String name,
                              ItemEntry<?> card, ItemStack emblem, Material anchor) {
        FluidStack magic = fluid(PollutionMaterials.InfusedMagic, 144);
        ItemStack anchorStack = anchor(anchor, 2);
        if (magic == null || anchorStack.isEmpty()) {
            Pollution.LOGGER.warn("Skipping magic_integration/tarot/{}: InfusedMagic or the anchor material is missing",
                    name);
            return;
        }
        GTRecipeBuilder.of(id("tarot/" + name), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .circuitMeta(circuit)
                .inputItems(PollutionItems.BLANK_TAROT_CARD.asStack())
                .inputItems(PollutionItems.ARCANE_INK_CAPSULE.asStack())
                .inputItems(PollutionItems.MAGIC_CIRCUIT_BOARD_ZPM.asStack())
                .inputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.Salisundus, 1))
                .inputItems(anchorStack)
                .inputItems(emblem)
                .inputFluids(magic)
                .outputItems(card.asStack())
                .duration(200)
                .EUt(GTValues.VA[GTValues.ZPM])
                .save(provider);
    }

    /**
     * Upstream used the anchor material's dust; GTCEu ignores the gem form of
     * several of them in favour of the vanilla/AE2 item, so the dust is tried
     * first and the gem form is used as the fallback.
     */
    private static ItemStack anchor(Material material, int amount) {
        ItemStack dust = ChemicalHelper.get(TagPrefix.dust, material, amount);
        if (!dust.isEmpty()) {
            return dust;
        }
        return ChemicalHelper.get(TagPrefix.gem, material, amount);
    }

    // ////////////////////////////////////
    // ***** circuit boards *****//
    // ////////////////////////////////////

    private static void magicCircuitBoardUlv(Consumer<FinishedRecipe> provider) {
        FluidStack glue = fluid(GTMaterials.Glue, 250);
        if (glue == null) {
            Pollution.LOGGER.warn("Skipping magic_integration/magic_circuit_board_ulv: Glue has no fluid");
            return;
        }
        GTRecipeBuilder.of(id("magic_circuit_board_ulv"), GTRecipeTypes.ASSEMBLER_RECIPES)
                .inputItems(Items.PAPER, 4)
                .inputItems(ChemicalHelper.get(TagPrefix.foil, GTMaterials.Copper, 4))
                .inputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.Salisundus, 1))
                .inputFluids(glue)
                .outputItems(PollutionItems.MAGIC_CIRCUIT_BOARD_ULV.asStack())
                .duration(120)
                .EUt(GTValues.VA[GTValues.ULV])
                .save(provider);
    }

    // ////////////////////////////////////
    // ***** LV..HV circuit boards *****//
    // ////////////////////////////////////

    /**
     * LV/MV/HV 魔法电路板。// 上游: BasicSubstrate ingot -> BasicSubstrate
     * fluid（本移植版只有流体形态）；plate Manasteel -> GTNN ManaSteel ingot；
     * GTQT Mana -> InfusedAura；ItemsTC.visResonator -> ESSENTIA_RESONATOR；
     * liquid starlight -> InfusedAura。
     */
    private static void magicCircuitBoardsLvHv(Consumer<FinishedRecipe> provider) {
        FluidStack basicSubstrate = fluid(PollutionMaterials.BasicSubstrate, 144);
        FluidStack aura = fluid(PollutionMaterials.InfusedAura, 500);
        FluidStack life = fluid(PollutionMaterials.InfusedLife, 288);
        ItemStack manaSteelIngot = ChemicalHelper.get(TagPrefix.ingot, dev.arbor.gtnn.data.GTNNMaterials.ManaSteel, 2);
        if (basicSubstrate == null || aura == null || life == null || manaSteelIngot.isEmpty()) {
            Pollution.LOGGER.warn("Skipping the magic_integration LV..HV circuit board group: a required input is missing");
            return;
        }
        // 上游为奥术工作台配方；LV 板需要两种流体，改用魔导组装机地图
        // （ASSEMBLER_RECIPES 只支持 1 种流体输入，魔导组装机支持 3 种）。
        GTRecipeBuilder.of(id("magic_circuit_board_lv"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                .inputItems(PollutionItems.MAGIC_CIRCUIT_BOARD_ULV.asStack())
                .inputFluids(basicSubstrate)
                .inputItems(manaSteelIngot)
                .inputItems(ChemicalHelper.get(TagPrefix.dust, PollutionMaterials.Salisundus, 2))
                .inputFluids(fluid(PollutionMaterials.InfusedAura, 500))
                .outputItems(PollutionItems.MAGIC_CIRCUIT_BOARD_LV.asStack())
                .duration(160)
                .EUt(GTValues.VA[GTValues.LV])
                .save(provider);

        GTRecipeBuilder.of(id("magic_circuit_board_mv"), GTRecipeTypes.ASSEMBLER_RECIPES)
                .inputItems(PollutionItems.MAGIC_CIRCUIT_BOARD_LV.asStack())
                .inputItems(PollutionItems.STERILE_SLATE_BLANK.asStack(2))
                .inputItems(ChemicalHelper.get(TagPrefix.wireFine, GTMaterials.Silver, 8))
                .inputItems(GTItems.RESISTOR.asStack(2))
                .inputFluids(fluid(PollutionMaterials.InfusedLife, 288))
                .outputItems(PollutionItems.MAGIC_CIRCUIT_BOARD_MV.asStack())
                .duration(220)
                .EUt(GTValues.VA[GTValues.MV])
                .save(provider);

        // 上游 notConsumable ItemsTC.visResonator -> 本移植版 ESSENTIA_RESONATOR；
        // liquid starlight -> InfusedAura
        GTRecipeBuilder.of(id("magic_circuit_board_hv"), GTRecipeTypes.ASSEMBLER_RECIPES)
                .inputItems(PollutionItems.MAGIC_CIRCUIT_BOARD_MV.asStack())
                .inputItems(ChemicalHelper.get(TagPrefix.gem, GTMaterials.CertusQuartz, 2))
                .inputItems(PollutionItems.SILVERED_GLASS_LENS.asStack())
                .inputItems(PollutionItems.MANA_RESONANCE_COIL.asStack())
                .notConsumable(new ItemStack(dev.tc4port.thaumcraft.registry.TCItems.ESSENTIA_RESONATOR.get()))
                .inputFluids(fluid(PollutionMaterials.InfusedAura, 500))
                .outputItems(PollutionItems.MAGIC_CIRCUIT_BOARD_HV.asStack())
                .duration(300)
                .EUt(GTValues.VA[GTValues.HV])
                .save(provider);
    }

    // ////////////////////////////////////
    // ***** LuV circuit board *****//
    // ////////////////////////////////////

    /**
     * LuV 电路板。// 上游: CelestialBiologicalMedium -> 本移植版: InfusedAura
     * （血液培养基属 Blood Magic，整合包无）。ZPM 及以上需要星图晶圆/星辉，
     * 跳过（整合包无 Astral Sorcery）。
     */
    private static void magicCircuitBoardLuv(Consumer<FinishedRecipe> provider) {
        FluidStack bioMedium = fluid(PollutionMaterials.InfusedAura, 500);
        if (bioMedium == null) {
            return;
        }
        GTRecipeBuilder.of(id("magic_circuit_board_luv"), GTRecipeTypes.ASSEMBLER_RECIPES)
                .inputItems(PollutionItems.MAGIC_CIRCUIT_BOARD_IV.asStack())
                .inputItems(PollutionItems.LIVING_MAGIC_BIOFILM.asStack(2))
                .inputItems(PollutionItems.BLOOD_CIRCUIT_ADVANCED.asStack())
                .inputItems(PollutionItems.ASTRAL_LENS_ADVANCED.asStack())
                .inputItems(GTItems.ADVANCED_SMD_CAPACITOR.asStack(8))
                .inputItems(GTItems.ADVANCED_SMD_TRANSISTOR.asStack(8))
                .inputFluids(bioMedium)
                .outputItems(PollutionItems.MAGIC_CIRCUIT_BOARD_LUV.asStack())
                .duration(600)
                .EUt(GTValues.VA[GTValues.IV])
                .save(provider);
    }

    // ////////////////////////////////////
    // ***** helpers *****//
    // ////////////////////////////////////

    /** @return the fluid stack, or null when the material has no fluid in this GTCEu build */
    private static FluidStack fluid(Material material, int amount) {
        if (material == null || !material.hasFluid()) {
            return null;
        }
        return material.getFluid(amount);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("pollution", "magic_integration/" + path);
    }
}
