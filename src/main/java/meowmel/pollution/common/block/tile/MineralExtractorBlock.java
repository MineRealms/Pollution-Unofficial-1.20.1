package meowmel.pollution.common.block.tile;

import meowmel.pollution.common.block.PollutionMiscBlocks;
import meowmel.pollution.common.menu.MineralExtractorMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraftforge.network.NetworkHooks;
import net.minecraft.world.Containers;
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
 * Port of the 1.12 {@code BlockMineralExtractor}: hosts the
 * {@link MineralExtractorBlockEntity} that scans and mines ore.
 *
 * <p>Right-click opens the synchronized menu; sneak-right-click cycles modes.</p>
 */
public class MineralExtractorBlock extends Block implements EntityBlock {

    public MineralExtractorBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MineralExtractorBlockEntity(pos, state);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return type == PollutionMiscBlocks.MINERAL_EXTRACTOR_BLOCK_ENTITY.get()
                ? (BlockEntityTicker<T>) (lvl, pos, st, be) ->
                        MineralExtractorBlockEntity.serverTick(lvl, pos, st, (MineralExtractorBlockEntity) be)
                : null;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(pos) instanceof MineralExtractorBlockEntity extractor)) {
            return InteractionResult.CONSUME;
        }

        if (player.isShiftKeyDown()) {
            extractor.setMode(extractor.getMode() + 1);
            player.displayClientMessage(Component.translatable("pollution.mineral_extractor.mode",
                    extractor.getMode()), true);
        } else if (player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider(
                    (id, inventory, menuPlayer) -> new MineralExtractorMenu(id, inventory, extractor),
                    getName()), pos);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof MineralExtractorBlockEntity extractor) {
            var inventory = extractor.getOutputInventory();
            for (int slot = 0; slot < inventory.getSlots(); slot++) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), inventory.getStackInSlot(slot));
            }
        }
        super.onRemove(state, level, pos, newState, moving);
    }
}
