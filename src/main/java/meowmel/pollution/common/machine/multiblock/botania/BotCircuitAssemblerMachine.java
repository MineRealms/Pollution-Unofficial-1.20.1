package meowmel.pollution.common.machine.multiblock.botania;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import meowmel.pollution.common.machine.multiblock.mana.ManaMultiblockController;

/**
 * Terra (Botania) circuit assembler.
 *
 * <p>Upstream origin: {@code MetaTileEntityBotCircuitAssembler} (1.12.2). The
 * shell (mana plate V, filter casing, fusion glass, daminated glass, tiered
 * frames, beam core 4) and the frame-tier duration bonus
 * ({@code duration * (10 - frameLevel) / 10}) are kept; the machine runs
 * GregTech's circuit assembler map.</p>
 *
 * <p>Deviations: upstream matched GTQT frames through
 * {@code POTieredCasingGroups.frames()} and read their tier from the formed
 * structure. The port matches GTCEu frames of comparable progression
 * (stainless steel .. naquadah alloy) through
 * {@code BotaniaStructureElements#tieredFrames()} and records the same tier
 * value. The BloodOfAvernus frame of the outer shell is substituted with a
 * TungstenSteel frame.</p>
 */
public class BotCircuitAssemblerMachine extends ManaMultiblockController {

    private int frameLevel;

    public BotCircuitAssemblerMachine(IMachineBlockEntity holder) {
        super(holder, GTRecipeTypes.CIRCUIT_ASSEMBLER_RECIPES);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        frameLevel = BotaniaStructureElements.frameTier(this);
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        frameLevel = 0;
    }

    public int getFrameLevel() {
        return frameLevel;
    }

    @Override
    protected GTRecipe getRealRecipe(GTRecipe recipe) {
        GTRecipe modified = super.getRealRecipe(recipe);
        if (modified == null || frameLevel <= 0) {
            return modified;
        }
        modified = modified.copy();
        modified.duration = Math.max(1,
                Math.round(modified.duration * (10.0F - frameLevel) / 10.0F));
        return modified;
    }

    public static BlockPattern createPattern(MultiblockMachineDefinition definition) {
        return BotCircuitAssemblerPatterns.create(definition);
    }
}
