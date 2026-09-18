package meowmel.pollution.api.pollution;

import meowmel.pollution.PollutionConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/**
 * Chunk-pollution entry points for Pollution machine events.
 *
 * <p>Upstream (1.12.2) applied both effects in its {@code MetaTileEntity}
 * mixin: {@code pollution(amount, ticks)} scaled the workable/muffler
 * pollution by {@code mufflerPollutionMultiplier}, and {@code executeExplosion}
 * scaled the explosion power by the same config while gating it on
 * {@code enableExplosionPollution}. Modern GregTech removed the pollution
 * hook, so the port exposes both calculations here and calls them from the
 * closest modern equivalents.</p>
 */
public final class MachinePollution {

    /**
     * Explosion power used for the port's self-destructing machines. The flux
     * promoted fuel cell explodes with power {@code 1.0F}
     * ({@code FluxFuelCellMachine#pollutionTick}) and the Forge explosion event
     * does not expose the radius, so the known power is used directly.
     */
    public static final float MACHINE_EXPLOSION_POWER = 1.0F;

    private MachinePollution() {}

    /**
     * Adds the chunk pollution of a machine explosion at {@code pos}.
     *
     * <p>Mirrors upstream {@code MixinMetaTileEntity#executeExplosion}: nothing
     * is added when {@link PollutionConfig#ENABLE_EXPLOSION_POLLUTION} is
     * disabled, and the explosion power is scaled by
     * {@link PollutionConfig#MUFFLER_POLLUTION_MULTIPLIER} like upstream.</p>
     */
    public static void addExplosionPollution(ServerLevel level, BlockPos pos, float explosionPower) {
        if (!PollutionConfig.ENABLE_EXPLOSION_POLLUTION.get() || explosionPower <= 0.0F) {
            return;
        }
        double multiplier = PollutionConfig.MUFFLER_POLLUTION_MULTIPLIER.get();
        PollutionEngine.add(level, pos, explosionPower * multiplier);
    }

    /**
     * Adds the pollution a muffler hatch emitted for one completed operation.
     *
     * <p>Upstream scaled the per-operation amount by
     * {@link PollutionConfig#MUFFLER_POLLUTION_MULTIPLIER} inside
     * {@code MetaTileEntity#pollution}; modern GT exposes the same per-operation
     * output as {@code IMufflerMachine#getHazardStrengthPerOperation()}.</p>
     */
    public static void addMufflerPollution(ServerLevel level, BlockPos pos, double mufflerOutput) {
        double multiplier = PollutionConfig.MUFFLER_POLLUTION_MULTIPLIER.get();
        if (mufflerOutput <= 0.0D || multiplier <= 0.0D) {
            return;
        }
        PollutionEngine.add(level, pos, mufflerOutput * multiplier);
    }
}
