package meowmel.pollution.api.capability;

import meowmel.pollution.api.amplification.TarotHatchView;

/**
 * Holds the active tarot card used as a non-consumable recipe authorization.
 *
 * <p>Port of upstream {@code meowmel.pollution.api.capability.ITarotHatch}. It
 * extends the port's {@link TarotHatchView} so a hatch instance can be passed
 * straight to {@code MagicAmplificationEngine#calculate} while machine code
 * still gets the {@code hasTarot} / focus-lock members upstream had.</p>
 */
public interface ITarotHatch extends TarotHatchView {

    /** True when the inserted card's stable id equals {@code tarotId}. */
    boolean hasTarot(String tarotId);

    /** Locks the focus slot while a recipe is running. */
    void setFocusLocked(boolean locked);

    boolean isFocusLocked();
}
