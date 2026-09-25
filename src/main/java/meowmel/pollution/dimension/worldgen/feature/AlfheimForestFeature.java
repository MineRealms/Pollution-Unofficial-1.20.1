package meowmel.pollution.dimension.worldgen.feature;

import meowmel.pollution.common.block.PollutionPlantBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import vazkii.botania.common.block.BotaniaBlocks;

/** WorldEngine's four forest entries, plus grape-bearing island oaks. */
public final class AlfheimForestFeature extends Feature<NoneFeatureConfiguration> {
    public enum Forest { ISLAND, FOREST, PLATEAU }
    private final Forest forest;

    public AlfheimForestFeature(Forest forest) {
        super(NoneFeatureConfiguration.CODEC);
        this.forest = forest;
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        boolean placed = false;
        var random = context.random();
        if (forest == Forest.ISLAND && random.nextInt(2) == 0) {
            placed = new WorldEngineTree(Blocks.OAK_LOG.defaultBlockState(), Blocks.OAK_LEAVES.defaultBlockState(),
                    PollutionPlantBlocks.ALFHEIM_RED_GRAPE_0.get(), 6, true, false)
                    .generate(context.level(), random, treeOrigin(context));
        }
        for (int entry = 0; entry < 4; entry++) {
            if (random.nextInt(forest == Forest.PLATEAU ? 32 : 2) != 0) continue;
            for (int attempt = 0; attempt < (forest == Forest.PLATEAU ? 3 : 1); attempt++) {
                boolean dark = entry >= 2;
                Block wood = switch (random.nextInt(3)) {
                    case 1 -> BotaniaBlocks.livingwood;
                    case 2 -> BotaniaBlocks.dreamwood;
                    default -> dark ? Blocks.DARK_OAK_LOG : Blocks.OAK_LOG;
                };
                BlockState leaves = (dark ? Blocks.DARK_OAK_LEAVES : Blocks.OAK_LEAVES).defaultBlockState();
                int chance = forest == Forest.ISLAND && entry % 2 == 1 ? 2 : 1;
                WorldEngineTreeBase tree = random.nextInt(chance) == 0
                        ? new WorldEngineBigTree(wood.defaultBlockState(), leaves, entry % 2 + 1,
                                12, 4, 0.618, 0.381, 1, 1)
                        : new WorldEngineTree(wood.defaultBlockState(), leaves, null, 4, false, false);
                placed |= tree.generate(context.level(), random, treeOrigin(context));
            }
        }
        return placed;
    }

    private static BlockPos treeOrigin(FeaturePlaceContext<?> context) {
        BlockPos column = context.origin().offset(context.random().nextInt(16), 0, context.random().nextInt(16));
        return context.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, column);
    }
}
