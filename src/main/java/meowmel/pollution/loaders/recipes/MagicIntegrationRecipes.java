package meowmel.pollution.loaders.recipes;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
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
 * (26 of 90+ recipes).
 *
 * <p><b>Ported</b>: the sterile slate blank, the natural infused coil, the
 * advanced blood circuit, the tarot stock (blank card plus 21 Major Arcana) and
 * the ULV magic circuit board.</p>
 *
 * <p><b>Material substitutions</b>: GTQT Mana / Starrymansus -&gt;
 * InfusedAura; Sunnarium -&gt; Titanium; the compound infused dusts used by
 * upstream do not exist in the port (the materials only carry
 * {@code ore/gem/fluid}), so {@code gem} stacks are used instead.</p>
 *
 * <p><b>Skipped (65+ recipes)</b></p>
 * <ul>
 *   <li>The whole Astral Sorcery group (foundational materials, optics,
 *       starmetal alchemy, rock-crystal catalysis, attuned wafers, advanced
 *       astral components, the three celestial machines, the astral
 *       constellation circuit boards): Astral Sorcery is not a dependency of
 *       the port and its items, liquid starlight and recipe maps are absent.</li>
 *   <li>Blood Magic bridge (precision rune blank, blood altar slate): the
 *       unported {@code BlocksAS.translucentBlock} / Blood Magic altar API.</li>
 *   <li>The flesh circuit line (blood circuit, living biofilm, ultimate /
 *       supreme boards): BloodPlasma, CelestialBiologicalMedium,
 *       InfusedPurifiedBlood and liquid starlight are unported; only
 *       {@code blood_circuit_advanced} uses fully ported inputs.</li>
 *   <li>Arcane ink capsule: ArcaneInk is unported; the 22nd tarot (The
 *       Magician) uses the Thaumcraft salis-mundus item and is skipped too.</li>
 *   <li>LV/MV/HV magic circuit boards: the port defines BasicSubstrate as a
 *       fluid only (no ingot form) and OpticalGradeAquamarine / the Thaumcraft
 *       vis resonator are unported.</li>
 *   <li>The Thaumcraft infusion components (ball-in-itself, node stabilization
 *       frame, magic control assembly, transform core): TC4R's datapack
 *       infusion serializer is usable from code (see
 *       {@code docs/TC4R_INFUSION_API.md}), but every one of these recipes
 *       needs the unported Thaumcraft vis/morphic resonators (and, for the
 *       ball-in-itself, the Astral Sorcery sky resonator), so they stay
 *       deferred.</li>
 * </ul>
 */
public final class MagicIntegrationRecipes {

    private MagicIntegrationRecipes() {}

    public static void init(Consumer<FinishedRecipe> provider) {
        sterileSlate(provider);
        naturalInfusedCoil(provider);
        bloodCircuit(provider);
        tarotStock(provider);
        magicCircuitBoardUlv(provider);
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

        // 22 张大阿卡纳中 The Magician 使用未移植的 TC salis mundus 物品，跳过
        tarot(provider, 1, "the_fool", PollutionItems.TAROT_THE_FOOL,
                new ItemStack(Items.ENDER_PEARL), GTMaterials.EnderPearl);
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
