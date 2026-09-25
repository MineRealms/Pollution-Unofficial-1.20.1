package meowmel.pollution.dimension.worldgen;

import meowmel.pollution.Pollution;
import meowmel.pollution.dimension.worldgen.feature.GardenFeature;
import meowmel.pollution.dimension.worldgen.feature.CaveShapeFeature;
import meowmel.pollution.dimension.worldgen.feature.AlfheimSchematicFeature;
import meowmel.pollution.dimension.worldgen.feature.AlfheimForestFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * Code-side feature registrations for the ported 1.12 generators.
 *
 * <p>1.12 registered world generators by hand ({@code GameRegistry} /
 * {@code Biome#decorate}); 1.20.1 requires a {@link Feature} type in the
 * {@code minecraft:feature} registry plus {@code configured_feature} /
 * {@code placed_feature} datapack JSON. Only generators with arbitrary code
 * (shape logic that no vanilla feature type expresses) live here; everything
 * else is pure JSON.</p>
 */
public final class PollutionFeatures {

    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, Pollution.MOD_ID);

    /** Port of the 1.12 {@code WorldGenGarden}. */
    public static final RegistryObject<GardenFeature> GARDEN =
            FEATURES.register("garden", () -> new GardenFeature(NoneFeatureConfiguration.CODEC));

    static {
        FEATURES.register("tar_pool", meowmel.pollution.dimension.worldgen.feature.TarPoolFeature::new);
        FEATURES.register("stone_spheres", meowmel.pollution.dimension.worldgen.feature.StoneSphereFeature::new);
        for (CaveShapeFeature.Shape shape : CaveShapeFeature.Shape.values()) {
            FEATURES.register(shape.name().toLowerCase(java.util.Locale.ROOT), () -> new CaveShapeFeature(shape));
        }
        for (AlfheimSchematicFeature.Shape shape : AlfheimSchematicFeature.Shape.values()) {
            FEATURES.register(shape.name().toLowerCase(java.util.Locale.ROOT), () -> new AlfheimSchematicFeature(shape));
        }
        for (AlfheimForestFeature.Forest forest : AlfheimForestFeature.Forest.values()) {
            FEATURES.register("alfheim_" + forest.name().toLowerCase(java.util.Locale.ROOT),
                    () -> new AlfheimForestFeature(forest));
        }
    }

    private PollutionFeatures() {
    }

    public static void init(FMLJavaModLoadingContext context) {
        FEATURES.register(context.getModEventBus());
    }
}
