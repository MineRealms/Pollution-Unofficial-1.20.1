package meowmel.pollution.dimension.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import meowmel.pollution.dimension.biome.POAlfheimBiomeSource;
import meowmel.pollution.dimension.biome.WorldSeededBiomeSource;
import meowmel.pollution.common.block.PollutionPlantBlocks;
import net.minecraft.core.*;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import vazkii.botania.common.block.BotaniaBlocks;
import java.util.*;
import java.util.concurrent.*;

/** WorldEngine height interpolation and biome layers, with modern carvers and decoration. */
public final class AlfheimChunkGenerator extends NoiseBasedChunkGenerator {
    public static final Codec<AlfheimChunkGenerator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(AlfheimChunkGenerator::getBiomeSource),
            NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(AlfheimChunkGenerator::generatorSettings)
    ).apply(instance, AlfheimChunkGenerator::new));
    private volatile long terrainSeed;
    private static final BlockState ROCK = BotaniaBlocks.livingrock.defaultBlockState();
    private static final BlockState WATER = Blocks.WATER.defaultBlockState();
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final Map<String, Profile> PROFILES = Map.ofEntries(
            Map.entry("alfheim_field", new Profile(1.8, 2, 64, 2, false, 0)),
            Map.entry("alfheim_giant_flower_field", new Profile(1.8, 2, 64, 2, false, 0)),
            Map.entry("alfheim_beach", new Profile(1.33, 1.4, 58, 4, false, 1)),
            Map.entry("alfheim_sandbank", new Profile(1.33, .5, 55, 2, false, 1)),
            Map.entry("alfheim_river", new Profile(1.33, 1, 51, 4, false, 2)),
            Map.entry("alfheim_low_plateau", new Profile(1.8, .8, 89, 1, true, 0)),
            Map.entry("alfheim_mid_plateau", new Profile(1.8, 1.6, 113, 1, true, 0)),
            Map.entry("alfheim_high_plateau", new Profile(1.8, 2.4, 137, 1, true, 0)),
            Map.entry("alfheim_high_plateau_forest", new Profile(1.8, 2.4, 137, 1, true, 0)),
            Map.entry("alfheim_high_plateau_field", new Profile(1.8, 2.4, 137, 1, true, 0)),
            Map.entry("alfheim_island_forest", new Profile(1.8, 1, 68, 4, false, 0)),
            Map.entry("alfheim_pit_forest", new Profile(1.8, 1, 64, 4, false, 0)));
    private record Profile(double persistence, double scaleY, int height, int quality, boolean plateau, int surface) {}
    private record Sample(int dx, int dz, int weight) {}
    private static final Sample[][][][] STENCILS = stencils();

    public AlfheimChunkGenerator(BiomeSource source, Holder<NoiseGeneratorSettings> settings) { super(source, settings); }
    @Override protected Codec<? extends ChunkGenerator> codec() { return CODEC; }
    @Override public ChunkGeneratorStructureState createState(HolderLookup<StructureSet> sets, RandomState random, long seed) {
        terrainSeed = seed;
        if (biomeSource instanceof WorldSeededBiomeSource source) source.bindWorldSeed(seed);
        return super.createState(sets, random, seed);
    }
    @Override public int getSeaLevel() { return 57; }
    @Override public int getMinY() { return 0; }
    @Override public int getGenDepth() { return 256; }

    private Profile profile(int x, int z) {
        Holder<Biome> biome = biomeSource instanceof POAlfheimBiomeSource source ? source.biomeAtBlock(x, z)
                : biomeSource.getNoiseBiome(QuartPos.fromBlock(x), 16, QuartPos.fromBlock(z), null);
        return PROFILES.getOrDefault(biome.unwrapKey().map(key -> key.location().getPath()).orElse(""), PROFILES.get("alfheim_field"));
    }
    private Profile profile(int x, int z, Map<Long, Profile> cache) {
        return cache.computeIfAbsent(ChunkPos.asLong(x, z), ignored -> profile(x, z));
    }
    public int terrainHeight(int x, int z) { return terrainHeight(x, z, new HashMap<>()); }
    private int terrainHeight(int x, int z, Map<Long, Profile> cache) {
        Profile center = profile(x, z, cache);
        double persistence = 0, scaleY = 0;
        int height = 0, count = 0;
        for (Sample sample : STENCILS[center.quality][Math.floorMod(x, 16)][Math.floorMod(z, 16)]) {
            Profile p = profile(x + sample.dx, z + sample.dz, cache);
            persistence += p.persistence * sample.weight;
            scaleY += p.scaleY * sample.weight;
            height += p.height * sample.weight;
            count += sample.weight;
        }
        // WorldEngine averages integral surface heights with integer division.
        return Mth.clamp(Mth.floor(height / count + WorldEngineNoise.perlinNoise2D(terrainSeed,
                x / 250.0, z / 250.0, persistence / count, 3) * (scaleY / count)), 1, 255);
    }

    private static Sample[][][][] stencils() {
        Sample[][][][] all = new Sample[5][16][16][];
        for (int q = 0; q <= 4; q++) for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) {
            Map<Long, Integer> counts = new LinkedHashMap<>();
            for (int angle = 0; angle <= 360; angle++) {
                float radians = angle * (float) Math.PI / 180f;
                for (int radius = 0; radius <= q; radius++) {
                    int dx = Mth.floor(x + 4 + Mth.cos(radians) * radius) - x - 4;
                    int dz = Mth.floor(z + 4 + Mth.sin(radians) * radius) - z - 4;
                    counts.merge(ChunkPos.asLong(dx, dz), 1, Integer::sum);
                }
            }
            all[q][x][z] = counts.entrySet().stream().map(e -> {
                ChunkPos p = new ChunkPos(e.getKey()); return new Sample(p.x, p.z, e.getValue());
            }).toArray(Sample[]::new);
        }
        return all;
    }

    @Override public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender,
            RandomState random, StructureManager structures, ChunkAccess chunk) {
        Map<Long, Profile> cache = new HashMap<>();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) {
            int worldX = chunk.getPos().getMinBlockX() + x, worldZ = chunk.getPos().getMinBlockZ() + z;
            int height = terrainHeight(worldX, worldZ, cache);
            for (int y = 0; y <= Math.max(height, 57); y++) {
                chunk.setBlockState(pos.set(worldX, y, worldZ), y <= height ? ROCK : WATER, false);
            }
        }
        Heightmap.primeHeightmaps(chunk, EnumSet.of(Heightmap.Types.OCEAN_FLOOR_WG, Heightmap.Types.WORLD_SURFACE_WG));
        return CompletableFuture.completedFuture(chunk);
    }

    // Apply source layers after carving so caves do not erase the upper soil pass.
    @Override public void buildSurface(WorldGenRegion region, StructureManager structures, RandomState random, ChunkAccess chunk) {}
    @Override public void applyCarvers(WorldGenRegion region, long seed, RandomState random, BiomeManager biomes,
            StructureManager structures, ChunkAccess chunk, GenerationStep.Carving carving) {
        super.applyCarvers(region, seed, random, biomes, structures, chunk, carving);
        if (carving != GenerationStep.Carving.AIR) return;
        Random layers = new Random(terrainSeed * (long) Math.pow(chunk.getPos().x, 3)
                + (long) Math.pow(chunk.getPos().z, 2) * 9874L + 7684053L);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) {
            int worldX = chunk.getPos().getMinBlockX() + x, worldZ = chunk.getPos().getMinBlockZ() + z;
            Profile p = profile(worldX, worldZ);
            int top = Math.min(255, chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z));
            while (top > 0 && (chunk.getBlockState(pos.set(worldX, top, worldZ)).isAir()
                    || !chunk.getFluidState(pos).isEmpty())) top--;
            BlockState filler = p.surface == 1 ? PollutionPlantBlocks.ALFHEIM_ELVEN_SAND.get().defaultBlockState()
                    : p.surface == 2 ? Blocks.CLAY.defaultBlockState() : Blocks.DIRT.defaultBlockState();
            int depth = p.plateau ? layers.nextInt(3) : 4 + layers.nextInt(3);
            for (int y = top; y >= Math.max(0, top - depth); y--) {
                if (chunk.getBlockState(pos.set(worldX, y, worldZ)).is(BotaniaBlocks.livingrock)) chunk.setBlockState(pos, filler, false);
            }
            boolean submerged = !chunk.getFluidState(pos.set(worldX, top + 1, worldZ)).isEmpty();
            if (p.surface != 0 || !submerged) {
                BlockState surface = p.surface == 1 ? filler : p.surface == 2
                        ? (layers.nextBoolean() ? filler : Blocks.GRAVEL.defaultBlockState()) : Blocks.GRASS_BLOCK.defaultBlockState();
                if (chunk.getBlockState(pos.set(worldX, top, worldZ)).equals(filler)) chunk.setBlockState(pos, surface, false);
            }
            chunk.setBlockState(pos.set(worldX, 0, worldZ), Blocks.BEDROCK.defaultBlockState(), false);
        }
    }

    @Override public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState random) {
        int height = terrainHeight(x, z);
        return (type.isOpaque().test(WATER) ? Math.max(57, height) : height) + 1;
    }
    @Override public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState random) {
        BlockState[] states = new BlockState[256];
        int height = terrainHeight(x, z);
        for (int y = 0; y < states.length; y++) states[y] = y == 0 ? Blocks.BEDROCK.defaultBlockState()
                : y <= height ? ROCK : y <= 57 ? WATER : AIR;
        return new NoiseColumn(0, states);
    }
    @Override public void addDebugScreenInfo(List<String> lines, RandomState random, BlockPos pos) {
        lines.add("WorldEngine height: " + terrainHeight(pos.getX(), pos.getZ()));
    }
}
