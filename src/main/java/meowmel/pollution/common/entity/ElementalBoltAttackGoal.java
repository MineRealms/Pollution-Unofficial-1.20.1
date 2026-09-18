package meowmel.pollution.common.entity;

import meowmel.pollution.common.entity.shoot.ElementalBolt;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;

import java.util.EnumSet;

/**
 * Modern port of the upstream {@code AIBasalzBoltAttack}.
 *
 * <p>Blaze-style attack pattern: melee when the target is closer than two
 * blocks, otherwise wind up and fire a short volley of elemental bolts
 * (60 tick wind-up, three shots 6 ticks apart, 100 tick cooldown). The synced
 * attack-mode flag is toggled so the client can animate the wind-up.</p>
 */
public class ElementalBoltAttackGoal extends Goal {

    private final EntityElemental mob;
    private int attackStep;
    private int attackTime;

    public ElementalBoltAttackGoal(EntityElemental mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.mob.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public void start() {
        this.attackStep = 0;
        this.attackTime = 0;
    }

    @Override
    public void stop() {
        this.mob.setInAttackMode(false);
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target == null) {
            return;
        }
        double distanceSqr = this.mob.distanceToSqr(target);
        --this.attackTime;

        if (distanceSqr < 4.0D) {
            if (this.attackTime <= 0) {
                this.attackTime = 20;
                this.mob.doHurtTarget(target);
            }
            this.mob.getMoveControl().setWantedPosition(target.getX(), target.getY(), target.getZ(), 1.0D);
        } else if (distanceSqr < followDistanceSqr()) {
            if (this.attackTime <= 0) {
                ++this.attackStep;
                if (this.attackStep == 1) {
                    this.attackTime = 60;
                    this.mob.setInAttackMode(true);
                } else if (this.attackStep <= 4) {
                    this.attackTime = 6;
                } else {
                    this.attackTime = 100;
                    this.attackStep = 0;
                    this.mob.setInAttackMode(false);
                }
                if (this.attackStep > 1) {
                    shootBolt(target);
                }
            }
            this.mob.getLookControl().setLookAt(target, 10.0F, 10.0F);
        } else {
            this.mob.getNavigation().stop();
            this.mob.getMoveControl().setWantedPosition(target.getX(), target.getY(), target.getZ(), 1.0D);
        }
    }

    private void shootBolt(LivingEntity target) {
        Level level = this.mob.level();
        ElementalBolt bolt = this.mob.createBolt(level);
        bolt.setPos(this.mob.getX(), this.mob.getY() + this.mob.getBbHeight() / 2.0F + 0.5D, this.mob.getZ());
        double dx = target.getX() - this.mob.getX();
        double dy = target.getY(0.5D) - bolt.getY();
        double dz = target.getZ() - this.mob.getZ();
        bolt.shoot(dx, dy, dz, 1.5F, 1.0F);
        level.playSound(null, this.mob.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE,
                2.0F, (this.mob.getRandom().nextFloat() - this.mob.getRandom().nextFloat()) * 0.2F + 1.0F);
        level.addFreshEntity(bolt);
    }

    private double followDistanceSqr() {
        double followRange = this.mob.getAttributeValue(Attributes.FOLLOW_RANGE);
        if (followRange <= 0.0D) {
            followRange = 16.0D;
        }
        return followRange * followRange;
    }
}
