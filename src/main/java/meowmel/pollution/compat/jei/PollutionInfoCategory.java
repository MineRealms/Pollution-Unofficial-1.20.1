package meowmel.pollution.compat.jei;

import meowmel.pollution.Pollution;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

/**
 * JEI category that documents how the Pollution machines interact with the
 * environment (industrial pollution, Thaumcraft flux, aura and vis).
 *
 * <p>Implemented against the JEI 15.56 API: the category only needs
 * {@code getRecipeType}/{@code getTitle}/{@code getIcon}/{@code setRecipe};
 * the text is drawn in the {@code draw} hook. Recipe ids are derived from the
 * machine item registry name so JEI can keep them stable.</p>
 */
public class PollutionInfoCategory implements IRecipeCategory<PollutionInfoRecipe> {

    public static final RecipeType<PollutionInfoRecipe> RECIPE_TYPE =
            RecipeType.create(Pollution.MOD_ID, "machine_info", PollutionInfoRecipe.class);

    private static final int WIDTH = 220;
    private static final int HEIGHT = 84;
    private static final int ICON_X = 4;
    private static final int ICON_Y = 4;
    private static final int TEXT_X = 26;
    private static final int TEXT_Y = 8;
    private static final int LINE_HEIGHT = 11;

    private final IDrawable icon;

    public PollutionInfoCategory(IGuiHelper guiHelper, ItemStack iconStack) {
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, iconStack);
    }

    @Override
    public RecipeType<PollutionInfoRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.literal("机器污染信息");
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public boolean needsRecipeBorder() {
        return false;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PollutionInfoRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.OUTPUT, ICON_X, ICON_Y)
                .addItemStack(recipe.icon());
    }

    @Override
    public void draw(PollutionInfoRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics,
                     double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        List<Component> lines = recipe.lines();
        for (int i = 0; i < lines.size(); i++) {
            guiGraphics.drawString(font, lines.get(i), TEXT_X, TEXT_Y + i * LINE_HEIGHT, 0xFF404040, false);
        }
    }

    @Override
    public ResourceLocation getRegistryName(PollutionInfoRecipe recipe) {
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(recipe.icon().getItem());
        if (itemId == null) {
            return null;
        }
        return ResourceLocation.fromNamespaceAndPath(itemId.getNamespace(),
                "machine_info/" + itemId.getPath());
    }
}
