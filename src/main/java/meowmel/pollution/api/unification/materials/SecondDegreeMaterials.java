package meowmel.pollution.api.unification.materials;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.BlastProperty.GasTier;
import com.gregtechceu.gtceu.api.fluids.FluidBuilder;
import meowmel.pollution.Pollution;
import meowmel.pollution.api.unification.PollutionMaterials;
import net.minecraft.resources.ResourceLocation;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.DECOMPOSITION_BY_CENTRIFUGING;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.DECOMPOSITION_BY_ELECTROLYZING;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.GENERATE_DENSE;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.GENERATE_FRAME;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.GENERATE_GEAR;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.GENERATE_LONG_ROD;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.GENERATE_PLATE;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.GENERATE_ROD;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.GENERATE_ROTOR;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.GENERATE_ROUND;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.GENERATE_SMALL_GEAR;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Caesium;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Carbon;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Chlorine;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Endstone;
import static com.gregtechceu.gtceu.common.data.GTMaterials.HSSG;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Hafnium;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Hydrogen;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Iron;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Lithium;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Netherrack;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Nitrogen;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Oxygen;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Sodium;
import static com.gregtechceu.gtceu.common.data.GTMaterials.StainlessSteel;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Stone;

/**
 * Partial port of upstream {@code meowmel.pollution.api.unification.materials.SecondDegreeMaterials}.
 *
 * <p>The upstream file defines 61 second-degree materials. The port carries the
 * subset that is the sole missing dependency of the still-skipped recipes (see
 * {@code docs/MATERIALS_AUDIT.md}):</p>
 * <ul>
 *   <li>LLP chain ({@code LotusDust}, {@code EthylSilicate}, {@code RoughLlp},
 *       {@code Llp}, {@code OilWithLlp}) - unblocks the nine upstream
 *       {@code MagicChemicalRecipes} LLP recipes. The first upstream recipe
 *       additionally needs GTQT {@code SiliconTetrachloride}, which GTCEu
 *       7.5.3 does not provide; a substitution has to be chosen when that
 *       recipe is ported.</li>
 *   <li>{@code PureTar}, {@code SuperStickyTar} - unblocks the tar-slime pair
 *       in {@code MagicChemicalRecipes} and the upstream {@code TarChain}
 *       (CoalTar + Redstone fermentation and the four cracking recipes).</li>
 *   <li>{@code DimensionalTransformingAgent} - unblocks the Valonite / Octine /
 *       Syrmorite philosopher-stone transmutations in
 *       {@code MagicChemicalRecipes}. Its own upstream production (the kqt
 *       chain) is now ported as well.</li>
 *   <li>The second batch unblocks the kqt / superconductor / battery / filth /
 *       hachimi chains: the alchemical residue-vapour chain, the mercury /
 *       tin / iron solution chain, {@code UnstableDimensionalSilver} /
 *       {@code ImpureHyperdimensionalSilver} / {@code HyperdimensionalSilver},
 *       {@code Filth} / {@code FilthWater} / {@code VoidWater} /
 *       {@code VoidMaterial}, the battery hull alloys and contents,
 *       {@code MagicalSulfoPlumbicSalt} and {@code FerrousChloride}.</li>
 * </ul>
 *
 * <p>Adaptations to GTCEu 7.5.3:</p>
 * <ul>
 *   <li>{@code .ingot()} does not imply a dust form any more, so the materials
 *       whose upstream recipes consume dust request {@code .dust()} explicitly
 *       ({@code HyperdimensionalSilver}, the battery hull alloys).</li>
 *   <li>Upstream component lists use {@code GTQTMaterials.Thaumium} and
 *       {@code Mansussteel}; both stay substituted by the port's established
 *       mappings (// 上游: Thaumium -&gt; 本移植版: StainlessSteel；
 *       // 上游: Mansussteel -&gt; 本移植版: HSSG).</li>
 *   <li>{@code KQGold} is a real port material now, so the advanced battery
 *       content keeps its upstream component.</li>
 * </ul>
 */
public final class SecondDegreeMaterials {

    private SecondDegreeMaterials() {}

