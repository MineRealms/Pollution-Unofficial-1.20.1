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

public class MagicCutterMachine extends MagicMultiblockController {

    public MagicCutterMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    public Material getMaterial() {
        return PollutionMaterials.InfusedWeapon;
    }

    public static BlockPattern createPattern(MultiblockMachineDefinition definition) {
        return FactoryBlockPattern.start()
                .aisle("XXXXXXX", "XXXXXXX", "XXXXXXX", "##XXXXX")
                .aisle("XXXXXXX", "XAXCCCX", "XXXAAAX", "##XXXXX")
                .aisle("XXXXXXX", "XAXCCCX", "XXXAAAX", "##XXXXX")
                .aisle("XXXXXXX", "XSXDDDX", "XXXDDDX", "##XXXXX")
                .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                .where('X', MagicStructureElements.magicCasing(PollutionMagicBlocks.SPELL_PRISM_HOT.get(),
                        GTRecipeTypes.CUTTER_RECIPES))
                .where('C', Predicates.blocks(PollutionMagicBlocks.BEAM_CORE_4.get()))
                .where('D', Predicates.blocks(PollutionMagicBlocks.AAMINATED_GLASS.get()))
                .where('A', Predicates.blocks(PollutionMagicBlocks.POLYTETRAFLUOROETHYLENE_PIPE.get()))
                .where('#', Predicates.any())
                .build();
    }
}
