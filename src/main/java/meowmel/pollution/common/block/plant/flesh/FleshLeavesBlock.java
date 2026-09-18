package meowmel.pollution.common.block.plant.flesh;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import meowmel.pollution.common.block.PollutionPlantBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;

import java.util.List;

/**
 * Port of the 1.12 {@code BlockFleshLeaves}: leaves of the flesh tree that
 * occasionally grow a heart fruit below them or drop rotten flesh.
 *
 * <p>Deviations from upstream: the {@code TileEntityFleshHeart} core is not
 * ported yet, so the heart-level gating (fruit at level 10, drops at level
 * 5/8) is dropped and all three outcomes are rolled from the leaves alone.
 * The primitive meat item is substituted with {@code GTMaterials.Meat} dust,
 * and the old {@code BlockLeaves} sapling/apple drops are replaced by the
 * modern self-drop (there is no loot table for these blocks).</p>
 */
public class FleshLeavesBlock extends LeavesBlock {

    public FleshLeavesBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);
        if (!level.getBlockState(pos).is(this)) {
            return;
        }
        BlockPos below = pos.below();
        if (!level.isEmptyBlock(below)) {
            return;
        }
        if (random.nextInt(100) == 0) {
            level.setBlock(below, PollutionPlantBlocks.HEART_FRUIT.get().defaultBlockState()
                    .setValue(HeartFruitBlock.AGE, 0), 3);
        } else if (random.nextInt(20) == 0) {
            popResource(level, below, new ItemStack(Items.ROTTEN_FLESH));
        } else if (random.nextInt(20) == 0) {
            ItemStack meat = ChemicalHelper.get(TagPrefix.dust, GTMaterials.Meat, 1);
            if (!meat.isEmpty()) {
                popResource(level, below, meat);
            }
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return List.of(new ItemStack(this));
    }
}
