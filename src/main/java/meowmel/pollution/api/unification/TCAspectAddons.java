package meowmel.pollution.api.unification;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import dev.tc4port.thaumcraft.api.aspect.AspectApi;
import dev.tc4port.thaumcraft.api.aspect.AspectDefinition;
import dev.tc4port.thaumcraft.api.aspect.AspectId;
import meowmel.pollution.Pollution;
import meowmel.pollution.api.magic.PollutionAspectMapping;
import net.minecraftforge.fml.ModList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Addon aspect mapping, port of the upstream
 * {@code integration/thaumcraft/TCAspects} addon hook (the enum that ended in
 * {@code // todo addons?}).
 *
 * <p>Upstream's {@code TCAspects} was a plain enum with a raw {@code tcAspect}
 * field; the 1.20.1 port's core mapping lives in
 * {@link PollutionAspectMapping}, which resolves materials to TC4R
 * {@link AspectId}s through the registry. This class adds the two id sets that
 * the addons actually expose, verified in {@code docs/PHASE6C_API.md}:</p>
 * <ul>
 *   <li>{@code TC4AspectIds} (Forbidden Magic) - 30 core aspect constants.
 *       The 18 that have a Pollution infused material are mapped here; the
 *       remaining 12 ({@code VENENUM}, {@code ITER}, {@code LIMUS},
 *       {@code CORPUS}, {@code MESSIS}, {@code PANNUS}, ...) have no Pollution
 *       counterpart and stay unmapped.</li>
 *   <li>{@code ForbiddenAspects} (Forbidden Magic) - the seven sin aspects.
 *       They have no Pollution material of their own, so each sin is resolved
 *       through its aspect components to the Pollution materials of those
 *       components (e.g. {@code INFERNUS} = {@code IGNIS} + {@code PRAECANTATIO}
 *       -&gt; {@code InfusedFire} + {@code InfusedMagic}). Component lists are
 *       read from the TC4R registry when it is available, otherwise the
 *       fallbacks below mirror the addon's own
 *       {@code data/forbidden_magic/thaumcraft/aspects/*.json}. Sins whose
 *       components all lack a Pollution material (e.g. {@code LUXURIA} =
 *       {@code CORPUS} + {@code FAMES}) resolve to no material and are
 *       omitted.</li>
 * </ul>
 *
 * <p>The other three addons expose no aspect ids: Tainted Magic ships focus
 * actions and wand parts, Thaumic Energistics ships storage/vis services and
 * Thaumic Tinkerer ships the osmotic enchantment API (all non-aspect), so there
 * is nothing to map here for them.</p>
 *
 * <p>Called from {@code PollutionMaterialEvents} after
 * {@link PollutionAspectMapping#init()}. Forbidden Magic is an optional runtime
 * dependency of the port, so the addon constants are only touched behind a
 * {@code ModList} guard (the nested holder class is loaded lazily).</p>
 */
public final class TCAspectAddons {

    private static final Map<AspectId, List<Material>> ASPECT_TO_MATERIALS = new LinkedHashMap<>();
    private static final Map<Material, Set<AspectId>> MATERIAL_TO_ASPECTS = new LinkedHashMap<>();

    private TCAspectAddons() {}

    /** Called from {@code PollutionMaterialEvents#onMaterial} after the core mapping. */
    public static void init() {
        ASPECT_TO_MATERIALS.clear();
        MATERIAL_TO_ASPECTS.clear();
        if (ModList.get().isLoaded("forbidden_magic")) {
            ForbiddenMagicHooks.install();
        } else {
            Pollution.LOGGER.info("Forbidden Magic not loaded; addon aspect mapping stays empty");
        }
        logSummary();
    }

    private static void register(AspectId addonAspect, Material material) {
        if (addonAspect == null || material == null) {
            return;
        }
        List<Material> materials = ASPECT_TO_MATERIALS.computeIfAbsent(addonAspect, key -> new ArrayList<>());
        if (!materials.contains(material)) {
            materials.add(material);
        }
        MATERIAL_TO_ASPECTS.computeIfAbsent(material, key -> new LinkedHashSet<>()).add(addonAspect);
    }

    /** Resolves a sin aspect through its components; falls back to the verified addon JSON components. */
    private static void registerByComponents(AspectId addonAspect, AspectId... fallbackComponents) {
        List<AspectId> components = fallbackComponents.length == 0
                ? List.of()
                : List.of(fallbackComponents);
        AspectDefinition definition = AspectApi.get(addonAspect);
        if (definition != null && !definition.components().isEmpty()) {
            components = definition.components();
        }
        for (AspectId component : components) {
            PollutionAspectMapping.materialOf(component)
                    .ifPresent(material -> register(addonAspect, material));
        }
    }

    private static void logSummary() {
        if (ASPECT_TO_MATERIALS.isEmpty()) {
            return;
        }
        List<String> unknown = new ArrayList<>();
        if (!AspectApi.definitions().isEmpty()) {
            for (AspectId aspect : ASPECT_TO_MATERIALS.keySet()) {
                if (!AspectApi.contains(aspect)) {
                    unknown.add(aspect.serialized());
                }
            }
        }
        if (!unknown.isEmpty()) {
            Pollution.LOGGER.warn("Addon aspect mapping references ids missing from the TC4R registry: {}",
                    unknown);
        }
        Pollution.LOGGER.info("Mapped {} addon aspects to {} Pollution materials",
                ASPECT_TO_MATERIALS.size(), MATERIAL_TO_ASPECTS.size());
    }

    public static Optional<Material> materialOf(AspectId addonAspect) {
        List<Material> materials = ASPECT_TO_MATERIALS.get(addonAspect);
        return materials == null || materials.isEmpty() ? Optional.empty() : Optional.of(materials.get(0));
    }

    public static List<Material> materialsOf(AspectId addonAspect) {
        List<Material> materials = ASPECT_TO_MATERIALS.get(addonAspect);
        return materials == null ? List.of() : Collections.unmodifiableList(materials);
    }

    public static Set<AspectId> aspectsOf(Material material) {
        Set<AspectId> aspects = MATERIAL_TO_ASPECTS.get(material);
        return aspects == null ? Set.of() : Collections.unmodifiableSet(aspects);
    }

    public static int size() {
        return ASPECT_TO_MATERIALS.size();
    }

    /** Read-only view of the addon-aspect to materials mapping, in registration order. */
    public static Map<AspectId, List<Material>> all() {
        return Collections.unmodifiableMap(ASPECT_TO_MATERIALS);
    }

    /** Stable, human readable view used by debug output. */
    public static List<String> describe() {
        List<String> lines = new ArrayList<>();
        ASPECT_TO_MATERIALS.forEach((aspect, materials) -> {
            List<String> names = new ArrayList<>();
            for (Material material : materials) {
                names.add(material.getName());
            }
            lines.add(aspect.serialized() + " -> " + names);
        });
        return lines;
    }

    /**
     * Isolated holder so the Forbidden Magic classes are only loaded when the
     * guard in {@link #init()} passed.
     */
    private static final class ForbiddenMagicHooks {

        private ForbiddenMagicHooks() {}

        private static void install() {
            // Core aspects re-exported by Forbidden Magic's TC4AspectIds.
            TCAspectAddons.register(dev.tc4port.forbiddenmagic.integration.TC4AspectIds.AER,
                    PollutionMaterials.InfusedAir);
            TCAspectAddons.register(dev.tc4port.forbiddenmagic.integration.TC4AspectIds.TERRA,
                    PollutionMaterials.InfusedEarth);
            TCAspectAddons.register(dev.tc4port.forbiddenmagic.integration.TC4AspectIds.IGNIS,
                    PollutionMaterials.InfusedFire);
            TCAspectAddons.register(dev.tc4port.forbiddenmagic.integration.TC4AspectIds.AQUA,
                    PollutionMaterials.InfusedWater);
            TCAspectAddons.register(dev.tc4port.forbiddenmagic.integration.TC4AspectIds.ORDO,
                    PollutionMaterials.InfusedOrder);
            TCAspectAddons.register(dev.tc4port.forbiddenmagic.integration.TC4AspectIds.PERDITIO,
                    PollutionMaterials.InfusedEntropy);
            TCAspectAddons.register(dev.tc4port.forbiddenmagic.integration.TC4AspectIds.VACUOS,
                    PollutionMaterials.InfusedVoid);
            TCAspectAddons.register(dev.tc4port.forbiddenmagic.integration.TC4AspectIds.VITREUS,
                    PollutionMaterials.InfusedCrystal);
            TCAspectAddons.register(dev.tc4port.forbiddenmagic.integration.TC4AspectIds.VICTUS,
                    PollutionMaterials.InfusedLife);
            TCAspectAddons.register(dev.tc4port.forbiddenmagic.integration.TC4AspectIds.POTENTIA,
                    PollutionMaterials.InfusedEnergy);
            TCAspectAddons.register(dev.tc4port.forbiddenmagic.integration.TC4AspectIds.METALLUM,
                    PollutionMaterials.InfusedMetal);
            TCAspectAddons.register(dev.tc4port.forbiddenmagic.integration.TC4AspectIds.VOLATUS,
                    PollutionMaterials.InfusedFly);
            TCAspectAddons.register(dev.tc4port.forbiddenmagic.integration.TC4AspectIds.ALIENIS,
                    PollutionMaterials.InfusedAlien);
            TCAspectAddons.register(dev.tc4port.forbiddenmagic.integration.TC4AspectIds.PRAECANTATIO,
                    PollutionMaterials.InfusedMagic);
            TCAspectAddons.register(dev.tc4port.forbiddenmagic.integration.TC4AspectIds.AURAM,
                    PollutionMaterials.InfusedAura);
            TCAspectAddons.register(dev.tc4port.forbiddenmagic.integration.TC4AspectIds.HERBA,
                    PollutionMaterials.InfusedPlant);
            TCAspectAddons.register(dev.tc4port.forbiddenmagic.integration.TC4AspectIds.BESTIA,
                    PollutionMaterials.InfusedAnimal);
            TCAspectAddons.register(dev.tc4port.forbiddenmagic.integration.TC4AspectIds.SENSUS,
                    PollutionMaterials.InfusedSense);
            TCAspectAddons.register(dev.tc4port.forbiddenmagic.integration.TC4AspectIds.HUMANUS,
                    PollutionMaterials.InfusedHuman);
            TCAspectAddons.register(dev.tc4port.forbiddenmagic.integration.TC4AspectIds.INSTRUMENTUM,
                    PollutionMaterials.InfusedInstrument);
            TCAspectAddons.register(dev.tc4port.forbiddenmagic.integration.TC4AspectIds.LUCRUM,
                    PollutionMaterials.InfusedGreed);
            TCAspectAddons.register(dev.tc4port.forbiddenmagic.integration.TC4AspectIds.FABRICO,
                    PollutionMaterials.InfusedCraft);
            TCAspectAddons.register(dev.tc4port.forbiddenmagic.integration.TC4AspectIds.MACHINA,
                    PollutionMaterials.InfusedMechanics);
            TCAspectAddons.register(dev.tc4port.forbiddenmagic.integration.TC4AspectIds.EXANIMIS,
                    PollutionMaterials.InfusedUndead);

            // Sin aspects: resolved from their components. Fallbacks are the
            // component lists from the addon's own aspect JSONs.
            registerByComponents(dev.tc4port.forbiddenmagic.aspect.ForbiddenAspects.INFERNUS,
                    AspectId.parse("ignis"), AspectId.parse("praecantatio"));
            registerByComponents(dev.tc4port.forbiddenmagic.aspect.ForbiddenAspects.IRA,
                    AspectId.parse("telum"), AspectId.parse("ignis"));
            registerByComponents(dev.tc4port.forbiddenmagic.aspect.ForbiddenAspects.GULA,
                    AspectId.parse("fames"), AspectId.parse("vacuos"));
            registerByComponents(dev.tc4port.forbiddenmagic.aspect.ForbiddenAspects.INVIDIA,
                    AspectId.parse("sensus"), AspectId.parse("fames"));
            registerByComponents(dev.tc4port.forbiddenmagic.aspect.ForbiddenAspects.SUPERBIA,
                    AspectId.parse("volatus"), AspectId.parse("vacuos"));
            registerByComponents(dev.tc4port.forbiddenmagic.aspect.ForbiddenAspects.DESIDIA,
                    AspectId.parse("vinculum"), AspectId.parse("spiritus"));
            registerByComponents(dev.tc4port.forbiddenmagic.aspect.ForbiddenAspects.LUXURIA,
                    AspectId.parse("corpus"), AspectId.parse("fames"));
        }
    }
}
