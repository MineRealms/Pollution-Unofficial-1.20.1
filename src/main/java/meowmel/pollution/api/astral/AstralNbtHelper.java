package meowmel.pollution.api.astral;

import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.item.ItemConstellationPaper;
import hellfirepvp.astralsorcery.common.item.crystal.ItemAttunedCrystalBase;
import meowmel.pollution.common.item.PollutionItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

/** Canonical constellation NBT shared by celestial items, recipes and hatches. */
public final class AstralNbtHelper {

    public static final String POLLUTION_CONSTELLATION = "pollutionConstellation";
    public static final String CELESTIAL_FUNCTION = "pollutionCelestialFunction";

    private AstralNbtHelper() {}

    public static ItemStack createDataWafer(IConstellation constellation) {
        return writeConstellation(new ItemStack(PollutionItems.CONSTELLATION_DATA_WAFER.get()), constellation);
    }

    public static ItemStack createCalibratedCore(IConstellation constellation) {
        return writeConstellation(new ItemStack(PollutionItems.CELESTIAL_CALIBRATION_CORE.get()), constellation);
    }

    public static ItemStack writeConstellation(ItemStack stack, IConstellation constellation) {
        if (stack == null || stack.isEmpty() || constellation == null) return ItemStack.EMPTY;
        ItemStack result = stack.copy();
        CompoundTag tag = result.getOrCreateTag();
        constellation.writeToNBT(tag);
        String id = normalize(constellation.getSimpleName());
        tag.putString(POLLUTION_CONSTELLATION, id);
        tag.putString(CELESTIAL_FUNCTION, getFunctionKey(id));
        return result;
    }

    /** Writes the Pollution constellation id and celestial function key onto a copy of the stack. */
    public static ItemStack writeConstellation(ItemStack stack, String constellationId) {
        if (stack == null || stack.isEmpty()) return ItemStack.EMPTY;
        ItemStack result = stack.copy();
        CompoundTag tag = result.getOrCreateTag();
        String id = normalize(constellationId);
        tag.putString(POLLUTION_CONSTELLATION, id);
        tag.putString(CELESTIAL_FUNCTION, getFunctionKey(id));
        IConstellation constellation = findConstellation(id);
        if (constellation != null) constellation.writeToNBT(tag);
        return result;
    }

    public static IConstellation readNativeConstellation(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        if (stack.getItem() instanceof ItemConstellationPaper paper) return paper.getConstellation(stack);
        if (stack.getItem() instanceof ItemAttunedCrystalBase crystal) return crystal.getFocusConstellation(stack);
        CompoundTag tag = stack.getTag();
        if (tag == null) return null;
        IConstellation nativeConstellation = IConstellation.readFromNBT(tag);
        return nativeConstellation != null ? nativeConstellation : findConstellation(tag.getString(POLLUTION_CONSTELLATION));
    }

    public static IConstellation findConstellation(String id) {
        String normalized = normalize(id);
        if (normalized.isEmpty()) return null;
        ResourceLocation key = ResourceLocation.tryParse(normalized.contains(":") ? normalized : "astralsorcery:" + normalized);
        IConstellation found = ConstellationRegistry.getConstellation(key);
        if (found != null) return found;
        for (IConstellation candidate : ConstellationRegistry.getAllConstellations()) {
            if (candidate.getSimpleName().equalsIgnoreCase(normalized)) return candidate;
        }
        return null;
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
        IConstellation nativeConstellation = readNativeConstellation(stack);
        return nativeConstellation != null ? normalize(nativeConstellation.getSimpleName()) : readConstellation(stack.getTag());
    }

    public static String readConstellation(CompoundTag tag) {
        return tag == null ? "" : tag.getString(POLLUTION_CONSTELLATION);
    }

    public static String normalize(String id) {
        return id == null ? "" : id.trim().toLowerCase(Locale.ROOT);
    }
}
