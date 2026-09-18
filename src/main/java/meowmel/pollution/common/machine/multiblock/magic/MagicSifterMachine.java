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

public class MagicSifterMachine extends MagicMultiblockController {

    public MagicSifterMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    public Material getMaterial() {
        return PollutionMaterials.InfusedCrystal;
    }

    public static BlockPattern createPattern(MultiblockMachineDefinition definition) {
        return FactoryBlockPattern.start()
                .aisle("#X#X#", "#X#X#", "#YYY#", "#YYY#", "#YYY#")
                .aisle("XXXXX", "X#X#X", "YCCCY", "YCCCY", "YEEEY")
                .aisle("#XXX#", "#X#X#", "YCCCY", "YCCCY", "YEEEY")
                .aisle("XXXXX", "X#X#X", "YCCCY", "YCCCY", "YEEEY")
                .aisle("#X#X#", "#X#X#", "#YYY#", "#YSY#", "#YYY#")
                .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                .where('X', MagicStructureElements.magicCasing(PollutionMagicBlocks.SPELL_PRISM_EARTH.get(),
                        GTRecipeTypes.SIFTER_RECIPES))
                .where('Y', Predicates.blocks(PollutionMagicBlocks.SPELL_PRISM_EARTH.get()))
                .where('C', Predicates.blocks(PollutionMagicBlocks.BEAM_CORE_3.get()))
                .where('E', Predicates.blocks(PollutionMagicBlocks.BAMINATED_GLASS.get()))
                .where('#', Predicates.any())
                .build();
    }
}
