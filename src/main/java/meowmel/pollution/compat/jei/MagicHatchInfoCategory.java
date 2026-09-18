package meowmel.pollution.compat.jei;

import meowmel.pollution.Pollution;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * JEI category documenting the magic hatches of the port: the vis hatch, the
 * infused fluid hatch, the mana energy hatches (including the wireless and
 * mana pool variants) and the flux muffler.
 *
 * <p>Upstream only exposed these through item tooltips; the port keeps the
 * tooltips and additionally collects the hatch rules on one JEI page so the
 * magic multiblock requirements (vis, infused fluid, mana) are discoverable
 * without the machine GUI. All text is read from the machine tooltip lang keys
 * already shipped by the machine registrations.</p>
 */
public class MagicHatchInfoCategory extends AbstractPollutionInfoCategory {

    public static final RecipeType<PollutionInfoRecipe> RECIPE_TYPE =
            RecipeType.create(Pollution.MOD_ID, "magic_hatch_info", PollutionInfoRecipe.class);

    public MagicHatchInfoCategory(IGuiHelper guiHelper, ItemStack iconStack) {
        super(guiHelper, RECIPE_TYPE,
                Component.translatable("pollution.jei.magic_hatch.title"),
                iconStack, 220, 84, "magic_hatch_info");
    }
}
