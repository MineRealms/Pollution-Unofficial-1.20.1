package meowmel.pollution.api.magic;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import dev.tc4port.thaumcraft.api.aspect.AspectApi;
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
 * Mapping between GregTech aspect materials and the Thaumcraft 4R aspect graph.
 *
 * <p>Ported semantics from upstream
 * {@code meowmel.pollution.api.utils.POAspectToGtFluidList}: every Thaumcraft
 * aspect corresponds to one {@code Infused*} material. The primals map onto the
 * six vis channels; compounds map onto {@link AspectId}s of the TC4R registry.</p>
 *
 * <p>Names follow the TC4R core registry
 * ({@code data/thaumcraft/thaumcraft/aspects/default.json} inside the TC4R jar),
 * not the TC6 names used upstream. Three upstream aspects have no TC4R core
 * counterpart and are intentionally left unmapped:
 * {@code ALCHEMY}, {@code SPATIO}, {@code TEMPUS}, {@code TINCTURA}
 * (upstream also used the TC6-only names {@code desiderium}/{@code aversio};
 * their materials use the TC4R ids {@code lucrum}/{@code telum}).</p>
 */
public final class PollutionAspectMapping {

    private static final Map<Material, AspectId> MATERIAL_TO_ASPECT = new LinkedHashMap<>();
    private static final Map<AspectId, Material> ASPECT_TO_MATERIAL = new LinkedHashMap<>();
    private static final Map<Material, VisChannel> MATERIAL_TO_CHANNEL = new LinkedHashMap<>();

    private PollutionAspectMapping() {}

    /** Called from {@code PollutionMaterialEvents#onMaterial} after material creation. */
    public static void init() {
        MATERIAL_TO_ASPECT.clear();
        ASPECT_TO_MATERIAL.clear();
        MATERIAL_TO_CHANNEL.clear();

        // Primals: direct vis channels.
        map(PollutionMaterials.InfusedAir, VisChannel.AER);
        map(PollutionMaterials.InfusedFire, VisChannel.IGNIS);
        map(PollutionMaterials.InfusedWater, VisChannel.AQUA);
        map(PollutionMaterials.InfusedEarth, VisChannel.TERRA);
        map(PollutionMaterials.InfusedEntropy, VisChannel.PERDITIO);
        map(PollutionMaterials.InfusedOrder, VisChannel.ORDO);

        // Compounds: TC4R aspect id (upstream aspect name in comment).
        map(PollutionMaterials.InfusedCrystal, "vitreus");        // CRYSTAL
        map(PollutionMaterials.InfusedLife, "victus");            // LIFE
        map(PollutionMaterials.InfusedDeath, "mortuus");          // DEATH
        map(PollutionMaterials.InfusedSoul, "spiritus");          // SOUL
        map(PollutionMaterials.InfusedWeapon, "telum");           // upstream AVERSION
        map(PollutionMaterials.InfusedMetal, "metallum");         // METAL
        map(PollutionMaterials.InfusedEnergy, "potentia");        // ENERGY
        map(PollutionMaterials.InfusedInstrument, "instrumentum");// TOOL
        map(PollutionMaterials.InfusedExchange, "permutatio");    // EXCHANGE
        map(PollutionMaterials.InfusedMagic, "praecantatio");     // MAGIC
        // InfusedAlchemy: TC4R core has no "alkimia" aspect.
        map(PollutionMaterials.InfusedCold, "gelum");             // COLD
        map(PollutionMaterials.InfusedAura, "auram");             // AURA
        map(PollutionMaterials.InfusedLight, "lux");              // LIGHT
        map(PollutionMaterials.InfusedCraft, "fabrico");          // CRAFT
        map(PollutionMaterials.InfusedVoid, "vacuos");            // VOID
        map(PollutionMaterials.InfusedMotion, "motus");           // MOTION
        map(PollutionMaterials.InfusedTaint, "vitium");           // FLUX
        map(PollutionMaterials.InfusedDark, "tenebrae");          // DARKNESS
        map(PollutionMaterials.InfusedAlien, "alienis");          // ELDRITCH
        map(PollutionMaterials.InfusedFly, "volatus");            // FLIGHT
        map(PollutionMaterials.InfusedPlant, "herba");            // PLANT
        map(PollutionMaterials.InfusedMechanics, "machina");      // MECHANISM
        map(PollutionMaterials.InfusedTrap, "vinculum");          // TRAP
        map(PollutionMaterials.InfusedUndead, "exanimis");        // UNDEAD
        map(PollutionMaterials.InfusedThought, "cognitio");       // MIND
        map(PollutionMaterials.InfusedSense, "sensus");           // SENSES
        map(PollutionMaterials.InfusedAnimal, "bestia");          // BEAST
        map(PollutionMaterials.InfusedHuman, "humanus");          // MAN
        map(PollutionMaterials.InfusedGreed, "lucrum");           // upstream DESIRE
        map(PollutionMaterials.InfusedArmor, "tutamen");          // upstream PROTECT
        // InfusedSpatio / InfusedTempus / InfusedTinctura: Planar Artifice aspects, absent from TC4R core.

        validateAgainstRegistry();
    }

    private static void validateAgainstRegistry() {
        List<String> unknown = new ArrayList<>();
        for (Map.Entry<Material, AspectId> entry : MATERIAL_TO_ASPECT.entrySet()) {
            if (!AspectApi.contains(entry.getValue())) {
                unknown.add(entry.getKey().getName() + " -> " + entry.getValue().serialized());
            }
        }
        if (!unknown.isEmpty()) {
            Pollution.LOGGER.warn("Aspect mapping references ids missing from the TC4R registry: {}", unknown);
        }
        Pollution.LOGGER.info("Mapped {} aspect materials to Thaumcraft 4R aspects ({} vis channels)",
                MATERIAL_TO_ASPECT.size(), MATERIAL_TO_CHANNEL.size());
    }

    private static void map(Material material, VisChannel channel) {
        map(material, channel.aspectId());
        if (material != null) {
            MATERIAL_TO_CHANNEL.put(material, channel);
        }
    }

    private static void map(Material material, String aspect) {
        map(material, AspectId.parse(aspect));
    }

    private static void map(Material material, AspectId aspect) {
        if (material == null) {
            return;
        }
        MATERIAL_TO_ASPECT.put(material, aspect);
        ASPECT_TO_MATERIAL.put(aspect, material);
    }

    public static Optional<VisChannel> channelOf(Material material) {
        return Optional.ofNullable(MATERIAL_TO_CHANNEL.get(material));
    }

    public static Optional<AspectId> aspectOf(Material material) {
        return Optional.ofNullable(MATERIAL_TO_ASPECT.get(material));
    }

    public static Optional<Material> materialOf(AspectId aspect) {
        return Optional.ofNullable(ASPECT_TO_MATERIAL.get(aspect));
    }

    public static Optional<Material> materialOf(VisChannel channel) {
        return materialOf(channel.aspectId());
    }

    public static int size() {
        return MATERIAL_TO_ASPECT.size();
    }

    /** Read-only view of the material to aspect mapping, in registration order. */
    public static Map<Material, AspectId> all() {
        return java.util.Collections.unmodifiableMap(MATERIAL_TO_ASPECT);
    }

    /** Stable, human readable view used by the debug command. */
    public static List<String> describe() {
        List<String> lines = new ArrayList<>();
        MATERIAL_TO_ASPECT.forEach((material, aspect) -> lines.add(
                material.getName() + " -> " + aspect.serialized()
                        + (VisChannel.fromAspectId(aspect).map(channel -> " [" + channel.name() + "]").orElse(""))));
        return lines;
    }
}
