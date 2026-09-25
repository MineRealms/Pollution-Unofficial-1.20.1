package meowmel.pollution.common.starstream;

import meowmel.pollution.Pollution;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import java.util.HashSet;

/** Restores only live anchors and migrates older saves that used entity UUID tickets. */
public final class StarstreamChunkTickets {
    private StarstreamChunkTickets() {}

    public static void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> ForgeChunkManager.setForcedChunkLoadingCallback(Pollution.MOD_ID, StarstreamChunkTickets::validate));
    }

    private static void validate(ServerLevel level, ForgeChunkManager.TicketHelper tickets) {
        tickets.getBlockTickets().forEach((owner, chunks) -> {
            if (!level.getBlockState(owner).is(StarstreamBlocks.CHUNK_ANCHOR.get())) tickets.removeAllTickets(owner);
        });
        tickets.getEntityTickets().forEach((owner, chunks) -> {
            var all = new HashSet<Long>();
            chunks.getFirst().forEach((long chunk) -> all.add(chunk));
            chunks.getSecond().forEach((long chunk) -> all.add(chunk));
            for (long packed : all) {
                ChunkPos pos = new ChunkPos(packed);
                for (var entity : level.getChunk(pos.x, pos.z).getBlockEntities().values()) {
                    if (entity instanceof StarstreamBlockEntity node && node.getKind() == StarstreamBlockEntity.Kind.ANCHOR
                            && owner.equals(node.getNodeId())) {
                        ForgeChunkManager.forceChunk(level, Pollution.MOD_ID, node.getBlockPos(), pos.x, pos.z, true, true);
                    }
                }
            }
            tickets.removeAllTickets(owner);
        });
    }
}
