package meowmel.pollution.client;

import meowmel.pollution.Pollution;
import meowmel.pollution.client.entity.EntityBasalzRenderer;
import meowmel.pollution.client.entity.EntityBlitzRenderer;
import meowmel.pollution.client.entity.EntityBlizzRenderer;
import meowmel.pollution.client.entity.PollutionSlimeRenderer;
import meowmel.pollution.common.entity.PollutionEntities;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
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
 *
 * <p>The elemental bolts use the vanilla {@link ThrownItemRenderer} with the
 * slime-ball placeholder item; upstream did not register bolt renderers at all,
 * so this is a port addition (a dedicated bolt model can replace it later).</p>
 */
@Mod.EventBusSubscriber(modid = Pollution.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class PollutionEntityRenderers {

    private PollutionEntityRenderers() {}

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(PollutionEntities.BASALZ.get(), EntityBasalzRenderer::new);
        event.registerEntityRenderer(PollutionEntities.BLITZ.get(), EntityBlitzRenderer::new);
        event.registerEntityRenderer(PollutionEntities.BLIZZ.get(), EntityBlizzRenderer::new);

        event.registerEntityRenderer(PollutionEntities.SLIME_AER.get(),
                context -> new PollutionSlimeRenderer(context, "slime_aer"));
        event.registerEntityRenderer(PollutionEntities.SLIME_IGNIS.get(),
                context -> new PollutionSlimeRenderer(context, "slime_ignis"));
        event.registerEntityRenderer(PollutionEntities.SLIME_AQUA.get(),
                context -> new PollutionSlimeRenderer(context, "slime_aqua"));
        event.registerEntityRenderer(PollutionEntities.SLIME_TERRA.get(),
                context -> new PollutionSlimeRenderer(context, "slime_terra"));
        event.registerEntityRenderer(PollutionEntities.SLIME_ORDO.get(),
                context -> new PollutionSlimeRenderer(context, "slime_ordo"));
        event.registerEntityRenderer(PollutionEntities.SLIME_PERDITIO.get(),
                context -> new PollutionSlimeRenderer(context, "slime_perditio"));

        event.registerEntityRenderer(PollutionEntities.BASALZ_BOLT.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(PollutionEntities.BLITZ_BOLT.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(PollutionEntities.BLIZZ_BOLT.get(), ThrownItemRenderer::new);
    }
}
