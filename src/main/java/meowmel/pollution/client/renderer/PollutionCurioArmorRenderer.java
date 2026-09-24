package meowmel.pollution.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import meowmel.pollution.Pollution;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

/**
 * Keeps the upstream nano/quantum head and chest armour appearance in Curios.
 * The original 128x64 textures use the normal 64x32 biped armour UV layout at
 * twice the resolution. In particular, the wings were a chest armour texture,
 * not a separate elytra model; this preserves that upstream presentation.
 */
public final class PollutionCurioArmorRenderer implements ICurioRenderer {

    private final ResourceLocation texture;
    private final boolean headSlot;
    private final HumanoidModel<LivingEntity> model;

    public PollutionCurioArmorRenderer(String textureName, boolean headSlot) {
        this.texture = ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID,
                "textures/armor/" + textureName + ".png");
        this.headSlot = headSlot;
        this.model = new HumanoidModel<>(Minecraft.getInstance().getEntityModels()
                .bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR));
    }

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(
            ItemStack stack, SlotContext slotContext, PoseStack poseStack,
            RenderLayerParent<T, M> renderLayerParent, MultiBufferSource buffers, int packedLight,
            float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
            float netHeadYaw, float headPitch) {
        LivingEntity wearer = slotContext.entity();
        if (wearer.isInvisible() || !(renderLayerParent.getModel() instanceof HumanoidModel<?>)) {
            return;
        }

        // Copy the already-animated parent pose, including sneaking, swimming,
        // flight, riding and small/slim player poses. Do not apply crouch twice.
        ICurioRenderer.followBodyRotations(wearer, model);
        model.setAllVisible(false);
        if (headSlot) {
            model.head.visible = true;
            model.hat.visible = true;
            ICurioRenderer.followHeadRotations(wearer, model.head, model.hat);
        } else {
            model.body.visible = true;
            model.leftArm.visible = true;
            model.rightArm.visible = true;
        }

        VertexConsumer vertices = ItemRenderer.getArmorFoilBuffer(buffers,
                RenderType.armorCutoutNoCull(texture), false, stack.hasFoil());
        model.renderToBuffer(poseStack, vertices, packedLight, OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F);
    }
}
