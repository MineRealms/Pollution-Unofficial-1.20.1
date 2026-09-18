package meowmel.pollution.common.machine.part.mana;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;

/**
 * Wireless mana energy hatch.
 *
 * <p>Upstream origin: {@code MetaTileEntityWirelessManaHatch} (1.12.2), which
 * pushed into and requested from {@code WirelessManager}, a per-dimension
 * {@code WorldSavedData} mana pool.</p>
 *
 * <p>Deviation / TODO: the 1.12 wireless manager has no modern equivalent yet,
 * so this part is registered with its own id and placeholder model but behaves
 * exactly like {@link ManaHatchMachine}. Restore the wireless behaviour by
 * porting {@code WirelessManager}/{@code WirelessWorldData} to a
 * {@code SavedData} based network and overriding the tick transfer here.</p>
 */
public class WirelessManaHatchMachine extends ManaHatchMachine {

    public WirelessManaHatchMachine(IMachineBlockEntity holder, int tier, int amperage, boolean isExport) {
        super(holder, tier, amperage, isExport);
    }
}
