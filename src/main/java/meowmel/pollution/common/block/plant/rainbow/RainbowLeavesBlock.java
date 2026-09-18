package meowmel.pollution.common.block.plant.rainbow;

import meowmel.pollution.common.block.PollutionPlantBlocks;
import meowmel.pollution.compat.tc4r.TC4RBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Port of the 1.12 {@code BlockRainbowLeaves}: leaves with a position dependent
 * rainbow tint, colourful dye drops and slow aura maintenance.
 *
 * <p>Deviations from upstream:</p>
 * <ul>
 *   <li>TC4R only exposes flux consumption, not ambient vis restoration, so
 *       the aura maintenance is reduced to {@link TC4RBridge#scrubFlux}
 *       (1 quantum at a random target within the same 33x9x33 volume).</li>
 *   <li>The {@code SMALL} blockstate property is dropped (it only affected
 *       drops); modern {@link LeavesBlock} properties are kept.</li>
 *   <li>Fortune is not applied to the rare drops because the modern
 *       {@code getDrops} hook does not expose it.</li>
 * </ul>
 */
public class RainbowLeavesBlock extends LeavesBlock {

    public RainbowLeavesBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);
        if (!level.getBlockState(pos).is(this) || random.nextInt(32) != 0) {
            return;
        }
        BlockPos target = pos.offset(random.nextInt(33) - 16, random.nextInt(9) - 4, random.nextInt(33) - 16);
        if (!level.hasChunkAt(target)) {
            return;
        }
        TC4RBridge.scrubFlux(level, target, 1);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(5) != 0) {
            return;
        }
        int color = rainbowColor(pos);
        float red = ((color >> 16) & 255) / 255.0F;
        float green = ((color >> 8) & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;
        level.addParticle(new DustParticleOptions(new Vector3f(red, green, blue), 1.0F),
                pos.getX() + random.nextDouble(),
                pos.getY() + random.nextDouble(),
                pos.getZ() + random.nextDouble(),
                0.0, 0.0, 0.0);
    }

    public static int rainbowColor(BlockPos pos) {
        if (pos == null) {
            long phase = System.currentTimeMillis() / 45L;
            return Mth.hsvToRgb((phase % 360L) / 360.0F, 0.72F, 1.0F);
        }
        double x = pos.getX();
        double y = pos.getY() * 3.0D;
        double z = pos.getZ() + pos.getX();
        int band = Math.floorMod((int) Math.floor(Math.sqrt(x * x + y * y + z * z)), 32);
        return Mth.hsvToRgb(band / 32.0F, 0.72F, 1.0F);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        RandomSource random = params.getLevel().getRandom();
        List<ItemStack> drops = new ArrayList<>();
        int dyeCount = 1 + random.nextInt(3);
        for (int i = 0; i < dyeCount; i++) {
            drops.add(new ItemStack(DyeItem.byColor(DyeColor.byId(random.nextInt(16)))));
        }
        addRareDrop(drops, random, new ItemStack(PollutionPlantBlocks.RAINBOW_SAPLING.get()), 8000);
        addRareDrop(drops, random, new ItemStack(Items.APPLE), 1000);
        addRareDrop(drops, random, new ItemStack(Items.GOLDEN_APPLE), 4000);
        return drops;
    }

    private static void addRareDrop(List<ItemStack> drops, RandomSource random, ItemStack stack, int chance) {
        if (random.nextInt(chance) == 0) {
            drops.add(stack);
        }
    }

    @Override
    public List<ItemStack> onSheared(Player player, ItemStack item, Level level, BlockPos pos, int fortune) {
        return List.of(new ItemStack(this));
    }
}
