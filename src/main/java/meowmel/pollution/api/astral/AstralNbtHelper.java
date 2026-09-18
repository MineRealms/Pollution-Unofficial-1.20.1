package meowmel.pollution.api.astral;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

/**
 * Canonical NBT helpers shared by celestial items, recipes and hatches.
 *
 * <p>1.12 upstream additionally resolved Astral Sorcery's {@code IConstellation}
 * instances and created the constellation-data wafer / calibration-core meta
 * items. Astral Sorcery and the Pollution meta items are not part of the 1.20.1
 * port yet, so this port is deliberately id-based: the constellation id string
 * is the authoritative data and all helpers operate on caller-provided
 * {@link ItemStack} / {@link CompoundTag} instances.</p>
 *
 * <p>TODO(port): when Astral Sorcery is available, restore the native
 * {@code IConstellation} write/read path and the wafer/core item factories.</p>
 */
public final class AstralNbtHelper {

    public static final String POLLUTION_CONSTELLATION = "pollutionConstellation";
    public static final String CELESTIAL_FUNCTION = "pollutionCelestialFunction";

    private AstralNbtHelper() {}

    /** Writes the Pollution constellation id and celestial function key onto a copy of the stack. */
    public static ItemStack writeConstellation(ItemStack stack, String constellationId) {
        if (stack == null || stack.isEmpty()) return ItemStack.EMPTY;
        ItemStack result = stack.copy();
        CompoundTag tag = result.getOrCreateTag();
        String id = normalize(constellationId);
        tag.putString(POLLUTION_CONSTELLATION, id);
        tag.putString(CELESTIAL_FUNCTION, getFunctionKey(id));
        return result;
    }

    public static String getFunctionKey(String constellationId) {
        String name = normalize(constellationId);
        if ("aevitas".equals(name)) return "life";
        if ("evorsio".equals(name)) return "processing";
        if ("armara".equals(name)) return "stability";
        if ("discidia".equals(name)) return "energy";
        if ("horologium".equals(name)) return "time";
        return "resonance";
    }

    /**
     * Reads the stored constellation id. Returns an empty string when the stack
     * carries no Pollution constellation data.
     */
    public static String readConstellation(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.hasTag()) return "";
        return readConstellation(stack.getTag());
    }

    public static String readConstellation(CompoundTag tag) {
        return tag == null ? "" : tag.getString(POLLUTION_CONSTELLATION);
    }

    public static String normalize(String id) {
        return id == null ? "" : id.trim().toLowerCase(Locale.ROOT);
    }
}
