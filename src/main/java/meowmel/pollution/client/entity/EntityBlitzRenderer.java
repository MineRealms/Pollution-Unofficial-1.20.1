package meowmel.pollution.client.entity;

import meowmel.pollution.Pollution;
import meowmel.pollution.common.entity.EntityBlitz;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for {@code pollution:blitz}. Geometry is the shared blaze-shaped
 * {@link ElementalRenderer}; texture is the air/lightning elemental skin ported
 * from the 1.12.2 mod.
 */
public class EntityBlitzRenderer extends ElementalRenderer<EntityBlitz> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID, "textures/entity/blitz.png");

    public EntityBlitzRenderer(EntityRendererProvider.Context context) {
        super(context, TEXTURE);
    }
}
