package meowmel.pollution.common.warp;

import dev.tc4port.thaumcraft.api.player.PlayerWarpView;

/**
 * Warp-driven events of the Pollution port.
 *
 * <p>Upstream had 30+ single-purpose event classes plus a queue/scheduler;
 * the port keeps the same model in a compact registry: every entry has an id,
 * a weight and a server-side effect. The scheduler reads the player's warp
 * through the TC4R read-only view and rolls for effects.</p>
 */
public final class PollutionWarpEvents {

    /** One warp event. */
    public interface WarpEvent {

        void trigger(net.minecraft.server.level.ServerPlayer player);
    }

    private record Entry(String id, int weight, WarpEvent event) {}

    private static final java.util.List<Entry> EVENTS = new java.util.ArrayList<>();
    private static boolean initialized;

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;

        add("blind", 10, player -> apply(player, net.minecraft.world.effect.MobEffects.BLINDNESS, 200, 0));
        add("nausea", 10, player -> apply(player, net.minecraft.world.effect.MobEffects.CONFUSION, 300, 0));
        add("poison", 8, player -> apply(player, net.minecraft.world.effect.MobEffects.POISON, 120, 0));
        add("wither", 5, player -> apply(player, net.minecraft.world.effect.MobEffects.WITHER, 80, 0));
        add("weakness", 8, player -> apply(player, net.minecraft.world.effect.MobEffects.WEAKNESS, 200, 0));
        add("jump", 6, player -> player.teleportTo(player.getX(), player.getY() + 2, player.getZ()));
        add("wind", 8, player -> {
            double angle = player.getRandom().nextDouble() * Math.PI * 2;
            player.push(Math.cos(angle) * 1.5, 0.6, Math.sin(angle) * 1.5);
            player.hurtMarked = true;
        });
        add("blood", 5, player -> player.hurt(player.damageSources().magic(), 2.0F));
        add("lightning", 3, player -> {
            var level = player.serverLevel();
            var pos = player.blockPosition();
            var bolt = net.minecraft.world.entity.EntityType.LIGHTNING_BOLT.create(level);
            if (bolt != null) {
                bolt.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                level.addFreshEntity(bolt);
            }
        });
        add("obsidian", 4, player -> {
            var level = player.serverLevel();
            var pos = player.blockPosition();
            if (level.isEmptyBlock(pos)) {
                level.setBlockAndUpdate(pos, net.minecraft.world.level.block.Blocks.OBSIDIAN.defaultBlockState());
            }
        });
        add("mushrooms", 4, player -> {
            var level = player.serverLevel();
            var pos = player.blockPosition();
            if (level.isEmptyBlock(pos)) {
                level.setBlockAndUpdate(pos, net.minecraft.world.level.block.Blocks.RED_MUSHROOM.defaultBlockState());
            }
        });
    }

    private static void apply(net.minecraft.server.level.ServerPlayer player,
                              net.minecraft.world.effect.MobEffect effect, int duration, int amplifier) {
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(effect, duration, amplifier));
    }

    public static void add(String id, int weight, WarpEvent event) {
        EVENTS.add(new Entry(id, Math.max(1, weight), event));
    }

    public static java.util.List<String> ids() {
        return EVENTS.stream().map(Entry::id).toList();
    }

    /**
     * Rolls one warp event for the player based on their current warp.
     *
     * @return true when an event fired
     */
    public static boolean tick(net.minecraft.server.level.ServerPlayer player) {
        if (EVENTS.isEmpty()) {
            return false;
        }
        int warp = totalWarp(player);
        if (warp <= 0) {
            return false;
        }
        double chance = Math.min(0.25, warp / 100.0);
        if (player.getRandom().nextDouble() >= chance) {
            return false;
        }
        int totalWeight = EVENTS.stream().mapToInt(Entry::weight).sum();
        int roll = player.getRandom().nextInt(totalWeight);
        for (Entry entry : EVENTS) {
            roll -= entry.weight();
            if (roll < 0) {
                entry.event().trigger(player);
                return true;
            }
        }
        return false;
    }

    private static int totalWarp(net.minecraft.server.level.ServerPlayer player) {
        PlayerWarpView view = meowmel.pollution.compat.tc4r.TC4RBridge.warpOf(player);
        if (view == null) {
            return 0;
        }
        return Math.max(0, view.permanent()) + Math.max(0, view.sticky()) + Math.max(0, view.temporary());
    }

    private PollutionWarpEvents() {}
}
