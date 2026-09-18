package meowmel.pollution.common.machine.multiblock.magic;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import meowmel.pollution.api.recipes.PORecipeMaps;
import meowmel.pollution.api.unification.PollutionMaterials;
import meowmel.pollution.common.block.PollutionMagicBlocks;
import meowmel.pollution.common.machine.multiblock.MagicMultiblockController;
import meowmel.pollution.common.machine.multiblock.MagicStructureElements;

public class MagicGreenHouseMachine extends MagicMultiblockController {

    public MagicGreenHouseMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    public Material getMaterial() {
        return PollutionMaterials.InfusedWater;
    }

    public static BlockPattern createPattern(MultiblockMachineDefinition definition) {
        return FactoryBlockPattern.start()
                .aisle("CCCCC", "CCCCC", "GGGGG", "GGGGG", "CCCCC", "CCCCC")
                .aisle("CCCCC", "CPHPC", "G###G", "G###G", "CPHPC", "CDDDC")
                .aisle("CCCCC", "CHHHC", "G###G", "G###G", "CHHHC", "CDDDC")
                .aisle("CCCCC", "CPHPC", "G###G", "G###G", "CPHPC", "CDDDC")
                .aisle("CCCCC", "CCSCC", "GGGGG", "GGGGG", "CCCCC", "CCCCC")
                .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                .where('C', MagicStructureElements.magicCasing(PollutionMagicBlocks.SPELL_PRISM_WATER.get(),
                        PORecipeMaps.MAGIC_GREENHOUSE_RECIPES))
                .where('P', Predicates.blocks(PollutionMagicBlocks.BEAM_CORE_3.get()))
                .where('D', Predicates.blocks(PollutionMagicBlocks.TUNGSTENSTEEL_PIPE.get()))
                .where('H', Predicates.blocks(PollutionMagicBlocks.TUNGSTENSTEEL_GEARBOX.get()))
                .where('G', Predicates.blocks(PollutionMagicBlocks.CAMINATED_GLASS.get()))
                .where('#', Predicates.air())
                .build();
    }
}
