package meowmel.pollution.common.block.plant.flesh;

import meowmel.pollution.common.block.PollutionMiscBlocks;
import meowmel.pollution.common.block.PollutionPlantBlocks;
import meowmel.pollution.common.block.tile.FleshHeartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Port of the 1.12 {@code BlockFleshSapling}: stage 0-1 sapling that grows into
 * the initial level-1 flesh tree (flesh trunk, heart core block entity, leaf
 * canopy), exactly like upstream {@code BlockFleshSapling#growTree}. The
 * higher-level cthulhu-style shapes (tentacles, eyes, ventricles) are driven by
 * the heart core's own growth once it collects LP.
 */
public class FleshSaplingBlock extends Block implements BonemealableBlock {

    public static final IntegerProperty STAGE = BlockStateProperties.STAGE;
    private static final VoxelShape SHAPE = Shapes.box(0.1, 0.0, 0.1, 0.9, 0.8, 0.9);

    public FleshSaplingBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(STAGE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STAGE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(STAGE) == 0) {
            level.setBlock(pos, state.setValue(STAGE, 1), 3);
        } else {
            growTree(level, pos, random);
        }
    }

    /**
     * Grows the initial level-1 flesh tree (upstream {@code BlockFleshSapling#growTree}):
     * a 3-block flesh trunk with the heart core at height 2, a 3x3 leaf ring around
     * the trunk top and a leaf cross above it.
     */
    private void growTree(ServerLevel level, BlockPos saplingPos, RandomSource random) {
        for (int y = 1; y <= 4; y++) {
            if (!level.isEmptyBlock(saplingPos.above(y))) {
                return;
            }
        }
        level.removeBlock(saplingPos, false);

        BlockState flesh = PollutionMiscBlocks.FLESH_BLOCK.get().defaultBlockState();
        BlockState leaves = PollutionPlantBlocks.FLESH_LEAVES.get().defaultBlockState();

        level.setBlock(saplingPos, flesh, 3);
        level.setBlock(saplingPos.above(1), flesh, 3);
        BlockPos heartPos = saplingPos.above(2);
        level.setBlock(heartPos, PollutionMiscBlocks.FLESH_HEART.get().defaultBlockState(), 3);
        BlockPos topTrunk = saplingPos.above(3);
        level.setBlock(topTrunk, flesh, 3);

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx != 0 || dz != 0) {
                    level.setBlock(topTrunk.offset(dx, 0, dz), leaves, 3);
                }
            }
        }
        BlockPos top = saplingPos.above(4);
        level.setBlock(top, leaves, 3);
        level.setBlock(top.north(), leaves, 3);
        level.setBlock(top.south(), leaves, 3);
        level.setBlock(top.east(), leaves, 3);
        level.setBlock(top.west(), leaves, 3);

        if (level.getBlockEntity(heartPos) instanceof FleshHeartBlockEntity heart) {
            heart.setHeartLevel(1);
            heart.setOrigin(saplingPos);
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean isClient) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        if (state.getValue(STAGE) == 0) {
            level.setBlock(pos, state.setValue(STAGE, 1), 3);
        } else {
            growTree(level, pos, random);
        }
    }
}
