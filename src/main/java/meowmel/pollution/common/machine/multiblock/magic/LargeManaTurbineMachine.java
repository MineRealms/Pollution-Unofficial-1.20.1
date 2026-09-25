package meowmel.pollution.common.machine.multiblock.magic;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import meowmel.pollution.api.metatileentity.POMultiblockAbility;
import meowmel.pollution.common.block.PollutionMagicBlocks;

/**
 * LuV large mana turbine from the 1.12 upstream.
 *
 * <p>The controller and rotor gate are the same as the large aspect turbine;
 * only the casing changes to the MANA_3 plate and the registered recipe map is
 * {@code mana_to_eu}.  This keeps the structure's rotor/maintenance/fluid
 * semantics aligned with the upstream {@code MetaTileEntityMagicLargeTurbine}
 * mana registration.</p>
 */
public final class LargeManaTurbineMachine extends AbstractMagicTurbineMachine {

    public LargeManaTurbineMachine(IMachineBlockEntity holder) {
        super(holder, com.gregtechceu.gtceu.api.GTValues.LuV, 1, 1);
    }

    public static BlockPattern createPattern(MultiblockMachineDefinition definition) {
        return FactoryBlockPattern.start()
                .aisle("CCCC", "CHHC", "CCCC")
                .aisle("CHHC", "RGGR", "CHHC")
                .aisle("CCCC", "CSHC", "CCCC")
                .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                .where('C', Predicates.blocks(PollutionMagicBlocks.MANA_3.get()))
                .where('H', Predicates.blocks(PollutionMagicBlocks.MANA_3.get())
                        .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setMaxGlobalLimited(4))
                        .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setMaxGlobalLimited(4))
                        .or(Predicates.abilities(PartAbility.OUTPUT_ENERGY).setMaxGlobalLimited(2))
                        .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                        .or(Predicates.abilities(POMultiblockAbility.TAROT_HATCH).setMaxGlobalLimited(1)))
                .where('R', rotorsAtLeast(definition.getTier()).setExactLimit(1)
                        .or(Predicates.abilities(POMultiblockAbility.MANA_OUTPUT_HATCH).setExactLimit(1)))
                .where('G', Predicates.blocks(PollutionMagicBlocks.TUNGSTENSTEEL_PIPE.get()))
                .build();
    }
}
