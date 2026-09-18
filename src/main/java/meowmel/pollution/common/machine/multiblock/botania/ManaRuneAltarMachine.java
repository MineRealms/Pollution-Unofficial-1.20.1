package meowmel.pollution.common.machine.multiblock.botania;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import meowmel.pollution.common.machine.multiblock.mana.ManaMultiblockController;

/**
 * Mana rune altar.
 *
 * <p>Upstream origin: {@code MetaTileEntityManaRuneAltar} (1.12.2), the rune
 * altar shell (livingrock brick/chiseled brick, four laminated glass kinds,
 * floating flowers, mana pylons, GTQT Mansussteel frame, beam core 4) running
 * the rune altar recipe map.</p>
 *
 * <p>Deviation: the GTQT Mansussteel frame is substituted with a GTCEu HSSG
 * frame; block variants collapse to their 1.20.1 Botania flattened blocks.</p>
 */
public class ManaRuneAltarMachine extends ManaMultiblockController {

    public ManaRuneAltarMachine(IMachineBlockEntity holder) {
        super(holder, BotaniaRecipeMaps.MANA_RUNE_ALTAR_RECIPES);
    }

    public static BlockPattern createPattern(MultiblockMachineDefinition definition) {
        return ManaRuneAltarPatterns.create(definition);
    }
}
