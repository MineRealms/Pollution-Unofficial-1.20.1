package meowmel.pollution.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import meowmel.pollution.common.block.tile.MineralExtractorBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.awt.Color;

/**
 * 1.20.1 block-entity renderer port of the 1.12 {@code TesrMineralExtractor}.
 *
 * <p>The original was a render-only adaptation of ChromatiCraft's mineral
 * extractor presentation: no textures or models are required because the
 * energy frame, moving edge flares, central crystal and orbit rings are all
 * generated as coloured geometry. The 1.12 {@code Tessellator}/{@code GlStateManager}
 * calls map onto {@link VertexConsumer} batches from the
 * {@link MultiBufferSource}: {@link RenderType#lines()} for the wire geometry
 * and {@link RenderType#debugQuads()} for the translucent crystal/glow quads
 * (each triangle is emitted as a quad with a duplicated vertex, which is the
 * same shape the fixed-function pipeline produced).</p>
 *
 * <p>{@link ItemMineralExtractorRenderer} shares this geometry for inventory,
 * held and dropped items, as the upstream item renderer shared its TESR.</p>
 */
public class MineralExtractorRenderer implements BlockEntityRenderer<MineralExtractorBlockEntity> {

    private static final int CUBE_EDGE_COUNT = 12;
    private static final int RING_SEGMENTS = 64;

    public MineralExtractorRenderer(BlockEntityRendererProvider.Context context) {
        // No textures or models are required; everything is generated geometry.
    }

    @Override
    public void render(MineralExtractorBlockEntity extractor, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        double time = extractor.getLevel() != null
                ? extractor.getLevel().getGameTime() + partialTick
                : System.currentTimeMillis() / 50.0D;

        renderGeometry(poseStack, bufferSource, time);
    }

    /** Renders the extractor in block-local coordinates without a live block entity. */
    public static void renderGeometry(PoseStack poseStack, MultiBufferSource bufferSource, double time) {

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);

        VertexConsumer lines = bufferSource.getBuffer(RenderType.lines());
        VertexConsumer quads = bufferSource.getBuffer(RenderType.debugQuads());

        renderEnergyFrame(lines, poseStack, time);
        renderEdgeFlares(quads, poseStack, time);
        renderOrbitRings(lines, poseStack, time);
        renderCrystal(quads, poseStack, time);

