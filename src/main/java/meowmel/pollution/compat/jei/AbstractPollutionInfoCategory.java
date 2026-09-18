package meowmel.pollution.compat.jei;

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
 * Shared layout of the Pollution JEI info categories.
 *
 * <p>All info pages have the same shape: one icon slot in the top-left corner
 * plus a list of text lines drawn next to it. The concrete categories only
 * differ in their {@link RecipeType}, title, page size and id prefix, so the
 * slot, text and stable registry-name handling live here.</p>
 */
public abstract class AbstractPollutionInfoCategory implements IRecipeCategory<PollutionInfoRecipe> {

    private static final int ICON_X = 4;
    private static final int ICON_Y = 4;
    private static final int TEXT_X = 26;
    private static final int TEXT_Y = 8;
    private static final int LINE_HEIGHT = 11;

    private final RecipeType<PollutionInfoRecipe> recipeType;
    private final Component title;
    private final IDrawable icon;
    private final int width;
    private final int height;
    private final String registryPrefix;

    protected AbstractPollutionInfoCategory(IGuiHelper guiHelper,
                                            RecipeType<PollutionInfoRecipe> recipeType,
                                            Component title,
                                            ItemStack iconStack,
                                            int width,
                                            int height,
                                            String registryPrefix) {
        this.recipeType = recipeType;
        this.title = title;
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, iconStack);
        this.width = width;
        this.height = height;
        this.registryPrefix = registryPrefix;
    }

    @Override
    public RecipeType<PollutionInfoRecipe> getRecipeType() {
        return this.recipeType;
    }

    @Override
    public Component getTitle() {
        return this.title;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
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
                this.registryPrefix + "/" + itemId.getPath());
    }
}
