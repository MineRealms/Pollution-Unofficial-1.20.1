package meowmel.pollution.common.entity;

import meowmel.pollution.Pollution;
import meowmel.pollution.common.entity.shoot.ElementalBolt;
import meowmel.pollution.common.entity.shoot.EntityBasalzBolt;
import meowmel.pollution.common.entity.shoot.EntityBlitzBolt;
import meowmel.pollution.common.entity.shoot.EntityBlizzBolt;
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
 *   <li>mobs: {@code basalz}, {@code blitz}, {@code blizz}</li>
 *   <li>slimes: {@code tc_slime_aer}, {@code tc_slime_ignis},
 *       {@code tc_slime_aqua}, {@code tc_slime_terra}, {@code tc_slime_ordo},
 *       {@code tc_slime_perditio}</li>
 *   <li>bolts: {@code basalz_bolt}, {@code blitz_bolt}, {@code blizz_bolt}</li>
 * </ul>
 * Upstream used {@code ele_basalz} / {@code ele_blitz} / {@code ele_blizz}; the
 * {@code ele_} prefix was dropped for cleaner ids and lang keys
 * ({@code entity.pollution.basalz}, ...).</p>
 *
 * <p>Natural spawning (upstream added the mobs and slimes to the underground
 * biome) is not registered yet - it needs biome spawn data owned by another
 * batch. The types can be spawned via commands, spawn eggs or {@code /summon}.</p>
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

    // ////////////////////////////////////
    // ***** Thaumcraft slimes *****//
    // ////////////////////////////////////

    public static final RegistryObject<EntityType<EntitySlimeAer>> SLIME_AER = slime("tc_slime_aer", EntitySlimeAer::new);
    public static final RegistryObject<EntityType<EntitySlimeIgnis>> SLIME_IGNIS = slime("tc_slime_ignis", EntitySlimeIgnis::new);
    public static final RegistryObject<EntityType<EntitySlimeAqua>> SLIME_AQUA = slime("tc_slime_aqua", EntitySlimeAqua::new);
    public static final RegistryObject<EntityType<EntitySlimeTerra>> SLIME_TERRA = slime("tc_slime_terra", EntitySlimeTerra::new);
    public static final RegistryObject<EntityType<EntitySlimeOrdo>> SLIME_ORDO = slime("tc_slime_ordo", EntitySlimeOrdo::new);
    public static final RegistryObject<EntityType<EntitySlimePerditio>> SLIME_PERDITIO = slime("tc_slime_perditio", EntitySlimePerditio::new);

    private static <T extends EntityTcSlime> RegistryObject<EntityType<T>> slime(
            String name, EntityType.EntityFactory<T> factory) {
        return ENTITY_TYPES.register(name,
                () -> EntityType.Builder.of(factory, MobCategory.MONSTER)
                        .sized(2.04F, 2.04F)
                        .clientTrackingRange(10)
                        .build(Pollution.MOD_ID + ":" + name));
    }

    // ////////////////////////////////////
    // ***** elemental bolts *****//
    // ////////////////////////////////////

    public static final RegistryObject<EntityType<EntityBasalzBolt>> BASALZ_BOLT = bolt("basalz_bolt", EntityBasalzBolt::new);
    public static final RegistryObject<EntityType<EntityBlitzBolt>> BLITZ_BOLT = bolt("blitz_bolt", EntityBlitzBolt::new);
    public static final RegistryObject<EntityType<EntityBlizzBolt>> BLIZZ_BOLT = bolt("blizz_bolt", EntityBlizzBolt::new);

    private static <T extends ElementalBolt> RegistryObject<EntityType<T>> bolt(
            String name, EntityType.EntityFactory<T> factory) {
        return ENTITY_TYPES.register(name,
                () -> EntityType.Builder.of(factory, MobCategory.MISC)
                        .sized(0.25F, 0.25F)
                        .clientTrackingRange(4)
                        .updateInterval(10)
                        .build(Pollution.MOD_ID + ":" + name));
    }

    public static void init(FMLJavaModLoadingContext context) {
        IEventBus modBus = context.getModEventBus();
        ENTITY_TYPES.register(modBus);
        modBus.addListener(PollutionEntities::onEntityAttributeCreation);
    }

    private static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(BASALZ.get(), EntityBasalz.createAttributes().build());
        event.put(BLITZ.get(), EntityBlitz.createAttributes().build());
        event.put(BLIZZ.get(), EntityBlizz.createAttributes().build());
        event.put(SLIME_AER.get(), EntityTcSlime.createTcSlimeAttributes().build());
        event.put(SLIME_IGNIS.get(), EntityTcSlime.createTcSlimeAttributes().build());
        event.put(SLIME_AQUA.get(), EntityTcSlime.createTcSlimeAttributes().build());
        event.put(SLIME_TERRA.get(), EntityTcSlime.createTcSlimeAttributes().build());
        event.put(SLIME_ORDO.get(), EntityTcSlime.createTcSlimeAttributes().build());
        event.put(SLIME_PERDITIO.get(), EntityTcSlime.createTcSlimeAttributes().build());
    }

    private PollutionEntities() {}
}
