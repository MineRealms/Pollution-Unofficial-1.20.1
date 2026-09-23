package meowmel.pollution.client.handler;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import meowmel.pollution.Pollution;
import meowmel.pollution.common.item.PollutionItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModList;
import org.joml.Matrix4f;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * Renders the carried magic broom below the local player's legs while flying.
 * Port of upstream {@code MagicBroomRenderHandler}.
 */
@Mod.EventBusSubscriber(modid = Pollution.MOD_ID, value = Dist.CLIENT)
public final class MagicBroomRenderHandler {

    private static final ResourceLocation BROOM_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID, "textures/item/metaitems/magic_sweep.png");
    private static final float BROOM_HALF_SIZE = 0.85F;

    private MagicBroomRenderHandler() {
    }

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Post event) {
        Player player = event.getEntity();
        Minecraft minecraft = Minecraft.getInstance();
        if (player != minecraft.player || !player.getAbilities().flying || !hasMagicBroom(player)) {
            return;
        }
        renderBroom(player, event);
    }

    private static boolean hasMagicBroom(Player player) {
        var sweep = PollutionItems.MAGIC_SWEEP.get();
        if (player.getMainHandItem().is(sweep) || player.getOffhandItem().is(sweep)) {
            return true;
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).is(sweep)) {
                return true;
            }
        }
        return ModList.get().isLoaded("curios")
                && CuriosApi.getCuriosInventory(player)
                        .map(handler -> handler.isEquipped(sweep))
                        .orElse(false);
    }

    private static void renderBroom(Player player, RenderPlayerEvent event) {
        float partialTick = event.getPartialTick();
        float yaw = Mth.lerp(partialTick, player.yBodyRotO, player.yBodyRot);

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource bufferSource = event.getMultiBufferSource();

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.03D, 0.0D);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180.0F - yaw));

        VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityTranslucent(BROOM_TEXTURE));
        Matrix4f pose = poseStack.last().pose();
        int light = event.getPackedLight();
        // Horizontal quad under the feet; winding faces +Y (visible while looking down).
        vertex(buffer, pose, -BROOM_HALF_SIZE, 0.0F, -BROOM_HALF_SIZE, 0.0F, 1.0F, light);
        vertex(buffer, pose, -BROOM_HALF_SIZE, 0.0F, BROOM_HALF_SIZE, 0.0F, 0.0F, light);
        vertex(buffer, pose, BROOM_HALF_SIZE, 0.0F, BROOM_HALF_SIZE, 1.0F, 0.0F, light);
        vertex(buffer, pose, BROOM_HALF_SIZE, 0.0F, -BROOM_HALF_SIZE, 1.0F, 1.0F, light);

        poseStack.popPose();
    }

    private static void vertex(VertexConsumer buffer, Matrix4f pose,
                               float x, float y, float z, float u, float v, int light) {
        buffer.vertex(pose, x, y, z)
                .uv(u, v)
                .color(255, 255, 255, 255)
                .overlayCoords(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
    }
}
