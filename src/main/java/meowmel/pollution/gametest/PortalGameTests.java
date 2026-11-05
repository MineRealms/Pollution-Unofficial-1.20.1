package meowmel.pollution.gametest;

import meowmel.pollution.Pollution;
import meowmel.pollution.dimension.PortalFormationEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.*;

/**
 * Regression guard for the Twilight Forest clash: a vanilla flower ring with a
 * dropped diamond is Twilight Forest's ritual and must never consume its item
 * or create an underground portal. The underground ritual needs a Thaumium
 * Ingot catalyst and a Botania Loonium decoration instead.
 */
@GameTestHolder(Pollution.MOD_ID)
@PrefixGameTestTemplate(false)
public final class PortalGameTests {
    private PortalGameTests() {}

    @GameTest(template = "machine_lab", batch = "portal")
    public static void twilightRitualDoesNotFormUndergroundPortal(GameTestHelper helper) {
        var level = helper.getLevel();
        var base = helper.absolutePos(new BlockPos(12, 3, 12));
        for (BlockPos pos : BlockPos.betweenClosed(base.offset(-1, -1, -1), base.offset(2, 0, 2))) {
            level.setBlock(pos, Blocks.DIRT.defaultBlockState(), 2);
        }
        for (BlockPos pos : BlockPos.betweenClosed(base, base.offset(1, 0, 1))) {
            level.setBlock(pos, Blocks.WATER.defaultBlockState(), 2);
        }
        // Vanilla flower ring - the Twilight Forest shape.
        for (BlockPos pos : BlockPos.betweenClosed(base.offset(-1, 0, -1), base.offset(2, 0, 2))) {
            if (!level.getBlockState(pos).is(Blocks.WATER)) {
                level.setBlock(pos.above(), Blocks.DANDELION.defaultBlockState(), 2);
            }
        }
        var player = net.minecraftforge.common.util.FakePlayerFactory.get(level,
                new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "PortalTest"));
        player.setPos(base.getX() + 6, base.getY() + 1, base.getZ());
        var diamond = new ItemEntity(level, base.getX() + .5, base.getY() + .2, base.getZ() + .5,
                new ItemStack(Items.DIAMOND, 2));
        diamond.setInvulnerable(true);
        level.addFreshEntity(diamond);

        PortalFormationEvents.checkForPortalCreation(player);

        helper.assertTrue(diamond.getItem().getCount() == 2,
                "underground ritual consumed Twilight Forest's diamond catalyst");
        for (BlockPos pos : BlockPos.betweenClosed(base, base.offset(1, 0, 1))) {
            helper.assertTrue(level.getBlockState(pos).is(Blocks.WATER),
                    "vanilla flower ring created an underground portal");
        }
        helper.succeed();
    }
}
