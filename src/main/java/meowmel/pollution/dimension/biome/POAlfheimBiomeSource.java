package meowmel.pollution.dimension.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import meowmel.pollution.dimension.worldgen.WorldEngineNoise;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

import java.util.stream.Stream;

/**
 * 1.20.1 port of {@code BiomeProviderAlfheim} (WorldEngine biome-map selection).
 *
 * <p>Each profile is a {@code [min, max]} window on the WorldEngine noise value;
 * the narrowest matching window wins and {@code alfheim_field} is the fallback,
 * exactly as upstream.</p>
 *
 * <p>The dimension stem enables world-seed binding. Legacy serialized sources keep
 * their stored seed unless use_world_seed is explicitly enabled.</p>
 */
public class POAlfheimBiomeSource extends BiomeSource implements WorldSeededBiomeSource {

    private static final double PERSISTENCE = 1.2D;
    private static final int OCTAVES = 6;
    private static final double SCALE_X = 8000.0D;
    private static final double SCALE_Y = 0.4D;
    private static final WorldEngineNoise.NoiseProfile BIOME_NOISE =
            WorldEngineNoise.profile(PERSISTENCE, OCTAVES);

    public static final Codec<POAlfheimBiomeSource> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.LONG.optionalFieldOf("seed", 0L).forGetter(source -> source.seed),
                    Codec.BOOL.optionalFieldOf("use_world_seed", false).forGetter(source -> source.useWorldSeed),
                    RegistryOps.retrieveElement(POBiomes.ALFHEIM_FIELD),
                    RegistryOps.retrieveElement(POBiomes.ALFHEIM_GIANT_FLOWER_FIELD),
                    RegistryOps.retrieveElement(POBiomes.ALFHEIM_BEACH),
                    RegistryOps.retrieveElement(POBiomes.ALFHEIM_SANDBANK),
                    RegistryOps.retrieveElement(POBiomes.ALFHEIM_RIVER),
                    RegistryOps.retrieveElement(POBiomes.ALFHEIM_LOW_PLATEAU),
                    RegistryOps.retrieveElement(POBiomes.ALFHEIM_MID_PLATEAU),
                    RegistryOps.retrieveElement(POBiomes.ALFHEIM_HIGH_PLATEAU),
                    RegistryOps.retrieveElement(POBiomes.ALFHEIM_HIGH_PLATEAU_FOREST),
                    RegistryOps.retrieveElement(POBiomes.ALFHEIM_HIGH_PLATEAU_FIELD),
                    RegistryOps.retrieveElement(POBiomes.ALFHEIM_ISLAND_FOREST),
                    RegistryOps.retrieveElement(POBiomes.ALFHEIM_PIT_FOREST)
            ).apply(instance, POAlfheimBiomeSource::new));

    private final long seed;
    private volatile long noiseSeed;
    private final boolean useWorldSeed;
    private final Holder<Biome> field;
    private final Profile[] profiles;

    private POAlfheimBiomeSource(long seed, boolean useWorldSeed,
                                 Holder<Biome> field,
                                 Holder<Biome> giantFlowerField,
                                 Holder<Biome> beach,
                                 Holder<Biome> sandbank,
                                 Holder<Biome> river,
                                 Holder<Biome> lowPlateau,
                                 Holder<Biome> midPlateau,
                                 Holder<Biome> highPlateau,
                                 Holder<Biome> highPlateauForest,
                                 Holder<Biome> highPlateauField,
                                 Holder<Biome> islandForest,
                                 Holder<Biome> pitForest) {
        this.seed = seed;
        this.useWorldSeed = useWorldSeed;
        this.noiseSeed = (long) Math.pow((double) (seed * 84L), 6.0D);
        this.field = field;
        // Registration order from WorldProviderAlfheim.genSettings.
        this.profiles = new Profile[]{
                new Profile(field, -0.55D, 0.82D),
                new Profile(giantFlowerField, 1.0D, 10.0D),
                new Profile(beach, -0.5D, -0.35D),
                new Profile(sandbank, -0.41D, -0.38D),
                new Profile(river, -0.48D, -0.38D),
                new Profile(lowPlateau, 0.2D, 0.78D),
                new Profile(midPlateau, 0.3D, 0.75D),
                new Profile(highPlateau, 0.4D, 0.7D),
                new Profile(highPlateauForest, 0.49D, 0.58D),
                new Profile(highPlateauField, 0.43D, 0.65D),
                new Profile(islandForest, -10.0D, 0.82D),
                new Profile(pitForest, 0.82D, 1.0D)
        };
    }

    @Override
    public void bindWorldSeed(long worldSeed) {
        if (useWorldSeed) noiseSeed = WorldEngineNoise.mixWorldSeed(worldSeed ^ seed);
    }

    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return java.util.Arrays.stream(profiles).map(profile -> profile.biome);
    }

    @Override
    public Holder<Biome> getNoiseBiome(int quartX, int quartY, int quartZ, Climate.Sampler sampler) {
        return biomeAtBlock(QuartPos.toBlock(quartX), QuartPos.toBlock(quartZ));
    }

    public Holder<Biome> biomeAtBlock(int blockX, int blockZ) {
        double mapValue = WorldEngineNoise.perlinNoise2D(
                noiseSeed, (double) blockX / SCALE_X, (double) blockZ / SCALE_X, BIOME_NOISE) * SCALE_Y;

        Profile selected = null;
        for (Profile profile : profiles) {
            if (mapValue >= profile.min && mapValue <= profile.max
                    && (selected == null || profile.width() < selected.width())) {
                selected = profile;
            }
        }
        return selected == null ? field : selected.biome;
    }

    private static final class Profile {
        private final Holder<Biome> biome;
        private final double min;
        private final double max;

        private Profile(Holder<Biome> biome, double min, double max) {
            this.biome = biome;
            this.min = min;
            this.max = max;
        }

        private double width() {
            return max - min;
        }
    }
}
