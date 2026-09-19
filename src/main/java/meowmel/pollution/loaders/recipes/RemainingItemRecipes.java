package meowmel.pollution.loaders.recipes;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import dev.tc4port.thaumcraft.api.ThaumcraftContent;
import meowmel.pollution.Pollution;
import meowmel.pollution.api.recipes.PORecipeMaps;
import meowmel.pollution.api.unification.PollutionMaterials;
import meowmel.pollution.common.item.PollutionItems;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fluids.FluidStack;

import java.util.function.Consumer;

import static meowmel.pollution.loaders.recipes.InfusionRecipes.aspects;
import static meowmel.pollution.loaders.recipes.InfusionRecipes.ing;
import static meowmel.pollution.loaders.recipes.InfusionRecipes.infusion;

/**
 * Final obtainability pass: recipes for the items that the refreshed
 * {@code docs/UNOBTAINABLE_ITEMS.md} audit still listed as recipe-less, minus
 * the task exclusions (astral content, blood magic, debug items).
 *
 * <p><b>Chains</b></p>
 * <ul>
 *   <li><b>Starlight optics / crystal cultivation</b> (HV..UV): silvered glass
 *       lens -&gt; precision rune blank -&gt; rock crystal seed -&gt; celestial
 *       crystal embryo -&gt; cultivated crystal -&gt; attuned crystal wafer
 *       -&gt; constellation data wafer -&gt; celestial calibration core; the
 *       primordial star blood crystal caps the chain as a UV infusion.</li>
 *   <li><b>Magic cores</b> (EV..UHV): depleted magic core (with a centrifuge
 *       recycling loop back to the blank catalyst core) -&gt; causality
 *       catalyst -&gt; harmonizing rune core, plus the UHV needle and cogito
 *       defibrillator components.</li>
 *   <li><b>Air filter cartridges I..V</b>: tiered assembler upgrades using the
 *       filter's own material key (infused earth, infused water, syrmorite,
 *       thaumium, octine).</li>
 *   <li><b>Blood-culture biology</b> (the items are not excluded, only the
 *       blood-circuit line is): primitive meat -&gt; rat brain -&gt;
 *       mitochondrion power / endorphins / lysosome stabilizers -&gt; iPS human
 *       brain, driven by GT meat/growth-medium fluids and the infused aspect
 *       fluids.</li>
 *   <li><b>Utilities</b>: vis checker, packaged aura node (TC4R infusion of a
 *       node in a jar), tar slime, magic sweep, the devay sanity pills and the
 *       pesticide bottle; the true philosopher's stone is the UHV capstone.</li>
 * </ul>
 *
 * <p>Every stack goes through {@link SafeItems} / {@link PollutionItems#get}
 * and every recipe is guarded: a missing item or fluid only logs a warning and
 * skips the affected recipe. No new items, no lang keys, no machine changes.</p>
 */
public final class RemainingItemRecipes {

    private RemainingItemRecipes() {}

    public static void init(Consumer<FinishedRecipe> provider) {
        silveredGlassLens(provider);
        precisionRuneBlank(provider);
        filters(provider);
        crystalChain(provider);
        magicCoreChain(provider);
        integrationCarriers(provider);
        biologyChain(provider);
        utilities(provider);
        philosopherFinal(provider);
    }

    // ////////////////////////////////////
    // ***** optics and runes *****//
    // ////////////////////////////////////

    /** 镀银玻璃透镜：HV 组装机（玻璃 + 银箔 + 世界盐粉 + 灵气）。 */
    private static void silveredGlassLens(Consumer<FinishedRecipe> provider) {
        ItemStack lens = SafeItems.of(PollutionItems.SILVERED_GLASS_LENS, 2);
        FluidStack aura = fluid(PollutionMaterials.InfusedAura, 250);
        if (lens.isEmpty() || aura.isEmpty()) {
            warn("silvered_glass_lens");
            return;
        }
        GTRecipeBuilder.of(id("silvered_glass_lens"), GTRecipeTypes.ASSEMBLER_RECIPES)
                .inputItems(new ItemStack(Items.GLASS, 2))
                .inputItems(ChemicalHelper.get(TagPrefix.foil, GTMaterials.Silver, 4))
                .inputItems(dust(PollutionMaterials.Salisundus, 1))
                .inputFluids(aura)
                .outputItems(lens)
                .duration(200)
                .EUt(GTValues.VA[GTValues.HV])
                .save(provider);
    }

    /** 精密符文坯：EV 组装机（无菌石板 + HSSG 板 + 世界盐粉 + 灵气），产出 2 个。 */
    private static void precisionRuneBlank(Consumer<FinishedRecipe> provider) {
        ItemStack output = SafeItems.of(PollutionItems.PRECISION_RUNE_BLANK, 2);
        ItemStack slate = SafeItems.of(PollutionItems.STERILE_SLATE_BLANK);
        FluidStack aura = fluid(PollutionMaterials.InfusedAura, 500);
        if (output.isEmpty() || slate.isEmpty() || aura.isEmpty()) {
            warn("precision_rune_blank");
            return;
        }
        GTRecipeBuilder.of(id("precision_rune_blank"), GTRecipeTypes.ASSEMBLER_RECIPES)
                .inputItems(slate)
                .inputItems(ChemicalHelper.get(TagPrefix.plate, GTMaterials.HSSG, 2))
                .inputItems(dust(PollutionMaterials.Salisundus, 2))
                .inputFluids(aura)
                .outputItems(output)
                .duration(300)
                .EUt(GTValues.VA[GTValues.EV])
                .save(provider);
    }

    // ////////////////////////////////////
    // ***** air filter cartridges I..V *****//
    // ////////////////////////////////////

