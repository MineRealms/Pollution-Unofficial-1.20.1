package meowmel.pollution.common.block.plant.rainbow;

import meowmel.pollution.common.block.PollutionPlantBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Modern port of the 1.12 {@code RainbowTreeGenerator}.
 *
 * <p>The upstream generator placed GregTech Food Option {@code rainbowwood} logs;
 * the port has no GTFO dependency and no rainbow log block of its own, so the
 * trunk and branches use vanilla oak logs as a placeholder. The canopy uses the
 * ported {@code pollution:rainbow_leaves}. The small and large (2x2) forms keep
 * the upstream proportions and branch layout.</p>
 */
public final class RainbowTreeGenerator {

    private static final int[][] BRANCH_DIRECTIONS = {
            {1, 0}, {-1, 0}, {0, 1}, {0, -1},
            {1, 1}, {-1, 1}, {1, -1}, {-1, -1}
    };

    private static final BlockState LOG = Blocks.OAK_LOG.defaultBlockState();
    private static final BlockState LEAVES = PollutionPlantBlocks.RAINBOW_LEAVES.get()
            .defaultBlockState()
            .setValue(LeavesBlock.PERSISTENT, true)
            .setValue(LeavesBlock.DISTANCE, 7);

    /** Tries to grow a tree from a single sapling, preferring the 2x2 large form. */
    public static boolean grow(ServerLevel level, BlockPos pos, RandomSource random) {
        BlockPos largeOrigin = findTwoByTwo(level, pos);
        if (largeOrigin != null && hasLargeGround(level, largeOrigin)
                && hasSpace(level, largeOrigin, 7, 30)) {
            BlockState[] oldStates = new BlockState[4];
            int index = 0;
            for (int dx = 0; dx < 2; dx++) {
                for (int dz = 0; dz < 2; dz++) {
                    BlockPos saplingPos = largeOrigin.offset(dx, 0, dz);
                    oldStates[index++] = level.getBlockState(saplingPos);
                    level.removeBlock(saplingPos, false);
                }
            }
            if (buildLarge(level, random, largeOrigin)) {
                return true;
            }
            index = 0;
            for (int dx = 0; dx < 2; dx++) {
                for (int dz = 0; dz < 2; dz++) {
                    level.setBlock(largeOrigin.offset(dx, 0, dz), oldStates[index++], 3);
                }
            }
            return false;
        }
        return generateSmall(level, pos, random);
    }

    public static boolean generateSmall(ServerLevel level, BlockPos base, RandomSource random) {
        if (!canGenerateSmall(level, base)) {
            return false;
        }
        int height = 7 + random.nextInt(5);
        for (int y = 0; y < height; y++) {
            placeLog(level, base.above(y), Direction.Axis.Y);
        }
        int crownStart = height - 4;
        for (int y = crownStart; y <= height + 1; y++) {
            int distanceFromTop = Math.abs(height - y);
            int radius = distanceFromTop == 0 ? 2 : (distanceFromTop <= 2 ? 3 : 2);
            if (y == height + 1) {
                radius = 1;
            }
            placeCanopyLayer(level, random, base.above(y), radius);
        }
        return true;
    }

    private static boolean buildLarge(ServerLevel level, RandomSource random, BlockPos origin) {
        int height = 23 + random.nextInt(5);
        for (int y = 0; y < height; y++) {
            for (int dx = 0; dx < 2; dx++) {
                for (int dz = 0; dz < 2; dz++) {
                    placeLog(level, origin.offset(dx, y, dz), Direction.Axis.Y);
                }
            }
        }
        // Four low root flares visually anchor the 2x2 trunk.
        placeLog(level, origin.west(), Direction.Axis.X);
        placeLog(level, origin.offset(2, 0, 1), Direction.Axis.X);
        placeLog(level, origin.north(), Direction.Axis.Z);
        placeLog(level, origin.offset(1, 0, 2), Direction.Axis.Z);

        for (int i = 0; i < BRANCH_DIRECTIONS.length; i++) {
            int[] direction = BRANCH_DIRECTIONS[i];
            int startY = height - 11 + (i % 4) * 2;
            int length = 3 + random.nextInt(2);
            BlockPos endpoint = growBranch(level, origin.offset(0, startY, 0),
                    direction[0], direction[1], length, i % 2);
            placeCanopyBlob(level, random, endpoint, 2 + random.nextInt(2));
        }
        for (int y = height - 5; y <= height + 3; y++) {
            int radius = y < height ? 5 : Math.max(1, 4 - (y - height));
            placeCanopyLayer(level, random, origin.above(y), radius);
        }
        placeLog(level, origin.above(height), Direction.Axis.Y);
        placeLog(level, origin.above(height + 1), Direction.Axis.Y);
        return true;
    }

