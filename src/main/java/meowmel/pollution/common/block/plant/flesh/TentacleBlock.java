package meowmel.pollution.common.block.plant.flesh;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Port of the 1.12 {@code BlockTentacle}: a fence-like tentacle with a
 * thickness property and automatic connections, slowing and damaging entities
 * that pass through it.
 *
 * <p>Deviations from upstream: the connection check only knows the ported
 * {@link FleshLeavesBlock} and other tentacles; the upstream flesh block and
 * flesh heart core are not ported yet. Connections are stored properties
 * updated on placement/neighbour change instead of {@code getActualState}.</p>
 */
public class TentacleBlock extends Block {

    public static final IntegerProperty THICKNESS = IntegerProperty.create("thickness", 0, 2);
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;

    private static final double[] CENTER_MIN = {0.375, 0.3125, 0.25};
    private static final double[] CENTER_MAX = {0.625, 0.6875, 0.75};

    public TentacleBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(THICKNESS, 1)
                .setValue(NORTH, false)
                .setValue(SOUTH, false)
                .setValue(EAST, false)
                .setValue(WEST, false)
                .setValue(UP, false)
                .setValue(DOWN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(THICKNESS, NORTH, SOUTH, EAST, WEST, UP, DOWN);
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
                .setValue(NORTH, canConnectTo(level.getBlockState(pos.north())))
                .setValue(SOUTH, canConnectTo(level.getBlockState(pos.south())))
                .setValue(EAST, canConnectTo(level.getBlockState(pos.east())))
                .setValue(WEST, canConnectTo(level.getBlockState(pos.west())))
                .setValue(UP, canConnectTo(level.getBlockState(pos.above())))
                .setValue(DOWN, canConnectTo(level.getBlockState(pos.below())));
    }

    private static boolean canConnectTo(BlockState state) {
        Block block = state.getBlock();
        return block instanceof TentacleBlock || block instanceof FleshLeavesBlock;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int thickness = state.getValue(THICKNESS);
        double min = CENTER_MIN[thickness];
        double max = CENTER_MAX[thickness];
        double minX = state.getValue(WEST) ? 0.0 : min;
        double minY = state.getValue(DOWN) ? 0.0 : min;
        double minZ = state.getValue(NORTH) ? 0.0 : min;
        double maxX = state.getValue(EAST) ? 1.0 : max;
        double maxY = state.getValue(UP) ? 1.0 : max;
        double maxZ = state.getValue(SOUTH) ? 1.0 : max;
        return Shapes.box(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!(entity instanceof LivingEntity living)) {
            return;
        }
        Vec3 motion = entity.getDeltaMovement();
        entity.setDeltaMovement(motion.x * 0.6, motion.y, motion.z * 0.6);
        living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1, true, false));
        if (!level.isClientSide && level.random.nextInt(20) == 0) {
            living.hurt(level.damageSources().cactus(), 1.0F);
        }
    }
}
