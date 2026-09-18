package meowmel.pollution.common.machine;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import meowmel.pollution.Pollution;
import net.minecraft.resources.ResourceLocation;

/**
 * GregTech machine registration listener.
 *
 * <p>GregTech posts {@code GTCEuAPI.RegisterEvent} for machines at the end of
 * {@code GTMachines} initialisation, immediately before freezing the machine
 * registry. That is the only window where addon machines can be registered with
 * all GregTech data and models already available. Subscribed with
 * {@code modBus.addGenericListener(MachineDefinition.class, ...)}.</p>
 */
public final class PollutionMachineEvents {

    private PollutionMachineEvents() {}

    public static void onMachineRegister(GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition> event) {
        PollutionMachines.register();
        Pollution.LOGGER.info("Registered Pollution machine definitions");
    }
}
