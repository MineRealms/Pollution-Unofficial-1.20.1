package meowmel.pollution.common.starstream;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import javax.annotation.Nullable;

public class StarstreamBlock extends BaseEntityBlock {
    private final StarstreamBlockEntity.Kind kind;
    public StarstreamBlock(Properties properties, StarstreamBlockEntity.Kind kind) { super(properties); this.kind = kind; }
    public StarstreamBlockEntity.Kind getKind() { return kind; }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new StarstreamBlockEntity(pos, state); }
    @Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) { return null; }
    @Override public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                           InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof StarstreamBlockEntity node) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "Starstream " + node.getKind().name().toLowerCase() + ": " + node.getTotalStored()));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override public void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock()) && !level.isClientSide
                && level.getBlockEntity(pos) instanceof StarstreamBlockEntity node) node.onBroken();
        super.onRemove(state, level, pos, replacement, moving);
    }
}
