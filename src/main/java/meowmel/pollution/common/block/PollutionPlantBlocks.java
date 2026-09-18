package meowmel.pollution.common.block;

import com.tterrag.registrate.util.entry.BlockEntry;
import meowmel.pollution.common.block.plant.alfheim.AlfheimDreamLeavesBlock;
import meowmel.pollution.common.block.plant.alfheim.AlfheimElvenSandBlock;
import meowmel.pollution.common.block.plant.alfheim.AlfheimRedGrapeBlock;
import meowmel.pollution.common.block.plant.alfheim.AlfheimWhiteGrapeBlock;
import meowmel.pollution.common.block.plant.flesh.EldritchEyeBlock;
import meowmel.pollution.common.block.plant.flesh.FleshFlowerBlock;
import meowmel.pollution.common.block.plant.flesh.FleshLeavesBlock;
import meowmel.pollution.common.block.plant.flesh.FleshPlantBlock;
import meowmel.pollution.common.block.plant.flesh.FleshSaplingBlock;
import meowmel.pollution.common.block.plant.flesh.HeartFruitBlock;
import meowmel.pollution.common.block.plant.flesh.TentacleBlock;
import meowmel.pollution.common.block.plant.rainbow.RainbowLeavesBlock;
import meowmel.pollution.common.block.plant.rainbow.RainbowSaplingBlock;
import meowmel.pollution.compat.gtceu.PollutionGTAddon;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

import java.util.List;

/**
 * Plant blocks of the Pollution port: the flesh tree family, the magical
 * rainbow tree and the Alfheim white grape.
 *
 * <p>Upstream registered these through the 1.12 registry events; here they are
 * registrate blocks on the shared Pollution GT registrate, like the magic
 * casings. Registry ids follow upstream where possible; growth that needs
 * unported worldgen / tile entities is stubbed (see the individual classes).</p>
 */
public final class PollutionPlantBlocks {

    // ////////////////////////////////////
    // ***** flesh tree family *****//
    // ////////////////////////////////////

