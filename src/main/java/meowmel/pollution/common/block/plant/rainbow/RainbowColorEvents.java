package meowmel.pollution.common.block.plant.rainbow;

import meowmel.pollution.Pollution;
import meowmel.pollution.common.block.PollutionPlantBlocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Client tint handlers for the position dependent rainbow colours. */
@Mod.EventBusSubscriber(modid = Pollution.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class RainbowColorEvents {

    @SubscribeEvent
    public static void onBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tintIndex) -> RainbowLeavesBlock.rainbowColor(pos),
                PollutionPlantBlocks.RAINBOW_LEAVES.get(), PollutionPlantBlocks.RAINBOW_SAPLING.get());
    }

    @SubscribeEvent
    public static void onItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> RainbowLeavesBlock.rainbowColor(null),
                PollutionPlantBlocks.RAINBOW_LEAVES.get(), PollutionPlantBlocks.RAINBOW_SAPLING.get());
    }

    private RainbowColorEvents() {}
}
