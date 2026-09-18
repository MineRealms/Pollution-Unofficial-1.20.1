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

public class MagicMixerMachine extends MagicMultiblockController {

    public MagicMixerMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    public Material getMaterial() {
        return PollutionMaterials.InfusedEntropy;
    }

    public static BlockPattern createPattern(MultiblockMachineDefinition definition) {
        return FactoryBlockPattern.start()
                .aisle("#XXX#", "#XXX#", "#XXX#", "#XXX#", "#XXX#", "##G##")
                .aisle("XXXXX", "XACAX", "XAAAX", "XACAX", "XAAAX", "##G##")
                .aisle("XXXXX", "XCPCX", "XAPAX", "XCPCX", "XAPAX", "GGGGG")
                .aisle("XXXXX", "XACAX", "XAAAX", "XACAX", "XAAAX", "##G##")
                .aisle("#XXX#", "#XSX#", "#XXX#", "#XXX#", "#XXX#", "##G##")
                .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                .where('X', MagicStructureElements.magicCasing(PollutionMagicBlocks.SPELL_PRISM_VOID.get(),
                        GTRecipeTypes.MIXER_RECIPES))
                .where('P', Predicates.blocks(PollutionMagicBlocks.BEAM_CORE_3.get()))
                .where('C', Predicates.blocks(PollutionMagicBlocks.POLYTETRAFLUOROETHYLENE_PIPE.get()))
                .where('G', Predicates.blocks(PollutionMagicBlocks.DAMINATED_GLASS.get()))
                .where('A', Predicates.air())
                .where('#', Predicates.any())
                .build();
    }
}
