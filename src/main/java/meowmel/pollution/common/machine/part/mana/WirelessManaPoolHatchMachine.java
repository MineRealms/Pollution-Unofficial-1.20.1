package meowmel.pollution.common.machine.part.mana;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import net.minecraft.world.level.Level;

/**
 * Wireless mana pool hatch: a {@link ManaPoolHatchMachine} that additionally
 * exchanges pure Botania mana with the {@link WirelessManaNetwork}.
 *
 * <p>Upstream origin: {@code MetaTileEntityWirelessManaPoolHatch} (1.12.2).
 * The upstream tick called {@code super.update()} (adjacent transfer and
 * tick-counter handling) and then talked to {@code WirelessManager}; the port
 * does the same through {@link #tickManaTransfer()}:</p>
 * <ul>
 *   <li>Output hatch: first pushes to neighbouring receivers through the
 *       inherited logic (throttled to {@code V[tier]} per tick), then deposits
 *       the remaining output budget, {@code min(stored, transferRate -
 *       alreadyTransferred)}, into the network.</li>
 *   <li>Input hatch: while not full, requests {@code min(missing capacity,
 *       transferRate - external intake used)} from the network and feeds it
 *       through the throttled external intake; anything the buffer cannot
 *       accept is refunded to the network so no mana is destroyed.</li>
 * </ul>
 *
 * <p>The network is the global per-dimension pool-mana store of
 * {@link WirelessManaNetwork} (separate from the energy-type mana used by
 * {@link WirelessManaHatchMachine}); no team or owner filtering is applied,
 * matching upstream. Both directions are no-ops on the client and null-safe
 * when the level or network is unavailable.</p>
 */
public class WirelessManaPoolHatchMachine extends ManaPoolHatchMachine {

    public WirelessManaPoolHatchMachine(IMachineBlockEntity holder, PoolType poolType, boolean isExport) {
        super(holder, poolType, isExport);
    }

    @Override
    protected void tickManaTransfer() {
        super.tickManaTransfer();
        Level level = getLevel();
        if (level == null || level.isClientSide()) return;
        if (isExport) {
            long trans = Math.min(manaContainer.getMana(), manaContainer.getRemainingOutputTransferRate());
            if (trans <= 0L) return;
            long accepted = WirelessManaNetwork.addManaPool(level, trans);
            manaContainer.noteOutputTransfer(manaContainer.removeMana(accepted));
        } else if (!manaContainer.isFull()) {
            long trans = Math.min(getMaxMana() - getMana(), manaContainer.getRemainingExternalReceiveRate());
            if (trans <= 0L) return;
            long requested = WirelessManaNetwork.requestManaPool(level, trans);
            long accepted = manaContainer.receiveExternalMana(requested);
            if (accepted < requested) {
                WirelessManaNetwork.addManaPool(level, requested - accepted);
            }
        }
    }
}
