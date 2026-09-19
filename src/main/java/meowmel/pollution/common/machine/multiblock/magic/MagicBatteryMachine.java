package meowmel.pollution.common.machine.multiblock.magic;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.common.machine.multiblock.part.EnergyHatchPartMachine;
import meowmel.pollution.common.machine.multiblock.AbstractDisplayMultiblockMachine;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import java.util.List;

/**
 * Magic battery: a multiblock energy buffer.
 *
 * <p>Upstream was a fancy display-only battery with bloom rings and progress
 * bars. The port keeps the functional core: energy from the input hatches is
 * moved into the output hatches through the machine each tick (up to a fixed
 * transfer rate), so it acts as a buffered relay between networks. The visual
 * ring/bars layer is deferred.</p>
 *
 * <p>Structure deviation: upstream additionally accepted the astral-lens and
 * tarot hatches on the casing (0..1 each); neither ability is registered in the
 * port, so they are not accepted.</p>
 */
public class MagicBatteryMachine extends AbstractDisplayMultiblockMachine {

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
        EnergyHatchPartMachine input = null;
        EnergyHatchPartMachine output = null;
        for (IMultiPart part : getParts()) {
            if (part.self() instanceof EnergyHatchPartMachine hatch) {
                if (input == null && PartAbility.INPUT_ENERGY.isApplicable(hatch.getBlockState().getBlock())) {
                    input = hatch;
                } else if (output == null
                        && PartAbility.OUTPUT_ENERGY.isApplicable(hatch.getBlockState().getBlock())) {
                    output = hatch;
                }
            }
        }
        if (input == null || output == null) {
            return;
        }
        var source = input.energyContainer;
        var target = output.energyContainer;
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

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (isFormed()) {
            long stored = 0L;
            long capacity = 0L;
            for (IMultiPart part : getParts()) {
                if (part.self() instanceof EnergyHatchPartMachine hatch) {
                    stored += hatch.energyContainer.getEnergyStored();
                    capacity += hatch.energyContainer.getEnergyCapacity();
                }
            }
            textList.add(Component.literal("Buffered Energy: " + stored + " / " + capacity + " EU"));
            textList.add(Component.literal("Transfer Rate: " + TRANSFER_RATE + " EU/t"));
        }
    }
}
