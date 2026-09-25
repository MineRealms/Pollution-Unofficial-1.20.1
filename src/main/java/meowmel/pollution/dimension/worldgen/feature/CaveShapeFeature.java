package meowmel.pollution.dimension.worldgen.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** Upstream cave decorations, positioned by the datapack's floor/ceiling scans. */
public final class CaveShapeFeature extends Feature<NoneFeatureConfiguration> {
    public enum Shape { QUARTZ_PILLAR, BIG_VINES, STALACTITES, RED_CLUSTER, BROWN_CLUSTER, GLOWSTONE }
    private final Shape shape;

    public CaveShapeFeature(Shape shape) {
        super(NoneFeatureConfiguration.CODEC);
        this.shape = shape;
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        return switch (shape) {
            case QUARTZ_PILLAR -> pillar(context);
            case BIG_VINES -> vines(context);
            case STALACTITES -> stalactites(context);
            case RED_CLUSTER -> cluster(context, Blocks.RED_MUSHROOM_BLOCK.defaultBlockState());
            case BROWN_CLUSTER -> cluster(context, Blocks.BROWN_MUSHROOM_BLOCK.defaultBlockState());
            case GLOWSTONE -> glowstone(context);
        };
    }

    private static boolean air(WorldGenLevel level, BlockPos pos) {
        return !level.isOutsideBuildHeight(pos) && level.ensureCanWrite(pos) && level.isEmptyBlock(pos);
    }

    private static boolean pillar(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        var level = context.level();
        var random = context.random();
        BlockPos origin = context.origin();
        if (!level.getBlockState(origin.below()).isSolid()) return false;
        Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        int height = 8 + random.nextInt(9);
        int thickness = random.nextBoolean() ? 2 : 3;
        // Validate the complete slanted footprint before changing any blocks.
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < thickness; x++) for (int z = 0; z < thickness; z++) {
                if (!air(level, origin.relative(direction, y / 3).offset(x, y, z))) return false;
            }
        }
        for (int y = 0; y < height; y++) {
            BlockState state = (y == height - 1 ? Blocks.QUARTZ_BLOCK : Blocks.QUARTZ_PILLAR).defaultBlockState();
            for (int x = 0; x < thickness; x++) for (int z = 0; z < thickness; z++) {
                level.setBlock(origin.relative(direction, y / 3).offset(x, y, z), state, 2);
            }
        }
        return true;
    }

    private static boolean vines(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        var level = context.level();
        var random = context.random();
        BlockPos origin = context.origin();
        if (!level.getBlockState(origin.above()).isSolid()) return false;
        int height = 20 + random.nextInt(26);
        int thickness = random.nextBoolean() ? 1 : 2;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < thickness; x++) for (int z = 0; z < thickness; z++) {
                if (!air(level, origin.offset(x, -y, z))) return false;
            }
        }
        BlockState leaves = Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < thickness; x++) for (int z = 0; z < thickness; z++) {
                level.setBlock(origin.offset(x, -y, z), leaves, 2);
            }
        }
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < thickness; x++) for (int z = 0; z < thickness; z++) {
                BlockPos core = origin.offset(x, -y, z);
                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    BlockPos target = core.relative(direction);
                    if (air(level, target)) level.setBlock(target, Blocks.VINE.defaultBlockState()
                            .setValue(VineBlock.getPropertyForFace(direction.getOpposite()), true), 2);
                }
            }
        }
        return true;
    }

    private static BlockPos scattered(BlockPos origin, RandomSource random, int vertical) {
        return origin.offset(random.nextInt(8) - random.nextInt(8), vertical,
                random.nextInt(8) - random.nextInt(8));
    }

    private static boolean stalactites(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        var level = context.level();
        var random = context.random();
        boolean placed = false;
        for (int attempt = 0; attempt < 64; attempt++) {
            BlockPos base = scattered(context.origin(), random, random.nextInt(4) - random.nextInt(4));
            if (!air(level, base) || !level.getBlockState(base.below()).isSolid()) continue;
            int height = 3 + random.nextInt(8);
            for (int y = 0; y < height && air(level, base.above(y)); y++) {
                level.setBlock(base.above(y), Blocks.STONE.defaultBlockState(), 2);
                placed = true;
            }
        }
        return placed;
    }

    private static boolean cluster(FeaturePlaceContext<NoneFeatureConfiguration> context, BlockState state) {
        var level = context.level();
        var random = context.random();
        BlockPos origin = context.origin();
        if (!air(level, origin) || !level.getBlockState(origin.above()).isSolid()) return false;
        level.setBlock(origin, state, 2);
        for (int attempt = 0; attempt < 1500; attempt++) {
            BlockPos target = scattered(origin, random, -random.nextInt(12));
            if (!air(level, target)) continue;
            int adjacent = 0;
            for (Direction direction : Direction.values()) {
                if (level.getBlockState(target.relative(direction)).is(state.getBlock())) adjacent++;
            }
            if (adjacent == 1) level.setBlock(target, state, 2);
        }
        return true;
    }

    private static boolean glowstone(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        var level = context.level();
        var random = context.random();
        boolean placed = false;
        for (int attempt = 0; attempt < 16; attempt++) {
            BlockPos target = context.origin().offset(random.nextInt(16) - 8, 0, random.nextInt(16) - 8);
            if (!air(level, target)) continue;
            for (int above = 1; above <= 3; above++) {
                if (!level.getBlockState(target.above(above)).isSolid()) continue;
                int length = 1 + random.nextInt(3);
                for (int y = 0; y < length && air(level, target.below(y)); y++) {
                    level.setBlock(target.below(y), Blocks.GLOWSTONE.defaultBlockState(), 2);
                    placed = true;
                }
                break;
            }
        }
        return placed;
    }
}
