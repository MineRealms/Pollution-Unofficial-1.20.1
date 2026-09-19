package meowmel.pollution.client.debug;

import com.mojang.datafixers.util.Either;
import meowmel.pollution.Pollution;
import meowmel.pollution.compat.jei.PollutionJeiPlugin;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.common.gui.JeiTooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Diagnostic sink for {@code RecipeSlotTooltipMixin}. Appends the exact tooltip
 * lines JEI builds for a hovered recipe slot to
 * {@code config/pollution-jei-tooltip.log} so it can be compared against the
 * API-level dump written by {@link JeiDebugDumper}.
 *
 * <p>Every method is defensive: a diagnostic must never take the game down, so
 * all failures are swallowed (or written to the log itself).</p>
 */
public final class JeiTooltipLog {

    private static final String LOG_FILE_NAME = "pollution-jei-tooltip.log";
    private static final Object LOCK = new Object();

    private static String lastBody;

    private JeiTooltipLog() {}

    public static void logSlotTooltip(IRecipeSlotDrawable slot) {
        try {
            String body = buildBody(slot);
            synchronized (LOCK) {
                if (body.equals(lastBody)) {
                    return;
                }
                lastBody = body;
            }
            write("=== Pollution JEI recipe slot tooltip ===\n"
                    + "timestamp: " + ZonedDateTime.now() + '\n'
                    + body
                    + '\n');
        } catch (Throwable ignored) {
            // Never crash on diagnostics.
        }
    }

    public static void logFailure(Throwable throwable) {
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("=== Pollution JEI recipe slot tooltip FAILED ===\n");
            builder.append("timestamp: ").append(ZonedDateTime.now()).append('\n');
            builder.append("error: ").append(throwable).append('\n');
            for (StackTraceElement element : throwable.getStackTrace()) {
                builder.append("    at ").append(element).append('\n');
            }
            builder.append('\n');
            write(builder.toString());
        } catch (Throwable ignored) {
            // Never crash on diagnostics.
        }
    }

    private static String buildBody(IRecipeSlotDrawable slot) {
        StringBuilder builder = new StringBuilder();
        builder.append("role: ").append(slot.getRole()).append('\n');

        Optional<ITypedIngredient<?>> displayed = slot.getDisplayedIngredient();
        if (displayed.isEmpty()) {
            builder.append("ingredient: (empty)\n");
        } else {
            appendIngredient(builder, displayed.get());
        }

        appendTooltip(builder, slot);
        return builder.toString();
    }

    private static void appendIngredient(StringBuilder builder, ITypedIngredient<?> typedIngredient) {
        Object raw = typedIngredient.getIngredient();
        builder.append("ingredientType: ").append(typedIngredient.getType().getUid()).append('\n');
        builder.append("ingredientClass: ")
                .append(raw == null ? "null" : raw.getClass().getName()).append('\n');

        IJeiRuntime runtime = PollutionJeiPlugin.getRuntime();
        if (runtime != null && raw != null) {
            try {
                IIngredientManager ingredientManager = runtime.getIngredientManager();
                IIngredientHelper<Object> helper = ingredientManager.getIngredientHelper(raw);
                builder.append("helperClass: ").append(helper.getClass().getName()).append('\n');
                builder.append("helperDisplayName: ").append(helper.getDisplayName(raw)).append('\n');
            } catch (Throwable throwable) {
                builder.append("helperDisplayName: <error: ").append(throwable).append(">\n");
            }
        }

        if (raw instanceof FluidStack fluidStack) {
            builder.append("fluid.registryKey: ")
                    .append(ForgeRegistries.FLUIDS.getKey(fluidStack.getFluid())).append('\n');
            builder.append("fluid.amount: ").append(fluidStack.getAmount()).append('\n');
            builder.append("fluid.displayName: ")
                    .append(fluidStack.getDisplayName().getString()).append('\n');
            String descriptionId;
            try {
                descriptionId = fluidStack.getFluid().getFluidType().getDescriptionId();
            } catch (Throwable throwable) {
                descriptionId = "<error: " + throwable + ">";
            }
            builder.append("fluid.descriptionId: ").append(descriptionId).append('\n');
            builder.append("fluid.translatedDescription: ")
                    .append(Component.translatable(descriptionId).getString()).append('\n');
        }
    }

    private static void appendTooltip(StringBuilder builder, IRecipeSlotDrawable slot) {
        List<Either<FormattedText, TooltipComponent>> lines;
        try {
            JeiTooltip tooltip = new JeiTooltip();
            slot.getTooltip(tooltip);
            lines = tooltip.getLines();
        } catch (Throwable throwable) {
            builder.append("tooltip: <error: ").append(throwable).append(">\n");
            return;
        }
        if (lines == null || lines.isEmpty()) {
            builder.append("tooltip: (empty)\n");
            return;
        }
        builder.append("tooltipLines: ").append(lines.size()).append('\n');
        for (int i = 0; i < lines.size(); i++) {
            Either<FormattedText, TooltipComponent> line = lines.get(i);
            builder.append("  [").append(i + 1).append("] ");
            if (line == null) {
                builder.append("(null)");
            } else {
                line.ifLeft(text -> builder.append(text.getString()));
                line.ifRight(component -> builder.append("<component: ")
                        .append(component.getClass().getName()).append(">"));
            }
            builder.append('\n');
        }
    }

    private static void write(String content) {
        try {
            Path file = FMLPaths.CONFIGDIR.get().resolve(LOG_FILE_NAME);
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(file, content, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND,
                    StandardOpenOption.WRITE);
        } catch (IOException ioException) {
            Pollution.LOGGER.warn("[Pollution] Could not write JEI tooltip log", ioException);
        }
    }
}
