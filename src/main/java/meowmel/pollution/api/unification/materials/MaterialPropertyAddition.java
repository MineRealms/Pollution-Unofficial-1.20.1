package meowmel.pollution.api.unification.materials;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.OreProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import meowmel.pollution.api.unification.PollutionMaterials;

import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

/**
 * Late material property additions.
 *
 * <p>Port of upstream {@code meowmel.pollution.api.unification.materials.MaterialPropertyAddition}.
 * The upstream body had two parts:</p>
 * <ul>
 *   <li>The four ore-property blocks (Pyrargyrite, PlutoZinc, AuthorityLead,
 *       MeltGold) are ported verbatim; the materials are registered by
 *       {@link OreMaterials} immediately before this class runs.</li>
 *   <li>{@code GTQTMaterials.Thaumium} dust/tool properties are skipped:
 *       GTCEu 7.5.3 has no {@code Thaumium} material. The port's Thaumcraft
 *       chemistry already substitutes {@code StainlessSteel} for it
 *       ({@code ThaumcraftRecipes}).</li>
 *   <li>The commented-out Okin/Octahedrite block stays commented out
 *       upstream and is not ported.</li>
 * </ul>
 */
public final class MaterialPropertyAddition {

    private MaterialPropertyAddition() {}

    public static void init() {
        oreProperties(PollutionMaterials.Pyrargyrite, SodiumPersulfate, Silver,
                Silver, Sulfur, Antimony);
        oreProperties(PollutionMaterials.PlutoZinc, SodiumPersulfate, Zinc,
                Silver, Sulfur, Tin);
        oreProperties(PollutionMaterials.AuthorityLead, Water, Lead,
                Silver, Lead, Iron);
        oreProperties(PollutionMaterials.MeltGold, Water, Gold,
                Gold, Silver, Lead);
    }

    private static void oreProperties(Material material, Material washedIn, Material directSmelt,
                                      Material... byProducts) {
        if (material == null) {
            return;
        }
        OreProperty property = material.getProperty(PropertyKey.ORE);
        if (property == null) {
            return;
        }
        property.setOreByProducts(byProducts);
        property.setWashedIn(washedIn);
        property.setDirectSmeltResult(directSmelt);
    }
}
