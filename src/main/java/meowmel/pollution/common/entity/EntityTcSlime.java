package meowmel.pollution.common.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.Level;

/**
 * Base class for the six Thaumcraft-flavoured slimes ported from the 1.12.2
 * Pollution mod (Aer, Ignis, Aqua, Terra, Ordo, Perditio).
 *
 * <p>Upstream extended vanilla {@code EntitySlime}, overrode the loot table per
 * element and only spawned in the Pollution underground biome. The port keeps
 * the vanilla slime AI (jumping, splitting, size-dependent damage) and the
 * per-element identity (entity type, renderer texture). The 1.12 biome
 * restricted natural spawn is not registered because biome spawn lists are data
 * owned by another batch (same policy as the ported elementals). Loot tables
 * are not shipped yet, so the slimes currently drop nothing.</p>
 */
public class EntityTcSlime extends Slime {

    public EntityTcSlime(EntityType<? extends EntityTcSlime> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createTcSlimeAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.2D);
    }
}
