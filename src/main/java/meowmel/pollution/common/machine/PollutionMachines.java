package meowmel.pollution.common.machine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import meowmel.pollution.Pollution;
import meowmel.pollution.api.metatileentity.POMultiblockAbility;
import meowmel.pollution.common.machine.part.FluxMufflerMachine;
import meowmel.pollution.common.machine.part.InfusedFluidHatchMachine;
import meowmel.pollution.common.machine.part.VisHatchMachine;
import meowmel.pollution.common.machine.single.FluxFuelCellMachine;
import meowmel.pollution.common.machine.single.FluxScrubberMachine;
import meowmel.pollution.common.machine.single.MagicEnergyAbsorberMachine;
import meowmel.pollution.common.machine.single.SolarPlateMachine;
import meowmel.pollution.common.machine.single.VisProviderMachine;
import meowmel.pollution.compat.gtceu.PollutionGTAddon;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;

/**
 * GregTech machine definitions of the Pollution port.
 *
 * <p>Registration mirrors GregTech's own addon-friendly helper
 * {@code GTMachineUtils.registerTieredMachines}, which prefixes the block id
 * with the voltage name ({@code lv_vis_generator}, ...). The solar plate family
 * is registered by an explicit loop because each machine additionally carries a
 * kind parameter.</p>
 */
public final class PollutionMachines {

