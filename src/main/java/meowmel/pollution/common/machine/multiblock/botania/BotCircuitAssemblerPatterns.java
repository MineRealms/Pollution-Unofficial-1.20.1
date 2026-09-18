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
 * Generated from the 1.12.2 aisles of {@code MetaTileEntityBotCircuitAssembler.java} by the one-off
 * batch-1 generator. Predicate characters are mapped to the modern blocks and
 * to {@link BotaniaStructureElements}; see the machine class for deviations.
 */
final class BotCircuitAssemblerPatterns {

    static BlockPattern create(MultiblockMachineDefinition definition) {
        return FactoryBlockPattern.start()
                .aisle("A           A", "A           A", "A           A", "A           A", "A           A", "B           B")
                .aisle(" ACCCCCCCCCA ", " ADDDDDDDDDA ", " AEEEEEEEEEA ", " CCCCCCCCCCC ", "             ", "             ")
                .aisle(" CCCCCCCCCCC ", " DFXFXFXFXFD ", " EXXXXXXXXXE ", " CEEEEGEEEEC ", "             ", "             ")
                .aisle(" CCGGGGGGGCC ", " DFXFXFXFXFD ", " EXXXXXXXXXE ", " CEGGGGGGGEC ", "             ", "             ")
                .aisle(" CCGGGGGGGCC ", " DFBFBFBFBFD ", " EXXXXXXXXXE ", " CGGGGGGGGGC ", "             ", "             ")
                .aisle(" CCGGGGGGGCC ", " DFXFXFXFXFD ", " EXXXXXXXXXE ", " CEGGGGGGGEC ", "             ", "             ")
                .aisle(" CCCCCCCCCCC ", " DFXFXFXFXFD ", " EXXXXXXXXXE ", " CEEEEGEEEEC ", "             ", "             ")
                .aisle(" ACCCCCCCCCA ", " ADDDDSDDDDA ", " AEEEEEEEEEA ", " CCCCCCCCCCC ", "             ", "             ")
                .aisle("A           A", "A           A", "A           A", "A           A", "A           A", "B           B")
                .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                .where('A', MagicStructureElements.frame(GTMaterials.TungstenSteel))
                .where('B', Predicates.blocks(PollutionMagicBlocks.BEAM_CORE_4.get()))
                .where('C', Predicates.blocks(PollutionMagicBlocks.MANA_5.get())
                        .or(Predicates.abilities(POMultiblockAbility.MANA_INPUT_HATCH).setMaxGlobalLimited(2))
                        .or(Predicates.abilities(POMultiblockAbility.MANA_INPUT_POOL).setMaxGlobalLimited(1))
                        .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(44))
                        .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                        .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(44))
                        .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(44))
                        .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setMaxGlobalLimited(44)))
                .where('D', Predicates.blocks(GTBlocks.FILTER_CASING.get()))
                .where('E', Predicates.blocks(GTBlocks.FUSION_GLASS.get()))
                .where('F', Predicates.blocks(PollutionMagicBlocks.DAMINATED_GLASS.get()))
                .where('G', BotaniaStructureElements.tieredFrames())
                .where(' ', Predicates.any())
                .where('X', Predicates.air())
                .build();
    }

    private BotCircuitAssemblerPatterns() {}
}
