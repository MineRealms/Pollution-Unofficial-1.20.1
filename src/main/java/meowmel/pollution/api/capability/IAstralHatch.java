package meowmel.pollution.api.capability;

import meowmel.pollution.api.amplification.AstralHatchView;
import meowmel.pollution.api.recipes.properties.AstralCondition;

/**
 * A calibrated Astral Sorcery lens that can validate live sky conditions.
 *
 * <p>Upstream origin: {@code meowmel.pollution.api.capability.IAstralHatch}
 * (1.12.2). The port declares it as an extension of
 * {@link AstralHatchView} (as that view's javadoc already reserved), so a live
 * hatch can be handed to
 * {@link meowmel.pollution.api.amplification.AstralAmplifierSnapshot#from}
 * unchanged. No astral lens machine exists in the port yet; the magic
 * controller discovers the interface from the formed parts and fails the
 * recipe gate when no hatch matches the recipe's
 * {@link AstralCondition}.</p>
 */
public interface IAstralHatch extends AstralHatchView {

    /** Keeps the non-consumable lens/wafer stable for one running recipe. */
    void setFocusLocked(boolean locked);

    boolean isFocusLocked();

    /** True when this lens satisfies every configured field of the condition. */
    boolean matches(AstralCondition condition);

    /** Share of the lens' lensable energy currently held by the focus, 0..1. */
    float getFocusedDistribution();

    String getMoonPhase();

    String getCelestialEvent();
}
