package meowmel.pollution.dimension.worldgen.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import meowmel.pollution.dimension.worldgen.PollutionStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

/**
 * 1.20.1 port of the 1.12 {@code MapGenUndergroundBridge} fortress. The layout
 * is generated procedurally by {@link UndergroundBridgePiece} instead of
 * re-authoring the 15 jigsaw NBT templates.
 */
public class UndergroundBridgeStructure extends Structure {

    public static final Codec<UndergroundBridgeStructure> CODEC = RecordCodecBuilder
            .<UndergroundBridgeStructure>mapCodec(instance -> instance
                    .group(settingsCodec(instance))
                    .apply(instance, UndergroundBridgeStructure::new))
            .codec();

    public UndergroundBridgeStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        RandomSource random = context.random();
        ChunkPos chunkPos = context.chunkPos();
        int x = chunkPos.getMinBlockX() + 8;
        int z = chunkPos.getMinBlockZ() + 8;
        int y = Mth.randomBetweenInclusive(random, 40, 88);
        return Optional.of(new GenerationStub(new BlockPos(x, y, z),
                builder -> builder.addPiece(new UndergroundBridgePiece(x, y, z, random))));
    }

    @Override
    public StructureType<?> type() {
        return PollutionStructures.UNDERGROUND_BRIDGE.get();
    }
}
