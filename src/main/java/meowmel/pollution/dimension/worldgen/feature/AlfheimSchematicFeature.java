package meowmel.pollution.dimension.worldgen.feature;

import com.google.gson.JsonParser;
import meowmel.pollution.common.block.PollutionPlantBlocks;
import meowmel.pollution.dimension.biome.POBiomes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import vazkii.botania.common.block.BotaniaBlocks;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Uses the unchanged 38 flower schematics and 473-block tree schematic from 1.12. */
public final class AlfheimSchematicFeature extends Feature<NoneFeatureConfiguration> {
    public enum Shape { GIANT_FLOWER, DREAM_TREE, SAD_OAK }
    private static final int[] VARIANTS = {2, 2, 2, 2, 2, 2, 3, 2, 2, 2, 2, 3, 3, 2, 2, 2, 3};
    private final Shape shape;

    public AlfheimSchematicFeature(Shape shape) {
        super(NoneFeatureConfiguration.CODEC);
        this.shape = shape;
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        if (!level.getBlockState(origin.below()).is(BlockTags.DIRT)) return false;
        if (shape == Shape.GIANT_FLOWER) {
            // Keep the entire landmark inside its biome, matching WorldGenMutatedFlowers.
            for (int x : new int[] {-24, 24}) for (int z : new int[] {-24, 24}) {
                if (!level.getBiome(origin.offset(x, 0, z)).is(POBiomes.ALFHEIM_GIANT_FLOWER_FIELD)) return false;
            }
            int color = context.random().nextInt(VARIANTS.length);
            int variant = context.random().nextInt(VARIANTS[color]);
            if (!placeCells(level, origin, Schematics.FLOWERS.get(color).get(variant), false)) return false;
            BlockState flower = BotaniaBlocks.getFlower(DyeColor.byId(color < 16 ? color : context.random().nextInt(16)))
                    .defaultBlockState();
            int count = 9 + context.random().nextInt(9);
            for (int i = 0; i < count; i++) {
                BlockPos target = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                        origin.offset(context.random().nextInt(9) - 4, 0, context.random().nextInt(9) - 4));
                if (level.ensureCanWrite(target) && level.isEmptyBlock(target) && flower.canSurvive(level, target)) {
                    level.setBlock(target, flower, 2);
                }
            }
            return true;
        }
        return placeCells(level, origin, shape == Shape.DREAM_TREE ? Schematics.DREAM_TREE : Schematics.SAD_OAK, true);
    }

    private static boolean placeCells(WorldGenLevel level, BlockPos origin, List<Cell> cells, boolean tree) {
        for (Cell cell : cells) {
            BlockPos pos = origin.offset(cell.offset());
            if (level.isOutsideBuildHeight(pos) || !level.ensureCanWrite(pos)) return false;
            if (!tree && !replaceable(level, pos, cell.state().is(BlockTags.LEAVES))) return false;
        }
        boolean placed = false;
        for (Cell cell : cells) {
            BlockPos pos = origin.offset(cell.offset());
            if (replaceable(level, pos, cell.state().is(BlockTags.LEAVES))) {
                level.setBlock(pos, cell.state(), 2);
                placed = true;
            }
        }
        return placed;
    }

    private static boolean replaceable(WorldGenLevel level, BlockPos pos, boolean leaves) {
        BlockState at = level.getBlockState(pos);
        if (at.hasBlockEntity() || at.getDestroySpeed(level, pos) < 0) return false;
        return at.isAir() || at.canBeReplaced() || at.is(BlockTags.LEAVES)
                || !leaves && (at.is(BlockTags.DIRT) || at.is(BlockTags.LOGS));
    }

    private record Cell(BlockPos offset, BlockState state) {}

    /** Lazy initialization keeps registry-dependent palettes out of feature registration. */
    private static final class Schematics {
        static final List<List<List<Cell>>> FLOWERS = loadFlowers();
        static final List<Cell> DREAM_TREE = loadTree(true);
        static final List<Cell> SAD_OAK = loadTree(false);

        private static BufferedReader read(String path) {
            var stream = AlfheimSchematicFeature.class.getResourceAsStream("/assets/pollution/alfheim/" + path);
            if (stream == null) throw new IllegalStateException("Missing Alfheim schematic " + path);
            return new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        }

        private static List<List<List<Cell>>> loadFlowers() {
            List<List<List<Cell>>> colors = new ArrayList<>();
            for (int color = 0; color < VARIANTS.length; color++) {
                List<List<Cell>> variants = new ArrayList<>();
                for (int variant = 0; variant < VARIANTS[color]; variant++) {
                    List<Cell> cells = new ArrayList<>();
                    try (var reader = read("flowers/" + color + "-" + variant + ".json")) {
                        for (var group : JsonParser.parseReader(reader).getAsJsonArray()) {
                            var entry = group.getAsJsonObject();
                            boolean cellular = entry.get("block").getAsString().equalsIgnoreCase("Botania:cellBlock");
                            for (var element : entry.getAsJsonArray("location")) {
                                var location = element.getAsJsonObject();
                                int meta = location.has("meta") ? location.get("meta").getAsInt() : 0;
                                // The upstream rainbow schema also resolves through the petal metadata palette.
                                BlockState state = (cellular ? BotaniaBlocks.cellBlock
                                        : BotaniaBlocks.getPetalBlock(DyeColor.byId(meta & 15))).defaultBlockState();
                                cells.add(new Cell(new BlockPos(value(location, "x"), value(location, "y"),
                                        value(location, "z")), state));
                                // All source cell NBT is generation=0, ticked=false: the modern BE defaults.
                            }
                        }
                    } catch (java.io.IOException exception) {
                        throw new IllegalStateException("Unable to read Alfheim flower schematic", exception);
                    }
                    variants.add(List.copyOf(cells));
                }
                colors.add(List.copyOf(variants));
            }
            return List.copyOf(colors);
        }

        private static int value(com.google.gson.JsonObject object, String key) {
            return object.has(key) ? object.get(key).getAsInt() : 0;
        }

        private static List<Cell> loadTree(boolean dream) {
            List<Cell> cells = new ArrayList<>();
            BlockState wood = (dream ? BotaniaBlocks.dreamwood : Blocks.OAK_LOG).defaultBlockState();
            BlockState leaves = dream ? PollutionPlantBlocks.ALFHEIM_DREAM_LEAVES.getDefaultState()
                    : Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.DISTANCE, 1);
            try (var reader = read("dream_tree.csv")) {
                for (String line; (line = reader.readLine()) != null;) {
                    String[] parts = line.split(",");
                    BlockState state = parts[3].equals("L") ? leaves : wood;
                    if (state.hasProperty(RotatedPillarBlock.AXIS)) {
                        state = state.setValue(RotatedPillarBlock.AXIS, Direction.Axis.valueOf(parts[3]));
                    }
                    cells.add(new Cell(new BlockPos(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]),
                            Integer.parseInt(parts[2])), state));
                }
            } catch (java.io.IOException exception) {
                throw new IllegalStateException("Unable to read Alfheim tree schematic", exception);
            }
            return List.copyOf(cells);
        }
    }
}