    /** Upstream registered {@code AURA_GENERATORS[6]} for tiers LV..LuV. */
    private static final int[] VIS_GENERATOR_TIERS = { 1, 2, 3, 4, 5, 6 };
    /** Upstream {@code VIS_PROVIDERS[9]} for tiers LV..UHV. */
    private static final int[] VIS_PROVIDER_TIERS = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
    /** Upstream {@code MAGIC_ENERGY_ABSORBER[5]} for tiers LV..IV. */
    private static final int[] MAGIC_ENERGY_ABSORBER_TIERS = { 1, 2, 3, 4, 5 };
    /**
     * Upstream registered {@code VIS_CLEAR[4]} (LV..EV) plus {@code FLUX_CLEARS}
     * (EV, IV) with colliding ids; the port keeps one machine per tier LV..IV.
     */
    private static final int[] FLUX_SCRUBBER_TIERS = { 1, 2, 3, 4, 5 };
    /** Upstream {@code FLUX_PROMOTED_FUEL_CELL[5]} for tiers LV..IV. */
    private static final int[] FLUX_FUEL_CELL_TIERS = { 1, 2, 3, 4, 5 };
    /** Upstream solar plates: 3 tiers x 6 kinds ({@code SOLAR_PLATE[18]}). */
    private static final int[] SOLAR_PLATE_TIERS = { 1, 2, 3 };
    private static final int SOLAR_PLATE_KINDS = 6;
    /** Upstream registered 14 tiers (LV..MAX); the port covers LV..UHV for now. */
    private static final int[] VIS_HATCH_TIERS = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
    /** Upstream {@code INFUSED_FLUID_HATCH[14]}, covered LV..UHV for now. */
    private static final int[] INFUSED_FLUID_HATCH_TIERS = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
    /** Upstream {@code FLUX_MUFFLERS[9]} for tiers LV..UHV. */
    private static final int[] FLUX_MUFFLER_TIERS = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };

    public static MachineDefinition[] VIS_GENERATOR;
    public static MachineDefinition[] VIS_PROVIDER;
    public static MachineDefinition[] MAGIC_ENERGY_ABSORBER;
    public static MachineDefinition[] FLUX_SCRUBBER;
    public static MachineDefinition[] FLUX_FUEL_CELL;
    /** Indexed by kind (1..6), each entry by tier index. */
    public static MachineDefinition[][] SOLAR_PLATE;
    public static MachineDefinition[] VIS_HATCH;
    public static MachineDefinition[] INFUSED_FLUID_HATCH;
    public static MachineDefinition[] FLUX_MUFFLER;

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
                        .simpleModel(model("vis_generator_" + tierName(tier)))
                        .tooltips(
                                Component.translatable("gtceu.universal.tooltip.voltage_out",
                                        GTValues.V[tier], GTValues.VNF[tier]),
                                Component.translatable("gtceu.universal.tooltip.energy_storage_capacity",
                                        GTValues.V[tier] * 64),
                                Component.translatable("pollution.machine.vis_generator.tooltip"))
                        .register(),
                VIS_GENERATOR_TIERS);

        VIS_PROVIDER = GTMachineUtils.registerTieredMachines(
                PollutionGTAddon.REGISTRATE,
                "vis_provider",
                VisProviderMachine::new,
                (tier, builder) -> builder
                        .langValue("%s Vis Provider".formatted(GTValues.VNF[tier]))
                        .rotationState(RotationState.ALL)
                        .simpleModel(model("vis_provider_" + tierName(tier)))
                        .tooltips(
                                Component.translatable("gtceu.universal.tooltip.voltage_in",
                                        GTValues.V[tier], GTValues.VNF[tier]),
                                Component.translatable("gtceu.universal.tooltip.energy_storage_capacity",
                                        GTValues.V[tier] * 64),
                                Component.translatable("pollution.machine.vis_provider.tooltip"))
                        .register(),
                VIS_PROVIDER_TIERS);

        MAGIC_ENERGY_ABSORBER = GTMachineUtils.registerTieredMachines(
                PollutionGTAddon.REGISTRATE,
                "magic_energy_absorber",
                MagicEnergyAbsorberMachine::new,
                (tier, builder) -> builder
                        .langValue("%s Magic Energy Absorber".formatted(GTValues.VNF[tier]))
                        .rotationState(RotationState.ALL)
                        .simpleModel(model("magic_energy_absorber_" + tierName(tier)))
                        .tooltips(
                                Component.translatable("gtceu.universal.tooltip.voltage_out",
                                        GTValues.V[tier], GTValues.VNF[tier]),
                                Component.translatable("gtceu.universal.tooltip.energy_storage_capacity",
                                        GTValues.V[tier] * 64),
                                Component.translatable("pollution.machine.magic_energy_absorber.tooltip"))
                        .register(),
                MAGIC_ENERGY_ABSORBER_TIERS);

        FLUX_SCRUBBER = GTMachineUtils.registerTieredMachines(
                PollutionGTAddon.REGISTRATE,
                "flux_scrubber",
                FluxScrubberMachine::new,
                (tier, builder) -> builder
                        .langValue("%s Flux Scrubber".formatted(GTValues.VNF[tier]))
                        .rotationState(RotationState.ALL)
                        .simpleModel(model("flux_scrubber_" + tierName(tier)))
                        .tooltips(
                                Component.translatable("gtceu.universal.tooltip.voltage_in",
                                        GTValues.V[tier], GTValues.VNF[tier]),
                                Component.translatable("gtceu.universal.tooltip.energy_storage_capacity",
                                        GTValues.V[tier] * 64),
                                Component.translatable("pollution.machine.flux_scrubber.tooltip"))
                        .register(),
                FLUX_SCRUBBER_TIERS);

        FLUX_FUEL_CELL = GTMachineUtils.registerTieredMachines(
                PollutionGTAddon.REGISTRATE,
                "flux_fuel_cell",
                FluxFuelCellMachine::new,
                (tier, builder) -> builder
                        .langValue("%s Flux Promoted Fuel Cell".formatted(GTValues.VNF[tier]))
                        .rotationState(RotationState.ALL)
                        .simpleModel(model("flux_fuel_cell_" + tierName(tier)))
                        .tooltips(
                                Component.translatable("gtceu.universal.tooltip.voltage_out",
                                        GTValues.V[tier], GTValues.VNF[tier]),
                                Component.translatable("gtceu.universal.tooltip.energy_storage_capacity",
                                        GTValues.V[tier] * 64),
                                Component.translatable("pollution.machine.flux_fuel_cell.tooltip"))
                        .register(),
                FLUX_FUEL_CELL_TIERS);

        SOLAR_PLATE = new MachineDefinition[SOLAR_PLATE_KINDS + 1][];
        for (int kind = 1; kind <= SOLAR_PLATE_KINDS; kind++) {
            final int plateKind = kind;
            MachineDefinition[] perKind = new MachineDefinition[SOLAR_PLATE_TIERS.length];
            for (int index = 0; index < SOLAR_PLATE_TIERS.length; index++) {
                int tier = SOLAR_PLATE_TIERS[index];
                perKind[index] = PollutionGTAddon.REGISTRATE
                        .machine(tierName(tier) + "_solar_plate_" + kind,
                                info -> new SolarPlateMachine(info, tier, plateKind))
                        .tier(tier)
                        .langValue("%s Solar Plate MK%d".formatted(GTValues.VNF[tier], kind))
                        .rotationState(RotationState.ALL)
                        .simpleModel(model("solar_plate_" + tier + "_" + kind))
                        .tooltips(
                                Component.translatable("gtceu.universal.tooltip.voltage_out",
                                        GTValues.V[tier], GTValues.VNF[tier]),
                                Component.translatable("gtceu.universal.tooltip.energy_storage_capacity",
                                        GTValues.V[tier] * 64),
                                Component.translatable("pollution.machine.solar_plate.tooltip", kind))
                        .register();
            }
            SOLAR_PLATE[kind] = perKind;
        }

        VIS_HATCH = GTMachineUtils.registerTieredMachines(
                PollutionGTAddon.REGISTRATE,
                "vis_hatch",
                VisHatchMachine::new,
                (tier, builder) -> builder
                        .langValue("%s Vis Hatch".formatted(GTValues.VNF[tier]))
                        .rotationState(RotationState.ALL)
                        .abilities(POMultiblockAbility.VIS_HATCH)
                        .simpleModel(model("vis_hatch_" + tierName(tier)))
                        .tooltips(
                                Component.translatable("pollution.machine.vis_hatch.tooltip.capacity",
                                        tier * 2000),
                                Component.translatable("pollution.machine.vis_hatch.tooltip.drain"),
                                Component.translatable("pollution.machine.vis_hatch.tooltip.buffer", tier))
                        .register(),
                VIS_HATCH_TIERS);

        INFUSED_FLUID_HATCH = GTMachineUtils.registerTieredMachines(
                PollutionGTAddon.REGISTRATE,
                "infused_fluid_hatch",
                InfusedFluidHatchMachine::new,
                (tier, builder) -> builder
                        .langValue("%s Infused Fluid Hatch".formatted(GTValues.VNF[tier]))
                        .rotationState(RotationState.ALL)
                        .abilities(POMultiblockAbility.INFUSED_FLUID_HATCH)
                        .simpleModel(model("infused_fluid_hatch_" + tierName(tier)))
                        .tooltips(
                                Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity",
                                        InfusedFluidHatchMachine.getTankCapacity(tier)),
                                Component.translatable("pollution.machine.infused_fluid_hatch.tooltip"))
                        .register(),
                INFUSED_FLUID_HATCH_TIERS);

        FLUX_MUFFLER = GTMachineUtils.registerTieredMachines(
                PollutionGTAddon.REGISTRATE,
                "flux_muffler",
                FluxMufflerMachine::new,
                (tier, builder) -> builder
                        .langValue("%s Flux Muffler".formatted(GTValues.VNF[tier]))
                        .rotationState(RotationState.ALL)
                        .abilities(PartAbility.MUFFLER)
                        .simpleModel(model("flux_muffler_" + tierName(tier)))
                        .tooltips(
                                Component.translatable("pollution.machine.flux_muffler.tooltip.recovery",
                                        Math.min((tier - 1) * 10, 100)),
                                Component.translatable("pollution.machine.flux_muffler.tooltip"))
                        .register(),
                FLUX_MUFFLER_TIERS);
    }

    private static String tierName(int tier) {
        return GTValues.VN[tier].toLowerCase(Locale.ROOT);
    }

    private static ResourceLocation model(String key) {
        return ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID, "block/machine/" + key);
    }

    private PollutionMachines() {}
}
