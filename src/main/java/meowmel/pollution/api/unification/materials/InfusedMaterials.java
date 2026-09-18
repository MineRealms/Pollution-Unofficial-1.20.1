package meowmel.pollution.api.unification.materials;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import meowmel.pollution.Pollution;
import meowmel.pollution.api.unification.PollutionMaterials;
import net.minecraft.resources.ResourceLocation;

/**
 * Compound aspect materials.
 *
 * <p>Ported one-to-one from upstream
 * {@code meowmel.pollution.api.unification.materials.InfusedMaterials}:
 * colors, shapes ({@code ore/gem/fluid}), icon set and the 1:1 component
 * recipes are unchanged; the upstream comment of each material names its
 * Thaumcraft aspect.</p>
 *
 * <p>Upstream called {@code Material#setTooltips(...)} after {@code build()};
 * GregTech CEu Modern 7.5.3 has no equivalent API (checked against the binary),
 * so the aspect name is kept as a comment instead.</p>
 */
public final class InfusedMaterials {

    private InfusedMaterials() {}

    public static void register() {
        // Vitreus (水晶 = 气 + 土)
        PollutionMaterials.InfusedCrystal = new Material.Builder(id("infused_crystal"))
                .color(0x87CEFA)
                .ore().gem().fluid()
                .iconSet(MaterialIconSet.SHINY)
                .components(PollutionMaterials.InfusedAir, 1, PollutionMaterials.InfusedEarth, 1)
                .buildAndRegister();

        // Victus (生命 = 土 + 水)
        PollutionMaterials.InfusedLife = new Material.Builder(id("infused_life"))
                .color(0xFF6A6A)
                .ore().gem().fluid()
                .iconSet(MaterialIconSet.SHINY)
                .components(PollutionMaterials.InfusedEarth, 1, PollutionMaterials.InfusedWater, 1)
                .buildAndRegister();

        // Mortuus (死亡 = 水 + 混沌)
        PollutionMaterials.InfusedDeath = new Material.Builder(id("infused_death"))
                .color(0x696969)
                .ore().gem().fluid()
                .iconSet(MaterialIconSet.SHINY)
                .components(PollutionMaterials.InfusedWater, 1, PollutionMaterials.InfusedEntropy, 1)
                .buildAndRegister();

        // Spiritus (灵魂 = 生命 + 死亡)
        PollutionMaterials.InfusedSoul = new Material.Builder(id("infused_soul"))
                .color(0xCFCFCF)
                .ore().gem().fluid()
                .iconSet(MaterialIconSet.SHINY)
                .components(PollutionMaterials.InfusedLife, 1, PollutionMaterials.InfusedDeath, 1)
                .buildAndRegister();

        // Telum (武器 = 灵魂 + 混沌)
        PollutionMaterials.InfusedWeapon = new Material.Builder(id("infused_weapon"))
                .color(0xB22222)
                .ore().gem().fluid()
                .iconSet(MaterialIconSet.SHINY)
                .components(PollutionMaterials.InfusedSoul, 1, PollutionMaterials.InfusedEntropy, 1)
                .buildAndRegister();

        // Metallum (金属 = 土 + 秩序)
        PollutionMaterials.InfusedMetal = new Material.Builder(id("infused_metal"))
                .color(0x9FB6CD)
                .ore().gem().fluid()
                .iconSet(MaterialIconSet.SHINY)
                .components(PollutionMaterials.InfusedEarth, 1, PollutionMaterials.InfusedOrder, 1)
                .buildAndRegister();

        // Potentia (能量 = 秩序 + 火)
        PollutionMaterials.InfusedEnergy = new Material.Builder(id("infused_energy"))
                .color(0xF0FFF0)
                .ore().gem().fluid()
                .iconSet(MaterialIconSet.SHINY)
                .components(PollutionMaterials.InfusedOrder, 1, PollutionMaterials.InfusedFire, 1)
                .buildAndRegister();

        // Instrumentum (工具 = 金属 + 能量)
        PollutionMaterials.InfusedInstrument = new Material.Builder(id("infused_instrument"))
                .color(0x0000CD)
                .ore().gem().fluid()
                .iconSet(MaterialIconSet.SHINY)
                .components(PollutionMaterials.InfusedMetal, 1, PollutionMaterials.InfusedEnergy, 1)
                .buildAndRegister();

        // Permutatio (交换 = 秩序 + 混沌)
        PollutionMaterials.InfusedExchange = new Material.Builder(id("infused_exchange"))
                .color(0x548B54)
                .ore().gem().fluid()
                .iconSet(MaterialIconSet.SHINY)
                .components(PollutionMaterials.InfusedOrder, 1, PollutionMaterials.InfusedEntropy, 1)
                .buildAndRegister();

        // Praecantatio (魔法 = 气 + 能量)
        PollutionMaterials.InfusedMagic = new Material.Builder(id("infused_magic"))
                .color(0x8A2BE2)
                .ore().gem().fluid()
                .iconSet(MaterialIconSet.SHINY)
                .components(PollutionMaterials.InfusedAir, 1, PollutionMaterials.InfusedEnergy, 1)
                .buildAndRegister();

        // Alchemia (炼金 = 魔法 + 水) — no alkimia entry in the TC4R core aspect registry
        PollutionMaterials.InfusedAlchemy = new Material.Builder(id("infused_alchemy"))
                .color(0x8FBC8F)
                .ore().gem().fluid()
                .iconSet(MaterialIconSet.SHINY)
                .components(PollutionMaterials.InfusedMagic, 1, PollutionMaterials.InfusedWater, 1)
                .buildAndRegister();

        // Gelum (寒冷 = 火 + 混沌)
        PollutionMaterials.InfusedCold = new Material.Builder(id("infused_cold"))
                .color(0xF0FFFF)
                .ore().gem().fluid()
                .iconSet(MaterialIconSet.SHINY)
                .components(PollutionMaterials.InfusedFire, 1, PollutionMaterials.InfusedEntropy, 1)
                .buildAndRegister();

        // Auram (灵气 = 魔法 + 气)
        PollutionMaterials.InfusedAura = new Material.Builder(id("infused_aura"))
                .color(0xF7AEFF)
                .ore().gem().fluid()
                .iconSet(MaterialIconSet.SHINY)
                .components(PollutionMaterials.InfusedMagic, 1, PollutionMaterials.InfusedAir, 1)
                .buildAndRegister();

        // Lux (光明 = 火 + 气)
        PollutionMaterials.InfusedLight = new Material.Builder(id("infused_light"))
                .color(0xF3FF80)
                .ore().gem().fluid()
                .iconSet(MaterialIconSet.SHINY)
                .components(PollutionMaterials.InfusedFire, 1, PollutionMaterials.InfusedAir, 1)
                .buildAndRegister();

        // Fabrico (合成 = 交换 + 工具)
        PollutionMaterials.InfusedCraft = new Material.Builder(id("infused_craft"))
                .color(0x6D26FC)
                .ore().gem().fluid()
                .iconSet(MaterialIconSet.SHINY)
                .components(PollutionMaterials.InfusedExchange, 1, PollutionMaterials.InfusedInstrument, 1)
                .buildAndRegister();

        // Vacuos (虚空 = 气 + 混沌)
        PollutionMaterials.InfusedVoid = new Material.Builder(id("infused_void"))
                .color(0xACACAC)
                .ore().gem().fluid()
                .iconSet(MaterialIconSet.SHINY)
                .components(PollutionMaterials.InfusedAir, 1, PollutionMaterials.InfusedEntropy, 1)
                .buildAndRegister();

        // Motus (运动 = 气 + 秩序)
        PollutionMaterials.InfusedMotion = new Material.Builder(id("infused_motion"))
                .color(0xB3B3B3)
                .ore().gem().fluid()
                .iconSet(MaterialIconSet.SHINY)
                .components(PollutionMaterials.InfusedAir, 1, PollutionMaterials.InfusedOrder, 1)
                .buildAndRegister();

        // Vitium (腐化 = 混沌 + 魔法)
        PollutionMaterials.InfusedTaint = new Material.Builder(id("infused_taint"))
                .color(0x7C1280)
                .ore().gem().fluid()
                .iconSet(MaterialIconSet.SHINY)
                .components(PollutionMaterials.InfusedMagic, 1, PollutionMaterials.InfusedEntropy, 1)
                .buildAndRegister();

        // Tenebrae (黑暗 = 虚空 + 光明)
        PollutionMaterials.InfusedDark = new Material.Builder(id("infused_dark"))
                .color(0x1A1A1A)
                .ore().gem().fluid()
                .iconSet(MaterialIconSet.SHINY)
                .components(PollutionMaterials.InfusedVoid, 1, PollutionMaterials.InfusedLight, 1)
                .buildAndRegister();

        // Alienis (异域 = 虚空 + 黑暗)
        PollutionMaterials.InfusedAlien = new Material.Builder(id("infused_alien"))
                .color(0xC17EC3)
                .ore().gem().fluid()
                .iconSet(MaterialIconSet.SHINY)
                .components(PollutionMaterials.InfusedVoid, 1, PollutionMaterials.InfusedDark, 1)
                .buildAndRegister();

        // Volatus (飞行 = 气 + 运动)
        PollutionMaterials.InfusedFly = new Material.Builder(id("infused_fly"))
                .color(0xFFFFEC)
                .ore().gem().fluid()
                .iconSet(MaterialIconSet.SHINY)
                .components(PollutionMaterials.InfusedAir, 1, PollutionMaterials.InfusedMotion, 1)
                .buildAndRegister();

        // Herba (植物 = 生命 + 土)
        PollutionMaterials.InfusedPlant = new Material.Builder(id("infused_plant"))
                .color(0x69FF4B)
                .ore().gem().fluid()
                .iconSet(MaterialIconSet.SHINY)
                .components(PollutionMaterials.InfusedLife, 1, PollutionMaterials.InfusedEarth, 1)
                .buildAndRegister();

        // Machina (机械 = 运动 + 工具)
        PollutionMaterials.InfusedMechanics = new Material.Builder(id("infused_mechanics"))
                .color(0x727272)
                .ore().gem().fluid()
                .iconSet(MaterialIconSet.SHINY)
                .components(PollutionMaterials.InfusedMotion, 1, PollutionMaterials.InfusedInstrument, 1)
                .buildAndRegister();

        // Vinculum (陷阱 = 运动 + 混沌)
        PollutionMaterials.InfusedTrap = new Material.Builder(id("infused_trap"))
                .color(0x756060)
                .ore().gem().fluid()
                .iconSet(MaterialIconSet.SHINY)
                .components(PollutionMaterials.InfusedMotion, 1, PollutionMaterials.InfusedEntropy, 1)
                .buildAndRegister();

        // Exanimis (亡灵 = 运动 + 死亡)
        PollutionMaterials.InfusedUndead = new Material.Builder(id("infused_undead"))
                .color(0x494244)
                .ore().gem().fluid()
                .iconSet(MaterialIconSet.SHINY)
                .components(PollutionMaterials.InfusedMotion, 1, PollutionMaterials.InfusedDeath, 1)
                .buildAndRegister();

        // Cognitio (思维 = 火 + 灵魂)
        PollutionMaterials.InfusedThought = new Material.Builder(id("infused_thought"))
                .color(0xFF9999)
                .ore().gem().fluid()
                .iconSet(MaterialIconSet.SHINY)
                .components(PollutionMaterials.InfusedFire, 1, PollutionMaterials.InfusedSoul, 1)
                .buildAndRegister();

        // Sensus (感知 = 气 + 灵魂)
        PollutionMaterials.InfusedSense = new Material.Builder(id("infused_sense"))
                .color(0x57D3FF)
                .ore().gem().fluid()
                .iconSet(MaterialIconSet.SHINY)
                .components(PollutionMaterials.InfusedAir, 1, PollutionMaterials.InfusedSoul, 1)
                .buildAndRegister();

        // Bestia (野兽 = 运动 + 生命)
        PollutionMaterials.InfusedAnimal = new Material.Builder(id("infused_animal"))
                .color(0x994C00)
                .ore().gem().fluid()
                .iconSet(MaterialIconSet.SHINY)
                .components(PollutionMaterials.InfusedMotion, 1, PollutionMaterials.InfusedLife, 1)
                .buildAndRegister();

        // Humanus (人类 = 灵魂 + 生命)
        PollutionMaterials.InfusedHuman = new Material.Builder(id("infused_human"))
                .color(0xFFD6D1)
                .ore().gem().fluid()
                .iconSet(MaterialIconSet.SHINY)
                .components(PollutionMaterials.InfusedSoul, 1, PollutionMaterials.InfusedLife, 1)
                .buildAndRegister();

        // Lucrum (贪婪 = 灵魂 + 虚空; upstream used the TC6 desire aspect)
        PollutionMaterials.InfusedGreed = new Material.Builder(id("infused_greed"))
                .color(0xF3F303)
                .ore().gem().fluid()
                .iconSet(MaterialIconSet.SHINY)
                .components(PollutionMaterials.InfusedSoul, 1, PollutionMaterials.InfusedVoid, 1)
                .buildAndRegister();

        // Tutamen (装备 = 灵魂 + 土; upstream used the TC6 protect aspect)
        PollutionMaterials.InfusedArmor = new Material.Builder(id("infused_armor"))
                .color(0x03CBF3)
                .ore().gem().fluid()
                .iconSet(MaterialIconSet.SHINY)
                .components(PollutionMaterials.InfusedSoul, 1, PollutionMaterials.InfusedEarth, 1)
                .buildAndRegister();

        // Spatium (空间 = 虚空 + 混沌) — Planar Artifice aspect, absent from TC4R core
        PollutionMaterials.InfusedSpatio = new Material.Builder(id("infused_spatio"))
                .color(0x4AF755)
                .ore().gem().fluid()
                .iconSet(MaterialIconSet.SHINY)
                .components(PollutionMaterials.InfusedVoid, 1, PollutionMaterials.InfusedEntropy, 1)
                .buildAndRegister();

        // Tempus (时间 = 空间 + 交换) — Planar Artifice aspect, absent from TC4R core
        PollutionMaterials.InfusedTempus = new Material.Builder(id("infused_tempus"))
                .color(0xD6DB43)
                .ore().gem().fluid()
                .iconSet(MaterialIconSet.SHINY)
                .components(PollutionMaterials.InfusedSpatio, 1, PollutionMaterials.InfusedExchange, 1)
                .buildAndRegister();

        // Tinctura (艺术 = 感觉 + 交换) — Planar Artifice aspect, absent from TC4R core
        PollutionMaterials.InfusedTinctura = new Material.Builder(id("infused_tinctura"))
                .color(0xD6DB43)
                .ore().gem().fluid()
                .iconSet(MaterialIconSet.SHINY)
                .components(PollutionMaterials.InfusedSense, 1, PollutionMaterials.InfusedExchange, 1)
                .buildAndRegister();
    }

    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID, name);
    }
}
