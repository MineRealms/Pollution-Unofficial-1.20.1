package meowmel.pollution;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.mojang.logging.LogUtils;
import com.tterrag.registrate.providers.ProviderType;
import meowmel.pollution.api.pollution.PollutionEngine;
import meowmel.pollution.common.block.PollutionMagicBlocks;
import meowmel.pollution.common.command.PollutionCommand;
import meowmel.pollution.common.machine.PollutionMachineEvents;
import meowmel.pollution.compat.gtceu.PollutionGTAddon;
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
        meowmel.pollution.common.item.PollutionItems.init();

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
                    "Vis buffer: %s units");
            provider.add("pollution.machine.vis_hatch.tooltip.drain",
                    "Slowly drains vis from the Thaumcraft 4R network");
            provider.add("pollution.machine.vis_hatch.tooltip.buffer",
                    "Stores %s units per drain");
            provider.add("pollution.machine.infused_fluid_hatch.tooltip",
                    "Buffers infused fluids for magic multiblocks");
            provider.add("pollution.machine.flux_muffler.tooltip.recovery",
                    "Item recovery chance: %s%%");
            provider.add("pollution.machine.flux_muffler.tooltip",
                    "Magically filtered muffler: keeps byproducts out of the environment");
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
            PollutionMagicBlocks.ALL_NAMES.forEach(name -> provider.add(
                    "block.pollution." + name, PollutionMagicBlocks.displayName(name)));
        });

        MinecraftForge.EVENT_BUS.addListener(Pollution::onRegisterCommands);
        MinecraftForge.EVENT_BUS.addListener(PollutionEngine::onServerTick);

        LOGGER.info("Pollution Unofficial booting: GregTech CEu Modern x Thaumcraft 4R integration");
    }

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        PollutionCommand.register(event.getDispatcher());
    }
}
