package meowmel.pollution.common.item.bauble;

import com.gregtechceu.gtceu.common.item.armor.Jetpack;
import meowmel.pollution.Pollution;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** The upstream wing thrust, hover speeds, particles and powered potion bonuses. */
final class WingArmorLogic extends Jetpack {
    private final boolean quantum;

    WingArmorLogic(int energyPerUse, long capacity, int tier, boolean quantum) {
        super(energyPerUse, capacity, tier);
        this.quantum = quantum;
    }

    @Override
    public void onArmorTick(Level level, Player player, ItemStack stack) {
        int cost = energyPerUse * (quantum ? 1 : 2);
        if (!level.isClientSide && canUseEnergy(stack, cost)) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 10, 1, true, false));
            if (quantum) {
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 10, 1, true, false));
                player.addEffect(new MobEffectInstance(MobEffects.JUMP, 10, 1, true, false));
            }
            drainEnergy(stack, cost);
        }
        super.onArmorTick(level, player, stack);
    }

    @Override public double getSprintEnergyModifier() { return quantum ? 5 : 2.5; }
    @Override public double getSprintSpeedModifier() { return quantum ? 2 : 1.5; }
    @Override public double getVerticalHoverSpeed() { return quantum ? 0.8 : 0.5; }
    @Override public double getVerticalHoverSlowSpeed() { return quantum ? 0.1 : 0.05; }
    @Override public double getVerticalAcceleration() { return quantum ? 0.5 : 0.2; }
    @Override public double getVerticalSpeed() { return quantum ? 1 : 0.6; }
    @Override public double getSidewaysSpeed() { return quantum ? 0.4 : 0.2; }
    @Override public float getFallDamageReduction() { return quantum ? 4 : 2; }
    @Override public ParticleOptions getParticle() { return ParticleTypes.CLOUD; }

    @Override
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID,
                "textures/armor/" + (quantum ? "quantumwing" : "nanowing") + ".png");
    }
}
