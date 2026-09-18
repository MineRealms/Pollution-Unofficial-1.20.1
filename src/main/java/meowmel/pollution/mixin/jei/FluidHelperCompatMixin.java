package meowmel.pollution.mixin.jei;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Compatibility shim between GregTech CEu Modern and JEI 15.40+.
 *
 * <p>GTCEu's {@code gtceu.mixins.json:jei.FluidHelperMixin} injects into
 * {@code FluidHelper.getTooltip(ITooltipBuilder, FluidStack, TooltipFlag)}.
 * JEI changed that overload to {@code getTooltip(List&lt;Component&gt;, ...)} somewhere
 * between 15.35 and 15.40, so Mixin finds a same-named method with a different
 * descriptor and aborts with {@code InvalidInjectionException} (a hard crash,
 * {@code require = 0} does not help for descriptor mismatches).</p>
 *
 * <p>This mixin is registered with config priority 900, i.e. before GTCEu's
 * default 1000, and re-adds the legacy overload as a no-op so the GT injection
 * resolves. JEI itself routes tooltips through the {@code List} overload, so the
 * only functional difference is that GT's extra fluid tooltip lines are not
 * printed by JEI.</p>
 */
@Mixin(targets = "mezz.jei.forge.platform.FluidHelper", remap = false)
public abstract class FluidHelperCompatMixin {

    public void getTooltip(ITooltipBuilder tooltip, FluidStack ingredient, TooltipFlag tooltipFlag) {
        // Intentionally empty: present only so GTCEu's injection has a target.
    }
}
