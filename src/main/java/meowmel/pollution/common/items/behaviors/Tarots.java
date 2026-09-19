package meowmel.pollution.common.items.behaviors;

import meowmel.pollution.api.recipes.properties.TarotCards;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Item behaviours of the tarot cards (port of upstream
 * {@code meowmel.pollution.common.items.behaviors.Tarots}).
 *
 * <p>Upstream only The Fool carried a behaviour: sneak + right-click teleports
 * the holder to the world spawn and plays the ender teleport sound. The other
 * 21 cards were plain items and stay plain in the port. The uniform
 * {@link #drawRandom(RandomSource)} helper exposes the 22-card draw used by
 * loot / ritual integrations without duplicating the id table.</p>
 */
public final class Tarots {

    private Tarots() {}

    /** Sneak + right-click teleports the holder to the level spawn (upstream THE_FOOL). */
    public static InteractionResultHolder<ItemStack> useTheFool(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player.isShiftKeyDown()) {
            BlockPos spawn = level.getSharedSpawnPos();
            player.teleportTo(spawn.getX() + 0.5D, spawn.getY(), spawn.getZ() + 0.5D);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 2.0F);
        }
        return InteractionResultHolder.pass(stack);
    }

    public static void addTheFoolInformation(List<Component> tooltip) {
        tooltip.add(Component.translatable("pollution.tarot.the_fool.tooltip"));
    }

    /** Uniformly draws one of the 22 major-arcana ids. */
    public static String drawRandom(RandomSource random) {
        return TarotCards.IDS.get(random.nextInt(TarotCards.IDS.size()));
    }
}
