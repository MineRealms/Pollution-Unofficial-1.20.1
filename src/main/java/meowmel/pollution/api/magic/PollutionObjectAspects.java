package meowmel.pollution.api.magic;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import dev.tc4port.thaumcraft.api.aspect.AspectId;
import dev.tc4port.thaumcraft.common.ObjectAspectCatalog;
import meowmel.pollution.Pollution;
import meowmel.pollution.api.unification.PollutionMaterials;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Registers Thaumcraft 4R object aspects for the GT items of the Pollution
 * aspect materials, through the official runtime API
 * ({@code ObjectAspectCatalog.registerRuntime}).
 *
 * <p>Upstream declared {@code ThaumcraftModule.registerAspectsToItem(...)} but
 * never called it, so no upstream aspect values exist for these materials. The
 * values below are an explicit port design rule modelled on TC4R's own
 * {@code object_aspects} data (iron ingot {@code metallum 4}, gold ingot
 * {@code metallum 3 + lucrum 2}, native clusters primary 5 + secondary 2):</p>
 *
 * <ul>
 *   <li>gem/dust of an aspect material: its own aspect at 4</li>
 *   <li>ingot of an aspect alloy: its own aspect at 3 plus {@code metallum 2}</li>
 * </ul>
 *
 * <p>Ore blocks are not registered here yet: GT generates them per stone type
 * ({@code pollution:red_granite_infused_fire_ore}, ...) and tag based
 * registration should be verified separately. Tracked as TODO.</p>
 */
public final class PollutionObjectAspects {

    private static final List<Runnable> REGISTRATIONS = new ArrayList<>();

    private PollutionObjectAspects() {}

    public static void register() {
        for (Runnable previous : REGISTRATIONS) {
            previous.run();
        }
        REGISTRATIONS.clear();

        int registered = 0;
        for (Map.Entry<Material, AspectId> entry : PollutionAspectMapping.all().entrySet()) {
            Material material = entry.getKey();
            AspectId aspect = entry.getValue();
            if (material.hasProperty(PropertyKey.INGOT)) {
                registered += register(material, TagPrefix.ingot,
                        Map.of(aspect, 3, AspectId.parse("metallum"), 2));
            }
            if (material.hasProperty(PropertyKey.GEM)) {
                registered += register(material, TagPrefix.gem, Map.of(aspect, 4));
                registered += register(material, TagPrefix.dust, Map.of(aspect, 4));
            }
        }

        // Aspect alloys: the six element alloys carry the primal affinity named
        // by their own aspect (aertitanium -> aer, ignissteel -> ignis, ...).
        // They stay outside PollutionAspectMapping because that mapping means
        // "aspect fuel material" upstream.
        registered += registerAlloyIngot(PollutionMaterials.Aertitanium, "aer");
        registered += registerAlloyIngot(PollutionMaterials.IgnisSteel, "ignis");
        registered += registerAlloyIngot(PollutionMaterials.Aquasilver, "aqua");
        registered += registerAlloyIngot(PollutionMaterials.Terracopper, "terra");
        registered += registerAlloyIngot(PollutionMaterials.Ordolead, "ordo");
        registered += registerAlloyIngot(PollutionMaterials.Perditioaluminium, "perditio");

        Pollution.LOGGER.info("Registered {} TC4R object aspect entries for Pollution materials", registered);
    }

    private static int registerAlloyIngot(Material material, String aspect) {
        if (material == null) {
            return 0;
        }
        return register(material, TagPrefix.ingot, Map.of(AspectId.parse(aspect), 3, AspectId.parse("metallum"), 2));
    }

    private static int register(Material material, TagPrefix prefix, Map<AspectId, Integer> aspects) {
        ItemStack stack = ChemicalHelper.get(prefix, material);
        if (stack.isEmpty()) {
            return 0;
        }
        REGISTRATIONS.add(ObjectAspectCatalog.registerRuntime(stack.getItem(), aspects));
        return 1;
    }
}
