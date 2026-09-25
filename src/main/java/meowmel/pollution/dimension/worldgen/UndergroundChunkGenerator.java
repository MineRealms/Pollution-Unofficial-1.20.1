package meowmel.pollution.dimension.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import meowmel.pollution.common.block.PollutionStoneBlocks;
import meowmel.pollution.dimension.biome.WorldSeededBiomeSource;
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
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import java.util.*;
import java.util.concurrent.*;

/** Source 5x17x5 nether-style noise grid, interpolated to 256-high underground terrain. */
public final class UndergroundChunkGenerator extends NoiseBasedChunkGenerator {
    public static final Codec<UndergroundChunkGenerator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(UndergroundChunkGenerator::getBiomeSource),
            NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(UndergroundChunkGenerator::generatorSettings)
    ).apply(instance, UndergroundChunkGenerator::new));
    private volatile Noise noise = new Noise(0);

    public UndergroundChunkGenerator(BiomeSource source, Holder<NoiseGeneratorSettings> settings) { super(source, settings); }
    @Override protected Codec<? extends ChunkGenerator> codec() { return CODEC; }
    @Override public ChunkGeneratorStructureState createState(HolderLookup<StructureSet> sets, RandomState random, long seed) {
        noise = new Noise(seed);
        if (biomeSource instanceof WorldSeededBiomeSource source) source.bindWorldSeed(seed);
        return super.createState(sets, random, seed);
    }
    @Override public int getSeaLevel() { return 63; }
    @Override public int getMinY() { return 0; }
    @Override public int getGenDepth() { return 256; }

    private static final class Noise {
        final ImprovedNoise[] low1, low2, blend, gravel;
        Noise(long seed) {
            var random = new LegacyRandomSource(seed);
            low1 = octaves(random, 16); low2 = octaves(random, 16);
            blend = octaves(random, 8); gravel = octaves(random, 4);
        }
        private static ImprovedNoise[] octaves(LegacyRandomSource random, int count) {
            ImprovedNoise[] result = new ImprovedNoise[count];
            for (int i = 0; i < count; i++) result[i] = new ImprovedNoise(random);
            return result;
        }
        private static double sample(ImprovedNoise[] octaves, double x, double y, double z) {
            double result = 0, frequency = 1;
            for (ImprovedNoise octave : octaves) {
                double nx = x * frequency, nz = z * frequency;
                // The source folds X/Z to 24 bits to retain precision far from spawn.
                long ix = Mth.lfloor(nx), iz = Mth.lfloor(nz);
                nx = nx - ix + ix % 16777216L;
                nz = nz - iz + iz % 16777216L;
                result += octave.noise(nx, y * frequency, nz) / frequency;
                frequency /= 2;
            }
            return result;
        }
        double value(int x, int y, int z) {
            double a = sample(low1, x * 684.412, y * 2053.236, z * 684.412) / 512;
            double b = sample(low2, x * 684.412, y * 2053.236, z * 684.412) / 512;
            double blendValue = (sample(blend, x * 8.55515, y * 34.2206, z * 8.55515) / 10 + 1) / 2;
            double weight = Math.cos(y * Math.PI * 6 / 16) * 2;
            double edge = Math.min(y, 16 - y);
            if (edge < 4) weight -= Math.pow(4 - edge, 3) * 10;
            double result = Mth.lerp(Mth.clamp(blendValue, 0, 1), a, b) - weight;
            if (y > 13) result = Mth.lerp((y - 13) / 3.0, result, -10);
            return result;
        }
    }

    private double[][][] grid(ChunkPos chunk) {
        double[][][] grid = new double[5][17][5];
        Noise current = noise;
        for (int x = 0; x < 5; x++) for (int z = 0; z < 5; z++) for (int y = 0; y < 17; y++) {
            grid[x][y][z] = current.value(chunk.x * 4 + x, y, chunk.z * 4 + z);
        }
        return grid;
    }
    private static BlockState state(double[][][] grid, int x, int y, int z) {
        if (y < 2 || y > 253) return Blocks.BEDROCK.defaultBlockState();
        int gx = x / 4, gy = y / 16, gz = z / 4;
        double fx = (x % 4) / 4.0, fy = (y % 16) / 16.0, fz = (z % 4) / 4.0;
        double a = Mth.lerp(fy, grid[gx][gy][gz], grid[gx][gy + 1][gz]);
        double b = Mth.lerp(fy, grid[gx + 1][gy][gz], grid[gx + 1][gy + 1][gz]);
        double c = Mth.lerp(fy, grid[gx][gy][gz + 1], grid[gx][gy + 1][gz + 1]);
        double d = Mth.lerp(fy, grid[gx + 1][gy][gz + 1], grid[gx + 1][gy + 1][gz + 1]);
        if (Mth.lerp(fz, Mth.lerp(fx, a, b), Mth.lerp(fx, c, d)) > -.2) {
            return y < 63 ? PollutionStoneBlocks.STONES.get("kimberlite").getDefaultState() : Blocks.STONE.defaultBlockState();
        }
        return y < 63 ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
    }
    @Override public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, RandomState random,
            StructureManager structures, ChunkAccess chunk) {
        var grid = grid(chunk.getPos());
        var pos = new BlockPos.MutableBlockPos();
        for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) for (int y = 0; y < 256; y++) {
            chunk.setBlockState(pos.set(chunk.getPos().getMinBlockX() + x, y, chunk.getPos().getMinBlockZ() + z), state(grid, x, y, z), false);
        }
        Heightmap.primeHeightmaps(chunk, EnumSet.of(Heightmap.Types.OCEAN_FLOOR_WG, Heightmap.Types.WORLD_SURFACE_WG));
        return CompletableFuture.completedFuture(chunk);
    }

    @Override public void buildSurface(WorldGenRegion region, StructureManager structures, RandomState random, ChunkAccess chunk) {
        var pos = new BlockPos.MutableBlockPos();
        var layers = new Random((long) chunk.getPos().x * 341873128712L + (long) chunk.getPos().z * 132897987541L);
        for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) {
            int wx = chunk.getPos().getMinBlockX() + x, wz = chunk.getPos().getMinBlockZ() + z;
            String biome = biomeSource.getNoiseBiome(QuartPos.fromBlock(wx), 16, QuartPos.fromBlock(wz), random.sampler())
                    .unwrapKey().map(key -> key.location().getPath()).orElse("");
            BlockState top = switch (biome) {
                case "mushroom_forest" -> Blocks.MYCELIUM.defaultBlockState();
                case "lush_cave", "primordial_cave" -> Blocks.GRASS_BLOCK.defaultBlockState();
                case "desert_cave" -> Blocks.SAND.defaultBlockState();
                case "magma_cave" -> Blocks.NETHERRACK.defaultBlockState();
                default -> Blocks.STONE.defaultBlockState();
            };
            BlockState filler = top.is(Blocks.SAND) ? Blocks.SANDSTONE.defaultBlockState()
                    : top.is(Blocks.NETHERRACK) ? top : top.is(Blocks.STONE) ? top : Blocks.DIRT.defaultBlockState();
            for (int y = 252; y >= 2; y--) {
                pos.set(wx, y, wz);
                if (chunk.getBlockState(pos).is(Blocks.STONE) && chunk.getBlockState(pos.above()).isAir()) {
                    chunk.setBlockState(pos, top, false);
                    int depth = 1 + layers.nextInt(3);
                    for (int dy = 1; dy <= depth && y - dy > 1; dy++) {
                        if (!chunk.getBlockState(pos.set(wx, y - dy, wz)).is(Blocks.STONE)) break;
                        chunk.setBlockState(pos, filler, false);
                    }
                }
            }
            int depth = Math.min(4, 2 + (int) (Math.abs(Noise.sample(noise.gravel, wx * .125, 0, wz * .125)) / 6));
            boolean water = false;
            for (int y = 62; y > 2; y--) {
                pos.set(wx, y, wz);
                if (chunk.getBlockState(pos).is(Blocks.WATER)) water = true;
                else if (rock(chunk.getBlockState(pos))) {
                    if (water) for (int dy = 0; dy < depth && y - dy > 1; dy++) {
                        pos.set(wx, y - dy, wz);
                        if (!rock(chunk.getBlockState(pos))) break;
                        chunk.setBlockState(pos, Blocks.GRAVEL.defaultBlockState(), false);
                    }
                    break;
                }
            }
            if (x == 0 || z == 0 || x == 15 || z == 15) continue;
            for (int y = 59; y <= 66; y++) {
                pos.set(wx, y, wz);
                if (!rock(chunk.getBlockState(pos))) continue;
                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    var adjacent = chunk.getBlockState(pos.relative(direction));
                    if (adjacent.isAir() || adjacent.is(Blocks.WATER)) {
                        chunk.setBlockState(pos, Blocks.GRAVEL.defaultBlockState(), false); break;
                    }
                }
            }
        }
    }
    private static boolean rock(BlockState state) { return state.is(Blocks.STONE) || state.is(PollutionStoneBlocks.STONES.get("kimberlite").get()); }
    @Override public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState random) { return 256; }
    @Override public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState random) {
        var grid = grid(new ChunkPos(x >> 4, z >> 4));
        var states = new BlockState[256];
        for (int y = 0; y < 256; y++) states[y] = state(grid, x & 15, y, z & 15);
        return new NoiseColumn(0, states);
    }
    @Override public void addDebugScreenInfo(List<String> lines, RandomState random, BlockPos pos) { lines.add("Underground: source noise grid, sea level 63"); }
}
