package meowmel.pollution.gametest;

import meowmel.pollution.Pollution;
import meowmel.pollution.common.starstream.StarstreamBlockEntity;
import meowmel.pollution.common.starstream.StarstreamBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import java.util.Map;
import java.util.UUID;

@GameTestHolder(Pollution.MOD_ID)
@PrefixGameTestTemplate(false)
public final class StarstreamGameTests {
    private StarstreamGameTests() {}

    @GameTest(template = "platform", batch = "starstream")
    public static void removalUnlinksAndUnloadedTargetsReceiveDurableUnlinks(GameTestHelper helper) {
        var core = place(helper, 0, StarstreamBlocks.NEXUS_CORE.get());
        var relay = place(helper, 1, StarstreamBlocks.RELAY.get());
        helper.assertTrue(relay.bindOutput(core), "initial binding failed");
        helper.getLevel().removeBlock(relay.getBlockPos(), false);
        helper.assertTrue(core.saveWithoutMetadata().getList("Inputs", 10).isEmpty(), "non-player removal left an inbound slot");
        relay = place(helper, 1, StarstreamBlocks.RELAY.get());
        helper.assertTrue(relay.bindOutput(core), "second binding failed");
        var saved = core.saveWithoutMetadata();
        helper.getLevel().removeBlockEntity(core.getBlockPos());
        relay.clearOutput();
        var data = meowmel.pollution.common.starstream.StarstreamLinkData.get(helper.getLevel());
        var diskCopy = meowmel.pollution.common.starstream.StarstreamLinkData.load(data.save(new net.minecraft.nbt.CompoundTag()));
        helper.assertTrue(diskCopy.takeUnlinks(core.getNodeId()).contains(relay.getNodeId()), "pending unlink did not survive serialization");
        var restored = new StarstreamBlockEntity(core.getBlockPos(), core.getBlockState());
        restored.load(saved);
        helper.getLevel().setBlockEntity(restored);
        restored.onLoad();
        helper.assertTrue(restored.saveWithoutMetadata().getList("Inputs", 10).isEmpty(), "reloaded target kept stale inbound slot");
        helper.succeed();
    }

    private static StarstreamBlockEntity place(GameTestHelper helper, int x, Block block) {
        BlockPos pos = helper.absolutePos(new BlockPos(x, 1, 1));
        helper.getLevel().setBlockAndUpdate(pos, block.defaultBlockState());
        return (StarstreamBlockEntity) helper.getLevel().getBlockEntity(pos);
    }

    @GameTest(template = "platform", batch = "starstream")
    public static void routesAreAtomicPersistentAndIdentityBound(GameTestHelper helper) {
        var core = place(helper, 0, StarstreamBlocks.NEXUS_CORE.get());
        var relay = place(helper, 1, StarstreamBlocks.RELAY.get());
        var relay2 = place(helper, 2, StarstreamBlocks.RELAY.get());
        helper.assertTrue(relay.bindOutput(core) && relay2.bindOutput(relay), "valid route was rejected");
        helper.assertTrue(!relay.bindOutput(relay2), "routing loop was accepted");
        helper.assertTrue(core.receiveConstellationEnergy("aevitas", 2000, false) == 2000, "core did not accept energy");
        core.receiveConstellationEnergy("evorsio", 1000, false);
        UUID network = core.getNodeId();
        UUID consumer = UUID.randomUUID();
        BlockPos pos = relay2.getBlockPos();
        helper.assertTrue(!relay2.consumeWirelessEnergy(pos, network, consumer,
                Map.of("aevitas", 1000L, "evorsio", 1001L), false), "underfunded request succeeded");
        helper.assertTrue(core.getTotalStored() == 3000, "failed request partially drained the bank");
        var request = Map.of("aevitas", 1000L, "evorsio", 500L);
        helper.assertTrue(relay2.consumeWirelessEnergy(pos, network, consumer, request, true), "simulation failed");
        helper.assertTrue(core.getTotalStored() == 3000, "simulation drained the bank");
        helper.assertTrue(relay2.consumeWirelessEnergy(pos, network, consumer, request, false), "valid request failed");
        helper.assertTrue(core.getTotalStored() == 1500, "wrong energy deduction");
        var bankTag = core.saveWithoutMetadata();
        var relayTag = relay.saveWithoutMetadata();
        core.load(bankTag);
        relay.load(relayTag);
        helper.assertTrue(core.getNodeId().equals(network) && core.getTotalStored() == 1500,
                "save/load changed network identity or energy");
        helper.assertTrue(relay2.getWirelessNetworkId().equals(network), "saved route did not resolve");
        helper.getLevel().removeBlock(core.getBlockPos(), false);
        var replacement = place(helper, 0, StarstreamBlocks.NEXUS_CORE.get());
        replacement.receiveConstellationEnergy("aevitas", 1000, false);
        helper.assertTrue(relay2.requestWirelessEnergy(pos, network, consumer, "aevitas", 1, false) == 0,
                "old link drained a replacement core with a different identity");
        helper.succeed();
    }
}
