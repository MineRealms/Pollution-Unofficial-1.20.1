package meowmel.pollution.api.unification;

import com.gregtechceu.gtceu.api.data.chemical.Element;

/**
 * Custom elements used by Pollution materials.
 *
 * <p>Ported from the 1.12.2 project {@code meowmel.pollution.api.unification.Elements}
 * (symbols, proton/neutron counts and names match the original). The upstream
 * file also declared {@code Wma}/{@code Bma}/{@code El}/{@code St}
 * (White/Blackmansus, Elven, Starrymansus); those were ported together with
 * their materials, and no symbol collides with {@code GTElements}.</p>
 */
public final class PollutionElements {

    private PollutionElements() {}

    public static final Element Ae = new Element(1, 1, -1, null, "Air", "Ae", false);
    public static final Element Ig = new Element(1, 2, -1, null, "Fire", "Ig", false);
    public static final Element Aq = new Element(1, 3, -1, null, "Water", "Aq", false);
    public static final Element Ter = new Element(1, 4, -1, null, "Earth", "Ter", false);
    public static final Element Pe = new Element(1, 5, -1, null, "Entropy", "Pe", false);
    public static final Element Ord = new Element(1, 6, -1, null, "Order", "Ord", false);
    // 上游: Wma/Bma/Kqt/El/St/Sen/Bin/Exn/Fan（元素序号沿用上游 9-17）
    public static final Element Wma = new Element(1, 9, -1, null, "Whitemansus", "Wma", false);
    public static final Element Bma = new Element(1, 10, -1, null, "Blackmansus", "Bma", false);
    public static final Element Kqt = new Element(1, 11, -1, null, "Keqinggold", "Kqt", false);
    public static final Element El = new Element(1, 12, -1, null, "Elven", "El", false);
    public static final Element St = new Element(1, 13, -1, null, "Starrymansus", "St", false);
    public static final Element Sen = new Element(1, 14, -1, null, "Sentience", "Sen", false);
    public static final Element Bin = new Element(1, 15, -1, null, "Binding", "Bin", false);
    public static final Element Exn = new Element(1, 16, -1, null, "ExistingNexus", "Exn", false);
    public static final Element Fan = new Element(1, 17, -1, null, "FadingNexus", "Fan", false);

    /** Forces class initialisation from {@code IGTAddon#registerElements()}. */
    public static void init() {}
}
