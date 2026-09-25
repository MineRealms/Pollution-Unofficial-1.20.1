package meowmel.pollution.gametest;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import meowmel.pollution.Pollution;
import meowmel.pollution.dimension.biome.WorldSeededBiomeSource;
import meowmel.pollution.dimension.worldgen.feature.AlfheimSchematicFeature;
import meowmel.pollution.dimension.worldgen.feature.CaveShapeFeature;
import meowmel.pollution.dimension.worldgen.feature.WorldEngineBigTree;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import vazkii.botania.common.block.BotaniaBlocks;

@GameTestHolder(Pollution.MOD_ID)
@PrefixGameTestTemplate(false)
public final class WorldgenGameTests {
    private WorldgenGameTests() {}

    @GameTest(template = "platform", batch = "terrain", timeoutTicks = 600)
    public static void undergroundHasSealedCavesAndKimberliteFloor(GameTestHelper helper) {
        var level = helper.getLevel().getServer().getLevel(meowmel.pollution.dimension.PollutionDimensions.UNDERGROUND);
        helper.assertTrue(level != null, "underground dimension is missing");
        helper.assertTrue(level.getChunkSource().getGenerator() instanceof meowmel.pollution.dimension.worldgen.UndergroundChunkGenerator,
                "underground still uses overworld terrain");
        var chunk = level.getChunk(65, 79);
        BlockPos base = chunk.getPos().getWorldPosition();
        int air = 0, stone = 0, kimberlite = 0;
        for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) {
            for (int y : new int[] {0, 1, 254, 255}) {
                helper.assertTrue(chunk.getBlockState(base.offset(x, y, z)).is(Blocks.BEDROCK), "underground bedrock seal has a hole");
            }
            for (int y = 2; y < 254; y++) {
                var state = chunk.getBlockState(base.offset(x, y, z));
                if (state.isAir()) air++;
                if (state.is(Blocks.STONE)) stone++;
                if (state.is(meowmel.pollution.common.block.PollutionStoneBlocks.STONES.get("kimberlite").get())) kimberlite++;
            }
        }
        helper.assertTrue(air > 1000 && stone > 1000, "underground has no substantial caves or walls");
        helper.assertTrue(kimberlite > 100, "source kimberlite basement is missing");
        helper.succeed();
    }

    @GameTest(template = "platform", batch = "terrain", timeoutTicks = 600)
    public static void alfheimGeneratesWorldEngineTerrain(GameTestHelper helper) {
        var level = helper.getLevel().getServer().getLevel(meowmel.pollution.dimension.PollutionDimensions.ALFHEIM);
        helper.assertTrue(level != null, "Alfheim dimension is missing");
        helper.assertTrue(level.getChunkSource().getGenerator() instanceof meowmel.pollution.dimension.worldgen.AlfheimChunkGenerator,
                "Alfheim still uses the vanilla overworld generator");
        var generator = (meowmel.pollution.dimension.worldgen.AlfheimChunkGenerator) level.getChunkSource().getGenerator();
        var chunk = level.getChunk(61, 73);
        BlockPos base = chunk.getPos().getWorldPosition();
        helper.assertTrue(chunk.getBlockState(base).is(Blocks.BEDROCK), "WorldEngine floor is missing");
        int expected = generator.terrainHeight(base.getX() + 8, base.getZ() + 8);
        helper.assertTrue(expected > 10 && expected < 200, "invalid biome height");
        helper.assertTrue(!chunk.getBlockState(base.offset(8, expected - 8, 8)).isAir(), "terrain did not reach its biome profile");
        helper.assertTrue(chunk.getBlockState(base.offset(8, 250, 8)).isAir(), "unexpected vanilla mountains above WorldEngine terrain");
        helper.succeed();
    }

    @GameTest(template = "machine_lab", batch = "worldgen")
    public static void tarPoolUsesRegisteredFluidAndProtectsContainers(GameTestHelper helper) {
        BlockPos origin = helper.absolutePos(new BlockPos(16, 10, 16));
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-8, -4, -8), origin.offset(7, 3, 7))) {
            helper.getLevel().setBlockAndUpdate(pos, Blocks.NETHERRACK.defaultBlockState());
        }
        var pool = new meowmel.pollution.dimension.worldgen.feature.TarPoolFeature();
        boolean placed = pool.place(NoneFeatureConfiguration.INSTANCE, helper.getLevel(),
                helper.getLevel().getChunkSource().getGenerator(), RandomSource.create(43), origin);
        helper.assertTrue(placed, "sealed tar pool was rejected");
        long cells = BlockPos.betweenClosedStream(origin.offset(-8, -4, -8), origin.offset(7, 3, 7))
                .filter(pos -> helper.getLevel().getFluidState(pos).getType() == meowmel.pollution.api.unification.PollutionMaterials.PureTar.getFluid()).count();
        helper.assertTrue(cells > 10, "pool contains no usable tar fluid");
        BlockPos chest = origin.offset(-8, -4, -8);
        helper.getLevel().setBlockAndUpdate(chest, Blocks.CHEST.defaultBlockState());
        helper.assertTrue(!pool.place(NoneFeatureConfiguration.INSTANCE, helper.getLevel(),
                helper.getLevel().getChunkSource().getGenerator(), RandomSource.create(43), origin), "pool overwrote an inventory");
        helper.succeed();
    }

    @GameTest(template = "platform", batch = "worldgen")
    public static void worldSeedChangesBothBiomeMapsAndLegacyMapsRemainStable(GameTestHelper helper) {
        var ops = RegistryOps.create(JsonOps.INSTANCE, helper.getLevel().registryAccess());
        for (String dimension : new String[] {"alfheim", "underground"}) {
            var json = JsonParser.parseString("{\"type\":\"pollution:" + dimension
                    + "\",\"seed\":0,\"use_world_seed\":true}").getAsJsonObject();
            BiomeSource source = BiomeSource.CODEC.parse(ops, json).getOrThrow(false, helper::fail);
            var seeded = (WorldSeededBiomeSource) source;
            seeded.bindWorldSeed(123456789012345L);
            var first = new java.util.ArrayList<>();
            for (int x = -10; x <= 10; x++) first.add(source.getNoiseBiome(x * 511, 16, 137, null));
            seeded.bindWorldSeed(-987654321012345L);
            boolean different = false;
            for (int x = -10; x <= 10; x++) {
                different |= !first.get(x + 10).equals(source.getNoiseBiome(x * 511, 16, 137, null));
            }
            helper.assertTrue(different, dimension + " uses the same biome map for different seeds");
            seeded.bindWorldSeed(123456789012345L);
            for (int x = -10; x <= 10; x++) {
                helper.assertTrue(first.get(x + 10).equals(source.getNoiseBiome(x * 511, 16, 137, null)),
                        "biome map did not reproduce after reseeding");
            }
            json.remove("use_world_seed");
            BiomeSource legacy = BiomeSource.CODEC.parse(ops, json).getOrThrow(false, helper::fail);
            var before = legacy.getNoiseBiome(0, 16, 0, null);
            ((WorldSeededBiomeSource) legacy).bindWorldSeed(88888L);
            helper.assertTrue(before.equals(legacy.getNoiseBiome(0, 16, 0, null)),
                    "loading an old serialized biome source changed its seed");
        }
        helper.succeed();
    }

    private static BlockPos soil(GameTestHelper helper) {
        BlockPos origin = helper.absolutePos(new BlockPos(16, 5, 16));
        for (int x = -12; x <= 12; x++) for (int z = -12; z <= 12; z++) {
            helper.getLevel().setBlockAndUpdate(origin.offset(x, -1, z), Blocks.DIRT.defaultBlockState());
        }
        return origin;
    }

    @GameTest(template = "machine_lab", batch = "worldgen")
    public static void dreamTreeUsesItsCompleteSchematic(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos origin = soil(helper);
        var feature = new AlfheimSchematicFeature(AlfheimSchematicFeature.Shape.DREAM_TREE);
        helper.assertTrue(feature.place(NoneFeatureConfiguration.INSTANCE, level, level.getChunkSource().getGenerator(), RandomSource.create(5),
                origin), "dream tree failed to generate");
        int wood = 0;
        int leaves = 0;
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-8, -3, -8), origin.offset(8, 22, 8))) {
            var state = level.getBlockState(pos);
            if (state.is(BotaniaBlocks.dreamwood)) wood++;
            if (state.is(meowmel.pollution.common.block.PollutionPlantBlocks.ALFHEIM_DREAM_LEAVES.get())) leaves++;
        }
        // The upstream file has 473 placements and one duplicated coordinate.
        helper.assertTrue(wood + leaves == 472, "fixed tree lost schematic cells: " + (wood + leaves));
        helper.assertTrue(wood > 0 && leaves > 0, "dream tree palette is incomplete");
        helper.succeed();
    }

    @GameTest(template = "machine_lab", batch = "worldgen")
    public static void quartzPillarSlantsAndDoesNotPartiallyOverwrite(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos origin = soil(helper);
        var feature = new CaveShapeFeature(CaveShapeFeature.Shape.QUARTZ_PILLAR);
        helper.assertTrue(feature.place(NoneFeatureConfiguration.INSTANCE, level, level.getChunkSource().getGenerator(), RandomSource.create(4),
                origin), "slanted pillar failed to generate");
        boolean displacedTop = false;
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-8, 7, -8), origin.offset(8, 16, 8))) {
            if (level.getBlockState(pos).is(Blocks.QUARTZ_BLOCK)
                    && (Math.abs(pos.getX() - origin.getX()) >= 3 || Math.abs(pos.getZ() - origin.getZ()) >= 3)) {
                displacedTop = true;
            }
        }
        helper.assertTrue(displacedTop, "pillar became a straight column");
        helper.assertTrue(!feature.place(NoneFeatureConfiguration.INSTANCE, level, level.getChunkSource().getGenerator(), RandomSource.create(4),
                origin), "pillar ignored an obstructed footprint");
        helper.succeed();
    }

    @GameTest(template = "machine_lab", batch = "worldgen")
    public static void worldEngineBigTreeBuildsBranchedCanopy(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos origin = soil(helper);
        var tree = new WorldEngineBigTree(Blocks.OAK_LOG.defaultBlockState(), Blocks.OAK_LEAVES.defaultBlockState(),
                2, 12, 4, 0.618, 0.381, 1, 1);
        helper.assertTrue(tree.generate(level, RandomSource.create(12), origin), "big tree failed to generate");
        helper.assertTrue(level.getBlockState(origin.east().south()).is(Blocks.OAK_LOG), "2x2 trunk was lost");
        int leaves = 0;
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-10, 0, -10), origin.offset(10, 25, 10))) {
            if (level.getBlockState(pos).is(Blocks.OAK_LEAVES)) leaves++;
        }
        helper.assertTrue(leaves > 100, "WorldEngine branched canopy is missing");
        helper.succeed();
    }
}
