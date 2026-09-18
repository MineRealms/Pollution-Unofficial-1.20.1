package meowmel.pollution.common.machine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import meowmel.pollution.Pollution;
import meowmel.pollution.compat.gtceu.PollutionGTAddon;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;

/**
 * GregTech machine definitions of the Pollution port.
 *
 * <p>Registration mirrors GregTech's own addon-friendly helper
 * {@code GTMachineUtils.registerTieredMachines}, which prefixes the block id
 * with the voltage name ({@code lv_vis_generator}, ...).</p>
 */
public final class PollutionMachines {

    /** Upstream registered {@code AURA_GENERATORS[6]} for tiers LV..LuV. */
    private static final int[] VIS_GENERATOR_TIERS = { 1, 2, 3, 4, 5, 6 };

    public static MachineDefinition[] VIS_GENERATOR;

    /**
     * Builds and registers all Pollution machines. Called from the
     * {@code GTCEuAPI.RegisterEvent} listener: GregTech posts it after its own
     * machines and models are ready, and freezes {@code GTRegistries.MACHINES}
     * afterwards. Registering earlier (e.g. from the mod constructor) forces
     * {@code GTMachineModels} to initialise before GregTech's data exists and
     * crashes.
     */
    public static void register() {
        VIS_GENERATOR = GTMachineUtils.registerTieredMachines(
                PollutionGTAddon.REGISTRATE,
                "vis_generator",
                VisGeneratorMachine::new,
                (tier, builder) -> builder
                        .langValue("%s Vis Generator".formatted(GTValues.VNF[tier]))
                        .rotationState(RotationState.ALL)
                        // Placeholder model owned by this project: parents to the GregTech
                        // generator template and uses GregTech voltage casing textures at
                        // runtime. GT's own model initialisers cannot be used here because
                        // their datagen existence checks do not see inside the GTCEu jar;
                        // see tools/generate_machine_models.py.
                        .simpleModel(model(tier))
                        .tooltips(
                                Component.translatable("gtceu.universal.tooltip.voltage_out",
                                        GTValues.V[tier], GTValues.VNF[tier]),
                                Component.translatable("gtceu.universal.tooltip.energy_storage_capacity",
                                        GTValues.V[tier] * 64),
                                Component.translatable("pollution.machine.vis_generator.tooltip"))
                        .register(),
                VIS_GENERATOR_TIERS);
    }

    private static ResourceLocation model(int tier) {
        return ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID,
                "block/machine/vis_generator_" + GTValues.VN[tier].toLowerCase(Locale.ROOT));
    }

    private PollutionMachines() {}
}
