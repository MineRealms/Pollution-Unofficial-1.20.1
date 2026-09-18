package meowmel.pollution.common.entity;

import meowmel.pollution.Pollution;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Entity registry for the Pollution port.
 *
 * <p>Registered ids (all under the {@code pollution} namespace):
 * <ul>
 *   <li>{@code pollution:basalz}</li>
 *   <li>{@code pollution:blitz}</li>
 *   <li>{@code pollution:blizz}</li>
 * </ul>
 * Upstream used {@code ele_basalz} / {@code ele_blitz} / {@code ele_blizz}; the
 * {@code ele_} prefix was dropped for cleaner ids and lang keys
 * ({@code entity.pollution.basalz}, ...).</p>
 *
 * <p>Natural spawning (upstream added the mobs to the underground biome at
 * weight 10, 1-4 per pack) is not registered yet - it needs biome spawn data
 * owned by another batch. The types can be spawned via commands, spawn eggs or
 * {@code /summon}.</p>
 */
public final class PollutionEntities {

    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Pollution.MOD_ID);

    public static final RegistryObject<EntityType<EntityBasalz>> BASALZ = ENTITY_TYPES.register("basalz",
            () -> EntityType.Builder.of(EntityBasalz::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(4)
                    .updateInterval(3)
                    .build(Pollution.MOD_ID + ":basalz"));

    public static final RegistryObject<EntityType<EntityBlitz>> BLITZ = ENTITY_TYPES.register("blitz",
            () -> EntityType.Builder.of(EntityBlitz::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(4)
                    .updateInterval(3)
                    .build(Pollution.MOD_ID + ":blitz"));

    public static final RegistryObject<EntityType<EntityBlizz>> BLIZZ = ENTITY_TYPES.register("blizz",
            () -> EntityType.Builder.of(EntityBlizz::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(4)
                    .updateInterval(3)
                    .build(Pollution.MOD_ID + ":blizz"));

    public static void init(FMLJavaModLoadingContext context) {
        IEventBus modBus = context.getModEventBus();
        ENTITY_TYPES.register(modBus);
        modBus.addListener(PollutionEntities::onEntityAttributeCreation);
    }

    private static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(BASALZ.get(), EntityBasalz.createAttributes().build());
        event.put(BLITZ.get(), EntityBlitz.createAttributes().build());
        event.put(BLIZZ.get(), EntityBlizz.createAttributes().build());
    }

    private PollutionEntities() {}
}
