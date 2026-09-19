package meowmel.pollution.common.machine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import dev.tc4port.thaumcraft.api.aspect.VisAction;
import dev.tc4port.thaumcraft.api.aspect.VisChannel;
import meowmel.pollution.PollutionConfig;
import meowmel.pollution.api.pollution.PollutionEngine;
import meowmel.pollution.common.machine.single.PollutionEnergyMachine;
import meowmel.pollution.compat.tc4r.TC4RBridge;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import java.util.List;

/**
 * Vis generator: drains Thaumcraft 4R vis from the surrounding aura, converts it
 * to EU at tier voltage and adds industrial pollution proportionally.
 *
 * <p>Upstream ({@code MetaTileEntityVisGenerator}) filled its energy buffer to
 * capacity every tick and only polluted the aura; it never actually drained vis.
 * The port keeps the configured conversion ratio
 * ({@code visGeneratorEuPerVis}, default 250 EU per vis) and the per-vis
 * pollution multiplier, but drains real vis through
 * {@link TC4RBridge#drainVis} so the generator only produces when the aura can
 * supply it.</p>
 *
 * <p>Output rate scales with tier: a fraction of a vis quantum accumulates each
 * tick until a whole quantum can be drained, so LV generates about
 * {@code 32/250} vis per tick while IV generates about {@code 8192/250}. The six
 * vis channels are consumed round-robin.</p>
 */
public class VisGeneratorMachine extends PollutionEnergyMachine {

    private final VisChannel[] channels = VisChannel.values();

    private int channelCursor;
    private double visBuffer;

    public VisGeneratorMachine(IMachineBlockEntity info, int tier) {
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

        int euPerVis = PollutionConfig.VIS_GENERATOR_EU_PER_VIS.get();
        if (euPerVis <= 0) {
            return;
        }

        // Accumulate this tier's generation rate as a fraction of one vis quantum.
        visBuffer += (double) GTValues.V[getTier()] / euPerVis;

        long room = energyContainer.getEnergyCapacity() - energyContainer.getEnergyStored();
        int quanta = (int) Math.min(visBuffer, (double) room / euPerVis);
        if (quanta <= 0) {
            // No room right now, but do not accumulate an unbounded backlog.
            visBuffer = Math.min(visBuffer, 1.0);
            return;
        }

        int drained = drainRoundRobin(level, quanta);
        if (drained <= 0) {
            visBuffer = Math.min(visBuffer, 1.0);
            return;
        }

        visBuffer -= drained;
        energyContainer.addEnergy((long) drained * euPerVis);
        PollutionEngine.add(level, getPos(),
                drained * PollutionConfig.VIS_GENERATOR_POLLUTION_MULTIPLIER.get());
    }

    private int drainRoundRobin(ServerLevel level, int quanta) {
        int remaining = quanta;
        int drained = 0;
        for (int attempt = 0; attempt < channels.length && remaining > 0; attempt++) {
            VisChannel channel = channels[channelCursor];
            channelCursor = (channelCursor + 1) % channels.length;
            int result = TC4RBridge.drainVis(level, getPos(), channel, remaining, VisAction.EXECUTE);
            drained += result;
            remaining -= result;
        }
        return drained;
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        textList.add(Component.literal("Vis Buffer: " + String.format("%.2f", visBuffer)));
        textList.add(Component.literal("EU per Vis: " + PollutionConfig.VIS_GENERATOR_EU_PER_VIS.get()));
    }
}
