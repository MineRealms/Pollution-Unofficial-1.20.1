package meowmel.pollution.api.unification.materials;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import meowmel.pollution.Pollution;
import meowmel.pollution.api.unification.PollutionElements;
import meowmel.pollution.api.unification.PollutionMaterials;
import net.minecraft.resources.ResourceLocation;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.CRYSTALLIZABLE;

/**
 * The six base aspect materials.
 *
 * <p>Ported from upstream {@code meowmel.pollution.api.unification.materials.ElementMaterials}.
 * Colors, shapes, icon set and element symbols are unchanged.</p>
 *
 * <p>Upstream-only flag {@code GTQTMaterialFlags.GENERATE_BOULE} has no equivalent in
 * GregTech CEu Modern and is intentionally dropped (see MIGRATION_TRACKER).</p>
 */
public final class ElementMaterials {

    private ElementMaterials() {}

    public static void register() {
        PollutionMaterials.InfusedAir = new Material.Builder(id("infused_air"))
                .color(0xFEFE7D)
                .ore().dust().fluid().gem()
                .flags(CRYSTALLIZABLE)
                .iconSet(MaterialIconSet.SHINY)
                .element(PollutionElements.Ae)
                .buildAndRegister();

        PollutionMaterials.InfusedFire = new Material.Builder(id("infused_fire"))
                .color(0xFE3C01)
                .ore().dust().fluid().gem()
                .flags(CRYSTALLIZABLE)
                .iconSet(MaterialIconSet.SHINY)
                .element(PollutionElements.Ig)
                .buildAndRegister();

        PollutionMaterials.InfusedWater = new Material.Builder(id("infused_water"))
                .color(0x0090FF)
                .ore().dust().fluid().gem()
                .flags(CRYSTALLIZABLE)
                .iconSet(MaterialIconSet.SHINY)
                .element(PollutionElements.Aq)
                .buildAndRegister();

        PollutionMaterials.InfusedEarth = new Material.Builder(id("infused_earth"))
                .color(0x00A000)
                .ore().dust().fluid().gem()
                .flags(CRYSTALLIZABLE)
                .iconSet(MaterialIconSet.SHINY)
                .element(PollutionElements.Ter)
                .buildAndRegister();

        PollutionMaterials.InfusedEntropy = new Material.Builder(id("infused_entropy"))
                .color(0x43435E)
                .ore().dust().fluid().gem()
                .flags(CRYSTALLIZABLE)
                .iconSet(MaterialIconSet.SHINY)
                .element(PollutionElements.Pe)
                .buildAndRegister();

        PollutionMaterials.InfusedOrder = new Material.Builder(id("infused_order"))
                .color(0xEECCFF)
                .ore().dust().fluid().gem()
                .flags(CRYSTALLIZABLE)
                .iconSet(MaterialIconSet.SHINY)
                .element(PollutionElements.Ord)
                .buildAndRegister();
    }

    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID, name);
    }
}
