package meowmel.pollution.client;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.integration.jei.recipe.GTRecipeJEICategory;
import meowmel.pollution.Pollution;
import meowmel.pollution.api.recipes.PORecipeMaps;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.api.distmarker.Dist;

import java.util.List;

/**
 * TEMPORARY diagnostic: dumps, after the client recipe sync, how many recipes of
 * each custom Pollution recipe type are (i) present in the client RecipeManager,
 * (ii) present in the GT category map JEI reads and (iii) actually held by JEI.
 * Must be deleted after use.
 */
@Mod.EventBusSubscriber(modid = Pollution.MOD_ID, value = Dist.CLIENT)
public final class DebugRecipeDump {

    private static boolean dumpedRecipes;
    private static boolean dumpedJei;

    private DebugRecipeDump() {}

    private static List<GTRecipeType> types() {
        return List.of(
                PORecipeMaps.MAGIC_ALLOY_BLAST_RECIPES,
                PORecipeMaps.STOVE_RECIPES,
                PORecipeMaps.MAGIC_FUSION_REACTOR,
                PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES,
                PORecipeMaps.MAGIC_ASSEMBLER_RECIPES,
                PORecipeMaps.MAGIC_GREENHOUSE_RECIPES,
                PORecipeMaps.MAGIC_TURBINE_FUELS,
                PORecipeMaps.FORGE_ALCHEMY_RECIPES,
                PORecipeMaps.NODE_MAGIC_FUSION_RECIPES,
                PORecipeMaps.INDUSTRIAL_INFUSION_RECIPES,
                GTRecipeTypes.ASSEMBLER_RECIPES,
                GTRecipeTypes.CHEMICAL_RECIPES);
    }

    @SubscribeEvent
    public static void onRecipesUpdated(RecipesUpdatedEvent event) {
        try {
            RecipeManager manager = event.getRecipeManager();
            Pollution.LOGGER.info("[jei-debug] RecipesUpdated dump begin");
            for (GTRecipeType type : types()) {
                if (type == null) {
                    continue;
                }
                int synced = manager.getAllRecipesFor(type).size();
                int categorized = type.getRecipesInCategory(type.getCategory()).size();
                boolean categoryRegistered = GTRegistries.RECIPE_CATEGORIES
                        .get(type.getCategory().registryKey) != null;
                Pollution.LOGGER.info("[jei-debug] type={} synced={} categorized={} categoryRegistered={}",
                        type.registryName, synced, categorized, categoryRegistered);
            }
            Pollution.LOGGER.info("[jei-debug] RecipesUpdated dump end");
        } catch (Throwable throwable) {
            Pollution.LOGGER.error("[jei-debug] recipe dump failed", throwable);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || dumpedJei) {
            return;
        }
        var runtime = DebugJeiRuntimePlugin.RUNTIME;
        if (runtime == null) {
            return;
        }
        dumpedJei = true;
        try {
            Pollution.LOGGER.info("[jei-debug] JEI dump begin (categories={})",
                    runtime.getRecipeManager().createRecipeCategoryLookup().get().count());
            for (GTRecipeType type : types()) {
                if (type == null) {
                    continue;
                }
                RecipeType<GTRecipe> recipeType = GTRecipeJEICategory.TYPES.apply(type.getCategory());
                var category = runtime.getRecipeManager().getRecipeCategory(recipeType);
                long jeiCount = runtime.getRecipeManager().createRecipeLookup(recipeType).get().count();
                long catalysts = runtime.getRecipeManager().createRecipeCatalystLookup(recipeType).get().count();
                Pollution.LOGGER.info("[jei-debug] type={} jeiCategory={} jeiRecipes={} jeiCatalysts={}",
                        type.registryName, category != null, jeiCount, catalysts);
            }
            Pollution.LOGGER.info("[jei-debug] JEI dump end");
        } catch (Throwable throwable) {
            Pollution.LOGGER.error("[jei-debug] JEI dump failed", throwable);
        }
    }
}
