package meowmel.pollution.common.machine.multiblock.botania;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import meowmel.pollution.common.machine.multiblock.mana.ManaMultiblockController;

/**
 * Terra (Botania) vacuum freezer.
 *
 * <p>Upstream origin: {@code MetaTileEntityBotVacuumFreezer} (1.12.2). The
 * shell (mana plate IV, caminated glass, beam cores, Terra casing, mana pylon,
 * floating flower, dirt core) is ported 1:1 and runs GregTech's vacuum freezer
 * map; the coil-tier duration bonus of upstream
 * ({@code duration * (10 - 2 * coilTier) / 10}) is kept.</p>
 *
 * <p>Deviations: upstream used GTQT cooling coils (temperature 160/50/1 K) and
 * a GTQT cryogenic casing. The port uses GregTech heating coils
 * ({@code Predicates.heatingCoils()}) and the frost-proof aluminium casing;
 * the coil tier is the GregTech coil level capped at 3. The GTQT
 * HyperdimensionalSilver frame is substituted with a NaquadahAlloy frame.</p>
 */
public class BotVacuumFreezerMachine extends ManaMultiblockController {

    public BotVacuumFreezerMachine(IMachineBlockEntity holder) {
        super(holder, GTRecipeTypes.VACUUM_RECIPES);
    }

    /** Coil tier used by the duration bonus: 0 without coils, otherwise 1..3. */
    public int getCoilTier() {
        return coilType == null ? 0 : Math.min(3, Math.max(1, coilType.getLevel()));
    }

    @Override
    protected GTRecipe getRealRecipe(GTRecipe recipe) {
        GTRecipe modified = super.getRealRecipe(recipe);
        int coilTier = getCoilTier();
        if (modified == null || coilTier <= 0) {
            return modified;
        }
        modified = modified.copy();
        modified.duration = Math.max(1,
                Math.round(modified.duration * (10.0F - 2.0F * coilTier) / 10.0F));
        return modified;
    }

    public static BlockPattern createPattern(MultiblockMachineDefinition definition) {
        return BotVacuumFreezerPatterns.create(definition);
    }
}
