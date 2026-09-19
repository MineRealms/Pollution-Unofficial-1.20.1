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

/**
 * Magic autoclave.
 *
 * <p>Upstream ran the GT autoclave map plus the Pollution crystal-cultivation
 * map; the latter is not part of the port, so only {@code AUTOCLAVE_RECIPES}
 * remains (documented deviation).</p>
 */
public class MagicAutoclaveMachine extends MagicMultiblockController {

    public MagicAutoclaveMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    public Material getMaterial() {
        return PollutionMaterials.InfusedFly;
    }

    public static BlockPattern createPattern(MultiblockMachineDefinition definition) {
        return FactoryBlockPattern.start()
                .aisle("YYY", "YYY", "YYY")
                .aisle("XXX", "XCX", "XXX")
                .aisle("XXX", "XCX", "XXX")
                .aisle("XXX", "XCX", "XXX")
                .aisle("XXX", "XSX", "XXX")
                .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                .where('X', MagicStructureElements.magicCasing(PollutionMagicBlocks.SPELL_PRISM_AIR.get(),
                        GTRecipeTypes.AUTOCLAVE_RECIPES))
                .where('C', Predicates.blocks(PollutionMagicBlocks.BEAM_CORE_2.get()))
                .where('Y', Predicates.blocks(PollutionMagicBlocks.SPELL_PRISM_WATER.get()))
                .build();
    }
}
