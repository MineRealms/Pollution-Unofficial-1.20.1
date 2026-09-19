package meowmel.pollution;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.mojang.logging.LogUtils;
import com.tterrag.registrate.providers.ProviderType;
import meowmel.pollution.api.pollution.PollutionEngine;
import meowmel.pollution.common.PollutionCreativeTabs;
import meowmel.pollution.common.block.PollutionMagicBlocks;
import meowmel.pollution.common.block.PollutionMiscBlocks;
import meowmel.pollution.common.block.PollutionPlantBlocks;
import meowmel.pollution.common.command.PollutionCommand;
import meowmel.pollution.common.machine.PollutionMachineEvents;
import meowmel.pollution.compat.gtceu.PollutionGTAddon;
import meowmel.pollution.dimension.worldgen.PollutionStructures;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(Pollution.MOD_ID)
public final class Pollution {

    public static final String MOD_ID = "pollution";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Pollution(FMLJavaModLoadingContext context) {
        context.registerConfig(ModConfig.Type.COMMON, PollutionConfig.SPEC);

        // Register the GTCEu addon registrate on this mod's own bus.
        PollutionGTAddon.REGISTRATE.registerRegistrate();

        // Casing blocks (magic multiblocks) are plain registrate blocks and can
        // be created here, on the Pollution bus, like the machine definitions.
        PollutionMagicBlocks.init();
        PollutionPlantBlocks.init();
        PollutionMiscBlocks.init(context);
        meowmel.pollution.common.item.PollutionItems.init();
        meowmel.pollution.common.entity.PollutionEntities.init(context);
        meowmel.pollution.dimension.biome.POBiomeSources.init(context);
        PollutionStructures.init(context);
        PollutionCreativeTabs.init(context);

        // Recipe types are created from GregTech's own GTRecipeType RegisterEvent
        // (posted inside GTRecipeTypes.init, before the registry freezes).
        context.getModEventBus().addGenericListener(GTRecipeType.class,
                PollutionMachineEvents::onRecipeTypeRegister);

        // Machines are registered through GregTech's machine RegisterEvent, which
        // fires after GregTech's data is ready and before its registry freezes.
        context.getModEventBus().addGenericListener(MachineDefinition.class,
                PollutionMachineEvents::onMachineRegister);

        // Manual lang keys are generated through the registrate so they land in
        // the same datagen file as the generated material names.
        PollutionGTAddon.REGISTRATE.addDataGenerator(ProviderType.LANG, provider -> {
            provider.add("mod.pollution.name", "Pollution Unofficial");
            provider.add("itemGroup.pollution.main", "Pollution Unofficial");
            provider.add("pollution.command.get", "Chunk pollution: %s");
            provider.add("pollution.command.set", "Chunk pollution set to %s");
            provider.add("pollution.command.scrub", "Scrubbed %s pollution");
            provider.add("pollution.effect.warning", "The polluted air is making you sick");
            provider.add("pollution.machine.vis_generator.tooltip",
                    "Drains Thaumcraft vis to generate EU and industrial pollution");
            provider.add("pollution.machine.vis_provider.tooltip",
                    "Consumes EU to recharge the nearest Thaumcraft aura node");
            provider.add("pollution.machine.magic_energy_absorber.tooltip",
                    "Generates EU while a dragon egg is placed on top");
            provider.add("pollution.machine.flux_scrubber.tooltip",
                    "Consumes EU to scrub Thaumcraft flux in a 16 block radius");
            provider.add("pollution.machine.flux_fuel_cell.tooltip",
                    "Burns nearby Thaumcraft flux for EU; explodes above its flux ceiling");
            provider.add("pollution.machine.solar_plate.tooltip",
                    "Solar plate MK%s: works in daylight with a per-kind boost condition");
            provider.add("pollution.machine.vis_hatch.tooltip.capacity",
                    "Vis buffer: %s Vis");
            provider.add("pollution.machine.vis_hatch.tooltip.drain",
                    "Drains 0.05 Vis/s from the Thaumcraft 4R vis network");
            provider.add("pollution.machine.vis_hatch.tooltip.buffer",
                    "Stores %s Vis per successful drain");
            provider.add("pollution.machine.infused_fluid_hatch.tooltip",
                    "Stores infused fluids and supplies them to magic multiblock recipes");
            provider.add("pollution.machine.flux_muffler.tooltip.recovery",
                    "Item recovery chance: %s%%");
            provider.add("pollution.machine.flux_muffler.tooltip",
                    "Magically filtered muffler: recovers machine byproducts; industrial pollution is vented here");
            provider.add("pollution.magic.failure.hatches",
                    "Missing required magic hatch");
            provider.add("pollution.magic.failure.vis",
                    "Not enough vis");
            provider.add("pollution.magic.failure.infused_fluid",
                    "Not enough infused fluid");
            provider.add("pollution.magic.failure.mana",
                    "Not enough mana");
            provider.add("pollution.magic.failure.life_essence",
                    "Not enough life essence");
            provider.add("pollution.magic.failure.coil",
                    "Requires GregTech heating coils");
            provider.add("pollution.magic.failure.temperature",
                    "Required temperature: %s K");
            provider.add("pollution.magic.failure.research",
                    "Missing Thaumcraft research: %s");
            provider.add("pollution.item.vis_checker.result",
                    "Warp — permanent: %s, sticky: %s, temporary: %s");
            provider.add("entity.pollution.basalz", "Basalz");
            provider.add("entity.pollution.blitz", "Blitz");
            provider.add("entity.pollution.blizz", "Blizz");
            // Block display names are generated by the registrate itself
            // (block.pollution.<name>); adding them here again aborts datagen
            // with "Duplicate translation key".
            provider.add("pollution.flesh_heart.bound",
                    "The flesh tree has bound itself to your soul...");
            provider.add("pollution.flesh_heart.level",
                    "Flesh tree level: %s/%s");
            provider.add("pollution.flesh_heart.next_growth",
                    "Next growth needs %s LP; the soul network holds %s LP");
            provider.add("pollution.flesh_heart.other",
                    "This tree is bound to %s's soul...");
            provider.add("pollution.mineral_extractor.enabled",
                    "Mineral extractor enabled");
            provider.add("pollution.mineral_extractor.disabled",
                    "Mineral extractor disabled");
            provider.add("pollution.mineral_extractor.mode",
                    "Mineral extractor mode: %s");

            // Item behaviour tooltips (upstream metaitem behaviour pass).
            provider.add("pollution.item.packaged_aura_node.header", "Node info:");
            provider.add("pollution.armor.goggles.food",
                    "Gluttony: consumes power to restore hunger");
            provider.add("pollution.armor.goggles.water",
                    "Aquatic: night vision mode grants water breathing");
            provider.add("pollution.armor.goggles.solar",
                    "Solar: recharges in daylight while night vision is off");
            provider.add("pollution.armor.goggles.vis_discount",
                    "Vis discount: %s");
            provider.add("pollution.tarot.the_fool.tooltip",
                    "Sneak + right-click to teleport to the world spawn");
            provider.add("pollution.bauble.source", "Source: %s/%s");
            provider.add("pollution.bauble.material", "Aspect: %s");
            provider.add("pollution.filter.durability", "Durability: %s/%s");
            provider.add("pollution.filter.material", "Primary material: %s");
            provider.add("pollution.filter.tier", "Filter tier: %s");
            provider.add("pollution.filter.expected", "Expected work: %s");
            provider.add("pollution.filter.remaining", "Until breakage: %s");
            provider.add("pollution.astral_data.unattuned", "No constellation data");
            provider.add("pollution.astral_data.constellation", "Constellation: %s");
            provider.add("pollution.astral_data.function", "Celestial bias: %s");
            provider.add("pollution.astral_data.function.life", "Life cultivation");
            provider.add("pollution.astral_data.function.processing", "Processing and destruction");
            provider.add("pollution.astral_data.function.stability", "Stability and protection");
            provider.add("pollution.astral_data.function.energy", "Energy and overclocking");
            provider.add("pollution.astral_data.function.time", "Timing and calibration");
            provider.add("pollution.astral_data.function.resonance", "General starlight resonance");
            provider.add("pollution.astral_data.nbt_preserved",
                    "Constellation identity is kept as native Astral Sorcery NBT");
            provider.add("pollution.crystal_quality.unselected", "No crystal quality data");
            provider.add("pollution.crystal_quality.purity", "Crystal purity: %s");
            provider.add("pollution.crystal_quality.stability", "Cultivation stability: %s");
            provider.add("pollution.crystal_quality.embryo", "In celestial cultivation stage");
            provider.add("pollution.crystal_quality.cultivated", "Cultivated crystal");
            provider.add("pollution.crystal_quality.grade", "Lens quality: grade %s (%s%%)");
            provider.add("pollution.machine.bot_gas_collector_beamLevel", "Beam level: %s");
            provider.add("pollution.machine.bot_gas_collector_essenceConsumptionSpeed", "Essence consumption: %s mB/t");
            provider.add("pollution.machine.bot_gas_collector_finalCollectionSpeed", "Collection rate: %s mB");
            provider.add("pollution.machine.bot_gas_collector_manaConsumptionSpeed", "Mana consumption: %s");
            provider.add("pollution.machine.endoflame_array.display.flowers", "Flowers: %s");
            provider.add("pollution.machine.endoflame_array.display.fuel_cache", "Fuel cache: %s / %s tick");
            provider.add("pollution.machine.endoflame_array.display.fuel_items", "Remaining fuel items: %s");
            provider.add("pollution.machine.endoflame_array.display.mana_pool", "Mana pool: %s / %s Mana");
            provider.add("pollution.machine.endoflame_array.display.output", "Actual mana output: %s Mana/t");
            provider.add("pollution.machine.mana_generator.tooltip", "Generates EU from a nearby Botania mana pool");
            provider.add("pollution.machine.mana_hatch.capacity", "Mana buffer: %s Mana");
            provider.add("pollution.machine.mana_hatch.input_rate",
                    "Supplies the multiblock: up to %s Mana/t (%s, %sA); receives Botania mana bursts");
            provider.add("pollution.machine.mana_hatch.output_rate",
                    "Outputs to adjacent devices: up to %s Mana/t (%s, %sA)");
            provider.add("pollution.machine.mana_hatch.tooltip",
                    "Energy-type mana hatch: uses Botania mana as the GT energy interface of magic multiblocks (1 Mana = 1 EU internally)");
            provider.add("pollution.machine.mana_plate.speed", "Speed: %s | Mana: %s");
            provider.add("pollution.machine.mana_plate.tier", "Tier: %s | Mana: %s / %s");
            provider.add("pollution.machine.mana_pool_hatch.capacity", "Pure mana capacity: %s Mana");
            provider.add("pollution.machine.mana_pool_hatch.tooltip",
                    "Pure mana pool hatch: pays/receives recipe mana through IManaHatch; not a GT energy interface");
            provider.add("pollution.machine.mana_pool_hatch.transfer", "Max transfer rate: %s Mana/t");
            provider.add("pollution.machine.mana_pool_hatch.type", "Mana pool type: %s");
            provider.add("pollution.machine.mana_pool_hatch.type.diluted", "Diluted");
            provider.add("pollution.machine.mana_pool_hatch.type.mythic", "Mythic");
            provider.add("pollution.machine.mana_pool_hatch.type.normal", "Normal");
            provider.add("pollution.machine.mana_pool_input_hatch.tooltip",
                    "Mana pool input hatch: supplies the multiblock; receives mana from Botania bursts/sparks and adjacent output hatches");
            provider.add("pollution.machine.mana_pool_output_hatch.tooltip",
                    "Mana pool output hatch: receives multiblock mana and pushes it to adjacent Botania receivers or input hatches");
            provider.add("pollution.machine.mega_mana_turbine.catalyst", "Catalyst tier: %s");
            provider.add("pollution.machine.mega_mana_turbine.max_output", "Max output: %s");
            provider.add("pollution.machine.mega_mana_turbine.parallel", "Parallels: %s | Coil tier: %s");
            provider.add("pollution.machine.pollution_multi_dan_de_life_on.buffer", "Garden EU buffer: %s");
            provider.add("pollution.machine.pollution_multi_dan_de_life_on.energy", "Garden energy: %s / %s");
            provider.add("pollution.machine.pollution_multi_dan_de_life_on.mode", "Garden mode: %s");
            provider.add("pollution.machine.pollution_multi_dan_de_life_on.mode0", "Energy output");
            provider.add("pollution.machine.pollution_multi_dan_de_life_on.mode1", "Fluid output");
            provider.add("pollution.machine.wireless_mana_hatch.tooltip",
                    "Wireless mana hatch: the wireless network is not ported yet; currently behaves like a normal mana hatch");
            provider.add("pollution.machine.wireless_mana_pool_hatch.tooltip",
                    "Wireless mana pool hatch: the wireless network is not ported yet; currently behaves like a normal mana pool hatch");
            provider.add("pollution.magic.failure.catalyst", "Missing required magic catalyst");
            provider.add("pollution.modeChanged.message", "Machine mode switched");
            provider.add("pollution.item.vis_checker.tooltip",
                    "Right-click to display your Thaumcraft warp");
            provider.add("pollution.item.starstream_linker.mode.input",
                    "Starstream Linker: input mode");
            provider.add("pollution.item.starstream_linker.mode.network",
                    "Starstream Linker: network mode");
            provider.add("pollution.item.starstream_linker.tooltip.toggle",
                    "Sneak + right-click air to switch mode");
            provider.add("pollution.item.starstream_linker.tooltip.input",
                    "Input mode: link constellation towers or relays to the starstream hub");
            provider.add("pollution.item.starstream_linker.tooltip.network",
                    "Network mode: configure relays, wireless terminals and cross-dimensional gateways");
            provider.add("pollution.item.starstream_linker.unported",
                    "The starstream network is not ported yet; linking is unavailable");
            provider.add("pollution.machine.aspect_tank.tooltip",
                    "Single-block aspect storage: the front face is the aspect port; wrench sets the output face, soft mallet toggles auto-output, sneak + soft mallet toggles voiding");
            provider.add("pollution.machine.aspect_tank.help",
                    "Insert aspect ampoules, jars or other aspect containers into the input slot to store or retrieve aspects");
            provider.add("pollution.machine.aspect_tank.tooltip.auto_output",
                    "§aAuto-output enabled");
            provider.add("pollution.machine.aspect_tank.tooltip.capacity",
                    "§5Aspect capacity: §f%s");
            provider.add("pollution.machine.aspect_tank.tooltip.locked",
                    "§f%1$s §cis locked!");
            provider.add("pollution.machine.aspect_tank.tooltip.stored",
                    "§9Stored aspects: §f%1$s §7× %2$s");
            provider.add("pollution.machine.aspect_tank.tooltip.voiding",
                    "§cVoiding enabled");
            provider.add("pollution.machine.small_node_generator.tooltip",
                    "Burns packaged aura nodes to generate EU; the multiplier depends on the node's properties and the machine tier");
            provider.add("pollution.machine.source_charge.tooltip",
                    "Consumes 1 mB/t of the matching aspect fluid to restore 1 charge to the bauble");
            provider.add("pollution.jei.machine_info.title", "Magic Machine Info");
            provider.add("pollution.jei.magic_hatch.title", "Magic Hatch Info");
            provider.add("pollution.jei.magic_amplification.title", "Amplification Info");
            provider.add("pollution.jei.magic_amplification.tags", "Process tags: %s");
            provider.add("pollution.jei.magic_amplification.constellation",
                    "Constellation (wafer) amplification:");
            provider.add("pollution.jei.magic_amplification.tarot",
                    "Tarot (tarot hatch) amplification:");
            provider.add("pollution.jei.magic_amplification.footer",
                    "Install the matching constellation wafer or tarot hatch to gain the amplification");
            provider.add("pollution.jei.recipe.cost", "Magic cost: %s");
            provider.add("pollution.jei.recipe.cost.vis", "Vis %s/craft");
            provider.add("pollution.jei.recipe.cost.infused", "Infused fluid %s mB/t");
            provider.add("pollution.jei.recipe.cost.mana", "Mana %s/t");
            provider.add("pollution.jei.recipe.cost.life", "Life essence %s/t");
            provider.add("pollution.jei.recipe.gate", "Recipe gate: %s");
            provider.add("pollution.jei.recipe.gate.research", "Research %s");
            provider.add("pollution.jei.recipe.gate.tarot", "Tarot %s");
            provider.add("pollution.jei.recipe.gate.astral", "Astral condition");
            provider.add("pollution.jei.recipe.gate.catalyst", "Catalyst protection input %s");
            provider.add("pollution.jei.recipe.process_tags", "Process tags: %s");
        });

        meowmel.pollution.common.warp.PollutionWarpEvents.init();

        MinecraftForge.EVENT_BUS.addListener(Pollution::onRegisterCommands);
        MinecraftForge.EVENT_BUS.addListener(PollutionEngine::onServerTick);
        MinecraftForge.EVENT_BUS.addListener(meowmel.pollution.common.warp.WarpEventHandler::onServerTick);

        LOGGER.info("Pollution Unofficial booting: GregTech CEu Modern x Thaumcraft 4R integration");
    }

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        PollutionCommand.register(event.getDispatcher());
    }
}
