package meowmel.pollution.common.warp;

import dev.tc4port.thaumcraft.api.player.PlayerWarpView;
import meowmel.pollution.PollutionConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
    private static final Map<UUID, Integer> COUNTDOWN_BOMBS = new HashMap<>();
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
        add("fake_explosion", 3, WarpNetwork::fakeExplosion);
        add("fake_rain", 3, player -> WarpNetwork.rain(player, 1, 100 + player.getRandom().nextInt(60)));
        add("rain", 3, player -> WarpNetwork.rain(player, 2, 120 + player.getRandom().nextInt(80)));
        add("junk", 4, player -> player.getInventory().placeItemBackInInventory(
                new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.ROTTEN_FLESH,
                        player.getRandom().nextInt(3) + 1)));
        add("blink", 3, player -> {
            var level = player.serverLevel();
            double oldX = player.getX(), oldY = player.getY(), oldZ = player.getZ();
            for (int attempt = 0; attempt < 16; attempt++) {
                double x = oldX + (player.getRandom().nextDouble() - 0.5) * 24;
                double z = oldZ + (player.getRandom().nextDouble() - 0.5) * 24;
                var pos = net.minecraft.core.BlockPos.containing(x, oldY, z);
                if (!level.hasChunkAt(pos) || !level.getWorldBorder().isWithinBounds(pos)) continue;
                if (player.randomTeleport(x, oldY, z, true)) {
                    level.sendParticles(net.minecraft.core.particles.ParticleTypes.PORTAL,
                            oldX, oldY + 1, oldZ, 32, 0.4, 0.7, 0.4, 0.1);
                    level.sendParticles(net.minecraft.core.particles.ParticleTypes.PORTAL,
                            player.getX(), player.getY() + 1, player.getZ(), 32, 0.4, 0.7, 0.4, 0.1);
                    player.connection.teleport(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
                    break;
                }
            }
        });
        add("swamp", 3, player -> {
            var level = player.serverLevel();
            var pos = player.blockPosition().below();
            if (level.getBlockState(pos).isSolidRender(level, pos)) {
                level.setBlockAndUpdate(pos, net.minecraft.world.level.block.Blocks.SLIME_BLOCK.defaultBlockState());
            }
        });
        add("countdown_bomb", 1, player -> {
            if (!PollutionConfig.ENABLE_COUNTDOWN_BOMB.get()) return;
            COUNTDOWN_BOMBS.put(player.getUUID(), PollutionConfig.COUNTDOWN_BOMB_TICKS.get());
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                    "chat.pollution.warp.countdownbomb.tick", PollutionConfig.COUNTDOWN_BOMB_TICKS.get() / 20), true);
        });
        add("wither_rose", 3, player -> {
            var level = player.serverLevel();
            var pos = player.blockPosition();
            if (level.isEmptyBlock(pos)) {
                level.setBlockAndUpdate(pos, net.minecraft.world.level.block.Blocks.WITHER_ROSE.defaultBlockState());
            }
        });
        add("fall", 5, player -> player.push(0, -1.5, 0));
        add("inventory_scramble", 2, player -> {
            var inventory = player.getInventory();
            int size = Math.min(9, inventory.getContainerSize());
            int first = player.getRandom().nextInt(size);
            int second = player.getRandom().nextInt(size);
            var a = inventory.getItem(first).copy();
            var b = inventory.getItem(second).copy();
            inventory.setItem(first, b);
            inventory.setItem(second, a);
        });
        add("zombie_siege", 2, player -> {
            var level = player.serverLevel();
            for (int i = 0; i < 2; i++) {
                var zombie = net.minecraft.world.entity.EntityType.ZOMBIE.create(level);
                if (zombie != null) {
                    zombie.moveTo(player.getX() + player.getRandom().nextInt(5) - 2, player.getY(),
                            player.getZ() + player.getRandom().nextInt(5) - 2);
                    level.addFreshEntity(zombie);
                }
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
        if (!PollutionConfig.ENABLE_WARP_EVENTS.get() || !player.isAlive() || player.isSpectator()) return false;
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
        int totalWeight = EVENTS.stream().filter(entry -> enabled(entry.id())).mapToInt(Entry::weight).sum();
        if (totalWeight == 0) return false;
        int roll = player.getRandom().nextInt(totalWeight);
        for (Entry entry : EVENTS) {
            if (!enabled(entry.id())) continue;
            roll -= entry.weight();
            if (roll < 0) {
                entry.event().trigger(player);
                return true;
            }
        }
        return false;
    }

    /** Advances delayed warp events. Called every server tick by {@link WarpEventHandler}. */
    public static void tickCountdowns(MinecraftServer server) {
        if (!PollutionConfig.ENABLE_WARP_EVENTS.get() || !PollutionConfig.ENABLE_COUNTDOWN_BOMB.get()) {
            COUNTDOWN_BOMBS.clear();
            return;
        }
        if (COUNTDOWN_BOMBS.isEmpty()) return;
        var iterator = COUNTDOWN_BOMBS.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            if (player == null || !player.isAlive() || player.isSpectator()) {
                iterator.remove();
                continue;
            }
            int left = entry.getValue() - 1;
            if (left > 0) {
                entry.setValue(left);
                if (left % 20 == 0) {
                    player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                            "chat.pollution.warp.countdownbomb.tick", left / 20), true);
                }
                continue;
            }
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                    "chat.pollution.warp.countdownbomb.end"), true);
            WarpNetwork.fakeExplosion(player);
            iterator.remove();
        }
    }

    public static boolean enabled(String id) {
        var toggle = PollutionConfig.WARP_EVENTS.get(id);
        return (toggle == null || toggle.get())
                && (!"countdown_bomb".equals(id) || PollutionConfig.ENABLE_COUNTDOWN_BOMB.get());
    }

    /** Explicit diagnostic trigger; normal scheduling still checks the player's warp. */
    public static boolean trigger(String id, ServerPlayer player) {
        if (!PollutionConfig.ENABLE_WARP_EVENTS.get() || !enabled(id) || !player.isAlive()) return false;
        for (Entry entry : EVENTS) {
            if (entry.id().equals(id)) {
                entry.event().trigger(player);
                return true;
            }
        }
        return false;
    }

    public static void clearPendingEvents() {
        COUNTDOWN_BOMBS.clear();
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
