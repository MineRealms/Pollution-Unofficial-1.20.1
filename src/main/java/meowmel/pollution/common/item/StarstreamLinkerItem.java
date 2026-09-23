package meowmel.pollution.common.item;

import meowmel.pollution.common.item.behaviors.StarstreamLinkerBehavior;
import net.minecraft.network.chat.Component;
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
 * Starstream linker item: carries the ported network-independent half of
 * upstream {@code StarstreamLinkerBehavior} (INPUT / NETWORK mode flag, sneak +
 * right-click toggle, mode tooltips).
 *
 * <p>Upstream also bound constellation towers, relay stations, obelisk cores
 * and wireless terminals through a pending-link NBT protocol with dimension and
 * range validation. Those targets are not ported (starstream network deferred),
 * so binding does nothing yet; the mode contract is already the upstream one
 * and save-compatible.</p>
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
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        StarstreamLinkerBehavior.appendTooltip(stack, tooltip);
    }
}
