package meowmel.pollution.dimension.worldgen.structure;

import meowmel.pollution.dimension.worldgen.PollutionStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/**
 * Procedural port of the 1.12 {@code StructureUndergroundBridgePieces} fortress:
 * a stone brick platform with cobblestone wall railings, straight arms along the
 * horizontal axes and pillars dropped towards the cave floor. The block palette
 * (stone bricks, cobblestone walls, mushrooms, one mob spawner) matches the
 * upstream {@code Straight}/{@code Crossing3} pieces.
 */
public class UndergroundBridgePiece extends StructurePiece {

    private static final int PLATFORM_HALF = 9;
    private static final int ARM_LENGTH = 19;
    private static final int RAIL_HEIGHT = 5;
    private static final int MAX_PILLAR_DEPTH = 40;

    private static final BlockState BRICKS = Blocks.STONE_BRICKS.defaultBlockState();
    private static final BlockState WALL = Blocks.COBBLESTONE_WALL.defaultBlockState();
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();

    private final int cx;
    private final int cy;
    private final int cz;
    private final int[] armLengths;

    public UndergroundBridgePiece(int cx, int cy, int cz, RandomSource random) {
        super(PollutionStructures.UNDERGROUND_BRIDGE_PIECE.get(), 0,
                new BoundingBox(cx - PLATFORM_HALF - ARM_LENGTH - 3, cy - MAX_PILLAR_DEPTH,
                        cz - PLATFORM_HALF - ARM_LENGTH - 3,
                        cx + PLATFORM_HALF + ARM_LENGTH + 3, cy + 10,
                        cz + PLATFORM_HALF + ARM_LENGTH + 3));
        this.cx = cx;
        this.cy = cy;
        this.cz = cz;

        Direction facing = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        int[] arms = new int[4];
        arms[facing.get2DDataValue()] = ARM_LENGTH;
        arms[facing.getOpposite().get2DDataValue()] = ARM_LENGTH;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (arms[direction.get2DDataValue()] == 0 && random.nextBoolean()) {
                arms[direction.get2DDataValue()] = ARM_LENGTH;
            }
        }
        this.armLengths = arms;
    }

    public UndergroundBridgePiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(PollutionStructures.UNDERGROUND_BRIDGE_PIECE.get(), tag);
        this.cx = tag.getInt("CX");
        this.cy = tag.getInt("CY");
        this.cz = tag.getInt("CZ");
        int[] arms = tag.getIntArray("Arms");
        this.armLengths = arms.length == 4 ? arms : new int[4];
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putIntArray("BB", new int[]{this.boundingBox.minX(), this.boundingBox.minY(),
                this.boundingBox.minZ(), this.boundingBox.maxX(), this.boundingBox.maxY(),
                this.boundingBox.maxZ()});
        tag.putInt("GD", this.genDepth);
        tag.putInt("CX", this.cx);
        tag.putInt("CY", this.cy);
        tag.putInt("CZ", this.cz);
        tag.putIntArray("Arms", this.armLengths);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
                            RandomSource random, BoundingBox chunkBB, ChunkPos chunkPos, BlockPos pivot) {
        this.buildPlatform(level, chunkBB, random);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            int length = this.armLengths[direction.get2DDataValue()];
            if (length > 0) {
                this.buildArm(level, chunkBB, direction, length);
            }
        }
    }

    private void buildPlatform(WorldGenLevel level, BoundingBox chunkBB, RandomSource random) {
        this.box(level, chunkBB, -PLATFORM_HALF, 3, -PLATFORM_HALF, PLATFORM_HALF, 4, PLATFORM_HALF, BRICKS);
        this.box(level, chunkBB, -PLATFORM_HALF + 1, RAIL_HEIGHT, -PLATFORM_HALF + 1,
                PLATFORM_HALF - 1, 7, PLATFORM_HALF - 1, AIR);

        for (int i = -PLATFORM_HALF; i <= PLATFORM_HALF; i++) {
            if (Math.abs(i) <= 2) {
                continue;
            }
            this.set(level, chunkBB, i, RAIL_HEIGHT, -PLATFORM_HALF, WALL);
            this.set(level, chunkBB, i, RAIL_HEIGHT, PLATFORM_HALF, WALL);
            this.set(level, chunkBB, -PLATFORM_HALF, RAIL_HEIGHT, i, WALL);
            this.set(level, chunkBB, PLATFORM_HALF, RAIL_HEIGHT, i, WALL);
        }

        for (int x : new int[]{-PLATFORM_HALF, PLATFORM_HALF}) {
            for (int z : new int[]{-PLATFORM_HALF, PLATFORM_HALF}) {
                this.pillarDown(level, chunkBB, x, 2, z);
            }
        }

        this.set(level, chunkBB, 0, RAIL_HEIGHT, 0, Blocks.SPAWNER.defaultBlockState());
        BlockPos spawnerPos = this.world(0, RAIL_HEIGHT, 0);
        if (chunkBB.isInside(spawnerPos) && level.getBlockEntity(spawnerPos) instanceof SpawnerBlockEntity spawner) {
            spawner.setEntityId(EntityType.ZOMBIE, random);
        }

        for (int i = 0; i < 2; i++) {
            int x = Mth.randomBetweenInclusive(random, -PLATFORM_HALF + 1, PLATFORM_HALF - 1);
            int z = Mth.randomBetweenInclusive(random, -PLATFORM_HALF + 1, PLATFORM_HALF - 1);
            this.set(level, chunkBB, x, RAIL_HEIGHT, z, random.nextBoolean()
                    ? Blocks.BROWN_MUSHROOM.defaultBlockState()
                    : Blocks.RED_MUSHROOM.defaultBlockState());
        }
    }

    private void buildArm(WorldGenLevel level, BoundingBox chunkBB, Direction direction, int length) {
        int ux = direction.getStepX();
        int uz = direction.getStepZ();
        int px = -uz;
        int pz = ux;
        int end = PLATFORM_HALF + length;

        for (int k = PLATFORM_HALF; k <= end; k++) {
            for (int w = -2; w <= 2; w++) {
                int x = ux * k + px * w;
                int z = uz * k + pz * w;
                this.set(level, chunkBB, x, 3, z, BRICKS);
                this.set(level, chunkBB, x, 4, z, BRICKS);
                this.set(level, chunkBB, x, RAIL_HEIGHT, z, AIR);
                this.set(level, chunkBB, x, 6, z, AIR);
                this.set(level, chunkBB, x, 7, z, AIR);
            }
            this.set(level, chunkBB, ux * k + px * 2, RAIL_HEIGHT, uz * k + pz * 2, WALL);
            this.set(level, chunkBB, ux * k - px * 2, RAIL_HEIGHT, uz * k - pz * 2, WALL);
        }

        for (int k = end; k <= end + 2; k++) {
            for (int w = -2; w <= 2; w++) {
                for (int y = 3; y <= 6; y++) {
                    boolean doorway = Math.abs(w) <= 1 && y <= RAIL_HEIGHT;
                    this.set(level, chunkBB, ux * k + px * w, y, uz * k + pz * w,
                            doorway ? AIR : BRICKS);
                }
            }
        }

        for (int w : new int[]{-2, 0, 2}) {
            this.pillarDown(level, chunkBB, ux * PLATFORM_HALF + px * w, 2, uz * PLATFORM_HALF + pz * w);
            this.pillarDown(level, chunkBB, ux * (PLATFORM_HALF + length / 2) + px * w, 2,
                    uz * (PLATFORM_HALF + length / 2) + pz * w);
            this.pillarDown(level, chunkBB, ux * (end + 2) + px * w, 2, uz * (end + 2) + pz * w);
        }
    }

    private void pillarDown(WorldGenLevel level, BoundingBox chunkBB, int dx, int dy, int dz) {
        for (int y = dy; y >= dy - MAX_PILLAR_DEPTH; y--) {
            BlockPos pos = this.world(dx, y, dz);
            if (!chunkBB.isInside(pos)) {
                continue;
            }
            BlockState state = level.getBlockState(pos);
            if (!state.isAir() && state.getFluidState().isEmpty()) {
                break;
            }
            level.setBlock(pos, BRICKS, 2);
        }
    }

    private BlockPos world(int dx, int dy, int dz) {
        return new BlockPos(this.cx + dx, this.cy + dy, this.cz + dz);
    }

    private void set(WorldGenLevel level, BoundingBox chunkBB, int dx, int dy, int dz, BlockState state) {
        BlockPos pos = this.world(dx, dy, dz);
        if (chunkBB.isInside(pos)) {
            level.setBlock(pos, state, 2);
        }
    }

    private void box(WorldGenLevel level, BoundingBox chunkBB, int x1, int y1, int z1,
                     int x2, int y2, int z2, BlockState state) {
        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                for (int z = z1; z <= z2; z++) {
                    this.set(level, chunkBB, x, y, z, state);
                }
            }
        }
    }
}
