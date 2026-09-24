package meowmel.pollution.api.starstream;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import java.util.Map;

/** Pulls energy only when a machine or ritual actually starts an operation. */
public interface IStarstreamOperationCore extends IStarstreamWirelessTerminal {
    long requestConstellationEnergy(Level level, BlockPos consumerPos, String channel,
                                    long amount, boolean simulate);
    boolean consumeConstellationEnergy(Level level, BlockPos consumerPos,
                                       Map<String, Long> requirements, boolean simulate);
}
