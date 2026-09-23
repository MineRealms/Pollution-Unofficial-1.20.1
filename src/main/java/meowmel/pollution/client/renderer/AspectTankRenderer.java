package meowmel.pollution.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.tc4port.thaumcraft.api.aspect.AspectApi;
import dev.tc4port.thaumcraft.api.aspect.AspectDefinition;
import dev.tc4port.thaumcraft.api.aspect.AspectId;
import meowmel.pollution.common.machine.single.AspectTankBlockEntity;
import meowmel.pollution.common.machine.single.AspectTankMachine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

/**
 * 1.20.1 port of the 1.12 {@code AspectStorageRenderer}.
 *
 * <p>Shows the stored essentia as a tinted fluid level inside the tank
 * (front + top faces, matching upstream), the aspect icon on the front face
 * and the amount text below it. The fluid uses {@code debugQuads} tinted with
 * {@link AspectDefinition#color()}, reproducing upstream's fullbright
 * {@code animatedglow} look without depending on a TC texture that may be
 * atlased differently on 1.20.1.</p>
 */
public class AspectTankRenderer implements BlockEntityRenderer<AspectTankBlockEntity> {

    /** Upstream {@code partialFluidBox} insets, in block units. */
    private static final double FLUID_INSET = 0.06640625D;
    private static final double FLUID_BOTTOM = 0.12890625D;
    private static final double FLUID_X_MAX = 0.93359375D;

    public AspectTankRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(AspectTankBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        AspectTankMachine tank;
        try {
            tank = be.tank();
        } catch (ClassCastException ignored) {
            return;
        }
        AspectId aspect = tank.getStoredAspect();
        int amount = tank.getAspectAmount();
        if (aspect == null || amount <= 0 || !AspectApi.contains(aspect)) {
            return;
        }
        AspectDefinition def = AspectApi.get(aspect);
        int color = def.color();
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        double fill = Mth.clamp((double) amount / tank.getMaxAspectCapacity(), 0.0D, 1.0D);
        // Upstream fill curve: top = min(11.875 * fill + 2.0625, 14.0) / 16.0
        double topY = Math.min(11.875D * fill + 2.0625D, 14.0D) / 16.0D;

        Direction facing = be.getMetaMachine().getFrontFacing();

        poseStack.pushPose();
        renderFluid(poseStack, bufferSource, topY, r, g, b);
        poseStack.popPose();

        if (facing.getAxis() != Direction.Axis.Y) {
            poseStack.pushPose();
            renderFrontPanel(poseStack, bufferSource, def, amount, facing, packedLight);
            poseStack.popPose();
        }
    }

    private static void renderFluid(PoseStack poseStack, MultiBufferSource bufferSource,
                                    double topY, int r, int g, int b) {
        VertexConsumer quads = bufferSource.getBuffer(RenderType.debugQuads());
        Matrix4f pose = poseStack.last().pose();
        double x0 = FLUID_INSET;
        double x1 = FLUID_X_MAX;
        double y0 = FLUID_BOTTOM;
        double y1 = topY;
        double z0 = FLUID_INSET;
        double z1 = FLUID_X_MAX;
        int alpha = 190;

        // Front face (+Z) and top face (+Y), matching upstream which only lit those two.
        quad(pose, quads, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1, r, g, b, alpha);
        quad(pose, quads, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0, r, g, b, alpha);
        // Side faces so the level is visible from every angle through the glass.
        quad(pose, quads, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0, r, g, b, alpha);
        quad(pose, quads, x1, y0, z1, x1, y0, z0, x1, y1, z0, x1, y1, z1, r, g, b, alpha);
        quad(pose, quads, x1, y0, z0, x0, y0, z0, x0, y1, z0, x1, y1, z0, r, g, b, alpha);
    }

