package meowmel.pollution.common.block.plant.alfheim;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
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
 * Port of the 1.12 {@code BlockAlfheimWhiteGrape}: a three stage grape pad
 * growing on source water that can be harvested by hand and slowly spreads
 * over adjacent lily pads.
 *
 * <p>Deviations: modern {@link BushBlock} has no {@code updateTick}, so the
 * 1.12 {@code canSustainBush}/{@code canBlockStay} pair is replaced by a
 * {@link #canSurvive} water source check; drops are handled explicitly since
 * the block has no loot table.</p>
 */
public class AlfheimWhiteGrapeBlock extends BushBlock implements BonemealableBlock {

    public static final IntegerProperty AGE = BlockStateProperties.AGE_2;
    private static final VoxelShape SHAPE = Shapes.box(0.0, 0.0, 0.0, 1.0, 1.0 / 16.0, 1.0);

    public AlfheimWhiteGrapeBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(AGE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        return below.getFluidState().is(FluidTags.WATER) && below.getFluidState().isSource();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!canSurvive(state, level, pos)) {
            return;
        }
        int age = state.getValue(AGE);
        int chance = age == 0 ? 50 : 10;
        if (age < 2 && random.nextInt(chance) == 0) {
            level.setBlock(pos, state.setValue(AGE, age + 1), 3);
            return;
        }
        if (random.nextInt(100) == 0) {
            Direction[] directions = {Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST};
            for (int i = directions.length - 1; i > 0; i--) {
                int j = random.nextInt(i + 1);
                Direction swap = directions[i];
                directions[i] = directions[j];
                directions[j] = swap;
            }
            for (Direction direction : directions) {
                BlockPos target = pos.relative(direction);
                if (level.getBlockState(target).is(Blocks.LILY_PAD)) {
                    level.setBlock(target, defaultBlockState(), 3);
                }
            }
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hitResult) {
        if (state.getValue(AGE) >= 2 && player.getItemInHand(hand).isEmpty()) {
            if (!level.isClientSide) {
                popResource(level, pos, new ItemStack(this, level.random.nextInt(2) + 1));
                level.setBlock(pos, state.setValue(AGE, 0), 3);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return List.of(new ItemStack(this));
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean isClient) {
        return state.getValue(AGE) < 2;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return random.nextInt(3) == 0;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        int age = state.getValue(AGE);
        level.setBlock(pos, state.setValue(AGE, Math.min(2, age + 1)), 3);
    }
}
