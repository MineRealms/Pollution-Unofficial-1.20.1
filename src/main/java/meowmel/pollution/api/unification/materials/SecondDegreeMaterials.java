package meowmel.pollution.api.unification.materials;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.fluids.FluidBuilder;
import meowmel.pollution.Pollution;
import meowmel.pollution.api.unification.PollutionMaterials;
import net.minecraft.resources.ResourceLocation;

/**
 * Partial port of upstream {@code meowmel.pollution.api.unification.materials.SecondDegreeMaterials}.
 *
 * <p>The upstream file defines 61 second-degree materials. Only the subset that
 * is the sole missing dependency of still-skipped recipes is ported here (see
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
 *       chain) is still blocked by GTQT materials.</li>
 * </ul>
 *
 * <p>Definitions follow the upstream colours, forms and formula. Everything
 * else of the upstream file (alchemical residue/vapour chains, battery hulls,
 * hachimi chain, kqt intermediates, ...) stays unported: those recipes are
 * blocked by more than the material definitions and are documented in the
 * audit.</p>
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
                .liquid(new FluidBuilder().block())
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
    }

    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID, name);
    }
}
