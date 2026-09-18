package meowmel.pollution.client.entity;

import meowmel.pollution.Pollution;
import meowmel.pollution.common.entity.EntityBlizz;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for {@code pollution:blizz}. Geometry is the shared blaze-shaped
 * {@link ElementalRenderer}; texture is the ice elemental skin ported from the
 * 1.12.2 mod.
 */
public class EntityBlizzRenderer extends ElementalRenderer<EntityBlizz> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID, "textures/entity/blizz.png");

    public EntityBlizzRenderer(EntityRendererProvider.Context context) {
        super(context, TEXTURE);
    }
}
