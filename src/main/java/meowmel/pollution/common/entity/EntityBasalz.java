package meowmel.pollution.common.entity;

import meowmel.pollution.common.entity.shoot.ElementalBolt;
import meowmel.pollution.common.entity.shoot.EntityBasalzBolt;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
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
 * Basalz - the earth elemental of the 1.12.2 Pollution mod.
 *
 * <p>Fires {@code pollution:basalz_bolt} projectiles with the ported
 * {@link ElementalBoltAttackGoal}; close range still lands a heavy knockback
 * melee hit, matching the upstream bolt's "heavy hit" feel. See
 * {@link EntityElemental} for the full deviation list.</p>
 */
public class EntityBasalz extends EntityElemental {

    public EntityBasalz(EntityType<? extends EntityBasalz> entityType, Level level) {
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
        return new EntityBasalzBolt(PollutionEntities.BASALZ_BOLT.get(), this, level);
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
            double dx = living.getX() - this.getX();
            double dz = living.getZ() - this.getZ();
            double distance = Math.max(Math.sqrt(dx * dx + dz * dz), 1.0E-4D);
            living.knockback(1.2D, dx / distance, dz / distance);
        }
        return true;
    }
}
