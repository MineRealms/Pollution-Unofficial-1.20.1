package meowmel.pollution.common.machine.part.mana;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;

/**
 * Wireless mana pool hatch.
 *
 * <p>Upstream origin: {@code MetaTileEntityWirelessManaPoolHatch} (1.12.2),
 * which exchanged pure mana through {@code WirelessManager} on top of the
 * regular pool hatch behaviour.</p>
 *
 * <p>Deviation / TODO: the 1.12 wireless manager has no modern equivalent yet,
 * so this part is registered with its own id and placeholder model but behaves
 * exactly like {@link ManaPoolHatchMachine}. Restore the wireless behaviour by
 * porting {@code WirelessManager}/{@code WirelessWorldData} to a
 * {@code SavedData} based network and overriding the tick transfer here.</p>
 */
public class WirelessManaPoolHatchMachine extends ManaPoolHatchMachine {

    public WirelessManaPoolHatchMachine(IMachineBlockEntity holder, PoolType poolType, boolean isExport) {
        super(holder, poolType, isExport);
    }
}
