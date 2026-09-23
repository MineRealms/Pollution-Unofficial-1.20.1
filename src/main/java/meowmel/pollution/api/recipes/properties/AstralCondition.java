package meowmel.pollution.api.recipes.properties;

import net.minecraft.nbt.CompoundTag;

import java.util.Locale;

/**
 * Server-side sky requirements for an Astral Sorcery powered recipe.
 * Empty values mean that the corresponding check is not required.
 *
 * <p>Upstream origin: {@code meowmel.pollution.api.recipes.properties.AstralCondition}
 * (1.12.2), kept verbatim. Upstream stored it through its GTCEu 1.12 recipe
 * property; the port stores it as a {@link CompoundTag} in {@code GTRecipe.data}
 * under {@link MagicRecipeProperties#ASTRAL_CONDITION} and exposes
 * {@link #toNbt()} / {@link #fromNbt(CompoundTag)} so recipe builders and the
 * controller logic can round-trip it.</p>
 */
public final class AstralCondition {

    public static final AstralCondition NONE = new AstralCondition("", "", "", false, 0.0F);

    private static final String CONSTELLATION = "constellation";
    private static final String MOON_PHASE = "moon_phase";
    private static final String CELESTIAL_EVENT = "celestial_event";
    private static final String REQUIRE_NIGHT = "require_night";
    private static final String MINIMUM_DISTRIBUTION = "minimum_distribution";

    private final String constellation;
    private final String moonPhase;
    private final String celestialEvent;
    private final boolean requireNight;
    private final float minimumDistribution;

    public AstralCondition(String constellation, String moonPhase, String celestialEvent,
                           boolean requireNight, float minimumDistribution) {
        this.constellation = normalize(constellation);
        this.moonPhase = normalize(moonPhase);
        this.celestialEvent = normalize(celestialEvent);
        this.requireNight = requireNight;
        this.minimumDistribution = Math.max(0.0F, Math.min(1.0F, minimumDistribution));
    }

    public static AstralCondition night(String constellation, float minimumDistribution) {
        return new AstralCondition(constellation, "", "", true, minimumDistribution);
    }

    public static AstralCondition fullMoon(String constellation, float minimumDistribution) {
        return new AstralCondition(constellation, "FULL", "", true, minimumDistribution);
    }

    public static AstralCondition event(String constellation, String celestialEvent, float minimumDistribution) {
        return new AstralCondition(constellation, "", celestialEvent, true, minimumDistribution);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    /** Serialises the configured condition; callers should check {@link #isConfigured()} first. */
    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        if (!constellation.isEmpty()) {
            tag.putString(CONSTELLATION, constellation);
        }
        if (!moonPhase.isEmpty()) {
            tag.putString(MOON_PHASE, moonPhase);
        }
        if (!celestialEvent.isEmpty()) {
            tag.putString(CELESTIAL_EVENT, celestialEvent);
        }
        if (requireNight) {
            tag.putBoolean(REQUIRE_NIGHT, true);
        }
        if (minimumDistribution > 0.0F) {
            tag.putFloat(MINIMUM_DISTRIBUTION, minimumDistribution);
        }
        return tag;
    }

    /** Reads a condition written by {@link #toNbt()}; malformed tags degrade to {@link #NONE}. */
    public static AstralCondition fromNbt(CompoundTag tag) {
        if (tag == null || tag.isEmpty()) {
            return NONE;
        }
        return new AstralCondition(tag.getString(CONSTELLATION), tag.getString(MOON_PHASE),
                tag.getString(CELESTIAL_EVENT), tag.getBoolean(REQUIRE_NIGHT),
                tag.getFloat(MINIMUM_DISTRIBUTION));
    }

    public String getConstellation() {
        return constellation;
    }

    public String getMoonPhase() {
        return moonPhase;
    }

    public String getCelestialEvent() {
        return celestialEvent;
    }

    public boolean isNightRequired() {
        return requireNight;
    }

    public float getMinimumDistribution() {
        return minimumDistribution;
    }

    public boolean isConfigured() {
        return requireNight || minimumDistribution > 0.0F || !constellation.isEmpty()
                || !moonPhase.isEmpty() || !celestialEvent.isEmpty();
    }
}
