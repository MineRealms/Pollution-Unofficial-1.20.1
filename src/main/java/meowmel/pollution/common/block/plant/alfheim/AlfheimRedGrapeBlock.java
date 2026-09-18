package meowmel.pollution.common.block.plant.alfheim;

import meowmel.pollution.common.block.PollutionPlantBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

/**
 * Port of the 1.12 {@code BlockAlfheimRedGrape}: one of the three source
 * red-grape vine growth stages.
 *
 * <p>Upstream used the vine shape and let stage 0 spread like a vanilla vine;
 * stage 1 and 2 only advanced on random ticks, and a bare-handed use at stage 2
 * harvested 1-2 grapes and reset the vine to stage 0. That behaviour is kept
 * here on top of the modern {@link VineBlock}. The stage is a field, not a
 * block state property, exactly like upstream (the direction properties carry
 * the vine shape).</p>
 *
 * <p>Deviations: vines drop nothing when broken (upstream {@code BlockVine}
 * behaviour, loot tables are not shipped); shears therefore do not return the
 * vine either.</p>
 */
public class AlfheimRedGrapeBlock extends VineBlock {

    private final int stage;

    public AlfheimRedGrapeBlock(BlockBehaviour.Properties properties, int stage) {
        super(properties);
        this.stage = stage;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (this.stage == 0) {
            super.randomTick(state, level, pos, random);
            state = level.getBlockState(pos);
            if (!state.is(this)) {
                return;
            }
        }
        if (this.stage < 2 && random.nextInt(this.stage == 0 ? 50 : 10) == 0) {
            Block next = switch (this.stage) {
                case 0 -> PollutionPlantBlocks.ALFHEIM_RED_GRAPE_1.get();
                default -> PollutionPlantBlocks.ALFHEIM_RED_GRAPE_2.get();
            };
            level.setBlock(pos, copyDirections(state, next.defaultBlockState()), 3);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hitResult) {
        if (this.stage == 2 && player.getItemInHand(hand).isEmpty()) {
            if (!level.isClientSide) {
                popResource(level, pos, new ItemStack(PollutionPlantBlocks.ALFHEIM_RED_GRAPE_0.get(),
                        level.random.nextInt(2) + 1));
                level.setBlock(pos, copyDirections(state,
                        PollutionPlantBlocks.ALFHEIM_RED_GRAPE_0.get().defaultBlockState()), 3);
                level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS,
                        1.0F, 0.8F + level.random.nextFloat() * 0.4F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return List.of();
    }

    private static BlockState copyDirections(BlockState from, BlockState to) {
        return to.setValue(UP, from.getValue(UP))
                .setValue(NORTH, from.getValue(NORTH))
                .setValue(EAST, from.getValue(EAST))
                .setValue(SOUTH, from.getValue(SOUTH))
                .setValue(WEST, from.getValue(WEST));
    }
}
