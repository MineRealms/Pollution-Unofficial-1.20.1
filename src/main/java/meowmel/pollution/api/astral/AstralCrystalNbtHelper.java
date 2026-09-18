package meowmel.pollution.api.astral;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

/**
 * Preserves Astral Sorcery's native rock-crystal properties through Pollution's
 * seed and embryo processing chain. The chain is deliberately one-way: a
 * cultivated crystal cannot be selected as a new industrial seed.
 *
 * <p>1.12 upstream depended on Astral Sorcery's {@code CrystalProperties} and on
 * Pollution's GregTech meta items. Neither exists in the 1.20.1 port yet, so this
 * port is intentionally generic: it owns the serialized crystal data format and
 * operates on caller-provided {@link ItemStack}s. Item identity and the raw
 * crystal stat math stay the caller's concern; the cultivation bonuses are
 * applied to the stored {@code poCrystalPurity} / {@code poCrystalStability}
 * values.</p>
 *
 * <p>TODO(port): restore the native Astral Sorcery {@code CrystalProperties}
 * read/write once that mod is available, including the native size / collective
 * / fracturation improvements of the cultivated generation.</p>
 */
public final class AstralCrystalNbtHelper {

    private static final String VERSION = "poCrystalVersion";
    private static final String SOURCE = "poSourceCrystal";
    private static final String PURITY = "poCrystalPurity";
    private static final String STABILITY = "poCrystalStability";
    private static final String GENERATION = "poCrystalGeneration";
    private static final String EMBRYO = "poCrystalEmbryo";
    private static final String CONSTELLATION = "poCultivatedConstellation";
    private static final String GRADE = "poCrystalGrade";
    private static final String NATIVE_CRYSTAL = "poCultivatedNativeCrystal";

    private AstralCrystalNbtHelper() {
    }

    /**
     * Upstream also required the stack to be Astral Sorcery's rock crystal.
     * Generic port: any non-empty stack without a cultivation generation.
     */
    public static boolean isEligibleRockCrystal(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        CompoundTag tag = stack.getTag();
        return tag == null || tag.getInt(GENERATION) == 0;
    }

    public static boolean isCrystalSeed(ItemStack stack) {
        CompoundTag tag = getTag(stack);
        return hasCrystalData(tag) && !tag.getBoolean(EMBRYO) && tag.getInt(GENERATION) <= 0;
    }

    public static boolean isCrystalEmbryo(ItemStack stack) {
        CompoundTag tag = getTag(stack);
        return hasCrystalData(tag) && tag.getBoolean(EMBRYO);
    }

    /** The dedicated Pollution output of the growth array and the only valid optical insert. */
    public static boolean isCultivatedCrystal(ItemStack stack) {
        CompoundTag tag = getTag(stack);
        return hasCrystalData(tag) && tag.getInt(GENERATION) > 0;
    }

    /**
     * Writes seed data onto a copy of {@code seed}. Purity and stability are
     * supplied by the caller (derived from the native crystal stats), so the
     * helper stays independent of Astral Sorcery.
     */
    public static ItemStack createSeed(ItemStack seed, ItemStack source, int purity, int stability) {
        if (seed == null || seed.isEmpty() || !isEligibleRockCrystal(source)) return ItemStack.EMPTY;

        ItemStack result = seed.copy();
        CompoundTag data = new CompoundTag();
        data.putInt(VERSION, 1);
        data.put(SOURCE, source.serializeNBT());
        data.putInt(PURITY, clamp(purity, 0, 100));
        data.putInt(STABILITY, clamp(stability, 0, 100));
        data.putInt(GENERATION, 0);
        data.putBoolean(EMBRYO, false);
        result.setTag(data);
        return result;
    }

    public static ItemStack createEmbryo(ItemStack embryo, ItemStack seed) {
        if (embryo == null || embryo.isEmpty() || !isCrystalSeed(seed)) return ItemStack.EMPTY;

        ItemStack result = embryo.copy();
        CompoundTag data = getTag(seed).copy();
        data.putBoolean(EMBRYO, true);
        result.setTag(data);
        return result;
    }

    /**
     * Produces the sole cultivated generation of a rock crystal. The result
     * keeps the source data and applies the one-time cultivation bonuses to the
     * stored purity and stability; the caller owns the resulting item.
     */
    public static ItemStack createCultivatedCrystal(ItemStack cultivated, ItemStack embryo) {
        return createCultivatedCrystal(cultivated, embryo, "");
    }

    public static ItemStack createCultivatedCrystal(ItemStack cultivated, ItemStack embryo, String constellationId) {
        if (cultivated == null || cultivated.isEmpty() || !isCrystalEmbryo(embryo)) return ItemStack.EMPTY;

        CompoundTag embryoData = getTag(embryo);
        CompoundTag stored = embryoData.getCompound(SOURCE);
        if (stored.isEmpty()) return ItemStack.EMPTY;

        CultivationBonus bonus = CultivationBonus.forConstellation(constellationId);
        ItemStack result = cultivated.copy();
        CompoundTag data = new CompoundTag();
        data.putInt(VERSION, 1);
        data.put(SOURCE, stored.copy());
        data.put(NATIVE_CRYSTAL, stored.copy());
        data.putInt(PURITY, clamp(embryoData.getInt(PURITY) + 12 + bonus.purity, 0, 100));
        data.putInt(STABILITY, clamp(embryoData.getInt(STABILITY) + 8 + bonus.collective, 0, 100));
        data.putInt(GENERATION, 1);
        data.putBoolean(EMBRYO, false);
        data.putString(CONSTELLATION, bonus.id);
        data.putString(GRADE, gradeFor(getOpticalQuality(data)));
        result.setTag(data);
        return result;
    }

