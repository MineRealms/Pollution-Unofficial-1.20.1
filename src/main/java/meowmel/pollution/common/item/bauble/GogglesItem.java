package meowmel.pollution.common.item.bauble;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.armor.ArmorComponentItem;
import com.gregtechceu.gtceu.api.item.armor.ArmorLogicSuite;
import com.gregtechceu.gtceu.common.item.armor.GTArmorMaterials;
import meowmel.pollution.Pollution;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
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

/** Powered helmet and Curios head accessory exposing the Thaumcraft goggles APIs. */
public class GogglesItem extends ArmorComponentItem
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
        super(GTArmorMaterials.GOGGLES, ArmorItem.Type.HELMET, properties);
        this.energyPerUse = energyPerUse;
        this.tier = tier;
        this.solarRecharge = solarRecharge;
        setArmorLogic(new ArmorLogicSuite(energyPerUse, maxCharge, tier, ArmorItem.Type.HELMET) {
            @Override
            public void onArmorTick(Level level, Player player, ItemStack stack) {
                tickGoggles(player, stack);
            }

            @Override
            public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
                return ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID, "textures/armor/" + (solarRecharge ? "quantum_goggles" : "nano_goggles") + ".png");
            }
        });
    }

    @Override
    public void curioTick(SlotContext context, ItemStack stack) {
        if (context.entity() instanceof Player player) tickGoggles(player, stack);
    }

    private void tickGoggles(Player player, ItemStack stack) {
        if (player.level().isClientSide) return;
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
            AccessoryEffects.refresh(player, "goggles", MobEffects.NIGHT_VISION, NIGHT_VISION_DURATION);
            AccessoryEffects.refresh(player, "goggles", MobEffects.WATER_BREATHING, NIGHT_VISION_DURATION);
            item.discharge(energyPerUse, tier, true, false, false);
        } else if (nightVision) {
            nightVision = false;
            disableNightVision(world, player, false);
        }
        if (solarRecharge && !nightVision && world.isDay()) {
            item.charge(energyPerUse * 2L, tier, true, false);
        }

        if (toggleTimer > 0) {
            toggleTimer--;
        }
        tag.putBoolean(TAG_NIGHT_VISION, nightVision);
        tag.putByte(TAG_TOGGLE_TIMER, toggleTimer);
    }

    @Override
    public void onUnequip(SlotContext context, ItemStack newStack, ItemStack stack) {
        if (context.entity() instanceof Player player) {
            disableNightVision(player.level(), player, false);
        }
    }

    public static void disableNightVision(Level world, Player player, boolean sendMsg) {
        if (!world.isClientSide) {
            AccessoryEffects.remove(player, "goggles", MobEffects.NIGHT_VISION);
            AccessoryEffects.remove(player, "goggles", MobEffects.WATER_BREATHING);
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
