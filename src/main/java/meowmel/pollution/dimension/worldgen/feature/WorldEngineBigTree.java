package meowmel.pollution.dimension.worldgen.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

/** State-based port of WorldEngine's parameterized WE_BigTreeGen. */
public final class WorldEngineBigTree extends WorldEngineTreeBase {

    private static final int[] OTHER_COORD_PAIRS = {2, 0, 0, 1, 2, 1};

    private final BlockState woodX;
    private final BlockState woodY;
    private final BlockState woodZ;
    private final BlockState leaves;
    private final int trunkSize;
    private final int heightLimitLimit;
    private final int leafDistanceLimit;
    private final double heightAttenuation;
    private final double branchSlope;
    private final double scaleWidth;
    private final double leafDensity;

    public WorldEngineBigTree(BlockState wood, BlockState leaves, int trunkSize,
                                      int heightLimitLimit, int leafDistanceLimit,
                                      double heightAttenuation, double branchSlope,
                                      double scaleWidth, double leafDensity) {
        this.woodX = orientWood(wood, Direction.Axis.X);
        this.woodY = orientWood(wood, Direction.Axis.Y);
        this.woodZ = orientWood(wood, Direction.Axis.Z);
        this.leaves = leaves;
        this.trunkSize = trunkSize;
        this.heightLimitLimit = heightLimitLimit;
        this.leafDistanceLimit = leafDistanceLimit;
        this.heightAttenuation = heightAttenuation;
        this.branchSlope = branchSlope;
        this.scaleWidth = scaleWidth;
        this.leafDensity = leafDensity;
    }

    @Override
    public boolean generate(WorldGenLevel world, RandomSource random, BlockPos origin) {
        int heightLimit = 10 + random.nextInt(heightLimitLimit);
        int[] base = {origin.getX(), origin.getY(), origin.getZ()};
        int[] lineStart = base.clone();
        int[] lineEnd = {base[0], base[1] + heightLimit - 1, base[2]};
        BlockPos soilPos = origin.below();
        BlockState soil = world.getBlockState(soilPos);
        if (!soil.is(BlockTags.DIRT)) {
            return false;
        }

        int obstruction = checkBlockLine(world, lineStart, lineEnd);
        if (obstruction != -1) {
            if (obstruction < 6) {
                return false;
            }
            heightLimit = obstruction;
        }

        int height = (int) (heightLimit * heightAttenuation);
        if (height >= heightLimit) {
            height = heightLimit - 1;
        }
        int nodesPerLayer = (int) (1.382D + Math.pow(leafDensity * heightLimit / 13.0D, 2.0D));
        if (nodesPerLayer < 1) {
            nodesPerLayer = 1;
        }

        int[][] candidates = new int[nodesPerLayer * heightLimit][4];
        int layerY = base[1] + heightLimit - leafDistanceLimit;
        int nodeCount = 1;
        int trunkTop = base[1] + height;
        int layerIndex = layerY - base[1];
        candidates[0] = new int[]{base[0], layerY, base[2], trunkTop};
        layerY--;

        while (layerIndex >= 0) {
            float layerSize = layerSize(layerIndex, heightLimit);
            if (layerSize >= 0.0F) {
                for (int attempt = 0; attempt < nodesPerLayer; attempt++) {
                    double radius = scaleWidth * layerSize * (random.nextFloat() + 0.328D);
                    double angle = random.nextFloat() * Math.PI * 2.0D;
                    int nodeX = floor(radius * Math.sin(angle) + base[0] + 0.5D);
                    int nodeZ = floor(radius * Math.cos(angle) + base[2] + 0.5D);
                    int[] node = {nodeX, layerY, nodeZ};
                    int[] nodeTop = {nodeX, layerY + leafDistanceLimit, nodeZ};
                    if (checkBlockLine(world, node, nodeTop) != -1) {
                        continue;
                    }

                    int[] branchBase = base.clone();
                    double horizontal = Math.sqrt(Math.pow(Math.abs(base[0] - nodeX), 2.0D)
                            + Math.pow(Math.abs(base[2] - nodeZ), 2.0D));
                    double drop = horizontal * branchSlope;
                    branchBase[1] = node[1] - drop > trunkTop ? trunkTop : (int) (node[1] - drop);
                    if (checkBlockLine(world, branchBase, node) == -1 && nodeCount < candidates.length) {
                        candidates[nodeCount++] = new int[]{nodeX, layerY, nodeZ, branchBase[1]};
                    }
                }
            }
            layerY--;
            layerIndex--;
        }

        int[][] leafNodes = new int[nodeCount][4];
        System.arraycopy(candidates, 0, leafNodes, 0, nodeCount);
        for (int[] node : leafNodes) {
            for (int y = node[1]; y < node[1] + leafDistanceLimit; y++) {
                float relative = y - node[1];
                float radius = relative >= 0.0F && relative < leafDistanceLimit
                        ? (relative != 0.0F && relative != leafDistanceLimit - 1 ? 3.0F : 2.0F) : -1.0F;
                placeLeafLayer(world, new int[]{node[0], y, node[2]}, radius);
            }
        }

        int[] trunkBase = base.clone();
        int[] trunkEnd = {base[0], base[1] + height, base[2]};
        placeBlockLine(world, trunkBase, trunkEnd);
        if (trunkSize == 2) {
            trunkBase[0]++;
            trunkEnd[0]++;
            placeBlockLine(world, trunkBase, trunkEnd);
            trunkBase[2]++;
            trunkEnd[2]++;
            placeBlockLine(world, trunkBase, trunkEnd);
            trunkBase[0]--;
            trunkEnd[0]--;
            placeBlockLine(world, trunkBase, trunkEnd);
        }

        int[] branchStart = base.clone();
        for (int[] node : leafNodes) {
            int[] nodePos = {node[0], node[1], node[2]};
            branchStart[1] = node[3];
            if (branchStart[1] - base[1] >= heightLimit * 0.2D) {
                placeBlockLine(world, branchStart, nodePos);
            }
        }
        return true;
    }

