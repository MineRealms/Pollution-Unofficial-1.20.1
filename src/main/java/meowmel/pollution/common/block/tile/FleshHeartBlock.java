package meowmel.pollution.common.block.tile;

import meowmel.pollution.common.block.PollutionMiscBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

/**
 * Port of the 1.12 {@code BlockFleshHeart}: the flesh tree core block. It owns
 * the {@link FleshHeartBlockEntity}, which heartbeats and can be soul-bound to
 * a player.
 *
 * <p>Deviations from upstream: the chat feedback is translated through the
 * {@code pollution.flesh_heart.*} lang keys instead of hardcoded Chinese; the
 * block is ticked through the modern {@link EntityBlock} ticker instead of the
 * removed {@code ITickable} interface.</p>
 */
public class FleshHeartBlock extends Block implements EntityBlock {

    public FleshHeartBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FleshHeartBlockEntity(pos, state);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return type == PollutionMiscBlocks.FLESH_HEART_BLOCK_ENTITY.get()
                ? (BlockEntityTicker<T>) (lvl, pos, st, be) ->
                        FleshHeartBlockEntity.serverTick(lvl, pos, st, (FleshHeartBlockEntity) be)
                : null;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(pos) instanceof FleshHeartBlockEntity heart)) {
            return InteractionResult.CONSUME;
        }

        if (!heart.isBound()) {
            heart.tryBind(player);
            player.sendSystemMessage(Component.translatable("pollution.flesh_heart.bound")
                    .withStyle(ChatFormatting.RED));
        } else if (heart.isOwner(player)) {
            player.sendSystemMessage(Component.translatable("pollution.flesh_heart.level",
                    heart.getHeartLevel(), FleshHeartBlockEntity.MAX_LEVEL).withStyle(ChatFormatting.GOLD));
            if (heart.getHeartLevel() < FleshHeartBlockEntity.MAX_LEVEL) {
                player.sendSystemMessage(Component.translatable("pollution.flesh_heart.next_growth",
                                String.format("%,d", heart.getRequiredGrowthLP()),
                                String.format("%,d", heart.getCurrentNetworkLP()))
                        .withStyle(ChatFormatting.GRAY));
            }
        } else {
            player.sendSystemMessage(Component.translatable("pollution.flesh_heart.other",
                    heart.getBoundPlayerName()).withStyle(ChatFormatting.DARK_PURPLE));
        }
        return InteractionResult.CONSUME;
    }
}
