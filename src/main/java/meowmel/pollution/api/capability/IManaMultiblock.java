package meowmel.pollution.api.capability;

/**
 * Marker for machines that participate in the Botania mana system.
 *
 * <p>Upstream origin: {@code meowmel.pollution.api.capability.IManaMultiblock}
 * (1.12.2). The port keeps the interface verbatim for parity; nothing in the
 * modern port implements it yet, because typed mana access now happens through
 * {@link IManaHatch} and {@link ManaHandlerList}.</p>
 */
public interface IManaMultiblock {

    default boolean isMana() {
        return true;
    }

    default int getTier() {
        return 1;
    }

    default boolean work() {
        return false;
    }
}
