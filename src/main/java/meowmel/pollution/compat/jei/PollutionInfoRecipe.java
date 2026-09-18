package meowmel.pollution.compat.jei;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Minimal JEI info recipe: one machine icon plus the pollution notes shown on
 * the category page. JEI 15.56 has no dedicated "info page" type, so the port
 * models its machine pollution documentation as a normal recipe category with
 * a single output-styled icon and drawn text lines.
 *
 * @param icon  machine item shown in the recipe slot
 * @param lines text lines drawn next to the icon
 */
public record PollutionInfoRecipe(ItemStack icon, List<Component> lines) {
}
