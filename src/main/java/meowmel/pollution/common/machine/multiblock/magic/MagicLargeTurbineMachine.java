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
 * Magic large turbine: burns the magic turbine fuel map.
 *
 * <p>Upstream reused GregTech's large-turbine base with a custom
 * {@code MagicTurbineType}: the turbine required a rotor in a tiered rotor
 * holder, which was spun up and damaged while the turbine ran. The port runs
 * the {@code MAGIC_TURBINE_FUELS} map on a magic shell and reuses the modern
 * GT rotor holder part through {@link AbstractMagicTurbineMachine}: a real
 * rotor is required before a craft may start, the rotor holder's own
 * {@code onWorking} damages the rotor once per second of operation (deleting
 * the rotor stack when it breaks), and a craft is interrupted when the rotor
 * disappears mid-run. Upstream's generation/parallel scaling by rotor speed,
 * power and efficiency is still not ported. The upstream tier-filtered rotor
 * holder accepts any GT rotor holder in the port. Upstream's {@code R} slot
 * takes one rotor holder plus one mana output hatch; the mana output hatch is
 * the turbine's upstream EU output interface (the turbine pushes generated EU
 * into it and the hatch emits it as Botania mana), so the port keeps the
 * requirement and additionally accepts a standard {@code OUTPUT_ENERGY} hatch
 * (port-side deviation, see {@code docs/HATCH_SEMANTICS.md}). Upstream's
 * tarot hatch is accepted on the casing and read by the amplification engine;
 * the astral-lens hatch has no registered counterpart in the port yet.</p>
 */
public class MagicLargeTurbineMachine extends AbstractMagicTurbineMachine {

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
                        .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                        .or(Predicates.abilities(POMultiblockAbility.TAROT_HATCH).setMaxGlobalLimited(1)))
                .where('R', Predicates.abilities(PartAbility.ROTOR_HOLDER).setExactLimit(1)
                        .or(Predicates.abilities(POMultiblockAbility.MANA_OUTPUT_HATCH).setExactLimit(1)))
                .where('G', Predicates.blocks(PollutionMagicBlocks.TUNGSTENSTEEL_GEARBOX.get()))
                .build();
    }
}
