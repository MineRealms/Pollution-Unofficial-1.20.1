package meowmel.pollution.common.item;

import meowmel.pollution.common.item.behaviors.StarstreamLinkerBehavior;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import meowmel.pollution.common.starstream.StarstreamBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Starstream linker item: carries the ported network-independent half of
 * upstream {@code StarstreamLinkerBehavior} (INPUT / NETWORK mode flag, sneak +
 * right-click toggle, mode tooltips).
 *
 * <p>The ported linker binds a loaded nexus core to an operation core through a
 * two-step NBT selection. Relay and gateway links use the same server-side
 * identity/range checks in the block entity; the Astral constellation tower
 * producer remains outside this port's dependency scope.</p>
 */
public class StarstreamLinkerItem extends Item {

    public StarstreamLinkerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(stack);
        }
        if (!level.isClientSide) {
            String mode = StarstreamLinkerBehavior.toggleMode(stack);
            player.displayClientMessage(
                    Component.translatable(StarstreamLinkerBehavior.modeMessageKey(mode)), true);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide || !(level.getBlockEntity(context.getClickedPos()) instanceof StarstreamBlockEntity node)) {
            return InteractionResult.PASS;
        }
        ItemStack stack = context.getItemInHand();
        if (StarstreamLinkerBehavior.isNetworkMode(stack)) {
            if (node.getKind() == StarstreamBlockEntity.Kind.CORE) {
                stack.getOrCreateTag().putUUID("StarstreamSelectedNetwork", node.getNodeId());
                context.getPlayer().displayClientMessage(Component.translatable(
                        "pollution.starstream_linker.selected"), true);
                return InteractionResult.CONSUME;
            }
            if (node.getKind() == StarstreamBlockEntity.Kind.OPERATION
                    && stack.getTag() != null && stack.getTag().hasUUID("StarstreamSelectedNetwork")) {
                node.bindNetwork(stack.getTag().getUUID("StarstreamSelectedNetwork"));
                stack.getTag().remove("StarstreamSelectedNetwork");
                context.getPlayer().displayClientMessage(Component.translatable(
                        "pollution.starstream_linker.bound"), true);
                return InteractionResult.CONSUME;
            }
        } else {
            // INPUT mode follows the upstream pending-link protocol: select a relay
            // first, then click its loaded target endpoint.
            if (node.getKind() == StarstreamBlockEntity.Kind.RELAY
                    || node.getKind() == StarstreamBlockEntity.Kind.GATEWAY) {
                stack.getOrCreateTag().putLong("StarstreamSelectedSource", node.getBlockPos().asLong());
                context.getPlayer().displayClientMessage(Component.translatable(
                        "pollution.starstream_linker.source_selected"), true);
                return InteractionResult.CONSUME;
            }
            if ((node.getKind() == StarstreamBlockEntity.Kind.CORE
                    || node.getKind() == StarstreamBlockEntity.Kind.RELAY)
                    && stack.getTag() != null && stack.getTag().contains("StarstreamSelectedSource")) {
                BlockPos sourcePos = BlockPos.of(stack.getTag().getLong("StarstreamSelectedSource"));
                if (level.getBlockEntity(sourcePos) instanceof StarstreamBlockEntity source
                        && source.bindOutput(node)) {
                    stack.getTag().remove("StarstreamSelectedSource");
                    context.getPlayer().displayClientMessage(Component.translatable(
                            "pollution.starstream_linker.linked"), true);
                    return InteractionResult.CONSUME;
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        StarstreamLinkerBehavior.appendTooltip(stack, tooltip);
    }
}
