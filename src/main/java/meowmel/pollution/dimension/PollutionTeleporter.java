package meowmel.pollution.dimension;

import meowmel.pollution.common.block.PollutionMiscBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Modern replacement for the 1.12 {@code POTeleporter}: the arrival handler of
 * the {@code pollution:portal} block.
 *
 * <p>Kept behaviour: an existing {@code pollution:portal} block near the arrival
 * point is reused; when none is found a 2x2 portal is built on a small
 * grass/dirt platform (upstream {@code makePortalAt}, including the cleared
 * 4x4x5 air pocket and the random nature decorations) and the arrival column is
 * clamped to y 30..118. The y factor is upstream's: arriving in the overworld
 * multiplies the entry height by 2, arriving in the underground by 0.5.
 * Coordinates stay 1:1 because the dimension type uses
 * {@code coordinate_scale: 1.0}.</p>
 *
 * <p>Deviations from upstream: the portal search radius is 16 instead of 200
 * (every portal in this dimension is created by this teleporter next to the
 * entry column), the destination coordinate cache and the
 * {@code isSafeAround}/world border progression scan are omitted until the
 * progression systems land.</p>
 */
public final class PollutionTeleporter implements ITeleporter {

    private static final PollutionTeleporter INSTANCE = new PollutionTeleporter();

    private static final int SEARCH_RADIUS = 16;
    private static final int SPOT_RADIUS = 8;
    private static final int MIN_PORTAL_Y = 30;
    private static final int MAX_PORTAL_Y = 118;

    private PollutionTeleporter() {}

    public static PollutionTeleporter get() {
        return INSTANCE;
    }

