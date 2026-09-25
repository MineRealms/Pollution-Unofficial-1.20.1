package meowmel.pollution.gametest;

import meowmel.pollution.Pollution;
import meowmel.pollution.common.block.PollutionMiscBlocks;
import meowmel.pollution.dimension.PortalFormationEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.*;

@GameTestHolder(Pollution.MOD_ID)
@PrefixGameTestTemplate(false)
public final class PortalGameTests {
    private PortalGameTests() {}
    @GameTest(template = "machine_lab", batch = "portal")
    public static void diamondRitualFormsOnlyAnEnclosedPool(GameTestHelper helper) {
        var level = helper.getLevel();
        var base = helper.absolutePos(new BlockPos(12, 3, 12));
        for (BlockPos pos : BlockPos.betweenClosed(base.offset(-1, -1, -1), base.offset(2, 0, 2))) {
            level.setBlock(pos, Blocks.STONE.defaultBlockState(), 2);
        }
        for (BlockPos pos : BlockPos.betweenClosed(base, base.offset(1, 0, 1))) {
            level.setBlock(pos, Blocks.WATER.defaultBlockState(), 2);
        }
        var player = net.minecraftforge.common.util.FakePlayerFactory.get(level,
                new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "PortalTest"));
        player.setPos(base.getX() + 6, base.getY() + 1, base.getZ());
        var diamond = new ItemEntity(level, base.getX() + .5, base.getY() + .2, base.getZ() + .5, new ItemStack(Items.DIAMOND, 2));
        diamond.setInvulnerable(true); // Isolate catalyst accounting from the source ritual's lightning effect.
        level.addFreshEntity(diamond);
        level.setBlock(base.west(), Blocks.AIR.defaultBlockState(), 2);
        PortalFormationEvents.checkForPortalCreation(player);
        helper.assertTrue(diamond.getItem().getCount() == 2 && level.getBlockState(base).is(Blocks.WATER), "invalid pool consumed its catalyst");
        level.setBlock(base.west(), Blocks.STONE.defaultBlockState(), 2);
        PortalFormationEvents.checkForPortalCreation(player);
        helper.assertTrue(diamond.getItem().getCount() == 1, "ritual did not consume exactly one diamond");
        for (BlockPos pos : BlockPos.betweenClosed(base, base.offset(1, 0, 1))) {
            helper.assertTrue(level.getBlockState(pos).is(PollutionMiscBlocks.PORTAL.get()), "ritual left a partial portal");
        }
        PollutionMiscBlocks.PORTAL.get().neighborChanged(level.getBlockState(base), level, base, Blocks.STONE, base.west(), false);
        helper.assertTrue(level.getBlockState(base).is(PollutionMiscBlocks.PORTAL.get()), "valid stone border immediately broke the portal");
        helper.succeed();
    }
}
