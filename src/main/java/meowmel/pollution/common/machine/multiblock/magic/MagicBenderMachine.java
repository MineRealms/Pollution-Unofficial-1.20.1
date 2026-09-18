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

public class MagicBenderMachine extends MagicMultiblockController {

    public MagicBenderMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    public Material getMaterial() {
        return PollutionMaterials.InfusedMetal;
    }

    public static BlockPattern createPattern(MultiblockMachineDefinition definition) {
        return FactoryBlockPattern.start()
                .aisle("XXXXXXX", "XXXXXXX", "XXXXXXX")
                .aisle("XXXXXXX", "XXXGGGX", "XXXXXXX")
                .aisle("XXXXXXX", "XSXCCCX", "XXXXXXX")
                .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                .where('X', MagicStructureElements.magicCasing(PollutionMagicBlocks.SPELL_PRISM_ORDER.get(),
                        GTRecipeTypes.BENDER_RECIPES, GTRecipeTypes.COMPRESSOR_RECIPES,
                        GTRecipeTypes.FORMING_PRESS_RECIPES, GTRecipeTypes.FORGE_HAMMER_RECIPES))
                .where('G', Predicates.blocks(PollutionMagicBlocks.STAINLESS_STEEL_GEARBOX.get()))
                .where('C', Predicates.blocks(PollutionMagicBlocks.LAMINATED_GLASS.get()))
                .build();
    }
}
