package meowmel.pollution.api.amplification;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Safe, deterministic item-output settlement for constellation and tarot bonuses.
 *
 * <p>1.12 upstream read {@code gregtech.api.recipes.Recipe#getOutputs()} and
 * {@code getChancedOutputs()}. Modern GTCEu stores item outputs as
 * {@link Content} entries under {@link ItemRecipeCapability#CAP}; this port keeps
 * the upstream method names and settlement semantics but consumes that format.
 * Chanced outputs use {@code content.chance / content.maxChance} as the
 * probability, matching the upstream 0..1 {@code getChance()}.</p>
 */
public final class MagicOutputProcessor {

    private MagicOutputProcessor() {
    }

    public static List<ItemStack> forecast(GTRecipe recipe, int parallel, MagicAmplificationResult result) {
        List<ItemStack> resultStacks = new ArrayList<>();
        if (recipe == null || result == null || result.getOutputBonus() <= 0.0D) return resultStacks;
        for (Content content : deterministicOutputs(recipe)) {
            ItemStack stack = toStack(content);
            if (!isAmplifiable(stack)) continue;
            int amount = safeCount(Math.ceil(stack.getCount() * Math.max(1, parallel) * result.getOutputBonus()));
            addSplit(resultStacks, stack, amount);
        }
        if (result.getChanceExtraRoll() > 0.0D) {
            for (int copy = 0; copy < Math.max(1, parallel); copy++) {
                for (Content content : chancedOutputs(recipe)) {
                    ItemStack stack = toStack(content);
                    if (isAmplifiable(stack)) addSplit(resultStacks, stack, Math.max(1, stack.getCount()));
                }
            }
        }
        return resultStacks;
    }

    /**
     * Advances only the safe deterministic accumulators. It is called exactly
     * once after GT has accepted and consumed the recipe inputs.
     */
    public static List<ItemStack> settle(GTRecipe recipe, int parallel, MagicAmplificationResult result,
                                          Map<String, Double> fractionalOutputs) {
        List<ItemStack> extras = new ArrayList<>();
        if (recipe == null || result == null) return extras;

        List<Content> outputs = recipe.getOutputContents(ItemRecipeCapability.CAP);
        if (result.getOutputBonus() > 0.0D) {
            for (int index = 0; index < outputs.size(); index++) {
                Content content = outputs.get(index);
                if (content.isChanced()) continue;
                ItemStack stack = toStack(content);
                if (!isAmplifiable(stack)) continue;
                String key = fractionKey(recipe, index);
                double stored = fractionalOutputs.containsKey(key) ? fractionalOutputs.get(key) : 0.0D;
                double value = stored + stack.getCount() * Math.max(1, parallel) * result.getOutputBonus();
                int extra = safeCount(Math.floor(value));
                fractionalOutputs.put(key, value - extra);
                addSplit(extras, stack, extra);
            }
        }

        if (result.getChanceExtraRoll() > 0.0D) {
            for (int copy = 0; copy < Math.max(1, parallel); copy++) {
                for (Content content : chancedOutputs(recipe)) {
                    ItemStack stack = toStack(content);
                    if (!isAmplifiable(stack)) continue;
                    int maxChance = Math.max(1, content.maxChance);
                    int chance = (int) Math.min(maxChance,
                            Math.ceil(content.chance * result.getChanceExtraRoll()));
                    if (chance > 0 && GTValues.RNG.nextInt(maxChance) < chance) {
                        addSplit(extras, stack, stack.getCount());
                    }
                }
            }
        }
        return extras;
    }

    private static List<Content> deterministicOutputs(GTRecipe recipe) {
        List<Content> deterministic = new ArrayList<>();
        for (Content content : recipe.getOutputContents(ItemRecipeCapability.CAP)) {
            if (!content.isChanced()) deterministic.add(content);
        }
        return deterministic;
    }

    private static List<Content> chancedOutputs(GTRecipe recipe) {
        List<Content> chanced = new ArrayList<>();
        for (Content content : recipe.getOutputContents(ItemRecipeCapability.CAP)) {
            if (content.isChanced()) chanced.add(content);
        }
        return chanced;
    }

    private static ItemStack toStack(Content content) {
        Object inner = content.getContent();
        if (inner instanceof ItemStack stack) return stack;
        if (inner instanceof Ingredient ingredient) {
            ItemStack[] items = ingredient.getItems();
            return items.length == 0 ? ItemStack.EMPTY : items[0];
        }
        return ItemStack.EMPTY;
    }

    private static boolean isAmplifiable(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (stack.isEdible() || stack.is(Tags.Items.SEEDS)) return true;
        TagPrefix prefix = ChemicalHelper.getPrefix(stack.getItem());
        return prefix == TagPrefix.ore || prefix == TagPrefix.rawOre
                || prefix == TagPrefix.crushed || prefix == TagPrefix.crushedPurified
                || prefix == TagPrefix.crushedRefined || prefix == TagPrefix.dust
                || prefix == TagPrefix.dustImpure || prefix == TagPrefix.dustPure || prefix == TagPrefix.dustSmall
                || prefix == TagPrefix.dustTiny || prefix == TagPrefix.gem || prefix == TagPrefix.gemChipped
                || prefix == TagPrefix.gemFlawed || prefix == TagPrefix.gemFlawless || prefix == TagPrefix.ingot
                || prefix == TagPrefix.ingotHot || prefix == TagPrefix.nugget;
    }

    private static String fractionKey(GTRecipe recipe, int index) {
        return "r" + Integer.toHexString(recipe.hashCode()) + "o" + index;
    }

    private static int safeCount(double amount) {
        return amount <= 0.0D ? 0 : amount >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) amount;
    }

    private static void addSplit(List<ItemStack> target, ItemStack source, int count) {
        while (count > 0) {
            int amount = Math.min(count, source.getMaxStackSize());
            ItemStack copy = source.copy();
            copy.setCount(amount);
            target.add(copy);
            count -= amount;
        }
    }
}
