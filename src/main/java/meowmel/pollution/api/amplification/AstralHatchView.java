package meowmel.pollution.api.amplification;

/**
 * Minimal read-only view of a calibrated astral lens hatch.
 *
 * <p>Upstream this role was played by {@code meowmel.pollution.api.capability.IAstralHatch}.
 * That capability interface is not part of the 1.20.1 port yet, so this package
 * declares the exact subset the amplification API consumes. When the capability
 * package is ported, {@code IAstralHatch} should extend this interface so machine
 * code can keep passing hatches to {@link AstralAmplifierSnapshot#from} unchanged.</p>
 */
public interface AstralHatchView {

    int getTier();

    /** A valid constellation-data wafer is required for passive machine amplification. */
    boolean hasConstellationDataWafer();

    /** Quality of the optional cultivated rock-crystal optical insert, 0..100. */
    int getOpticalCrystalQuality();

    /** Extra constellation strength supplied by the optical insert, 0..10 percentage points. */
    double getOpticalCrystalStrengthBonus();

    String getFocusedConstellation();

    boolean isSkyVisible();

    boolean isNight();

    boolean isFocusedConstellationActive();
}
