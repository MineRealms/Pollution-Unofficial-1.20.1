package meowmel.pollution.common.machine.multiblock.botania;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import meowmel.pollution.common.machine.multiblock.LayeredMagicTowerMachine;

/**
 * Terra (Botania) distillation tower.
 *
 * <p>Upstream origin: {@code MetaTileEntityBotDistillery} (1.12.2). The shell
 * (Terra watertight casing, caminated glass, tempered glass) and the
 * repeatable 1..12 body with one export fluid hatch per layer are ported 1:1;
 * the machine runs GregTech's distillation map and accepts mana energy/pool
 * hatches.</p>
 *
 * <p>Fluid fractions use the shared local-axis layer routing, including rotated structures.</p>
 */
public class BotDistilleryMachine extends LayeredMagicTowerMachine {

    public BotDistilleryMachine(IMachineBlockEntity holder) {
        super(holder, GTRecipeTypes.DISTILLATION_RECIPES);
    }

    public static BlockPattern createPattern(MultiblockMachineDefinition definition) {
        return BotDistilleryPatterns.create(definition);
    }
}
