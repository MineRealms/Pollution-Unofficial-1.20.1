package meowmel.pollution.mixin.jei;

import com.lowdragmc.lowdraglib.gui.ingredient.IRecipeIngredientSlot;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Restores slot tooltips on GregTech/LDLib recipe pages under JEI 15.56.
 *
 * <p>LDLib renders every recipe ingredient through the anonymous
 * {@code com.lowdragmc.lowdraglib.jei.ModularUIRecipeCategory$2} renderer. That
 * class implements JEI's abstract
 * {@code IIngredientRenderer#getTooltip(T, TooltipFlag)} as a stub returning
 * {@code Collections.emptyList()} and keeps the real lines in
 * {@code getTooltip(ITooltipBuilder, T, TooltipFlag)}, which adds
 * {@code slot.getFullTooltipTexts()}.</p>
 *
 * <p>JEI 15.56's {@code SafeIngredientUtil.getRichTooltip} (used by
 * {@code RecipeSlot.addTooltip}) invokes the four-arg
 * {@code getTooltip(ITooltipBuilder, T, Player, TooltipFlag)} default. That
 * default falls back to the three-arg {@code List} default, which falls back to
 * the stubbed two-arg method — so every GT/LDLib ingredient renders an empty
 * tooltip. Cancelling the two-arg call and returning the captured slot's full
 * tooltip lines fixes the rich path (via JEI's fallback chain) as well as the
 * plain {@code getPlainTooltipForSearch} path.</p>
 *
 * <p>The captured {@code val$slot} field is read reflectively instead of via
 * {@code @Shadow}: Mixin AP 0.8.5's
 * {@code TypeHandleASM.findField} feeds each target field descriptor through
 * {@code TypeUtils.getJavaSignature}, which interprets it as a method
 * descriptor and throws {@code StringIndexOutOfBoundsException} for the 63-char
 * descriptor of this field, aborting compilation.</p>
 *
 * <p>Signatures verified with {@code javap -p} against
 * {@code ldlib-forge-1.20.1-1.0.40.b} (compile classpath, deobf),
 * {@code ldlib-forge-1.20.1-1.0.50} and {@code ldlib-forge-1.20.1-1.0.52.a}
 * (target pack): all declare
 * {@code final IRecipeIngredientSlot val$slot},
 * {@code List<Component> getTooltip(Object, TooltipFlag)} (stub) and
 * {@code void getTooltip(ITooltipBuilder, Object, TooltipFlag)} (rich).
 * {@code require = 0} keeps the mixin inert (logged, not fatal) if a future
 * LDLib removes the stubbed method; the handler body is additionally wrapped in
 * a try/catch so a broken slot can never take the recipe screen down.</p>
 */
@Mixin(targets = "com.lowdragmc.lowdraglib.jei.ModularUIRecipeCategory$2", remap = false)
public abstract class LdlibRecipeSlotTooltipMixin {

    @Inject(
            method = "getTooltip(Ljava/lang/Object;Lnet/minecraft/world/item/TooltipFlag;)Ljava/util/List;",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private void pollution$restoreSlotTooltip(Object ingredient, TooltipFlag tooltipFlag,
                                              CallbackInfoReturnable<List<Component>> cir) {
        List<Component> lines = List.of();
        try {
            Field slotField = this.getClass().getDeclaredField("val$slot");
            slotField.setAccessible(true);
            Object slot = slotField.get(this);
            if (slot instanceof IRecipeIngredientSlot ingredientSlot) {
                List<Component> fullTooltip = ingredientSlot.getFullTooltipTexts();
                if (fullTooltip != null) {
                    lines = fullTooltip;
                }
            }
        } catch (Throwable ignored) {
            // Keep the empty fallback rather than crashing the recipe screen.
        }
        cir.setReturnValue(lines);
    }
}
