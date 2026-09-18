package meowmel.pollution.client;

import meowmel.pollution.Pollution;
import meowmel.pollution.common.block.PollutionPlantBlocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client-only block/item tint registration.
 *
 * <p>The 1.12.2 mod tinted its Alfheim grape blocks from {@code ColorHandlerEvent}
 * because the vine and lily-pad pad models carry a {@code tintindex} face and
 * custom blocks are not part of the vanilla colour tables. The same tints are
 * registered here through the modern {@link RegisterColorHandlersEvent}.</p>
 */
@Mod.EventBusSubscriber(modid = Pollution.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class PollutionColorHandlers {

    /** Upstream red-grape tint. */
    private static final int RED_GRAPE_TINT = 0xBBBBBB;
    /** Upstream white-grape tint when placed in the world. */
    private static final int WHITE_GRAPE_WORLD_TINT = 0x208030;
    /** Upstream white-grape item tint (vanilla water-lily fallback). */
    private static final int WHITE_GRAPE_ITEM_TINT = 0x71C35C;

    private PollutionColorHandlers() {}

    @SubscribeEvent
    public static void onRegisterBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tintIndex) -> RED_GRAPE_TINT,
                PollutionPlantBlocks.ALFHEIM_RED_GRAPE_0.get(),
                PollutionPlantBlocks.ALFHEIM_RED_GRAPE_1.get(),
                PollutionPlantBlocks.ALFHEIM_RED_GRAPE_2.get());
        event.register((state, level, pos, tintIndex) ->
                        level != null && pos != null ? WHITE_GRAPE_WORLD_TINT : WHITE_GRAPE_ITEM_TINT,
                PollutionPlantBlocks.ALFHEIM_WHITE_GRAPE.get());
    }

    @SubscribeEvent
    public static void onRegisterItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> WHITE_GRAPE_ITEM_TINT,
                PollutionPlantBlocks.ALFHEIM_WHITE_GRAPE.get());
        event.register((stack, tintIndex) -> RED_GRAPE_TINT,
                PollutionPlantBlocks.ALFHEIM_RED_GRAPE_0.get(),
                PollutionPlantBlocks.ALFHEIM_RED_GRAPE_1.get(),
                PollutionPlantBlocks.ALFHEIM_RED_GRAPE_2.get());
    }
}
