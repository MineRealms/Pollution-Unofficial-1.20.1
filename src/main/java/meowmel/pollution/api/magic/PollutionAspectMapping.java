package meowmel.pollution.api.magic;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import dev.tc4port.thaumcraft.api.aspect.AspectId;
import dev.tc4port.thaumcraft.api.aspect.VisChannel;
import meowmel.pollution.Pollution;
import meowmel.pollution.api.unification.PollutionMaterials;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Mapping between GregTech aspect materials and Thaumcraft 4R vis channels.
 *
 * <p>Ported semantics from upstream
 * {@code meowmel.pollution.api.utils.POAspectToGtFluidList}: every Thaumcraft
 * aspect corresponds to one {@code Infused*} material, the six primals mapping
 * to the six vis channels. The upstream class covered 35 aspects; only the six
 * primals exist in this phase, later phases add the remaining aspects
 * (crystal, metal, life, ...) together with their materials.</p>
 *
 * <p>Aspect IDs resolved through {@link VisChannel#aspectId()} keep the
 * {@code thaumcraft:} namespace used by the TC4R port
 * ({@code AspectId.parse("aer") -> thaumcraft:aer}).</p>
 */
public final class PollutionAspectMapping {

    private static final Map<Material, VisChannel> MATERIAL_TO_CHANNEL = new LinkedHashMap<>();
    private static final Map<AspectId, Material> ASPECT_TO_MATERIAL = new LinkedHashMap<>();

    private PollutionAspectMapping() {}

    /** Called from {@code PollutionMaterialEvents#onMaterial} after material creation. */
    public static void init() {
        MATERIAL_TO_CHANNEL.clear();
        ASPECT_TO_MATERIAL.clear();

        map(PollutionMaterials.InfusedAir, VisChannel.AER);
        map(PollutionMaterials.InfusedFire, VisChannel.IGNIS);
        map(PollutionMaterials.InfusedWater, VisChannel.AQUA);
        map(PollutionMaterials.InfusedEarth, VisChannel.TERRA);
        map(PollutionMaterials.InfusedEntropy, VisChannel.PERDITIO);
        map(PollutionMaterials.InfusedOrder, VisChannel.ORDO);

        Pollution.LOGGER.info("Mapped {} aspect materials to Thaumcraft 4R vis channels", MATERIAL_TO_CHANNEL.size());
    }

    private static void map(Material material, VisChannel channel) {
        if (material == null) {
            return;
        }
        MATERIAL_TO_CHANNEL.put(material, channel);
        ASPECT_TO_MATERIAL.put(channel.aspectId(), material);
    }

    public static Optional<VisChannel> channelOf(Material material) {
        return Optional.ofNullable(MATERIAL_TO_CHANNEL.get(material));
    }

    public static Optional<AspectId> aspectOf(Material material) {
        return channelOf(material).map(VisChannel::aspectId);
    }

    public static Optional<Material> materialOf(AspectId aspect) {
        return Optional.ofNullable(ASPECT_TO_MATERIAL.get(aspect));
    }

    public static Optional<Material> materialOf(VisChannel channel) {
        return materialOf(channel.aspectId());
    }

    public static int size() {
        return MATERIAL_TO_CHANNEL.size();
    }

    /** Stable, human readable view used by the debug command. */
    public static List<String> describe() {
        List<String> lines = new ArrayList<>();
        MATERIAL_TO_CHANNEL.forEach((material, channel) -> lines.add(
                material.getName() + " -> " + channel.aspectId().serialized() + " (" + channel.name() + ")"));
        return lines;
    }
}
