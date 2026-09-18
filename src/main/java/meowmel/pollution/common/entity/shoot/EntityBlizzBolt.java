package meowmel.pollution.common.entity.shoot;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/** Ice elemental bolt fired by {@link meowmel.pollution.common.entity.EntityBlizz}. */
public class EntityBlizzBolt extends ElementalBolt {

    public EntityBlizzBolt(EntityType<? extends EntityBlizzBolt> entityType, Level level) {
        super(entityType, level);
    }

    public EntityBlizzBolt(EntityType<? extends EntityBlizzBolt> entityType, LivingEntity shooter, Level level) {
        super(entityType, shooter, level);
    }

    public EntityBlizzBolt(EntityType<? extends EntityBlizzBolt> entityType, double x, double y, double z, Level level) {
        super(entityType, x, y, z, level);
    }
}
