package meowmel.pollution.common.machine.multiblock.magic;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import meowmel.pollution.api.unification.PollutionMaterials;
import meowmel.pollution.common.machine.multiblock.MagicMultiblockController;

/**
 * Magic fusion reactor: runs the magic fusion map on a frame/coil shell.
 *
 * <p>Structure deviation: upstream additionally accepted the blood-magic,
 * astral-lens and tarot hatches on the frame (0..1 each); none of those
 * abilities is registered in the port, so they are not accepted.</p>
 */
public class MagicFusionReactorMachine extends MagicMultiblockController {

    public MagicFusionReactorMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    public Material getMaterial() {
        return PollutionMaterials.InfusedMagic;
    }

    public static BlockPattern createPattern(MultiblockMachineDefinition definition) {
        return MagicFusionReactorPatterns.create(definition);
    }
}
