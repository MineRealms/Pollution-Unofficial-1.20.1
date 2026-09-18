package meowmel.pollution.common.block.tile;

import meowmel.pollution.Pollution;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Port of the 1.12 {@code BlockPOPortal}: the flesh-pool portal to the
 * underground dimension.
 *
 * <p>Kept behaviour: the {@code one_way} boolean state (upstream
 * {@code is_one_way}), the flat portal shape with a one-way collision floor,
 * portal ambient sound and particles, frame validation on neighbour changes
 * (an invalid portal turns back into water), the recursive pool validation and
 * portal formation ritual, and {@code entityInside} teleporting when the
 * target dimension exists.</p>
 *
 * <p>Deviations from upstream: the destination is the resource key
 * {@code pollution:underground} instead of the numeric dimension id 41, and
 * that dimension is registered by a later batch. Until it exists
 * {@link #attemptSendPlayer} and {@link #tryToCreatePortal} stay inert; the
 * {@code POTeleporter} safe-arrival check is omitted for the same reason. The
 * formation catalyst event ({@code EventLoader} diamond check) also belongs to
 * that later batch, so {@link #tryToCreatePortal} is currently uncalled. The
 * 1.12 translucency render layer is not set because the placeholder cube model
 * is opaque.</p>
 */
public class PortalBlock extends Block {

    /** Upstream {@code is_one_way}: portals that do not send the player back. */
    public static final BooleanProperty ONE_WAY = BooleanProperty.create("one_way");

    /** Destination key of the upstream underground dimension (numeric id 41). */
    public static final ResourceKey<Level> TARGET_DIMENSION = ResourceKey.create(Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID, "underground"));

    private static final VoxelShape SHAPE = Shapes.box(0.0, 0.0, 0.0, 1.0, 0.8125, 1.0);
    private static final int MIN_PORTAL_SIZE = 4;
    private static final int MAX_PORTAL_SIZE = 64;

    public PortalBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(ONE_WAY, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ONE_WAY);
    }

    // ////////////////////////////////////
    // ***** shape *****//
    // ////////////////////////////////////

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
                                        CollisionContext context) {
        return state.getValue(ONE_WAY) ? SHAPE : Shapes.empty();
    }

    // ////////////////////////////////////
    // ***** teleport *****//
    // ////////////////////////////////////

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!state.getValue(ONE_WAY)) {
            attemptSendPlayer(entity, false);
        }
    }

    /**
     * Sends an entity to the underground dimension.
     *
     * <p>TODO(dimension batch): {@code pollution:underground} is not registered
     * yet, so this returns early and the portal is inert. The teleport call is
     * already in place for when the dimension lands.</p>
     */
    public static void attemptSendPlayer(Entity entity, boolean forcedEntry) {
        if (entity.level().isClientSide || !entity.isAlive()) {
            return;
        }
        if (entity.isPassenger() || entity.isVehicle() || !entity.canChangeDimensions()) {
            return;
        }
        if (!forcedEntry && entity.getPortalCooldown() > 0) {
            return;
        }
        ServerLevel target = getTargetLevel(entity.level());
        if (target == null) {
            return;
        }
        entity.setPortalCooldown(10);
        entity.changeDimension(target);
    }

    @Nullable
    public static ServerLevel getTargetLevel(Level level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return null;
        }
        return serverLevel.getServer().getLevel(TARGET_DIMENSION);
    }

    // ////////////////////////////////////
    // ***** portal formation *****//
    // ////////////////////////////////////

    public boolean canFormPortal(BlockState state) {
        return state.is(Blocks.WATER) || (state.is(this) && state.getValue(ONE_WAY));
    }

    /**
     * Validates the pool and, when the target dimension exists, converts the
     * pool floor into portal blocks.
     *
     * @param catalyst the catalyst stack, one item is consumed on success
     */
    public boolean tryToCreatePortal(Level level, BlockPos pos, ItemStack catalyst, @Nullable Player player) {
        BlockState state = level.getBlockState(pos);
        if (!canFormPortal(state) || !isSturdyBelow(level, pos)) {
            return false;
        }

        Map<BlockPos, Boolean> blocksChecked = new HashMap<>();
        blocksChecked.put(pos, true);
        int[] portalSize = {0};
        if (!recursivelyValidatePortal(level, pos, blocksChecked, portalSize, state)
                || portalSize[0] < MIN_PORTAL_SIZE) {
            return false;
        }

        // TODO(dimension batch): upstream checked POTeleporter#isSafeAround and
        // messaged the player when the arrival was unsafe. Without the
        // dimension there is nothing to validate, and forming a portal would
        // only create dead blocks.
        if (getTargetLevel(level) == null) {
            return false;
        }

        catalyst.shrink(1);
        causeLightning(level, pos, true);
        for (Map.Entry<BlockPos, Boolean> checkedPos : blocksChecked.entrySet()) {
            if (checkedPos.getValue()) {
                level.setBlock(checkedPos.getKey(), defaultBlockState(), 2);
            }
        }
        return true;
    }

    private static boolean recursivelyValidatePortal(Level level, BlockPos pos, Map<BlockPos, Boolean> blocksChecked,
                                                     int[] portalSize, BlockState requiredState) {
        if (++portalSize[0] > MAX_PORTAL_SIZE) {
            return false;
        }
        boolean isPoolProbablyEnclosed = true;

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (portalSize[0] > MAX_PORTAL_SIZE) {
                break;
            }
            BlockPos positionCheck = pos.relative(direction);
            if (blocksChecked.containsKey(positionCheck)) {
                continue;
            }
            BlockState state = level.getBlockState(positionCheck);
            if (state == requiredState && isSturdyBelow(level, positionCheck)) {
                blocksChecked.put(positionCheck, true);
                if (isPoolProbablyEnclosed) {
                    isPoolProbablyEnclosed = recursivelyValidatePortal(level, positionCheck, blocksChecked,
                            portalSize, requiredState);
                }
            } else if ((isGrassOrDirt(state) && isNatureBlock(level.getBlockState(positionCheck.above())))
                    || state.is(Blocks.STONE)) {
                blocksChecked.put(positionCheck, false);
            } else {
                return false;
            }
        }
        return isPoolProbablyEnclosed;
    }

    private static boolean isSturdyBelow(Level level, BlockPos pos) {
        BlockPos below = pos.below();
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
    }

    /** Modern replacement for the 1.12 {@code Material.GRASS}/{@code GROUND} check. */
    private static boolean isGrassOrDirt(BlockState state) {
        return state.is(BlockTags.DIRT);
    }

    /** Modern replacement for the 1.12 {@code Material.PLANTS}/{@code VINE}/{@code LEAVES} check. */
    private static boolean isNatureBlock(BlockState state) {
        return state.is(BlockTags.LEAVES) || state.is(Blocks.VINE) || state.getBlock() instanceof BushBlock;
    }

    private static void causeLightning(Level level, BlockPos pos, boolean visualOnly) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt == null) {
            return;
        }
        bolt.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        bolt.setVisualOnly(visualOnly);
        level.addFreshEntity(bolt);

        if (visualOnly) {
            double range = 3.0;
            for (Entity victim : level.getEntitiesOfClass(Entity.class, new AABB(pos).inflate(range))) {
                if (!ForgeEventFactory.onEntityStruckByLightning(victim, bolt)) {
                    victim.thunderHit(serverLevel, bolt);
                }
            }
        }
    }

    // ////////////////////////////////////
    // ***** frame upkeep *****//
    // ////////////////////////////////////

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
                                BlockPos fromPos, boolean isMoving) {
        boolean good = isSturdyBelow(level, pos);
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            if (!good) {
                break;
            }
            BlockState neighboringState = level.getBlockState(pos.relative(facing));
            good = isGrassOrDirt(neighboringState) || neighboringState == state;
        }
        if (!good) {
            level.levelEvent(2001, pos, Block.getId(state));
            level.setBlock(pos, Blocks.WATER.defaultBlockState(), 3);
        }
    }

    // ////////////////////////////////////
    // ***** client effects (vanilla portal copy) *****//
    // ////////////////////////////////////

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(ONE_WAY) && random.nextInt(100) < 80) {
            return;
        }
        if (random.nextInt(100) == 0) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    SoundEvents.PORTAL_AMBIENT, SoundSource.BLOCKS, 0.5F,
                    random.nextFloat() * 0.4F + 0.8F, false);
        }
        for (int i = 0; i < 4; i++) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + 1.0;
            double z = pos.getZ() + random.nextDouble();
            double vx = (random.nextDouble() - 0.5) * 0.5;
            double vy = random.nextDouble();
            double vz = (random.nextDouble() - 0.5) * 0.5;
            level.addParticle(ParticleTypes.PORTAL, x, y, z, vx, vy, vz);
        }
    }
}
