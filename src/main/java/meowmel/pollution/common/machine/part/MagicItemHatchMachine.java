package meowmel.pollution.common.machine.part;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
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
}
