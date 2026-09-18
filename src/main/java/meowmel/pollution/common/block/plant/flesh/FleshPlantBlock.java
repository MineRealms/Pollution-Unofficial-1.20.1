package meowmel.pollution.common.block.plant.flesh;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import dev.tc4port.thaumcraft.registry.TCBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;

/**
 * Port of the 1.12 {@code BlockFleshPlant}: a fence-like flesh growth that
 * connects to neighbouring flesh blocks.
 *
 * <p>Deviations from upstream:</p>
 * <ul>
 *   <li>{@code getActualState} is replaced with stored connection properties
 *       updated through {@link #getStateForPlacement} / {@link #updateShape},
 *       the modern idiom for fence-like blocks.</li>
 *   <li>The upstream GT dust drop ({@code <gregtech:meta_dust:1616>}) could not
 *       be resolved; the closest GTCEu Modern equivalent,
 *       {@code GTMaterials.Meat} dust, is dropped instead.</li>
 *   <li>{@code BlocksTC.fleshBlock} maps to TC4R {@code thaumcraft:tainted_flesh}.</li>
 * </ul>
 */
public class FleshPlantBlock extends Block {

    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;

    private static final VoxelShape CORE = Shapes.box(0.1875, 0.1875, 0.1875, 0.8125, 0.8125, 0.8125);
    private static final VoxelShape ARM_WEST = Shapes.box(0.0, 0.1875, 0.1875, 0.1875, 0.8125, 0.8125);
    private static final VoxelShape ARM_EAST = Shapes.box(0.8125, 0.1875, 0.1875, 1.0, 0.8125, 0.8125);
    private static final VoxelShape ARM_UP = Shapes.box(0.1875, 0.8125, 0.1875, 0.8125, 1.0, 0.8125);
    private static final VoxelShape ARM_DOWN = Shapes.box(0.1875, 0.0, 0.1875, 0.8125, 0.1875, 0.8125);
    private static final VoxelShape ARM_NORTH = Shapes.box(0.1875, 0.1875, 0.0, 0.8125, 0.8125, 0.1875);
    private static final VoxelShape ARM_SOUTH = Shapes.box(0.1875, 0.1875, 0.8125, 0.8125, 0.8125, 1.0);

    public FleshPlantBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(NORTH, false)
                .setValue(EAST, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(UP, false)
                .setValue(DOWN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return computeConnections(defaultBlockState(), context.getLevel(), context.getClickedPos());
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return computeConnections(state, level, pos);
    }

    private static BlockState computeConnections(BlockState state, BlockGetter level, BlockPos pos) {
        return state
                .setValue(DOWN, connectsTo(level.getBlockState(pos.below()), true))
                .setValue(UP, connectsTo(level.getBlockState(pos.above()), false))
                .setValue(NORTH, connectsTo(level.getBlockState(pos.north()), false))
                .setValue(EAST, connectsTo(level.getBlockState(pos.east()), false))
                .setValue(SOUTH, connectsTo(level.getBlockState(pos.south()), false))
                .setValue(WEST, connectsTo(level.getBlockState(pos.west()), false));
    }

    private static boolean connectsTo(BlockState state, boolean includeTaintedFlesh) {
        Block block = state.getBlock();
        return block instanceof FleshPlantBlock || block instanceof FleshFlowerBlock
                || (includeTaintedFlesh && block == TCBlocks.TAINTED_FLESH.get());
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        boolean airAbove = level.isEmptyBlock(pos.above());
        boolean airBelow = level.isEmptyBlock(pos.below());
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos neighborPos = pos.relative(direction);
            Block neighbor = level.getBlockState(neighborPos).getBlock();
            if (neighbor instanceof FleshPlantBlock) {
                if (!airAbove && !airBelow) {
                    return false;
                }
                Block belowNeighbor = level.getBlockState(neighborPos.below()).getBlock();
                if (belowNeighbor instanceof FleshPlantBlock || belowNeighbor == TCBlocks.TAINTED_FLESH.get()) {
                    return true;
                }
            }
        }
        Block below = level.getBlockState(pos.below()).getBlock();
        return below instanceof FleshPlantBlock || below == TCBlocks.TAINTED_FLESH.get();
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
                                BlockPos fromPos, boolean isMoving) {
        if (!canSurvive(state, level, pos)) {
            level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!canSurvive(state, level, pos)) {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!canSurvive(state, level, pos)) {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        double minX = state.getValue(WEST) ? 0.0 : 0.1875;
        double minY = state.getValue(DOWN) ? 0.0 : 0.1875;
        double minZ = state.getValue(NORTH) ? 0.0 : 0.1875;
        double maxX = state.getValue(EAST) ? 1.0 : 0.8125;
        double maxY = state.getValue(UP) ? 1.0 : 0.8125;
        double maxZ = state.getValue(SOUTH) ? 1.0 : 0.8125;
        return Shapes.box(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
                                        CollisionContext context) {
        VoxelShape shape = CORE;
        if (state.getValue(WEST)) {
            shape = Shapes.join(shape, ARM_WEST, BooleanOp.OR);
        }
        if (state.getValue(EAST)) {
            shape = Shapes.join(shape, ARM_EAST, BooleanOp.OR);
        }
        if (state.getValue(UP)) {
            shape = Shapes.join(shape, ARM_UP, BooleanOp.OR);
        }
        if (state.getValue(DOWN)) {
            shape = Shapes.join(shape, ARM_DOWN, BooleanOp.OR);
        }
        if (state.getValue(NORTH)) {
            shape = Shapes.join(shape, ARM_NORTH, BooleanOp.OR);
        }
        if (state.getValue(SOUTH)) {
            shape = Shapes.join(shape, ARM_SOUTH, BooleanOp.OR);
        }
        return shape;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = new ArrayList<>();
        RandomSource random = params.getLevel().getRandom();
        int rottenFlesh = random.nextInt(3);
        if (rottenFlesh > 0) {
            drops.add(new ItemStack(Items.ROTTEN_FLESH, rottenFlesh));
        }
        int meatDust = random.nextInt(3);
        if (meatDust > 0) {
            ItemStack dust = ChemicalHelper.get(TagPrefix.dust, GTMaterials.Meat, meatDust);
            if (!dust.isEmpty()) {
                drops.add(dust);
            }
        }
        return drops;
    }
}
