package meowmel.pollution.dimension.worldgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;

/**
 * 1.20.1 port of the 1.12 {@code WorldGenGarden}.
 *
 * <p>The upstream generator turns a patch of exposed stone into a small garden:
 * dirt is placed on the stone tops, short grass, poppies/dandelions and up to
 * three oak/birch trees are scattered over a 16x16 area with an irregular,
 * gaussian-jittered border.</p>
 *
 * <p>Port notes:</p>
 * <ul>
 *   <li>The palette is unchanged: dirt + {@code minecraft:grass} +
 *       poppy/dandelion + oak/birch logs and leaves.</li>
 *   <li>The 1.12 {@code WorldGenTrees} call is replaced with the vanilla
 *       {@code minecraft:tree} feature using the same trunk heights
 *       (oak 4-5, birch 5-6) and the vanilla blob canopy, so leaf decay data
 *       is written correctly.</li>
 *   <li>Upstream based trees on {@code getTopSolidOrLiquidBlock(pos).up()},
 *       which points one block above the first air position; the port bases the
 *       trunk directly on the first air position above the ground instead.</li>
 *   <li>Wired into the {@code pollution:underground} (Deep Cave) biome through
 *       {@code pollution:underground/garden}, matching upstream: the 1.12
 *       generator only ran the garden from
 *       {@code ChunkGeneratorUndergroundWorld.populateWithVanilla}, i.e. the
 *       fallback path used by the unstyled deep cave biome. The seven styled
 *       underground biomes override {@code populate} with their own decorator
 *       groups and never ran it.</li>
 * </ul>
 */
public class GardenFeature extends Feature<NoneFeatureConfiguration> {

    private static final int GARDEN_SIZE = 16;
    private static final int FLOWER_ATTEMPTS = 50;
    private static final int TREE_ATTEMPTS = 8;
    private static final int MAX_TREES = 3;
    private static final int EDGE_IRREGULARITY = 5;

    private static final TreeConfiguration OAK_TREE = new TreeConfiguration.TreeConfigurationBuilder(
            BlockStateProvider.simple(Blocks.OAK_LOG),
            new StraightTrunkPlacer(4, 1, 0),
            BlockStateProvider.simple(Blocks.OAK_LEAVES),
            new BlobFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 3),
            new TwoLayersFeatureSize(1, 0, 1))
            .dirt(BlockStateProvider.simple(Blocks.DIRT))
            .ignoreVines()
            .build();

    private static final TreeConfiguration BIRCH_TREE = new TreeConfiguration.TreeConfigurationBuilder(
            BlockStateProvider.simple(Blocks.BIRCH_LOG),
            new StraightTrunkPlacer(5, 1, 0),
            BlockStateProvider.simple(Blocks.BIRCH_LEAVES),
            new BlobFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 3),
            new TwoLayersFeatureSize(1, 0, 1))
            .dirt(BlockStateProvider.simple(Blocks.DIRT))
            .ignoreVines()
            .build();

    public GardenFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();

        convertToGrass(level, random, origin);
        generateFlowers(level, random, origin);
        generateTrees(context, origin);
        return true;
    }

    /** Upstream {@code convertToGrass}: dirt on exposed stone tops plus 30% short grass. */
    private static void convertToGrass(WorldGenLevel level, RandomSource random, BlockPos center) {
        for (int x = -GARDEN_SIZE / 2; x < GARDEN_SIZE / 2; x++) {
            for (int z = -GARDEN_SIZE / 2; z < GARDEN_SIZE / 2; z++) {
                int offsetX = x + (int) (random.nextGaussian() * EDGE_IRREGULARITY);
                int offsetZ = z + (int) (random.nextGaussian() * EDGE_IRREGULARITY);
                BlockPos pos = center.offset(offsetX, 0, offsetZ);

                if (!level.hasChunkAt(pos)) {
                    continue;
                }

                if (level.getBlockState(pos).is(Blocks.STONE) && level.isEmptyBlock(pos.above())) {
                    level.setBlock(pos.above(), Blocks.DIRT.defaultBlockState(), 2);
                    if (random.nextFloat() < 0.3F && level.isEmptyBlock(pos.above(2))) {
                        level.setBlock(pos.above(2), Blocks.GRASS.defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    /** Upstream {@code generateFlowers}: 50 attempts, 60% red / 40% yellow flowers. */
    private static void generateFlowers(WorldGenLevel level, RandomSource random, BlockPos center) {
        BlockPos startPos = center.offset(-GARDEN_SIZE / 2, 0, -GARDEN_SIZE / 2);
        for (int attempt = 0; attempt < FLOWER_ATTEMPTS; attempt++) {
            BlockPos pos = startPos.offset(random.nextInt(GARDEN_SIZE), 0, random.nextInt(GARDEN_SIZE));
            BlockPos surfacePos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos);
            if (!level.isEmptyBlock(surfacePos)) {
                continue;
            }
            BlockState flower = random.nextFloat() < 0.6F
                    ? Blocks.POPPY.defaultBlockState()
                    : Blocks.DANDELION.defaultBlockState();
            if (flower.canSurvive(level, surfacePos)) {
                level.setBlock(surfacePos, flower, 2);
            }
        }
    }

    /** Upstream {@code generateTrees}: up to 8 attempts, at most 3 oaks/birches. */
    private static void generateTrees(FeaturePlaceContext<NoneFeatureConfiguration> context, BlockPos center) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos startPos = center.offset(-GARDEN_SIZE / 2, 0, -GARDEN_SIZE / 2);
        int treesGenerated = 0;

        for (int attempt = 0; attempt < TREE_ATTEMPTS && treesGenerated < MAX_TREES; attempt++) {
            BlockPos pos = startPos.offset(random.nextInt(GARDEN_SIZE), 0, random.nextInt(GARDEN_SIZE));
            BlockPos surfacePos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos);
            TreeConfiguration config = random.nextBoolean() ? OAK_TREE : BIRCH_TREE;
            FeaturePlaceContext<TreeConfiguration> treeContext = new FeaturePlaceContext<>(
                    context.topFeature(), level, context.chunkGenerator(), random, surfacePos, config);
            if (Feature.TREE.place(treeContext)) {
                treesGenerated++;
            }
        }
    }
}
