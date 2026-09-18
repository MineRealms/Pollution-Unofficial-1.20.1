package meowmel.pollution.common.machine.single;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import dev.tc4port.thaumcraft.api.aspect.VisAction;
import meowmel.pollution.PollutionConfig;
import meowmel.pollution.compat.tc4r.TC4RBridge;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/**
 * Flux promoted fuel cell: burns nearby Thaumcraft flux for EU.
 *
 * <p>Upstream ({@code MetaTileEntityFluxPromotedFuelCell}) used a GTQT fuel
 * recipe map for its UI and overclock while the actual generation happened in
 * its tick override. The port keeps the generation semantics (efficiency bands
 * 10-50 / 50-ceiling / explosion above the ceiling, {@code desiredFlux} formula
 * and explosion on over-ceiling) but runs as a plain EU emitter, since the GTQT
 * fuel maps are not ported. Nearby flux is measured by simulation of the TC4R
 * scrubber API (0..64 quanta).</p>
 */
public class FluxFuelCellMachine extends PollutionEnergyMachine {

    private static final int FLUX_SAMPLE = 64;

    private double fluxBuffer;

    public FluxFuelCellMachine(BlockEntityCreationInfo info, int tier) {
        super(info, tier);
    }

    @Override
    protected boolean isEnergyEmitter() {
        return true;
    }

    @Override
    protected void pollutionTick() {
        if (!(getLevel() instanceof ServerLevel level)) {
            return;
        }
        int flux = TC4RBridge.scrubFlux(level, getBlockPos(), FLUX_SAMPLE, VisAction.SIMULATE);
        double desired = PollutionConfig.FLUX_FUEL_CELL_FLUX_PER_TICK.get() * 4.0D + 0.05D * 4.0D * (getTier() - 1);
        double ceiling = 60.0D + 5.0D * Math.pow(4, getTier());

        if (flux >= ceiling) {
            level.explode(null,
                    getBlockPos().getX() + 0.5D, getBlockPos().getY() + 0.5D, getBlockPos().getZ() + 0.5D,
                    1.0F, Level.ExplosionInteraction.BLOCK);
            return;
        }
        if (flux < 10 || flux < desired) {
            return;
        }

        double efficiency = flux < 50 ? (5.0D - 256.0D / (1.6D * flux + 48.0D)) : 3.0D;
        fluxBuffer += desired;
        int quanta = (int) fluxBuffer;
        if (quanta <= 0) {
            return;
        }
        int removed = TC4RBridge.scrubFlux(level, getBlockPos(), quanta);
        if (removed <= 0) {
            return;
        }
        fluxBuffer -= removed;
        energyContainer.addEnergy((long) (efficiency * GTValues.V[getTier()]));
    }
}
