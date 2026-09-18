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

public class MagicSolidifierMachine extends MagicMultiblockController {

    public MagicSolidifierMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    public Material getMaterial() {
        return PollutionMaterials.InfusedExchange;
    }

    public static BlockPattern createPattern(MultiblockMachineDefinition definition) {
        return FactoryBlockPattern.start()
                .aisle("#XXX#####", "#XXX#####", "#XXX#####", "#XXX#####", "#XXX#####", "##G######", "#########")
                .aisle("XXXXXPYYY", "XXXXXPYYY", "XXXXX#YYY", "XXXXX#YYY", "XCCCX#YYY", "##G###GGG", "#######G#")
                .aisle("XXXXXPYYY", "XXXXXPYCY", "XXXXX#YCY", "XXXXX#YCY", "XCCCX#YCY", "GGGGG#GCG", "######GGG")
                .aisle("XXXXXPYYY", "XXXXXPYYY", "XXXXX#YYY", "XXXXX#YYY", "XCCCX#YYY", "##G###GGG", "#######G#")
                .aisle("#XXX#####", "#XXX#####", "#XSX#####", "#XXX#####", "#XXX#####", "##G######", "#########")
                .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                .where('X', MagicStructureElements.magicCasing(PollutionMagicBlocks.SPELL_PRISM_ORDER.get(),
                        GTRecipeTypes.FLUID_SOLIDFICATION_RECIPES, GTRecipeTypes.EXTRACTOR_RECIPES,
                        GTRecipeTypes.CANNER_RECIPES))
                .where('Y', Predicates.blocks(PollutionMagicBlocks.SPELL_PRISM_VOID.get()))
                .where('C', Predicates.blocks(PollutionMagicBlocks.BEAM_CORE_4.get()))
                .where('G', Predicates.blocks(PollutionMagicBlocks.DAMINATED_GLASS.get()))
                .where('P', Predicates.blocks(PollutionMagicBlocks.POLYTETRAFLUOROETHYLENE_PIPE.get()))
                .where('#', Predicates.any())
                .build();
    }
}
