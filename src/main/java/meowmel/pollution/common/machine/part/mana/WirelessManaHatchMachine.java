package meowmel.pollution.common.machine.part.mana;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import net.minecraft.world.level.Level;

/**
 * Wireless mana energy hatch: a {@link ManaHatchMachine} that additionally
 * exchanges energy-type mana with the {@link WirelessManaNetwork}.
 *
 * <p>Upstream origin: {@code MetaTileEntityWirelessManaHatch} (1.12.2). The
 * upstream tick called {@code super.update()} (adjacent transfer) and then
 * talked to {@code WirelessManager}; the port does the same through
 * {@link #tickManaTransfer()}:</p>
 * <ul>
 *   <li>Output hatch: first pushes up to {@code V[tier] * amperage} per tick
 *       to neighbouring receivers through the inherited logic, then deposits
 *       everything left in its buffer (capacity {@code V[tier] * 64 *
 *       amperage}) into the network.</li>
 *   <li>Input hatch: while not full, requests {@code capacity - stored} from
 *       the network each tick and adds the accepted amount to its own buffer
 *       (capacity {@code V[tier] * 16 * amperage}).</li>
 * </ul>
 *
 * <p>The network is the global per-dimension store of
 * {@link WirelessManaNetwork}; no team or owner filtering is applied, matching
 * upstream. Both directions are no-ops on the client and null-safe when the
 * level or network is unavailable.</p>
 */
public class WirelessManaHatchMachine extends ManaHatchMachine {

    public WirelessManaHatchMachine(IMachineBlockEntity holder, int tier, int amperage, boolean isExport) {
        super(holder, tier, amperage, isExport);
    }

    @Override
    protected void tickManaTransfer() {
        super.tickManaTransfer();
        Level level = getLevel();
        if (level == null || level.isClientSide()) return;
        if (isExportHatch) {
            long stored = energyContainer.getEnergyStored();
            if (stored <= 0L) return;
            long accepted = WirelessManaNetwork.addEnergy(level, stored);
            if (accepted > 0L) {
                energyContainer.removeEnergy(accepted);
            }
        } else if (!isFull()) {
            long requested = WirelessManaNetwork.requestEnergy(level, getMaxMana() - getMana());
            if (requested > 0L) {
                energyContainer.addEnergy(requested);
            }
        }
    }
}
