package meowmel.pollution.common.machine.multiblock.magic;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import meowmel.pollution.api.metatileentity.POMultiblockAbility;
import meowmel.pollution.common.block.PollutionMagicBlocks;
import net.minecraft.world.level.block.Block;

/**
 * EV magic turbine with the source casing pattern and GT rotor mechanics.
 * <p>Fuel parallel, acceleration, efficiency and rotor wear come from LargeTurbineMachine;
 * the holder tier must meet the controller tier.</p>
 */
public class MagicLargeTurbineMachine extends AbstractMagicTurbineMachine {

    public MagicLargeTurbineMachine(IMachineBlockEntity holder) {
        super(holder, com.gregtechceu.gtceu.api.GTValues.EV, 1, 1);
    }

    public static BlockPattern createPattern(MultiblockMachineDefinition definition) {
        return createPattern(definition, PollutionMagicBlocks.SPELL_PRISM_HOT.get());
    }

    /**
     * Builds the shared large-turbine pattern with a caller-selected casing.
     * The upstream mana variant uses the MANA_3 plate in every casing slot,
     * while the aspect-fuel variant uses SPELL_PRISM; keeping that difference
     * here avoids duplicating the rotor/ability constraints.
     */
    protected static BlockPattern createPattern(MultiblockMachineDefinition definition, Block casing) {
        return FactoryBlockPattern.start()
                .aisle("CCCC", "CHHC", "CCCC")
                .aisle("CHHC", "RGGR", "CHHC")
                .aisle("CCCC", "CSHC", "CCCC")
                .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                .where('C', Predicates.blocks(casing))
                .where('H', Predicates.blocks(casing)
                        .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setMaxGlobalLimited(4))
                        .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setMaxGlobalLimited(4))
                        .or(Predicates.abilities(PartAbility.OUTPUT_ENERGY).setMaxGlobalLimited(2))
                        .or(Predicates.abilities(PartAbility.MUFFLER).setMaxGlobalLimited(1))
                        .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                        .or(Predicates.abilities(POMultiblockAbility.TAROT_HATCH).setMaxGlobalLimited(1)))
                .where('R', rotorsAtLeast(definition.getTier()).setExactLimit(1)
                        .or(Predicates.abilities(POMultiblockAbility.MANA_OUTPUT_HATCH).setExactLimit(1)))
                .where('G', Predicates.blocks(PollutionMagicBlocks.STAINLESS_STEEL_GEARBOX.get()))
                .build();
    }
}
