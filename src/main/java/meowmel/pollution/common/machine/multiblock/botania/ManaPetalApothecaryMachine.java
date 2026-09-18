package meowmel.pollution.common.machine.multiblock.botania;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import meowmel.pollution.common.machine.multiblock.mana.ManaMultiblockController;

/**
 * Mana petal apothecary.
 *
 * <p>Upstream origin: {@code MetaTileEntityManaPetalApothecary} (1.12.2), a
 * Botania-styled shell (livingrock brick / chiseled brick, livingwood planks,
 * floating flower, mana pylon, GTQT Mansussteel frame, beam core 4) running the
 * mana petal recipe map with the standard mana hatch layout.</p>
 *
 * <p>Deviation: the GTQT Mansussteel frame is substituted with a GTCEu HSSG
 * frame (see {@code MagicStructureElements#frame}); block variants collapse to
 * their 1.20.1 Botania flattened blocks.</p>
 */
public class ManaPetalApothecaryMachine extends ManaMultiblockController {

    public ManaPetalApothecaryMachine(IMachineBlockEntity holder) {
        super(holder, BotaniaRecipeMaps.MANA_PETAL_RECIPES);
    }

    public static BlockPattern createPattern(MultiblockMachineDefinition definition) {
        return ManaPetalApothecaryPatterns.create(definition);
    }
}
