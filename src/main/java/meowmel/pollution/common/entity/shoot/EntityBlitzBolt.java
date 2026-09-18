package meowmel.pollution.common.entity.shoot;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/** Air/lightning elemental bolt fired by {@link meowmel.pollution.common.entity.EntityBlitz}. */
public class EntityBlitzBolt extends ElementalBolt {

    public EntityBlitzBolt(EntityType<? extends EntityBlitzBolt> entityType, Level level) {
        super(entityType, level);
    }

    public EntityBlitzBolt(EntityType<? extends EntityBlitzBolt> entityType, LivingEntity shooter, Level level) {
        super(entityType, shooter, level);
    }

    public EntityBlitzBolt(EntityType<? extends EntityBlitzBolt> entityType, double x, double y, double z, Level level) {
        super(entityType, x, y, z, level);
    }
}
