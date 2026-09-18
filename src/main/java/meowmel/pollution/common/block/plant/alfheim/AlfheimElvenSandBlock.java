package meowmel.pollution.common.block.plant.alfheim;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;

import java.util.List;

/**
 * Port of the 1.12 {@code BlockAlfheimElvenSand}: falling sand used by the
 * Alfheim world generation.
 *
 * <p>Deviations: the 1.12 {@code canSustainPlant} override only existed to let
 * Forge {@code IPlantable} crops (Desert / Beach types) sit on the sand; that
 * API was removed in modern Forge, where plant support is expressed with block
 * tags, so the override is dropped. Drops are declared explicitly because the
 * block has no loot table.</p>
 */
public class AlfheimElvenSandBlock extends FallingBlock {

    public AlfheimElvenSandBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return List.of(new ItemStack(this));
    }
}
