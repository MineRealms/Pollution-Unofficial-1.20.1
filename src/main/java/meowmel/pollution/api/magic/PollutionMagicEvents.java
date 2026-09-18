package meowmel.pollution.api.magic;

import meowmel.pollution.Pollution;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

/**
 * Wiring for the Thaumcraft 4R integrations that need the item registry to be
 * populated (GT material items are created during {@code RegisterEvent}, before
 * common setup).
 */
@Mod.EventBusSubscriber(modid = Pollution.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class PollutionMagicEvents {

    private PollutionMagicEvents() {}

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(PollutionObjectAspects::register);
    }
}
