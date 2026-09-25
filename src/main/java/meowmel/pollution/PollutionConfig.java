package meowmel.pollution;

import net.minecraftforge.common.ForgeConfigSpec;
import java.util.LinkedHashMap;
import java.util.Map;

public final class PollutionConfig {

    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.ConfigValue<String> PORTAL_ORIGIN_DIMENSION;
    public static final ForgeConfigSpec.BooleanValue ALLOW_PORTALS_IN_OTHER_DIMENSIONS;
    public static final ForgeConfigSpec.BooleanValue RETURN_PORTAL_USABLE;
    public static final ForgeConfigSpec.BooleanValue CHECK_PORTAL_DESTINATION;

    public static final ForgeConfigSpec.BooleanValue ENABLE_POLLUTION;
    public static final ForgeConfigSpec.BooleanValue ENABLE_EXPLOSION_POLLUTION;
    public static final ForgeConfigSpec.DoubleValue MUFFLER_POLLUTION_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue FLUX_SCRUBBER_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue POLLUTION_DECAY_PER_TICK;
    public static final ForgeConfigSpec.DoubleValue EFFECT_THRESHOLD;
    public static final ForgeConfigSpec.BooleanValue ENABLE_TERRAIN_CONVERSION;
    public static final ForgeConfigSpec.BooleanValue ENABLE_WARP_EVENTS;
    public static final ForgeConfigSpec.BooleanValue ENABLE_COUNTDOWN_BOMB;
    public static final ForgeConfigSpec.IntValue WARP_EVENT_INTERVAL_TICKS;
    public static final ForgeConfigSpec.IntValue COUNTDOWN_BOMB_TICKS;
    public static final Map<String, ForgeConfigSpec.BooleanValue> WARP_EVENTS;
    public static final ForgeConfigSpec.DoubleValue TERRAIN_CONVERSION_THRESHOLD;
    public static final ForgeConfigSpec.IntValue TERRAIN_CONVERSION_BUDGET_PER_TICK;
    public static final ForgeConfigSpec.IntValue VIS_GENERATOR_EU_PER_VIS;
    public static final ForgeConfigSpec.DoubleValue VIS_GENERATOR_POLLUTION_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue VIS_PROVIDER_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue FLUX_FUEL_CELL_FLUX_PER_TICK;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Underground portal ritual").push("world");
        PORTAL_ORIGIN_DIMENSION = builder.define("portalOriginDimension", "minecraft:overworld",
                value -> value instanceof String id && net.minecraft.resources.ResourceLocation.tryParse(id) != null);
        ALLOW_PORTALS_IN_OTHER_DIMENSIONS = builder.define("allowPortalsInOtherDimensions", false);
        RETURN_PORTAL_USABLE = builder.comment("If false, arrival portals need another diamond to enable return travel.")
                .define("shouldReturnPortalBeUsable", true);
        CHECK_PORTAL_DESTINATION = builder.comment("Reject rituals outside the destination world border; checks every 100 instead of 20 ticks.")
                .define("checkPortalDestination", false);
        builder.pop();

        builder.comment("Industrial pollution system").push("pollution");
        ENABLE_POLLUTION = builder
                .comment("Master switch for the industrial pollution system.")
                .define("enablePollution", true);
        ENABLE_EXPLOSION_POLLUTION = builder
                .comment("Machines emit pollution when they explode.")
                .define("enableExplosionPollution", true);
        MUFFLER_POLLUTION_MULTIPLIER = builder
                .comment("Multiplier applied to multiblock muffler hatch pollution output.")
                .defineInRange("mufflerPollutionMultiplier", 1.0D, 0.0D, 1000.0D);
        FLUX_SCRUBBER_MULTIPLIER = builder
                .comment("TC4R flux scrubbed per operation, scaled by the scrubber machine tier.")
                .defineInRange("fluxScrubberMultiplier", 0.002D, 0.0D, 1.0D);
        POLLUTION_DECAY_PER_TICK = builder
                .comment("Natural pollution decay per tick and per polluted chunk.")
                .defineInRange("pollutionDecayPerTick", 0.001D, 0.0D, 10.0D);
        EFFECT_THRESHOLD = builder
                .comment("Chunk pollution value at which players start receiving harmful effects.")
                .defineInRange("effectThreshold", 10.0D, 0.0D, 1_000_000.0D);
        ENABLE_TERRAIN_CONVERSION = builder
                .comment("Heavy pollution slowly kills the environment: grass turns to sand,",
                        "and water turns to lava at twice the conversion threshold.")
                .define("enableTerrainConversion", true);
        TERRAIN_CONVERSION_THRESHOLD = builder
                .comment("Chunk pollution value at which terrain conversion starts.")
                .defineInRange("terrainConversionThreshold", 25.0D, 0.0D, 1_000_000.0D);
        TERRAIN_CONVERSION_BUDGET_PER_TICK = builder
                .comment("Maximum blocks converted per dimension per tick (performance budget).")
                .defineInRange("terrainConversionBudgetPerTick", 4, 0, 64);
        builder.pop();

        builder.comment("Thaumcraft warp events").push("warp");
        ENABLE_WARP_EVENTS = builder
                .comment("Enable Pollution warp events driven by the player's TC4R warp.")
                .define("enableWarpEvents", true);
        ENABLE_COUNTDOWN_BOMB = builder
                .comment("Allow the countdown bomb warp event to schedule a delayed explosion.")
                .define("enableCountdownBomb", true);
        WARP_EVENT_INTERVAL_TICKS = builder
                .comment("Ticks between warp event rolls for each player.")
                .defineInRange("warpEventIntervalTicks", 200, 20, 12000);
        COUNTDOWN_BOMB_TICKS = builder
                .comment("Fuse length of the countdown bomb warp event.")
                .defineInRange("countdownBombTicks", 200, 20, 1200);
        builder.push("events");
        Map<String, ForgeConfigSpec.BooleanValue> events = new LinkedHashMap<>();
        for (String id : new String[]{"blind", "nausea", "poison", "wither", "weakness", "jump", "wind",
                "blood", "lightning", "obsidian", "mushrooms", "fake_explosion", "fake_rain", "rain",
                "junk", "blink", "swamp", "countdown_bomb", "wither_rose", "fall", "inventory_scramble", "zombie_siege"}) {
            events.put(id, builder.define(id, true));
        }
        WARP_EVENTS = Map.copyOf(events);
        builder.pop();
        builder.pop();

        builder.comment("Vis generator and aura machines").push("aura");
        VIS_GENERATOR_EU_PER_VIS = builder
                .comment("EU produced per unit of vis drained from the Thaumcraft 4R network.")
                .defineInRange("visGeneratorEuPerVis", 250, 1, 1_000_000);
        VIS_GENERATOR_POLLUTION_MULTIPLIER = builder
                .comment("Pollution added per unit of vis drained.")
                .defineInRange("visGeneratorPollutionMultiplier", 0.1D, 0.0D, 1000.0D);
        VIS_PROVIDER_MULTIPLIER = builder
                .comment("Vis recharged per EU spent by the vis provider (upstream visProviderMultiplier).")
                .defineInRange("visProviderMultiplier", 0.05D, 0.0D, 100.0D);
        FLUX_FUEL_CELL_FLUX_PER_TICK = builder
                .comment("Base flux quanta consumed per operation by the flux promoted fuel cell.")
                .defineInRange("fluxFuelCellFluxPerTick", 0.005D, 0.0D, 100.0D);
        builder.pop();

        SPEC = builder.build();
    }

    private PollutionConfig() {}
}
