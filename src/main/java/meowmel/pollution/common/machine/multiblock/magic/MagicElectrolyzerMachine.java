package meowmel.pollution.common.machine.multiblock.magic;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import meowmel.pollution.api.unification.PollutionMaterials;
import meowmel.pollution.common.block.PollutionMagicBlocks;
import meowmel.pollution.common.machine.multiblock.MagicMultiblockController;
import meowmel.pollution.common.machine.multiblock.MagicStructureElements;

public class MagicElectrolyzerMachine extends MagicMultiblockController {

    public MagicElectrolyzerMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    public Material getMaterial() {
        return PollutionMaterials.InfusedOrder;
    }

    public static BlockPattern createPattern(MultiblockMachineDefinition definition) {
        return FactoryBlockPattern.start()
                .aisle("XXXXX", "XXXXX", "XXXXX")
                .aisle("XXXXX", "XBDBX", "XCCCX")
                .aisle("XXXXX", "XBDBX", "XCCCX")
                .aisle("XXXXX", "XXSXX", "XXXXX")
                .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                .where('X', MagicStructureElements.magicCasing(PollutionMagicBlocks.SPELL_PRISM_ORDER.get(),
                        GTRecipeTypes.ELECTROLYZER_RECIPES))
                .where('C', Predicates.blocks(PollutionMagicBlocks.BEAM_CORE_2.get()))
                .where('B', Predicates.blocks(PollutionMagicBlocks.BRONZE_GEARBOX.get()))
                .where('D', Predicates.blocks(PollutionMagicBlocks.BRONZE_PIPE.get()))
                .build();
    }
}
