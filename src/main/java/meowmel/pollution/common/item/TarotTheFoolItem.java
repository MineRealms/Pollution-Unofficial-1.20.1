package meowmel.pollution.common.item;

import meowmel.pollution.common.items.behaviors.Tarots;
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
 * Tarot — The Fool: behaviour port of upstream {@code Tarots.THE_FOOL}.
 *
 * <p>Sneak + right-click teleports the holder to the level spawn point and
 * plays the ender teleport sound. The behaviour itself lives in
 * {@link Tarots}; the remaining tarots are plain items upstream and stay plain
 * in the port.</p>
 */
public class TarotTheFoolItem extends Item {

    public TarotTheFoolItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return Tarots.useTheFool(level, player, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        Tarots.addTheFoolInformation(tooltip);
    }
}