    public static net.minecraft.resources.ResourceKey<Level> originDimension() {
        return net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION,
                net.minecraft.resources.ResourceLocation.parse(meowmel.pollution.PollutionConfig.PORTAL_ORIGIN_DIMENSION.get()));
    }

    /** Upstream progression checks are no-ops; its actual safety rule is the world border. */
    public static boolean isSafeAround(ServerLevel level, BlockPos pos) {
        if (!level.getWorldBorder().isWithinBounds(pos)) return false;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (!level.getWorldBorder().isWithinBounds(pos.relative(direction, 16))) return false;
        }
        return true;
    }

    @Override
    public PortalInfo getPortalInfo(Entity entity, ServerLevel destination,
                                    Function<ServerLevel, PortalInfo> defaultPortalInfo) {
        BlockPos origin = entity.blockPosition();
        if (!isSafeAround(destination, origin)) {
            var border = destination.getWorldBorder();
            // Keep the full 4x4 arrival platform inside even unusually small world borders.
            int x = (int) Mth.clamp(origin.getX(), border.getMinX() + Math.min(18, border.getSize() / 3),
                    border.getMaxX() - Math.min(18, border.getSize() / 3));
            int z = (int) Mth.clamp(origin.getZ(), border.getMinZ() + Math.min(18, border.getSize() / 3),
                    border.getMaxZ() - Math.min(18, border.getSize() / 3));
            origin = new BlockPos(x, origin.getY(), z);
        }
        BlockPos portalPos = findExistingPortal(destination, origin);
        if (portalPos == null) {
            double yFactor = destination.dimension().equals(originDimension()) ? 2.0D : 0.5D;
            portalPos = createPortal(destination, origin, yFactor);
        }
        return new PortalInfo(Vec3.atBottomCenterOf(portalPos), Vec3.ZERO,
                entity.getYRot(), entity.getXRot());
    }

    @Override
    public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw,
                              Function<Boolean, Entity> repositionEntity) {
        return repositionEntity.apply(false);
    }

    /**
     * Finds the nearest portal block in the loaded chunks around the arrival
     * column. Only one portal per column is considered, so the scan cost stays
     * proportional to the loaded area.
     */
    @Nullable
    private static BlockPos findExistingPortal(ServerLevel level, BlockPos origin) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        int minY = level.getMinBuildHeight();
        int maxY = level.getMaxBuildHeight() - 1;

        for (int dx = -SEARCH_RADIUS; dx <= SEARCH_RADIUS; dx++) {
            for (int dz = -SEARCH_RADIUS; dz <= SEARCH_RADIUS; dz++) {
                int x = origin.getX() + dx;
                int z = origin.getZ() + dz;
                if (!level.hasChunkAt(cursor.set(x, origin.getY(), z))) {
                    continue;
                }
                for (int y = maxY; y >= minY; y--) {
                    cursor.set(x, y, z);
                    if (level.getBlockState(cursor).is(PollutionMiscBlocks.PORTAL.get())) {
                        double distance = cursor.distSqr(origin);
                        if (distance < bestDistance) {
                            bestDistance = distance;
                            best = cursor.immutable();
                        }
                        break;
                    }
                }
            }
        }
        return best;
    }

    /**
     * Builds an arrival portal near the entry column. Falls back to the clamped
     * target position when no naturally suitable spot was found, mirroring the
     * upstream unconditional {@code makePortalAt} fallback.
     */
    private static BlockPos createPortal(ServerLevel level, BlockPos origin, double yFactor) {
        int targetY = Mth.clamp((int) (origin.getY() * yFactor), MIN_PORTAL_Y, MAX_PORTAL_Y);
        BlockPos target = new BlockPos(origin.getX(), targetY, origin.getZ());
        BlockPos spot = findPortalSpot(level, target);
        return buildPortal(level, spot != null ? spot : target);
    }

    /**
     * Highest two-block-high replaceable pocket on solid ground per column,
     * closest to the target point. Mirrors the upstream {@code findPortalCoords}
     * scan restricted to the clamped portal band.
     */
    @Nullable
    private static BlockPos findPortalSpot(ServerLevel level, BlockPos target) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;

        for (int dx = -SPOT_RADIUS; dx <= SPOT_RADIUS; dx++) {
            for (int dz = -SPOT_RADIUS; dz <= SPOT_RADIUS; dz++) {
                int x = target.getX() + dx;
                int z = target.getZ() + dz;
                if (!level.hasChunkAt(cursor.set(x, target.getY(), z))) {
                    continue;
                }
                for (int y = MAX_PORTAL_Y; y >= MIN_PORTAL_Y; y--) {
                    cursor.set(x, y, z);
                    if (!isPortalSpot(level, cursor)) {
                        continue;
                    }
                    double distance = cursor.distSqr(target);
                    if (distance < bestDistance) {
                        bestDistance = distance;
                        best = cursor.immutable();
                    }
                    break;
                }
            }
        }
        return best;
    }

    private static boolean isPortalSpot(ServerLevel level, BlockPos pos) {
        if (!level.getBlockState(pos).canBeReplaced()) {
            return false;
        }
        if (!level.getBlockState(pos.above()).canBeReplaced()) {
            return false;
        }
        BlockPos below = pos.below();
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
    }

    /**
     * Upstream {@code makePortalAt}: a grass ring around a 2x2 dirt platform,
     * the portal blocks on top, a cleared 4x4x5 pocket and one random nature
     * decoration per ring block.
     *
     * <p>Blocks are placed with {@link Block#UPDATE_CLIENTS} only: notifying
     * neighbours mid-build would trip the portal block's own frame validation
     * while the 2x2 is still incomplete and turn it back into water.</p>
     */
    private static BlockPos buildPortal(ServerLevel level, BlockPos pos) {
        BlockState grass = Blocks.GRASS_BLOCK.defaultBlockState();
        level.setBlock(pos.west().north(), grass, Block.UPDATE_CLIENTS);
        level.setBlock(pos.north(), grass, Block.UPDATE_CLIENTS);
        level.setBlock(pos.east().north(), grass, Block.UPDATE_CLIENTS);
        level.setBlock(pos.east(2).north(), grass, Block.UPDATE_CLIENTS);
        level.setBlock(pos.west(), grass, Block.UPDATE_CLIENTS);
        level.setBlock(pos.east(2), grass, Block.UPDATE_CLIENTS);
        level.setBlock(pos.west().south(), grass, Block.UPDATE_CLIENTS);
        level.setBlock(pos.east(2).south(), grass, Block.UPDATE_CLIENTS);
        level.setBlock(pos.west().south(2), grass, Block.UPDATE_CLIENTS);
        level.setBlock(pos.south(2), grass, Block.UPDATE_CLIENTS);
        level.setBlock(pos.east().south(2), grass, Block.UPDATE_CLIENTS);
        level.setBlock(pos.east(2).south(2), grass, Block.UPDATE_CLIENTS);

        BlockState dirt = Blocks.DIRT.defaultBlockState();
        level.setBlock(pos.below(), dirt, Block.UPDATE_CLIENTS);
        level.setBlock(pos.east().below(), dirt, Block.UPDATE_CLIENTS);
        level.setBlock(pos.south().below(), dirt, Block.UPDATE_CLIENTS);
        level.setBlock(pos.east().south().below(), dirt, Block.UPDATE_CLIENTS);

        BlockState portal = PollutionMiscBlocks.PORTAL.get().defaultBlockState()
                .setValue(meowmel.pollution.common.block.tile.PortalBlock.ONE_WAY, !meowmel.pollution.PollutionConfig.RETURN_PORTAL_USABLE.get());
        level.setBlock(pos, portal, Block.UPDATE_CLIENTS);
        level.setBlock(pos.east(), portal, Block.UPDATE_CLIENTS);
        level.setBlock(pos.south(), portal, Block.UPDATE_CLIENTS);
        level.setBlock(pos.east().south(), portal, Block.UPDATE_CLIENTS);

        for (int dx = -1; dx <= 2; dx++) {
            for (int dz = -1; dz <= 2; dz++) {
                for (int dy = 1; dy <= 5; dy++) {
                    level.setBlock(pos.offset(dx, dy, dz), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                }
            }
        }

        decorate(level, pos.west().north().above());
        decorate(level, pos.north().above());
        decorate(level, pos.east().north().above());
        decorate(level, pos.east(2).north().above());
        decorate(level, pos.west().above());
        decorate(level, pos.east(2).above());
        decorate(level, pos.west().south().above());
        decorate(level, pos.east(2).south().above());
        decorate(level, pos.west().south(2).above());
        decorate(level, pos.south(2).above());
        decorate(level, pos.east().south(2).above());
        decorate(level, pos.east(2).south(2).above());

        return pos;
    }

    private static void decorate(ServerLevel level, BlockPos pos) {
        Block[] decorations = {
                Blocks.BROWN_MUSHROOM, Blocks.RED_MUSHROOM, Blocks.GRASS,
                Blocks.POPPY, Blocks.DANDELION, Blocks.OAK_LEAVES
        };
        Block decoration = decorations[level.random.nextInt(decorations.length)];
        level.setBlock(pos, decoration.defaultBlockState(), Block.UPDATE_CLIENTS);
    }
}
