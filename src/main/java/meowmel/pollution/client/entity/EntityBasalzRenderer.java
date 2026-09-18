package meowmel.pollution.client.entity;

import meowmel.pollution.Pollution;
import meowmel.pollution.common.entity.EntityBasalz;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for {@code pollution:basalz}. Geometry is the shared blaze-shaped
 * {@link ElementalRenderer}; texture is the earth elemental skin ported from
 * the 1.12.2 mod.
 */
public class EntityBasalzRenderer extends ElementalRenderer<EntityBasalz> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID, "textures/entity/basalz.png");

    public EntityBasalzRenderer(EntityRendererProvider.Context context) {
        super(context, TEXTURE);
    }
}
