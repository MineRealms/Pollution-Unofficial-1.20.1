package meowmel.pollution.client;

import meowmel.pollution.Pollution;
import meowmel.pollution.client.renderer.PollutionCurioArmorRenderer;
import meowmel.pollution.common.item.PollutionItems;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

/** Curios calls these renderers for visible equipped and cosmetic accessories. */
@Mod.EventBusSubscriber(modid = Pollution.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class PollutionCuriosRenderers {

    private PollutionCuriosRenderers() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            CuriosRendererRegistry.register(PollutionItems.NANO_GOGGLES.get(),
                    () -> new PollutionCurioArmorRenderer("nano_goggles", true));
            CuriosRendererRegistry.register(PollutionItems.QUANTUM_GOGGLES.get(),
                    () -> new PollutionCurioArmorRenderer("quantum_goggles", true));
            CuriosRendererRegistry.register(PollutionItems.WING_NANO.get(),
                    () -> new PollutionCurioArmorRenderer("nanowing", false));
            CuriosRendererRegistry.register(PollutionItems.WING_QUANTUM.get(),
                    () -> new PollutionCurioArmorRenderer("quantumwing", false));
        });
    }
}
