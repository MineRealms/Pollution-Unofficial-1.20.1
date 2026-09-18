package meowmel.pollution.compat.kubejs;

import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.integration.kjs.recipe.GTRecipeSchema;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.schema.RegisterRecipeSchemasEvent;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import meowmel.pollution.Pollution;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

/**
 * KubeJS integration entry point. Registered through
 * {@code src/main/resources/kubejs.plugins.txt}.
 *
 * <p>Verified against kubejs-forge 2001.6.5-build.16 and gtceu 7.5.3 with
 * javap; every overridden method below exists in those jars with the exact
 * signatures used here.</p>
 *
 * <ul>
 *   <li>{@link #registerEvents()} publishes the {@code PollutionEvents} group
 *       (see {@link PollutionEvents}).</li>
 *   <li>{@link #registerBindings(BindingsEvent)} binds the static
 *       {@link PollutionJS} facade as {@code Pollution} for server scripts.</li>
 *   <li>{@link #registerRecipeSchemas(RegisterRecipeSchemasEvent)} maps the
 *       generic GregTech recipe schema onto every {@code pollution:*} recipe
 *       type in GregTech's registry. GregTech's own KubeJS plugin already does
 *       this for all {@code GTRegistries.RECIPE_TYPES} entries
 *       ({@code GregTechKubeJSPlugin#registerRecipeSchemas} iterates the
 *       registry; KubeJS calls the hook lazily from
 *       {@code RecipeNamespace#getAll()}, after registries are populated).
 *       The explicit pass here is a guarantee, not a requirement; KubeJS
 *       overwrites duplicate ids, so the two registrations cannot
 *       conflict.</li>
 * </ul>
 */
public final class PollutionKubeJSPlugin extends KubeJSPlugin {

    @Override
    public void init() {
        Pollution.LOGGER.debug("Pollution KubeJS plugin loaded");
    }

    @Override
    public void registerEvents() {
        PollutionEvents.register();
    }

    @Override
    public void registerBindings(BindingsEvent event) {
        if (event.getType().isServer()) {
            event.add("Pollution", PollutionJS.class);
        }
    }

    @Override
    public void registerRecipeSchemas(RegisterRecipeSchemasEvent event) {
        // GTRecipeTypes.register() namespaces every id with "gtceu", so the
        // previous pollution-namespace filter never matched. Register the
        // generic GT schema for all GT recipe types instead.
        for (Map.Entry<ResourceLocation, GTRecipeType> entry : GTRegistries.RECIPE_TYPES.entries()) {
            event.register(entry.getKey(), GTRecipeSchema.SCHEMA);
        }
    }
}
