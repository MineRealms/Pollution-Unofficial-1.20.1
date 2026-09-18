package meowmel.pollution;

import com.mojang.logging.LogUtils;
import meowmel.pollution.api.pollution.PollutionEngine;
import meowmel.pollution.common.command.PollutionCommand;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(Pollution.MOD_ID)
public final class Pollution {

    public static final String MOD_ID = "pollution";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Pollution() {
        FMLJavaModLoadingContext.get();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, PollutionConfig.SPEC);

        MinecraftForge.EVENT_BUS.addListener(Pollution::onRegisterCommands);
        MinecraftForge.EVENT_BUS.addListener(PollutionEngine::onServerTick);

        LOGGER.info("Pollution Unofficial booting: GregTech CEu Modern x Thaumcraft 4R integration");
    }

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        PollutionCommand.register(event.getDispatcher());
    }
}
