package meowmel.pollution.compat.jei;

import meowmel.pollution.Pollution;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * JEI category that documents how the Pollution machines interact with the
 * environment (industrial pollution, Thaumcraft flux, aura and vis).
 *
 * <p>Implemented against the JEI 15.56 API: the category only needs
 * {@code getRecipeType}/{@code getTitle}/{@code getIcon}/{@code setRecipe};
 * the text is drawn in the {@code draw} hook of
 * {@link AbstractPollutionInfoCategory}. Recipe ids are derived from the
 * machine item registry name so JEI can keep them stable.</p>
 */
public class PollutionInfoCategory extends AbstractPollutionInfoCategory {

    public static final RecipeType<PollutionInfoRecipe> RECIPE_TYPE =
            RecipeType.create(Pollution.MOD_ID, "machine_info", PollutionInfoRecipe.class);

    public PollutionInfoCategory(IGuiHelper guiHelper, ItemStack iconStack) {
        super(guiHelper, RECIPE_TYPE,
                Component.translatable("pollution.jei.machine_info.title"),
                iconStack, 220, 84, "machine_info");
    }
}
