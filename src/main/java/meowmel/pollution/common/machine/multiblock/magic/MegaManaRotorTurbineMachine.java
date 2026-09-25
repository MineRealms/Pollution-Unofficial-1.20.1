package meowmel.pollution.common.machine.multiblock.magic;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import meowmel.pollution.common.block.PollutionMagicBlocks;

/** The registered upstream ZPM twelve-rotor turbine; distinct from its unregistered catalytic prototype. */
public final class MegaManaRotorTurbineMachine extends MagicMegaTurbineMachine {
    public MegaManaRotorTurbineMachine(IMachineBlockEntity holder) {
        super(holder, GTValues.ZPM);
    }

    public static BlockPattern createPattern(MultiblockMachineDefinition definition) {
        return createPattern(definition, PollutionMagicBlocks.MANA_3.get(),
                PollutionMagicBlocks.TUNGSTENSTEEL_PIPE.get());
    }
}
