package meowmel.pollution.common.machine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GCYMRecipeTypes;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import meowmel.pollution.Pollution;
import meowmel.pollution.api.metatileentity.POMultiblockAbility;
import meowmel.pollution.api.recipes.PORecipeMaps;
import meowmel.pollution.common.machine.multiblock.magic.MagicAlloyBlastSmelterMachine;
import meowmel.pollution.common.machine.multiblock.magic.MagicAssemblerMachine;
import meowmel.pollution.common.machine.multiblock.magic.MagicAutoclaveMachine;
import meowmel.pollution.common.machine.multiblock.magic.MagicBatteryMachine;
import meowmel.pollution.common.machine.multiblock.magic.MagicBenderMachine;
import meowmel.pollution.common.machine.multiblock.magic.MagicBreweryMachine;
import meowmel.pollution.common.machine.multiblock.magic.MagicFusionReactorMachine;
import meowmel.pollution.common.machine.multiblock.magic.MagicLargeTurbineMachine;
import meowmel.pollution.common.machine.multiblock.magic.MagicMegaTurbineMachine;
import meowmel.pollution.common.machine.multiblock.magic.MagicCentrifugeMachine;
import meowmel.pollution.common.machine.multiblock.magic.MagicChemicalBathMachine;
import meowmel.pollution.common.machine.multiblock.magic.MagicChemicalReactorMachine;
import meowmel.pollution.common.machine.multiblock.magic.MagicCutterMachine;
import meowmel.pollution.common.machine.multiblock.magic.MagicDistilleryMachine;
import meowmel.pollution.common.machine.multiblock.magic.MagicElectricBlastFurnaceMachine;
import meowmel.pollution.common.machine.multiblock.magic.MagicElectrolyzerMachine;
import meowmel.pollution.common.machine.multiblock.magic.EssenceCollectorMachine;
import meowmel.pollution.common.machine.multiblock.magic.EssenceSmelterMachine;
import meowmel.pollution.common.machine.multiblock.magic.GtEssenceSmelterMachine;
import meowmel.pollution.common.machine.multiblock.magic.IndustrialInfusionMachine;
import meowmel.pollution.common.machine.multiblock.magic.InfusedExchangeMachine;
import meowmel.pollution.common.machine.multiblock.node.CentralVisTowerMachine;
import meowmel.pollution.common.machine.multiblock.node.LargeNodeGeneratorMachine;
import meowmel.pollution.common.machine.multiblock.node.NodeBlastFurnaceMachine;
import meowmel.pollution.common.machine.multiblock.node.NodeFusionReactorMachine;
import meowmel.pollution.common.machine.multiblock.node.NodeProducerMachine;
import meowmel.pollution.common.machine.multiblock.node.NodeWasherMachine;
import meowmel.pollution.common.machine.multiblock.magic.MagicExtruderMachine;
import meowmel.pollution.common.machine.multiblock.magic.MagicGreenHouseMachine;
import meowmel.pollution.common.machine.multiblock.magic.MagicMaceratorMachine;
import meowmel.pollution.common.machine.multiblock.magic.MagicMixerMachine;
import meowmel.pollution.common.machine.multiblock.magic.MagicSifterMachine;
import meowmel.pollution.common.machine.multiblock.magic.SmallChemicalPlantMachine;
import meowmel.pollution.common.machine.multiblock.magic.MagicSolidifierMachine;
import meowmel.pollution.common.machine.multiblock.magic.MagicWireMillMachine;
import meowmel.pollution.common.machine.part.FluxMufflerMachine;
import meowmel.pollution.common.machine.part.InfusedFluidHatchMachine;
import meowmel.pollution.common.machine.part.VisHatchMachine;
import meowmel.pollution.common.machine.part.mana.ManaHatchMachine;
import meowmel.pollution.common.machine.part.mana.ManaPoolHatchMachine;
import meowmel.pollution.common.machine.part.mana.WirelessManaHatchMachine;
import meowmel.pollution.common.machine.part.mana.WirelessManaPoolHatchMachine;
import meowmel.pollution.common.machine.single.FluxFuelCellMachine;
import meowmel.pollution.common.machine.single.FluxScrubberMachine;
import meowmel.pollution.common.machine.single.MagicEnergyAbsorberMachine;
import meowmel.pollution.common.machine.single.SolarPlateMachine;
import meowmel.pollution.common.machine.single.VisProviderMachine;
import meowmel.pollution.compat.gtceu.PollutionGTAddon;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

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
    /** Upstream mana hatch arrays held 14 tiers (LV..MAX); the port covers LV..UHV. */
    private static final int[] MANA_HATCH_TIERS = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };

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
    public static MachineDefinition[] MANA_INPUT_HATCH_1A;
    public static MachineDefinition[] MANA_INPUT_HATCH_4A;
    public static MachineDefinition[] MANA_INPUT_HATCH_16A;
    public static MachineDefinition[] MANA_INPUT_HATCH_64A;
    public static MachineDefinition[] MANA_OUTPUT_HATCH_1A;
    public static MachineDefinition[] MANA_OUTPUT_HATCH_4A;
    public static MachineDefinition[] MANA_OUTPUT_HATCH_16A;
    public static MachineDefinition[] MANA_OUTPUT_HATCH_64A;
    public static MachineDefinition[] WIRELESS_MANA_INPUT_HATCH_1A;
    public static MachineDefinition[] WIRELESS_MANA_INPUT_HATCH_4A;
    public static MachineDefinition[] WIRELESS_MANA_INPUT_HATCH_16A;
    public static MachineDefinition[] WIRELESS_MANA_INPUT_HATCH_64A;
    public static MachineDefinition[] WIRELESS_MANA_OUTPUT_HATCH_1A;
    public static MachineDefinition[] WIRELESS_MANA_OUTPUT_HATCH_4A;
    public static MachineDefinition[] WIRELESS_MANA_OUTPUT_HATCH_16A;
    public static MachineDefinition[] WIRELESS_MANA_OUTPUT_HATCH_64A;
    /** Indexed by {@link ManaPoolHatchMachine.PoolType#ordinal()}. */
    public static MachineDefinition[] MANA_POOL_INPUT_HATCH;
    public static MachineDefinition[] MANA_POOL_OUTPUT_HATCH;
    public static MachineDefinition[] WIRELESS_MANA_POOL_INPUT_HATCH;
    public static MachineDefinition[] WIRELESS_MANA_POOL_OUTPUT_HATCH;

    public static MultiblockMachineDefinition MAGIC_MACERATOR;
    public static MultiblockMachineDefinition MAGIC_BENDER;
    public static MultiblockMachineDefinition MAGIC_CENTRIFUGE;
    public static MultiblockMachineDefinition MAGIC_WIRE_MILL;
    public static MultiblockMachineDefinition MAGIC_AUTOCLAVE;
    public static MultiblockMachineDefinition MAGIC_ELECTROLYZER;
    public static MultiblockMachineDefinition MAGIC_EXTRUDER;
    public static MultiblockMachineDefinition MAGIC_MIXER;
    public static MultiblockMachineDefinition MAGIC_SIFTER;
    public static MultiblockMachineDefinition MAGIC_SOLIDIFIER;
    public static MultiblockMachineDefinition MAGIC_BREWERY;
    public static MultiblockMachineDefinition MAGIC_CUTTER;
    public static MultiblockMachineDefinition MAGIC_GREEN_HOUSE;
    public static MultiblockMachineDefinition MAGIC_ELECTRIC_BLAST_FURNACE;
    public static MultiblockMachineDefinition MAGIC_ALLOY_BLAST;
    public static MultiblockMachineDefinition MAGIC_CHEMICAL_BATH;
    public static MultiblockMachineDefinition MAGIC_CHEMICAL_REACTOR;
    public static MultiblockMachineDefinition MAGIC_DISTILLERY;
    public static MultiblockMachineDefinition MAGIC_ASSEMBLER;
    public static MultiblockMachineDefinition INFUSED_EXCHANGE;
    public static MultiblockMachineDefinition ESSENCE_SMELTER;
    public static MultiblockMachineDefinition NODE_PRODUCER;
    public static MultiblockMachineDefinition LARGE_NODE_GENERATOR;
    public static MultiblockMachineDefinition NODE_WASHER;
    public static MultiblockMachineDefinition NODE_BLAST_FURNACE;
    public static MultiblockMachineDefinition NODE_FUSION_REACTOR_LUV;
    public static MultiblockMachineDefinition NODE_FUSION_REACTOR_ZPM;
    public static MultiblockMachineDefinition NODE_FUSION_REACTOR_UV;
    public static MultiblockMachineDefinition CENTRAL_VIS_TOWER;
    public static MultiblockMachineDefinition GT_ESSENCE_SMELTER;
    public static MultiblockMachineDefinition ESSENCE_COLLECTOR;
    public static MultiblockMachineDefinition INDUSTRIAL_INFUSION;
    public static MultiblockMachineDefinition SMALL_CHEMICAL_PLANT;
    public static MultiblockMachineDefinition MAGIC_FUSION_REACTOR;
    public static MultiblockMachineDefinition MAGIC_BATTERY;
    public static MultiblockMachineDefinition MAGIC_LARGE_TURBINE;
    public static MultiblockMachineDefinition MAGIC_MEGA_TURBINE;

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

        MANA_INPUT_HATCH_1A = registerManaHatches("mana_input_hatch_1a", 1, false, false);
        MANA_INPUT_HATCH_4A = registerManaHatches("mana_input_hatch_4a", 4, false, false);
        MANA_INPUT_HATCH_16A = registerManaHatches("mana_input_hatch_16a", 16, false, false);
        MANA_INPUT_HATCH_64A = registerManaHatches("mana_input_hatch_64a", 64, false, false);
        MANA_OUTPUT_HATCH_1A = registerManaHatches("mana_output_hatch_1a", 1, true, false);
        MANA_OUTPUT_HATCH_4A = registerManaHatches("mana_output_hatch_4a", 4, true, false);
        MANA_OUTPUT_HATCH_16A = registerManaHatches("mana_output_hatch_16a", 16, true, false);
        MANA_OUTPUT_HATCH_64A = registerManaHatches("mana_output_hatch_64a", 64, true, false);
        WIRELESS_MANA_INPUT_HATCH_1A = registerManaHatches("wireless_mana_input_hatch_1a", 1, false, true);
        WIRELESS_MANA_INPUT_HATCH_4A = registerManaHatches("wireless_mana_input_hatch_4a", 4, false, true);
        WIRELESS_MANA_INPUT_HATCH_16A = registerManaHatches("wireless_mana_input_hatch_16a", 16, false, true);
        WIRELESS_MANA_INPUT_HATCH_64A = registerManaHatches("wireless_mana_input_hatch_64a", 64, false, true);
        WIRELESS_MANA_OUTPUT_HATCH_1A = registerManaHatches("wireless_mana_output_hatch_1a", 1, true, true);
        WIRELESS_MANA_OUTPUT_HATCH_4A = registerManaHatches("wireless_mana_output_hatch_4a", 4, true, true);
        WIRELESS_MANA_OUTPUT_HATCH_16A = registerManaHatches("wireless_mana_output_hatch_16a", 16, true, true);
        WIRELESS_MANA_OUTPUT_HATCH_64A = registerManaHatches("wireless_mana_output_hatch_64a", 64, true, true);

        ManaPoolHatchMachine.PoolType[] poolTypes = ManaPoolHatchMachine.PoolType.values();
        MANA_POOL_INPUT_HATCH = new MachineDefinition[poolTypes.length];
        MANA_POOL_OUTPUT_HATCH = new MachineDefinition[poolTypes.length];
        WIRELESS_MANA_POOL_INPUT_HATCH = new MachineDefinition[poolTypes.length];
        WIRELESS_MANA_POOL_OUTPUT_HATCH = new MachineDefinition[poolTypes.length];
        for (int index = 0; index < poolTypes.length; index++) {
            ManaPoolHatchMachine.PoolType poolType = poolTypes[index];
            MANA_POOL_INPUT_HATCH[index] = registerManaPoolHatch(
                    "mana_pool_input_hatch_" + poolType.getName(), poolType, false, false);
            MANA_POOL_OUTPUT_HATCH[index] = registerManaPoolHatch(
                    "mana_pool_output_hatch_" + poolType.getName(), poolType, true, false);
            WIRELESS_MANA_POOL_INPUT_HATCH[index] = registerManaPoolHatch(
                    "wireless_mana_pool_input_hatch_" + poolType.getName(), poolType, false, true);
            WIRELESS_MANA_POOL_OUTPUT_HATCH[index] = registerManaPoolHatch(
                    "wireless_mana_pool_output_hatch_" + poolType.getName(), poolType, true, true);
        }

        MAGIC_MACERATOR = magicMultiblock("magic_macerator", "Magic Macerator",
                MagicMaceratorMachine::new, MagicMaceratorMachine::createPattern,
                GTRecipeTypes.MACERATOR_RECIPES);

        MAGIC_BENDER = magicMultiblock("magic_bender", "Magic Bender",
                MagicBenderMachine::new, MagicBenderMachine::createPattern,
                GTRecipeTypes.BENDER_RECIPES, GTRecipeTypes.COMPRESSOR_RECIPES,
                GTRecipeTypes.FORMING_PRESS_RECIPES, GTRecipeTypes.FORGE_HAMMER_RECIPES);

        MAGIC_CENTRIFUGE = magicMultiblock("magic_centrifuge", "Magic Centrifuge",
                MagicCentrifugeMachine::new, MagicCentrifugeMachine::createPattern,
                GTRecipeTypes.CENTRIFUGE_RECIPES, GTRecipeTypes.THERMAL_CENTRIFUGE_RECIPES);

        MAGIC_WIRE_MILL = magicMultiblock("magic_wiremill", "Magic Wire Mill",
                MagicWireMillMachine::new, MagicWireMillMachine::createPattern,
                GTRecipeTypes.WIREMILL_RECIPES);

        MAGIC_AUTOCLAVE = magicMultiblock("magic_autoclave", "Magic Autoclave",
                MagicAutoclaveMachine::new, MagicAutoclaveMachine::createPattern,
                GTRecipeTypes.AUTOCLAVE_RECIPES);

        MAGIC_ELECTROLYZER = magicMultiblock("magic_electrolyzer", "Magic Electrolyzer",
                MagicElectrolyzerMachine::new, MagicElectrolyzerMachine::createPattern,
                GTRecipeTypes.ELECTROLYZER_RECIPES);

        MAGIC_EXTRUDER = magicMultiblock("magic_extruder", "Magic Extruder",
                MagicExtruderMachine::new, MagicExtruderMachine::createPattern,
                GTRecipeTypes.EXTRUDER_RECIPES);

        MAGIC_MIXER = magicMultiblock("magic_mixer", "Magic Mixer",
                MagicMixerMachine::new, MagicMixerMachine::createPattern,
                GTRecipeTypes.MIXER_RECIPES);

        MAGIC_SIFTER = magicMultiblock("magic_sifter", "Magic Sifter",
                MagicSifterMachine::new, MagicSifterMachine::createPattern,
                GTRecipeTypes.SIFTER_RECIPES);

        MAGIC_SOLIDIFIER = magicMultiblock("magic_solidifier", "Magic Solidifier",
                MagicSolidifierMachine::new, MagicSolidifierMachine::createPattern,
                GTRecipeTypes.FLUID_SOLIDFICATION_RECIPES, GTRecipeTypes.EXTRACTOR_RECIPES,
                GTRecipeTypes.CANNER_RECIPES);

        MAGIC_BREWERY = magicMultiblock("magic_brewery", "Magic Brewery",
                MagicBreweryMachine::new, MagicBreweryMachine::createPattern,
                GTRecipeTypes.BREWING_RECIPES, GTRecipeTypes.FERMENTING_RECIPES,
                GTRecipeTypes.FLUID_HEATER_RECIPES);

        MAGIC_CUTTER = magicMultiblock("magic_cutter", "Magic Cutter",
                MagicCutterMachine::new, MagicCutterMachine::createPattern,
                GTRecipeTypes.CUTTER_RECIPES);

        MAGIC_GREEN_HOUSE = magicMultiblock("magic_green_house", "Magic Greenhouse",
                MagicGreenHouseMachine::new, MagicGreenHouseMachine::createPattern,
                PORecipeMaps.MAGIC_GREENHOUSE_RECIPES);

        MAGIC_ELECTRIC_BLAST_FURNACE = magicMultiblock("magic_electric_blast_furnace",
                "Magic Electric Blast Furnace",
                MagicElectricBlastFurnaceMachine::new, MagicElectricBlastFurnaceMachine::createPattern,
                GTRecipeTypes.BLAST_RECIPES);

        MAGIC_ALLOY_BLAST = magicMultiblock("magic_alloy_blast", "Magic Alloy Blast Smelter",
                MagicAlloyBlastSmelterMachine::new, MagicAlloyBlastSmelterMachine::createPattern,
                PORecipeMaps.MAGIC_ALLOY_BLAST_RECIPES, GCYMRecipeTypes.ALLOY_BLAST_RECIPES);

        MAGIC_CHEMICAL_BATH = magicMultiblock("magic_chemical_bath", "Magic Chemical Bath",
                MagicChemicalBathMachine::new, MagicChemicalBathMachine::createPattern,
                GTRecipeTypes.CHEMICAL_BATH_RECIPES, GTRecipeTypes.ORE_WASHER_RECIPES);

        MAGIC_CHEMICAL_REACTOR = magicMultiblock("magic_chemical_reactor", "Magic Chemical Reactor",
                MagicChemicalReactorMachine::new, MagicChemicalReactorMachine::createPattern,
                GTRecipeTypes.CHEMICAL_RECIPES, PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES);

        MAGIC_DISTILLERY = magicMultiblock("magic_distillery", "Magic Distillery",
                MagicDistilleryMachine::new, MagicDistilleryMachine::createPattern,
                GTRecipeTypes.DISTILLATION_RECIPES, GTRecipeTypes.DISTILLERY_RECIPES);

        MAGIC_ASSEMBLER = magicMultiblock("magic_assembler", "Magic Assembler",
                MagicAssemblerMachine::new, MagicAssemblerMachine::createPattern,
                GTRecipeTypes.ASSEMBLER_RECIPES, PORecipeMaps.MAGIC_ASSEMBLER_RECIPES);

        INFUSED_EXCHANGE = PollutionGTAddon.REGISTRATE
                .multiblock("infused_exchange", InfusedExchangeMachine::new)
                .langValue("Infused Exchange")
                .rotationState(RotationState.ALL)
                .pattern(InfusedExchangeMachine::createPattern)
                .simpleModel(model("infused_exchange"))
                .register();

        ESSENCE_SMELTER = PollutionGTAddon.REGISTRATE
                .multiblock("essence_smelter", EssenceSmelterMachine::new)
                .langValue("Essence Smelter")
                .rotationState(RotationState.ALL)
                .pattern(EssenceSmelterMachine::createPattern)
                .simpleModel(model("essence_smelter"))
                .register();

        NODE_PRODUCER = PollutionGTAddon.REGISTRATE
                .multiblock("node_producer", NodeProducerMachine::new)
                .langValue("Node Producer")
                .rotationState(RotationState.ALL)
                .pattern(NodeProducerMachine::createPattern)
                .simpleModel(model("node_producer"))
                .register();

        LARGE_NODE_GENERATOR = PollutionGTAddon.REGISTRATE
                .multiblock("large_node_generator", LargeNodeGeneratorMachine::new)
                .langValue("Large Node Generator")
                .rotationState(RotationState.ALL)
                .pattern(LargeNodeGeneratorMachine::createPattern)
                .simpleModel(model("large_node_generator"))
                .register();

        NODE_WASHER = PollutionGTAddon.REGISTRATE
                .multiblock("node_washer", NodeWasherMachine::new)
                .langValue("Node Washer")
                .rotationState(RotationState.ALL)
                .pattern(NodeWasherMachine::createPattern)
                .simpleModel(model("node_washer"))
                .register();

        NODE_BLAST_FURNACE = magicMultiblock("node_blast_furnace", "Node Blast Furnace",
                NodeBlastFurnaceMachine::new, NodeBlastFurnaceMachine::createPattern,
                GTRecipeTypes.BLAST_RECIPES, PORecipeMaps.FORGE_ALCHEMY_RECIPES);

        NODE_FUSION_REACTOR_LUV = fusionReactor("luv_node_fusion_reactor", "LuV Node Fusion Reactor", 6);
        NODE_FUSION_REACTOR_ZPM = fusionReactor("zpm_node_fusion_reactor", "ZPM Node Fusion Reactor", 7);
        NODE_FUSION_REACTOR_UV = fusionReactor("uv_node_fusion_reactor", "UV Node Fusion Reactor", 8);

        CENTRAL_VIS_TOWER = PollutionGTAddon.REGISTRATE
                .multiblock("central_vis_tower", CentralVisTowerMachine::new)
                .langValue("Central Vis Tower")
                .rotationState(RotationState.ALL)
                .pattern(CentralVisTowerMachine::createPattern)
                .simpleModel(model("central_vis_tower"))
                .register();

        GT_ESSENCE_SMELTER = PollutionGTAddon.REGISTRATE
                .multiblock("gt_essence_smelter", GtEssenceSmelterMachine::new)
                .langValue("GT Essence Smelter")
                .rotationState(RotationState.ALL)
                .pattern(GtEssenceSmelterMachine::createPattern)
                .simpleModel(model("gt_essence_smelter"))
                .register();

        ESSENCE_COLLECTOR = PollutionGTAddon.REGISTRATE
                .multiblock("essence_collector", EssenceCollectorMachine::new)
                .langValue("Essence Collector")
                .rotationState(RotationState.ALL)
                .pattern(EssenceCollectorMachine::createPattern)
                .simpleModel(model("essence_collector"))
                .register();

        INDUSTRIAL_INFUSION = magicMultiblock("industrial_infusion", "Industrial Infusion",
                IndustrialInfusionMachine::new, IndustrialInfusionMachine::createPattern,
                PORecipeMaps.INDUSTRIAL_INFUSION_RECIPES);

        SMALL_CHEMICAL_PLANT = magicMultiblock("small_chemical_plant", "Small Chemical Plant",
                SmallChemicalPlantMachine::new, SmallChemicalPlantMachine::createPattern,
                GTRecipeTypes.CHEMICAL_RECIPES, GTRecipeTypes.LARGE_CHEMICAL_RECIPES,
                PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES);

        MAGIC_FUSION_REACTOR = magicMultiblock("magic_fusion_reactor", "Magic Fusion Reactor",
                MagicFusionReactorMachine::new, MagicFusionReactorMachine::createPattern,
                PORecipeMaps.MAGIC_FUSION_REACTOR);

        MAGIC_BATTERY = PollutionGTAddon.REGISTRATE
                .multiblock("magic_battery", MagicBatteryMachine::new)
                .langValue("Magic Battery")
                .rotationState(RotationState.ALL)
                .pattern(MagicBatteryMachine::createPattern)
                .simpleModel(model("magic_battery"))
                .register();

        MAGIC_LARGE_TURBINE = magicMultiblock("magic_large_turbine", "Magic Large Turbine",
                MagicLargeTurbineMachine::new, MagicLargeTurbineMachine::createPattern,
                PORecipeMaps.MAGIC_TURBINE_FUELS);

        MAGIC_MEGA_TURBINE = magicMultiblock("magic_mega_turbine", "Magic Mega Turbine",
                MagicMegaTurbineMachine::new, MagicMegaTurbineMachine::createPattern,
                PORecipeMaps.MAGIC_TURBINE_FUELS);
    }

    /**
     * Registers one amperage / IO variant of the mana energy hatch for every
     * tier, matching the upstream {@code MANA_*_HATCH_*A} arrays.
     */
    private static MachineDefinition[] registerManaHatches(String name, int amperage, boolean isExport,
                                                           boolean wireless) {
        return GTMachineUtils.registerTieredMachines(
                PollutionGTAddon.REGISTRATE,
                name,
                (holder, tier) -> wireless
                        ? new WirelessManaHatchMachine(holder, tier, amperage, isExport)
                        : new ManaHatchMachine(holder, tier, amperage, isExport),
                (tier, builder) -> builder
                        .langValue("%s %sMana %s Hatch (%dA)".formatted(
                                GTValues.VNF[tier], wireless ? "Wireless " : "",
                                isExport ? "Output" : "Input", amperage))
                        .rotationState(RotationState.ALL)
                        .abilities(isExport
                                ? POMultiblockAbility.MANA_OUTPUT_HATCH
                                : POMultiblockAbility.MANA_INPUT_HATCH)
                        .simpleModel(model(name + "_" + tierName(tier)))
                        .tooltips(manaHatchTooltips(tier, amperage, isExport, wireless))
                        .register(),
                MANA_HATCH_TIERS);
    }

    private static List<Component> manaHatchTooltips(int tier, int amperage, boolean isExport, boolean wireless) {
        List<Component> tooltips = new ArrayList<>(5);
        if (isExport) {
            tooltips.add(Component.translatable("gtceu.universal.tooltip.voltage_out",
                    GTValues.V[tier], GTValues.VNF[tier]));
            tooltips.add(Component.translatable("gtceu.universal.tooltip.amperage_out_till", amperage));
            tooltips.add(Component.translatable("gtceu.universal.tooltip.energy_storage_capacity",
                    GTValues.V[tier] * 64L * amperage));
        } else {
            tooltips.add(Component.translatable("gtceu.universal.tooltip.voltage_in",
                    GTValues.V[tier], GTValues.VNF[tier]));
            tooltips.add(Component.translatable("gtceu.universal.tooltip.amperage_in_till", amperage));
            tooltips.add(Component.translatable("gtceu.universal.tooltip.energy_storage_capacity",
                    GTValues.V[tier] * 16L * amperage));
        }
        tooltips.add(Component.translatable("pollution.machine.mana_hatch.tooltip"));
        if (wireless) {
            tooltips.add(Component.translatable("pollution.machine.wireless_mana_hatch.tooltip"));
        }
        return tooltips;
    }

    /**
     * Registers one pool type / IO variant of the mana pool hatch. The machine
     * tier comes from the pool type, exactly like upstream.
     */
    private static MachineDefinition registerManaPoolHatch(String name, ManaPoolHatchMachine.PoolType poolType,
                                                           boolean isExport, boolean wireless) {
        List<Component> tooltips = new ArrayList<>(5);
        tooltips.add(Component.translatable("pollution.machine.mana_pool_hatch.type",
                Component.translatable("pollution.machine.mana_pool_hatch.type." + poolType.getName())));
        tooltips.add(Component.translatable(isExport
                ? "pollution.machine.mana_pool_output_hatch.tooltip"
                : "pollution.machine.mana_pool_input_hatch.tooltip"));
        tooltips.add(Component.translatable("pollution.machine.mana_pool_hatch.capacity", poolType.getCapacity()));
        tooltips.add(Component.translatable("pollution.machine.mana_pool_hatch.transfer", poolType.getTransferRate()));
        if (wireless) {
            tooltips.add(Component.translatable("pollution.machine.wireless_mana_pool_hatch.tooltip"));
        }
        return PollutionGTAddon.REGISTRATE
                .machine(name, info -> wireless
                        ? new WirelessManaPoolHatchMachine(info, poolType, isExport)
                        : new ManaPoolHatchMachine(info, poolType, isExport))
                .tier(poolType.getMachineTier())
                .langValue("%s %sMana Pool %s Hatch".formatted(
                        GTValues.VNF[poolType.getMachineTier()], wireless ? "Wireless " : "",
                        isExport ? "Output" : "Input"))
                .rotationState(RotationState.ALL)
                .abilities(isExport
                        ? POMultiblockAbility.MANA_OUTPUT_POOL
                        : POMultiblockAbility.MANA_INPUT_POOL)
                .simpleModel(model(name))
                .tooltips(tooltips)
                .register();
    }

    private static MultiblockMachineDefinition fusionReactor(String name, String displayName, int tier) {
        return PollutionGTAddon.REGISTRATE
                .multiblock(name, holder -> new NodeFusionReactorMachine(holder, tier))
                .tier(tier)
                .langValue(displayName)
                .rotationState(RotationState.ALL)
                .recipeTypes(GTRecipeTypes.FUSION_RECIPES, PORecipeMaps.NODE_MAGIC_FUSION_RECIPES)
                .pattern(NodeFusionReactorMachine::createPattern)
                .simpleModel(model(name))
                .register();
    }

    private static MultiblockMachineDefinition magicMultiblock(
            String name, String displayName,
            Function<IMachineBlockEntity, ? extends MultiblockControllerMachine> factory,
            Function<MultiblockMachineDefinition, BlockPattern> pattern,
            GTRecipeType... recipeTypes) {
        return PollutionGTAddon.REGISTRATE
                .multiblock(name, factory)
                .langValue(displayName)
                .rotationState(RotationState.ALL)
                .recipeTypes(recipeTypes)
                .pattern(pattern)
                .simpleModel(model(name))
                .register();
    }

    private static String tierName(int tier) {
        return GTValues.VN[tier].toLowerCase(Locale.ROOT);
    }

    private static ResourceLocation model(String key) {
        return ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID, "block/machine/" + key);
    }

    private PollutionMachines() {}
}
