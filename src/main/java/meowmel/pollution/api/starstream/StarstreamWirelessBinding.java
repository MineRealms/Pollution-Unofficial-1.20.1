package meowmel.pollution.api.starstream;

import meowmel.pollution.common.starstream.StarstreamBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;

/** Persistent terminal identity and nearest loaded provider discovery; never loads chunks. */
public final class StarstreamWirelessBinding {
    private UUID terminalId = UUID.randomUUID();
    private UUID networkId;
    private BlockPos providerPos;
    private long nextDiscovery;
    private String status = "unbound";

    public UUID getTerminalId() { return terminalId; }
    @Nullable public UUID getNetworkId() { return networkId; }
    @Nullable public BlockPos getProviderPos() { return providerPos; }
    public boolean isBound() { return networkId != null; }
    public String getStatusTranslationKey() { return "pollution.starstream_terminal.status." + status; }
    public void bind(@Nullable UUID id) {
        networkId = id;
        providerPos = null;
        nextDiscovery = 0;
        status = id == null ? "unbound" : "discovering";
    }
    public void clear() { bind(null); }

    public long requestEnergy(Level level, BlockPos pos, String channel, long amount, boolean simulate) {
        if (channel == null || !StarstreamNetwork.CHANNELS.contains(channel) || amount <= 0) {
            status = "invalid_request";
            return 0;
        }
        IStarstreamWirelessProvider provider = resolve(level, pos);
        if (provider == null) return 0;
        long received = provider.requestWirelessEnergy(pos, networkId, terminalId, channel, amount, simulate);
        status = received > 0 ? "online" : "waiting_for_energy";
        return received;
    }

    public boolean consumeEnergy(Level level, BlockPos pos, Map<String, Long> requirements, boolean simulate) {
        if (StarstreamNetwork.total(requirements) <= 0) {
            status = "invalid_request";
            return false;
        }
        IStarstreamWirelessProvider provider = resolve(level, pos);
        if (provider == null) return false;
        boolean consumed = provider.consumeWirelessEnergy(pos, networkId, terminalId, requirements, simulate);
        status = consumed ? "online" : "waiting_for_energy";
        return consumed;
    }

    @Nullable private IStarstreamWirelessProvider resolve(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel server) || pos == null) {
            status = "wrong_side";
            return null;
        }
        if (networkId == null) { status = "unbound"; return null; }
        if (providerPos != null && level.hasChunkAt(providerPos)
                && level.getBlockEntity(providerPos) instanceof IStarstreamWirelessProvider provider
                && networkId.equals(provider.getWirelessNetworkId())
                && provider.isWirelessNetworkOnline()
                && providerPos.distSqr(pos) <= (long) provider.getWirelessRange() * provider.getWirelessRange()) {
            return provider;
        }
        providerPos = null;
        if (level.getGameTime() < nextDiscovery) return null;
        nextDiscovery = level.getGameTime() + 20;
        StarstreamBlockEntity provider = StarstreamBlockEntity.findNearestProvider(server, pos, networkId);
        if (provider == null) { status = "provider_unavailable"; return null; }
        providerPos = provider.getBlockPos();
        return provider;
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("TerminalId", terminalId);
        if (networkId != null) tag.putUUID("NetworkId", networkId);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        if (tag.hasUUID("TerminalId")) terminalId = tag.getUUID("TerminalId");
        bind(tag.hasUUID("NetworkId") ? tag.getUUID("NetworkId") : null);
    }
}
