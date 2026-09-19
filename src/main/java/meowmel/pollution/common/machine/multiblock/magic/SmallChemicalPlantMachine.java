package meowmel.pollution.common.machine.multiblock.magic;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import meowmel.pollution.api.recipes.PORecipeMaps;
import meowmel.pollution.common.block.PollutionMagicBlocks;
import meowmel.pollution.common.machine.multiblock.MagicMultiblockController;
import meowmel.pollution.common.machine.multiblock.MagicStructureElements;

/**
 * Small chemical plant.
 *
 * <p>Upstream ran GT chemical + large chemical + GTQT chemical plant + magic
 * chemical recipes. The GTQT recipe map is not part of the port and is dropped
 * (documented deviation); the remaining three maps are kept.</p>
 */
public class SmallChemicalPlantMachine extends MagicMultiblockController {

    public SmallChemicalPlantMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    public static BlockPattern createPattern(MultiblockMachineDefinition definition) {
        return FactoryBlockPattern.start()
                .aisle("GGGGG", "BAAAB", "BAAAB", "BAAAB", "GGGGG")
                .aisle("GXXXG", "ADDDA", "ABEBA", "ADDDA", "GXXXG")
                .aisle("GXXXG", "ADDDA", "AECEA", "ADDDA", "GXXXG")
                .aisle("GXXXG", "ADDDA", "ABEBA", "ADDDA", "GXXXG")
                .aisle("GGSGG", "BAAAB", "BAAAB", "BAAAB", "GGGGG")
                .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                .where('G', Predicates.blocks(PollutionMagicBlocks.TERRA_WATERTIGHT_CASING.get())
                        .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(23))
                        .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                        .or(Predicates.abilities(PartAbility.MUFFLER).setExactLimit(1))
                        .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMinGlobalLimited(1).setMaxGlobalLimited(23))
                        .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setMinGlobalLimited(1).setMaxGlobalLimited(23))
                        .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setMinGlobalLimited(1).setMaxGlobalLimited(23))
                        .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setMinGlobalLimited(1).setMaxGlobalLimited(23)))
                .where('B', MagicStructureElements.frame(GTMaterials.HSSG))
                .where('C', Predicates.blocks(PollutionMagicBlocks.BEAM_CORE_4.get()))
                .where('D', Predicates.blocks(PollutionMagicBlocks.CAMINATED_GLASS.get()))
                .where('E', Predicates.blocks(PollutionMagicBlocks.TERRA_1_CASING.get()))
                .where('X', Predicates.blocks(PollutionMagicBlocks.TERRA_1_CASING.get()))
                .where('A', Predicates.any())
                .build();
    }
}
