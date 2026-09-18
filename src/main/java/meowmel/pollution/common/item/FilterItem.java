package meowmel.pollution.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Air filter cartridge: behaviour port of upstream {@code FilterBehavior}.
 *
 * <p>Upstream kept the filter durability in item NBT through GregTech's
 * {@code AbstractMaterialPartBehavior} (key {@code Damage}) and reported it in
 * the tooltip. The port keeps the same NBT contract so machine code ported
 * later can consume filters with {@link #applyDamage(ItemStack, int)}. The
 * stack size is forced to 1 like upstream's {@code IItemMaxStackSizeProvider}.</p>
 */
public class FilterItem extends Item {

    public static final String TAG_DAMAGE = "Damage";

    private final int maxDurability;
    private final int filterTier;
    private final String materialKey;

    public FilterItem(Properties properties, int maxDurability, int filterTier, String materialKey) {
        super(properties);
        this.maxDurability = maxDurability;
        this.filterTier = filterTier;
        this.materialKey = materialKey;
    }

    /**
     * Upstream {@code FilterBehavior#getInstanceFor}: resolves the behaviour
     * from a stack. The port keeps the same contract for machine code that
     * consumes filters.
     */
    @Nullable
    public static FilterItem getInstanceFor(ItemStack stack) {
        return stack.getItem() instanceof FilterItem filter ? filter : null;
    }

    public int getFilterTier() {
        return filterTier;
    }

    public int getPartMaxDurability(ItemStack stack) {
        return maxDurability;
    }

    public int getPartDamage(ItemStack stack) {
        return stack.getOrCreateTag().getInt(TAG_DAMAGE);
    }

    public void setPartDamage(ItemStack stack, int damage) {
        stack.getOrCreateTag().putInt(TAG_DAMAGE, Math.max(0, damage));
    }

    public double getDurabilityPercent(ItemStack stack) {
        return 1.0D - (double) getPartDamage(stack) / maxDurability;
    }

    /** Applies wear; destroys the stack when the durability runs out (upstream behaviour). */
    public void applyDamage(ItemStack stack, int damageApplied) {
        int resultDamage = getPartDamage(stack) + damageApplied;
        if (resultDamage >= maxDurability) {
            stack.shrink(1);
        } else {
            setPartDamage(stack, resultDamage);
        }
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return 1;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        int damage = getPartDamage(stack);
        tooltip.add(Component.translatable("pollution.filter.durability", maxDurability - damage, maxDurability)
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("pollution.filter.material", Component.translatable(materialKey))
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("pollution.filter.tier", filterTier).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("pollution.filter.expected", formatTicks(maxDurability))
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("pollution.filter.remaining", formatTicks(maxDurability - damage))
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    /** Port of upstream's {@code GTQTDateHelper.getTimeFromTicks} formatting. */
    public static String formatTicks(int ticks) {
        int seconds = ticks / 20;
        int minutes = seconds / 60;
        int hours = minutes / 60;
        int days = hours / 24;
        if (days > 0) {
            return days + "d " + (hours % 24) + "h " + (minutes % 60) + "m";
        }
        if (hours > 0) {
            return hours + "h " + (minutes % 60) + "m " + (seconds % 60) + "s";
        }
        if (minutes > 0) {
            return minutes + "m " + (seconds % 60) + "s";
        }
        return seconds + "s";
    }
}
