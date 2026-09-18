package meowmel.pollution.common.entity;

import meowmel.pollution.common.entity.shoot.ElementalBolt;
import meowmel.pollution.common.entity.shoot.EntityBlizzBolt;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Blizz - the ice elemental of the 1.12.2 Pollution mod.
 *
 * <p>Fires {@code pollution:blizz_bolt} projectiles with the ported
 * {@link ElementalBoltAttackGoal}; close range still chills the victim: Slowness
 * II for 5 seconds plus a chunk of freeze ticks (powder-snow style frost). See
 * {@link EntityElemental} for the full deviation list.</p>
 */
public class EntityBlizz extends EntityElemental {

    public EntityBlizz(EntityType<? extends EntityBlizz> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createElementalAttributes();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(4, new ElementalBoltAttackGoal(this));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public ElementalBolt createBolt(Level level) {
        return new EntityBlizzBolt(PollutionEntities.BLIZZ_BOLT.get(), this, level);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.BLAZE_AMBIENT;
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        if (!super.doHurtTarget(entity)) {
            return false;
        }
        if (entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1), this);
            living.setTicksFrozen(living.getTicksFrozen() + 120);
        }
        return true;
    }
}
