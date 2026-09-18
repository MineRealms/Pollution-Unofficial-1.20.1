package meowmel.pollution.api.unification.materials;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import meowmel.pollution.Pollution;
import meowmel.pollution.api.unification.PollutionMaterials;
import net.minecraft.resources.ResourceLocation;

/**
 * Partial port of upstream
 * {@code meowmel.pollution.api.unification.materials.MagicIntegrationMaterials}.
 *
 * <p>The upstream file defines seven cross-mod integration materials
 * (OpticalGradeAquamarine, StarlightPollen, MoonlightResin, AstralBloodPlasma,
 * CelestialBiologicalMedium, ArcaneInk, StarryArcaneAlloy).</p>
 *
 * <p>Ported: {@code ArcaneInk} (the sole missing dependency of the arcane ink
 * capsule recipe), plus {@code OpticalGradeAquamarine}, {@code StarlightPollen}
 * and {@code MoonlightResin} (the Astral-material trio; their production and
 * consumer recipes stay skipped with the rest of the Astral Sorcery group, the
 * definitions are kept for parity). See {@code docs/MATERIALS_AUDIT.md}.</p>
 *
 * <p>Still unported: {@code AstralBloodPlasma},
 * {@code CelestialBiologicalMedium} and {@code StarryArcaneAlloy} - every
 * recipe that consumes them also needs Astral Sorcery or Blood Magic content,
 * which the port does not depend on
 * (// 跳过: 整合包无 Astral Sorcery/Blood Magic).</p>
 */
public final class MagicIntegrationMaterials {

    private MagicIntegrationMaterials() {}

    public static void register() {
        // 光学级海蓝宝石 OpticalGradeAquamarine
        PollutionMaterials.OpticalGradeAquamarine = new Material.Builder(id("optical_grade_aquamarine"))
                .color(0x6DE8F2)
                .gem()
                .iconSet(MaterialIconSet.GEM_HORIZONTAL)
                .buildAndRegister();

        // 星光花粉 StarlightPollen
        PollutionMaterials.StarlightPollen = new Material.Builder(id("starlight_pollen"))
                .color(0xBCEBFF)
                .dust()
                .fluid()
                .iconSet(MaterialIconSet.SHINY)
                .buildAndRegister();

        // 月光树脂 MoonlightResin
        PollutionMaterials.MoonlightResin = new Material.Builder(id("moonlight_resin"))
                .color(0xA9B5F7)
                .fluid()
                .iconSet(MaterialIconSet.DULL)
                .buildAndRegister();

        PollutionMaterials.ArcaneInk = new Material.Builder(id("arcane_ink"))
                .color(0x241035)
                .fluid()
                .iconSet(MaterialIconSet.DULL)
                .buildAndRegister();
    }

    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID, name);
    }
}
