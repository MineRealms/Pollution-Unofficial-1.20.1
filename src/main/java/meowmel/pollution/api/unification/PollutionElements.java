package meowmel.pollution.api.unification;

import com.gregtechceu.gtceu.api.data.chemical.Element;

/**
 * Custom elements used by Pollution materials.
 *
 * <p>Ported from the 1.12.2 project {@code meowmel.pollution.api.unification.Elements}
 * (symbols, proton/neutron counts and names match the original).</p>
 */
public final class PollutionElements {

    private PollutionElements() {}

    public static final Element Ae = new Element(1, 1, -1, null, "Air", "Ae", false);
    public static final Element Ig = new Element(1, 2, -1, null, "Fire", "Ig", false);
    public static final Element Aq = new Element(1, 3, -1, null, "Water", "Aq", false);
    public static final Element Ter = new Element(1, 4, -1, null, "Earth", "Ter", false);
    public static final Element Pe = new Element(1, 5, -1, null, "Entropy", "Pe", false);
    public static final Element Ord = new Element(1, 6, -1, null, "Order", "Ord", false);

    /** Forces class initialisation from {@code IGTAddon#registerElements()}. */
    public static void init() {}
}
