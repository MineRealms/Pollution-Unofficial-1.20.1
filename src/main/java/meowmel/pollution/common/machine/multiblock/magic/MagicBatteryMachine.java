package meowmel.pollution.common.machine.multiblock.magic;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.common.machine.multiblock.part.EnergyHatchPartMachine;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.List;

/**
 * Magic battery: a multiblock energy buffer.
 *
 * <p>Upstream was a fancy display-only battery with bloom rings and progress
 * bars. The port keeps the functional core: energy from the input hatches is
 * moved into the output hatches through the machine each tick (up to a fixed
 * transfer rate), so it acts as a buffered relay between networks. The visual
 * ring/bars layer is deferred.</p>
 */
public class MagicBatteryMachine extends MultiblockControllerMachine {

    private static final long TRANSFER_RATE = 1L << 20;

    private TickableSubscription tickSubscription;

    public MagicBatteryMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            tickSubscription = subscribeServerTick(this::tickBattery);
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

    private void tickBattery() {
        if (!(getLevel() instanceof ServerLevel) || !isFormed()) {
            return;
        }
        List<EnergyHatchPartMachine> hatches = new ArrayList<>();
        for (IMultiPart part : getParts()) {
            if (part.self() instanceof EnergyHatchPartMachine hatch) {
                hatches.add(hatch);
            }
        }
        if (hatches.size() < 2) {
            return;
        }
        var source = hatches.get(0).energyContainer;
        var target = hatches.get(hatches.size() - 1).energyContainer;
        long available = Math.max(0, source.getEnergyStored());
        long free = Math.max(0, target.getEnergyCapacity() - target.getEnergyStored());
        long moved = Math.min(Math.min(available, free), TRANSFER_RATE);
        if (moved <= 0) {
            return;
        }
        long removed = -source.changeEnergy(-moved);
        if (removed > 0) {
            target.changeEnergy(removed);
        }
    }

    public static BlockPattern createPattern(MultiblockMachineDefinition definition) {
        return MagicBatteryPatterns.create(definition);
    }
}
