package meowmel.pollution.common.machine.multiblock.botania;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import meowmel.pollution.api.metatileentity.POMultiblockAbility;
import meowmel.pollution.common.block.PollutionMagicBlocks;
import meowmel.pollution.common.machine.multiblock.MagicStructureElements;
import net.minecraft.world.level.block.Blocks;
import vazkii.botania.common.block.BotaniaBlocks;

/**
 * Generated from the 1.12.2 aisles of {@code MetaTileEntityManaPlate.java} by the one-off
 * batch-1 generator. Predicate characters are mapped to the modern blocks and
 * to {@link BotaniaStructureElements}; see the machine class for deviations.
 */
final class ManaPlatePatterns {

    static BlockPattern create(MultiblockMachineDefinition definition) {
        return FactoryBlockPattern.start()
                .aisle("CCCCCCCCCCC")
                .aisle("CCCCCCCCCCC")
                .aisle("CCCCCCCCCCC")
                .aisle("CCCCCCCCCCC")
                .aisle("CCCCCCCCCCC")
                .aisle("CCCCCCCCCCC")
                .aisle("CCCCCCCCCCC")
                .aisle("CCCCCCCCCCC")
                .aisle("CCCCCCCCCCC")
                .aisle("CCCCCCCCCCC")
                .aisle("CCCCCSCCCCC")
                .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                .where('C', Predicates.blocks(PollutionMagicBlocks.MANA_BASIC.get())
                        .or(Predicates.abilities(POMultiblockAbility.MANA_INPUT_POOL).setExactLimit(1)))
                .build();
    }

    private ManaPlatePatterns() {}
}
