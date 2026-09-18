package meowmel.pollution.common.block.plant.flesh;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

/**
 * Port of the 1.12 {@code BlockHeartFruit}: a fruit hanging below flesh leaves
 * with 4 growth stages. At stage 3 it can be harvested with an empty hand and
 * emits a heartbeat sound.
 *
 * <p>Deviation from upstream: the harvest drops the block item because the
 * dedicated {@code heart_fruit_i} food item is not ported yet.</p>
 */
public class HeartFruitBlock extends Block {

    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;

    private static final VoxelShape[] SHAPES = {
            Shapes.box(0.375, 0.625, 0.375, 0.625, 1.0, 0.625),
            Shapes.box(0.3125, 0.5, 0.3125, 0.6875, 1.0, 0.6875),
            Shapes.box(0.25, 0.375, 0.25, 0.75, 1.0, 0.75),
            Shapes.box(0.1875, 0.25, 0.1875, 0.8125, 1.0, 0.8125)
    };

    public HeartFruitBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(AGE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.above()).getBlock() instanceof FleshLeavesBlock;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
                                BlockPos fromPos, boolean isMoving) {
        if (!canSurvive(state, level, pos)) {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(AGE)];
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!(level.getBlockState(pos.above()).getBlock() instanceof FleshLeavesBlock)) {
            level.destroyBlock(pos, true);
            return;
        }
        int age = state.getValue(AGE);
        if (age < 3 && random.nextInt(5) == 0) {
            int newAge = age + 1;
            level.setBlock(pos, state.setValue(AGE, newAge), 3);
            if (newAge == 3) {
                level.scheduleTick(pos, this, 20);
            }
        } else if (age == 3) {
            level.scheduleTick(pos, this, 40 + random.nextInt(41));
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(AGE) == 3) {
            level.playSound(null, pos, SoundEvents.NOTE_BLOCK_BASEDRUM.value(),
                    SoundSource.BLOCKS, 0.12F, 0.6F);
            level.scheduleTick(pos, this, 40 + random.nextInt(41));
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hitResult) {
        if (state.getValue(AGE) == 3) {
            if (!level.isClientSide) {
                popResource(level, pos, new ItemStack(this));
                level.removeBlock(pos, false);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        if (state.getValue(AGE) == 3) {
            return List.of(new ItemStack(this));
        }
        return List.of();
    }
}