    public static void register() {
        // ---- LLP chain (MagicChemicalRecipes) --------------------------------

        PollutionMaterials.LotusDust = new Material.Builder(id("lotus_dust"))
                .color(0x008B45)
                .dust()
                .iconSet(MaterialIconSet.DULL)
                .buildAndRegister();

        PollutionMaterials.EthylSilicate = new Material.Builder(id("ethyl_silicate"))
                .color(0x708090)
                .fluid()
                .formula("(C2H5O)4Si", true)
                .buildAndRegister();

        PollutionMaterials.RoughLlp = new Material.Builder(id("rough_llp"))
                .color(0xB4EEB4)
                .dust()
                .iconSet(MaterialIconSet.DULL)
                .buildAndRegister();

        PollutionMaterials.Llp = new Material.Builder(id("llp"))
                .color(0xB4EEB4)
                .dust()
                .iconSet(MaterialIconSet.SHINY)
                .buildAndRegister();

        PollutionMaterials.OilWithLlp = new Material.Builder(id("oil_with_llp"))
                .color(0x698B69)
                .fluid()
                .buildAndRegister();

        // ---- tar chain (MagicChemicalRecipes + TarChain) ---------------------

        PollutionMaterials.PureTar = new Material.Builder(id("pure_tar"))
                .color(0x4F4F4F)
                // 上游: 流体方块 (FluidBuilder#block) -> 本移植版: 仅流体。
                // GT 为附加模组流体方块生成的模型引用 gtceu:block/void，而
                // datagen 的 ExistingFileHelper 看不到 GT jar 内资源，会直接
                // 中断 datagen；焦油链只需要流体。
                .liquid(new FluidBuilder())
                .buildAndRegister();

        PollutionMaterials.SuperStickyTar = new Material.Builder(id("super_sticky_tar"))
                .color(0x4F4F4F)
                .fluid()
                .iconSet(MaterialIconSet.SHINY)
                .buildAndRegister();

        // ---- philosopher-stone transmutations --------------------------------

        PollutionMaterials.DimensionalTransformingAgent = new Material.Builder(
                id("dimensional_transforming_agent"))
                .color(0xFFC7F7)
                .fluid()
                .buildAndRegister();

        // ---- 污秽链 filth chain ----------------------------------------------

        // 污秽 Filth
        PollutionMaterials.Filth = new Material.Builder(id("filth"))
                .color(0x5C0101)
                .dust()
                .iconSet(MaterialIconSet.DULL)
                .components(Netherrack, 6, Endstone, 1, Stone, 1, PollutionMaterials.InfusedTaint, 1)
                .buildAndRegister();

        // 污秽之水 FilthWater
        PollutionMaterials.FilthWater = new Material.Builder(id("filth_water"))
                .color(0x392323)
                .fluid()
                .components(PollutionMaterials.Filth, 9, PollutionMaterials.InfusedDeath, 1,
                        PollutionMaterials.InfusedDark, 1)
                .buildAndRegister();

        // 虚空之水 VoidWater
        PollutionMaterials.VoidWater = new Material.Builder(id("void_water"))
                .color(0x837D7D)
                .fluid()
                .buildAndRegister();

        // 虚空物质 VoidMaterial
        PollutionMaterials.VoidMaterial = new Material.Builder(id("void_material"))
                .color(0xC3C3C3)
                .dust()
                .iconSet(MaterialIconSet.SHINY)
                .buildAndRegister();

        // ---- kqt 链 alchemical chain -----------------------------------------

        // 蕴魔硫铅盐 MagicalSulfoPlumbicSalt
        PollutionMaterials.MagicalSulfoPlumbicSalt = new Material.Builder(id("magical_sulfo_plumbic_salt"))
                .color(0x5A286F)
                .iconSet(MaterialIconSet.SHINY)
                .dust()
                .buildAndRegister();

        // 一次炼金残渣/升华蒸汽
        PollutionMaterials.AlchemicalResidue1 = new Material.Builder(id("alchemical_residue_1"))
                .color(0x401751)
                .iconSet(MaterialIconSet.DULL)
                .dust()
                .buildAndRegister();
        PollutionMaterials.AlchemicalVapor1 = new Material.Builder(id("alchemical_vapor_1"))
                .color(0x9159A9)
                .fluid()
                .buildAndRegister();

        // 二次炼金残渣/升华蒸汽
        PollutionMaterials.AlchemicalResidue2 = new Material.Builder(id("alchemical_residue_2"))
                .color(0x647080)
                .iconSet(MaterialIconSet.DULL)
                .dust()
                .buildAndRegister();
        PollutionMaterials.AlchemicalVapor2 = new Material.Builder(id("alchemical_vapor_2"))
                .color(0x99CCFF)
                .fluid()
                .buildAndRegister();

        // 三次炼金残渣/升华蒸汽
        PollutionMaterials.AlchemicalResidue3 = new Material.Builder(id("alchemical_residue_3"))
                .color(0x494949)
                .iconSet(MaterialIconSet.DULL)
                .dust()
                .buildAndRegister();
        PollutionMaterials.AlchemicalVapor3 = new Material.Builder(id("alchemical_vapor_3"))
                .color(0xD7D7D7)
                .fluid()
                .buildAndRegister();

        // 四次炼金残渣/升华蒸汽
        PollutionMaterials.AlchemicalResidue4 = new Material.Builder(id("alchemical_residue_4"))
                .color(0x759F8A)
                .iconSet(MaterialIconSet.DULL)
                .dust()
                .buildAndRegister();
        PollutionMaterials.AlchemicalVapor4 = new Material.Builder(id("alchemical_vapor_4"))
                .color(0x86FCC1)
                .fluid()
                .buildAndRegister();

        // 五次炼金残渣/升华蒸汽
        PollutionMaterials.AlchemicalResidue5 = new Material.Builder(id("alchemical_residue_5"))
                .color(0x1F447C)
                .iconSet(MaterialIconSet.DULL)
                .dust()
                .buildAndRegister();
        PollutionMaterials.AlchemicalVapor5 = new Material.Builder(id("alchemical_vapor_5"))
                .color(0x3385FF)
                .fluid()
                .buildAndRegister();

        // 六次炼金残渣/升华蒸汽（上游色值 0xFCDBF 照录）
        PollutionMaterials.AlchemicalResidue6 = new Material.Builder(id("alchemical_residue_6"))
                .color(0x6B4B6F)
                .iconSet(MaterialIconSet.DULL)
                .dust()
                .buildAndRegister();
        PollutionMaterials.AlchemicalVapor6 = new Material.Builder(id("alchemical_vapor_6"))
                .color(0xFCDBF)
                .fluid()
                .buildAndRegister();

        // 神秘锡溶液 MagicalTinSolution
        PollutionMaterials.MagicalTinSolution = new Material.Builder(id("magical_tin_solution"))
                .color(0xB9B9B9)
                .fluid()
                .buildAndRegister();

        // 硫酸亚锡神秘溶液 MagicalStannousSulfateSolution
        PollutionMaterials.MagicalStannousSulfateSolution = new Material.Builder(
                id("magical_stannous_sulfate_solution"))
                .color(0xF1FF97)
                .fluid()
                .buildAndRegister();

        // 高魔素硫酸亚锡 HighmanaStannousSulfate
        PollutionMaterials.HighmanaStannousSulfate = new Material.Builder(id("highmana_stannous_sulfate"))
                .color(0xCCFF33)
                .iconSet(MaterialIconSet.SHINY)
                .dust()
                .buildAndRegister();

        // 含杂汞盐溶液 ImpureMercuricSaltSolution
        PollutionMaterials.ImpureMercuricSaltSolution = new Material.Builder(id("impure_mercuric_salt_solution"))
                .color(0x95978E)
                .fluid()
                .buildAndRegister();

        // 神秘汞盐溶液 MercuricSaltSolution
        PollutionMaterials.MercuricSaltSolution = new Material.Builder(id("mercuric_salt_solution"))
                .color(0xDBDCD8)
                .fluid()
                .buildAndRegister();

        // 魔力激活氯化铁溶液 MagicActivatedIronChlorideSolution
        PollutionMaterials.MagicActivatedIronChlorideSolution = new Material.Builder(
                id("magic_activated_iron_chloride_solution"))
                .color(0x394320)
                .fluid()
                .buildAndRegister();

        // 魔力激活氯化亚铁甲醇溶液 MagicActivatedFerrousChlorideEthanolSolution
        PollutionMaterials.MagicActivatedFerrousChlorideEthanolSolution = new Material.Builder(
                id("magic_activated_ferrous_chloride_ethanol_solution"))
                .color(0x80FF66)
                .fluid()
                .buildAndRegister();

        // 除杂激活氯化亚铁甲醇溶液 PurifiedActivatedFerrousChlorideEthanolSolution
        PollutionMaterials.PurifiedActivatedFerrousChlorideEthanolSolution = new Material.Builder(
                id("purified_activated_ferrous_chloride_ethanol_solution"))
                .color(0xACFF9B)
                .fluid()
                .buildAndRegister();

        // 除杂激活氯化亚铁 PurifiedActivatedFerrousChloride
        PollutionMaterials.PurifiedActivatedFerrousChloride = new Material.Builder(
                id("purified_activated_ferrous_chloride"))
                .color(0xDEFFD7)
                .iconSet(MaterialIconSet.SHINY)
                .dust()
                .buildAndRegister();

        // 赛摩铜掺杂魔水溶液 SyrmoriteDopedMagicWaterSolution
        PollutionMaterials.SyrmoriteDopedMagicWaterSolution = new Material.Builder(
                id("syrmorite_doped_magic_water_solution"))
                .color(0x1D4FDA)
                .fluid()
                .buildAndRegister();

        // 未成形胚胎魔水 UnformedEmbryoMagicWater
        PollutionMaterials.UnformedEmbryoMagicWater = new Material.Builder(id("unformed_embryo_magic_water"))
                .color(0x537EF2)
                .fluid()
                .buildAndRegister();

        // 胚胎魔水 EmbryoMagicWater
        PollutionMaterials.EmbryoMagicWater = new Material.Builder(id("embryo_magic_water"))
                .color(0xA5BDFF)
                .fluid()
                .buildAndRegister();

        // 不稳次元银 UnstableDimensionalSilver
        PollutionMaterials.UnstableDimensionalSilver = new Material.Builder(id("unstable_dimensional_silver"))
                .color(0x7D989C)
                .dust()
                .buildAndRegister();

        // 超次元含杂秘银流 ImpureHyperdimensionalSilver
        PollutionMaterials.ImpureHyperdimensionalSilver = new Material.Builder(
                id("impure_hyperdimensional_silver"))
                .color(0x9AD4DC)
                .fluid()
                .buildAndRegister();

        // 超次元秘银 HyperdimensionalSilver
        PollutionMaterials.HyperdimensionalSilver = new Material.Builder(id("hyperdimensional_silver"))
                .color(0xD1FAFF)
                .ingot().dust().fluid().plasma()
                .iconSet(MaterialIconSet.SHINY)
                .flags(GENERATE_PLATE, GENERATE_ROTOR, GENERATE_ROD, GENERATE_LONG_ROD, GENERATE_FRAME,
                        GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_ROUND, DECOMPOSITION_BY_CENTRIFUGING)
                .blast(5400, GasTier.MID)
                .buildAndRegister()
                .setFormula("Ag50(RnMa)m(AeIgAqTerOrdPe)n", true);

        // ---- 电池链 battery chain --------------------------------------------
        // （定义在 HyperdimensionalSilver 之后：外壳合金组件引用它）

        // 基础电池外壳合金
        // // 上游: Mansussteel -> 本移植版: HSSG
        PollutionMaterials.BasicBatteryHullAlloy = new Material.Builder(id("basic_battery_hull_alloy"))
                .color(0x877886)
                .ingot().dust().fluid()
                .iconSet(MaterialIconSet.METALLIC)
                .flags(DECOMPOSITION_BY_ELECTROLYZING)
                .components(HSSG, 4, PollutionMaterials.Ordolead, 1)
                .flags(GENERATE_DENSE, GENERATE_FRAME, GENERATE_PLATE, GENERATE_ROTOR, GENERATE_ROD,
                        GENERATE_LONG_ROD, GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_ROUND)
                .blast(2700, GasTier.LOW)
                .buildAndRegister();

        // 进阶电池外壳合金
        PollutionMaterials.AdvancedBatteryHullAlloy = new Material.Builder(id("advanced_battery_hull_alloy"))
                .color(0xA4D4CD)
                .ingot().dust().fluid()
                .iconSet(MaterialIconSet.METALLIC)
                .flags(DECOMPOSITION_BY_ELECTROLYZING)
                .components(PollutionMaterials.HyperdimensionalSilver, 4, PollutionMaterials.Valonite, 1)
                .flags(GENERATE_DENSE, GENERATE_FRAME, GENERATE_PLATE, GENERATE_ROTOR, GENERATE_ROD,
                        GENERATE_LONG_ROD, GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_ROUND)
                .blast(5400, GasTier.MID)
                .buildAndRegister();

        // 基础电池内容物
        // // 上游: GTQT Thaumium -> 本移植版: StainlessSteel
        PollutionMaterials.BasicBatteryContent = new Material.Builder(id("basic_battery_content"))
                .color(0x687D9F)
                .dust()
                .iconSet(MaterialIconSet.DULL)
                .flags(DECOMPOSITION_BY_ELECTROLYZING)
                .components(Lithium, 6, StainlessSteel, 2, PollutionMaterials.InfusedEnergy, 1,
                        PollutionMaterials.InfusedMotion, 1)
                .buildAndRegister();

        // 进阶电池内容物
        PollutionMaterials.AdvancedBatteryContent = new Material.Builder(id("advanced_battery_content"))
                .color(0xFFFFE2)
                .dust()
                .iconSet(MaterialIconSet.BRIGHT)
                .flags(DECOMPOSITION_BY_ELECTROLYZING)
                .components(Caesium, 6, PollutionMaterials.KQGold, 2, PollutionMaterials.InfusedEnergy, 1,
                        PollutionMaterials.InfusedMotion, 1)
                .buildAndRegister();

        // 氯化亚铁 FerrousChloride
        PollutionMaterials.FerrousChloride = new Material.Builder(id("ferrous_chloride"))
                .color(0x86FF8E)
                .dust()
                .iconSet(MaterialIconSet.BRIGHT)
                .components(Iron, 1, Chlorine, 2)
                .flags(DECOMPOSITION_BY_ELECTROLYZING)
                .buildAndRegister();

        // ---- 耄耋哈基米链 hachimi chain --------------------------------------

        // 叠氮酸 HydrazoicAcid
        PollutionMaterials.HydrazoicAcid = new Material.Builder(id("hydrazoic_acid"))
                .color(0xFAF0AF)
                .fluid()
                .iconSet(MaterialIconSet.DULL)
                .components(Hydrogen, 1, Nitrogen, 3)
                .buildAndRegister();

        // 叠氮化钠 SodiumAzide
        PollutionMaterials.SodiumAzide = new Material.Builder(id("sodium_azide"))
                .color(0xFAF0AF)
                .dust()
                .iconSet(MaterialIconSet.DULL)
                .components(Sodium, 1, Nitrogen, 3)
                .buildAndRegister();

        // 环戊二烯基钠 SodiumCyclopentadienide
        PollutionMaterials.SodiumCyclopentadienide = new Material.Builder(id("sodium_cyclopentadienide"))
                .color(0xF0BB74)
                .dust()
                .iconSet(MaterialIconSet.DULL)
                .components(Carbon, 5, Hydrogen, 5, Sodium, 1)
                .buildAndRegister()
                .setFormula("(C5H5)Na");

        // 二氯二茂铪 HafnoceneDichloride
        PollutionMaterials.HafnoceneDichloride = new Material.Builder(id("hafnocene_dichloride"))
                .color(0xF0A274)
                .dust()
                .iconSet(MaterialIconSet.DULL)
                .components(Carbon, 10, Hydrogen, 10, Hafnium, 1, Chlorine, 2)
                .buildAndRegister()
                .setFormula("(C5H5)2HfCl2");

        // 茂叠铪基醚 uOxoBisHafnoceneAzide（上游 setTooltips 在现代 API 无对应，略；
        // 上游 id 以希腊字母 μ 开头，1.20.1 ResourceLocation 不接受非 ASCII，改为 u_）
        PollutionMaterials.uOxoBisHafnoceneAzide = new Material.Builder(id("u_oxo_bis_hafnocene_azide"))
                .color(0xFDDD5A)
                .dust()
                .iconSet(MaterialIconSet.DULL)
                .components(Carbon, 20, Hydrogen, 20, Hafnium, 2, Nitrogen, 6, Oxygen, 1)
                .buildAndRegister()
                .setFormula("[(Cp)2Hf(N3)2]2(μ-O)");
    }

    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID, name);
    }
}
