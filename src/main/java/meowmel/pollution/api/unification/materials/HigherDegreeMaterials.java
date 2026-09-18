package meowmel.pollution.api.unification.materials;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.BlastProperty.GasTier;
import meowmel.pollution.Pollution;
import meowmel.pollution.api.unification.PollutionMaterials;
import net.minecraft.resources.ResourceLocation;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.DECOMPOSITION_BY_CENTRIFUGING;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.GENERATE_BOLT_SCREW;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.GENERATE_FRAME;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.GENERATE_GEAR;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.GENERATE_LONG_ROD;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.GENERATE_PLATE;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.GENERATE_RING;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.GENERATE_ROD;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.GENERATE_ROTOR;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.GENERATE_ROUND;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.GENERATE_SMALL_GEAR;

/**
 * Partial port of upstream
 * {@code meowmel.pollution.api.unification.materials.HigherDegreeMaterials}.
 *
 * <p>The upstream file defines four materials. Ported here:</p>
 * <ul>
 *   <li>{@code AethericDarkSteel} - consumed by the forge-alchemy
 *       {@code catalyst/aetheric_dark_steel} recipe, the magic-GCYM advanced
 *       components, the node-fusion reactor casings and the Botania white-rune
 *       recipe.</li>
 *   <li>{@code IizunamaruElectrum} - consumed by the forge-alchemy
 *       {@code catalyst/iizunamaru_electrum} recipe, the philosopher-stone-2
 *       duplication, the magic-GCYM advanced components, the node-fusion
 *       reactor casings and the Botania starry-rune recipe.</li>
 * </ul>
 *
 * <p>Still unported: {@code Kobemetal} (no consumer in upstream or port) and
 * {@code BloodOfAvernus} (// 跳过: 整合包无 Blood Magic).</p>
 *
 * <p>Adaptations: upstream relied on {@code .ingot()} implying a dust form in
 * 1.12; GTCEu Modern does not, so {@code .dust()} is requested explicitly (the
 * forge-alchemy stone upgrades consume the dust). Tool/rotor stats are not
 * part of the upstream definitions.</p>
 */
public final class HigherDegreeMaterials {

    private HigherDegreeMaterials() {}

    public static void register() {
        // 太虚玄钢 AethericDarkSteel
        PollutionMaterials.AethericDarkSteel = new Material.Builder(id("aetheric_dark_steel"))
                .color(0x041B4E)
                .ingot().dust().fluid()
                .iconSet(MaterialIconSet.SHINY)
                .flags(GENERATE_BOLT_SCREW, GENERATE_RING, GENERATE_PLATE, GENERATE_ROTOR, GENERATE_ROD,
                        GENERATE_LONG_ROD, GENERATE_FRAME, GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_ROUND,
                        DECOMPOSITION_BY_CENTRIFUGING)
                .blast(7200, GasTier.HIGH)
                .buildAndRegister()
                .setFormula("䷜", true);

        // 光风霁月琥珀金 IizunamaruElectrum
        PollutionMaterials.IizunamaruElectrum = new Material.Builder(id("iizunamaru_electrum"))
                .color(0xF2FF2C)
                .ingot().dust().fluid()
                .iconSet(MaterialIconSet.SHINY)
                .flags(GENERATE_BOLT_SCREW, GENERATE_RING, GENERATE_PLATE, GENERATE_ROTOR, GENERATE_ROD,
                        GENERATE_LONG_ROD, GENERATE_FRAME, GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_ROUND,
                        DECOMPOSITION_BY_CENTRIFUGING)
                .blast(7200, GasTier.HIGH)
                .buildAndRegister()
                .setFormula("✦✧", true);
    }

    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID, name);
    }
}
