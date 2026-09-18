package meowmel.pollution.common.machine.single;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;

/**
 * Shared ticking base for the ported single-block magic machines.
 *
 * <p>Upstream machines overrode {@code MetaTileEntity#update()}; in GregTech CEu
 * Modern {@code MetaMachine#serverTick()} is final, so machines subscribe a
 * server tick callback from {@code onLoad} instead.</p>
 */
public abstract class PollutionEnergyMachine extends TieredEnergyMachine {

    private TickableSubscription tickSubscription;

    protected PollutionEnergyMachine(BlockEntityCreationInfo info, int tier) {
        super(info, tier);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            tickSubscription = subscribeServerTick(this::pollutionTick);
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (tickSubscription != null) {
            tickSubscription.unsubscribe();
            tickSubscription = null;
        }
    }

    protected abstract void pollutionTick();
}
