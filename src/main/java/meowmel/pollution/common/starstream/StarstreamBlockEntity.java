package meowmel.pollution.common.starstream;

import meowmel.pollution.Pollution;
import meowmel.pollution.api.starstream.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.world.ForgeChunkManager;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Portable Starstream transport: persistent channel bank, stateless directed relays,
 * cross-dimensional gateways and on-demand operation terminals. Astral producers
 * feed receiveConstellationEnergy; these blocks never convert ordinary EU into stars.
 */
public class StarstreamBlockEntity extends BlockEntity
        implements IStarstreamWirelessProvider, IStarstreamOperationCore {
    public enum Kind { CORE, RELAY, GATEWAY, OPERATION, ANCHOR }
    private static final Map<ServerLevel, Set<BlockPos>> LOADED = new WeakHashMap<>();
    private UUID nodeId = UUID.randomUUID();
    private final StarstreamWirelessBinding binding = new StarstreamWirelessBinding();
    private final Map<String, Long> energies = new HashMap<>();
    private final Set<UUID> inbound = new HashSet<>();
    private ResourceKey<Level> targetDimension;
    private BlockPos targetPos;
    private UUID targetId;
    private boolean wirelessEnabled = true;
    private long outputLimit = StarstreamNetwork.CORE_OUTPUT;
    private long windowTick = Long.MIN_VALUE;
    private long wirelessTransferred;
    private long inputTransferred;
    private final Map<UUID, Long> consumers = new HashMap<>();

    public StarstreamBlockEntity(BlockPos pos, BlockState state) {
        super(StarstreamBlocks.BLOCK_ENTITY.get(), pos, state);
    }
    public Kind getKind() { return ((StarstreamBlock) getBlockState().getBlock()).getKind(); }
    public UUID getNodeId() { return nodeId; }
    @Nullable public BlockPos getTargetPos() { return targetPos; }
    public long getTotalStored() { return energies.values().stream().mapToLong(Long::longValue).sum(); }
    public long getConstellationEnergyStored(String channel) { return energies.getOrDefault(channel, 0L); }
    public boolean isWirelessEnabled() { return wirelessEnabled; }
    public void bindNetwork(@Nullable UUID network) { binding.bind(network); onStarstreamNetworkChanged(network); }
    public void setWirelessEnabled(boolean enabled) { wirelessEnabled = enabled; changed(); }
    public void setWirelessOutputLimit(long limit) {
        outputLimit = Math.max(0, Math.min(StarstreamNetwork.CORE_OUTPUT, limit));
        changed();
    }

    @Override public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel server) {
            LOADED.computeIfAbsent(server, ignored -> new HashSet<>()).add(worldPosition);
            applyPendingUnlinks();
            if (getKind() == Kind.ANCHOR) forceChunk(true);
        }
    }
    @Override public void setRemoved() {
        if (level instanceof ServerLevel server) {
            Set<BlockPos> loaded = LOADED.get(server);
            if (loaded != null) loaded.remove(worldPosition);
        }
        super.setRemoved();
    }
    /** Called only when the block is broken, not when its chunk unloads. */
    public void onBroken() {
        if (getKind() == Kind.ANCHOR) forceChunk(false);
        clearOutput();
    }
    private void forceChunk(boolean force) {
        if (level instanceof ServerLevel server) {
            ForgeChunkManager.forceChunk(server, Pollution.MOD_ID, worldPosition,
                    worldPosition.getX() >> 4, worldPosition.getZ() >> 4, force, true);
        }
    }

    @Nullable public static StarstreamBlockEntity findNearestProvider(ServerLevel level, BlockPos pos, UUID network) {
        StarstreamBlockEntity nearest = null;
        double distance = Double.MAX_VALUE;
        for (BlockPos candidate : LOADED.getOrDefault(level, Collections.emptySet())) {
            if (!level.hasChunkAt(candidate)
                    || !(level.getBlockEntity(candidate) instanceof StarstreamBlockEntity node)) continue;
            double next = candidate.distSqr(pos);
            if (next < distance && next <= (long) node.getWirelessRange() * node.getWirelessRange()
                    && network.equals(node.getWirelessNetworkId()) && node.isWirelessNetworkOnline()) {
                nearest = node;
                distance = next;
            }
        }
        return nearest;
    }

    /** Producer/admin hook: returns a loaded nexus with the requested network identity. */
    @Nullable public static StarstreamBlockEntity findLoadedCore(ServerLevel level, UUID network) {
        for (BlockPos candidate : LOADED.getOrDefault(level, Collections.emptySet())) {
            if (level.hasChunkAt(candidate) && level.getBlockEntity(candidate) instanceof StarstreamBlockEntity node
                    && node.getKind() == Kind.CORE && network.equals(node.nodeId)) return node;
        }
        return null;
    }

    /** Validates endpoint identity, capacity, range and loops before replacing an old link. */
    public boolean bindOutput(StarstreamBlockEntity target) {
        if (!(level instanceof ServerLevel) || target == this || target.level == null) return false;
        Kind kind = getKind();
        if (kind != Kind.RELAY && kind != Kind.GATEWAY) return false;
        if (target.getKind() != Kind.CORE && target.getKind() != Kind.RELAY && target.getKind() != Kind.GATEWAY) return false;
        if (kind == Kind.GATEWAY) {
            if (target.getKind() != Kind.CORE) return false;
        } else if (level != target.level
                || worldPosition.distSqr(target.worldPosition) > (long) StarstreamNetwork.LINK_RANGE * StarstreamNetwork.LINK_RANGE) {
            return false;
        }
        Set<UUID> visited = new HashSet<>();
        StarstreamBlockEntity current = target;
        int hops = 1;
        while (current != null) {
            if (!visited.add(current.nodeId) || current.nodeId.equals(nodeId)
                    || hops++ > StarstreamNetwork.MAX_HOPS) return false;
            current = current.resolveTarget();
        }
        int max = target.getKind() == Kind.CORE ? StarstreamNetwork.MAX_INPUTS : StarstreamNetwork.MAX_RELAY_INPUTS;
        target.applyPendingUnlinks();
        if (!target.inbound.contains(nodeId) && target.inbound.size() >= max) return false;
        clearOutput();
        targetDimension = target.level.dimension();
        targetPos = target.worldPosition.immutable();
        targetId = target.nodeId;
        target.inbound.add(nodeId);
        target.changed();
        changed();
        return true;
    }

    public void clearOutput() {
        StarstreamBlockEntity old = resolveTarget();
        if (old != null) { old.inbound.remove(nodeId); old.changed(); }
        else if (targetId != null && level instanceof ServerLevel server) {
            StarstreamLinkData.get(server).unlink(targetId, nodeId);
        }
        targetDimension = null;
        targetPos = null;
        targetId = null;
        changed();
    }

    private void applyPendingUnlinks() {
        if (level instanceof ServerLevel server
                && inbound.removeAll(StarstreamLinkData.get(server).takeUnlinks(nodeId))) setChanged();
    }

    @Nullable private StarstreamBlockEntity resolveTarget() {
        if (!(level instanceof ServerLevel server) || targetPos == null || targetDimension == null || targetId == null) return null;
        ServerLevel targetLevel = server.getServer().getLevel(targetDimension);
        if (targetLevel == null || !targetLevel.hasChunkAt(targetPos)) return null;
        if (getKind() != Kind.GATEWAY && (targetLevel != level || worldPosition.distSqr(targetPos)
                > (long) StarstreamNetwork.LINK_RANGE * StarstreamNetwork.LINK_RANGE)) return null;
        if (!(targetLevel.getBlockEntity(targetPos) instanceof StarstreamBlockEntity target)
                || !targetId.equals(target.nodeId)) return null;
        if (getKind() == Kind.GATEWAY && target.getKind() != Kind.CORE) return null;
        return target;
    }

    /** A route ends at a loaded core; an unloaded endpoint never triggers a chunk load. */
    private List<StarstreamBlockEntity> route() {
        List<StarstreamBlockEntity> path = new ArrayList<>();
        Set<UUID> visited = new HashSet<>();
        StarstreamBlockEntity current = this;
        for (int hops = 0; hops <= StarstreamNetwork.MAX_HOPS; hops++) {
            if (current == null || !visited.add(current.nodeId)) return List.of();
            path.add(current);
            if (current.getKind() == Kind.CORE) return path;
            if (current.getKind() != Kind.RELAY && current.getKind() != Kind.GATEWAY) return List.of();
            current = current.resolveTarget();
        }
        return List.of();
    }

    @Override @Nullable public UUID getWirelessNetworkId() {
        List<StarstreamBlockEntity> route = route();
        return route.isEmpty() ? null : route.get(route.size() - 1).nodeId;
    }
    @Override public int getWirelessRange() {
        return switch (getKind()) { case CORE -> 128; case RELAY -> 64; case GATEWAY -> 96; default -> 0; };
    }
    @Override public boolean isWirelessNetworkOnline() {
        List<StarstreamBlockEntity> route = route();
        return !route.isEmpty() && route.get(route.size() - 1).wirelessEnabled;
    }
    private void refreshWindow() {
        if (level != null && windowTick != level.getGameTime()) {
            windowTick = level.getGameTime();
            wirelessTransferred = 0;
            inputTransferred = 0;
            consumers.clear();
        }
    }
    private long wirelessLimit() {
        return switch (getKind()) {
            case CORE -> outputLimit;
            case RELAY -> StarstreamNetwork.RELAY_OUTPUT;
            case GATEWAY -> StarstreamNetwork.GATEWAY_OUTPUT;
            default -> 0;
        };
    }

    /** Receives a producer's channel at a bank, or forwards it without relay storage. */
    public long receiveConstellationEnergy(String channel, long amount, boolean simulate) {
        if (!(level instanceof ServerLevel) || channel == null || !StarstreamNetwork.CHANNELS.contains(channel) || amount <= 0) return 0;
        List<StarstreamBlockEntity> route = route();
        if (route.isEmpty() || route.stream().anyMatch(node -> node.getKind() == Kind.GATEWAY)) return 0;
        StarstreamBlockEntity core = route.get(route.size() - 1);
        long accepted = Math.min(amount, StarstreamNetwork.CAPACITY_PER_CHANNEL - core.getConstellationEnergyStored(channel));
        for (StarstreamBlockEntity node : route) {
            node.refreshWindow();
            if (node.getKind() != Kind.CORE) accepted = Math.min(accepted, StarstreamNetwork.RELAY_OUTPUT - node.inputTransferred);
        }
        if (!simulate && accepted > 0) {
            core.energies.merge(channel, accepted, Long::sum);
            core.setChanged();
            for (StarstreamBlockEntity node : route) node.inputTransferred += accepted;
        }
        return accepted;
    }

    private List<StarstreamBlockEntity> wirelessRoute(BlockPos consumerPos, UUID network, UUID consumer) {
        if (!(level instanceof ServerLevel) || consumerPos == null || network == null || consumer == null
                || worldPosition.distSqr(consumerPos) > (long) getWirelessRange() * getWirelessRange()) return List.of();
        List<StarstreamBlockEntity> path = route();
        if (path.isEmpty()) return path;
        StarstreamBlockEntity core = path.get(path.size() - 1);
        if (!network.equals(core.nodeId) || !core.wirelessEnabled) return List.of();
        path.forEach(StarstreamBlockEntity::refreshWindow);
        return path;
    }
    private static long available(List<StarstreamBlockEntity> path, UUID consumer) {
        StarstreamBlockEntity core = path.get(path.size() - 1);
        long available = StarstreamNetwork.TERMINAL_OUTPUT - core.consumers.getOrDefault(consumer, 0L);
        for (StarstreamBlockEntity node : path) available = Math.min(available, node.wirelessLimit() - node.wirelessTransferred);
        return Math.max(0, available);
    }
    private static void record(List<StarstreamBlockEntity> path, UUID consumer, long amount) {
        for (StarstreamBlockEntity node : path) node.wirelessTransferred += amount;
        StarstreamBlockEntity core = path.get(path.size() - 1);
        core.consumers.merge(consumer, amount, Long::sum);
        core.setChanged();
    }
    @Override public long requestWirelessEnergy(BlockPos pos, UUID network, UUID consumer,
                                                String channel, long amount, boolean simulate) {
        if (channel == null || !StarstreamNetwork.CHANNELS.contains(channel) || amount <= 0) return 0;
        List<StarstreamBlockEntity> path = wirelessRoute(pos, network, consumer);
        if (path.isEmpty()) return 0;
        StarstreamBlockEntity core = path.get(path.size() - 1);
        long extracted = Math.min(amount, Math.min(available(path, consumer), core.getConstellationEnergyStored(channel)));
        if (!simulate && extracted > 0) {
            core.energies.put(channel, core.getConstellationEnergyStored(channel) - extracted);
            record(path, consumer, extracted);
        }
        return extracted;
    }
    @Override public boolean consumeWirelessEnergy(BlockPos pos, UUID network, UUID consumer,
                                                   Map<String, Long> requirements, boolean simulate) {
        long total = StarstreamNetwork.total(requirements);
        if (total <= 0) return false;
        List<StarstreamBlockEntity> path = wirelessRoute(pos, network, consumer);
        if (path.isEmpty() || total > available(path, consumer)) return false;
        StarstreamBlockEntity core = path.get(path.size() - 1);
        for (var entry : requirements.entrySet()) if (entry.getValue() > core.getConstellationEnergyStored(entry.getKey())) return false;
        if (!simulate) {
            requirements.forEach((channel, amount) -> core.energies.put(channel, core.getConstellationEnergyStored(channel) - amount));
            record(path, consumer, total);
        }
        return true;
    }

    @Override public StarstreamWirelessBinding getStarstreamWirelessBinding() { return binding; }
    @Override public void onStarstreamNetworkChanged(@Nullable UUID networkId) { changed(); }
    @Override public boolean canBindStarstreamNetwork(net.minecraft.world.entity.player.Player player, UUID id) {
        return getKind() == Kind.OPERATION;
    }
    @Override public long requestConstellationEnergy(Level requestLevel, BlockPos pos, String channel, long amount, boolean simulate) {
        return getKind() == Kind.OPERATION ? binding.requestEnergy(requestLevel, pos, channel, amount, simulate) : 0;
    }
    @Override public boolean consumeConstellationEnergy(Level requestLevel, BlockPos pos, Map<String, Long> requirements, boolean simulate) {
        return getKind() == Kind.OPERATION && binding.consumeEnergy(requestLevel, pos, requirements, simulate);
    }

    private void changed() {
        setChanged();
        if (level != null && !level.isClientSide) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }
    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putUUID("NodeId", nodeId);
        tag.putBoolean("WirelessEnabled", wirelessEnabled);
        tag.putLong("OutputLimit", outputLimit);
        CompoundTag bank = new CompoundTag();
        energies.forEach(bank::putLong);
        tag.put("ConstellationEnergies", bank);
        tag.put("StarstreamWireless", binding.serializeNBT());
        if (targetPos != null && targetDimension != null && targetId != null) {
            tag.putString("TargetDimension", targetDimension.location().toString());
            tag.putLong("TargetPos", targetPos.asLong());
            tag.putUUID("TargetId", targetId);
        }
        ListTag inputs = new ListTag();
        for (UUID id : inbound) { CompoundTag input = new CompoundTag(); input.putUUID("Id", id); inputs.add(input); }
        tag.put("Inputs", inputs);
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.hasUUID("NodeId")) nodeId = tag.getUUID("NodeId");
        wirelessEnabled = !tag.contains("WirelessEnabled") || tag.getBoolean("WirelessEnabled");
        outputLimit = tag.contains("OutputLimit") ? Math.max(0, Math.min(StarstreamNetwork.CORE_OUTPUT, tag.getLong("OutputLimit"))) : StarstreamNetwork.CORE_OUTPUT;
        energies.clear();
        CompoundTag bank = tag.getCompound("ConstellationEnergies");
        for (String channel : StarstreamNetwork.CHANNELS) energies.put(channel,
                Math.max(0, Math.min(StarstreamNetwork.CAPACITY_PER_CHANNEL, bank.getLong(channel))));
        binding.deserializeNBT(tag.getCompound("StarstreamWireless"));
        ResourceLocation dimension = ResourceLocation.tryParse(tag.getString("TargetDimension"));
        targetDimension = dimension == null ? null : ResourceKey.create(Registries.DIMENSION, dimension);
        targetPos = tag.contains("TargetPos") ? BlockPos.of(tag.getLong("TargetPos")) : null;
        targetId = tag.hasUUID("TargetId") ? tag.getUUID("TargetId") : null;
        inbound.clear();
        ListTag inputs = tag.getList("Inputs", Tag.TAG_COMPOUND);
        for (int i = 0; i < Math.min(inputs.size(), StarstreamNetwork.MAX_INPUTS); i++) {
            if (inputs.getCompound(i).hasUUID("Id")) inbound.add(inputs.getCompound(i).getUUID("Id"));
        }
        windowTick = Long.MIN_VALUE;
    }
    @Override public CompoundTag getUpdateTag() { return saveWithoutMetadata(); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}