    public static int getPurity(ItemStack stack) {
        return hasCrystalData(stack) ? clamp(getTag(stack).getInt(PURITY), 0, 100) : 0;
    }

    public static int getStability(ItemStack stack) {
        return hasCrystalData(stack) ? clamp(getTag(stack).getInt(STABILITY), 0, 100) : 0;
    }

    /**
     * A quality score used only by the advanced lens optical insert. Purity is
     * weighted slightly higher than stability because fractured crystals are
     * already penalized during seed selection.
     */
    public static int getOpticalQuality(ItemStack stack) {
        if (!isCultivatedCrystal(stack)) return 0;
        return clamp((getPurity(stack) * 7 + getStability(stack) * 3) / 10, 0, 100);
    }

    /** Native Astral properties preserved inside the independent cultivated-crystal item. */
    public static CompoundTag getCultivatedProperties(ItemStack stack) {
        return isCultivatedCrystal(stack) ? getTag(stack).getCompound(NATIVE_CRYSTAL) : new CompoundTag();
    }

    /** Convenience view of the preserved native crystal as an item stack; empty when absent. */
    public static ItemStack getCultivatedNativeCrystal(ItemStack stack) {
        CompoundTag nativeTag = getCultivatedProperties(stack);
        return nativeTag.isEmpty() ? ItemStack.EMPTY : ItemStack.of(nativeTag);
    }

    public static String getCultivationConstellation(ItemStack stack) {
        return isCultivatedCrystal(stack) ? getTag(stack).getString(CONSTELLATION) : "";
    }

    public static String getCultivationGrade(ItemStack stack) {
        return isCultivatedCrystal(stack) ? getTag(stack).getString(GRADE) : "";
    }

    /** Generic port of the upstream stability formula; maxSize is the crystal size ceiling. */
    public static int calculateStability(int size, int maxSize, int collectiveCapability, int fracturation) {
        int sizePercent = clamp(maxSize <= 0 ? 0 : size * 100 / maxSize, 0, 100);
        int collective = clamp(collectiveCapability, 0, 100);
        int fracturePenalty = Math.min(50, Math.max(0, fracturation) * 10);
        return clamp(25 + sizePercent / 2 + collective / 2 - fracturePenalty, 0, 100);
    }

    private static boolean hasCrystalData(ItemStack stack) {
        return hasCrystalData(getTag(stack));
    }

    private static boolean hasCrystalData(CompoundTag tag) {
        return tag != null && tag.contains(VERSION) && tag.contains(SOURCE)
                && tag.contains(PURITY) && tag.contains(STABILITY);
    }

    private static CompoundTag getTag(ItemStack stack) {
        if (stack == null) return new CompoundTag();
        CompoundTag tag = stack.getTag();
        return tag == null ? new CompoundTag() : tag;
    }

    private static int getOpticalQuality(CompoundTag data) {
        int purity = clamp(data.getInt(PURITY), 0, 100);
        int stability = clamp(data.getInt(STABILITY), 0, 100);
        return clamp((purity * 7 + stability * 3) / 10, 0, 100);
    }

    private static String gradeFor(int quality) {
        if (quality >= 85) return "S";
        if (quality >= 70) return "A";
        if (quality >= 50) return "B";
        return "C";
    }

    private static final class CultivationBonus {
        private final String id;
        private final int size;
        private final int purity;
        private final int collective;
        private final int fractureRepair;

        private CultivationBonus(String id, int size, int purity, int collective, int fractureRepair) {
            this.id = id;
            this.size = size;
            this.purity = purity;
            this.collective = collective;
            this.fractureRepair = fractureRepair;
        }

        private static CultivationBonus forConstellation(String constellationId) {
            String id = constellationId == null ? "" : constellationId.toLowerCase(Locale.ROOT);
            switch (id) {
                case "aevitas": return new CultivationBonus(id, 6, 0, 4, 0);
                case "evorsio": return new CultivationBonus(id, 0, 6, 0, 1);
                case "armara": return new CultivationBonus(id, 0, 0, 4, 2);
                case "discidia": return new CultivationBonus(id, 8, 0, 0, 0);
                case "vicio": return new CultivationBonus(id, 0, 0, 10, 0);
                case "mineralis": return new CultivationBonus(id, 0, 10, 0, 0);
                case "fornax": return new CultivationBonus(id, 7, 3, 0, 0);
                case "horologium": return new CultivationBonus(id, 0, 4, 8, 0);
                case "lucerna": return new CultivationBonus(id, 0, 12, 0, 0);
                case "octans": return new CultivationBonus(id, 0, 0, 8, 1);
                case "bootes": return new CultivationBonus(id, 8, 0, 0, 0);
                case "pelotrio": return new CultivationBonus(id, 0, 8, 3, 0);
                case "gelu": return new CultivationBonus(id, 0, 0, 0, 2);
                case "ulteria": return new CultivationBonus(id, 0, 0, 12, 0);
                case "alcara": return new CultivationBonus(id, 4, 4, 4, 0);
                case "vorux": return new CultivationBonus(id, 10, 5, 0, 0);
                default: return new CultivationBonus(id, 0, 0, 0, 0);
            }
        }
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
