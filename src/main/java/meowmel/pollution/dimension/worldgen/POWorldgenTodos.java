package meowmel.pollution.dimension.worldgen;

/**
 * World-generation port map; see docs/PORT_COMPLETION_CHECKLIST.md for validation.
 * <p>Alfheim uses WorldEngine terrain, weighted biome smoothing, source tree generators,
 * 38 flower schematics and the fixed dream tree. Underground uses the source 5x17x5
 * noise grid, 256-block bedrock seals and kimberlite below its sea level of 63.</p>
 * <p>Cave shapes and tar pools are registered by PollutionFeatures. PollutionOreVeins
 * registers 19 ore veins and four fluid deposits. StoneSphereFeature restores the
 * source stone palettes with deterministic chunk-local placement on the modern pipeline.</p>
 * <p>Blood terrain and Astral meteors remain outside the requested scope. The old
 * numeric dimension/orb naming API is replaced by modern registry identifiers.</p>
 */
public final class POWorldgenTodos {

    private POWorldgenTodos() {
    }
}