    private static BlockPos growBranch(ServerLevel level, BlockPos start, int dx, int dz,
                                       int length, int verticalOffset) {
        BlockPos previous = start;
        for (int step = 1; step <= length; step++) {
            int y = (step + verticalOffset) / 2;
            BlockPos next = start.offset(dx * step, y, dz * step);
            Direction.Axis axis = dx != 0 ? Direction.Axis.X : Direction.Axis.Z;
            if (dx != 0 && dz != 0) {
                axis = step % 2 == 0 ? Direction.Axis.X : Direction.Axis.Z;
            }
            placeLine(level, previous, next, axis);
            previous = next;
        }
        return previous;
    }

    private static void placeLine(ServerLevel level, BlockPos from, BlockPos to, Direction.Axis axis) {
        int length = Math.max(Math.abs(to.getX() - from.getX()),
                Math.max(Math.abs(to.getY() - from.getY()), Math.abs(to.getZ() - from.getZ())));
        for (int i = 1; i <= Math.max(1, length); i++) {
            double fraction = i / (double) Math.max(1, length);
            BlockPos pos = new BlockPos(
                    (int) Math.round(from.getX() + (to.getX() - from.getX()) * fraction),
                    (int) Math.round(from.getY() + (to.getY() - from.getY()) * fraction),
                    (int) Math.round(from.getZ() + (to.getZ() - from.getZ()) * fraction));
            placeLog(level, pos, axis);
        }
    }

    private static void placeCanopyBlob(ServerLevel level, RandomSource random, BlockPos center, int radius) {
        for (int y = -2; y <= 2; y++) {
            int layerRadius = Math.max(1, radius - Math.abs(y));
            placeCanopyLayer(level, random, center.above(y), layerRadius);
        }
    }

    private static void placeCanopyLayer(ServerLevel level, RandomSource random, BlockPos center, int radius) {
        int radiusSquared = radius * radius;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int distance = dx * dx + dz * dz;
                if (distance <= radiusSquared + random.nextInt(3) - 1) {
                    placeLeaf(level, center.offset(dx, 0, dz));
                }
            }
        }
    }

    private static boolean canGenerateSmall(ServerLevel level, BlockPos base) {
        return canGrowOn(level.getBlockState(base.below()))
                && hasSpace(level, base, 3, 12);
    }

    private static boolean hasLargeGround(ServerLevel level, BlockPos origin) {
        for (int dx = 0; dx < 2; dx++) {
            for (int dz = 0; dz < 2; dz++) {
                if (!canGrowOn(level.getBlockState(origin.offset(dx, -1, dz)))) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean hasSpace(ServerLevel level, BlockPos base, int radius, int height) {
        if (base.getY() < level.getMinBuildHeight() + 1
                || base.getY() + height + 3 >= level.getMaxBuildHeight()) {
            return false;
        }
        for (int y = 0; y <= height + 2; y++) {
            int scanRadius = y == 0 ? 0 : (y < 3 ? 1 : radius);
            for (int dx = -scanRadius; dx <= scanRadius; dx++) {
                for (int dz = -scanRadius; dz <= scanRadius; dz++) {
                    BlockPos pos = base.offset(dx, y, dz);
                    if (!level.isLoaded(pos) || !canReplace(level.getBlockState(pos))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static boolean canGrowOn(BlockState state) {
        return state.is(BlockTags.DIRT) || state.is(Blocks.FARMLAND);
    }

    private static boolean canReplace(BlockState state) {
        return state.isAir()
                || state.is(BlockTags.LEAVES)
                || state.is(Blocks.VINE)
                || state.is(PollutionPlantBlocks.RAINBOW_SAPLING.get())
                || state.canBeReplaced();
    }

    private static void placeLog(ServerLevel level, BlockPos pos, Direction.Axis axis) {
        BlockState existing = level.getBlockState(pos);
        if (canReplace(existing) || existing.is(LEAVES.getBlock())) {
            level.setBlock(pos, LOG.setValue(RotatedPillarBlock.AXIS, axis), 2);
        }
    }

    private static void placeLeaf(ServerLevel level, BlockPos pos) {
        if (canReplace(level.getBlockState(pos))) {
            level.setBlock(pos, LEAVES, 2);
        }
    }

    private static BlockPos findTwoByTwo(ServerLevel level, BlockPos pos) {
        for (int dx = -1; dx <= 0; dx++) {
            for (int dz = -1; dz <= 0; dz++) {
                BlockPos origin = pos.offset(dx, 0, dz);
                if (isSapling(level, origin)
                        && isSapling(level, origin.east())
                        && isSapling(level, origin.south())
                        && isSapling(level, origin.east().south())) {
                    return origin;
                }
            }
        }
        return null;
    }

    private static boolean isSapling(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).is(PollutionPlantBlocks.RAINBOW_SAPLING.get());
    }

    private RainbowTreeGenerator() {}
}
