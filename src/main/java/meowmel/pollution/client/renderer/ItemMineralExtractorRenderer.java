package meowmel.pollution.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** The upstream mineral extractor TESR item adapter, including GUI and held contexts. */
public final class ItemMineralExtractorRenderer extends BlockEntityWithoutLevelRenderer {

    private static ItemMineralExtractorRenderer instance;

    private ItemMineralExtractorRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
    }

    /** Created only when Forge first requests the client item renderer. */
    public static ItemMineralExtractorRenderer getInstance() {
        if (instance == null) {
            instance = new ItemMineralExtractorRenderer();
        }
        return instance;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                             MultiBufferSource buffers, int packedLight, int packedOverlay) {
        Minecraft minecraft = Minecraft.getInstance();
        double time = minecraft.level == null
                ? Util.getMillis() / 50.0D
                : minecraft.level.getGameTime() + minecraft.getFrameTime();
        // Item transforms come from mineral_extractor.json. This geometry has
        // no baked model or world state to cache, so resource reloads are safe.
        MineralExtractorRenderer.renderGeometry(poseStack, buffers, time);
    }
}
