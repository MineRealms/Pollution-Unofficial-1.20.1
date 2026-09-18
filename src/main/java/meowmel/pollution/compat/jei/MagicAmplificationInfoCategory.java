package meowmel.pollution.compat.jei;

import meowmel.pollution.Pollution;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * JEI category for the magic amplification guide.
 *
 * <p>Port of upstream's static magic guide pages
 * ({@code MagicGuideUI}/{@code MagicPropertyRecipeUI}): one page per ported
 * magic machine, listing the recipe-domain tags used by the amplification
 * engine and the matching Astral constellation / Tarot card effects from
 * {@link meowmel.pollution.api.amplification.MagicJeiHintResolver}. Upstream
 * reserved extra recipe-property space for these lines; the modern port shows
 * them on a dedicated info page and on the GT recipe pages through
 * {@link MagicRecipeDataInfos}.</p>
 */
public class MagicAmplificationInfoCategory extends AbstractPollutionInfoCategory {

    public static final RecipeType<PollutionInfoRecipe> RECIPE_TYPE =
            RecipeType.create(Pollution.MOD_ID, "magic_amplification_info", PollutionInfoRecipe.class);

    public MagicAmplificationInfoCategory(IGuiHelper guiHelper, ItemStack iconStack) {
        super(guiHelper, RECIPE_TYPE,
                Component.translatable("pollution.jei.magic_amplification.title"),
                iconStack, 220, 132, "magic_amplification_info");
    }
}
