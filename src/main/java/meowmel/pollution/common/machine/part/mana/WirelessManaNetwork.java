package meowmel.pollution.common.machine.part.mana;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;

/**
 * Wireless mana network shared by the wireless mana hatches.
 *
 * <p>Upstream origin: {@code WirelessManager} / {@code WirelessWorldData}
 * (1.12.2). The 1.12 manager kept two static caches, energy-type mana for
 * {@code MetaTileEntityWirelessManaHatch} and pure Botania mana for
 * {@code MetaTileEntityWirelessManaPoolHatch}, and persisted them through a
 * {@code WorldSavedData} stored on dimension 0 and loaded/saved from the
 * {@code WorldEvent.Load} / {@code WorldEvent.Save} hooks.</p>
 *
 * <p>Semantics preserved by the port:</p>
 * <ul>
 *   <li>The network is <b>global per dimension</b> (not per team or owner)
 *       and effectively unbounded; the only throughput limits are the hatch
 *       transfer rates.</li>
 *   <li>Energy-type mana and pure pool mana use two independent buffers.</li>
 *   <li>{@code add*} returns the accepted amount and {@code request*}
 *       returns the removed amount, so a partial transfer never creates or
 *       destroys mana.</li>
 * </ul>
 *
 * <p>Modern mapping: the static caches are gone; this {@link SavedData} is
 * fetched from the overworld's data storage and still keys the buffers by
 * dimension, so vanilla handles load/save and {@code setDirty}. It is only
 * touched from the server thread by machine ticks, so no client-side state
 * exists.</p>
 */
public final class WirelessManaNetwork extends SavedData {

    public static final String DATA_NAME = "pollution_wireless_mana";

    private static final String TAG_ENERGY_MANA = "EnergyManaDimData";
    private static final String TAG_MANA_POOL = "ManaPoolDimData";

    private final Map<String, Long> energyManaByDimension = new HashMap<>();
    private final Map<String, Long> manaPoolByDimension = new HashMap<>();

    public static WirelessManaNetwork load(CompoundTag tag) {
        WirelessManaNetwork network = new WirelessManaNetwork();
        readDimensionMap(tag.getCompound(TAG_ENERGY_MANA), network.energyManaByDimension);
        readDimensionMap(tag.getCompound(TAG_MANA_POOL), network.manaPoolByDimension);
        return network;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.put(TAG_ENERGY_MANA, writeDimensionMap(energyManaByDimension));
        tag.put(TAG_MANA_POOL, writeDimensionMap(manaPoolByDimension));
        return tag;
    }

    /** @return the energy-type mana stored in {@code level}'s dimension */
    public static long getEnergy(Level level) {
        WirelessManaNetwork network = get(level);
        return network == null ? 0L
                : network.energyManaByDimension.getOrDefault(dimensionKey(level), 0L);
    }

    /** @return the amount of energy-type mana actually accepted */
    public static long addEnergy(Level level, long amount) {
        WirelessManaNetwork network = get(level);
        return network == null ? 0L
                : network.addToCache(network.energyManaByDimension, dimensionKey(level), amount);
    }

    /** @return the amount of energy-type mana actually removed */
    public static long requestEnergy(Level level, long amount) {
        WirelessManaNetwork network = get(level);
        return network == null ? 0L
                : network.removeFromCache(network.energyManaByDimension, dimensionKey(level), amount);
    }

    /** @return the pure pool mana stored in {@code level}'s dimension */
    public static long getManaPool(Level level) {
        WirelessManaNetwork network = get(level);
        return network == null ? 0L
                : network.manaPoolByDimension.getOrDefault(dimensionKey(level), 0L);
    }

    /** @return the amount of pool mana actually accepted */
    public static long addManaPool(Level level, long amount) {
        WirelessManaNetwork network = get(level);
        return network == null ? 0L
                : network.addToCache(network.manaPoolByDimension, dimensionKey(level), amount);
    }

    /** @return the amount of pool mana actually removed */
    public static long requestManaPool(Level level, long amount) {
        WirelessManaNetwork network = get(level);
        return network == null ? 0L
                : network.removeFromCache(network.manaPoolByDimension, dimensionKey(level), amount);
    }

    private long addToCache(Map<String, Long> cache, String dimension, long amount) {
        if (amount <= 0L) return 0L;
        long current = cache.getOrDefault(dimension, 0L);
        long accepted = Math.min(amount, Long.MAX_VALUE - current);
        if (accepted <= 0L) return 0L;
        cache.put(dimension, current + accepted);
        setDirty();
        return accepted;
    }

    private long removeFromCache(Map<String, Long> cache, String dimension, long amount) {
        if (amount <= 0L) return 0L;
        long current = cache.getOrDefault(dimension, 0L);
        long removed = Math.min(current, amount);
        if (removed <= 0L) return 0L;
        long remaining = current - removed;
        if (remaining == 0L) {
            cache.remove(dimension);
        } else {
            cache.put(dimension, remaining);
        }
        setDirty();
        return removed;
    }

    private static WirelessManaNetwork get(Level level) {
        if (level == null || level.isClientSide()) return null;
        MinecraftServer server = level.getServer();
        if (server == null) return null;
        ServerLevel overworld = server.overworld();
        if (overworld == null) return null;
        return overworld.getDataStorage().computeIfAbsent(
                WirelessManaNetwork::load, WirelessManaNetwork::new, DATA_NAME);
    }

    private static String dimensionKey(Level level) {
        return level.dimension().location().toString();
    }

    private static void readDimensionMap(CompoundTag dimensions, Map<String, Long> target) {
        for (String key : dimensions.getAllKeys()) {
            long amount = dimensions.getLong(key);
            if (amount > 0L) {
                target.put(key, amount);
            }
        }
    }

    private static CompoundTag writeDimensionMap(Map<String, Long> source) {
        CompoundTag dimensions = new CompoundTag();
        for (Map.Entry<String, Long> entry : source.entrySet()) {
            if (entry.getValue() > 0L) {
                dimensions.putLong(entry.getKey(), entry.getValue());
            }
        }
        return dimensions;
    }

    private WirelessManaNetwork() {
    }
}