    private static float layerSize(int layerIndex, int heightLimit) {
        if (layerIndex < heightLimit * 0.3D) {
            return -1.618F;
        }
        float half = heightLimit / 2.0F;
        float distance = half - layerIndex;
        float radius;
        if (distance == 0.0F) {
            radius = half;
        } else if (Math.abs(distance) >= half) {
            radius = 0.0F;
        } else {
            radius = (float) Math.sqrt(Math.pow(Math.abs(half), 2.0D) - Math.pow(Math.abs(distance), 2.0D));
        }
        return radius * 0.5F;
    }

    private void placeLeafLayer(WorldGenLevel world, int[] center, float radius) {
        int firstAxis = OTHER_COORD_PAIRS[1];
        int secondAxis = OTHER_COORD_PAIRS[4];
        int[] cursor = {0, center[1], 0};
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
        int rounded = (int) (radius + 0.618D);
        for (int a = -rounded; a <= rounded; a++) {
            cursor[firstAxis] = center[firstAxis] + a;
            for (int b = -rounded; b <= rounded; b++) {
                double distance = Math.pow(Math.abs(a) + 0.5D, 2.0D) + Math.pow(Math.abs(b) + 0.5D, 2.0D);
                if (distance > radius * radius) {
                    continue;
                }
                cursor[secondAxis] = center[secondAxis] + b;
                blockPos.set(cursor[0], cursor[1], cursor[2]);
                BlockState state = world.getBlockState(blockPos);
                if (state.isAir()
                        || state.is(BlockTags.LEAVES)) {
                    setBlockAndNotifyAdequately(world, blockPos, leaves);
                }
            }
        }
    }

