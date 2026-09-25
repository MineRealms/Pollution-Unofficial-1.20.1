package meowmel.pollution.common.network;

import meowmel.pollution.Pollution;
import meowmel.pollution.client.PollutionClientState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/** Small, throttled server-to-client channel for the local chunk pollution HUD. */
public final class PollutionNetwork {

    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID, "pollution_sync"),
            () -> "1", "1"::equals, "1"::equals);

    private PollutionNetwork() {}

    public static void init() {
        CHANNEL.messageBuilder(ChunkPollutionPacket.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ChunkPollutionPacket::encode)
                .decoder(ChunkPollutionPacket::decode)
                .consumerMainThread((packet, context) -> {
                    DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                            () -> () -> PollutionClientState.update(packet.dimension(), packet.chunk(),
                                    packet.amount(), packet.threshold()));
                    context.get().setPacketHandled(true);
                })
                .add();
    }

    public static void send(ServerPlayer player, double amount, double threshold) {
        // Fake players and GameTest players have no transport to receive a HUD.
        if (player.connection == null || player.connection.connection.channel() == null) return;
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new ChunkPollutionPacket(player.level().dimension().location(), player.chunkPosition().toLong(),
                        amount, threshold));
    }

    private record ChunkPollutionPacket(ResourceLocation dimension, long chunk, double amount, double threshold) {
        private void encode(FriendlyByteBuf buffer) {
            buffer.writeResourceLocation(dimension);
            buffer.writeLong(chunk);
            buffer.writeDouble(amount);
            buffer.writeDouble(threshold);
        }

        private static ChunkPollutionPacket decode(FriendlyByteBuf buffer) {
            return new ChunkPollutionPacket(buffer.readResourceLocation(), buffer.readLong(),
                    buffer.readDouble(), buffer.readDouble());
        }
    }
}
