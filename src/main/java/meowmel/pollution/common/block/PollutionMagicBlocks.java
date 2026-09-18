package meowmel.pollution.common.block;

import com.tterrag.registrate.util.entry.BlockEntry;
import meowmel.pollution.compat.gtceu.PollutionGTAddon;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Casing blocks of the magic multiblocks.
 *
 * <p>Upstream used three variant blocks ({@code magic_block}, {@code beam_core},
 * {@code glass}). Modern GregTech has no addon-facing variant-block helper, so
 * every variant is registered as its own block here. Block ids therefore differ
 * from the 1.12 {@code block[variant=...]} spelling; every other behaviour
 * (hardness 5 / resistance 10, metal or glass material, no mob spawning) is
 * kept. Textures are placeholders until the asset conversion pass.</p>
 */
public final class PollutionMagicBlocks {

    // ////////////////////////////////////
    // ***** magic_block variants *****//
    // ////////////////////////////////////

    public static final BlockEntry<Block> VOID_PRISM = casing("void_prism");
    public static final BlockEntry<Block> SPELL_PRISM = casing("spell_prism");
    public static final BlockEntry<Block> SPELL_PRISM_COLD = casing("spell_prism_cold");
    public static final BlockEntry<Block> SPELL_PRISM_HOT = casing("spell_prism_hot");
    public static final BlockEntry<Block> SPELL_PRISM_WATER = casing("spell_prism_water");
    public static final BlockEntry<Block> SPELL_PRISM_ORDER = casing("spell_prism_order");
    public static final BlockEntry<Block> SPELL_PRISM_AIR = casing("spell_prism_air");
    public static final BlockEntry<Block> SPELL_PRISM_EARTH = casing("spell_prism_earth");
    public static final BlockEntry<Block> SPELL_PRISM_VOID = casing("spell_prism_void");
    public static final BlockEntry<Block> ALLOY_BLAST_CASING = casing("alloy_blast_casing");
    public static final BlockEntry<Block> MAGIC_BATTERY_CASING = casing("magic_battery_casing");

    // ////////////////////////////////////
    // ***** beam_core variants *****//
    // ////////////////////////////////////

    public static final BlockEntry<Block> BEAM_CORE_0 = casing("beam_core_0");
    public static final BlockEntry<Block> BEAM_CORE_1 = casing("beam_core_1");
    public static final BlockEntry<Block> BEAM_CORE_2 = casing("beam_core_2");
    public static final BlockEntry<Block> BEAM_CORE_3 = casing("beam_core_3");
    public static final BlockEntry<Block> BEAM_CORE_4 = casing("beam_core_4");
    public static final BlockEntry<Block> FILTER_1 = casing("filter_1");
    public static final BlockEntry<Block> FILTER_2 = casing("filter_2");
    public static final BlockEntry<Block> FILTER_3 = casing("filter_3");
    public static final BlockEntry<Block> FILTER_4 = casing("filter_4");
    public static final BlockEntry<Block> FILTER_5 = casing("filter_5");

    // ////////////////////////////////////
    // ***** glass variants *****//
    // ////////////////////////////////////

    public static final BlockEntry<Block> LAMINATED_GLASS = glass("laminated_glass");
    public static final BlockEntry<Block> AAMINATED_GLASS = glass("aaminated_glass");
    public static final BlockEntry<Block> BAMINATED_GLASS = glass("baminated_glass");
    public static final BlockEntry<Block> CAMINATED_GLASS = glass("caminated_glass");
    public static final BlockEntry<Block> DAMINATED_GLASS = glass("daminated_glass");

    // ////////////////////////////////////
    // ***** turbine variants (upstream POTurbine) *****//
    // ////////////////////////////////////

    public static final BlockEntry<Block> BRONZE_GEARBOX = casing("bronze_gearbox");
    public static final BlockEntry<Block> STEEL_GEARBOX = casing("steel_gearbox");
    public static final BlockEntry<Block> STAINLESS_STEEL_GEARBOX = casing("stainless_steel_gearbox");
    public static final BlockEntry<Block> TITANIUM_GEARBOX = casing("titanium_gearbox");
    public static final BlockEntry<Block> TUNGSTENSTEEL_GEARBOX = casing("tungstensteel_gearbox");
    public static final BlockEntry<Block> BRONZE_PIPE = casing("bronze_pipe");
    public static final BlockEntry<Block> STEEL_PIPE = casing("steel_pipe");
    public static final BlockEntry<Block> TITANIUM_PIPE = casing("titanium_pipe");
    public static final BlockEntry<Block> TUNGSTENSTEEL_PIPE = casing("tungstensteel_pipe");
    public static final BlockEntry<Block> POLYTETRAFLUOROETHYLENE_PIPE = casing("polytetrafluoroethylene_pipe");

    // ////////////////////////////////////
    // ***** mana plate variants (upstream POManaPlate) *****//
    // ////////////////////////////////////

