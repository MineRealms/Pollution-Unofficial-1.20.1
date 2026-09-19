package meowmel.pollution.common.machine.multiblock.magic;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import meowmel.pollution.api.unification.PollutionMaterials;
import meowmel.pollution.common.block.PollutionMagicBlocks;
import meowmel.pollution.common.machine.multiblock.MagicMultiblockController;
import meowmel.pollution.common.machine.multiblock.MagicStructureElements;

/**
 * Magic macerator: the first ported magic multiblock and the template for the
 * remaining processing machines.
 *
 * <p>Upstream ({@code MetaTileEntityMagicMacerator}) reuses GregTech's macerator
 * recipe map on a spell-prism casing shell. The structure, casing kinds (spell
 * prism of earth / beam core 0 / baminated glass) and the single fluid import
 * hatch are ported 1:1.</p>
 */
public class MagicMaceratorMachine extends MagicMultiblockController {

    public MagicMaceratorMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    public Material getMaterial() {
        return PollutionMaterials.InfusedEarth;
    }

    public static BlockPattern createPattern(MultiblockMachineDefinition definition) {
        return FactoryBlockPattern.start()
                .aisle("XXXXX", "XXXXX", "XXXXX", "XXXXX")
                .aisle("XXXXX", "XCCCX", "XCCCX", "X#G#X")
                .aisle("XXXXX", "XCCCX", "XCCCX", "XGGGX")
                .aisle("XXXXX", "XCCCX", "XCCCX", "X#G#X")
                .aisle("XXIXX", "XXSXX", "XXXXX", "XXXXX")
                .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                .where('X', MagicStructureElements.magicCasing(PollutionMagicBlocks.SPELL_PRISM_EARTH.get(),
                        GTRecipeTypes.MACERATOR_RECIPES))
                .where('C', Predicates.blocks(PollutionMagicBlocks.BEAM_CORE_0.get()))
                .where('G', Predicates.blocks(PollutionMagicBlocks.BAMINATED_GLASS.get()))
                .where('I', Predicates.abilities(PartAbility.IMPORT_FLUIDS).setMaxGlobalLimited(1))
                .where('#', Predicates.air())
                .build();
    }
}
