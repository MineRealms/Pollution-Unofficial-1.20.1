package meowmel.pollution.common.block.tile;

import meowmel.pollution.api.unification.PollutionMaterials;
import meowmel.pollution.common.block.PollutionMiscBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Port of the 1.12 {@code TileEntityFleshHeart}: the core of the flesh tree.
 *
 * <p>Kept server logic: level 1-10 tracking, player soul binding, heartbeat
 * sound (faster with level), life-essence output to neighbouring fluid
 * handlers, and a cached scan for an adjacent item handler. Fluid output uses
 * the mod's own {@code InfusedLife} fluid because Blood Magic does not exist on
 * 1.20.1; the level-9 soul-network essence refill is dropped for the same
 * reason.</p>
 *
 * <p>Deviations from upstream: growth is inert. Upstream syphoned LP from a
 * Blood Magic {@code SoulNetwork} and placed the tree through
 * {@code FleshTreeGrowth}, which is not ported yet, so {@link #growOnce()}
 * returns false and the level never rises ({@link #getCurrentNetworkLP()} is
 * always 0). The heartbeat, binding, inventory scan and fluid output all work
 * unchanged.</p>
 */
public class FleshHeartBlockEntity extends BlockEntity {

    public static final int MAX_LEVEL = 10;

    /** Growth cooldown in ticks (about five minutes). */
    private static final int GROWTH_INTERVAL = 6000;

    /** Retry delay when the growth attempt fails, so the full cycle is not reset. */
    private static final int GROWTH_RETRY_INTERVAL = 400;

    /** Neighbour inventory rescan interval in ticks. */
    private static final int SCAN_INTERVAL = 60;

    /**
     * Current level, 1-10. Named {@code heartLevel} because 1.20.1
     * {@link BlockEntity} already owns a {@code level} world field and a
     * {@code getLevel()} accessor.
     */
    private int heartLevel = 1;

    /** Tree root position (the lowest trunk block). */
    private BlockPos originPos = BlockPos.ZERO;

    private int growthTimer;
    private int heartbeatTimer;
    private int fluidTimer;
    private int scanTimer;

    private UUID boundPlayerUUID;
    private String boundPlayerName = "";

    /** Cached neighbour item handler, null when none was found. */
    private BlockPos linkedInventoryPos;
    private Direction linkedInventoryFacing;

    public FleshHeartBlockEntity(BlockPos pos, BlockState state) {
        super(PollutionMiscBlocks.FLESH_HEART_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FleshHeartBlockEntity heart) {
        heart.tick();
    }

    private void tick() {
        if (level == null || level.isClientSide) {
            return;
        }

        // The server must broadcast the heartbeat; a client-side call with a null
        // player would not reach the other clients.
        heartbeatTimer++;
        if (heartbeatTimer >= getHeartbeatInterval()) {
            heartbeatTimer = 0;
            playHeartbeat();
        }

        pushFluidToNeighbors();
        scanForInventory();

        if (heartLevel >= MAX_LEVEL) {
            return;
        }
        growthTimer++;
        int requiredTicks = getGrowthIntervalTicks();
        if (growthTimer >= requiredTicks) {
            if (growOnce()) {
                growthTimer = 0;
            } else {
                growthTimer = Math.max(0, requiredTicks - GROWTH_RETRY_INTERVAL);
            }
        }
    }

    /** Higher levels beat faster (level 1: 75 ticks, level 10: 30 ticks). */
    private int getHeartbeatInterval() {
        return Math.max(20, 80 - heartLevel * 5);
    }

    private int getGrowthIntervalTicks() {
        return (int) (GROWTH_INTERVAL * (heartLevel * 1.25D));
    }

    /** LP required to reach the target level: 100,000 * (target - 1)^2. */
    public int getGrowthLPCost(int targetLevel) {
        if (targetLevel <= 1 || targetLevel > MAX_LEVEL) {
            return 0;
        }
        int growthStep = targetLevel - 1;
        return 100_000 * growthStep * growthStep;
    }

    public int getRequiredGrowthLP() {
        return heartLevel >= MAX_LEVEL ? 0 : getGrowthLPCost(heartLevel + 1);
    }

    /**
     * Upstream read this from the bound Blood Magic soul network. Blood Magic
     * has no 1.20.1 counterpart here, so the network is always empty.
     */
    public int getCurrentNetworkLP() {
        return 0;
    }

    /** Fluid output interval in ticks; higher levels push faster. */
    private int getFluidInterval() {
        return Math.max(10, 60 - heartLevel * 5);
    }

    /** Fluid output amount in mB; higher levels push more. */
    private int getFluidAmount() {
        return 50 + (heartLevel - 1) * 50;
    }

    private void pushFluidToNeighbors() {
        fluidTimer++;
        if (fluidTimer < getFluidInterval()) {
            return;
        }
        fluidTimer = 0;

        FluidStack toFill = PollutionMaterials.InfusedLife.getFluid(getFluidAmount());
        if (toFill.isEmpty()) {
            return;
        }
        for (Direction facing : Direction.values()) {
            BlockPos neighborPos = worldPosition.relative(facing);
            if (!level.isLoaded(neighborPos)) {
                continue;
            }
            BlockEntity neighbor = level.getBlockEntity(neighborPos);
            if (neighbor == null) {
                continue;
            }
            IFluidHandler handler = neighbor
                    .getCapability(ForgeCapabilities.FLUID_HANDLER, facing.getOpposite())
                    .orElse(null);
            if (handler == null) {
                continue;
            }
            int accepted = handler.fill(toFill, IFluidHandler.FluidAction.SIMULATE);
            if (accepted > 0) {
                handler.fill(new FluidStack(toFill.getFluid(), accepted), IFluidHandler.FluidAction.EXECUTE);
            }
        }
    }

    /** Scans the six neighbours once in a while and remembers the first item handler. */
    private void scanForInventory() {
        scanTimer++;
        if (scanTimer < SCAN_INTERVAL) {
            return;
        }
        scanTimer = 0;

        if (linkedInventoryPos != null) {
            if (isValidInventory(linkedInventoryPos, linkedInventoryFacing)) {
                return;
            }
            linkedInventoryPos = null;
            linkedInventoryFacing = null;
            setChanged();
        }

        for (Direction facing : Direction.values()) {
            BlockPos neighborPos = worldPosition.relative(facing);
            if (isValidInventory(neighborPos, facing)) {
                linkedInventoryPos = neighborPos;
                linkedInventoryFacing = facing;
                setChanged();
                return;
            }
        }
    }

    private boolean isValidInventory(BlockPos checkPos, Direction facing) {
        if (level == null || !level.isLoaded(checkPos)) {
            return false;
        }
        BlockEntity blockEntity = level.getBlockEntity(checkPos);
        return blockEntity != null
                && blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, facing.getOpposite()).isPresent();
    }

    public BlockPos getLinkedInventoryPos() {
        return linkedInventoryPos;
    }

    public Direction getLinkedInventoryFacing() {
        return linkedInventoryFacing;
    }

    /** Direct access to the linked neighbour handler, null when invalid. */
    @Nullable
    public IItemHandler getLinkedItemHandler() {
        if (level == null || linkedInventoryPos == null || linkedInventoryFacing == null) {
            return null;
        }
        BlockEntity blockEntity = level.getBlockEntity(linkedInventoryPos);
        if (blockEntity == null) {
            return null;
        }
        return blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, linkedInventoryFacing.getOpposite())
                .orElse(null);
    }

    /**
     * Performs one growth step.
     *
     * <p>TODO(port): upstream delegated the tree layout to {@code FleshTreeGrowth}
     * and paid LP through the Blood Magic soul network. Neither exists on
     * 1.20.1 yet, so growth is intentionally inert; this hook is kept so the
     * growth batch only has to fill in the body.</p>
     */
    private boolean growOnce() {
        if (heartLevel >= MAX_LEVEL) {
            return false;
        }
        return false;
    }

    private void playHeartbeat() {
        float volume = 0.2F + heartLevel * 0.08F;
        level.playSound(null, worldPosition, SoundEvents.NOTE_BLOCK_BASEDRUM.value(),
                SoundSource.BLOCKS, volume, 0.5F);
    }

    // ////////////////////////////////////
    // ***** level / position access *****//
    // ////////////////////////////////////

    public int getHeartLevel() {
        return heartLevel;
    }

    public void setHeartLevel(int level) {
        this.heartLevel = Math.min(Math.max(level, 1), MAX_LEVEL);
        setChanged();
    }

    public BlockPos getOrigin() {
        return originPos;
    }

    public void setOrigin(BlockPos origin) {
        this.originPos = origin;
        setChanged();
    }

    // ////////////////////////////////////
    // ***** soul binding *****//
    // ////////////////////////////////////

    public boolean isBound() {
        return boundPlayerUUID != null;
    }

    public UUID getBoundPlayerUUID() {
        return boundPlayerUUID;
    }

    public String getBoundPlayerName() {
        return boundPlayerName;
    }

    public boolean isOwner(Player player) {
        return boundPlayerUUID != null && boundPlayerUUID.equals(player.getUUID());
    }

    /** Binds a player, only once. Returns false when already bound. */
    public boolean tryBind(Player player) {
        if (boundPlayerUUID != null) {
            return false;
        }
        boundPlayerUUID = player.getUUID();
        boundPlayerName = player.getName().getString();
        setChanged();
        syncToClients();
        return true;
    }

    private void syncToClients() {
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, 3);
        }
    }

    // ////////////////////////////////////
    // ***** NBT *****//
    // ////////////////////////////////////

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Level", heartLevel);
        tag.putInt("GrowthTimer", growthTimer);
        tag.putInt("OriginX", originPos.getX());
        tag.putInt("OriginY", originPos.getY());
        tag.putInt("OriginZ", originPos.getZ());
        tag.putBoolean("HasBoundPlayer", boundPlayerUUID != null);
        if (boundPlayerUUID != null) {
            tag.putLong("BoundUUIDMost", boundPlayerUUID.getMostSignificantBits());
            tag.putLong("BoundUUIDLeast", boundPlayerUUID.getLeastSignificantBits());
            tag.putString("BoundPlayerName", boundPlayerName);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        heartLevel = tag.getInt("Level");
        if (heartLevel < 1) {
            heartLevel = 1;
        }
        if (heartLevel > MAX_LEVEL) {
            heartLevel = MAX_LEVEL;
        }
        growthTimer = tag.getInt("GrowthTimer");
        originPos = new BlockPos(tag.getInt("OriginX"), tag.getInt("OriginY"), tag.getInt("OriginZ"));
        if (tag.getBoolean("HasBoundPlayer")) {
            boundPlayerUUID = new UUID(tag.getLong("BoundUUIDMost"), tag.getLong("BoundUUIDLeast"));
            boundPlayerName = tag.getString("BoundPlayerName");
        } else {
            boundPlayerUUID = null;
            boundPlayerName = "";
        }
    }

    // ////////////////////////////////////
    // ***** client sync *****//
    // ////////////////////////////////////

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        if (packet.getTag() != null) {
            load(packet.getTag());
        }
    }
}
