package meowmel.pollution.common.item.bauble;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

/** Effect ownership prevents unequipping an accessory from removing a potion's effect. */
final class AccessoryEffects {
    private AccessoryEffects() {}

    private static String key(String source, MobEffect effect) {
        return "pollution.accessory." + source + "." + effect.getDescriptionId();
    }

    private static boolean owns(Player player, String key, MobEffectInstance effect) {
        return effect != null && effect.getAmplifier() == 0 && effect.isAmbient() && !effect.isVisible()
                && player.getPersistentData().contains(key)
                && Math.abs(player.getPersistentData().getLong(key)
                        - player.level().getGameTime() - effect.getDuration()) <= 1L;
    }

    static void refresh(Player player, String source, MobEffect effect, int duration) {
        if (player.level().isClientSide) return;
        String key = key(source, effect);
        MobEffectInstance current = player.getEffect(effect);
        if (current != null && !owns(player, key, current)) {
            player.getPersistentData().remove(key);
            return;
        }
        // Refresh above the vanilla night-vision flashing window (200 ticks).
        if (current != null && current.getDuration() > 240) return;
        player.addEffect(new MobEffectInstance(effect, duration, 0, true, false));
        player.getPersistentData().putLong(key, player.level().getGameTime() + duration);
    }

    static void remove(Player player, String source, MobEffect effect) {
        if (player.level().isClientSide) return;
        String key = key(source, effect);
        if (owns(player, key, player.getEffect(effect))) player.removeEffect(effect);
        player.getPersistentData().remove(key);
    }
}
