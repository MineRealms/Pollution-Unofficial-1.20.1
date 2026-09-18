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
 * Generated from the 1.12.2 aisles of {@code MetaTileEntityBotGasCollector.java} by the one-off
 * batch-1 generator. Predicate characters are mapped to the modern blocks and
 * to {@link BotaniaStructureElements}; see the machine class for deviations.
 */
final class BotGasCollectorPatterns {

    static BlockPattern create(MultiblockMachineDefinition definition) {
        return FactoryBlockPattern.start()
                .aisle("E E", "E E", "DDD", "AAA", "AAA", "AAA", "AAA", "AAA", "AAA")
                .aisle("   ", "   ", "DDD", "AAA", "ABA", "ABA", "ABA", "AAA", "AAA")
                .aisle("E E", "E E", "DDD", "ASA", "ACA", "ACA", "ACA", "AAA", "AAA")
                .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                .where('A', Predicates.blocks(PollutionMagicBlocks.TERRA_5_CASING.get())
                        .or(Predicates.abilities(POMultiblockAbility.MANA_INPUT_POOL).setExactLimit(1))
                        .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                        .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS)
                                .setMinGlobalLimited(2).setMaxGlobalLimited(32))
                        .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS)
                                .setMinGlobalLimited(1).setMaxGlobalLimited(32)))
                .where('B', BotaniaStructureElements.beamCores())
                .where('C', Predicates.blocks(PollutionMagicBlocks.BAMINATED_GLASS.get()))
                .where('D', Predicates.blocks(PollutionMagicBlocks.MANA_2.get()))
                .where('E', MagicStructureElements.frame(GTMaterials.TungstenSteel))
                .where(' ', Predicates.any())
                .build();
    }

    private BotGasCollectorPatterns() {}
}
