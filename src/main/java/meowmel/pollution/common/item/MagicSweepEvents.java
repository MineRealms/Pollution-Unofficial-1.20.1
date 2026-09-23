package meowmel.pollution.common.item;

import meowmel.pollution.Pollution;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * Magic sweep flight mechanic - port of upstream {@code SweepEventLoader}
 * (itself ported from GregTech-Lite-Core PR #139).
 *
 * <p>Holding the magic sweep, carrying it in the inventory or wearing it in any
 * Curios slot grants creative-style flight and full damage immunity; both are
 * revoked as soon as the item leaves the player. Flight state is tracked in the
 * persisted player NBT (upstream keys kept) so an abnormal exit can never leave
 * permanent flight behind; damage immunity is event-based and never written to
 * capabilities, exactly like upstream.</p>
 */
@Mod.EventBusSubscriber(modid = Pollution.MOD_ID)
public class MagicSweepEvents {

    private static final String ACTIVE_KEY = "PollutionMagicSweepActive";
    private static final String PREVIOUS_FLIGHT_KEY = "PollutionMagicSweepPreviousFlight";

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        if (!(event.player instanceof ServerPlayer player)) {
            return;
        }
        CompoundTag sweepData = persistedData(player);

        // Creative and spectator flight is managed by vanilla; the sweep never touches it.
        if (player.isCreative() || player.isSpectator()) {
            if (sweepData.getBoolean(ACTIVE_KEY)) {
                clearSweepState(sweepData);
            }
            return;
        }

        if (hasMagicSweep(player)) {
            if (!sweepData.getBoolean(ACTIVE_KEY)) {
                sweepData.putBoolean(ACTIVE_KEY, true);
                sweepData.putBoolean(PREVIOUS_FLIGHT_KEY, player.getAbilities().mayfly);
            }
            if (!player.getAbilities().mayfly) {
                player.getAbilities().mayfly = true;
                player.onUpdateAbilities();
            }
        } else if (sweepData.getBoolean(ACTIVE_KEY)) {
            restoreFlight(player, sweepData.getBoolean(PREVIOUS_FLIGHT_KEY));
            clearSweepState(sweepData);
        }
    }

    /** Damage immunity is not written to capabilities, so it can never leak (upstream rule). */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingAttack(LivingAttackEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (!player.level().isClientSide && hasMagicSweep(player)) {
            event.setCanceled(true);
        }
    }

    private static boolean hasMagicSweep(Player player) {
        ItemStack sweep = new ItemStack(PollutionItems.MAGIC_SWEEP.get());
        if (player.getMainHandItem().is(sweep.getItem())) {
            return true;
        }
        if (player.getOffhandItem().is(sweep.getItem())) {
            return true;
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).is(sweep.getItem())) {
                return true;
            }
        }
        // Upstream scanned the Baubles inventory; the port scans every Curios slot.
        return ModList.get().isLoaded("curios")
                && CuriosApi.getCuriosInventory(player)
                        .map(handler -> handler.isEquipped(sweep.getItem()))
                        .orElse(false);
    }

    private static void restoreFlight(ServerPlayer player, boolean previousAllowFlying) {
        boolean changed = player.getAbilities().mayfly != previousAllowFlying;
        player.getAbilities().mayfly = previousAllowFlying;
        if (!previousAllowFlying && player.getAbilities().flying) {
            player.getAbilities().flying = false;
            changed = true;
        }
        if (changed) {
            player.onUpdateAbilities();
        }
    }

    private static CompoundTag persistedData(ServerPlayer player) {
        CompoundTag entityData = player.getPersistentData();
        if (!entityData.contains(Player.PERSISTED_NBT_TAG)) {
            entityData.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        }
        return entityData.getCompound(Player.PERSISTED_NBT_TAG);
    }

    private static void clearSweepState(CompoundTag sweepData) {
        sweepData.remove(ACTIVE_KEY);
        sweepData.remove(PREVIOUS_FLIGHT_KEY);
    }
}
