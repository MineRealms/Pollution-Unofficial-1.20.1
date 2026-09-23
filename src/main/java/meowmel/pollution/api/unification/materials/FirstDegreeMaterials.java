package meowmel.pollution.api.unification.materials;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.BlastProperty.GasTier;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import meowmel.pollution.Pollution;
import meowmel.pollution.api.unification.PollutionElements;
import meowmel.pollution.api.unification.PollutionMaterials;
import net.minecraft.resources.ResourceLocation;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.DECOMPOSITION_BY_CENTRIFUGING;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.GENERATE_DENSE;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.GENERATE_FRAME;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.GENERATE_GEAR;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.GENERATE_LONG_ROD;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.GENERATE_PLATE;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.GENERATE_ROD;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.GENERATE_ROTOR;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.GENERATE_ROUND;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.GENERATE_SMALL_GEAR;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Aluminium;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Bauxite;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Boron;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Carbon;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Copper;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Fluorine;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Gold;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Lead;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Lithium;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Magnesium;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Manganese;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Mercury;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Oxygen;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Phosphate;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Silicon;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Silver;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Steel;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Thorium;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Tin;

/**
 * The six aspect alloys used by the magic multiblock parts, plus the magic
 * superconductor chain.
 *
 * <p>Ported from upstream {@code meowmel.pollution.api.unification.materials.FirstDegreeMaterials}
 * (colors, component ratios and blast temperature are unchanged). Upstream depended on
 * GTQT-only materials for later entries of the same class; those stay TODO until the
 * magic lines they belong to are ported.</p>
 *
 * <p>The second batch ports {@code Impuremana}, {@code KQGold},
 * {@code CrudeLk99}, {@code MagicalSuperconductiveLiquid} and the two
 * thaumic superconductors. Adaptations to GTCEu 7.5.3:</p>
 * <ul>
 *   <li>{@code KQGold}'s tool/rotor stats are dropped (the modern
 *       {@code ToolProperty.Builder} values cannot be transplanted 1:1, see
 *       {@link OreMaterials}); cable and fluid-pipe properties are kept.</li>
 *   <li>Upstream {@code .ingot()} implied a dust form in 1.12; GTCEu Modern
 *       does not, so {@code KQGold} and {@code CrudeLk99} request
 *       {@code .dust()} explicitly (the upstream recipes consume the dust).</li>
 *   <li>The superconductors carry {@code GENERATE_PLATE} because the battery
 *       chain consumes their plate form.</li>
 * </ul>
 *
 * <p>The third batch ports {@code RichAura}, {@code ErichAura} and
 * {@code ElvenElementium} (colors, components and flags unchanged).
 * {@code ElvenElementium}'s upstream {@code .ingot()} implied a dust in 1.12,
 * so {@code .dust()} is requested explicitly here: the upstream
 * {@code ForgeAlchemyRecipes} consumes the dust form (see the KQGold/CrudeLk99
 * note above) and {@code DECOMPOSITION_BY_CENTRIFUGING} needs a dust input.</p>
 */
public final class FirstDegreeMaterials {

    private FirstDegreeMaterials() {}

