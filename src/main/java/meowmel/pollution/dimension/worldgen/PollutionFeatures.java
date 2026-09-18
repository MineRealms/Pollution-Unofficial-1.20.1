package meowmel.pollution.dimension.worldgen;

import meowmel.pollution.Pollution;
import meowmel.pollution.dimension.worldgen.feature.GardenFeature;
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

    private PollutionFeatures() {
    }

    public static void init(FMLJavaModLoadingContext context) {
        FEATURES.register(context.getModEventBus());
    }
}
