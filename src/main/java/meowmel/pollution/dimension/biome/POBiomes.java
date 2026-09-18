package meowmel.pollution.dimension.biome;

import meowmel.pollution.Pollution;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

/**
 * Biome ids ported from the 1.12 {@code POBiomeHandler}.
 *
 * <p>On 1.20.1 the biomes themselves are datapack JSON under
 * {@code data/pollution/worldgen/biome/}. This class only carries the
 * {@link ResourceKey}s so the codec-based biome sources and future dimension
 * wiring share one source of truth.</p>
 *
 * <p>Upstream registry names were kept where the target already referenced
 * them ({@code pollution:underground}); legacy 1.12 names
 * ({@code pollution_biome.1}/{@code pollution_biome.3}) were normalized to
 * {@code pollution:demiplane}/{@code pollution:blood}. The 12 Alfheim ids match
 * upstream verbatim.</p>
 */
public final class POBiomes {

    // Underground world: fallback + 7 style biomes (upstream UndergroundBiomes).
    public static final ResourceKey<Biome> UNDERGROUND = key("underground");
    public static final ResourceKey<Biome> STALACTITE_CLUSTER = key("stalactite_cluster");
    public static final ResourceKey<Biome> CRYSTAL_CLUSTER = key("crystal_cluster");
    public static final ResourceKey<Biome> MUSHROOM_FOREST = key("mushroom_forest");
    public static final ResourceKey<Biome> LUSH_CAVE = key("lush_cave");
    public static final ResourceKey<Biome> PRIMORDIAL_CAVE = key("primordial_cave");
    public static final ResourceKey<Biome> DESERT_CAVE = key("desert_cave");
    public static final ResourceKey<Biome> MAGMA_CAVE = key("magma_cave");

    // Other Pollution dimensions.
    public static final ResourceKey<Biome> DEMIPLANE = key("demiplane");
    public static final ResourceKey<Biome> BLOOD = key("blood");

    // Alfheim (upstream AlfheimBiomes).
    public static final ResourceKey<Biome> ALFHEIM_FIELD = key("alfheim_field");
    public static final ResourceKey<Biome> ALFHEIM_GIANT_FLOWER_FIELD = key("alfheim_giant_flower_field");
    public static final ResourceKey<Biome> ALFHEIM_BEACH = key("alfheim_beach");
    public static final ResourceKey<Biome> ALFHEIM_SANDBANK = key("alfheim_sandbank");
    public static final ResourceKey<Biome> ALFHEIM_RIVER = key("alfheim_river");
    public static final ResourceKey<Biome> ALFHEIM_LOW_PLATEAU = key("alfheim_low_plateau");
    public static final ResourceKey<Biome> ALFHEIM_MID_PLATEAU = key("alfheim_mid_plateau");
    public static final ResourceKey<Biome> ALFHEIM_HIGH_PLATEAU = key("alfheim_high_plateau");
    public static final ResourceKey<Biome> ALFHEIM_HIGH_PLATEAU_FOREST = key("alfheim_high_plateau_forest");
    public static final ResourceKey<Biome> ALFHEIM_HIGH_PLATEAU_FIELD = key("alfheim_high_plateau_field");
    public static final ResourceKey<Biome> ALFHEIM_ISLAND_FOREST = key("alfheim_island_forest");
    public static final ResourceKey<Biome> ALFHEIM_PIT_FOREST = key("alfheim_pit_forest");

    private static ResourceKey<Biome> key(String path) {
        return ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID, path));
    }

    private POBiomes() {
    }
}
