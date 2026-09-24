package meowmel.pollution.api.starstream;

import net.minecraft.core.BlockPos;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;

public interface IStarstreamWirelessProvider {
    @Nullable UUID getWirelessNetworkId();
    int getWirelessRange();
    boolean isWirelessNetworkOnline();
    long requestWirelessEnergy(BlockPos consumerPos, UUID networkId, UUID consumerId,
                               String channel, long amount, boolean simulate);
    boolean consumeWirelessEnergy(BlockPos consumerPos, UUID networkId, UUID consumerId,
                                  Map<String, Long> requirements, boolean simulate);
}
