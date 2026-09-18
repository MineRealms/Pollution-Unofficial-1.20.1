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

public class MagicExtruderMachine extends MagicMultiblockController {

    public MagicExtruderMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    public Material getMaterial() {
        return PollutionMaterials.InfusedInstrument;
    }

    public static BlockPattern createPattern(MultiblockMachineDefinition definition) {
        return FactoryBlockPattern.start()
                .aisle("##XXX", "##XXX", "##XXX")
                .aisle("##XXX", "##XPX", "##XGX")
                .aisle("##XXX", "##XPX", "##XGX")
                .aisle("XXXXX", "XXXPX", "XXXGX")
                .aisle("XXXXX", "XAXPX", "XXXGX")
                .aisle("XXXXX", "XSXXX", "XXXXX")
                .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                .where('X', MagicStructureElements.magicCasing(PollutionMagicBlocks.SPELL_PRISM_ORDER.get(),
                        GTRecipeTypes.EXTRUDER_RECIPES))
                .where('P', Predicates.blocks(PollutionMagicBlocks.BEAM_CORE_0.get()))
                .where('G', Predicates.blocks(PollutionMagicBlocks.AAMINATED_GLASS.get()))
                .where('A', Predicates.blocks(PollutionMagicBlocks.STAINLESS_STEEL_GEARBOX.get()))
                .where('#', Predicates.any())
                .build();
    }
}
