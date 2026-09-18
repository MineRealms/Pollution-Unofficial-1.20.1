package meowmel.pollution.api.capability;

/**
 * Provider-side view of a mana source.
 *
 * <p>Upstream origin: {@code meowmel.pollution.api.capability.ipml.IManaProvider}
 * (1.12.2), where it was unused as well. Ported for parity so future mana
 * sources can expose a stable contract; the shipped mana hatches use
 * {@link IManaHatch} instead.</p>
 */
public interface IManaProvider {

    IManaProvider getManaProvider();

    boolean drainMana(int amount, boolean sim);
}
