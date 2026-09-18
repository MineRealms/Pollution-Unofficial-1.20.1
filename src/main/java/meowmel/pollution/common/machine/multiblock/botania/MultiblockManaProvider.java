package meowmel.pollution.common.machine.multiblock.botania;

import meowmel.pollution.api.capability.IManaHatch;
import meowmel.pollution.api.capability.IVisHatch;
import net.minecraft.network.chat.Component;

/**
 * Mana / vis probe data of the Pollution mana machines.
 *
 * <p>Upstream origin: {@code integration.theoneprobe.MultiblockManaProvider}
 * (1.12.2), an {@code IProbeInfoProvider} that drew a yellow mana progress bar
 * for any machine implementing {@code IManaHatch}, a vis bar for
 * {@code MetaTileEntityVisHatch} and a mana bar for Botania's {@code TilePool}.</p>
 *
 * <p>Deviation: the 1.20.1 port has no TheOneProbe dependency (see
 * {@code build.gradle}), so the provider cannot implement
 * {@code IProbeInfoProvider}. The upstream bar formatting is kept as pure data
 * ({@link ManaProbe}) plus a number formatter, so a future TOP/Jade integration
 * can feed these values into its own progress widget without re-deriving them.
 * The class lives next to the Botania machines because the port's machine
 * ownership stops at this package.</p>
 */
public final class MultiblockManaProvider {

    /** Upstream bar colours (0xAARRGGBB). */
    public static final int BAR_FILLED_COLOR = 0xFFEEE600;
    public static final int BAR_BORDER_COLOR = 0xFF555555;

    /** One probe bar: current / max value plus the upstream display formatting. */
    public record ManaProbe(long current, long max, String suffix, int filledColor, int borderColor) {

        /** 0..1 fill fraction, safe for an empty (0 max) bar. */
        public double fillRatio() {
            return max <= 0L ? 0.0D : Math.max(0.0D, Math.min(1.0D, (double) current / (double) max));
        }
    }

    /** Upstream {@code IManaHatch} bar: {@code " / <max> Mana"}. */
    public static ManaProbe manaProbe(IManaHatch hatch) {
        return new ManaProbe(hatch.getMana(), hatch.getMaxMana(),
                " / " + formatNumbers(hatch.getMaxMana()) + " Mana",
                BAR_FILLED_COLOR, BAR_BORDER_COLOR);
    }

    /** Upstream vis hatch bar: {@code " / <max> Vis"}. */
    public static ManaProbe visProbe(IVisHatch hatch) {
        return new ManaProbe(hatch.getVisStore(), hatch.getMaxVisStore(),
                " / " + formatNumbers(hatch.getMaxVisStore()) + " Vis",
                BAR_FILLED_COLOR, BAR_BORDER_COLOR);
    }

    /** One-line summary for machine UIs. */
    public static Component describe(IManaHatch hatch) {
        ManaProbe probe = manaProbe(hatch);
        return Component.literal(formatNumbers(probe.current()) + probe.suffix());
    }

    /** Port of {@code TextFormattingUtil.formatNumbers}: grouped thousands. */
    public static String formatNumbers(long value) {
        return String.format("%,d", value);
    }

    private MultiblockManaProvider() {}
}
