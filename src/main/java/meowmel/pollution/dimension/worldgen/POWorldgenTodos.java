package meowmel.pollution.dimension.worldgen;

/**
 * TODO ledger for 1.12 worldgen sources that have no direct 1.20.1 equivalent.
 *
 * <p>Everything listed here was deliberately skipped while porting
 * {@code meowmel.pollution.dimension.worldgen} (31 upstream files). The parts
 * that do map to modern data-driven worldgen live in
 * {@code data/pollution/worldgen/configured_feature/} and
 * {@code data/pollution/worldgen/placed_feature/}; the pure-noise helper was
 * ported to {@link WorldEngineNoise} and the biome providers to
 * {@code POUndergroundBiomeSource}/{@code POAlfheimBiomeSource}.</p>
 *
 * <h2>TODO: chunk generators</h2>
 * <ul>
 *   <li>TODO: {@code ChunkGeneratorUndergroundWorld} - 1.12 {@code IChunkGenerator}
 *       with a hand-written noise height field, kimberlite water floor, gravel
 *       lake beds and {@code IUndergroundBiome} surface dispatch. On 1.20.1 the
 *       terrain is produced by {@code noise_settings} datapack JSON (the
 *       underground dimension already uses {@code minecraft:caves}); the
 *       kimberlite/gravel surface rules must be authored as a
 *       {@code surface_rule} in that file. Not portable as Java without
 *       reimplementing {@code ChunkGenerator}.</li>
 *   <li>TODO: {@code ChunkGeneratorAlfheim} - custom WorldEngine terrain
 *       interpolation, livingrock restoration and per-biome layers; same
 *       {@code noise_settings} story as above.</li>
 *   <li>TODO: {@code ChunkGeneratorBlood} - flesh/plasma terrain from
 *       {@code TerrainGenerator}; the blood dimension has no target datapack
 *       yet.</li>
 *   <li>TODO: {@code TerrainGenerator} - Perlin height source used only by the
 *       blood chunk generator; skipped together with it.</li>
 * </ul>
 *
 * <h2>TODO: biome surface blocks</h2>
 * <ul>
 *   <li>TODO: 1.12 biome top/filler blocks ({@code POBiomeUndergroundStyle}:
 *       mycelium/dirt, grass/dirt, sand/sandstone, netherrack/netherrack) are
 *       not expressible in a 1.20.1 biome JSON. They belong to the dimension's
 *       {@code noise_settings} as biome-conditional {@code surface_rule}s.
 *       {@code data/pollution/dimension/underground.json} currently points at
 *       {@code minecraft:caves}, so a {@code pollution:underground} noise
 *       settings file must be authored and wired by the dimension batch
 *       before the style biomes can show their surfaces.</li>
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
 *   <li>TODO: {@code WorldGenGarden} - garden carving (stone to grass, flowers,
 *       trees) is arbitrary code; only the scattered-block pieces were mapped.</li>
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
 *   <li>TODO: {@code PollutionOreVeins} - 1.12 GTCEu {@code WorldGenRegistry}
 *       vein/fluid-deposit/sphere registration. GTCEu Modern 7.5.3 exposes
 *       {@code GTOreDefinition}/{@code BedrockFluidDefinition} with
 *       {@code GTLayerPattern}/vein generators, but the target has no GT
 *       worldgen registration and most vein materials are not ported yet
 *       (FlameCoal, Dragonstone, Pyrargyrite, Scabyst, PlutoZinc,
 *       ElvenQuartz, ElvenElementium, AuthorityLead, MeltGold, DumbTin and
 *       PureTar are missing; only Octine/Syrmorite/Valonite exist). Port once
 *       the materials and GT registries land.</li>
 *   <li>TODO: {@code PODimensionManager}/{@code PODimensionType} - dimensions
 *       are datapack JSON on 1.20.1 ({@code data/pollution/dimension[_type]/}),
 *       owned by the dimension batch. All four dimension files now exist, but
 *       alfheim/blood/demiplane still use {@code minecraft:fixed} +
 *       {@code minecraft:plains} placeholders. Follow-up (dimension batch):
 *       switch them to {@code pollution:alfheim} (custom source),
 *       {@code pollution:blood} / {@code pollution:demiplane} (fixed biomes),
 *       and the underground file to {@code pollution:underground}.</li>
 * </ul>
 */
public final class POWorldgenTodos {

    private POWorldgenTodos() {
    }
}