    private int checkBlockLine(WorldGenLevel world, int[] start, int[] end) {
        int[] delta = {end[0] - start[0], end[1] - start[1], end[2] - start[2]};
        int mainAxis = dominantAxis(delta);
        if (delta[mainAxis] == 0) {
            return -1;
        }
        int secondAxis = OTHER_COORD_PAIRS[mainAxis];
        int thirdAxis = OTHER_COORD_PAIRS[mainAxis + 3];
        int step = delta[mainAxis] > 0 ? 1 : -1;
        double secondSlope = (double) delta[secondAxis] / delta[mainAxis];
        double thirdSlope = (double) delta[thirdAxis] / delta[mainAxis];
        int stop = delta[mainAxis] + step;
        int distance = 0;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (; distance != stop; distance += step) {
            int x = coordinateOnLine(start, mainAxis, secondAxis, thirdAxis,
                    distance, secondSlope, thirdSlope, 0);
            int y = coordinateOnLine(start, mainAxis, secondAxis, thirdAxis,
                    distance, secondSlope, thirdSlope, 1);
            int z = coordinateOnLine(start, mainAxis, secondAxis, thirdAxis,
                    distance, secondSlope, thirdSlope, 2);
            cursor.set(x, y, z);
            if (!isReplaceable(world, cursor)) {
                break;
            }
        }
        return distance == stop ? -1 : Math.abs(distance);
    }

    private void placeBlockLine(WorldGenLevel world, int[] start, int[] end) {
        int[] delta = {end[0] - start[0], end[1] - start[1], end[2] - start[2]};
        int mainAxis = dominantAxis(delta);
        if (delta[mainAxis] == 0) {
            return;
        }
        int secondAxis = OTHER_COORD_PAIRS[mainAxis];
        int thirdAxis = OTHER_COORD_PAIRS[mainAxis + 3];
        int step = delta[mainAxis] > 0 ? 1 : -1;
        double secondSlope = (double) delta[secondAxis] / delta[mainAxis];
        double thirdSlope = (double) delta[thirdAxis] / delta[mainAxis];
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int distance = 0, stop = delta[mainAxis] + step; distance != stop; distance += step) {
            int x = coordinateOnLine(start, mainAxis, secondAxis, thirdAxis,
                    distance, secondSlope, thirdSlope, 0, 0.5D);
            int y = coordinateOnLine(start, mainAxis, secondAxis, thirdAxis,
                    distance, secondSlope, thirdSlope, 1, 0.5D);
            int z = coordinateOnLine(start, mainAxis, secondAxis, thirdAxis,
                    distance, secondSlope, thirdSlope, 2, 0.5D);
            int dx = Math.abs(x - start[0]);
            int dz = Math.abs(z - start[2]);
            Direction.Axis axis = dx == 0 && dz == 0 ? Direction.Axis.Y
                    : (dx >= dz ? Direction.Axis.X : Direction.Axis.Z);
            cursor.set(x, y, z);
            setBlockAndNotifyAdequately(world, cursor, orientedWood(axis));
        }
    }

    private static int coordinateOnLine(int[] start, int mainAxis, int secondAxis, int thirdAxis,
                                        int distance, double secondSlope, double thirdSlope, int axis) {
        return coordinateOnLine(start, mainAxis, secondAxis, thirdAxis,
                distance, secondSlope, thirdSlope, axis, 0.0D);
    }

    private static int coordinateOnLine(int[] start, int mainAxis, int secondAxis, int thirdAxis,
                                        int distance, double secondSlope, double thirdSlope,
                                        int axis, double roundingOffset) {
        if (axis == mainAxis) {
            return floor(start[axis] + distance + roundingOffset);
        }
        double slope = axis == secondAxis ? secondSlope : thirdSlope;
        return floor(start[axis] + distance * slope + roundingOffset);
    }

    private static int dominantAxis(int[] delta) {
        int axis = 0;
        for (int i = 1; i < 3; i++) {
            if (Math.abs(delta[i]) > Math.abs(delta[axis])) {
                axis = i;
            }
        }
        return axis;
    }

    private BlockState orientedWood(Direction.Axis axis) {
        switch (axis) {
            case X:
                return woodX;
            case Z:
                return woodZ;
            case Y:
            default:
                return woodY;
        }
    }

    private static BlockState orientWood(BlockState wood, Direction.Axis axis) {
        return wood.getBlock() instanceof RotatedPillarBlock
                ? wood.setValue(RotatedPillarBlock.AXIS, axis) : wood;
    }

    private static int floor(double value) {
        int integer = (int) value;
        return value < integer ? integer - 1 : integer;
    }
}
