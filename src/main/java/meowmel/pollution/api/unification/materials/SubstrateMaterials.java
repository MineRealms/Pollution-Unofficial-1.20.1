package meowmel.pollution.api.unification.materials;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import meowmel.pollution.Pollution;
import meowmel.pollution.api.unification.PollutionMaterials;
import net.minecraft.resources.ResourceLocation;

/**
 * Substrate / catalyst chemistry materials (from the upstream
 * {@code ThaumcraftRecipes} chain).
 *
 * <p>Upstream defined these in its second-degree material batch with its own
 * colours; the port registers them with approximate colours because the
 * upstream definitions were not part of the carried-over field table. The
 * substrate family (basic/advanced/hyper) are fluids, the rest are dusts/gems.</p>
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
                .color(0x6FD6C4).gem().iconSet(MaterialIconSet.SHINY).buildAndRegister();
        PollutionMaterials.Syrmorite = new Material.Builder(id("syrmorite"))
                .color(0x4A5D6A).dust().iconSet(MaterialIconSet.METALLIC).buildAndRegister();
        PollutionMaterials.Octine = new Material.Builder(id("octine"))
                .color(0xC46A2A).dust().iconSet(MaterialIconSet.METALLIC).buildAndRegister();
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
