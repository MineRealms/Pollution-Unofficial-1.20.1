package meowmel.pollution.common.warp;

import meowmel.pollution.Pollution;
import meowmel.pollution.client.warp.ClientWarpEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/** Visual-only warp effects. No fake explosion invokes the damaging explosion API. */
public final class WarpNetwork {
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID, "warp"),
            () -> "1", "1"::equals, "1"::equals);

    public static void init() {
        CHANNEL.messageBuilder(Rain.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(Rain::encode).decoder(Rain::decode)
                .consumerMainThread((packet, context) -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                        () -> () -> ClientWarpEffects.rain(packet.dimension(), packet.level(), packet.ticks())))
                .add();
    }

    public static void rain(ServerPlayer player, int level, int ticks) {
        var world = player.serverLevel();
        for (ServerPlayer viewer : world.players()) {
            if (viewer.distanceToSqr(player) <= 64 * 64) {
                CHANNEL.send(PacketDistributor.PLAYER.with(() -> viewer),
                        new Rain(world.dimension().location(), level, ticks));
            }
        }
    }

    public static void fakeExplosion(ServerPlayer player) {
        var level = player.serverLevel();
        level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 4, 1);
        for (ServerPlayer viewer : level.players()) {
            if (viewer.distanceToSqr(player) <= 64 * 64) {
                level.sendParticles(viewer, ParticleTypes.LARGE_SMOKE, true,
                        player.getX(), player.getY() + 1, player.getZ(), 32, 1, 1, 1, 0.02);
            }
        }
    }

    private record Rain(ResourceLocation dimension, int level, int ticks) {
        private void encode(FriendlyByteBuf buffer) {
            buffer.writeResourceLocation(dimension);
            buffer.writeVarInt(level);
            buffer.writeVarInt(ticks);
        }
        private static Rain decode(FriendlyByteBuf buffer) {
            return new Rain(buffer.readResourceLocation(), buffer.readVarInt(), buffer.readVarInt());
        }
    }

    private WarpNetwork() {}
}
