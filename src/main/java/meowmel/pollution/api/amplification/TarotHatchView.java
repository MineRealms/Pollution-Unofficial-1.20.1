package meowmel.pollution.api.amplification;

/**
 * Minimal view of a tarot hatch.
 *
 * <p>Upstream this role was played by {@code meowmel.pollution.api.capability.ITarotHatch}.
 * That capability interface is not part of the 1.20.1 port yet, so this package
 * declares the exact subset the amplification API consumes. When the capability
 * package is ported, {@code ITarotHatch} should extend this interface so machine
 * code can keep passing hatches to {@link MagicAmplificationEngine#calculate} and
 * {@link MagicEnergyAmplification#read} unchanged.</p>
 */
public interface TarotHatchView {

    /** Active major-arcana id, e.g. {@code "the_star"}; empty when no card is inserted. */
    String getActiveTarot();
}
