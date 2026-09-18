package meowmel.pollution.client;

import meowmel.pollution.Pollution;
import meowmel.pollution.client.screen.MineralExtractorScreen;
import meowmel.pollution.common.menu.PollutionMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Client-only screen registration. Guarded with {@link Dist#CLIENT} so the
 * dedicated server never loads the {@link MineralExtractorScreen} class.
 *
 * <p>Forge 1.20.1 has no menu-screen registry event, so the vanilla
 * {@link MenuScreens#register} call is queued onto the client thread during
 * {@link FMLClientSetupEvent}.</p>
 */
@Mod.EventBusSubscriber(modid = Pollution.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class PollutionMenuScreens {

    private PollutionMenuScreens() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(
                PollutionMenus.MINERAL_EXTRACTOR.get(), MineralExtractorScreen::new));
    }
}
