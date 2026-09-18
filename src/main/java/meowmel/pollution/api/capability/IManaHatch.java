package meowmel.pollution.api.capability;

/**
 * Mana buffer contract shared by the Pollution mana hatches.
 *
 * <p>Upstream origin: {@code meowmel.pollution.api.capability.IManaHatch}
 * (1.12.2). The method set is unchanged; the port stores mana as a
 * {@code long} everywhere, while the Botania-facing bridge clamps to
 * {@code int} because Botania's own API is int-based.</p>
 */
public interface IManaHatch {

    long getMaxMana();

    long getMana();

    boolean isFull();

    int getTier();

    void receiveMana(long mana);

    default void receiveManaFromBursts(int mana) {
        receiveMana(mana);
    }

    default boolean canReceiveManaFromBursts() {
        return !isFull();
    }

    boolean consumeMana(long amount, boolean simulate);
}
