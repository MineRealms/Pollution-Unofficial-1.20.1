package meowmel.pollution.common.machine.single;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import dev.tc4port.thaumcraft.api.aspect.VisAction;
import meowmel.pollution.PollutionConfig;
import meowmel.pollution.compat.tc4r.TC4RBridge;
import net.minecraft.server.level.ServerLevel;

/**
 * Flux scrubber: consumes EU to remove Thaumcraft flux near the machine.
 *
 * <p>Upstream ({@code MetaTileEntityFluxClear}) additionally required a
 * filter item with durability in its input slot. The filter item family is not
 * ported yet (item phase), so the port runs EU-only for now; the rate formula
 * {@code 2^(tier-1) * fluxScrubberMultiplier} matches upstream.</p>
 */
public class FluxScrubberMachine extends PollutionEnergyMachine {

    private double scrubBuffer;

    public FluxScrubberMachine(IMachineBlockEntity info, int tier) {
        super(info, tier);
    }

    @Override
    protected void pollutionTick() {
        if (!(getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (TC4RBridge.scrubFlux(level, getPos(), 1, VisAction.SIMULATE) <= 0) {
            scrubBuffer = 0.0D;
            return;
        }
        long cost = GTValues.VA[getTier()];
        if (energyContainer.getEnergyStored() < cost) {
            return;
        }
        scrubBuffer += Math.pow(2, getTier() - 1) * PollutionConfig.FLUX_SCRUBBER_MULTIPLIER.get();
        int quanta = (int) scrubBuffer;
        if (quanta <= 0) {
            return;
        }
        int removed = TC4RBridge.scrubFlux(level, getPos(), quanta);
        if (removed <= 0) {
            return;
        }
        scrubBuffer -= removed;
        energyContainer.removeEnergy(cost);
    }
}
