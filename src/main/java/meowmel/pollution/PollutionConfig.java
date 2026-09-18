package meowmel.pollution;

import net.minecraftforge.common.ForgeConfigSpec;

public final class PollutionConfig {

    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue ENABLE_POLLUTION;
    public static final ForgeConfigSpec.BooleanValue ENABLE_EXPLOSION_POLLUTION;
    public static final ForgeConfigSpec.DoubleValue MUFFLER_POLLUTION_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue FLUX_SCRUBBER_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue POLLUTION_DECAY_PER_TICK;
    public static final ForgeConfigSpec.DoubleValue EFFECT_THRESHOLD;
    public static final ForgeConfigSpec.IntValue VIS_GENERATOR_EU_PER_VIS;
    public static final ForgeConfigSpec.DoubleValue VIS_GENERATOR_POLLUTION_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue VIS_PROVIDER_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue FLUX_FUEL_CELL_FLUX_PER_TICK;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

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
                .comment("Fraction of the chunk pollution that is scrubbed per TC4R flux consumed.")
                .defineInRange("fluxScrubberMultiplier", 0.002D, 0.0D, 1.0D);
        POLLUTION_DECAY_PER_TICK = builder
                .comment("Natural pollution decay per tick and per polluted chunk.")
                .defineInRange("pollutionDecayPerTick", 0.001D, 0.0D, 10.0D);
        EFFECT_THRESHOLD = builder
                .comment("Chunk pollution value at which players start receiving harmful effects.")
                .defineInRange("effectThreshold", 10.0D, 0.0D, 1_000_000.0D);
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
