package meowmel.pollution.client;

import meowmel.pollution.Pollution;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Pollution.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class PollutionDimensionEffects {
    private PollutionDimensionEffects() {}
    @SubscribeEvent
    public static void register(RegisterDimensionSpecialEffectsEvent event) {
        event.register(ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID, "alfheim"),
                new DimensionSpecialEffects.OverworldEffects() {
                    @Override public float getCloudHeight() { return 164; }
                });
    }
}
