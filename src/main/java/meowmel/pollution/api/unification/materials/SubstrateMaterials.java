package meowmel.pollution.api.unification.materials;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import meowmel.pollution.Pollution;
import meowmel.pollution.api.unification.PollutionMaterials;
import net.minecraft.resources.ResourceLocation;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ToolProperty;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

/**
 * Substrate chemistry and its ore-bearing catalyst materials.
 * <p>Syrmorite and Octine retain source ingot, fluid, ore, cable, pipe and tool forms.
 * Valonite retains its gem, fluid, ore and tool forms. Rotor percentages are converted
 * from the legacy rotor behavior formula; dust remains available for chemistry.</p>
 */
public final class SubstrateMaterials {

    private SubstrateMaterials() {}

    public static void register() {
        PollutionMaterials.Salisundus = new Material.Builder(id("salisundus"))
                .color(0xE8E8D0).dust().iconSet(MaterialIconSet.DULL).buildAndRegister();
        PollutionMaterials.Roughdraft = new Material.Builder(id("roughdraft"))
                .color(0x7A6A5A).dust().iconSet(MaterialIconSet.ROUGH).buildAndRegister();
        PollutionMaterials.Substrate = new Material.Builder(id("substrate"))
                .color(0x8FAF8F).dust().iconSet(MaterialIconSet.DULL).buildAndRegister();
        PollutionMaterials.Valonite = new Material.Builder(id("valonite"))
                .color(0xFFCCFF).gem().fluid().ore(true).iconSet(MaterialIconSet.EMERALD)
                .toolStats(ToolProperty.Builder.of(4, 4, 1024, 4).enchantability(10).build())
                .flags(GENERATE_PLATE, GENERATE_GEAR, GENERATE_ROD, GENERATE_LONG_ROD,
                        DECOMPOSITION_BY_CENTRIFUGING, GENERATE_BOLT_SCREW, GENERATE_FRAME, GENERATE_DENSE)
                .buildAndRegister();
        PollutionMaterials.Syrmorite = new Material.Builder(id("syrmorite"))
                .color(0x2414B3).ingot().fluid().ore(true).iconSet(MaterialIconSet.METALLIC)
                .components(Copper, 1, PollutionMaterials.InfusedEarth, 10, PollutionMaterials.InfusedOrder, 5)
                .toolStats(ToolProperty.Builder.of(2, 4, 384, 3).enchantability(10).build())
                // Legacy speed 6 -> efficiency 105%; damage 3 -> power 130%.
                .rotorStats(130, 105, 3, 512).fluidPipeProperties(1500, 50, true)
                .cableProperties(com.gregtechceu.gtceu.api.GTValues.V[2], 2, 2)
                .flags(GENERATE_PLATE, GENERATE_ROTOR, GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_ROD, GENERATE_LONG_ROD,
                        DECOMPOSITION_BY_CENTRIFUGING, GENERATE_BOLT_SCREW, GENERATE_RING, GENERATE_FRAME, GENERATE_DENSE, GENERATE_FOIL)
                .formula("CuTer5(TerOrd)5", true).buildAndRegister();
        PollutionMaterials.Octine = new Material.Builder(id("octine"))
                .color(0xFFAE33).ingot().fluid().ore(true).iconSet(MaterialIconSet.SHINY)
                .components(Iron, 1, PollutionMaterials.InfusedFire, 10, PollutionMaterials.InfusedAir, 5)
                .toolStats(ToolProperty.Builder.of(2, 4, 256, 3).enchantability(10).build())
                .rotorStats(130, 105, 3, 512).fluidPipeProperties(1250, 100, true)
                .cableProperties(com.gregtechceu.gtceu.api.GTValues.V[2], 4, 2)
                .flags(GENERATE_PLATE, GENERATE_ROTOR, GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_ROD, GENERATE_LONG_ROD,
                        DECOMPOSITION_BY_CENTRIFUGING, GENERATE_BOLT_SCREW, GENERATE_RING, GENERATE_FRAME, GENERATE_DENSE, GENERATE_FOIL)
                .formula("FeIg5(AeIg)5", true).buildAndRegister();
        PollutionMaterials.Thaummix = new Material.Builder(id("thaummix"))
                .color(0x7A4FA0).dust().iconSet(MaterialIconSet.DULL).buildAndRegister();
        PollutionMaterials.SulfoPlumbicSalt = new Material.Builder(id("sulfo_plumbic_salt"))
                .color(0xC8C8A0).dust().iconSet(MaterialIconSet.DULL).buildAndRegister();

        PollutionMaterials.BasicSubstrate = new Material.Builder(id("basic_substrate"))
                .color(0x6F8F6F).fluid().iconSet(MaterialIconSet.FLUID).buildAndRegister();
        PollutionMaterials.AdvancedSubstrate = new Material.Builder(id("advanced_substrate"))
                .color(0x4F6FAF).fluid().iconSet(MaterialIconSet.FLUID).buildAndRegister();
        PollutionMaterials.HyperSubstrate = new Material.Builder(id("hyper_substrate"))
                .color(0x9F4FAF).fluid().iconSet(MaterialIconSet.FLUID).buildAndRegister();
    }

    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID, name);
    }
}
