package meowmel.pollution.common.item.bauble;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.component.ElectricStats;
import com.gregtechceu.gtceu.utils.input.SyncedKeyMappings;
import dev.tc4port.thaumcraft.api.aspect.VisChannel;
import dev.tc4port.thaumcraft.api.item.AuraRevealingGear;
import dev.tc4port.thaumcraft.api.item.GogglesOverlayGear;
import dev.tc4port.thaumcraft.api.item.VisDiscountGear;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Nano / quantum goggles: Curios port of upstream {@code GogglesNano} and
 * {@code GogglesQuantum}.
 *
 * <p>Upstream wore the goggles in the helmet slot through GregTech's
 * {@code ArmorComponentItem}. That GT class is annotated with
 * {@code @NotNullByDefault} from a JetBrains annotations version that is not on
 * this port's compile classpath (and build files are frozen for this pass), so
 * the port wears them in Curios' {@code head} slot instead — which the
 * Thaumcraft 4R port's {@code EquippedItems.armorAndAccessories} already
 * scans. Behaviours kept:</p>
 * <ul>
 *   <li>consumes power to feed the wearer ({@code needsFood()} / {@code eat});</li>
 *   <li>night vision toggle via GTCEu's armour mode switch key, granting water
 *       breathing while active and discharging per tick;</li>
 *   <li>quantum variant: solar recharge in daylight while night vision is off;</li>
 *   <li>{@link AuraRevealingGear}, {@link GogglesOverlayGear} and a flat
 *       {@link VisDiscountGear} discount of 5, as upstream.</li>
 * </ul>
 *
 * <p>TODO(port): restore the helmet-slot armour form once GT's armour component
 * compiles on this classpath.</p>
 */
public class GogglesItem extends ComponentItem
        implements ICurioItem, AuraRevealingGear, GogglesOverlayGear, VisDiscountGear {

    public static final String TAG_NIGHT_VISION = "Nightvision";
    public static final String TAG_TOGGLE_TIMER = "toggleTimer";
    public static final String TAG_NIGHT_VISION_TIMER = "nightVisionTimer";
    public static final int VIS_DISCOUNT = 5;
    private static final int NIGHT_VISION_DURATION = 400;

    private final int energyPerUse;
    private final int tier;
    private final boolean solarRecharge;

    public GogglesItem(Properties properties, long maxCharge, int tier, int energyPerUse, boolean solarRecharge) {
        super(properties);
        this.energyPerUse = energyPerUse;
        this.tier = tier;
        this.solarRecharge = solarRecharge;
        attachComponents(ElectricStats.createRechargeableBattery(maxCharge, tier));
    }

    @Override
    public void curioTick(SlotContext context, ItemStack stack) {
        if (!(context.entity() instanceof Player player) || player.level().isClientSide) {
            return;
        }
        IElectricItem item = GTCapabilityHelper.getElectricItem(stack);
        if (item == null) {
            return;
        }
        Level world = player.level();

        FoodData foodStats = player.getFoodData();
        if (foodStats.needsFood() && item.getCharge() >= energyPerUse) {
            item.discharge(energyPerUse, tier, true, false, false);
            foodStats.eat(1, 0.2F);
        }

        var tag = stack.getOrCreateTag();
        byte toggleTimer = tag.contains(TAG_TOGGLE_TIMER) ? tag.getByte(TAG_TOGGLE_TIMER) : 0;
        int nightVisionTimer = tag.contains(TAG_NIGHT_VISION_TIMER)
                ? tag.getInt(TAG_NIGHT_VISION_TIMER) : NIGHT_VISION_DURATION;
        boolean nightVision = tag.getBoolean(TAG_NIGHT_VISION);

        if (toggleTimer == 0 && SyncedKeyMappings.ARMOR_MODE_SWITCH.isKeyDown(player)) {
            toggleTimer = 5;
            if (!nightVision && item.getCharge() >= energyPerUse) {
                nightVision = true;
                player.displayClientMessage(
                        Component.translatable("metaarmor.message.nightvision.enabled"), true);
            } else if (nightVision) {
                nightVision = false;
                disableNightVision(world, player, true);
            } else {
                player.displayClientMessage(
                        Component.translatable("metaarmor.message.nightvision.error"), true);
            }
        }

        if (nightVision && item.getCharge() >= energyPerUse) {
            player.removeEffect(MobEffects.BLINDNESS);
            if (nightVisionTimer <= NIGHT_VISION_DURATION - 160) {
                nightVisionTimer = NIGHT_VISION_DURATION;
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION,
                        NIGHT_VISION_DURATION, 0, true, false));
                player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING,
                        NIGHT_VISION_DURATION, 0, true, false));
            }
            item.discharge(energyPerUse, tier, true, false, false);
        }
        if (solarRecharge && !nightVision && world.isDay()) {
            item.charge(energyPerUse * 2L, tier, true, false);
        }

        if (toggleTimer > 0) {
            toggleTimer--;
        }
        if (nightVisionTimer > 0) {
            nightVisionTimer--;
        }
        tag.putBoolean(TAG_NIGHT_VISION, nightVision);
        tag.putByte(TAG_TOGGLE_TIMER, toggleTimer);
        tag.putInt(TAG_NIGHT_VISION_TIMER, nightVisionTimer);
    }

    @Override
    public void onUnequip(SlotContext context, ItemStack newStack, ItemStack stack) {
        if (context.entity() instanceof Player player) {
            disableNightVision(player.level(), player, false);
        }
    }

    public static void disableNightVision(Level world, Player player, boolean sendMsg) {
        if (!world.isClientSide) {
            player.removeEffect(MobEffects.NIGHT_VISION);
            player.removeEffect(MobEffects.WATER_BREATHING);
            if (sendMsg) {
                player.displayClientMessage(
                        Component.translatable("metaarmor.message.nightvision.disabled"), true);
            }
        }
    }

    @Override
    public boolean revealsThaumcraftAura(ItemStack stack, Player player) {
        return true;
    }

    @Override
    public boolean showsThaumcraftGogglesOverlay(ItemStack stack, Player player) {
        return true;
    }

    @Override
    public int thaumcraftVisDiscount(ItemStack stack, Player player, VisChannel channel) {
        return VIS_DISCOUNT;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("pollution.armor.goggles.food"));
        tooltip.add(Component.translatable("pollution.armor.goggles.water"));
        if (solarRecharge) {
            tooltip.add(Component.translatable("pollution.armor.goggles.solar"));
        }
        tooltip.add(Component.translatable("pollution.armor.goggles.vis_discount", VIS_DISCOUNT));
        boolean nightVision = stack.getOrCreateTag().getBoolean(TAG_NIGHT_VISION);
        tooltip.add(Component.translatable(nightVision
                ? "metaarmor.message.nightvision.enabled"
                : "metaarmor.message.nightvision.disabled"));
    }
}
