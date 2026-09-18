package meowmel.pollution.client.entity;

import meowmel.pollution.common.entity.EntityElemental;
import net.minecraft.client.model.BlazeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Shared renderer for the three elementals ported from the 1.12.2 Pollution mod
 * (Basalz, Blitz, Blizz).
 *
 * <p>The upstream client used a custom {@code ModelElemental} whose geometry is
 * identical to the vanilla blaze (an 8x8x8 head plus twelve 2x8x2 rods on the
 * same 64x32 UV layout), so this port reuses {@link BlazeModel} rather than
 * shipping a custom modern {@code EntityModel}. The textures were ported from
 * the upstream mod and were drawn for exactly that UV layout, so no vanilla
 * blaze placeholder texture is needed.</p>
 *
 * <p>TODO: port {@code ModelElemental} as a dedicated model if the elementals
 * later need bespoke animation or extra parts, and port the upstream xmas
 * texture variants ({@code basalz_xmas.png} / {@code blitz_xmas.png} /
 * {@code blizz_xmas.png}) once seasonal texture swapping is implemented.</p>
 */
public abstract class ElementalRenderer<T extends EntityElemental> extends MobRenderer<T, BlazeModel<T>> {

    private final ResourceLocation texture;

    protected ElementalRenderer(EntityRendererProvider.Context context, ResourceLocation texture) {
        super(context, new BlazeModel<>(context.bakeLayer(ModelLayers.BLAZE)), 0.5F);
        this.texture = texture;
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return this.texture;
    }
}