    public static final BlockEntry<Block> MANA_BASIC = casing("mana_basic");
    public static final BlockEntry<Block> MANA_1 = casing("mana_1");
    public static final BlockEntry<Block> MANA_2 = casing("mana_2");
    public static final BlockEntry<Block> MANA_3 = casing("mana_3");
    public static final BlockEntry<Block> MANA_4 = casing("mana_4");
    public static final BlockEntry<Block> MANA_5 = casing("mana_5");

    // ////////////////////////////////////
    // ***** bot block variants (upstream POBotBlock) *****//
    // ////////////////////////////////////

    public static final BlockEntry<Block> TERRA_WATERTIGHT_CASING = casing("terra_watertight_casing");
    public static final BlockEntry<Block> TERRA_1_CASING = casing("terra_1_casing");
    public static final BlockEntry<Block> TERRA_2_CASING = casing("terra_2_casing");
    public static final BlockEntry<Block> TERRA_3_CASING = casing("terra_3_casing");
    public static final BlockEntry<Block> TERRA_4_CASING = casing("terra_4_casing");
    public static final BlockEntry<Block> TERRA_5_CASING = casing("terra_5_casing");
    public static final BlockEntry<Block> TERRA_6_CASING = casing("terra_6_casing");

    // ////////////////////////////////////
    // ***** starstream obelisk variants (upstream POStarstreamObelisk) *****//
    // ////////////////////////////////////

    public static final BlockEntry<Block> STARSTREAM_CASING = starstream("starstream_casing", 0);
    public static final BlockEntry<Block> STARSTREAM_RUNED_CASING = starstream("starstream_runed_casing", 5);
    public static final BlockEntry<Block> CONSTELLATION_ANCHOR = starstream("constellation_anchor", 11);

    /** Registration names, used for the language keys. */
    public static final java.util.List<String> ALL_NAMES = java.util.List.of(
            "void_prism", "spell_prism", "spell_prism_cold", "spell_prism_hot", "spell_prism_water",
            "spell_prism_order", "spell_prism_air", "spell_prism_earth", "spell_prism_void",
            "alloy_blast_casing", "magic_battery_casing",
            "beam_core_0", "beam_core_1", "beam_core_2", "beam_core_3", "beam_core_4",
            "filter_1", "filter_2", "filter_3", "filter_4", "filter_5",
            "laminated_glass", "aaminated_glass", "baminated_glass", "caminated_glass", "daminated_glass",
            "bronze_gearbox", "steel_gearbox", "stainless_steel_gearbox", "titanium_gearbox",
            "tungstensteel_gearbox",
            "bronze_pipe", "steel_pipe", "titanium_pipe", "tungstensteel_pipe",
            "polytetrafluoroethylene_pipe",
            "mana_basic", "mana_1", "mana_2", "mana_3", "mana_4", "mana_5",
            "terra_watertight_casing", "terra_1_casing", "terra_2_casing", "terra_3_casing",
            "terra_4_casing", "terra_5_casing", "terra_6_casing",
            "starstream_casing", "starstream_runed_casing", "constellation_anchor");

    /** "spell_prism_earth" -&gt; "Spell Prism Earth". */
    public static String displayName(String name) {
        StringBuilder display = new StringBuilder();
        for (String part : name.split("_")) {
            if (display.length() > 0) {
                display.append(' ');
            }
            display.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return display.toString();
    }

    private static BlockEntry<Block> casing(String name) {
        return PollutionGTAddon.REGISTRATE
                .block(name, Block::new)
                .properties(properties -> properties
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 10.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops())
                .simpleItem()
                .register();
    }

    private static BlockEntry<Block> glass(String name) {
        return PollutionGTAddon.REGISTRATE
                .block(name, Block::new)
                .properties(properties -> properties
                        .mapColor(MapColor.NONE)
                        .strength(5.0F, 5.0F)
                        .sound(SoundType.GLASS)
                        .noOcclusion())
                .simpleItem()
                .register();
    }

    /**
     * Starstream obelisk structural blocks. Upstream these were variants of a
     * single block whose {@code obelisk_core} variant carried a tile entity;
     * the core (needs the constellation network) is not ported, so the three
     * structural casings are registered as plain blocks with the upstream
     * hardness/resistance/sound/light values.
     */
    private static BlockEntry<Block> starstream(String name, int lightLevel) {
        return PollutionGTAddon.REGISTRATE
                .block(name, Block::new)
                .properties(properties -> properties
                        .mapColor(MapColor.STONE)
                        .strength(12.0F, 80.0F)
                        .sound(SoundType.STONE)
                        .lightLevel(state -> lightLevel)
                        .requiresCorrectToolForDrops())
                .simpleItem()
                .register();
    }

    /** Forces class initialisation from the mod constructor (own mod bus). */
    public static void init() {}

    private PollutionMagicBlocks() {}
}
