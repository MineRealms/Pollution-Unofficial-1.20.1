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

public class MagicCentrifugeMachine extends MagicMultiblockController {

    public MagicCentrifugeMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    public Material getMaterial() {
        return PollutionMaterials.InfusedAir;
    }

    public static BlockPattern createPattern(MultiblockMachineDefinition definition) {
        return FactoryBlockPattern.start()
                .aisle("BXXXB", "XXXXX", "BXXXB")
                .aisle("XXXXX", "XHGHX", "XXXXX")
                .aisle("XXXXX", "XGHGX", "XXXXX")
                .aisle("XXXXX", "XHGHX", "XXXXX")
                .aisle("BXXXB", "XXSXX", "BXXXB")
                .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                .where('X', MagicStructureElements.magicCasing(PollutionMagicBlocks.SPELL_PRISM_AIR.get(),
                        GTRecipeTypes.CENTRIFUGE_RECIPES, GTRecipeTypes.THERMAL_CENTRIFUGE_RECIPES))
                .where('B', Predicates.any())
                .where('G', Predicates.blocks(PollutionMagicBlocks.STEEL_PIPE.get()))
                .where('H', Predicates.blocks(PollutionMagicBlocks.STEEL_GEARBOX.get()))
                .build();
    }
}
