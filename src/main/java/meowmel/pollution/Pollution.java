package meowmel.pollution;

import com.mojang.logging.LogUtils;
import com.tterrag.registrate.providers.ProviderType;
import meowmel.pollution.api.pollution.PollutionEngine;
import meowmel.pollution.common.command.PollutionCommand;
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

        // Manual lang keys are generated through the registrate so they land in
        // the same datagen file as the generated material names.
        PollutionGTAddon.REGISTRATE.addDataGenerator(ProviderType.LANG, provider -> {
            provider.add("mod.pollution.name", "Pollution Unofficial");
            provider.add("pollution.command.get", "Chunk pollution: %s");
            provider.add("pollution.command.set", "Chunk pollution set to %s");
            provider.add("pollution.command.scrub", "Scrubbed %s pollution");
            provider.add("pollution.effect.warning", "The polluted air is making you sick");
        });

        MinecraftForge.EVENT_BUS.addListener(Pollution::onRegisterCommands);
        MinecraftForge.EVENT_BUS.addListener(PollutionEngine::onServerTick);

        LOGGER.info("Pollution Unofficial booting: GregTech CEu Modern x Thaumcraft 4R integration");
    }

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        PollutionCommand.register(event.getDispatcher());
    }
}
