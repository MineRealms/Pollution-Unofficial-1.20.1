package meowmel.pollution.common.menu;

import meowmel.pollution.common.block.PollutionMiscBlocks;
import meowmel.pollution.common.block.tile.MineralExtractorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nullable;

/**
 * 1.20.1 port of the 1.12 {@code ContainerMineralExtractor}.
 *
 * <p>Layout matches the legacy screen: one read-only row of nine output slots
 * at x=8/y=76, a pending-ore display slot at x=8/y=101, and the player
 * inventory at y=122 (hotbar at y=176). The legacy fake button slots are
 * replaced by modern menu buttons: the screen sends
 * {@code handleInventoryButtonClick} and {@link #clickMenuButton(Player, int)}
 * toggles the mode / run state server-side.</p>
 *
 * <p>Chamber amounts, mode and run state are synced through
 * {@link ContainerData}. The pending ore stack is mirrored into the display
 * slot handler every {@link #broadcastChanges()} so the client sees the real
 * item instead of the removed 1.12 item id/meta integers.</p>
 */
public class MineralExtractorMenu extends AbstractContainerMenu {

    public static final int MACHINE_SLOTS = 9;
    public static final int DISPLAY_SLOT = MACHINE_SLOTS;
    public static final int PLAYER_SLOT_START = DISPLAY_SLOT + 1;
    public static final int PLAYER_SLOT_END = PLAYER_SLOT_START + 36;

    public static final int BUTTON_MODE = 0;
    public static final int BUTTON_POWER = 1;

    public static final int GRID_X = 8;
    public static final int MACHINE_Y = 76;
    public static final int DISPLAY_X = 8;
    public static final int DISPLAY_Y = 101;
    public static final int PLAYER_ROW_Y = 122;
    public static final int HOTBAR_Y = 176;

    private static final int DATA_CHAOS = 0;
    private static final int DATA_MAGIC = 1;
    private static final int DATA_MODE = 2;
    private static final int DATA_ENABLED = 3;
    private static final int DATA_COUNT = 4;

    @Nullable
    private final MineralExtractorBlockEntity extractor;
    private final ContainerLevelAccess access;
    private final ItemStackHandler displayInventory = new ItemStackHandler(1);
    private final int[] clientData = new int[DATA_COUNT];

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            if (extractor != null && extractor.getLevel() != null && !extractor.getLevel().isClientSide()) {
                return switch (index) {
                    case DATA_CHAOS -> extractor.getChaosAmount();
                    case DATA_MAGIC -> extractor.getMagicAmount();
                    case DATA_MODE -> extractor.getMode();
                    case DATA_ENABLED -> extractor.isEnabled() ? 1 : 0;
                    default -> 0;
                };
            }
            return clientData[index];
        }

        @Override
        public void set(int index, int value) {
            clientData[index] = value;
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    /** Server-side constructor. */
    public MineralExtractorMenu(int containerId, Inventory playerInventory,
                                @Nullable MineralExtractorBlockEntity extractor) {
        this(containerId, playerInventory, extractor,
                extractor != null ? extractor.getOutputInventory() : new ItemStackHandler(MACHINE_SLOTS),
                extractor != null
                        ? ContainerLevelAccess.create(extractor.getLevel(), extractor.getBlockPos())
                        : ContainerLevelAccess.NULL);
        if (extractor != null) {
            this.displayInventory.setStackInSlot(0, extractor.getPendingOre().copy());
        }
    }

    /** Client-side constructor fed by {@code NetworkHooks.openScreen}. */
    public MineralExtractorMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, findClientExtractor(playerInventory, buf.readBlockPos()));
    }

    private MineralExtractorMenu(int containerId, Inventory playerInventory,
                                 @Nullable MineralExtractorBlockEntity extractor,
                                 ItemStackHandler outputHandler, ContainerLevelAccess access) {
        super(PollutionMenus.MINERAL_EXTRACTOR.get(), containerId);
        this.extractor = extractor;
        this.access = access;

        for (int i = 0; i < MACHINE_SLOTS; i++) {
            this.addSlot(new SlotItemHandler(outputHandler, i, GRID_X + i * 18, MACHINE_Y) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }
            });
        }

        this.addSlot(new SlotItemHandler(this.displayInventory, 0, DISPLAY_X, DISPLAY_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public boolean mayPickup(Player player) {
                return false;
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9,
                        GRID_X + col * 18, PLAYER_ROW_Y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, GRID_X + col * 18, HOTBAR_Y));
        }

        this.addDataSlots(this.data);
    }

    @Nullable
    private static MineralExtractorBlockEntity findClientExtractor(Inventory playerInventory, BlockPos pos) {
        return playerInventory.player.level().getBlockEntity(pos) instanceof MineralExtractorBlockEntity be
                ? be
                : null;
    }

    // ////////////////////////////////////
    // ***** sync *****//
    // ////////////////////////////////////

    @Override
    public void broadcastChanges() {
        if (extractor != null && extractor.getLevel() != null && !extractor.getLevel().isClientSide()) {
            ItemStack pending = extractor.getPendingOre();
            if (!ItemStack.matches(this.displayInventory.getStackInSlot(0), pending)) {
                this.displayInventory.setStackInSlot(0, pending.copy());
            }
        }
        super.broadcastChanges();
    }

    // ////////////////////////////////////
    // ***** buttons *****//
    // ////////////////////////////////////

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (extractor == null || !stillValid(player)) {
            return false;
        }
        switch (id) {
            case BUTTON_MODE -> {
                extractor.setMode(extractor.getMode() + 1);
                return true;
            }
            case BUTTON_POWER -> {
                extractor.setEnabled(!extractor.isEnabled());
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    // ////////////////////////////////////
    // ***** slots *****//
    // ////////////////////////////////////

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size() || !stillValid(player)) return ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index < MACHINE_SLOTS) {
            if (!this.moveItemStackTo(stack, PLAYER_SLOT_START, PLAYER_SLOT_END, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, PollutionMiscBlocks.MINERAL_EXTRACTOR.get());
    }

    // ////////////////////////////////////
    // ***** GUI accessors *****//
    // ////////////////////////////////////

    public static int getMaxStock() {
        return MineralExtractorBlockEntity.getMaxStockStatic();
    }

    public int getChaosAmount() {
        return this.data.get(DATA_CHAOS);
    }

    public int getMagicAmount() {
        return this.data.get(DATA_MAGIC);
    }

    public int getMode() {
        return this.data.get(DATA_MODE);
    }

    public boolean isEnabled() {
        return this.data.get(DATA_ENABLED) != 0;
    }

    public ItemStack getPendingOre() {
        return this.displayInventory.getStackInSlot(0);
    }
}
