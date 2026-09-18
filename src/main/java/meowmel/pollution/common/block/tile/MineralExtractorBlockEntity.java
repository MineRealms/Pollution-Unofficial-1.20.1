package meowmel.pollution.common.block.tile;

import dev.tc4port.thaumcraft.api.aspect.AspectId;
import dev.tc4port.thaumcraft.api.essentia.EssentiaApi;
import dev.tc4port.thaumcraft.api.essentia.EssentiaTransferMode;
import dev.tc4port.thaumcraft.api.essentia.EssentiaTransport;
import meowmel.pollution.common.block.PollutionMiscBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Port of the 1.12 {@code TileEntityMineralExtractor}: mines ore blocks inside a
 * 3x3x3 chunk area (radius 24) and converts them into its 9-slot output
 * inventory, powered by entropy (perditio) and magic (praecantatio) essentia.
 *
 * <p>Kept server logic: essentia intake from neighbouring TC4R
 * {@link EssentiaTransport}s, per-tick entropy/magic cost, nearest-first ring
 * scan with amber-ore fallback, all-or-nothing output insertion, virtual
 * grass/stone modes, item + fluid output push, and the {@code outputInventory}
 * / {@code outputTank} Forge capabilities. The extractor itself is an
 * {@link EssentiaTransport} input, replacing the removed
 * {@code IAspectSource}.</p>
 *
 * <p>Deviations from upstream: the 1.12 {@code OreDictionary} is replaced by
 * item tags ({@code forge:ores}, plus any {@code *:ores} / {@code *:ores/*}
 * tag), the GT {@code MetaTileEntityHolder} unwrapping is gone because modern
 * essentia transports are block entities, item id/meta GUI fields are replaced
 * by a pending {@link ItemStack}, and {@code enabled} can be toggled by
 * right-click because the upstream Container/GUI is not ported yet.</p>
 */
public class MineralExtractorBlockEntity extends BlockEntity implements EssentiaTransport {

    // ////////////////////////////////////
    // ***** constants *****//
    // ////////////////////////////////////

    private static final int MAX_STOCK = 10000;
    private static final int MAGIC_COST_PER_TICK = 5;
    private static final int ENTROPY_COST_PER_TICK = 5;
    private static final int MINE_INTERVAL = 5;
    private static final int MINE_RADIUS = 24;
    private static final int SCAN_PER_TICK = 2048;
    private static final int OUTPUT_SLOTS = 9;
    private static final int OUTPUT_TANK_CAPACITY = 10000;
    private static final int MAX_ESSENTIA_PER_TICK = 1000;

    /** Upstream {@code Aspect.ENTROPY}. */
    private static final AspectId CHAOS_ASPECT = AspectId.parse("perditio");
    /** Upstream {@code Aspect.MAGIC}. */
    private static final AspectId MAGIC_ASPECT = AspectId.parse("praecantatio");

    // ////////////////////////////////////
    // ***** essentia chambers *****//
    // ////////////////////////////////////

    private int chaosAmount;
    private int magicAmount;

    // ////////////////////////////////////
    // ***** output buffers *****//
    // ////////////////////////////////////

    private final ItemStackHandler outputInventory = new ItemStackHandler(OUTPUT_SLOTS);
    private final FluidTank outputTank = new FluidTank(OUTPUT_TANK_CAPACITY);
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> outputInventory);
    private final LazyOptional<IFluidHandler> fluidHandler = LazyOptional.of(() -> outputTank);

    // ////////////////////////////////////
    // ***** state *****//
    // ////////////////////////////////////

    /** 0 = real ore, 1 = virtual grass, 2 = virtual stone. */
    private int mode;
    /** Placed extractors start stopped, like upstream. */
    private boolean enabled;

    // ////////////////////////////////////
    // ***** scan / mining state *****//
    // ////////////////////////////////////

    private boolean scanInitialized;
    private int scanRing;
    private int scanDx;
    private int scanDy;
    private int scanDz;
    @Nullable
    private BlockPos pendingTarget;
    private ItemStack pendingOre = ItemStack.EMPTY;
    private int mineTimer;

    public MineralExtractorBlockEntity(BlockPos pos, BlockState state) {
        super(PollutionMiscBlocks.MINERAL_EXTRACTOR_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MineralExtractorBlockEntity extractor) {
        extractor.tick();
    }

    private void tick() {
        if (level == null || level.isClientSide) {
            return;
        }

        // Intake and output keep running even while stopped.
        pullEssentiaFromNeighbors();
        if (enabled) {
            tickRunning();
        }
        pushOutputToNeighbors();
    }

    // ////////////////////////////////////
    // ***** running logic *****//
    // ////////////////////////////////////

    private void tickRunning() {
        if (magicAmount < MAGIC_COST_PER_TICK || chaosAmount < ENTROPY_COST_PER_TICK) {
            return;
        }
        magicAmount -= MAGIC_COST_PER_TICK;
        chaosAmount -= ENTROPY_COST_PER_TICK;
        setChanged();

        mineTimer--;
        if (mineTimer > 0) {
            return;
        }
        mineTimer = MINE_INTERVAL;

        switch (mode) {
            case 1 -> produceVirtualBlock(new ItemStack(Blocks.GRASS_BLOCK));
            case 2 -> produceVirtualBlock(new ItemStack(Blocks.STONE));
            default -> {
                if (pendingTarget == null) {
                    scanForOre();
                }
                if (pendingTarget != null && mineBlock(pendingTarget)) {
                    pendingTarget = null;
                    resetScanPosition();
                }
            }
        }
    }

    private void produceVirtualBlock(ItemStack stack) {
        ItemHandlerHelper.insertItemStacked(outputInventory, stack, false);
        setChanged();
    }

    // ////////////////////////////////////
    // ***** essentia intake *****//
    // ////////////////////////////////////

    private void pullEssentiaFromNeighbors() {
        for (Direction facing : Direction.values()) {
            BlockPos neighborPos = worldPosition.relative(facing);
            if (!level.isLoaded(neighborPos)) {
                continue;
            }
            if (!(level.getBlockEntity(neighborPos) instanceof EssentiaTransport transport)) {
                continue;
            }
            Direction side = facing.getOpposite();
            if (chaosAmount < MAX_STOCK) {
                pullAspect(transport, side, CHAOS_ASPECT, MAX_STOCK - chaosAmount);
            }
            if (magicAmount < MAX_STOCK) {
                pullAspect(transport, side, MAGIC_ASPECT, MAX_STOCK - magicAmount);
            }
        }
    }

    private boolean pullAspect(EssentiaTransport transport, Direction side, AspectId aspect, int amount) {
        int want = Math.min(amount, MAX_ESSENTIA_PER_TICK);
        if (!transport.canOutputTo(side)) {
            return false;
        }
        int taken = EssentiaApi.take(level, transport, aspect, want, side, EssentiaTransferMode.EXECUTE);
        if (taken > 0) {
            addToChamber(aspect, taken);
            return true;
        }
        return false;
    }

    private void addToChamber(AspectId aspect, int amount) {
        if (CHAOS_ASPECT.equals(aspect)) {
            chaosAmount = Math.min(MAX_STOCK, chaosAmount + amount);
        } else if (MAGIC_ASPECT.equals(aspect)) {
            magicAmount = Math.min(MAX_STOCK, magicAmount + amount);
        }
        setChanged();
    }

    private int containerContains(AspectId aspect) {
        if (CHAOS_ASPECT.equals(aspect)) {
            return chaosAmount;
        }
        if (MAGIC_ASPECT.equals(aspect)) {
            return magicAmount;
        }
        return 0;
    }

    private boolean doesContainerAccept(AspectId aspect) {
        return CHAOS_ASPECT.equals(aspect) || MAGIC_ASPECT.equals(aspect);
    }

    // ////////////////////////////////////
    // ***** output push *****//
    // ////////////////////////////////////

    private void pushOutputToNeighbors() {
        for (Direction facing : Direction.values()) {
            BlockPos neighborPos = worldPosition.relative(facing);
            if (!level.isLoaded(neighborPos)) {
                continue;
            }
            BlockEntity neighbor = level.getBlockEntity(neighborPos);
            if (neighbor == null || neighbor instanceof EssentiaTransport) {
                // Essentia containers are input only, never item/fluid sinks.
                continue;
            }
            Direction side = facing.getOpposite();

            IItemHandler destItem = neighbor.getCapability(ForgeCapabilities.ITEM_HANDLER, side).orElse(null);
            if (destItem != null) {
                boolean moved = false;
                for (int i = 0; i < outputInventory.getSlots(); i++) {
                    ItemStack stack = outputInventory.getStackInSlot(i);
                    if (stack.isEmpty()) {
                        continue;
                    }
                    ItemStack rest = ItemHandlerHelper.insertItemStacked(destItem, stack.copy(), false);
                    if (rest.getCount() != stack.getCount()) {
                        outputInventory.setStackInSlot(i, rest);
                        moved = true;
                    }
                }
                if (moved) {
                    setChanged();
                }
            }

            if (outputTank.getFluidAmount() > 0) {
                IFluidHandler destFluid = neighbor.getCapability(ForgeCapabilities.FLUID_HANDLER, side).orElse(null);
                if (destFluid != null) {
                    FluidStack fluid = outputTank.getFluid();
                    int filled = destFluid.fill(fluid, IFluidHandler.FluidAction.EXECUTE);
                    if (filled > 0) {
                        outputTank.drain(filled, IFluidHandler.FluidAction.EXECUTE);
                        setChanged();
                    }
                }
            }
        }
    }

    // ////////////////////////////////////
    // ***** ore scan *****//
    // ////////////////////////////////////

    /** Nearest-first 3D shell scan, amber ore is used only as a fallback. */
    private void scanForOre() {
        if (!scanInitialized) {
            resetScanPosition();
            scanInitialized = true;
        }
        BlockPos fallback = null;
        for (int i = 0; i < SCAN_PER_TICK; i++) {
            if (!advanceScanPosition()) {
                resetScanPosition();
                break;
            }
            if (scanDx == 0 && scanDy == 0 && scanDz == 0) {
                continue;
            }
            BlockPos target = worldPosition.offset(scanDx, scanDy, scanDz);
            if (target.getY() < level.getMinBuildHeight() || target.getY() >= level.getMaxBuildHeight()) {
                continue;
            }
            if (!level.isLoaded(target)) {
                continue;
            }
            ItemStack item = getOreItem(target);
            if (item.isEmpty()) {
                continue;
            }
            if (isAmberOre(item)) {
                if (fallback == null) {
                    fallback = target;
                }
                continue;
            }
            pendingTarget = target;
            updatePendingOre(item);
            return;
        }
        if (pendingTarget == null) {
            if (fallback != null) {
                pendingTarget = fallback;
                updatePendingOre(getOreItem(fallback));
            } else {
                updatePendingOre(ItemStack.EMPTY);
            }
        }
    }

    private void resetScanPosition() {
        scanRing = 0;
        scanDx = 0;
        scanDy = 0;
        scanDz = -1;
    }

    /**
     * Advances the scan shell by shell: dz, then dy, then dx, then the next
     * larger shell. The first position of every new shell resets all axes to
     * {@code -ring} so the negative outer faces are not skipped.
     */
    private boolean advanceScanPosition() {
        while (true) {
            scanDz++;
            if (scanDz > scanRing) {
                scanDz = -scanRing;
                scanDy++;
                if (scanDy > scanRing) {
                    scanDy = -scanRing;
                    scanDx++;
                    if (scanDx > scanRing) {
                        scanRing++;
                        if (scanRing > MINE_RADIUS) {
                            return false;
                        }
                        scanDx = -scanRing;
                        scanDy = -scanRing;
                        scanDz = -scanRing;
                    }
                }
            }
            int maxAbs = Math.max(Math.abs(scanDx), Math.max(Math.abs(scanDy), Math.abs(scanDz)));
            if (maxAbs == scanRing) {
                return true;
            }
        }
    }

    /** Returns the block's item form when it is an ore, empty otherwise. */
    private ItemStack getOreItem(BlockPos pos) {
        if (level == null || !level.isLoaded(pos)) {
            return ItemStack.EMPTY;
        }
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(state.getBlock().asItem());
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return isOre(stack) ? stack : ItemStack.EMPTY;
    }

    /** OreDictionary replacement: forge:ores plus any {@code ores} / {@code ores/*} tag. */
    private static boolean isOre(ItemStack stack) {
        if (stack.is(Tags.Items.ORES)) {
            return true;
        }
        for (TagKey<net.minecraft.world.item.Item> tag : stack.getTags().toList()) {
            String path = tag.location().getPath();
            if (path.equals("ores") || path.startsWith("ores/")) {
                return true;
            }
        }
        return false;
    }

    private static boolean isAmberOre(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id != null && id.getPath().toLowerCase().contains("amber")) {
            return true;
        }
        for (TagKey<net.minecraft.world.item.Item> tag : stack.getTags().toList()) {
            if (tag.location().getPath().toLowerCase().contains("amber")) {
                return true;
            }
        }
        return false;
    }

    private void updatePendingOre(ItemStack stack) {
        pendingOre = stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1);
    }

    // ////////////////////////////////////
    // ***** mining *****//
    // ////////////////////////////////////

    private boolean mineBlock(BlockPos target) {
        if (!(level instanceof ServerLevel serverLevel) || !level.isLoaded(target)) {
            return true;
        }
        if (getOreItem(target).isEmpty()) {
            return true;
        }
        BlockState state = level.getBlockState(target);
        List<ItemStack> drops = Block.getDrops(state, serverLevel, target, null);
        if (drops.isEmpty()) {
            ItemStack blockItem = new ItemStack(state.getBlock().asItem());
            if (blockItem.isEmpty()) {
                return true;
            }
            drops = List.of(blockItem);
        }
        if (!canInsertAll(drops)) {
            return false;
        }
        insertDrops(drops);
        level.removeBlock(target, false);
        return true;
    }

    private boolean canInsertAll(List<ItemStack> drops) {
        for (ItemStack drop : drops) {
            if (drop.isEmpty()) {
                continue;
            }
            if (!ItemHandlerHelper.insertItemStacked(outputInventory, drop, true).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void insertDrops(List<ItemStack> drops) {
        for (ItemStack drop : drops) {
            if (drop.isEmpty()) {
                continue;
            }
            ItemHandlerHelper.insertItemStacked(outputInventory, drop, false);
        }
        setChanged();
    }

    // ////////////////////////////////////
    // ***** GUI / automation accessors *****//
    // ////////////////////////////////////

    public static int getMaxStockStatic() {
        return MAX_STOCK;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = ((mode % 3) + 3) % 3;
        setChanged();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        setChanged();
    }

    public int getChaosAmount() {
        return chaosAmount;
    }

    public int getMagicAmount() {
        return magicAmount;
    }

    public ItemStack getPendingOre() {
        return pendingOre;
    }

    public ItemStackHandler getOutputInventory() {
        return outputInventory;
    }

    public FluidTank getOutputTank() {
        return outputTank;
    }

    // ////////////////////////////////////
    // ***** capabilities *****//
    // ////////////////////////////////////

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        }
        if (capability == ForgeCapabilities.FLUID_HANDLER) {
            return fluidHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
        fluidHandler.invalidate();
    }

    // ////////////////////////////////////
    // ***** essentia transport (input only) *****//
    // ////////////////////////////////////

    @Override
    public boolean isConnectable(Direction side) {
        return true;
    }

    @Override
    public boolean canInputFrom(Direction side) {
        return true;
    }

    @Override
    public boolean canOutputTo(Direction side) {
        return false;
    }

    @Nullable
    @Override
    public AspectId suctionType(Direction side) {
        return null;
    }

    @Override
    public int suctionAmount(Direction side) {
        return 0;
    }

    @Override
    public int takeEssentia(AspectId aspect, int amount, Direction side, EssentiaTransferMode mode) {
        return 0;
    }

    @Override
    public int addEssentia(AspectId aspect, int amount, Direction side, EssentiaTransferMode mode) {
        if (amount <= 0 || !doesContainerAccept(aspect)) {
            return 0;
        }
        int before = containerContains(aspect);
        addToChamber(aspect, amount);
        return containerContains(aspect) - before;
    }

    @Nullable
    @Override
    public AspectId essentiaType(Direction side) {
        if (magicAmount > 0) {
            return MAGIC_ASPECT;
        }
        if (chaosAmount > 0) {
            return CHAOS_ASPECT;
        }
        return null;
    }

    @Override
    public int essentiaAmount(Direction side) {
        return chaosAmount + magicAmount;
    }

    @Override
    public int minimumSuction() {
        return 0;
    }

    @Override
    public boolean renderExtendedTube() {
        return false;
    }

    // ////////////////////////////////////
    // ***** NBT *****//
    // ////////////////////////////////////

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("chaosAmount", chaosAmount);
        tag.putInt("magicAmount", magicAmount);
        tag.putInt("mode", mode);
        tag.putBoolean("enabled", enabled);
        tag.putInt("mineTimer", mineTimer);
        tag.put("pendingOre", pendingOre.save(new CompoundTag()));
        tag.put("outputInventory", outputInventory.serializeNBT());
        tag.put("outputTank", outputTank.writeToNBT(new CompoundTag()));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        chaosAmount = tag.getInt("chaosAmount");
        magicAmount = tag.getInt("magicAmount");
        mode = tag.getInt("mode");
        enabled = tag.getBoolean("enabled");
        mineTimer = tag.getInt("mineTimer");
        pendingOre = ItemStack.of(tag.getCompound("pendingOre"));

        // Manual slot reads keep the 1.12 "Size" tag (16) from resizing the
        // modern 9-slot handler through ItemStackHandler#deserializeNBT.
        CompoundTag inventory = tag.getCompound("outputInventory");
        ListTag list = inventory.getList("Items", 10);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag itemTag = list.getCompound(i);
            int slot = itemTag.getInt("Slot");
            if (slot >= 0 && slot < OUTPUT_SLOTS) {
                outputInventory.setStackInSlot(slot, ItemStack.of(itemTag));
            }
        }
        if (tag.contains("outputTank")) {
            outputTank.readFromNBT(tag.getCompound("outputTank"));
        }
    }
}
