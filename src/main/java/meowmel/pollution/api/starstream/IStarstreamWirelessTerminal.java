package meowmel.pollution.api.starstream;

import net.minecraft.world.entity.player.Player;
import javax.annotation.Nullable;
import java.util.UUID;

public interface IStarstreamWirelessTerminal {
    StarstreamWirelessBinding getStarstreamWirelessBinding();
    default boolean canBindStarstreamNetwork(Player player, UUID networkId) { return true; }
    default void onStarstreamNetworkChanged(@Nullable UUID networkId) {}
}
