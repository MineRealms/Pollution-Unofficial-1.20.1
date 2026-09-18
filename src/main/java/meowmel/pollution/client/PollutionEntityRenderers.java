package meowmel.pollution.client;

import meowmel.pollution.Pollution;
import meowmel.pollution.client.entity.EntityBasalzRenderer;
import meowmel.pollution.client.entity.EntityBlitzRenderer;
import meowmel.pollution.client.entity.EntityBlizzRenderer;
import meowmel.pollution.common.entity.PollutionEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client-only renderer registration for the Pollution entity types.
 *
 * <p>Guarded with {@link Dist#CLIENT} so the dedicated server never loads the
 * client renderer classes. This class only reacts to the mod-bus
 * {@link EntityRenderersEvent.RegisterRenderers} event, which is fired on the
 * physical client during client setup.</p>
 */
@Mod.EventBusSubscriber(modid = Pollution.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class PollutionEntityRenderers {

    private PollutionEntityRenderers() {}

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(PollutionEntities.BASALZ.get(), EntityBasalzRenderer::new);
        event.registerEntityRenderer(PollutionEntities.BLITZ.get(), EntityBlitzRenderer::new);
        event.registerEntityRenderer(PollutionEntities.BLIZZ.get(), EntityBlizzRenderer::new);
    }
}