    /**
     * 空气过滤器滤芯 I..V。每级使用自己的材料（InfusedEarth、InfusedWater、
     * Syrmorite、Thaumium、Octine），上一级滤芯作为升级基座；III 级及以上走
     * 魔导组装机以匹配魔法电池注魔链。
     */
    private static void filters(Consumer<FinishedRecipe> provider) {
        ItemStack filter1 = SafeItems.of(PollutionItems.FILTER_I);
        ItemStack filter2 = SafeItems.of(PollutionItems.FILTER_II);
        ItemStack filter3 = SafeItems.of(PollutionItems.FILTER_III);
        ItemStack filter4 = SafeItems.of(PollutionItems.FILTER_IV);
        ItemStack filter5 = SafeItems.of(PollutionItems.FILTER_V);

        // I: LV 组装机
        if (!filter1.isEmpty()) {
            FluidStack earth = fluid(PollutionMaterials.InfusedEarth, 250);
            if (!earth.isEmpty()) {
                GTRecipeBuilder.of(id("filter/i"), GTRecipeTypes.ASSEMBLER_RECIPES)
                        .inputItems(Items.PAPER, 4)
                        .inputItems(dust(PollutionMaterials.InfusedEarth, 4))
                        .inputItems(dust(GTMaterials.Charcoal, 2))
                        .inputFluids(earth)
                        .outputItems(filter1)
                        .duration(100)
                        .EUt(GTValues.VA[GTValues.LV])
                        .save(provider);
            } else {
                warn("filter/i");
            }
        }

        // II: MV 组装机
        if (!filter1.isEmpty() && !filter2.isEmpty()) {
            FluidStack water = fluid(PollutionMaterials.InfusedWater, 500);
            if (!water.isEmpty()) {
                GTRecipeBuilder.of(id("filter/ii"), GTRecipeTypes.ASSEMBLER_RECIPES)
                        .inputItems(filter1)
                        .inputItems(dust(PollutionMaterials.InfusedWater, 4))
                        .inputItems(ChemicalHelper.get(TagPrefix.plate, GTMaterials.Aluminium, 2))
                        .inputFluids(water)
                        .outputItems(filter2)
                        .duration(150)
                        .EUt(GTValues.VA[GTValues.MV])
                        .save(provider);
            } else {
                warn("filter/ii");
            }
        }

        // III: HV 魔导组装机
        if (!filter2.isEmpty() && !filter3.isEmpty()) {
            FluidStack aura = fluid(PollutionMaterials.InfusedAura, 500);
            FluidStack water = fluid(PollutionMaterials.InfusedWater, 250);
            if (!aura.isEmpty() && !water.isEmpty()) {
                GTRecipeBuilder.of(id("filter/iii"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                        .inputItems(filter2)
                        .inputItems(dust(PollutionMaterials.Syrmorite, 4))
                        .inputItems(ChemicalHelper.get(TagPrefix.plate, GTMaterials.HSSG, 2))
                        .inputFluids(aura)
                        .inputFluids(water)
                        .outputItems(filter3)
                        .duration(200)
                        .EUt(GTValues.VA[GTValues.HV])
                        .save(provider);
            } else {
                warn("filter/iii");
            }
        }

        // IV: EV 魔导组装机（上游 thaumium 材料 -> TC4R 神秘锭）
        ItemStack thaumium = SafeItems.byId("thaumcraft", "thaumium_ingot", 2);
        if (!filter3.isEmpty() && !filter4.isEmpty() && !thaumium.isEmpty()) {
            FluidStack aura = fluid(PollutionMaterials.InfusedAura, 1000);
            FluidStack magic = fluid(PollutionMaterials.InfusedMagic, 500);
            if (!aura.isEmpty() && !magic.isEmpty()) {
                GTRecipeBuilder.of(id("filter/iv"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                        .inputItems(filter3)
                        .inputItems(thaumium)
                        .inputItems(ChemicalHelper.get(TagPrefix.plate, GTMaterials.StainlessSteel, 2))
                        .inputFluids(aura)
                        .inputFluids(magic)
                        .outputItems(filter4)
                        .duration(300)
                        .EUt(GTValues.VA[GTValues.EV])
                        .save(provider);
            } else {
                warn("filter/iv");
            }
        }

        // V: IV 魔导组装机
        if (!filter4.isEmpty() && !filter5.isEmpty()) {
            FluidStack aura = fluid(PollutionMaterials.InfusedAura, 2000);
            FluidStack taint = fluid(PollutionMaterials.InfusedTaint, 1000);
            FluidStack magic = fluid(PollutionMaterials.InfusedMagic, 1000);
            if (!aura.isEmpty() && !taint.isEmpty() && !magic.isEmpty()) {
                GTRecipeBuilder.of(id("filter/v"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                        .inputItems(filter4)
                        .inputItems(dust(PollutionMaterials.Octine, 4))
                        .inputItems(ChemicalHelper.get(TagPrefix.plate, GTMaterials.TungstenSteel, 2))
                        .inputFluids(aura)
                        .inputFluids(taint)
                        .inputFluids(magic)
                        .outputItems(filter5)
                        .duration(400)
                        .EUt(GTValues.VA[GTValues.IV])
                        .save(provider);
            } else {
                warn("filter/v");
            }
        }
    }

    // ////////////////////////////////////
    // ***** starlight crystal chain *****//
    // ////////////////////////////////////

    /** 岩石水晶晶种 -> 天体晶体培养胚 -> 培育水晶 -> 调谐晶体晶圆 -> 星座数据晶圆 -> 天体校准核心。 */
    private static void crystalChain(Consumer<FinishedRecipe> provider) {
        ItemStack seed = SafeItems.of(PollutionItems.ROCK_CRYSTAL_SEED);
        ItemStack embryo = SafeItems.of(PollutionItems.CELESTIAL_CRYSTAL_EMBRYO);
        ItemStack cultivated = SafeItems.of(PollutionItems.CULTIVATED_CRYSTAL);
        ItemStack wafer = SafeItems.of(PollutionItems.ATTUNED_CRYSTAL_WAFER);
        ItemStack dataWafer = SafeItems.of(PollutionItems.CONSTELLATION_DATA_WAFER);
        ItemStack calibration = SafeItems.of(PollutionItems.CELESTIAL_CALIBRATION_CORE);
        ItemStack starryRune = SafeItems.of(PollutionItems.STARRY_RUNE);
        ItemStack lens = SafeItems.of(PollutionItems.SILVERED_GLASS_LENS, 2);

        // 晶种：HV 魔导组装机
        if (!seed.isEmpty()) {
            FluidStack crystal = fluid(PollutionMaterials.InfusedCrystal, 288);
            FluidStack aura = fluid(PollutionMaterials.InfusedAura, 288);
            if (!crystal.isEmpty() && !aura.isEmpty()) {
                GTRecipeBuilder.of(id("crystal/rock_crystal_seed"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                        .inputItems(ChemicalHelper.get(TagPrefix.gem, GTMaterials.Amethyst, 2))
                        .inputItems(ChemicalHelper.get(TagPrefix.gem, GTMaterials.CertusQuartz, 2))
                        .inputItems(dust(PollutionMaterials.Salisundus, 2))
                        .inputFluids(crystal)
                        .inputFluids(aura)
                        .outputItems(seed)
                        .duration(200)
                        .EUt(GTValues.VA[GTValues.HV])
                        .save(provider);
            } else {
                warn("crystal/rock_crystal_seed");
            }
        }

        // 培养胚：EV 魔导组装机
        if (!seed.isEmpty() && !embryo.isEmpty()) {
            FluidStack light = fluid(PollutionMaterials.InfusedLight, 1000);
            FluidStack life = fluid(PollutionMaterials.InfusedLife, 500);
            FluidStack aura = fluid(PollutionMaterials.InfusedAura, 500);
            if (!light.isEmpty() && !life.isEmpty() && !aura.isEmpty()) {
                GTRecipeBuilder.of(id("crystal/celestial_crystal_embryo"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                        .inputItems(seed)
                        .inputItems(crystal("order", 2))
                        .inputFluids(light)
                        .inputFluids(life)
                        .inputFluids(aura)
                        .outputItems(embryo)
                        .duration(600)
                        .EUt(GTValues.VA[GTValues.EV])
                        .save(provider);
            } else {
                warn("crystal/celestial_crystal_embryo");
            }
        }

        // 培育水晶：IV 魔导组装机
        if (!embryo.isEmpty() && !cultivated.isEmpty() && !starryRune.isEmpty()) {
            FluidStack crystal = fluid(PollutionMaterials.InfusedCrystal, 1000);
            FluidStack aura = fluid(PollutionMaterials.InfusedAura, 1000);
            FluidStack light = fluid(PollutionMaterials.InfusedLight, 500);
            if (!crystal.isEmpty() && !aura.isEmpty() && !light.isEmpty()) {
                GTRecipeBuilder.of(id("crystal/cultivated_crystal"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                        .inputItems(embryo)
                        .inputItems(crystal("order", 4))
                        .inputItems(ChemicalHelper.get(TagPrefix.gem, GTMaterials.Opal, 2))
                        .inputItems(starryRune)
                        .inputFluids(crystal)
                        .inputFluids(aura)
                        .inputFluids(light)
                        .outputItems(cultivated)
                        .duration(800)
                        .EUt(GTValues.VA[GTValues.IV])
                        .save(provider);
            } else {
                warn("crystal/cultivated_crystal");
            }
        }

        // 调谐晶圆：IV 魔导组装机
        if (!cultivated.isEmpty() && !wafer.isEmpty() && !lens.isEmpty()) {
            FluidStack crystal = fluid(PollutionMaterials.InfusedCrystal, 1000);
            FluidStack light = fluid(PollutionMaterials.InfusedLight, 1000);
            FluidStack aura = fluid(PollutionMaterials.InfusedAura, 1000);
            if (!crystal.isEmpty() && !light.isEmpty() && !aura.isEmpty()) {
                GTRecipeBuilder.of(id("crystal/attuned_crystal_wafer"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                        .inputItems(cultivated)
                        .inputItems(SafeItems.of(PollutionItems.MAGIC_CIRCUIT_BOARD_IV))
                        .inputItems(lens)
                        .inputFluids(crystal)
                        .inputFluids(light)
                        .inputFluids(aura)
                        .outputItems(wafer)
                        .duration(1000)
                        .EUt(GTValues.VA[GTValues.IV])
                        .save(provider);
            } else {
                warn("crystal/attuned_crystal_wafer");
            }
        }

        // 星座数据晶圆：ZPM 注魔
        if (!wafer.isEmpty() && !dataWafer.isEmpty() && !starryRune.isEmpty() && !embryo.isEmpty()
                && !lens.isEmpty()) {
            infusion(provider, "constellation_data_wafer", dataWafer, 8, wafer,
                    aspects("ordo", 128, "cognitio", 64, "praecantatio", 64, "auram", 32, "lux", 64),
                    ing(starryRune),
                    ing(crystal("order", 2)),
                    ing(SafeItems.byId("thaumcraft", "vis_charge_relay", 1)),
                    ing(SafeItems.gt("zpm_sensor", 1)),
                    ing(SafeItems.gt("zpm_emitter", 1)),
                    ing(SafeItems.byId("thaumcraft", "primordial_pearl", 1)),
                    ing(embryo),
                    ing(lens));
        }

        // 天体校准核心：LuV 注魔
        if (!dataWafer.isEmpty() && !calibration.isEmpty() && !starryRune.isEmpty()
                && !cultivated.isEmpty() && !lens.isEmpty()) {
            infusion(provider, "celestial_calibration_core", calibration, 8, dataWafer,
                    aspects("ordo", 250, "lux", 128, "praecantatio", 128, "auram", 64, "cognitio", 64),
                    ing(starryRune.copyWithCount(2)),
                    ing(cultivated),
                    ing(lens),
                    ing(SafeItems.byId("thaumcraft", "vis_charge_relay", 1)),
                    ing(SafeItems.gt("luv_field_generator", 1)),
                    ing(SafeItems.of(PollutionItems.WHITE_RUNE)),
                    ing(SafeItems.byId("thaumcraft", "eldritch_object", 1)));
        }

        // 原初星血晶体：UV 注魔（原初珍珠核心）
        ItemStack bloodCrystal = SafeItems.of(PollutionItems.PRIMORDIAL_STAR_BLOOD_CRYSTAL);
        ItemStack causality = SafeItems.of(PollutionItems.CAUSALITY_CATALYST);
        ItemStack pearl = SafeItems.byId("thaumcraft", "primordial_pearl", 1);
        if (!bloodCrystal.isEmpty() && !causality.isEmpty() && !pearl.isEmpty()
                && !cultivated.isEmpty() && !starryRune.isEmpty()) {
            infusion(provider, "primordial_star_blood_crystal", bloodCrystal, 10, pearl,
                    aspects("spiritus", 250, "victus", 250, "lux", 128, "praecantatio", 128, "auram", 64),
                    ing(cultivated),
                    ing(causality),
                    ing(starryRune.copyWithCount(2)),
                    ing(SafeItems.of(PollutionItems.WHITE_RUNE)),
                    ing(SafeItems.of(PollutionItems.BLACK_RUNE)),
                    ing(SafeItems.byId("thaumcraft", "eldritch_object", 1)),
                    ing(SafeItems.gt("uv_field_generator", 1)),
                    ing(crystal("order", 2)));
        }
    }

    // ////////////////////////////////////
    // ***** magic cores and components *****//
    // ////////////////////////////////////

    /** 耗竭魔导核心（含离心回收）-> 因果催化剂 -> 谐律符文核心 -> UHV 针/除颤仪。 */
    private static void magicCoreChain(Consumer<FinishedRecipe> provider) {
        ItemStack depleted = SafeItems.of(PollutionItems.DEPLETED_MAGIC_CORE);
        ItemStack causality = SafeItems.of(PollutionItems.CAUSALITY_CATALYST);
        ItemStack harmonizing = SafeItems.of(PollutionItems.HARMONIZING_RUNE_CORE);
        ItemStack needle = SafeItems.of(PollutionItems.get("needle_of_mystic_interpellation"));
        ItemStack defibrillator = SafeItems.of(PollutionItems.get("cogito_defibrillator"));
        ItemStack blankCore = SafeItems.of(PollutionItems.BLANK_CATALYST_CORE);
        ItemStack runeBlank = SafeItems.of(PollutionItems.PRECISION_RUNE_BLANK);
        ItemStack wafer = SafeItems.of(PollutionItems.ATTUNED_CRYSTAL_WAFER);
        ItemStack starryRune = SafeItems.of(PollutionItems.STARRY_RUNE);
        ItemStack relay = SafeItems.byId("thaumcraft", "vis_charge_relay", 1);

        // 耗竭核心：EV 魔导组装机
        if (!depleted.isEmpty() && !blankCore.isEmpty() && !relay.isEmpty()) {
            FluidStack voidFluid = fluid(PollutionMaterials.InfusedVoid, 500);
            FluidStack aura = fluid(PollutionMaterials.InfusedAura, 500);
            if (!voidFluid.isEmpty() && !aura.isEmpty()) {
                GTRecipeBuilder.of(id("core/depleted_magic_core"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                        .inputItems(blankCore)
                        .inputItems(relay)
                        .inputItems(ChemicalHelper.get(TagPrefix.gem, PollutionMaterials.InfusedVoid, 4))
                        .inputFluids(voidFluid)
                        .inputFluids(aura)
                        .outputItems(depleted)
                        .duration(400)
                        .EUt(GTValues.VA[GTValues.EV])
                        .save(provider);
            } else {
                warn("core/depleted_magic_core");
            }
        }

        // 离心回收：耗竭核心 -> 空白催化核心（上游 tooltip：可离心回收）
        if (!depleted.isEmpty() && !blankCore.isEmpty()) {
            GTRecipeBuilder.of(id("core/depleted_recycling"), GTRecipeTypes.CENTRIFUGE_RECIPES)
                    .inputItems(depleted)
                    .outputItems(blankCore)
                    .duration(200)
                    .EUt(GTValues.VA[GTValues.EV])
                    .save(provider);
        }

        // 因果催化剂：IV 注魔
        ItemStack eldritch = SafeItems.byId("thaumcraft", "eldritch_object", 1);
        ItemStack pearl = SafeItems.byId("thaumcraft", "primordial_pearl", 1);
        if (!depleted.isEmpty() && !causality.isEmpty() && !wafer.isEmpty()
                && !eldritch.isEmpty() && !pearl.isEmpty()) {
            infusion(provider, "causality_catalyst", causality, 8, depleted,
                    aspects("cognitio", 128, "spiritus", 64, "praecantatio", 64, "permutatio", 64),
                    ing(eldritch),
                    ing(pearl),
                    ing(crystal("entropy")),
                    ing(crystal("order")),
                    ing(wafer),
                    ing(starryRune),
                    ing(SafeItems.gt("iv_field_generator", 1)));
        }

        // 谐律符文核心：LuV 注魔
        ItemStack manaPearl = SafeItems.byId("botania", "mana_pearl", 1);
        ItemStack terrasteel = SafeItems.byId("botania", "terrasteel_ingot", 1);
        ItemStack naturalCoil = SafeItems.of(PollutionItems.NATURAL_INFUSED_COIL);
        if (!runeBlank.isEmpty() && !harmonizing.isEmpty() && !wafer.isEmpty()
                && !manaPearl.isEmpty() && !terrasteel.isEmpty() && !naturalCoil.isEmpty()
                && !relay.isEmpty() && !starryRune.isEmpty()) {
            infusion(provider, "harmonizing_rune_core", harmonizing, 8, runeBlank,
                    aspects("ordo", 250, "praecantatio", 128, "auram", 64, "fabrico", 64, "potentia", 64),
                    ing(starryRune),
                    ing(crystal("order")),
                    ing(wafer),
                    ing(manaPearl),
                    ing(terrasteel),
                    ing(relay),
                    ing(naturalCoil),
                    ing(SafeItems.gt("luv_field_generator", 1)));
        }

        // 密契询唤针：UHV 魔导组装机
        if (!needle.isEmpty() && !wafer.isEmpty()) {
            ItemStack lens = SafeItems.of(PollutionItems.SILVERED_GLASS_LENS, 2);
            FluidStack thought = fluid(PollutionMaterials.InfusedThought, 1000);
            FluidStack aura = fluid(PollutionMaterials.InfusedAura, 1000);
            FluidStack light = fluid(PollutionMaterials.InfusedLight, 500);
            if (!lens.isEmpty() && !thought.isEmpty() && !aura.isEmpty() && !light.isEmpty()) {
                GTRecipeBuilder.of(id("component/needle_of_mystic_interpellation"),
                                PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                        .inputItems(lens)
                        .inputItems(wafer)
                        .inputItems(SafeItems.of(PollutionItems.MAGIC_CIRCUIT_BOARD_UHV))
                        .inputItems(starryRune.copyWithCount(2))
                        .inputFluids(thought)
                        .inputFluids(aura)
                        .inputFluids(light)
                        .outputItems(needle)
                        .duration(1600)
                        .EUt(GTValues.VA[GTValues.UHV])
                        .save(provider);
            } else {
                warn("component/needle_of_mystic_interpellation");
            }
        }

        // 我思除颤仪：UHV 魔导组装机
        if (!defibrillator.isEmpty() && !causality.isEmpty() && !needle.isEmpty() && !depleted.isEmpty()) {
            FluidStack life = fluid(PollutionMaterials.InfusedLife, 1000);
            FluidStack energy = fluid(PollutionMaterials.InfusedEnergy, 1000);
            FluidStack aura = fluid(PollutionMaterials.InfusedAura, 1000);
            if (!life.isEmpty() && !energy.isEmpty() && !aura.isEmpty()) {
                GTRecipeBuilder.of(id("component/cogito_defibrillator"),
                                PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                        .inputItems(causality)
                        .inputItems(depleted.copyWithCount(2))
                        .inputItems(needle)
                        .inputItems(SafeItems.of(PollutionItems.MAGIC_CIRCUIT_BOARD_UHV))
                        .inputFluids(life)
                        .inputFluids(energy)
                        .inputFluids(aura)
                        .outputItems(defibrillator)
                        .duration(1600)
                        .EUt(GTValues.VA[GTValues.UHV])
                        .save(provider);
            } else {
                warn("component/cogito_defibrillator");
            }
        }
    }

    // ////////////////////////////////////
    // ***** integration carriers *****//
    // ////////////////////////////////////

    /**
     * 碧水指环与活体魔法生物膜：两者已是既有配方的原料（Source Charge 的中央
     * 物品、LuV 电路板的培养介质），补上各自的产出。
     */
    private static void integrationCarriers(Consumer<FinishedRecipe> provider) {
        // 碧水指环：MV 组装机（金环 + 魔力珍珠 + 注魔水要素）
        ItemStack ring = SafeItems.of(PollutionItems.BAUBLES_WATER_RING);
        ItemStack goldRing = ChemicalHelper.get(TagPrefix.ring, GTMaterials.Gold, 1);
        ItemStack manaPearl = SafeItems.byId("botania", "mana_pearl", 1);
        FluidStack water = fluid(PollutionMaterials.InfusedWater, 1000);
        if (!ring.isEmpty() && !goldRing.isEmpty() && !manaPearl.isEmpty() && !water.isEmpty()) {
            GTRecipeBuilder.of(id("carrier/water_ring"), GTRecipeTypes.ASSEMBLER_RECIPES)
                    .inputItems(goldRing)
                    .inputItems(manaPearl)
                    .inputItems(dust(PollutionMaterials.InfusedWater, 4))
                    .inputFluids(water)
                    .outputItems(ring)
                    .duration(300)
                    .EUt(GTValues.VA[GTValues.MV])
                    .save(provider);
        } else {
            warn("carrier/water_ring");
        }

        // 活体魔法生物膜：IV 魔导组装机（血肉培养 + 无菌培养基 + 生命/魔力要素）
        ItemStack biofilm = SafeItems.of(PollutionItems.LIVING_MAGIC_BIOFILM, 2);
        ItemStack meat = SafeItems.of(PollutionItems.BLOOD_PRIMITIVE_MEAT);
        ItemStack ratBrain = SafeItems.of(PollutionItems.BLOOD_RATS_BRAIN);
        FluidStack life = fluid(PollutionMaterials.InfusedLife, 1000);
        FluidStack magic = fluid(PollutionMaterials.InfusedMagic, 500);
        FluidStack growthMedium = fluid(GTMaterials.SterileGrowthMedium, 1000);
        if (!biofilm.isEmpty() && !meat.isEmpty() && !ratBrain.isEmpty()
                && !life.isEmpty() && !magic.isEmpty() && !growthMedium.isEmpty()) {
            GTRecipeBuilder.of(id("carrier/living_magic_biofilm"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                    .inputItems(meat.copyWithCount(2))
                    .inputItems(ratBrain)
                    .inputFluids(life)
                    .inputFluids(magic)
                    .inputFluids(growthMedium)
                    .outputItems(biofilm)
                    .duration(600)
                    .EUt(GTValues.VA[GTValues.IV])
                    .save(provider);
        } else {
            warn("carrier/living_magic_biofilm");
        }
    }

    // ////////////////////////////////////
    // ***** biology chain *****//
    // ////////////////////////////////////

    /**
     * 血肉培养链（仅血液电路线被排除，培养材料照常补全）：原始血肉团 -> 鼠脑
     * -> 线粒能源块/内啡肽稳定器/自噬稳定器 -> iPS 重建人脑。使用 GT 的
     * Biomass/SterileGrowthMedium 流体与要素流体。
     */
    private static void biologyChain(Consumer<FinishedRecipe> provider) {
        ItemStack meat = SafeItems.of(PollutionItems.BLOOD_PRIMITIVE_MEAT);
        ItemStack ratBrain = SafeItems.of(PollutionItems.BLOOD_RATS_BRAIN);
        ItemStack mitochondria = SafeItems.of(PollutionItems.BLOOD_MITOCHONDRION_POWER);
        ItemStack endorphins = SafeItems.of(PollutionItems.BLOOD_ENDORPHINS_STABILIZER);
        ItemStack lysosome = SafeItems.of(PollutionItems.BLOOD_LYSOSOME_STABILIZER);
        ItemStack freezeCooler = SafeItems.of(PollutionItems.BLOOD_FREEZE_COOLER);
        ItemStack ipsBrain = SafeItems.of(PollutionItems.BLOOD_IPS_HUMAN_BRAIN);
        ItemStack zombieBrain = SafeItems.byId("thaumcraft", "zombie_brain", 1);

        // 原始血肉团：LV 化学反应釜（腐肉 + 生物质）
        FluidStack biomass = fluid(GTMaterials.Biomass, 500);
        if (!meat.isEmpty() && !biomass.isEmpty()) {
            GTRecipeBuilder.of(id("biology/primitive_meat"), GTRecipeTypes.CHEMICAL_RECIPES)
                    .inputItems(Items.ROTTEN_FLESH, 4)
                    .inputFluids(biomass)
                    .outputItems(meat.copyWithCount(2))
                    .duration(100)
                    .EUt(GTValues.VA[GTValues.LV])
                    .save(provider);
        } else {
            warn("biology/primitive_meat");
        }

        // 鼠脑：MV 化学反应釜（TC4R 僵尸脑 + 原始血肉 + 无菌培养基）
        FluidStack growthMedium = fluid(GTMaterials.SterileGrowthMedium, 250);
        if (!ratBrain.isEmpty() && !meat.isEmpty() && !zombieBrain.isEmpty() && !growthMedium.isEmpty()) {
            GTRecipeBuilder.of(id("biology/rat_brain"), GTRecipeTypes.CHEMICAL_RECIPES)
                    .inputItems(zombieBrain)
                    .inputItems(meat)
                    .inputFluids(growthMedium)
                    .outputItems(ratBrain)
                    .duration(200)
                    .EUt(GTValues.VA[GTValues.MV])
                    .save(provider);
        } else {
            warn("biology/rat_brain");
        }

        // 线粒能源块：MV 化学反应釜
        if (!mitochondria.isEmpty() && !meat.isEmpty()) {
            FluidStack energy = fluid(PollutionMaterials.InfusedEnergy, 288);
            if (!energy.isEmpty()) {
                GTRecipeBuilder.of(id("biology/mitochondrion_power"), GTRecipeTypes.CHEMICAL_RECIPES)
                        .inputItems(meat.copyWithCount(2))
                        .inputItems(Items.SUGAR, 4)
                        .inputFluids(energy)
                        .outputItems(mitochondria)
                        .duration(200)
                        .EUt(GTValues.VA[GTValues.MV])
                        .save(provider);
            } else {
                warn("biology/mitochondrion_power");
            }
        }

        // 内啡肽稳定器：HV 化学反应釜
        if (!endorphins.isEmpty() && !ratBrain.isEmpty()) {
            FluidStack life = fluid(PollutionMaterials.InfusedLife, 288);
            if (!life.isEmpty()) {
                GTRecipeBuilder.of(id("biology/endorphins_stabilizer"), GTRecipeTypes.CHEMICAL_RECIPES)
                        .inputItems(ratBrain)
                        .inputItems(dust(GTMaterials.Salt, 2))
                        .inputFluids(life)
                        .outputItems(endorphins)
                        .duration(200)
                        .EUt(GTValues.VA[GTValues.HV])
                        .save(provider);
            } else {
                warn("biology/endorphins_stabilizer");
            }
        }

        // 自噬稳定器：HV 化学反应釜
        if (!lysosome.isEmpty() && !ratBrain.isEmpty()) {
            FluidStack growthMedium500 = fluid(GTMaterials.SterileGrowthMedium, 500);
            if (!growthMedium500.isEmpty()) {
                GTRecipeBuilder.of(id("biology/lysosome_stabilizer"), GTRecipeTypes.CHEMICAL_RECIPES)
                        .inputItems(ratBrain)
                        .inputItems(dust(PollutionMaterials.Thaummix, 2))
                        .inputFluids(growthMedium500)
                        .outputItems(lysosome)
                        .duration(200)
                        .EUt(GTValues.VA[GTValues.HV])
                        .save(provider);
            } else {
                warn("biology/lysosome_stabilizer");
            }
        }

        // 寒冰冷冻器：MV 化学浸洗
        if (!freezeCooler.isEmpty()) {
            FluidStack cold = fluid(PollutionMaterials.InfusedCold, 576);
            if (!cold.isEmpty()) {
                GTRecipeBuilder.of(id("biology/freeze_cooler"), GTRecipeTypes.CHEMICAL_BATH_RECIPES)
                        .inputItems(Items.ICE, 4)
                        .inputFluids(cold)
                        .outputItems(freezeCooler.copyWithCount(2))
                        .duration(200)
                        .EUt(GTValues.VA[GTValues.MV])
                        .save(provider);
            } else {
                warn("biology/freeze_cooler");
            }
        }

        // iPS 重建人脑：EV 魔导组装机（TC4R 脑缸）
        ItemStack brainJar = SafeItems.byId("thaumcraft", "brain_jar", 1);
        if (!ipsBrain.isEmpty() && !ratBrain.isEmpty() && !brainJar.isEmpty()) {
            FluidStack human = fluid(PollutionMaterials.InfusedHuman, 500);
            FluidStack life = fluid(PollutionMaterials.InfusedLife, 1000);
            FluidStack growthMedium1000 = fluid(GTMaterials.SterileGrowthMedium, 1000);
            if (!human.isEmpty() && !life.isEmpty() && !growthMedium1000.isEmpty()) {
                GTRecipeBuilder.of(id("biology/ips_human_brain"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                        .inputItems(ratBrain.copyWithCount(2))
                        .inputItems(brainJar)
                        .inputFluids(human)
                        .inputFluids(life)
                        .inputFluids(growthMedium1000)
                        .outputItems(ipsBrain)
                        .duration(800)
                        .EUt(GTValues.VA[GTValues.EV])
                        .save(provider);
            } else {
                warn("biology/ips_human_brain");
            }
        }
    }

    // ////////////////////////////////////
    // ***** utilities *****//
    // ////////////////////////////////////

    /** 污染检测器、封装灵气节点、焦油史莱姆、魔法扫帚、理智药与杀虫剂。 */
    private static void utilities(Consumer<FinishedRecipe> provider) {
        // 污染检测器：LV 组装机
        ItemStack visChecker = SafeItems.of(PollutionItems.VIS_CHECKER);
        ItemStack thaumometer = SafeItems.byId("thaumcraft", "thaumometer", 1);
        if (!visChecker.isEmpty() && !thaumometer.isEmpty()) {
            FluidStack aura = fluid(PollutionMaterials.InfusedAura, 250);
            if (!aura.isEmpty()) {
                GTRecipeBuilder.of(id("utility/vis_checker"), GTRecipeTypes.ASSEMBLER_RECIPES)
                        .inputItems(thaumometer)
                        .inputItems(SafeItems.of(PollutionItems.MAGIC_CIRCUIT_LV))
                        .inputItems(dust(PollutionMaterials.Salisundus, 1))
                        .inputItems(Items.GLASS, 2)
                        .inputFluids(aura)
                        .outputItems(visChecker)
                        .duration(200)
                        .EUt(GTValues.VA[GTValues.LV])
                        .save(provider);
            } else {
                warn("utility/vis_checker");
            }
        }

        // 封装灵气节点：HV 注魔（TC4R 罐中节点）
        ItemStack auraNode = SafeItems.of(PollutionItems.PACKAGED_AURA_NODE);
        ItemStack nodeJar = SafeItems.byId("thaumcraft", "node_jar_item", 1);
        ItemStack nodeTransducer = SafeItems.byId("thaumcraft", "node_transducer", 1);
        ItemStack relay = SafeItems.byId("thaumcraft", "vis_charge_relay", 1);
        if (!auraNode.isEmpty() && !nodeJar.isEmpty() && !nodeTransducer.isEmpty() && !relay.isEmpty()) {
            infusion(provider, "packaged_aura_node", auraNode, 6, nodeJar,
                    aspects("auram", 64, "praecantatio", 32, "ordo", 32, "motus", 16),
                    ing(nodeTransducer),
                    ing(relay),
                    ing(SafeItems.byId("thaumcraft", "resonator", 1)),
                    ing(SafeItems.byId("thaumcraft", "balanced_shard", 2)),
                    ing(SafeItems.byId("thaumcraft", "air_shard", 1)),
                    ing(SafeItems.byId("thaumcraft", "fire_shard", 1)),
                    ing(SafeItems.byId("thaumcraft", "warded_jar", 1)),
                    ing(SafeItems.gt("hv_field_generator", 1)));
        }

        // 焦油史莱姆：LV 化学反应釜（黏液球 + 纯焦油）
        ItemStack tarSlime = SafeItems.of(PollutionItems.TAR_SLIME);
        if (!tarSlime.isEmpty()) {
            FluidStack tar = fluid(PollutionMaterials.PureTar, 500);
            if (!tar.isEmpty()) {
                GTRecipeBuilder.of(id("utility/tar_slime"), GTRecipeTypes.CHEMICAL_RECIPES)
                        .inputItems(Items.SLIME_BALL)
                        .inputFluids(tar)
                        .outputItems(tarSlime.copyWithCount(2))
                        .duration(100)
                        .EUt(GTValues.VA[GTValues.LV])
                        .save(provider);
            } else {
                warn("utility/tar_slime");
            }
        }

        // 魔法扫帚：HV 组装机
        ItemStack sweep = SafeItems.of(PollutionItems.MAGIC_SWEEP);
        ItemStack manaPowder = SafeItems.byId("botania", "mana_powder", 4);
        if (!sweep.isEmpty() && !manaPowder.isEmpty()) {
            FluidStack aura = fluid(PollutionMaterials.InfusedAura, 1000);
            if (!aura.isEmpty()) {
                GTRecipeBuilder.of(id("utility/magic_sweep"), GTRecipeTypes.ASSEMBLER_RECIPES)
                        .inputItems(Items.STICK, 2)
                        .inputItems(Items.HAY_BLOCK, 1)
                        .inputItems(manaPowder)
                        .inputFluids(aura)
                        .outputItems(sweep)
                        .duration(200)
                        .EUt(GTValues.VA[GTValues.HV])
                        .save(provider);
            } else {
                warn("utility/magic_sweep");
            }
        }

        devayPills(provider);
        pesticide(provider);
    }

    /**
     * 理智回复药链（对应 TC4R 的净化浴盐/消毒肥皂）：
     * 空药 -&gt; 1 -&gt; 5 -&gt; 10 -&gt; 20。
     */
    private static void devayPills(Consumer<FinishedRecipe> provider) {
        ItemStack empty = SafeItems.of(PollutionItems.DEVAY_PILL_EMPTY);
        ItemStack pill1 = SafeItems.of(PollutionItems.DEVAY_PILL_1);
        ItemStack pill5 = SafeItems.of(PollutionItems.DEVAY_PILL_5);
        ItemStack pill10 = SafeItems.of(PollutionItems.DEVAY_PILL_10);
        ItemStack pill20 = SafeItems.of(PollutionItems.DEVAY_PILL_20);
        ItemStack bathSalts = SafeItems.byId("thaumcraft", "bath_salts", 1);
        ItemStack sanitySoap = SafeItems.byId("thaumcraft", "sanity_soap", 1);

        // 空药：LV 组装机
        if (!empty.isEmpty()) {
            FluidStack life = fluid(PollutionMaterials.InfusedLife, 144);
            if (!life.isEmpty()) {
                GTRecipeBuilder.of(id("devay/empty"), GTRecipeTypes.ASSEMBLER_RECIPES)
                        .inputItems(Items.PAPER, 2)
                        .inputItems(Items.SUGAR, 2)
                        .inputFluids(life)
                        .outputItems(empty.copyWithCount(2))
                        .duration(100)
                        .EUt(GTValues.VA[GTValues.LV])
                        .save(provider);
            } else {
                warn("devay/empty");
            }
        }

        // 一级：MV 化学反应釜
        if (!empty.isEmpty() && !pill1.isEmpty() && !bathSalts.isEmpty()) {
            FluidStack life = fluid(PollutionMaterials.InfusedLife, 288);
            if (!life.isEmpty()) {
                GTRecipeBuilder.of(id("devay/pill_1"), GTRecipeTypes.CHEMICAL_RECIPES)
                        .inputItems(empty)
                        .inputItems(bathSalts)
                        .inputFluids(life)
                        .outputItems(pill1)
                        .duration(200)
                        .EUt(GTValues.VA[GTValues.MV])
                        .save(provider);
            } else {
                warn("devay/pill_1");
            }
        }

        // 五级：MV 化学反应釜
        if (!pill1.isEmpty() && !pill5.isEmpty() && !bathSalts.isEmpty()) {
            FluidStack alchemy = fluid(PollutionMaterials.InfusedAlchemy, 288);
            if (!alchemy.isEmpty()) {
                GTRecipeBuilder.of(id("devay/pill_5"), GTRecipeTypes.CHEMICAL_RECIPES)
                        .inputItems(pill1)
                        .inputItems(bathSalts.copyWithCount(2))
                        .inputFluids(alchemy)
                        .outputItems(pill5)
                        .duration(300)
                        .EUt(GTValues.VA[GTValues.MV])
                        .save(provider);
            } else {
                warn("devay/pill_5");
            }
        }

        // 十级：HV 化学反应釜
        if (!pill5.isEmpty() && !pill10.isEmpty() && !sanitySoap.isEmpty()) {
            FluidStack alchemy = fluid(PollutionMaterials.InfusedAlchemy, 576);
            if (!alchemy.isEmpty()) {
                GTRecipeBuilder.of(id("devay/pill_10"), GTRecipeTypes.CHEMICAL_RECIPES)
                        .inputItems(pill5)
                        .inputItems(sanitySoap)
                        .inputFluids(alchemy)
                        .outputItems(pill10)
                        .duration(400)
                        .EUt(GTValues.VA[GTValues.HV])
                        .save(provider);
            } else {
                warn("devay/pill_10");
            }
        }

        // 二十级：EV 魔导组装机
        if (!pill10.isEmpty() && !pill20.isEmpty() && !sanitySoap.isEmpty() && !bathSalts.isEmpty()) {
            FluidStack alchemy = fluid(PollutionMaterials.InfusedAlchemy, 1000);
            FluidStack light = fluid(PollutionMaterials.InfusedLight, 1000);
            FluidStack aura = fluid(PollutionMaterials.InfusedAura, 1000);
            if (!alchemy.isEmpty() && !light.isEmpty() && !aura.isEmpty()) {
                GTRecipeBuilder.of(id("devay/pill_20"), PORecipeMaps.MAGIC_ASSEMBLER_RECIPES)
                        .inputItems(pill10)
                        .inputItems(sanitySoap.copyWithCount(2))
                        .inputItems(bathSalts.copyWithCount(4))
                        .inputFluids(alchemy)
                        .inputFluids(light)
                        .inputFluids(aura)
                        .outputItems(pill20)
                        .duration(600)
                        .EUt(GTValues.VA[GTValues.EV])
                        .save(provider);
            } else {
                warn("devay/pill_20");
            }
        }
    }

    /** 杀虫剂：空瓶组装，满瓶用注魔污浊流体灌装。 */
    private static void pesticide(Consumer<FinishedRecipe> provider) {
        ItemStack empty = SafeItems.of(PollutionItems.PESTICIDE_EMPTY, 4);
        ItemStack full = SafeItems.of(PollutionItems.PESTICIDE);
        if (empty.isEmpty()) {
            warn("pesticide/empty");
            return;
        }
        GTRecipeBuilder.of(id("pesticide/empty"), GTRecipeTypes.ASSEMBLER_RECIPES)
                .inputItems(Items.GLASS_BOTTLE, 4)
                .inputItems(ChemicalHelper.get(TagPrefix.plate, GTMaterials.Polyethylene, 2))
                .outputItems(empty)
                .duration(100)
                .EUt(GTValues.VA[GTValues.MV])
                .save(provider);

        if (!full.isEmpty()) {
            FluidStack taint = fluid(PollutionMaterials.InfusedTaint, 250);
            if (!taint.isEmpty()) {
                GTRecipeBuilder.of(id("pesticide/full"), GTRecipeTypes.CANNER_RECIPES)
                        .inputItems(SafeItems.of(PollutionItems.PESTICIDE_EMPTY))
                        .inputFluids(taint)
                        .outputItems(full)
                        .duration(100)
                        .EUt(GTValues.VA[GTValues.MV])
                        .save(provider);
            } else {
                warn("pesticide/full");
            }
        }
    }

    // ////////////////////////////////////
    // ***** philosopher stone capstone *****//
    // ////////////////////////////////////

    /** 真·贤者之石：UHV 注魔，汇聚因果催化剂、原初星血晶体与天体校准核心。 */
    private static void philosopherFinal(Consumer<FinishedRecipe> provider) {
        ItemStack finalStone = SafeItems.of(PollutionItems.get("stone_of_philosopher_final"));
        ItemStack stone4 = SafeItems.of(PollutionItems.STONE_OF_PHILOSOPHER_4);
        ItemStack causality = SafeItems.of(PollutionItems.CAUSALITY_CATALYST);
        ItemStack bloodCrystal = SafeItems.of(PollutionItems.PRIMORDIAL_STAR_BLOOD_CRYSTAL);
        ItemStack calibration = SafeItems.of(PollutionItems.CELESTIAL_CALIBRATION_CORE);
        ItemStack starryRune = SafeItems.of(PollutionItems.STARRY_RUNE, 4);
        ItemStack coreOfIdea = SafeItems.of(PollutionItems.get("core_of_idea"));
        if (finalStone.isEmpty() || stone4.isEmpty() || causality.isEmpty() || bloodCrystal.isEmpty()
                || calibration.isEmpty() || starryRune.isEmpty() || coreOfIdea.isEmpty()) {
            warn("philosopher_final");
            return;
        }
        infusion(provider, "stone_of_philosopher_final", finalStone, 12, stone4,
                aspects("praecantatio", 512, "permutatio", 512, "fabrico", 512,
                        "auram", 256, "potentia", 256, "lux", 256, "cognitio", 256),
                ing(causality),
                ing(bloodCrystal),
                ing(calibration),
                ing(starryRune),
                ing(coreOfIdea.copyWithCount(2)),
                ing(SafeItems.byId("thaumcraft", "eldritch_object", 2)),
                ing(SafeItems.gt("uhv_field_generator", 1)),
                ing(crystal("order", 4)));
    }

    // ////////////////////////////////////
    // ***** helpers *****//
    // ////////////////////////////////////

    private static ItemStack dust(Material material, int count) {
        return ChemicalHelper.get(TagPrefix.dust, material, count);
    }

    private static ItemStack crystal(String aspect) {
        return crystal(aspect, 1);
    }

    private static ItemStack crystal(String aspect, int count) {
        var block = ThaumcraftContent.block(aspect + "_crystal_cluster");
        return block == null ? ItemStack.EMPTY : new ItemStack(block.asItem(), count);
    }

    /** @return the fluid stack, or an empty stack when the material has no fluid in this GTCEu build */
    private static FluidStack fluid(Material material, int amount) {
        if (material == null || !material.hasFluid()) {
            return FluidStack.EMPTY;
        }
        return material.getFluid(amount);
    }

    private static void warn(String recipe) {
        Pollution.LOGGER.warn("Skipping remaining_items/{}: a required item or fluid is missing", recipe);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID, "remaining_items/" + path);
    }
}
