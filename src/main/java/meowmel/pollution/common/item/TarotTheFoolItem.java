package meowmel.pollution.common.item;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
 * plays the ender teleport sound. The remaining tarots are plain items
 * upstream and stay plain in the port.</p>
 */
public class TarotTheFoolItem extends Item {

    public TarotTheFoolItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player.isShiftKeyDown()) {
            BlockPos spawn = level.getSharedSpawnPos();
            player.teleportTo(spawn.getX() + 0.5D, spawn.getY(), spawn.getZ() + 0.5D);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 2.0F);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("pollution.tarot.the_fool.tooltip"));
    }
}
