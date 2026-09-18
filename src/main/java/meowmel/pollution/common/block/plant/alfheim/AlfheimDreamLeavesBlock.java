package meowmel.pollution.common.block.plant.alfheim;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;

import java.util.List;

/**
 * Port of the 1.12 {@code BlockAlfheimDreamLeaves}: the Dreamwood-coloured
 * canopy of Alfheim's fixed Dream Tree structure.
 *
 * <p>Upstream defaulted {@code DECAYABLE=false} / {@code CHECK_DECAY=false}, so
 * the port starts with {@link #PERSISTENT} true; the modern distance-based
 * decay logic still applies once persistence is turned off. Vanilla-style leaf
 * loot is not used (no loot table): the block drops itself, matching the
 * upstream sheared drop.</p>
 */
public class AlfheimDreamLeavesBlock extends LeavesBlock {

    public AlfheimDreamLeavesBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(PERSISTENT, true));
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return List.of(new ItemStack(this));
    }
}
