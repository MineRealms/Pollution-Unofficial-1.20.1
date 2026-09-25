package meowmel.pollution.common.item.bauble;

import com.gregtechceu.gtceu.api.item.armor.ArmorComponentItem;
import com.gregtechceu.gtceu.common.item.armor.GTArmorMaterials;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nullable;
import java.util.List;

/** Powered chest armour with upstream jetpack physics; also accepts the Curios back slot. */
public class WingItem extends ArmorComponentItem implements ICurioItem {

    public static final String TAG_FLIGHT_ACTIVE = "PollutionWingsFlightActive";
    public static final String TAG_PREVIOUS_FLIGHT = "PollutionWingsPreviousFlight";

    private final int energyPerUse;
    private final int tier;
    private final boolean quantum;
    private final float fallDamageReduction;

    public WingItem(Properties properties, long maxCharge, int tier, int energyPerUse,
                    boolean quantum, float fallDamageReduction) {
        super(GTArmorMaterials.JETPACK, ArmorItem.Type.CHESTPLATE, properties);
        this.energyPerUse = energyPerUse;
        this.tier = tier;
        this.quantum = quantum;
        this.fallDamageReduction = fallDamageReduction;
        setArmorLogic(new WingArmorLogic(energyPerUse, maxCharge, tier, quantum));
    }

    public float getFallDamageReduction() {
        return fallDamageReduction;
    }

    @Override
    public void curioTick(SlotContext context, ItemStack stack) {
        if (context.entity() instanceof Player player) onArmorTick(stack, player.level(), player);
    }

    @Override
    public void onArmorTick(ItemStack stack, Level level, Player player) {
        // Remove the creative-flight grant left by the older approximation on upgrade.
        if (player instanceof ServerPlayer serverPlayer) revokeFlight(serverPlayer);
        super.onArmorTick(stack, level, player);
    }

    @Override
    public void onUnequip(SlotContext context, ItemStack newStack, ItemStack stack) {
        if (context.entity() instanceof ServerPlayer player) revokeFlight(player);
    }

    private static void revokeFlight(ServerPlayer player) {
        CompoundTag data = persistedData(player);
        if (!data.getBoolean(TAG_FLIGHT_ACTIVE)) {
            return;
        }
        boolean previous = data.getBoolean(TAG_PREVIOUS_FLIGHT);
        data.remove(TAG_FLIGHT_ACTIVE);
        data.remove(TAG_PREVIOUS_FLIGHT);
        previous |= player.isCreative() || player.isSpectator();
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
