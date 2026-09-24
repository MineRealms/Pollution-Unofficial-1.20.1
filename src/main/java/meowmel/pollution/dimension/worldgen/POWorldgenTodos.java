package meowmel.pollution.dimension.worldgen;

/**
 * TODO ledger for 1.12 worldgen sources whose 1.20.1 implementation is still
 * approximate or has no direct equivalent.
 *
 * <p>This ledger records the remaining differences after porting
 * {@code meowmel.pollution.dimension.worldgen} (31 upstream files). The parts
 * that map to modern data-driven worldgen live in
 * {@code data/pollution/worldgen/configured_feature/} and
 * {@code data/pollution/worldgen/placed_feature/}; the pure-noise helper was
 * ported to {@link WorldEngineNoise} and the biome providers to
 * {@code POUndergroundBiomeSource}/{@code POAlfheimBiomeSource}.</p>
 *
 * <h2>TODO: chunk generators</h2>
 * <ul>
 *   <li>DONE: {@code ChunkGeneratorUndergroundWorld} - replaced by datapack
 *       JSON on 1.20.1: {@code dimension/underground.json} now uses the
 *       registered {@code pollution:underground} biome source plus a
 *       {@code pollution:underground} noise settings whose biome-conditional
 *       {@code surface_rule}s reproduce the {@code IUndergroundBiome} surface
 *       dispatch. The kimberlite water floor and gravel lake beds are not
 *       reproduced (no kimberlite block in GTCEu Modern).</li>
 *   <li>TODO: {@code ChunkGeneratorAlfheim} - custom WorldEngine terrain
 *       interpolation, livingrock restoration and per-biome layers; same
 *       {@code noise_settings} story as above.</li>
 *   <li>TODO: {@code ChunkGeneratorBlood} - flesh/plasma terrain from
 *       {@code TerrainGenerator}. The blood dimension datapack
 *       ({@code data/pollution/dimension/blood.json} +
 *       {@code noise_settings/blood.json}) now uses the ported
 *       {@code pollution:flesh_block} as the default block; the upstream
 *       {@code BloodPlasma} fluid is still not ported (water stands in).
 *       The flesh mounds still wait for this generator.</li>
 *   <li>TODO: {@code TerrainGenerator} - Perlin height source used only by the
 *       blood chunk generator; skipped together with it.</li>
 * </ul>
 *
 * <h2>TODO: biome surface blocks</h2>
 * <ul>
 *   <li>DONE: 1.12 biome top/filler blocks ({@code POBiomeUndergroundStyle}:
 *       mycelium/dirt, grass/dirt, sand/sandstone, netherrack/netherrack) are
 *       authored as biome-conditional {@code surface_rule}s in
 *       {@code noise_settings/underground.json} (dry cave floors only, via a
 *       {@code not water_above_threshold} guard, matching the upstream
 *       buildSurface scan that skipped submerged stone).</li>
 *   <li>DONE: Alfheim biome layers - {@code noise_settings/alfheim.json}
 *       reproduces the upstream WorldEngine layers: grass over dirt for the
 *       field/plateau/forest biomes, elven sand for beach/sandbank and
 *       gravel-over-clay for the river (upstream rolled clay for half of the
 *       river tops, which a surface rule cannot express). The plateau (0-2)
 *       vs non-plateau (4-6) filler depth difference is approximated by the
 *       noise-driven surface depth instead of per-biome depth rules.</li>
 * </ul>
 *
 * <h2>TODO: structures</h2>
 * <ul>
 *   <li>TODO: {@code POStructureManager}, {@code MapGenUndergroundBridge},
 *       {@code StructureUndergroundBridgePieces} - the 1.12
 *       {@code StructureComponent}/{@code MapGenStructure} fortress
 *       (15 piece classes, loot tables, spawn lists). The 1.20.1 equivalent is
 *       a jigsaw structure with {@code template_pool} JSON and one structure
 *       NBT per piece. No templates exist yet (only
 *       {@code data/pollution/structures/platform.nbt}), so re-authoring all
 *       pieces is required before a {@code worldgen/structure} JSON can be
 *       added.</li>
 *   <li>TODO: {@code MapGenCavesUnderground} - replaced by the vanilla
 *       {@code minecraft:cave}/{@code minecraft:canyon} carvers referenced from
 *       the dimension's {@code noise_settings}.</li>
 * </ul>
 *
 * <h2>TODO: 1.12 Forge terrain events</h2>
 * <ul>
 *   <li>TODO: {@code terraingen/InitMapGenEvent} and
 *       {@code terraingen/TerrainGen} - Forge 1.12 {@code TERRAIN_GEN_BUS}
 *       hooks. 1.20.1 has no equivalent biome/mapgen replacement bus; the
 *       modern extension points are {@code BiomeModifier} (Forge) or a custom
 *       {@code BiomeSource} codec (done here).</li>
 * </ul>
 *
 * <h2>TODO: features that could not be expressed as vanilla feature JSON</h2>
 * <ul>
 *   <li>TODO: {@code WorldGenSlantedPillar} - the stair-stepped 2x2/3x3 slanted
 *       quartz pillar has no vanilla feature type. A straight
 *       {@code minecraft:block_column} quartz pillar is shipped as a visual
 *       approximation ({@code pollution:quartz_pillar}).</li>
 *   <li>TODO: {@code WorldGenBigVines} - the 20-45 block leaf-core column
 *       wrapped in vines is custom shape logic; the lush cave uses the vanilla
 *       {@code minecraft:vines} feature instead.</li>
 *   <li>DONE: {@code WorldGenGarden} - ported as
 *       {@link meowmel.pollution.dimension.worldgen.feature.GardenFeature}
 *       ({@code pollution:garden}) with configured/placed feature JSON. The
 *       1.12 {@code WorldGenTrees} trees use the vanilla tree feature with the
 *       same oak/birch heights. Upstream called it only from the underground
 *       decoration pass: {@code ChunkGeneratorUndergroundWorld.populateWithVanilla},
 *       the fallback taken by the unstyled {@code POBiomeUnderground} (Deep
 *       Cave) biome, while the seven styled biomes override {@code populate}
 *       with their own decorator groups and never ran the garden. It is
 *       therefore wired as {@code pollution:underground/garden} from the
 *       {@code pollution:underground} (Deep Cave) biome, not from the Alfheim
 *       field biomes.</li>
 *   <li>TODO: {@code WorldGenFleshMound} - flesh mound is a custom shape; the
 *       blood dimension exists but its generator is still the overworld noise
 *       placeholder, so the mound has no placement hook yet.</li>
 *   <li>TODO: {@code WorldGenFluidPool} with {@code PureTar} - the target mod
 *       does not register the tar fluid/material, so the magma-cave tar pools
 *       are skipped. Water/lava pools are ported as {@code minecraft:lake}
 *       configured features ({@code pollution:water_pool},
 *       {@code minecraft:lake_lava}).</li>
 *   <li>TODO: {@code WorldGenAlfheimFixedTree} (dream tree/sad oak schema),
 *       {@code WorldGenAlfheimGiantFlower} (38 NBT schemas),
 *       {@code WorldGenAlfheimProgramTrees} and
 *       {@code WorldGenWorldEngineTree}/{@code BigTree} - WorldEngine's custom
 *       tree shapes and multi-block flower schemas cannot be represented by
 *       {@code minecraft:tree}. Alfheim forests use vanilla oak/dark-oak placed
 *       features and the flower field uses vanilla flower patches as
 *       stand-ins.</li>
 *   <li>TODO: {@code WorldGenGlowstoneCeiling} is ported approximately as a
 *       downward {@code minecraft:block_column} of glowstone (1-3 blocks) under
 *       a cave ceiling scan.</li>
 *   <li>TODO: {@code WorldGenMushroomBlockCluster} is ported approximately as
 *       {@code minecraft:block_pile} under a cave ceiling scan.</li>
 *   <li>TODO: {@code WorldGenStalactite} is ported as an upward
 *       {@code minecraft:block_column} of stone (3-10 blocks).</li>
 *   <li>TODO: {@code WorldGenOnCaveFloor}, {@code WorldGenScatteredBlock},
 *       {@code WorldGenUndergroundWater} - the cave-floor/ceiling scan logic is
 *       reproduced with the vanilla {@code environment_scan} placement
 *       modifier; the scattered blocks map to {@code random_patch}-based
 *       placed features.</li>
 * </ul>
 *
 * <h2>TODO: other registries</h2>
 * <ul>
 *   <li>DONE: {@code PollutionOreVeins} - ported to
 *       {@link PollutionOreVeins} as GTCEu 7.5.3 {@code GTOreDefinition}s with
 *       custom {@code IWorldGenLayer}s for {@code pollution:underground} and
 *       {@code pollution:alfheim} (registered through the
 *       {@code GTCEuAPI.RegisterEvent} fired by {@code GTOreLoader}) plus
 *       {@code BedrockFluidDefinition}s. Veins whose materials have no ore
 *       block in this port (cryolite, elementium, octine, syrmorite, valonite)
 *       and the {@code PureTar} deposit are skipped with comments in that
 *       class; the 1.12 stone spheres and orb/named-dimension helpers have no
 *       7.5.3 equivalent.</li>
 *   <li>DONE: {@code PODimensionManager}/{@code PODimensionType} - dimensions
 *       are datapack JSON on 1.20.1 ({@code data/pollution/dimension[_type]/}).
 *       Alfheim uses the custom {@code pollution:alfheim} biome source, blood
 *       and demiplane use fixed {@code pollution:blood} / {@code
 *       pollution:demiplane} biomes, and {@code pollution:alfheim} /
 *       {@code pollution:blood} noise settings are authored. Remaining
 *       follow-up: point the underground file at the already registered
 *       {@code pollution:underground} biome source and the
 *       {@code pollution:underground} noise settings are already authored in
 *       {@code data/pollution/dimension/underground.json} and
 *       {@code data/pollution/worldgen/noise_settings/underground.json}; the
 *       style-biome surface rules are wired there. Remaining differences are
 *       the missing 1.12 orb/named-dimension helpers and material deposits
 *       called out in {@link PollutionOreVeins}.</li>
 * </ul>
 */
public final class POWorldgenTodos {

    private POWorldgenTodos() {
    }
}
