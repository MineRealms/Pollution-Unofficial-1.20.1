package meowmel.pollution.common.machine.multiblock.magic;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import meowmel.pollution.api.metatileentity.POMultiblockAbility;
import meowmel.pollution.common.block.PollutionMagicBlocks;
import meowmel.pollution.common.machine.multiblock.MagicMultiblockController;

/**
 * Magic large turbine: burns the magic turbine fuel map.
 *
 * <p>Upstream reused GregTech's large-turbine base with a custom
 * {@code MagicTurbineType}. The port runs the {@code MAGIC_TURBINE_FUELS} map
 * on a magic shell; the rotor durability mechanics of the GT base are not
 * reimplemented (documented deviation), and the upstream tier-filtered rotor
 * holder accepts any GT rotor holder in the port. Upstream's {@code R} slot
 * takes one rotor holder plus one mana output hatch; the port also keeps its
 * {@code OUTPUT_ENERGY} hatch (EU output deviation). The astral-lens and tarot
 * hatches of the upstream casing have no registered counterpart in the port.</p>
 */
public class MagicLargeTurbineMachine extends MagicMultiblockController {

    public MagicLargeTurbineMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    public static BlockPattern createPattern(MultiblockMachineDefinition definition) {
        return FactoryBlockPattern.start()
                .aisle("CCCC", "CHHC", "CCCC")
                .aisle("CHHC", "RGGR", "CHHC")
                .aisle("CCCC", "CSHC", "CCCC")
                .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                .where('C', Predicates.blocks(PollutionMagicBlocks.SPELL_PRISM.get()))
                .where('H', Predicates.blocks(PollutionMagicBlocks.SPELL_PRISM.get())
                        .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setMaxGlobalLimited(4))
                        .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setMaxGlobalLimited(4))
                        .or(Predicates.abilities(PartAbility.OUTPUT_ENERGY).setMaxGlobalLimited(2))
                        .or(Predicates.abilities(PartAbility.MUFFLER).setMaxGlobalLimited(1))
                        .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                .where('R', Predicates.abilities(PartAbility.ROTOR_HOLDER).setExactLimit(1)
                        .or(Predicates.abilities(POMultiblockAbility.MANA_OUTPUT_HATCH).setExactLimit(1)))
                .where('G', Predicates.blocks(PollutionMagicBlocks.TUNGSTENSTEEL_GEARBOX.get()))
                .build();
    }
}
