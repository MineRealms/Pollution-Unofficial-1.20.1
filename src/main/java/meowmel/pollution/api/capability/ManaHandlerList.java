package meowmel.pollution.api.capability;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Aggregates the mana hatches of one multiblock.
 *
 * <p>Upstream origin:
 * {@code meowmel.pollution.api.capability.ipml.ManaHandlerList} (1.12.2), which
 * wrapped the ability list returned by {@code getAbilities(...)}. The modern
 * port keeps the same aggregation semantics but is built from the controller's
 * part list instead; see
 * {@code meowmel.pollution.common.machine.multiblock.mana.ManaMultiblockController}.</p>
 *
 * <p>{@link #addMana(long)} keeps the upstream contract: it returns the amount
 * that could <em>not</em> be stored (i.e. zero on full acceptance).</p>
 */
public class ManaHandlerList {

    private final List<IManaHatch> handlers;

    public ManaHandlerList(@NotNull List<IManaHatch> handlers) {
        this.handlers = handlers;
    }

    public long getMana() {
        return handlers.stream().mapToLong(IManaHatch::getMana).sum();
    }

    public long getMaxMana() {
        return handlers.stream().mapToLong(IManaHatch::getMaxMana).sum();
    }

    public boolean isFull() {
        return getMana() >= getMaxMana();
    }

    public boolean isEmpty() {
        return handlers.isEmpty();
    }

    public long addMana(long amount) {
        long mana = amount;
        for (IManaHatch handler : handlers) {
            if (mana <= 0) break;
            long toAdd = handler.getMaxMana() - handler.getMana();
            toAdd = Math.min(toAdd, mana);
            handler.receiveMana(toAdd);
            mana -= toAdd;
        }
        return mana;
    }

    public long removeMana(long amount) {
        long mana = amount;
        for (IManaHatch handler : handlers) {
            if (mana <= 0) break;
            long toRemove = handler.getMana();
            toRemove = Math.min(toRemove, mana);
            handler.consumeMana(toRemove, false);
            mana -= toRemove;
        }
        return mana;
    }

    public boolean consumeMana(long amount, boolean simulate) {
        if (amount <= 0) return true;
        if (simulate) return getMana() >= amount;
        if (amount <= getMana()) {
            removeMana(amount);
            return true;
        }
        return false;
    }

    /** Returns the lowest tier among the hatches, as upstream did. */
    public int getTier() {
        return handlers.stream().mapToInt(IManaHatch::getTier).min().orElse(1);
    }
}
