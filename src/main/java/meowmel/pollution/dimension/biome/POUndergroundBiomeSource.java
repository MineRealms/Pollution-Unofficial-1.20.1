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
 * 1.20.1 port of {@code BiomeProviderUnderground} + {@code GenLayerUndergroundBiomes}.
 *
 * <p>Modern {@link BiomeSource} replaces the 1.12 GenLayer/BiomeCache machinery.
 * The distribution structure is unchanged: the 7 style biomes are "window
 * islands" in the WorldEngine noise value range, everything between them is the
 * deep-cave fallback biome.</p>
 *
 * <p>Coordinates: 1.12 queried the provider with block coordinates, 1.20.1
 * queries biome sources with quart coordinates, so the noise is evaluated at
 * {@code QuartPos.toBlock(...)} to preserve the original 4000-block biome
 * scale.</p>
 *
 * <p>TODO: the current {@code data/pollution/dimension/underground.json} still
 * uses {@code minecraft:fixed}; point its {@code biome_source.type} at
 * {@code pollution:underground} (with a {@code seed} field) to enable the
 * 8-biome distribution. That file is owned by the dimension batch.</p>
 */
public class POUndergroundBiomeSource extends BiomeSource {

    /** Upstream GenLayerUndergroundBiomes constants. */
    private static final double SCALE = 4000.0D;
    private static final double NOISE_AMPLITUDE = 0.4D;
    private static final double WINDOW = 0.06D;
    private static final double SLOT_SPACING = NOISE_AMPLITUDE * 2.0D / 7.0D;
    private static final WorldEngineNoise.NoiseProfile BIOME_NOISE =
            WorldEngineNoise.profile(1.2D, 6);

    public static final Codec<POUndergroundBiomeSource> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.LONG.fieldOf("seed").forGetter(source -> source.seed),
                    RegistryOps.retrieveElement(POBiomes.MAGMA_CAVE),
                    RegistryOps.retrieveElement(POBiomes.DESERT_CAVE),
                    RegistryOps.retrieveElement(POBiomes.PRIMORDIAL_CAVE),
                    RegistryOps.retrieveElement(POBiomes.CRYSTAL_CLUSTER),
                    RegistryOps.retrieveElement(POBiomes.MUSHROOM_FOREST),
                    RegistryOps.retrieveElement(POBiomes.LUSH_CAVE),
                    RegistryOps.retrieveElement(POBiomes.STALACTITE_CLUSTER),
                    RegistryOps.retrieveElement(POBiomes.UNDERGROUND)
            ).apply(instance, POUndergroundBiomeSource::new));

    private final long seed;
    private final long noiseSeed;
    private final Holder<Biome> magmaCave;
    private final Holder<Biome> desertCave;
    private final Holder<Biome> primordialCave;
    private final Holder<Biome> crystalCluster;
    private final Holder<Biome> mushroomForest;
    private final Holder<Biome> lushCave;
    private final Holder<Biome> stalactiteCluster;
    private final Holder<Biome> deepCave;

    private POUndergroundBiomeSource(long seed,
                                     Holder<Biome> magmaCave,
                                     Holder<Biome> desertCave,
                                     Holder<Biome> primordialCave,
                                     Holder<Biome> crystalCluster,
                                     Holder<Biome> mushroomForest,
                                     Holder<Biome> lushCave,
                                     Holder<Biome> stalactiteCluster,
                                     Holder<Biome> deepCave) {
        this.seed = seed;
        this.noiseSeed = (long) Math.pow((double) (seed * 84L), 6.0D);
        this.magmaCave = magmaCave;
        this.desertCave = desertCave;
        this.primordialCave = primordialCave;
        this.crystalCluster = crystalCluster;
        this.mushroomForest = mushroomForest;
        this.lushCave = lushCave;
        this.stalactiteCluster = stalactiteCluster;
        this.deepCave = deepCave;
    }

    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.of(magmaCave, desertCave, primordialCave, crystalCluster,
                mushroomForest, lushCave, stalactiteCluster, deepCave);
    }

    @Override
    public Holder<Biome> getNoiseBiome(int quartX, int quartY, int quartZ, Climate.Sampler sampler) {
        int blockX = QuartPos.toBlock(quartX);
        int blockZ = QuartPos.toBlock(quartZ);

        double value = WorldEngineNoise.perlinNoise2D(
                noiseSeed, (double) blockX / SCALE, (double) blockZ / SCALE, BIOME_NOISE) * NOISE_AMPLITUDE;

        int slot = (int) Math.floor((value + NOISE_AMPLITUDE) / SLOT_SPACING);
        if (slot < 0 || slot >= 7) {
            return deepCave;
        }
        double center = -NOISE_AMPLITUDE + SLOT_SPACING * (slot + 0.5D);
        if (Math.abs(value - center) > WINDOW / 2.0D) {
            return deepCave;
        }
        return switch (slot) {
            case 0 -> magmaCave;
            case 1 -> desertCave;
            case 2 -> primordialCave;
            case 3 -> crystalCluster;
            case 4 -> mushroomForest;
            case 5 -> lushCave;
            case 6 -> stalactiteCluster;
            default -> deepCave;
        };
    }
}
