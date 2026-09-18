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
 * CelestialBiologicalMedium, ArcaneInk, StarryArcaneAlloy). Only
 * {@code ArcaneInk} is ported: it is the sole missing dependency of the arcane
 * ink capsule recipe in {@code MagicIntegrationRecipes} (glass bottle + ink ->
 * capsule), see {@code docs/MATERIALS_AUDIT.md}.</p>
 *
 * <p>The other six stay unported because every recipe that consumes them also
 * needs Astral Sorcery or Blood Magic content, which the port does not depend
 * on. The upstream ArcaneInk production (starlight pollen + salis mundus +
 * moonlight resin) needs Astral Sorcery as well, so the ported material
 * currently has no in-port source; the capsule recipe is portable but its
 * input has to be supplied by a later integration phase or a substitution.</p>
 */
public final class MagicIntegrationMaterials {

    private MagicIntegrationMaterials() {}

    public static void register() {
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
