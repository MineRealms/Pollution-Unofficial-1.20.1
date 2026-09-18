package meowmel.pollution.common.machine.multiblock.botania;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import meowmel.pollution.common.machine.multiblock.mana.ManaMultiblockController;

/**
 * Mana infusion reactor: the Thaumcraft/Botania infusion altar shell.
 *
 * <p>Upstream origin: {@code MetaTileEntityManaInfusionReactor} (1.12.2,
 * package {@code multiblock.bot}). It ran the mana infusion map on a shell of
 * arcane stone bricks, livingrock variants, runic matrices, laminated glass,
 * beam core IV, mana pylons and livingwood planks.</p>
 *
 * <p>Deviations: the 1.12 Thaumcraft blocks map to Thaumcraft 4R 1.20.1
 * ({@code BlocksTC.stoneArcaneBrick} to {@code TCBlocks.ARCANE_STONE_BRICKS},
 * {@code BlocksTC.infusionMatrix} to {@code TCBlocks.RUNIC_MATRIX}); the KQGold
 * beam core maps to the port's {@code BEAM_CORE_4}.</p>
 */
public class ManaInfusionReactorMachine extends ManaMultiblockController {

    public ManaInfusionReactorMachine(IMachineBlockEntity holder) {
        super(holder, BotaniaRecipeMaps.MANA_INFUSION_RECIPES);
    }

    public static BlockPattern createPattern(MultiblockMachineDefinition definition) {
        return ManaInfusionReactorPatterns.create(definition);
    }
}
