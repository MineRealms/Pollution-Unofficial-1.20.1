package meowmel.pollution.common.item;

import dev.tc4port.thaumcraft.api.player.PlayerWarpView;
import meowmel.pollution.compat.tc4r.TC4RBridge;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Vis checker: reports the holder's Thaumcraft warp.
 *
 * <p>Upstream shipped several diagnostic items (vis checker, runes,
 * enhancement parts). The port starts the behaviour layer with the vis checker:
 * right-click prints the warp totals read through the TC4R warp view; the
 * enhancement/rune behaviours arrive with the enhancement system. Upstream's
 * {@code VisCheckerBehavior.addInformation} tooltip is kept and describes the
 * ported warp report.</p>
 */
public class VisCheckerItem extends Item {

    public VisCheckerItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("pollution.item.vis_checker.tooltip"));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            PlayerWarpView view = TC4RBridge.warpOf(serverPlayer);
            int permanent = view == null ? 0 : view.permanent();
            int sticky = view == null ? 0 : view.sticky();
            int temporary = view == null ? 0 : view.temporary();
            serverPlayer.displayClientMessage(
                    Component.translatable("pollution.item.vis_checker.result", permanent, sticky, temporary), true);
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }
}