        poseStack.popPose();
    }

    // ////////////////////////////////////
    // ***** energy frame *****//
    // ////////////////////////////////////

    private static void renderEnergyFrame(VertexConsumer consumer, PoseStack poseStack, double time) {
        drawCubeEdges(consumer, poseStack, 0.485D, time, 34);
        drawCubeEdges(consumer, poseStack, 0.487D, time, 210);
    }

    private static void drawCubeEdges(VertexConsumer consumer, PoseStack poseStack,
                                      double radius, double time, int alpha) {
        double[][] corners = {
                {-radius, -radius, -radius}, {radius, -radius, -radius},
                {radius, radius, -radius}, {-radius, radius, -radius},
                {-radius, -radius, radius}, {radius, -radius, radius},
                {radius, radius, radius}, {-radius, radius, radius}
        };
        int[][] edges = {
                {0, 1}, {1, 2}, {2, 3}, {3, 0},
                {4, 5}, {5, 6}, {6, 7}, {7, 4},
                {0, 4}, {1, 5}, {2, 6}, {3, 7}
        };

        for (int i = 0; i < CUBE_EDGE_COUNT; i++) {
            int colorA = rainbow(time * 2.0D + i * 23.0D);
            int colorB = rainbow(time * 2.0D + i * 23.0D + 42.0D);
            addLine(consumer, poseStack, corners[edges[i][0]], corners[edges[i][1]], colorA, colorB, alpha);
        }
    }

    // ////////////////////////////////////
    // ***** edge flares *****//
    // ////////////////////////////////////

    private static void renderEdgeFlares(VertexConsumer consumer, PoseStack poseStack, double time) {
        double[] cursor = squareCursor(time * 0.035D);
        double px = -0.5D + cursor[0];
        double py = -0.5D + cursor[1];
        int color = rainbow(time * 4.0D);

        drawGlowPoint(consumer, poseStack, px, py, -0.505D, 0.045D, color, 190);
        drawGlowPoint(consumer, poseStack, -px, -py, -0.505D, 0.045D, color, 190);
        drawGlowPoint(consumer, poseStack, -px, py, 0.505D, 0.045D, color, 190);
        drawGlowPoint(consumer, poseStack, px, -py, 0.505D, 0.045D, color, 190);

        drawGlowPoint(consumer, poseStack, -0.505D, py, px, 0.045D, color, 190);
        drawGlowPoint(consumer, poseStack, -0.505D, -py, -px, 0.045D, color, 190);
        drawGlowPoint(consumer, poseStack, 0.505D, py, -px, 0.045D, color, 190);
        drawGlowPoint(consumer, poseStack, 0.505D, -py, px, 0.045D, color, 190);
    }

    private static double[] squareCursor(double time) {
        double phase = time % 4.0D;
        if (phase < 1.0D) {
            return new double[]{phase, 0.0D};
        }
        if (phase < 2.0D) {
            return new double[]{1.0D, phase - 1.0D};
        }
        if (phase < 3.0D) {
            return new double[]{3.0D - phase, 1.0D};
        }
        return new double[]{0.0D, 4.0D - phase};
    }

    // ////////////////////////////////////
    // ***** orbit rings *****//
    // ////////////////////////////////////

    private static void renderOrbitRings(VertexConsumer consumer, PoseStack poseStack, double time) {
        poseStack.pushPose();
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees((float) (time * 1.8D % 360.0D)));
        drawRing(consumer, poseStack, 0.37D, time, 135);
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(67.5F));
        poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees((float) (-time * 1.2D % 360.0D)));
        drawRing(consumer, poseStack, 0.31D, time + 90.0D, 110);
        poseStack.popPose();
    }

    private static void drawRing(VertexConsumer consumer, PoseStack poseStack,
                                 double radius, double time, int alpha) {
        for (int i = 0; i < RING_SEGMENTS; i++) {
            double angle = Math.PI * 2.0D * i / RING_SEGMENTS;
            double nextAngle = Math.PI * 2.0D * (i + 1) / RING_SEGMENTS;
            int color = rainbow(time * 2.0D + i * 360.0D / RING_SEGMENTS);
            int nextColor = rainbow(time * 2.0D + (i + 1) * 360.0D / RING_SEGMENTS);
            addLine(consumer, poseStack,
                    new double[]{Math.cos(angle) * radius, 0.0D, Math.sin(angle) * radius},
                    new double[]{Math.cos(nextAngle) * radius, 0.0D, Math.sin(nextAngle) * radius},
                    color, nextColor, alpha);
        }
    }

    // ////////////////////////////////////
    // ***** central crystal *****//
    // ////////////////////////////////////

    private static void renderCrystal(VertexConsumer consumer, PoseStack poseStack, double time) {
        poseStack.pushPose();
        poseStack.translate(
                Math.sin(time / 32.0D) * 0.035D,
                Math.sin(time / 18.0D) * 0.055D,
                Math.sin(time / 41.0D) * 0.035D);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees((float) (time * 2.25D % 360.0D)));
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees((float) (18.0D * Math.cos(time / 45.0D))));

        drawCrystalGeometry(consumer, poseStack, 0.18D, 0.42D, time, 155);
        poseStack.scale(0.72F, 0.72F, 0.72F);
        drawCrystalGeometry(consumer, poseStack, 0.18D, 0.42D, time + 70.0D, 235);
        poseStack.popPose();
    }

    private static void drawCrystalGeometry(VertexConsumer consumer, PoseStack poseStack,
                                            double radius, double halfHeight, double time, int alpha) {
        double middle = 0.13D;
        double[][] top = {
                {0.0D, halfHeight, 0.0D},
                {-radius, middle, -radius}, {radius, middle, -radius},
                {radius, middle, radius}, {-radius, middle, radius}
        };
        double[][] bottom = {
                {0.0D, -halfHeight, 0.0D},
                {-radius, -middle, -radius}, {radius, -middle, -radius},
                {radius, -middle, radius}, {-radius, -middle, radius}
        };

        for (int i = 0; i < 4; i++) {
            int next = 1 + (i + 1) % 4;
            int color = rainbow(time * 3.0D + i * 52.0D);
            addTriangle(consumer, poseStack, top[0], top[i + 1], top[next], color, alpha);
            addTriangle(consumer, poseStack, bottom[0], bottom[next], bottom[i + 1], color, alpha);

            addTriangle(consumer, poseStack, top[i + 1], bottom[i + 1], bottom[next], color, alpha);
            addTriangle(consumer, poseStack, top[i + 1], bottom[next], top[next], color, alpha);
        }
    }

    // ////////////////////////////////////
    // ***** geometry helpers *****//
    // ////////////////////////////////////

    private static void drawGlowPoint(VertexConsumer consumer, PoseStack poseStack,
                                      double x, double y, double z, double radius, int color, int alpha) {
        double[][] points = {
                {x, y + radius, z}, {x, y - radius, z},
                {x - radius, y, z}, {x + radius, y, z},
                {x, y, z - radius}, {x, y, z + radius}
        };
        int[][] faces = {
                {0, 2, 4}, {0, 4, 3}, {0, 3, 5}, {0, 5, 2},
                {1, 4, 2}, {1, 3, 4}, {1, 5, 3}, {1, 2, 5}
        };

        for (int[] face : faces) {
            addTriangle(consumer, poseStack, points[face[0]], points[face[1]], points[face[2]], color, alpha);
        }
    }

    private static void addLine(VertexConsumer consumer, PoseStack poseStack,
                                double[] start, double[] end, int startColor, int endColor, int alpha) {
        addLineVertex(consumer, poseStack, start[0], start[1], start[2], startColor, alpha);
        addLineVertex(consumer, poseStack, end[0], end[1], end[2], endColor, alpha);
    }

    private static void addTriangle(VertexConsumer consumer, PoseStack poseStack,
                                    double[] first, double[] second, double[] third, int color, int alpha) {
        Matrix4f pose = poseStack.last().pose();
        addQuadVertex(consumer, pose, first[0], first[1], first[2], color, alpha);
        addQuadVertex(consumer, pose, second[0], second[1], second[2], color, alpha);
        addQuadVertex(consumer, pose, third[0], third[1], third[2], color, alpha);
        addQuadVertex(consumer, pose, third[0], third[1], third[2], color, alpha);
    }

    private static void addLineVertex(VertexConsumer consumer, PoseStack poseStack,
                                      double x, double y, double z, int color, int alpha) {
        Matrix4f pose = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();
        consumer.vertex(pose, (float) x, (float) y, (float) z)
                .color((color >> 16) & 255, (color >> 8) & 255, color & 255, alpha)
                .normal(normal, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    private static void addQuadVertex(VertexConsumer consumer, Matrix4f pose,
                                      double x, double y, double z, int color, int alpha) {
        consumer.vertex(pose, (float) x, (float) y, (float) z)
                .color((color >> 16) & 255, (color >> 8) & 255, color & 255, alpha)
                .endVertex();
    }

    private static int rainbow(double phase) {
        float hue = (float) ((phase % 360.0D + 360.0D) % 360.0D / 360.0D);
        return Color.HSBtoRGB(hue, 0.78F, 1.0F) & 0xFFFFFF;
    }
}
