package meowmel.pollution.common.block.tile;

import meowmel.pollution.common.block.PollutionMiscBlocks;
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
 * <p>Deviations from upstream: the 1.12 {@code Container}/{@code GuiHandler}
 * screen is not ported (client batch), so right-click toggles the extractor
 * and sneak-right-click cycles its mode as a stopgap. The upstream block was
 * invisible and drawn by a TESR; the port uses a placeholder cube model until
 * the renderer batch lands.</p>
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

        // TODO(client batch): replace with the ported Container/GUI; this
        // toggle keeps the server logic usable in the meantime.
        if (player.isShiftKeyDown()) {
            extractor.setMode(extractor.getMode() + 1);
            player.displayClientMessage(Component.translatable("pollution.mineral_extractor.mode",
                    extractor.getMode()), true);
        } else {
            extractor.setEnabled(!extractor.isEnabled());
            player.displayClientMessage(Component.translatable(extractor.isEnabled()
                    ? "pollution.mineral_extractor.enabled"
                    : "pollution.mineral_extractor.disabled"), true);
        }
        return InteractionResult.CONSUME;
    }
}
