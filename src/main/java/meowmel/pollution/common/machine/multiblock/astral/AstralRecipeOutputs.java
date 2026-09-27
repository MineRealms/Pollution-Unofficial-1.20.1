package meowmel.pollution.common.machine.multiblock.astral;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import meowmel.pollution.api.astral.AstralCrystalNbtHelper;
import meowmel.pollution.common.machine.multiblock.MagicMultiblockController;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.StrictNBTIngredient;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;

/** Resolves input-dependent outputs before GT simulates space or calculates parallel inputs. */
public final class AstralRecipeOutputs {
    public static final String TRANSFORM = "pollution.astral.transform";
    public static final String TARGET = "pollution.astral.target";
    public static final String MIN_QUALITY = "pollution.astral.minimum_quality";

    private AstralRecipeOutputs() {}

    public static ModifierFunction recipeModifier(MetaMachine machine, GTRecipe recipe) {
        return machine instanceof MagicMultiblockController controller
                ? input -> transform(controller, input) : ModifierFunction.IDENTITY;
    }

    public static GTRecipe transform(MagicMultiblockController machine, GTRecipe recipe) {
        if (!recipe.data.contains(TRANSFORM) && !recipe.data.contains(MIN_QUALITY)) return recipe;
        List<ItemStack> inputs = new ArrayList<>();
        for (var handler : machine.getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP)) {
            if (handler instanceof IItemHandler inventory) {
                for (int slot = 0; slot < inventory.getSlots(); slot++) inputs.add(inventory.getStackInSlot(slot));
            }
        }
        return transform(recipe, inputs);
    }

    /** Also used by integration tests, with exactly the same input selection as a real machine. */
    public static GTRecipe transform(GTRecipe recipe, List<ItemStack> inventory) {
        String mode = recipe.data.getString(TRANSFORM);
        int minimumQuality = recipe.data.getInt(MIN_QUALITY);
        if (mode.isEmpty() && minimumQuality <= 0) return recipe;
        GTRecipe result = recipe.copy();
        List<Content> inputs = new ArrayList<>(result.getInputContents(ItemRecipeCapability.CAP));
        if (minimumQuality > 0) {
            int index = -1;
            for (int i = 0; i < inputs.size(); i++) {
                for (ItemStack candidate : ItemRecipeCapability.CAP.of(inputs.get(i).content).getItems()) {
                    if (AstralCrystalNbtHelper.isCultivatedCrystal(candidate)) index = i;
                }
            }
            if (index < 0) return null;
            ItemStack crystal = find(inventory, ItemRecipeCapability.CAP.of(inputs.get(index).content), "quality", minimumQuality);
            if (crystal.isEmpty()) return null;
            inputs.set(index, exact(inputs.get(index), crystal));
        }
        if (!mode.isEmpty()) {
            if (inputs.isEmpty()) return null;
            Content sourceInput = inputs.get(0);
            ItemStack source = find(inventory, ItemRecipeCapability.CAP.of(sourceInput.content), mode, 0);
            if (source.isEmpty()) return null;
            ItemStack output = switch (mode) {
                case "seed" -> AstralCrystalNbtHelper.createSeed(source.copyWithCount(1));
                case "embryo" -> AstralCrystalNbtHelper.createEmbryo(source.copyWithCount(1));
                case "growth" -> AstralCrystalNbtHelper.createCultivatedCrystal(source.copyWithCount(1), result.data.getString(TARGET));
                case "copy_nbt" -> copyNativeOutput(result, source);
                default -> ItemStack.EMPTY;
            };
            if (output.isEmpty()) return null;
            List<Content> outputs = new ArrayList<>(result.getOutputContents(ItemRecipeCapability.CAP));
            if (outputs.isEmpty()) return null;
            Content old = outputs.get(0);
            outputs.set(0, new Content(SizedIngredient.create(output), old.chance, old.maxChance, old.tierChanceBoost));
            result.outputs.put(ItemRecipeCapability.CAP, outputs);
            // Bind consumption to this exact source, so other qualities cannot be combined by parallel logic.
            inputs.set(0, exact(sourceInput, source));
        }
        result.inputs.put(ItemRecipeCapability.CAP, inputs);
        return result;
    }

    private static ItemStack copyNativeOutput(GTRecipe recipe, ItemStack source) {
        List<Content> outputs = recipe.getOutputContents(ItemRecipeCapability.CAP);
        if (outputs.isEmpty()) return ItemStack.EMPTY;
        ItemStack[] candidates = ItemRecipeCapability.CAP.of(outputs.get(0).content).getItems();
        if (candidates.length == 0) return ItemStack.EMPTY;
        ItemStack output = candidates[0].copy();
        output.setTag(source.hasTag() ? source.getTag().copy() : null);
        return output;
    }

    private static Content exact(Content original, ItemStack source) {
        Ingredient old = ItemRecipeCapability.CAP.of(original.content);
        int amount = old instanceof SizedIngredient sized ? sized.getAmount() : 1;
        Ingredient exact = SizedIngredient.create(StrictNBTIngredient.of(source.copyWithCount(1)), amount);
        return new Content(exact, original.chance, original.maxChance, original.tierChanceBoost);
    }

    private static ItemStack find(List<ItemStack> inventory, Ingredient ingredient, String mode, int minimumQuality) {
        for (ItemStack stack : inventory) {
            if (stack.isEmpty() || !ingredient.test(stack)) continue;
            boolean valid = switch (mode) {
                case "seed" -> AstralCrystalNbtHelper.isEligibleRockCrystal(stack)
                        && !AstralCrystalNbtHelper.createSeed(stack.copyWithCount(1)).isEmpty();
                case "embryo" -> AstralCrystalNbtHelper.isCrystalSeed(stack);
                case "growth" -> AstralCrystalNbtHelper.isCrystalEmbryo(stack);
                case "quality" -> AstralCrystalNbtHelper.getOpticalQuality(stack) >= minimumQuality;
                case "copy_nbt" -> true;
                default -> false;
            };
            if (valid) return stack;
        }
        return ItemStack.EMPTY;
    }
}
