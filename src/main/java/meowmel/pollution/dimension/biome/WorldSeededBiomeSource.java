package meowmel.pollution.dimension.biome;

import meowmel.pollution.Pollution;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Seeds opted-in biome sources before the server starts generating their spawn chunks. */
public interface WorldSeededBiomeSource {
    void bindWorldSeed(long seed);

    @Mod.EventBusSubscriber(modid = Pollution.MOD_ID)
    final class Events {
        private Events() {}

        @SubscribeEvent
        public static void onLevelLoad(LevelEvent.Load event) {
            if (event.getLevel() instanceof ServerLevel level
                    && level.getChunkSource().getGenerator().getBiomeSource() instanceof WorldSeededBiomeSource source) {
                source.bindWorldSeed(level.getSeed());
            }
        }
    }
}
