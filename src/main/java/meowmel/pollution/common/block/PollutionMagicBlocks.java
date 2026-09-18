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

    /** Registration names, used for the language keys. */
    public static final java.util.List<String> ALL_NAMES = java.util.List.of(
            "void_prism", "spell_prism", "spell_prism_cold", "spell_prism_hot", "spell_prism_water",
            "spell_prism_order", "spell_prism_air", "spell_prism_earth", "spell_prism_void",
            "alloy_blast_casing", "magic_battery_casing",
            "beam_core_0", "beam_core_1", "beam_core_2", "beam_core_3", "beam_core_4",
            "filter_1", "filter_2", "filter_3", "filter_4", "filter_5",
            "laminated_glass", "aaminated_glass", "baminated_glass", "caminated_glass", "daminated_glass");

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

    /** Forces class initialisation from the mod constructor (own mod bus). */
    public static void init() {}

    private PollutionMagicBlocks() {}
}
