package meowmel.pollution.dimension.biome;

import com.mojang.serialization.Codec;
import meowmel.pollution.Pollution;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;

/**
 * Registers the codec-based biome sources that replace the 1.12
 * {@code BiomeProvider}/{@code GenLayer} pair.
 *
 * <p>Registered ids:</p>
 * <ul>
 *   <li>{@code pollution:underground} - 8-biome noise distribution</li>
 *   <li>{@code pollution:alfheim} - 12-profile WorldEngine biome map</li>
 * </ul>
 */
public final class POBiomeSources {

    private static final DeferredRegister<Codec<? extends BiomeSource>> BIOME_SOURCES =
            DeferredRegister.create(Registries.BIOME_SOURCE, Pollution.MOD_ID);
    private static final DeferredRegister<Codec<? extends net.minecraft.world.level.chunk.ChunkGenerator>> CHUNK_GENERATORS =
            DeferredRegister.create(Registries.CHUNK_GENERATOR, Pollution.MOD_ID);

    public static void init(FMLJavaModLoadingContext context) {
        BIOME_SOURCES.register("underground", () -> POUndergroundBiomeSource.CODEC);
        BIOME_SOURCES.register("alfheim", () -> POAlfheimBiomeSource.CODEC);
        BIOME_SOURCES.register(context.getModEventBus());
        CHUNK_GENERATORS.register("alfheim", () -> meowmel.pollution.dimension.worldgen.AlfheimChunkGenerator.CODEC);
        CHUNK_GENERATORS.register("underground", () -> meowmel.pollution.dimension.worldgen.UndergroundChunkGenerator.CODEC);
        CHUNK_GENERATORS.register(context.getModEventBus());
    }

    private POBiomeSources() {
    }
}
