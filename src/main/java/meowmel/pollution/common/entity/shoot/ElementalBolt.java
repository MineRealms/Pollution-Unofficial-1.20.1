package meowmel.pollution.common.entity.shoot;

import net.minecraft.core.particles.ParticleTypes;
import meowmel.pollution.common.entity.EntityElemental;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.List;

/**
 * Base class for the three elemental bolts of the 1.12.2 Pollution mod
 * ({@code EntityBasalzBolt} / {@code EntityBlitzBolt} / {@code EntityBlizzBolt}).
 *
 * <p>Upstream extended {@code EntityThrowable}: near-zero gravity, 5 damage on
 * impact with an indirect projectile damage source named after the element, and
 * a non-curable Weakness (5s, amplifier II) rider. Modern
 * {@link ThrowableProjectile} keeps the same trajectory; the damage type uses
 * the vanilla {@code thrown} source because 1.12 custom damage source names have
 * no equivalent, and the Weakness instance clears its curative items exactly
 * like upstream.</p>
 *
 * <p>Deviation: upstream bolts only skipped friendly fire against
 * {@code EntityBasalz} (a copy-paste bug in the Blitz/Blizz bolts); the port
 * treats every {@link meowmel.pollution.common.entity.EntityElemental} as
 * friendly and deals no damage to it.</p>
 */
public abstract class ElementalBolt extends ThrowableProjectile implements ItemSupplier {

    private static final float DAMAGE = 5.0F;
    private static final int WEAKNESS_DURATION = 5 * 20;
    private static final int WEAKNESS_AMPLIFIER = 2;

    protected ElementalBolt(EntityType<? extends ElementalBolt> entityType, Level level) {
        super(entityType, level);
    }

    protected ElementalBolt(EntityType<? extends ElementalBolt> entityType, LivingEntity shooter, Level level) {
        super(entityType, shooter, level);
    }

    protected ElementalBolt(EntityType<? extends ElementalBolt> entityType, double x, double y, double z, Level level) {
        super(entityType, x, y, z, level);
    }

    @Override
    protected float getGravity() {
        return 0.005F;
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(Items.SLIME_BALL);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.level().isClientSide) {
            return;
        }
        if (result.getType() == HitResult.Type.ENTITY) {
            Entity target = ((EntityHitResult) result).getEntity();
            if (target instanceof LivingEntity living) {
                if (isFriendly(living)) {
                    living.hurt(this.damageSources().thrown(this, this.getOwner()), 0.0F);
                } else if (living.hurt(this.damageSources().thrown(this, this.getOwner()), DAMAGE)) {
                    MobEffectInstance weakness = new MobEffectInstance(
                            MobEffects.WEAKNESS, WEAKNESS_DURATION, WEAKNESS_AMPLIFIER, false, true);
                    weakness.setCurativeItems(List.of());
                    living.addEffect(weakness, this.getOwner() instanceof LivingEntity shooter ? shooter : null);
                }
            }
        }
        for (int i = 0; i < 8; i++) {
            this.level().addParticle(ParticleTypes.EXPLOSION,
                    this.getX(), this.getY(), this.getZ(),
                    this.random.nextDouble(), this.random.nextDouble(), this.random.nextDouble());
        }
        this.discard();
    }

    protected boolean isFriendly(LivingEntity living) {
        return living instanceof EntityElemental;
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
    }
}
