package meowmel.pollution.common.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Base class for the three Thaumcraft-flavoured elementals ported from the
 * 1.12.2 Pollution mod (Basalz, Blitz, Blizz).
 *
 * <p>Upstream extended {@code EntityMob} and hovered like a blaze: reduced
 * vertical speed while airborne, no fall damage, a randomised "height offset"
 * used to float upwards towards flying targets, ambient town-aura particles
 * (renamed {@code MYCELIUM} in modern versions) and a synced attack-mode flag
 * toggled by the ranged attack AI.
 * All of that is preserved here with modern mappings.</p>
 *
 * <p>DEVIATIONS from upstream:
 * <ul>
 *   <li>The 1.12.2 ranged bolts ({@code EntityBasalzBolt} / {@code EntityBlitzBolt} /
 *       {@code EntityBlizzBolt}) are NOT ported in this batch. The mobs fight in
 *       melee ({@link net.minecraft.world.entity.ai.goal.MeleeAttackGoal}) and apply
 *       a small elemental rider on hit instead; projectiles are deferred to a later
 *       batch.</li>
 *   <li>Upstream bolts applied Weakness (5s, amplifier II, non-curable) to every
 *       victim. Here the rider is per element (knockback / fire / frost) and
 *       Weakness is dropped.</li>
 *   <li>Upstream registered biome spawns in the underground biome and a loot table
 *       ({@code pollution:entities/<name>}). Natural spawning and loot tables are
 *       deferred (they need data files owned by other batches); the entity types
 *       can still be spawned with commands or spawn eggs.</li>
 *   <li>Client rendering (renderer + texture layer) is deferred to the client
 *       batch: no renderer is registered, so the mobs are invisible until then
 *       but server-side AI, attributes and particles work.</li>
 * </ul>
 * </p>
 */
public abstract class EntityElemental extends Monster {

    private static final EntityDataAccessor<Boolean> DATA_ATTACK_MODE =
            SynchedEntityData.defineId(EntityElemental.class, EntityDataSerializers.BOOLEAN);

    protected float heightOffset = 0.5F;
    protected int heightOffsetUpdateTime;

    protected EntityElemental(EntityType<? extends EntityElemental> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 10;
    }

    public static AttributeSupplier.Builder createElementalAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.23000000417232513D)
                .add(Attributes.FOLLOW_RANGE, 48.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ATTACK_MODE, false);
    }

    @Override
    public void aiStep() {
        Vec3 movement = this.getDeltaMovement();
        if (!this.onGround() && movement.y < 0.0D) {
            this.setDeltaMovement(movement.multiply(1.0D, 0.6D, 1.0D));
        }
        if (this.level().isClientSide) {
            for (int i = 0; i < 2; ++i) {
                this.level().addParticle(ParticleTypes.MYCELIUM,
                        this.getX() + (this.random.nextDouble() - 0.5D) * (double) this.getBbWidth(),
                        this.getY() + this.random.nextDouble() * (double) this.getBbHeight(),
                        this.getZ() + (this.random.nextDouble() - 0.5D) * (double) this.getBbWidth(),
                        0.0D, 0.0D, 0.0D);
            }
        }
        super.aiStep();
    }

    @Override
    protected void customServerAiStep() {
        --this.heightOffsetUpdateTime;
        if (this.heightOffsetUpdateTime <= 0) {
            this.heightOffsetUpdateTime = 100;
            this.heightOffset = 0.5F + (float) this.random.nextGaussian() * 3.0F;
        }
        LivingEntity target = this.getTarget();
        if (target != null && target.getEyeY() > this.getEyeY() + (double) this.heightOffset) {
            Vec3 movement = this.getDeltaMovement();
            this.setDeltaMovement(movement.x, movement.y + (0.3D - movement.y) * 0.3D, movement.z);
        }
        super.customServerAiStep();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.BLAZE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BLAZE_DEATH;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    public void setInAttackMode(boolean inAttackMode) {
        this.entityData.set(DATA_ATTACK_MODE, inAttackMode);
    }

    public boolean isInAttackMode() {
        return this.entityData.get(DATA_ATTACK_MODE);
    }
}
