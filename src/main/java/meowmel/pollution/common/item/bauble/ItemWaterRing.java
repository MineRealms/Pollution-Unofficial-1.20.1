package meowmel.pollution.common.item.bauble;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import vazkii.botania.api.mana.ManaItemHandler;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Water ring: Curios port of upstream {@code ItemWaterRing} (a Baubles ring).
 *
 * <p>While worn in the {@code ring} slot and the wearer is in water, the ring
 * consumes one unit of stored source per tick to boost swimming motion, grant
 * night vision and, when drowning, request Botania mana to refill air.
 * Upstream also refused to apply its night vision while Botania's own water
 * ring occupied the first Baubles slot; Curios has no fixed first-ring slot,
 * so that cross-mod guard is intentionally dropped.</p>
 *
 * <p>TODO(port): upstream refilled the source store through
 * {@code SourceMaterialItem} integrations (Botania cosmetics/phantom ink).
 * Only the storage contract is ported for now; recipes or machines can charge
 * the ring through {@link #addSource(int, boolean, ItemStack)}.</p>
 */
public class ItemWaterRing extends Item implements ICurioItem {

    public static final String TAG_SOURCE = "source";
    private static final String TAG_NIGHT_VISION = "pollution_water_ring_night_vision";
    private static final int NIGHT_VISION_DURATION = 300;

    private final int consumeSource;
    private final int maxSource;
    private final String materialKey;

    public ItemWaterRing(Properties properties, int consumeSource, int maxSource, String materialKey) {
        super(properties);
        this.consumeSource = consumeSource;
        this.maxSource = maxSource;
        this.materialKey = materialKey;
    }

    @Override
    public void curioTick(SlotContext context, ItemStack stack) {
        if (!(context.entity() instanceof Player player)) {
            return;
        }
        if (!player.isInWater() || !consumeSource(consumeSource, false, stack)) {
            removeNightVision(player);
            return;
        }
        consumeSource(consumeSource, true, stack);

        if (player.level().isClientSide) {
            return;
        }

        double motionX = player.getDeltaMovement().x * 1.2D;
        double motionY = player.getDeltaMovement().y * 1.2D;
        double motionZ = player.getDeltaMovement().z * 1.2D;
        boolean flying = player.getAbilities().flying;
        if (!flying) {
            player.setDeltaMovement(
                    Math.abs(motionX) < 1.3D ? motionX : player.getDeltaMovement().x,
                    Math.abs(motionY) < 1.3D ? motionY : player.getDeltaMovement().y,
                    Math.abs(motionZ) < 1.3D ? motionZ : player.getDeltaMovement().z);
            player.hurtMarked = true;
        }

        applyNightVision(player);

        if (player.getAirSupply() <= 1) {
            int mana = ManaItemHandler.INSTANCE.requestMana(stack, player, 300, true);
            if (mana > 0) {
                player.setAirSupply(mana);
            }
        }
    }

    @Override
    public void onUnequip(SlotContext context, ItemStack newStack, ItemStack stack) {
        if (context.entity() instanceof Player player) {
            removeNightVision(player);
        }
    }

    private static void applyNightVision(Player player) {
        CompoundTag data = player.getPersistentData();
        if (data.getBoolean(TAG_NIGHT_VISION) && player.hasEffect(MobEffects.NIGHT_VISION)) {
            return;
        }
        data.putBoolean(TAG_NIGHT_VISION, true);
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION,
                NIGHT_VISION_DURATION, 0, true, false));
    }

    private static void removeNightVision(Player player) {
        CompoundTag data = player.getPersistentData();
        if (data.getBoolean(TAG_NIGHT_VISION)) {
            data.remove(TAG_NIGHT_VISION);
            if (!player.level().isClientSide) {
                player.removeEffect(MobEffects.NIGHT_VISION);
            }
        }
    }

    public int getMaxSourceStore() {
        return maxSource;
    }

    public int getSourceStore(ItemStack stack) {
        return stack.getOrCreateTag().getInt(TAG_SOURCE);
    }

    public void setSourceStore(int source, ItemStack stack) {
        stack.getOrCreateTag().putInt(TAG_SOURCE, Math.max(0, Math.min(source, maxSource)));
    }

    public boolean addSource(int amount, boolean simulate, ItemStack stack) {
        if (amount <= 0) {
            return false;
        }
        int stored = getSourceStore(stack);
        if (stored + amount > maxSource) {
            return false;
        }
        if (!simulate) {
            setSourceStore(stored + amount, stack);
        }
        return true;
    }

    public boolean consumeSource(int amount, boolean simulate, ItemStack stack) {
        if (amount < 0) {
            return false;
        }
        int stored = getSourceStore(stack);
        if (stored < amount) {
            return false;
        }
        if (!simulate) {
            setSourceStore(stored - amount, stack);
        }
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("pollution.bauble.source",
                getSourceStore(stack), getMaxSourceStore()));
        tooltip.add(Component.translatable("pollution.bauble.material",
                Component.translatable(materialKey)));
    }
}
