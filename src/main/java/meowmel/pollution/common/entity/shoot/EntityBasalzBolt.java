package meowmel.pollution.common.entity.shoot;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/** Earth elemental bolt fired by {@link meowmel.pollution.common.entity.EntityBasalz}. */
public class EntityBasalzBolt extends ElementalBolt {

    public EntityBasalzBolt(EntityType<? extends EntityBasalzBolt> entityType, Level level) {
        super(entityType, level);
    }

    public EntityBasalzBolt(EntityType<? extends EntityBasalzBolt> entityType, LivingEntity shooter, Level level) {
        super(entityType, shooter, level);
    }

    public EntityBasalzBolt(EntityType<? extends EntityBasalzBolt> entityType, double x, double y, double z, Level level) {
        super(entityType, x, y, z, level);
    }
}
