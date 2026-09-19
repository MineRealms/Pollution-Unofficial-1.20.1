package meowmel.pollution.loaders.recipes;

import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.utils.GTUtil;
import meowmel.pollution.Pollution;
import meowmel.pollution.common.block.PollutionPlantBlocks;
import meowmel.pollution.common.machine.multiblock.botania.BotaniaRecipeMaps;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Consumer;

/**
 * Botania-flavoured conversions for the Alfheim/rainbow world-gen plants that
 * the refreshed audit still listed as recipe-less. The plants themselves stay
 * world-gen content; these recipes are the magical "seeding" alternatives, so
 * they can also be produced in the ported Botania recipe machines
 * ({@code MANA_INFUSION_RECIPES} on the Mana Infusion Reactor and
 * {@code PURE_DAISY_RECIPES} on the Industrial Pure Daisy).
 *
 * <p><b>Recipe choices</b></p>
 * <ul>
 *   <li>{@code alfheim_elven_sand}: pure daisy conversion of vanilla sand,
 *       mirroring Botania's own sand recipes.</li>
 *   <li>{@code rainbow_sapling}: conjuration infusion of Botania mana powder;
 *       {@code rainbow_leaves}: alchemy infusion of livingwood.</li>
 *   <li>{@code alfheim_dream_leaves}: alchemy infusion of dreamwood, the
 *       Alfheim tree material.</li>
 *   <li>The four grape stages: alchemy infusions of the matching Botania
 *       petals (white / red / pink / magenta).</li>
 * </ul>
 *
 * <p>All inputs are resolved through {@link ForgeRegistries} at recipe-build
 * time and any missing id skips the affected recipe with a warning; no new
 * items or machine classes are introduced.</p>
 */
public final class PlantBlockRecipes {

    private PlantBlockRecipes() {}

    public static void init(Consumer<FinishedRecipe> provider) {
        int added = 0;
        if (pureDaisy(provider, "alfheim_elven_sand", "minecraft:sand",
                PollutionPlantBlocks.ALFHEIM_ELVEN_SAND.asStack())) {
            added++;
        }
        if (manaInfusion(provider, "rainbow_sapling", "botania:mana_powder",
                "botania:conjuration_catalyst", 10000, PollutionPlantBlocks.RAINBOW_SAPLING.asStack())) {
            added++;
        }
        if (manaInfusion(provider, "rainbow_leaves", "botania:livingwood_log",
                "botania:alchemy_catalyst", 5000, PollutionPlantBlocks.RAINBOW_LEAVES.asStack())) {
            added++;
        }
        if (manaInfusion(provider, "alfheim_dream_leaves", "botania:dreamwood_log",
                "botania:alchemy_catalyst", 10000, PollutionPlantBlocks.ALFHEIM_DREAM_LEAVES.asStack())) {
            added++;
        }
        if (manaInfusion(provider, "alfheim_white_grape", "botania:white_petal",
                "botania:alchemy_catalyst", 8000, PollutionPlantBlocks.ALFHEIM_WHITE_GRAPE.asStack())) {
            added++;
        }
        if (manaInfusion(provider, "alfheim_red_grape_0", "botania:red_petal",
                "botania:alchemy_catalyst", 4000, PollutionPlantBlocks.ALFHEIM_RED_GRAPE_0.asStack())) {
            added++;
        }
        if (manaInfusion(provider, "alfheim_red_grape_1", "botania:pink_petal",
                "botania:alchemy_catalyst", 8000, PollutionPlantBlocks.ALFHEIM_RED_GRAPE_1.asStack())) {
            added++;
        }
        if (manaInfusion(provider, "alfheim_red_grape_2", "botania:magenta_petal",
                "botania:alchemy_catalyst", 12000, PollutionPlantBlocks.ALFHEIM_RED_GRAPE_2.asStack())) {
            added++;
        }
        Pollution.LOGGER.info("[plant] registered {} alfheim/rainbow plant recipes", added);
    }

    private static boolean pureDaisy(Consumer<FinishedRecipe> provider, String id, String input,
                                     ItemStack output) {
        ItemStack inputStack = stack(input, 1);
        if (inputStack.isEmpty() || output.isEmpty()) {
            Pollution.LOGGER.warn("[plant] skipping pure daisy {}: the input or output is missing", id);
            return false;
        }
        GTRecipeBuilder.of(recipeId("pure_daisy", id), BotaniaRecipeMaps.PURE_DAISY_RECIPES)
                .inputItems(inputStack)
                .outputItems(output)
                .duration(200)
                .EUt(100)
                .save(provider);
        return true;
    }

    private static boolean manaInfusion(Consumer<FinishedRecipe> provider, String id, String input,
                                        String catalyst, int mana, ItemStack output) {
        ItemStack inputStack = stack(input, 1);
        Block catalystBlock = ForgeRegistries.BLOCKS.getValue(parse(catalyst));
        if (inputStack.isEmpty() || catalystBlock == null || output.isEmpty()) {
            Pollution.LOGGER.warn("[plant] skipping mana infusion {}: the input, catalyst or output is missing", id);
            return false;
        }
        GTRecipeBuilder.of(recipeId("mana_infusion", id), BotaniaRecipeMaps.MANA_INFUSION_RECIPES)
                .inputItems(inputStack)
                .notConsumable(new ItemStack(catalystBlock))
                .outputItems(output)
                .duration(200 * GTUtil.getTierByVoltage(mana))
                .EUt(mana)
                .save(provider);
        return true;
    }

    private static ItemStack stack(String id, int count) {
        Item item = ForgeRegistries.ITEMS.getValue(parse(id));
        return item == null ? ItemStack.EMPTY : new ItemStack(item, count);
    }

    /** Parses a full {@code namespace:path} id; the deprecated constructor is isolated here. */
    @SuppressWarnings("removal")
    private static ResourceLocation parse(String id) {
        return new ResourceLocation(id);
    }

    private static ResourceLocation recipeId(String category, String id) {
        return ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID, "plant/" + category + "/" + id);
    }
}
