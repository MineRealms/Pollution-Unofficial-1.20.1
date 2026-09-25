package meowmel.pollution.common.block;

import com.tterrag.registrate.util.entry.BlockEntry;
import meowmel.pollution.compat.gtceu.PollutionGTAddon;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import java.util.LinkedHashMap;
import java.util.Map;

/** Standalone names for source stone deposits whose GTQT blocks do not exist on 1.20.1. */
public final class PollutionStoneBlocks {
    public static final Map<String, BlockEntry<Block>> STONES = new LinkedHashMap<>();
    static {
        for (String name : new String[]{"black_granite", "limestone", "komatiite", "green_schist", "blue_schist", "quartzite", "slate", "shale", "kimberlite"}) {
            STONES.put(name, PollutionGTAddon.REGISTRATE.block(name, Block::new)
                    .properties(p -> p.strength(1.5f, 6).sound(SoundType.STONE).requiresCorrectToolForDrops())
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.STONE_ORE_REPLACEABLES, BlockTags.OVERWORLD_CARVER_REPLACEABLES)
                    .simpleItem().register());
        }
    }
    public static void init() {}
    private PollutionStoneBlocks() {}
}
