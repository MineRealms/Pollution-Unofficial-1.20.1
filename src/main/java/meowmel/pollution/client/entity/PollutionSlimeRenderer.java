package meowmel.pollution.client.entity;

import meowmel.pollution.Pollution;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SlimeRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Slime;

/**
 * Renderer for the six Thaumcraft slime variants.
 *
 * <p>Geometry and gel layer are the vanilla slime model (the upstream textures
 * are 64x32 and were drawn for exactly that UV layout); only the texture
 * location differs per aspect. Textures were ported from the 1.12.2 mod.</p>
 */
public class PollutionSlimeRenderer extends SlimeRenderer {

    private final ResourceLocation texture;

    public PollutionSlimeRenderer(EntityRendererProvider.Context context, String textureName) {
        super(context);
        this.texture = ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID,
                "textures/entity/slime/" + textureName + ".png");
    }

    @Override
    public ResourceLocation getTextureLocation(Slime entity) {
        return this.texture;
    }
}