    private static void renderFrontPanel(PoseStack poseStack, MultiBufferSource bufferSource,
                                         AspectDefinition def, int amount, Direction facing,
                                         int packedLight) {
        poseStack.translate(0.5D, 0.5D, 0.5D);
        // Rotate so local +Z points out of the front face.
        float rotY = switch (facing) {
            case NORTH -> 180.0F;
            case WEST -> -90.0F;
            case EAST -> 90.0F;
            default -> 0.0F;
        };
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotY));
        // Just outside the front face.
        poseStack.translate(0.0D, 0.0D, 0.501D);

        // Aspect icon, upper half of the face.
        poseStack.pushPose();
        poseStack.translate(-0.18D, 0.02D, 0.0D);
        poseStack.scale(0.36F, 0.36F, 1.0F);
        drawIcon(poseStack, bufferSource, def, packedLight);
        poseStack.popPose();

        // Amount, lower half of the face.
        Minecraft minecraft = Minecraft.getInstance();
        String text = formatAmount(amount);
        float scale = 0.030F;
        poseStack.pushPose();
        poseStack.translate(0.0D, -0.28D, 0.001D);
        poseStack.scale(scale, -scale, scale);
        int textWidth = minecraft.font.width(text);
        minecraft.font.drawInBatch(text, -textWidth / 2.0F, 0, 0xFFFFFF, false,
                poseStack.last().pose(), bufferSource,
                net.minecraft.client.gui.Font.DisplayMode.NORMAL, 0, packedLight);
        poseStack.popPose();
    }

    private static void drawIcon(PoseStack poseStack, MultiBufferSource bufferSource,
                                 AspectDefinition def, int packedLight) {
        var texture = def.texture();
        VertexConsumer buf = bufferSource.getBuffer(RenderType.entityTranslucent(texture));
        Matrix4f pose = poseStack.last().pose();
        // Unit quad facing +Z (out of the panel), CCW when viewed from outside.
        buf.vertex(pose, 0.0F, 1.0F, 0.0F).uv(0.0F, 0.0F)
                .color(255, 255, 255, 255)
                .overlayCoords(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(0.0F, 0.0F, 1.0F).endVertex();
        buf.vertex(pose, 0.0F, 0.0F, 0.0F).uv(0.0F, 1.0F)
                .color(255, 255, 255, 255)
                .overlayCoords(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(0.0F, 0.0F, 1.0F).endVertex();
        buf.vertex(pose, 1.0F, 0.0F, 0.0F).uv(1.0F, 1.0F)
                .color(255, 255, 255, 255)
                .overlayCoords(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(0.0F, 0.0F, 1.0F).endVertex();
        buf.vertex(pose, 1.0F, 1.0F, 0.0F).uv(1.0F, 0.0F)
                .color(255, 255, 255, 255)
                .overlayCoords(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(0.0F, 0.0F, 1.0F).endVertex();
    }

    private static void quad(Matrix4f pose, VertexConsumer consumer,
                             double x0, double y0, double z0,
                             double x1, double y1, double z1,
                             double x2, double y2, double z2,
                             double x3, double y3, double z3,
                             int r, int g, int b, int a) {
        vertex(pose, consumer, x0, y0, z0, r, g, b, a);
        vertex(pose, consumer, x1, y1, z1, r, g, b, a);
        vertex(pose, consumer, x2, y2, z2, r, g, b, a);
        vertex(pose, consumer, x3, y3, z3, r, g, b, a);
    }

    private static void vertex(Matrix4f pose, VertexConsumer consumer,
                               double x, double y, double z, int r, int g, int b, int a) {
        consumer.vertex(pose, (float) x, (float) y, (float) z)
                .color(r, g, b, a)
                .endVertex();
    }

    /** Upstream {@code TextFormattingUtil.formatLongToCompactString(amount, 4)}. */
    private static String formatAmount(int amount) {
        if (amount < 10_000) {
            return Integer.toString(amount);
        }
        if (amount < 1_000_000) {
            return (amount / 1000) + "." + (amount % 1000 / 100) + "k";
        }
        if (amount < 1_000_000_000) {
            return (amount / 1_000_000) + "." + (amount % 1_000_000 / 100_000) + "M";
        }
        return (amount / 1_000_000_000) + "." + (amount % 1_000_000_000 / 100_000_000) + "G";
    }
}
