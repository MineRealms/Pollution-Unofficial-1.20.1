package meowmel.pollution.dimension.worldgen.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Modern placement adapters shared by the unchanged WorldEngine shape algorithms. */
abstract class WorldEngineTreeBase {
    public abstract boolean generate(WorldGenLevel world, RandomSource random, BlockPos origin);

    protected static void setBlockAndNotifyAdequately(WorldGenLevel world, BlockPos pos, BlockState state) {
        if (!isReplaceable(world, pos)) return;
        if (state.hasProperty(LeavesBlock.DISTANCE)) state = state.setValue(LeavesBlock.DISTANCE, 1);
        world.setBlock(pos, state, 2);
    }

    protected static boolean isReplaceable(WorldGenLevel world, BlockPos pos) {
        if (world.isOutsideBuildHeight(pos) || !world.ensureCanWrite(pos)) return false;
        BlockState state = world.getBlockState(pos);
        return !state.hasBlockEntity() && (state.isAir() || state.canBeReplaced()
                || state.is(BlockTags.LEAVES) || state.is(BlockTags.LOGS) || state.is(BlockTags.DIRT));
    }

    protected static BlockState vineState(Block vine, int metadata) {
        Direction face = switch (metadata) {
            case 8 -> Direction.EAST;
            case 2 -> Direction.WEST;
            case 1 -> Direction.SOUTH;
            default -> Direction.NORTH;
        };
        return vine.defaultBlockState().setValue(VineBlock.getPropertyForFace(face), true);
    }
}
