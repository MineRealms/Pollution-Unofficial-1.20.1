package meowmel.pollution.dimension.worldgen.feature;

import com.gregtechceu.gtceu.common.data.GTBlocks;
import meowmel.pollution.common.block.PollutionStoneBlocks;
import meowmel.pollution.dimension.worldgen.WorldEngineNoise;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import vazkii.botania.common.block.BotaniaBlocks;
import java.util.ArrayList;

/** Chunk-local slices of deterministic 10-20 radius deposits; adjacent chunks agree on the same sphere. */
public final class StoneSphereFeature extends Feature<NoneFeatureConfiguration> {
    public StoneSphereFeature() { super(NoneFeatureConfiguration.CODEC); }
    @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        var level = context.level();
        String dimension = level.getLevel().dimension().location().toString();
        boolean custom = dimension.equals("pollution:alfheim") || dimension.equals("pollution:underground");
        if (!custom && !dimension.equals("minecraft:overworld")) return false;
        var palette = new ArrayList<BlockState>();
        for (String stone : new String[]{"limestone", "komatiite", "green_schist", "blue_schist", "quartzite", "slate", "shale"}) {
            palette.add(PollutionStoneBlocks.STONES.get(stone).getDefaultState());
        }
        if (custom) {
            palette.add(PollutionStoneBlocks.STONES.get("black_granite").getDefaultState());
            palette.add(GTBlocks.RED_GRANITE.getDefaultState());
            palette.add(GTBlocks.MARBLE.getDefaultState());
            palette.add(Blocks.SMOOTH_BASALT.defaultBlockState());
        }
        ChunkPos chunk = new ChunkPos(context.origin());
        int gridX = Math.floorDiv(chunk.x, 3), gridZ = Math.floorDiv(chunk.z, 3);
        var pos = new BlockPos.MutableBlockPos();
        boolean changed = false;
        for (int gx = gridX - 1; gx <= gridX + 1; gx++) for (int gz = gridZ - 1; gz <= gridZ + 1; gz++) {
            RandomSource random = RandomSource.create(WorldEngineNoise.mixWorldSeed(level.getSeed() ^ ChunkPos.asLong(gx, gz) ^ 0x5710AEL));
            int cx = gx * 48 + random.nextInt(48), cz = gz * 48 + random.nextInt(48);
            int radius = 10 + random.nextInt(11), cy = 10 + random.nextInt(custom && dimension.endsWith("alfheim") ? 130 : 230);
            BlockState stone = palette.get(random.nextInt(palette.size()));
            for (int x = Math.max(chunk.getMinBlockX(), cx - radius); x <= Math.min(chunk.getMaxBlockX(), cx + radius); x++) {
                for (int z = Math.max(chunk.getMinBlockZ(), cz - radius); z <= Math.min(chunk.getMaxBlockZ(), cz + radius); z++) {
                    for (int y = Math.max(10, cy - radius); y <= Math.min(level.getMaxBuildHeight() - 1, cy + radius); y++) {
                        if ((x - cx) * (x - cx) + (y - cy) * (y - cy) + (z - cz) * (z - cz) > radius * radius) continue;
                        pos.set(x, y, z);
                        var old = level.getBlockState(pos);
                        if (!old.hasBlockEntity() && (old.is(BlockTags.STONE_ORE_REPLACEABLES)
                                || old.is(BlockTags.DEEPSLATE_ORE_REPLACEABLES) || old.is(BotaniaBlocks.livingrock))) {
                            level.setBlock(pos, stone, 2);
                            changed = true;
                        }
                    }
                }
            }
        }
        return changed;
    }
}
