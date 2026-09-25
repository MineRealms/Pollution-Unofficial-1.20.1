package meowmel.pollution.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import meowmel.pollution.dimension.PollutionDimensions;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vazkii.botania.client.render.world.SkyblockSkyRenderer;

/** The source Alfheim provider uses Botania's Garden of Glass sky. */
@Mixin(LevelRenderer.class)
public abstract class AlfheimSkyMixin {
    @Shadow private ClientLevel level;
    @Shadow private VertexBuffer starBuffer;

    @Inject(method = "renderSky", slice = @Slice(from = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/ClientLevel;getRainLevel(F)F", ordinal = 0)),
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionf;)V",
                    ordinal = 0, shift = At.Shift.AFTER))
    private void pollution$planets(PoseStack pose, Matrix4f projection, float partialTick, Camera camera,
            boolean foggy, Runnable resetFog, CallbackInfo ci) {
        if (level != null && level.dimension().equals(PollutionDimensions.ALFHEIM)) {
            SkyblockSkyRenderer.renderExtra(pose, level, partialTick, 0);
        }
    }

    @Inject(method = "renderSky", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/ClientLevel;getStarBrightness(F)F"))
    private void pollution$stars(PoseStack pose, Matrix4f projection, float partialTick, Camera camera,
            boolean foggy, Runnable resetFog, CallbackInfo ci) {
        if (level != null && level.dimension().equals(PollutionDimensions.ALFHEIM) && starBuffer != null) {
            SkyblockSkyRenderer.renderStars(starBuffer, pose, projection, partialTick, resetFog);
        }
    }
}
