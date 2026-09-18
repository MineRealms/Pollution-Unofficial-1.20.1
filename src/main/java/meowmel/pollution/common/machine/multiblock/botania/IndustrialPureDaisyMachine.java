package meowmel.pollution.common.machine.multiblock.botania;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import meowmel.pollution.common.machine.multiblock.mana.ManaMultiblockController;

/**
 * Industrial pure daisy.
 *
 * <p>Upstream origin: {@code MetaTileEntityIndustrialPureDaisy} (1.12.2), a
 * livingrock/floating-flower shell around the pure daisy recipe map.</p>
 *
 * <p>Deviation: the GTQT Mansussteel frame is substituted with a GTCEu HSSG
 * frame; block variants collapse to their 1.20.1 Botania flattened blocks. The
 * upstream id typo ({@code industial_pure_daisy}) is corrected to
 * {@code industrial_pure_daisy}.</p>
 */
public class IndustrialPureDaisyMachine extends ManaMultiblockController {

    public IndustrialPureDaisyMachine(IMachineBlockEntity holder) {
        super(holder, BotaniaRecipeMaps.PURE_DAISY_RECIPES);
    }

    public static BlockPattern createPattern(MultiblockMachineDefinition definition) {
        return IndustrialPureDaisyPatterns.create(definition);
    }
}
