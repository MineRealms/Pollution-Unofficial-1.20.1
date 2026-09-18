package meowmel.pollution.dimension;

import meowmel.pollution.Pollution;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/**
 * Resource keys and data-registration notes for the four Pollution dimensions.
 *
 * <p>Dimensions are data-driven in 1.20.1, so this class only centralises the
 * {@link ResourceKey}s used by the portal wiring. The matching data files
 * are:</p>
 * <ul>
 *   <li>{@code data/pollution/dimension_type/underground.json} (already
 *       registered by the first dimension batch)</li>
 *   <li>{@code data/pollution/dimension_type/alfheim.json},
 *       {@code blood.json}, {@code demiplane.json}</li>
 *   <li>the four matching {@code data/pollution/dimension/*.json} stems</li>
 * </ul>
 *
 * <p>Upstream 1.12.2 mapping ({@code PODimensionManager} /
 * {@code PODimensionType}): demiplane id 40, underground id 41, blood id 42,
 * alfheim id 43 (configurable). The numeric ids have no meaning in 1.20.1 and
 * are kept here for reference only.</p>
 *
 * <p>Dimension-type values were taken from the upstream providers:</p>
 * <ul>
 *   <li>underground ({@code UndergroundWorlds}): no skylight, ceiling,
 *       ambient 0.1, fixed time 18000.</li>
 *   <li>alfheim ({@code AlfheimWorld}): surface world, skylight, no ceiling,
 *       ambient 0.0, normal day/night, respawn allowed (upstream config
 *       default {@code enableAlfheimRespawn = true}).</li>
 *   <li>blood ({@code BloodWorld}): not a surface world, skylight (1.12
 *       {@code WorldProvider} default), ambient 0.0, fixed noon
 *       ({@code calculateCelestialAngle} 0.0), no respawn.</li>
 *   <li>demiplane ({@code DimensionDemiplane}): surface world, skylight,
 *       ambient 0.0, fixed celestial angle 0.5 (time 18000), no respawn.
 *       Upstream also forced {@code isDaytime} true, which 1.20.1 derives
 *       from the level time instead; the fixed time keeps the upstream sky
 *       angle.</li>
 * </ul>
 *
 * <p>Stubs kept for later batches:</p>
 * <ul>
 *   <li><b>Biome sources</b> — the worldgen batch owns the biome JSONs. Only
 *       {@code pollution:underground} exists today, so the alfheim, blood and
 *       demiplane dimension JSONs use {@code minecraft:plains} as their fixed
 *       biome until {@code pollution:alfheim}, {@code pollution:blood} and
 *       {@code pollution:demiplane} land; switch the fixed biome then.</li>
 *   <li><b>Chunk generators</b> — the custom WorldEngine generators
 *       ({@code ChunkGeneratorAlfheim}, {@code ChunkGeneratorBlood}) are not
 *       ported yet, so all three new dimensions use the
 *       {@code minecraft:overworld} noise settings placeholder.
 *       {@code DimensionDemiplane} used the vanilla overworld generator
 *       upstream, so for it the placeholder is already the final shape.</li>
 *   <li><b>Fog and sky colours</b> — in 1.20.1 these live on the biome
 *       ({@code effects.fog_color} / {@code effects.sky_color}), not on the
 *       dimension type. The upstream provider values are recorded for the
 *       biome batch: blood fog {@code (0.8, 0.1, 0.1)} with sky colour 0;
 *       demiplane sky {@code (0.35, 0.0, 0.35)}; alfheim sky
 *       {@code 0x266EFF} (see {@code AlfheimBiome}).</li>
 *   <li><b>Client effects</b> — Alfheim's Garden of Glass sky renderer
 *       (Botania, client-only in 1.12) has no dimension-type hook in 1.20.1;
 *       it needs a client {@code DimensionSpecialEffects} in the client
 *       batch.</li>
 * </ul>
 */
public final class PollutionDimensions {

    /** Upstream id 43, {@code AlfheimWorld}. */
    public static final ResourceKey<Level> ALFHEIM = key("alfheim");

    /** Upstream id 42, {@code BloodWorld}. */
    public static final ResourceKey<Level> BLOOD = key("blood");

    /** Upstream id 40, {@code DimensionDemiplane}. */
    public static final ResourceKey<Level> DEMIPLANE = key("demiplane");

    /** Upstream id 41, {@code UndergroundWorlds}; first registered dimension. */
    public static final ResourceKey<Level> UNDERGROUND = key("underground");

    private static ResourceKey<Level> key(String name) {
        return ResourceKey.create(Registries.DIMENSION,
                ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID, name));
    }

    private PollutionDimensions() {}
}