    public static final BlockEntry<FleshPlantBlock> FLESH_PLANT = PollutionGTAddon.REGISTRATE
            .block("flesh_plant", FleshPlantBlock::new)
            .properties(properties -> properties
                    .mapColor(MapColor.COLOR_RED)
                    .strength(0.6F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
                    .randomTicks())
            .simpleItem()
            .register();

    public static final BlockEntry<FleshFlowerBlock> FLESH_FLOWER = PollutionGTAddon.REGISTRATE
            .block("flesh_flower", FleshFlowerBlock::new)
            .properties(properties -> properties
                    .mapColor(MapColor.COLOR_RED)
                    .strength(0.6F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
                    .randomTicks())
            .simpleItem()
            .register();

    public static final BlockEntry<FleshLeavesBlock> FLESH_LEAVES = PollutionGTAddon.REGISTRATE
            .block("flesh_leaves", FleshLeavesBlock::new)
            .properties(properties -> properties
                    .mapColor(MapColor.COLOR_RED)
                    .strength(0.3F)
                    .sound(SoundType.SLIME_BLOCK)
                    .noOcclusion()
                    .randomTicks())
            .simpleItem()
            .register();

    public static final BlockEntry<FleshSaplingBlock> FLESH_SAPLING = PollutionGTAddon.REGISTRATE
            .block("flesh_sapling", FleshSaplingBlock::new)
            .properties(properties -> properties
                    .mapColor(MapColor.COLOR_RED)
                    .instabreak()
                    .sound(SoundType.SLIME_BLOCK)
                    .noOcclusion()
                    .noCollission()
                    .randomTicks())
            .simpleItem()
            .register();

    public static final BlockEntry<HeartFruitBlock> HEART_FRUIT = PollutionGTAddon.REGISTRATE
            .block("heart_fruit", HeartFruitBlock::new)
            .properties(properties -> properties
                    .mapColor(MapColor.COLOR_RED)
                    .strength(0.2F)
                    .sound(SoundType.SLIME_BLOCK)
                    .noOcclusion()
                    .noCollission()
                    .randomTicks())
            .simpleItem()
            .register();

    public static final BlockEntry<EldritchEyeBlock> ELDRITCH_EYE = PollutionGTAddon.REGISTRATE
            .block("eldritch_eye", EldritchEyeBlock::new)
            .properties(properties -> properties
                    .mapColor(MapColor.COLOR_RED)
                    .strength(0.5F)
                    .sound(SoundType.SLIME_BLOCK)
                    .noOcclusion()
                    .lightLevel(state -> 6)
                    .randomTicks())
            .simpleItem()
            .register();

    public static final BlockEntry<TentacleBlock> TENTACLE = PollutionGTAddon.REGISTRATE
            .block("tentacle", TentacleBlock::new)
            .properties(properties -> properties
                    .mapColor(MapColor.COLOR_RED)
                    .strength(1.0F)
                    .sound(SoundType.SLIME_BLOCK)
                    .noOcclusion()
                    .noCollission())
            .simpleItem()
            .register();

    // ////////////////////////////////////
    // ***** magical rainbow tree *****//
    // ////////////////////////////////////

    public static final BlockEntry<RainbowLeavesBlock> RAINBOW_LEAVES = PollutionGTAddon.REGISTRATE
            .block("rainbow_leaves", RainbowLeavesBlock::new)
            .properties(properties -> properties
                    .mapColor(MapColor.PLANT)
                    .strength(0.2F)
                    .sound(SoundType.GRASS)
                    .noOcclusion()
                    .randomTicks())
            .simpleItem()
            .register();

    public static final BlockEntry<RainbowSaplingBlock> RAINBOW_SAPLING = PollutionGTAddon.REGISTRATE
            .block("rainbow_sapling", RainbowSaplingBlock::new)
            .properties(properties -> properties
                    .mapColor(MapColor.PLANT)
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .noOcclusion()
                    .noCollission()
                    .randomTicks())
            .simpleItem()
            .register();

    // ////////////////////////////////////
    // ***** alfheim *****//
    // ////////////////////////////////////

    public static final BlockEntry<AlfheimWhiteGrapeBlock> ALFHEIM_WHITE_GRAPE = PollutionGTAddon.REGISTRATE
            .block("alfheim_white_grape", AlfheimWhiteGrapeBlock::new)
            .properties(properties -> properties
                    .mapColor(MapColor.PLANT)
                    .strength(0.2F)
                    .sound(SoundType.CROP)
                    .noOcclusion()
                    .noCollission()
                    .randomTicks())
            .simpleItem()
            .register();

    public static final BlockEntry<AlfheimElvenSandBlock> ALFHEIM_ELVEN_SAND = PollutionGTAddon.REGISTRATE
            .block("alfheim_elven_sand", AlfheimElvenSandBlock::new)
            .properties(properties -> properties
                    .mapColor(MapColor.SAND)
                    .strength(0.5F)
                    .sound(SoundType.SAND))
            .simpleItem()
            .register();

    public static final BlockEntry<AlfheimDreamLeavesBlock> ALFHEIM_DREAM_LEAVES = PollutionGTAddon.REGISTRATE
            .block("alfheim_dream_leaves", AlfheimDreamLeavesBlock::new)
            .properties(properties -> properties
                    .mapColor(MapColor.PLANT)
                    .strength(0.2F)
                    .sound(SoundType.GRASS)
                    .noOcclusion()
                    .randomTicks())
            .simpleItem()
            .register();

    public static final BlockEntry<AlfheimRedGrapeBlock> ALFHEIM_RED_GRAPE_0 = redGrape("alfheim_red_grape_0", 0);
    public static final BlockEntry<AlfheimRedGrapeBlock> ALFHEIM_RED_GRAPE_1 = redGrape("alfheim_red_grape_1", 1);
    public static final BlockEntry<AlfheimRedGrapeBlock> ALFHEIM_RED_GRAPE_2 = redGrape("alfheim_red_grape_2", 2);

    private static BlockEntry<AlfheimRedGrapeBlock> redGrape(String name, int stage) {
        return PollutionGTAddon.REGISTRATE
                .block(name, properties -> new AlfheimRedGrapeBlock(properties, stage))
                .properties(properties -> properties
                        .mapColor(MapColor.PLANT)
                        .strength(0.2F)
                        .sound(SoundType.VINE)
                        .noOcclusion()
                        .noCollission()
                        .randomTicks())
                .simpleItem()
                .register();
    }

    /** Registration names, used for the language keys. */
    public static final List<String> ALL_NAMES = List.of(
            "flesh_plant", "flesh_flower", "flesh_leaves", "flesh_sapling",
            "heart_fruit", "eldritch_eye", "tentacle",
            "rainbow_leaves", "rainbow_sapling",
            "alfheim_white_grape", "alfheim_elven_sand", "alfheim_dream_leaves",
            "alfheim_red_grape_0", "alfheim_red_grape_1", "alfheim_red_grape_2");

    /** "flesh_plant" -&gt; "Flesh Plant". */
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

    /** Forces class initialisation from the mod constructor (own mod bus). */
    public static void init() {}

    private PollutionPlantBlocks() {}
}
