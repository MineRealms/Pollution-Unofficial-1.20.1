package meowmel.pollution.api.unification.materials;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import meowmel.pollution.Pollution;
import meowmel.pollution.api.unification.PollutionElements;
import meowmel.pollution.api.unification.PollutionMaterials;
import net.minecraft.resources.ResourceLocation;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.CRYSTALLIZABLE;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.DECOMPOSITION_BY_CENTRIFUGING;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.GENERATE_FRAME;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.GENERATE_GEAR;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.GENERATE_LONG_ROD;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.GENERATE_PLATE;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.GENERATE_ROD;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.GENERATE_ROTOR;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.GENERATE_ROUND;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.GENERATE_SMALL_GEAR;

/**
 * The six base aspect materials plus the four nexus/sentience metals.
 *
 * <p>Ported from upstream {@code meowmel.pollution.api.unification.materials.ElementMaterials}.
 * Colors, shapes, icon set and element symbols are unchanged.</p>
 *
 * <p>Upstream-only flag {@code GTQTMaterialFlags.GENERATE_BOULE} has no equivalent in
 * GregTech CEu Modern and is intentionally dropped (see MIGRATION_TRACKER).</p>
 *
 * <p>The second batch ({@code SentientMetal}, {@code BindingMetal},
 * {@code ExistingNexus}, {@code FadingNexus}) was added to unblock the
 * forge-alchemy stone upgrades, the node-fusion fuels and the magic-GCYM
 * advanced components. Upstream relied on {@code .ingot()} implying a dust
 * form; GTCEu Modern does not, so {@code .dust()} is requested explicitly
 * (the upstream recipes use the dust form in the stone upgrades and in the
 * sentient-metal/nexus recipes).</p>
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

        // 感知金属 SentientMetal
        PollutionMaterials.SentientMetal = new Material.Builder(id("sentient_metal"))
                .color(0x55FFFA)
                .ingot().dust().fluid()
                .iconSet(MaterialIconSet.BRIGHT)
                .flags(GENERATE_PLATE, GENERATE_ROTOR, GENERATE_ROD, GENERATE_LONG_ROD, GENERATE_FRAME,
                        GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_ROUND, DECOMPOSITION_BY_CENTRIFUGING)
                .element(PollutionElements.Sen)
                .buildAndRegister();

        // 缚束金属 BindingMetal
        PollutionMaterials.BindingMetal = new Material.Builder(id("binding_metal"))
                .color(0xDA1D0F)
                .ingot().dust().fluid()
                .iconSet(MaterialIconSet.SHINY)
                .flags(GENERATE_PLATE, GENERATE_ROTOR, GENERATE_ROD, GENERATE_LONG_ROD, GENERATE_FRAME,
                        GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_ROUND, DECOMPOSITION_BY_CENTRIFUGING)
                .element(PollutionElements.Bin)
                .buildAndRegister();

        // 既存之枢 ExistingNexus
        PollutionMaterials.ExistingNexus = new Material.Builder(id("existing_nexus"))
                .color(0xC0C0C0)
                .ingot().dust().fluid()
                .iconSet(MaterialIconSet.BRIGHT)
                .flags(GENERATE_PLATE, GENERATE_ROTOR, GENERATE_ROD, GENERATE_LONG_ROD, GENERATE_FRAME,
                        GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_ROUND, DECOMPOSITION_BY_CENTRIFUGING)
                .element(PollutionElements.Exn)
                .buildAndRegister();

        // 消逝之枢 FadingNexus
        PollutionMaterials.FadingNexus = new Material.Builder(id("fading_nexus"))
                .color(0x404040)
                .ingot().dust().fluid()
                .iconSet(MaterialIconSet.SHINY)
                .flags(GENERATE_PLATE, GENERATE_ROTOR, GENERATE_ROD, GENERATE_LONG_ROD, GENERATE_FRAME,
                        GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_ROUND, DECOMPOSITION_BY_CENTRIFUGING)
                .element(PollutionElements.Fan)
                .buildAndRegister();
    }

    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID, name);
    }
}
