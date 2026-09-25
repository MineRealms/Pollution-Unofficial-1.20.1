package meowmel.pollution.api.starstream;

import java.util.Map;
import java.util.Set;

/** Limits and channel identities of the 1.12 directed Starstream network. */
public final class StarstreamNetwork {
    public static final Set<String> CHANNELS = Set.of("aevitas", "evorsio", "armara", "discidia",
            "vicio", "mineralis", "fornax", "horologium", "lucerna", "octans", "bootes",
            "pelotrio", "gelu", "ulteria", "alcara", "vorux");
    public static final int LINK_RANGE = 256;
    public static final int MAX_HOPS = 16;
    public static final int MAX_INPUTS = 64;
    public static final int MAX_RELAY_INPUTS = 16;
    public static final long CAPACITY_PER_CHANNEL = 1_677_721_600L;
    public static final long CORE_OUTPUT = 524_288L;
    public static final long RELAY_OUTPUT = 32_768L;
    public static final long GATEWAY_OUTPUT = 65_536L;
    public static final long TERMINAL_OUTPUT = 8_192L;

    private StarstreamNetwork() {}

    /** -1 denotes an invalid request, including overflowing or unknown channels. */
    public static long total(Map<String, Long> requirements) {
        if (requirements == null || requirements.isEmpty()) return -1;
        long total = 0;
        for (var entry : requirements.entrySet()) {
            Long amount = entry.getValue();
            if (entry.getKey() == null || !CHANNELS.contains(entry.getKey()) || amount == null || amount < 0
                    || Long.MAX_VALUE - total < amount) return -1;
            total += amount;
        }
        return total;
    }
}
