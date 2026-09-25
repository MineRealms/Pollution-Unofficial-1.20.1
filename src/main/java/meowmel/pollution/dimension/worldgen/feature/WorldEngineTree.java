package meowmel.pollution.dimension.worldgen.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

/** 1.12 state-based port of WorldEngine's WE_TreeGen. */
public final class WorldEngineTree extends WorldEngineTreeBase {

    private final BlockState verticalWood;
    private final BlockState leaves;
    private final Block vine;
    private final int minTreeHeight;
    private final boolean vinesGrowLeaves;
    private final boolean vinesGrowLog;

    public WorldEngineTree(BlockState wood, BlockState leaves, Block vine,
                                   int minTreeHeight, boolean vinesGrowLeaves, boolean vinesGrowLog) {
        this.verticalWood = verticalWood(wood);
        this.leaves = leaves;
        this.vine = vine;
        this.minTreeHeight = minTreeHeight;
        this.vinesGrowLeaves = vinesGrowLeaves;
        this.vinesGrowLog = vinesGrowLog;
    }

    @Override
    public boolean generate(WorldGenLevel world, RandomSource random, BlockPos origin) {
        int height = random.nextInt(3) + minTreeHeight;
        if (origin.getY() <= world.getMinBuildHeight() || origin.getY() + height + 1 > world.getMaxBuildHeight()) {
            return false;
        }

        BlockPos.MutableBlockPos check = new BlockPos.MutableBlockPos();
        for (int y = origin.getY(); y <= origin.getY() + height + 1; y++) {
            int radius = y == origin.getY() ? 0 : (y >= origin.getY() + height - 1 ? 2 : 1);
            for (int x = origin.getX() - radius; x <= origin.getX() + radius; x++) {
                for (int z = origin.getZ() - radius; z <= origin.getZ() + radius; z++) {
                    check.set(x, y, z);
                    if (y < world.getMinBuildHeight() || y >= world.getMaxBuildHeight() || !isReplaceable(world, check)) {
                        return false;
                    }
                }
            }
        }

        BlockPos soilPos = origin.below();
        BlockState soil = world.getBlockState(soilPos);
        if (!soil.is(BlockTags.DIRT)
                || origin.getY() >= world.getMaxBuildHeight() - height - 1) {
            return false;
        }
        world.setBlock(soilPos, Blocks.DIRT.defaultBlockState(), 2);

        BlockPos.MutableBlockPos canopyPos = new BlockPos.MutableBlockPos();
        for (int y = origin.getY() + height - 3; y <= origin.getY() + height; y++) {
            int fromTop = y - origin.getY() - height;
            int radius = 1 - fromTop / 2;
            for (int x = origin.getX() - radius; x <= origin.getX() + radius; x++) {
                int dx = x - origin.getX();
                for (int z = origin.getZ() - radius; z <= origin.getZ() + radius; z++) {
                    int dz = z - origin.getZ();
                    if (Math.abs(dx) != radius || Math.abs(dz) != radius
                            || random.nextInt(2) == 0 || fromTop == 0) {
                        canopyPos.set(x, y, z);
                        BlockState state = world.getBlockState(canopyPos);
                        if (state.isAir()
                                || state.is(BlockTags.LEAVES)) {
                            setBlockAndNotifyAdequately(world, canopyPos, leaves);
                        }
                    }
                }
            }
        }

        BlockPos.MutableBlockPos trunkPos = new BlockPos.MutableBlockPos();
        for (int y = 0; y < height; y++) {
            trunkPos.set(origin.getX(), origin.getY() + y, origin.getZ());
            BlockState state = world.getBlockState(trunkPos);
            if (state.isAir()
                    || state.is(BlockTags.LEAVES)) {
                setBlockAndNotifyAdequately(world, trunkPos, verticalWood);
                if (vinesGrowLog && y > 0 && vine != null) {
                    placeTrunkVine(world, random, trunkPos.west(), 8);
                    placeTrunkVine(world, random, trunkPos.east(), 2);
                    placeTrunkVine(world, random, trunkPos.north(), 1);
                    placeTrunkVine(world, random, trunkPos.south(), 4);
                }
            }
        }

        if (vinesGrowLeaves && vine != null) {
            for (int y = origin.getY() + height - 3; y <= origin.getY() + height; y++) {
                int radius = 2 - (y - origin.getY() - height) / 2;
                for (int x = origin.getX() - radius; x <= origin.getX() + radius; x++) {
                    for (int z = origin.getZ() - radius; z <= origin.getZ() + radius; z++) {
                        BlockPos leafPos = new BlockPos(x, y, z);
                        BlockState state = world.getBlockState(leafPos);
                        if (!state.is(BlockTags.LEAVES)) {
                            continue;
                        }
                        maybeGrowLeafVine(world, random, leafPos.west(), 8);
                        maybeGrowLeafVine(world, random, leafPos.east(), 2);
                        maybeGrowLeafVine(world, random, leafPos.north(), 1);
                        maybeGrowLeafVine(world, random, leafPos.south(), 4);
                    }
                }
            }
        }
        return true;
    }

    private void placeTrunkVine(WorldGenLevel world, RandomSource random, BlockPos pos, int meta) {
        if (random.nextInt(3) > 0 && world.isEmptyBlock(pos)) {
            setBlockAndNotifyAdequately(world, pos, vineState(vine, meta));
        }
    }

    private void maybeGrowLeafVine(WorldGenLevel world, RandomSource random, BlockPos pos, int meta) {
        if (random.nextInt(4) == 0 && world.isEmptyBlock(pos)) {
            growVines(world, pos, meta);
        }
    }

    private void growVines(WorldGenLevel world, BlockPos pos, int meta) {
        BlockState vineState = vineState(vine, meta);
        setBlockAndNotifyAdequately(world, pos, vineState);
        BlockPos cursor = pos;
        for (int remaining = 4; remaining > 0; remaining--) {
            cursor = cursor.below();
            if (!world.isEmptyBlock(cursor)) {
                break;
            }
            setBlockAndNotifyAdequately(world, cursor, vineState);
        }
    }

    private static BlockState verticalWood(BlockState state) {
        if (state.getBlock() instanceof RotatedPillarBlock) {
            return state.setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        }
        return state;
    }
}
