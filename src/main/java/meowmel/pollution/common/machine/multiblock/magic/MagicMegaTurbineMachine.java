package meowmel.pollution.common.machine.multiblock.magic;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import meowmel.pollution.api.metatileentity.POMultiblockAbility;
import meowmel.pollution.common.block.PollutionMagicBlocks;
import meowmel.pollution.common.machine.multiblock.MagicMultiblockController;

/**
 * Magic mega turbine: the nine-layer magic turbine variant. Same fuel map and
 * deviations as the large turbine; the GTQT reinforced rotor holder maps to the
 * standard rotor holder.
 */
public class MagicMegaTurbineMachine extends MagicMultiblockController {

    public MagicMegaTurbineMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    public static BlockPattern createPattern(MultiblockMachineDefinition definition) {
        return FactoryBlockPattern.start()
                .aisle("CCCCCCC", "CCCCCCC", "CCMMMCC", "CCMMMCC", "CCMMMCC", "CCCCCCC", "CCCCCCC")
                .aisle("CCCCCCC", "RGGGGGR", "CCCCCCC", "CCCCCCC", "CCCCCCC", "RGGGGGR", "CCCCCCC")
                .aisle("CCCCCCC", "CCCCCCC", "CCCCCCC", "CCCCCCC", "CCCCCCC", "CCCCCCC", "CCCCCCC")
                .aisle("CCCCCCC", "CCCCCCC", "CCCCCCC", "CCCCCCC", "CCCCCCC", "CCCCCCC", "CCCCCCC")
                .aisle("CCCCCCC", "RGGGGGR", "CCCCCCC", "CCCCCCC", "CCCCCCC", "RGGGGGR", "CCCCCCC")
                .aisle("CCCCCCC", "CCCCCCC", "CCCCCCC", "CCCCCCC", "CCCCCCC", "CCCCCCC", "CCCCCCC")
                .aisle("CCCCCCC", "CCCCCCC", "CCCCCCC", "CCCCCCC", "CCCCCCC", "CCCCCCC", "CCCCCCC")
                .aisle("CCCCCCC", "RGGGGGR", "CCCCCCC", "CCCCCCC", "CCCCCCC", "RGGGGGR", "CCCCCCC")
                .aisle("CCCCCCC", "CAAAAAC", "CAAAAAC", "CAASAAC", "CAAAAAC", "CAAAAAC", "CCCCCCC")
                .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                .where('C', Predicates.blocks(PollutionMagicBlocks.SPELL_PRISM_VOID.get()))
                .where('G', Predicates.blocks(PollutionMagicBlocks.TUNGSTENSTEEL_GEARBOX.get()))
                .where('R', Predicates.abilities(PartAbility.ROTOR_HOLDER))
                .where('M', Predicates.abilities(PartAbility.MUFFLER))
                .where('A', Predicates.blocks(PollutionMagicBlocks.SPELL_PRISM_VOID.get())
                        .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setMinGlobalLimited(1).setMaxGlobalLimited(4))
                        .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setMinGlobalLimited(1).setMaxGlobalLimited(4))
                        .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(1))
                        .or(Predicates.abilities(PartAbility.OUTPUT_ENERGY).setMaxGlobalLimited(3))
                        .or(Predicates.abilities(POMultiblockAbility.MANA_OUTPUT_HATCH).setMaxGlobalLimited(8))
                        .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                .build();
    }
}
