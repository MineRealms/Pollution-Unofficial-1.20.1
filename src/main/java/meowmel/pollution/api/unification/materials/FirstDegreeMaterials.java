package meowmel.pollution.api.unification.materials;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.BlastProperty.GasTier;
import meowmel.pollution.Pollution;
import meowmel.pollution.api.unification.PollutionMaterials;
import net.minecraft.resources.ResourceLocation;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.DECOMPOSITION_BY_CENTRIFUGING;
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
import static com.gregtechceu.gtceu.common.data.GTMaterials.Silicon;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Silver;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Steel;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Thorium;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Tin;

/**
 * The six aspect alloys used by the magic multiblock parts.
 *
 * <p>Ported from upstream {@code meowmel.pollution.api.unification.materials.FirstDegreeMaterials}
 * (colors, component ratios and blast temperature are unchanged). Upstream depended on
 * GTQT-only materials for later entries of the same class; those stay TODO until the
 * magic lines they belong to are ported.</p>
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
    }

    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID, name);
    }
}
