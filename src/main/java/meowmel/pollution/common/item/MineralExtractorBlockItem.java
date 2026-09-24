package meowmel.pollution.common.item;

import meowmel.pollution.client.renderer.ItemMineralExtractorRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

/** Connects the builtin/entity item model to the extractor's procedural renderer. */
public final class MineralExtractorBlockItem extends BlockItem {

    public MineralExtractorBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return ItemMineralExtractorRenderer.getInstance();
            }
        });
    }
}
