package meowmel.pollution.common.machine.part;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.gui.widget.ToggleButtonWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.jei.IngredientIO;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import net.minecraft.world.item.ItemStack;

/**
 * Base class of the filtered, lockable authorization hatches.
 *
 * <p>Upstream ({@code MetaTileEntityMagicItemHatch}) kept one focus slot (plus
 * optional auxiliary slots for specialised subclasses) that only accepts
 * whitelisted items and can be locked while a multiblock is running. The port
 * keeps the same semantics on top of {@link NotifiableItemStackHandler} so the
 * inventory is exposed as an item capability.</p>
 */
public abstract class MagicItemHatchMachine extends TieredPartMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            MagicItemHatchMachine.class, MultiblockPartMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    protected final NotifiableItemStackHandler inventory;

    @Persisted
    @DescSynced
    protected boolean focusLocked;

    protected MagicItemHatchMachine(IMachineBlockEntity holder, int tier) {
        this(holder, tier, 1);
    }

    protected MagicItemHatchMachine(IMachineBlockEntity holder, int tier, int slots) {
        super(holder, tier);
        int size = Math.max(1, slots);
        this.inventory = new NotifiableItemStackHandler(this, size, IO.BOTH, IO.BOTH,
                handlerSize -> new CustomItemStackHandler(handlerSize) {

                    @Override
                    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                        if (focusLocked) {
                            return stack;
                        }
                        return isAcceptedStack(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
                    }

                    @Override
                    public ItemStack extractItem(int slot, int amount, boolean simulate) {
                        return focusLocked ? ItemStack.EMPTY : super.extractItem(slot, amount, simulate);
                    }

                    @Override
                    public void setStackInSlot(int slot, ItemStack stack) {
                        if (!focusLocked && (stack.isEmpty() || isAcceptedStack(slot, stack))) {
                            super.setStackInSlot(slot, stack);
                        }
                    }
                });
    }

    protected abstract boolean isAcceptedStack(ItemStack stack);

    /** Slot-aware filter; one-slot hatches keep the single-argument form. */
    protected boolean isAcceptedStack(int slot, ItemStack stack) {
        return isAcceptedStack(stack);
    }

    protected ItemStack getFocusStack() {
        return inventory.storage.getStackInSlot(0);
    }

    protected ItemStack getAuxiliaryStack(int slot) {
        return slot > 0 && slot < inventory.storage.getSlots() ? inventory.storage.getStackInSlot(slot) :
                ItemStack.EMPTY;
    }

    public void setFocusLocked(boolean locked) {
        if (focusLocked != locked) {
            focusLocked = locked;
            markDirty();
        }
    }

    public boolean isFocusLocked() {
        return focusLocked;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    // ////////////////////////////////////
    // ***** UI *****//
    // ////////////////////////////////////

    /**
     * Slot grid mirroring GT's {@code ItemBusPartMachine}, plus the focus lock
     * toggle in GT's lock-button style.
     */
    @Override
    public Widget createUIWidget() {
        int slots = inventory.storage.getSlots();
        int rowSize = (int) Math.ceil(Math.sqrt(slots));
        if (slots == 8) {
            rowSize = 4;
        }
        int colSize = (int) Math.ceil(slots / (double) rowSize);
        var group = new WidgetGroup(0, 0, 18 * rowSize + 38, 18 * colSize + 16);
        var container = new WidgetGroup(4, 4, 18 * rowSize + 8, 18 * colSize + 8);
        int index = 0;
        for (int y = 0; y < colSize; y++) {
            for (int x = 0; x < rowSize && index < slots; x++) {
                container.addWidget(new SlotWidget(inventory.storage, index++, 4 + x * 18, 4 + y * 18, true, true)
                        .setBackgroundTexture(GuiTextures.SLOT)
                        .setIngredientIO(IngredientIO.BOTH));
            }
        }
        container.setBackground(GuiTextures.BACKGROUND_INVERSE);
        group.addWidget(container);
        group.addWidget(new ToggleButtonWidget(18 * rowSize + 20, 4, 18, 18,
                GuiTextures.BUTTON_LOCK, this::isFocusLocked, this::setFocusLocked)
                .setTooltipText("gtceu.gui.item_lock.tooltip")
                .setShouldUseBaseBackground());
        return group;
    }
}
