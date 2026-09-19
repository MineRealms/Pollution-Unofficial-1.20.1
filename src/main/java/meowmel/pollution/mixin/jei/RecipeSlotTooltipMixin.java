package meowmel.pollution.mixin.jei;

import meowmel.pollution.client.debug.JeiTooltipLog;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.library.gui.ingredients.RecipeSlot;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Diagnostic-only mixin: logs the exact tooltip lines JEI renders for a
 * hovered recipe slot.
 *
 * <p>JEI 15.56 routes recipe-slot hover tooltips through
 * {@code RecipeSlot#drawTooltip(GuiGraphics, int, int)} (called from
 * {@code RecipeLayout#drawOverlays}), which builds a {@code JeiTooltip} via the
 * private {@code addTooltip(ITooltipBuilder)} chain. At HEAD this mixin rebuilds
 * the very same tooltip with the public
 * {@code getTooltip(ITooltipBuilder)} entry point and hands it to
 * {@link JeiTooltipLog}.</p>
 *
 * <p>Applying is gated by {@code PollutionMixinPlugin}, so the class is never
 * touched when JEI is absent.</p>
 */
@Mixin(value = RecipeSlot.class, remap = false)
public abstract class RecipeSlotTooltipMixin {

    @Inject(method = "drawTooltip(Lnet/minecraft/client/gui/GuiGraphics;II)V", at = @At("HEAD"))
    private void pollution$logSlotTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY,
                                          CallbackInfo callbackInfo) {
        try {
            JeiTooltipLog.logSlotTooltip((IRecipeSlotDrawable) this);
        } catch (Throwable throwable) {
            JeiTooltipLog.logFailure(throwable);
        }
    }
}