    public static void register() {
        // 律动钛
        PollutionMaterials.Aertitanium = new Material.Builder(id("aertitanium"))
                .color(0xEED2EE)
                .ingot().fluid()
                .components(Bauxite, 2, Aluminium, 1, Manganese, 1, PollutionMaterials.InfusedAir, 5)
                .iconSet(MaterialIconSet.SHINY)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .blast(2700, GasTier.LOW)
                .buildAndRegister();

        // 残日钢
        PollutionMaterials.IgnisSteel = new Material.Builder(id("ignissteel"))
                .color(0x8B1A1A)
                .ingot().fluid()
                .components(Steel, 2, Magnesium, 1, Lithium, 1, PollutionMaterials.InfusedFire, 5)
                .iconSet(MaterialIconSet.SHINY)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .blast(2700, GasTier.LOW)
                .buildAndRegister();

        // 捩花银
        PollutionMaterials.Aquasilver = new Material.Builder(id("aquasilver"))
                .color(0xCAE1FF)
                .ingot().fluid()
                .components(Silver, 2, Tin, 1, Mercury, 1, PollutionMaterials.InfusedWater, 5)
                .iconSet(MaterialIconSet.SHINY)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .blast(2700, GasTier.LOW)
                .buildAndRegister();

        // 定坤铜
        PollutionMaterials.Terracopper = new Material.Builder(id("terracopper"))
                .color(0x8FBC8F)
                .ingot().fluid()
                .components(Copper, 2, Boron, 1, Carbon, 1, PollutionMaterials.InfusedEarth, 5)
                .iconSet(MaterialIconSet.SHINY)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .blast(2700, GasTier.LOW)
                .buildAndRegister();

        // 司辰铅
        PollutionMaterials.Ordolead = new Material.Builder(id("ordolead"))
                .color(0x00008B)
                .ingot().fluid()
                .components(Lead, 2, Silicon, 1, Gold, 1, PollutionMaterials.InfusedOrder, 5)
                .iconSet(MaterialIconSet.SHINY)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .blast(2700, GasTier.LOW)
                .buildAndRegister();

        // 无极铝
        PollutionMaterials.Perditioaluminium = new Material.Builder(id("perditioaluminium"))
                .color(0x9C9C9C)
                .ingot().fluid()
                .components(Aluminium, 2, Fluorine, 1, Thorium, 1, PollutionMaterials.InfusedEntropy, 5)
                .iconSet(MaterialIconSet.SHINY)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .blast(2700, GasTier.LOW)
                .buildAndRegister();

        // ---- 魔力/超导链（第二轮移植）---------------------------------------

    // 不纯魔力 Impuremana
    PollutionMaterials.Impuremana = new Material.Builder(id("impuremana"))
            .color(0x008B8B)
            .fluid()
            .iconSet(MaterialIconSet.DULL)
            .buildAndRegister();

    // 纯魔力 Mana（启命花园模式 1 输出；上游 pollution:mana 流体）
    PollutionMaterials.Mana = new Material.Builder(id("mana"))
            .color(0x4FC3F7)
            .fluid()
            .iconSet(MaterialIconSet.DULL)
            .buildAndRegister();

        // 刻金 KQGold（上游 toolStats/rotorStats 因现代 API 差异跳过；
        // 上游 id "Keqinggold" 含大写，1.20.1 ResourceLocation 要求全小写）
        PollutionMaterials.KQGold = new Material.Builder(id("keqinggold"))
                .color(0xFCF770)
                .fluid().ingot().dust().plasma()
                .iconSet(MaterialIconSet.SHINY)
                .fluidPipeProperties(6000, 400, true)
                .cableProperties(GTValues.V[6], 16, 2)
                .element(PollutionElements.Kqt)
                .flags(GENERATE_PLATE, GENERATE_ROTOR, GENERATE_ROD, GENERATE_LONG_ROD, GENERATE_FRAME,
                        GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_ROUND)
                .blast(3600, GasTier.MID)
                .buildAndRegister();

        // LK-99 粗胚 CrudeLk99
        PollutionMaterials.CrudeLk99 = new Material.Builder(id("crude_lk_99"))
                .color(0x808080)
                .ingot().dust().fluid()
                .components(Lead, 6, Copper, 4, Phosphate, 6, Oxygen, 1)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .iconSet(MaterialIconSet.BRIGHT)
                .blast(2700)
                .buildAndRegister();

        // 灌魔超导液 MagicalSuperconductiveLiquid
        PollutionMaterials.MagicalSuperconductiveLiquid = new Material.Builder(id("magical_superconductive_liquid"))
                .color(0x9C039C)
                .fluid()
                .buildAndRegister();

        // 初阶神秘超导体 BasicThaumicSuperconductor
        PollutionMaterials.BasicThaumicSuperconductor = new Material.Builder(id("basic_thaumic_superconductor"))
                .color(0xC6B3C6)
                .ingot().fluid()
                .iconSet(MaterialIconSet.BRIGHT)
                .flags(GENERATE_PLATE)
                .cableProperties(GTValues.V[4], 8, 0, true)
                .buildAndRegister();

        // 高阶神秘超导体 AdvancedThaumicSuperconductor
        PollutionMaterials.AdvancedThaumicSuperconductor = new Material.Builder(id("advanced_thaumic_superconductor"))
                .color(0xDDFF6E)
                .ingot().fluid()
                .iconSet(MaterialIconSet.BRIGHT)
                .flags(GENERATE_PLATE)
                .cableProperties(GTValues.V[8], 8, 0, true)
                .buildAndRegister();

        // ---- 灵气/精灵链（第三轮移植）---------------------------------------

        // 富灵气 RichAura
        PollutionMaterials.RichAura = new Material.Builder(id("rich_aura"))
                .color(0xCD6600)
                .fluid()
                .iconSet(MaterialIconSet.SHINY)
                .buildAndRegister();

        // 浓灵气 ErichAura
        PollutionMaterials.ErichAura = new Material.Builder(id("erich_aura"))
                .color(0xCD0000)
                .fluid()
                .iconSet(MaterialIconSet.SHINY)
                .buildAndRegister();

        // 精灵元素 ElvenElementium
        // 上游 .ingot() 在 1.12 隐含 dust，本移植版显式补上（上游
        // ForgeAlchemyRecipes 消耗其 dust，且 DECOMPOSITION_BY_CENTRIFUGING 需要 dust 输入）
        PollutionMaterials.ElvenElementium = new Material.Builder(id("elven_elementium"))
                .color(0xEE6AA7)
                .ingot().dust().fluid().ore()
                .components(GTMaterials.Iron, 4, PollutionMaterials.Elven, 1)
                .iconSet(MaterialIconSet.SHINY)
                .flags(GENERATE_DENSE, GENERATE_FRAME, GENERATE_PLATE, GENERATE_ROTOR, GENERATE_ROD,
                        GENERATE_LONG_ROD, GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_ROUND,
                        DECOMPOSITION_BY_CENTRIFUGING)
                .buildAndRegister();
    }

    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID, name);
    }
}
