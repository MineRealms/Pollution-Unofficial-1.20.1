package meowmel.pollution.common.block.plant.flesh;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

/**
 * Port of the 1.12 {@code BlockEldritchEye}: an eye that blinks, tracks nearby
 * players and inflicts nausea on anyone within 3 blocks.
 *
 * <p>Deviations: scheduled reopen uses {@link #tick} instead of the removed
 * {@code updateTick}; collision follows the visual box instead of the 1.12
 * full-cube collision.</p>
 */
public class EldritchEyeBlock extends Block {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty OPEN = BooleanProperty.create("open");

    private static final VoxelShape SHAPE = Shapes.box(0.2, 0.2, 0.2, 0.8, 0.8, 0.8);

    public EldritchEyeBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(FACING, Direction.SOUTH)
                .setValue(OPEN, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
                .setValue(FACING, context.getClickedFace())
                .setValue(OPEN, true);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(5) == 0) {
            boolean open = state.getValue(OPEN);
            state = state.setValue(OPEN, !open);
            level.setBlock(pos, state, 2);
            if (open) {
                level.scheduleTick(pos, this, 10 + random.nextInt(10));
            }
        }

        Player nearest = level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 16.0, false);
        if (nearest != null) {
            Direction look = Direction.getNearest(
                    nearest.getX() - (pos.getX() + 0.5),
                    nearest.getY() - (pos.getY() + 0.5),
                    nearest.getZ() - (pos.getZ() + 0.5));
            if (look != state.getValue(FACING)) {
                state = state.setValue(FACING, look);
                level.setBlock(pos, state, 2);
            }
        }

        List<Player> nearby = level.getEntitiesOfClass(Player.class, new AABB(pos).inflate(3.0));
        for (Player player : nearby) {
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 60, 0, true, false));
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.getValue(OPEN)) {
            level.setBlock(pos, state.setValue(OPEN, true), 2);
        }
    }
}
