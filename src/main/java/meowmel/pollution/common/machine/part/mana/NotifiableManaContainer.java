package meowmel.pollution.common.machine.part.mana;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import meowmel.pollution.api.capability.IManaHatch;

/**
 * Notifiable mana buffer trait, the modern replacement of upstream
 * {@code ManaContainer} (1.12.2).
 *
 * <p>The trait keeps the upstream capacity / stored / drain semantics and adds
 * the per-tick external receive and output throttles that upstream held inside
 * {@code MetaTileEntityManaPoolHatch}. Being a {@link MachineTrait} it is
 * persisted and synced through GregTech's field system exactly like
 * {@code NotifiableFluidTank}, which is the modern pattern the port follows.</p>
 *
 * <p>Deviation: upstream {@code ManaContainer.drainMana} returned {@code true}
 * in execute mode even when the buffer held less than the requested amount;
 * this port checks first and returns {@code false} without draining.</p>
 */
public class NotifiableManaContainer extends MachineTrait implements IManaHatch {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            NotifiableManaContainer.class);

    @Persisted
    protected long manaStored;

    protected final long manaCapacity;
    protected final int tier;
    protected final long transferRate;

    private long lastCountedTick = Long.MIN_VALUE;
    private long externalManaReceivedThisTick;
    private long outputManaTransferredThisTick;

    public NotifiableManaContainer(MetaMachine machine, long manaCapacity, int tier, long transferRate) {
        super(machine);
        this.manaCapacity = Math.max(0L, manaCapacity);
        this.tier = tier;
        this.transferRate = Math.max(0L, transferRate);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public long getMana() {
        return manaStored;
    }

    @Override
    public long getMaxMana() {
        return manaCapacity;
    }

    @Override
    public int getTier() {
        return tier;
    }

    @Override
    public boolean isFull() {
        return manaStored >= manaCapacity;
    }

    /** @return the amount of mana actually accepted */
    public long addMana(long amount) {
        if (amount <= 0L || manaCapacity <= 0L) return 0L;
        long accepted = Math.min(amount, manaCapacity - manaStored);
        if (accepted <= 0L) return 0L;
        manaStored += accepted;
        onChanged();
        return accepted;
    }

    /** @return the amount of mana actually removed */
    public long removeMana(long amount) {
        if (amount <= 0L || manaStored <= 0L) return 0L;
        long removed = Math.min(amount, manaStored);
        manaStored -= removed;
        onChanged();
        return removed;
    }

    @Override
    public void receiveMana(long mana) {
        addMana(mana);
    }

    @Override
    public boolean consumeMana(long amount, boolean simulate) {
        return drainMana(amount, simulate);
    }

    public boolean drainMana(long amount, boolean simulate) {
        if (amount <= 0L) return true;
        if (simulate) return manaStored >= amount;
        if (manaStored < amount) return false;
        removeMana(amount);
        return true;
    }

    @Override
    public boolean canReceiveManaFromBursts() {
        if (isFull()) return false;
        return getRemainingExternalReceiveRate() > 0L;
    }

    @Override
    public void receiveManaFromBursts(int mana) {
        if (!canReceiveManaFromBursts()) return;
        receiveExternalMana(mana);
    }

    /** External (Botania) intake, limited to the transfer rate per tick. */
    public long receiveExternalMana(long amount) {
        if (amount <= 0L) return 0L;
        long accepted = addMana(Math.min(amount, getRemainingExternalReceiveRate()));
        externalManaReceivedThisTick += accepted;
        return accepted;
    }

    public long getRemainingExternalReceiveRate() {
        refreshTickCounters();
        return Math.max(0L, transferRate - externalManaReceivedThisTick);
    }

    public long getRemainingOutputTransferRate() {
        refreshTickCounters();
        return Math.max(0L, transferRate - outputManaTransferredThisTick);
    }

    public void noteOutputTransfer(long amount) {
        if (amount <= 0L) return;
        refreshTickCounters();
        outputManaTransferredThisTick += amount;
    }

    private void refreshTickCounters() {
        long currentTick = getMachine().getOffsetTimer();
        if (currentTick != lastCountedTick) {
            lastCountedTick = currentTick;
            externalManaReceivedThisTick = 0L;
            outputManaTransferredThisTick = 0L;
        }
    }
}
