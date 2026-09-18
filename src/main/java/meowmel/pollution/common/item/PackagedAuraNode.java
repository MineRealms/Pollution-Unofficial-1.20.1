package meowmel.pollution.common.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * NBT contract of the packaged aura node.
 *
 * <p>Tag names are kept verbatim from upstream so any ported recipes or
 * scripts keep working: {@code NodeTire} (Normal/Withering/Bright/Pale),
 * {@code NodeType} (Standard/Ominous/Pure/Concussive/Voracious) and the six
 * {@code Essence*} integers.</p>
 */
public final class PackagedAuraNode {

    public static final String TAG_TIER = "NodeTire";
    public static final String TAG_TYPE = "NodeType";
    public static final String ESSENCE_AIR = "EssenceAir";
    public static final String ESSENCE_FIRE = "EssenceFire";
    public static final String ESSENCE_WATER = "EssenceWater";
    public static final String ESSENCE_EARTH = "EssenceEarth";
    public static final String ESSENCE_ORDER = "EssenceOrder";
    public static final String ESSENCE_ENTROPY = "EssenceEntropy";

    public static boolean isNode(ItemStack stack) {
        return !stack.isEmpty() && stack.is(PollutionItems.PACKAGED_AURA_NODE.get());
    }

    public static String tier(ItemStack stack) {
        return tag(stack).getString(TAG_TIER);
    }

    public static String type(ItemStack stack) {
        return tag(stack).getString(TAG_TYPE);
    }

    public static int essence(ItemStack stack, String key) {
        return tag(stack).getInt(key);
    }

    public static void setEssence(ItemStack stack, String key, int value) {
        stack.getOrCreateTag().putInt(key, Math.max(0, value));
    }

    public static boolean hasEssence(ItemStack stack, String key) {
        return tag(stack).contains(key);
    }

    private static CompoundTag tag(ItemStack stack) {
        return stack.getOrCreateTag();
    }

    private PackagedAuraNode() {}
}
