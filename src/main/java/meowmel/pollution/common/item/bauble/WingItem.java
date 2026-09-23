package meowmel.pollution.common.item.bauble;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.component.ElectricStats;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Nano / quantum wings: Curios port of upstream {@code NanosuitWings} and
 * {@code QuantumWings} (GregTech jetpack chest armours).
 *
 * <p>GT's {@code ArmorComponentItem} does not compile on this port's frozen
 * classpath (same JetBrains annotations problem as the goggles), so the wings
 * moved to Curios' {@code back} slot. Kept from upstream:</p>
 * <ul>
 *   <li>electric flight: creative-style flying while the item has charge,
 *       draining {@code energyPerUse} per flying tick;</li>
 *   <li>nano buff "急行侠I": Speed II for 10 ticks per tick at
 *       {@code energyPerUse * 2} cost;</li>
 *   <li>quantum buffs "急行侠II/急死了": Haste + Speed + Jump Boost II for
 *       10 ticks per tick at {@code energyPerUse} cost;</li>
 *   <li>fall damage reduction (nano 2x, quantum 4x, upstream
 *       {@code getFallDamageReduction}) applied by {@link WingFallEvents}.</li>
 * </ul>
 *
 * <p>Deviations: the jetpack thrust/hover physics ({@code performFlying},
 * hover key, sprint modifiers, cloud particles) are replaced by vanilla
 * creative flight — the GT 1.12 {@code Jetpack} flight model has no GTCEu
 * Modern base class available here.</p>
 */
public class WingItem extends ComponentItem implements ICurioItem {

    public static final String TAG_FLIGHT_ACTIVE = "PollutionWingsFlightActive";
    public static final String TAG_PREVIOUS_FLIGHT = "PollutionWingsPreviousFlight";

    private final int energyPerUse;
    private final int tier;
    private final boolean quantum;
    private final float fallDamageReduction;

    public WingItem(Properties properties, long maxCharge, int tier, int energyPerUse,
                    boolean quantum, float fallDamageReduction) {
        super(properties);
        this.energyPerUse = energyPerUse;
        this.tier = tier;
        this.quantum = quantum;
        this.fallDamageReduction = fallDamageReduction;
        attachComponents(ElectricStats.createRechargeableBattery(maxCharge, tier));
    }

    public float getFallDamageReduction() {
        return fallDamageReduction;
    }

    @Override
    public void curioTick(SlotContext context, ItemStack stack) {
        if (!(context.entity() instanceof ServerPlayer player)) {
            return;
        }
        IElectricItem item = GTCapabilityHelper.getElectricItem(stack);
        if (item == null) {
            return;
        }
        if (player.isCreative() || player.isSpectator()) {
            return;
        }

        if (item.getCharge() >= energyPerUse) {
            grantFlight(player);
            if (quantum) {
                // Upstream QuantumWings#onArmorTick: haste + speed + jump, 10t amplifier 1.
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 10, 1, true, false));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 10, 1, true, false));
                player.addEffect(new MobEffectInstance(MobEffects.JUMP, 10, 1, true, false));
                item.discharge(energyPerUse, tier, true, false, false);
            } else if (item.getCharge() >= energyPerUse * 2L) {
                // Upstream NanosuitWings#onArmorTick: speed only, double energy cost.
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 10, 1, true, false));
                item.discharge(energyPerUse * 2L, tier, true, false, false);
            }
            if (player.getAbilities().flying) {
                item.discharge(energyPerUse, tier, true, false, false);
            }
        } else {
            revokeFlight(player);
        }
    }

    @Override
    public void onUnequip(SlotContext context, ItemStack newStack, ItemStack stack) {
        if (context.entity() instanceof ServerPlayer player) {
            revokeFlight(player);
        }
    }

    private static void grantFlight(ServerPlayer player) {
        CompoundTag data = persistedData(player);
        if (!player.getAbilities().mayfly) {
            data.putBoolean(TAG_FLIGHT_ACTIVE, true);
            data.putBoolean(TAG_PREVIOUS_FLIGHT, false);
            player.getAbilities().mayfly = true;
            player.onUpdateAbilities();
        } else if (!data.getBoolean(TAG_FLIGHT_ACTIVE)) {
            // Flight came from elsewhere (creative item, sweep): remember but do not own it.
            data.putBoolean(TAG_FLIGHT_ACTIVE, true);
            data.putBoolean(TAG_PREVIOUS_FLIGHT, true);
        }
    }

    private static void revokeFlight(ServerPlayer player) {
        CompoundTag data = persistedData(player);
        if (!data.getBoolean(TAG_FLIGHT_ACTIVE)) {
            return;
        }
        boolean previous = data.getBoolean(TAG_PREVIOUS_FLIGHT);
        data.remove(TAG_FLIGHT_ACTIVE);
        data.remove(TAG_PREVIOUS_FLIGHT);
        player.getAbilities().mayfly = previous;
        if (!previous && player.getAbilities().flying) {
            player.getAbilities().flying = false;
        }
        player.onUpdateAbilities();
    }

    private static CompoundTag persistedData(ServerPlayer player) {
        CompoundTag entityData = player.getPersistentData();
        if (!entityData.contains(Player.PERSISTED_NBT_TAG)) {
            entityData.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        }
        return entityData.getCompound(Player.PERSISTED_NBT_TAG);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("pollution.armor.wings.flight").withStyle(ChatFormatting.AQUA));
        if (quantum) {
            tooltip.add(Component.translatable("pollution.armor.wings.quantum_buffs").withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltip.add(Component.translatable("pollution.armor.wings.fall", 4).withStyle(ChatFormatting.GREEN));
        } else {
            tooltip.add(Component.translatable("pollution.armor.wings.nano_buffs").withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltip.add(Component.translatable("pollution.armor.wings.fall", 2).withStyle(ChatFormatting.GREEN));
        }
    }
}
