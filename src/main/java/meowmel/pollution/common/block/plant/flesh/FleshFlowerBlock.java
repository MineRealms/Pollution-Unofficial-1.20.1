package meowmel.pollution.common.block.plant.flesh;

import dev.tc4port.thaumcraft.registry.TCBlocks;
import meowmel.pollution.common.block.PollutionPlantBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraftforge.common.ForgeHooks;

/**
 * Port of the 1.12 {@code BlockFleshFlower}: an age 0-7 flower that spreads
 * sideways and eventually grows into {@link FleshPlantBlock}, dying at age 7.
 *
 * <p>Deviations from upstream: {@code BlocksTC.fleshBlock} maps to TC4R
 * {@code thaumcraft:tainted_flesh}; the static {@code generatePlant} helper is
 * kept for future flesh worldgen but is not called anywhere yet.</p>
 */
public class FleshFlowerBlock extends Block {

    public static final IntegerProperty AGE = BlockStateProperties.AGE_7;

    public FleshFlowerBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(AGE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        Block belowBlock = below.getBlock();
        if (belowBlock instanceof FleshPlantBlock || belowBlock == TCBlocks.TAINTED_FLESH.get()) {
            return true;
        }
        if (below.isAir()) {
            int adjacentPlants = 0;
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockState side = level.getBlockState(pos.relative(direction));
                if (side.getBlock() instanceof FleshPlantBlock) {
                    adjacentPlants++;
                } else if (!side.isAir()) {
                    return false;
                }
            }
            return adjacentPlants == 1;
        }
        return false;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
                                BlockPos fromPos, boolean isMoving) {
        if (!canSurvive(state, level, pos)) {
            level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!canSurvive(state, level, pos)) {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!canSurvive(state, level, pos)) {
            level.destroyBlock(pos, true);
            return;
        }
        BlockPos above = pos.above();
        if (!level.isEmptyBlock(above) || above.getY() >= level.getMaxBuildHeight()) {
            return;
        }
        int age = state.getValue(AGE);
        if (age >= 7 || !ForgeHooks.onCropsGrowPre(level, above, state, true)) {
            return;
        }

        boolean canGrowUp = false;
        boolean overFleshBlock = false;
        BlockState below = level.getBlockState(pos.below());
        Block belowBlock = below.getBlock();
        if (belowBlock == TCBlocks.TAINTED_FLESH.get()) {
            canGrowUp = true;
        } else if (belowBlock instanceof FleshPlantBlock) {
            int columnHeight = 1;
            for (int i = 0; i < 4; i++) {
                Block next = level.getBlockState(pos.below(columnHeight + 1)).getBlock();
                if (!(next instanceof FleshPlantBlock)) {
                    if (next == TCBlocks.TAINTED_FLESH.get()) {
                        overFleshBlock = true;
                    }
                    break;
                }
                columnHeight++;
            }
            int chance = 4 + (overFleshBlock ? 1 : 0);
            if (columnHeight < 2 || random.nextInt(chance) >= columnHeight) {
                canGrowUp = true;
            }
        } else if (below.isAir()) {
            canGrowUp = true;
        }

        if (canGrowUp && areAllNeighborsEmpty(level, above, null) && level.isEmptyBlock(pos.above(2))) {
            level.setBlock(pos, PollutionPlantBlocks.FLESH_PLANT.get().defaultBlockState(), 2);
            placeGrownFlower(level, above, age);
        } else if (age < 4) {
            int attempts = random.nextInt(4);
            if (overFleshBlock) {
                attempts++;
            }
            boolean spread = false;
            for (int i = 0; i < attempts; i++) {
                Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
                BlockPos target = pos.relative(direction);
                if (level.isEmptyBlock(target) && level.isEmptyBlock(target.below())
                        && areAllNeighborsEmpty(level, target, direction.getOpposite())) {
                    placeGrownFlower(level, target, age + 1);
                    spread = true;
                }
            }
            if (spread) {
                level.setBlock(pos, PollutionPlantBlocks.FLESH_PLANT.get().defaultBlockState(), 2);
            } else {
                placeDeadFlower(level, pos);
            }
        } else if (age == 4) {
            placeDeadFlower(level, pos);
        }
        ForgeHooks.onCropsGrowPost(level, pos, level.getBlockState(pos));
    }

    private static void placeGrownFlower(LevelAccessor level, BlockPos pos, int age) {
        level.setBlock(pos, PollutionPlantBlocks.FLESH_FLOWER.get().defaultBlockState().setValue(AGE, age), 2);
        level.levelEvent(1033, pos, 0);
    }

    private static void placeDeadFlower(LevelAccessor level, BlockPos pos) {
        level.setBlock(pos, PollutionPlantBlocks.FLESH_FLOWER.get().defaultBlockState().setValue(AGE, 7), 2);
        level.levelEvent(1034, pos, 0);
    }

    private static boolean areAllNeighborsEmpty(LevelReader level, BlockPos pos, Direction excluding) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (direction != excluding && !level.isEmptyBlock(pos.relative(direction))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state,
                              BlockEntity blockEntity, ItemStack tool) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
        popResource(level, pos, new ItemStack(this));
    }

    /** Flesh worldgen helper, ported from upstream but not called yet. */
    public static void generatePlant(LevelAccessor level, BlockPos pos, RandomSource random, int radius) {
        level.setBlock(pos, PollutionPlantBlocks.FLESH_PLANT.get().defaultBlockState(), 2);
        growTreeRecursive(level, pos, random, pos, radius, 0);
    }

    private static void growTreeRecursive(LevelAccessor level, BlockPos pos, RandomSource random,
                                          BlockPos origin, int radius, int depth) {
        int trunkHeight = random.nextInt(6) + 3;
        if (depth == 0) {
            trunkHeight += 2;
        }
        for (int i = 0; i < trunkHeight; i++) {
            BlockPos upPos = pos.above(i + 1);
            if (!areAllNeighborsEmpty(level, upPos, null)) {
                return;
            }
            level.setBlock(upPos, PollutionPlantBlocks.FLESH_PLANT.get().defaultBlockState(), 2);
        }

        boolean grewBranch = false;
        if (depth < 8) {
            int branchCount = random.nextInt(10) + 3;
            if (depth == 0) {
                branchCount++;
            }
            for (int i = 0; i < branchCount; i++) {
                Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
                BlockPos branchPos = pos.above(trunkHeight).relative(direction);
                if (Math.abs(branchPos.getX() - origin.getX()) < radius
                        && Math.abs(branchPos.getZ() - origin.getZ()) < radius
                        && level.isEmptyBlock(branchPos) && level.isEmptyBlock(branchPos.below())
                        && areAllNeighborsEmpty(level, branchPos, direction.getOpposite())) {
                    grewBranch = true;
                    level.setBlock(branchPos, PollutionPlantBlocks.FLESH_PLANT.get().defaultBlockState(), 2);
                    growTreeRecursive(level, branchPos, random, origin, radius, depth + 1);
                }
            }
        }
        if (!grewBranch) {
            level.setBlock(pos.above(trunkHeight),
                    PollutionPlantBlocks.FLESH_FLOWER.get().defaultBlockState().setValue(AGE, 7), 2);
        }
    }
}
