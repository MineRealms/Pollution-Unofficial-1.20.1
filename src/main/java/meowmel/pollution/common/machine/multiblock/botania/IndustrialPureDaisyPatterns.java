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
 * Generated from the 1.12.2 aisles of {@code MetaTileEntityIndustrialPureDaisy.java} by the one-off
 * batch-1 generator. Predicate characters are mapped to the modern blocks and
 * to {@link BotaniaStructureElements}; see the machine class for deviations.
 */
final class IndustrialPureDaisyPatterns {

    static BlockPattern create(MultiblockMachineDefinition definition) {
        return FactoryBlockPattern.start()
                .aisle("ABA   ABA", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "         ")
                .aisle("BCCCCCCCB", " CC   CC ", " C     C ", " C     C ", " DAAAAAD ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "         ")
                .aisle("ACABBBACA", " CAEEEAC ", "  AEEEA  ", "  ABBBA  ", " AACCCAA ", "  CCDCC  ", "   CCC   ", "    C    ", "         ", "         ", "         ", "         ", "         ")
                .aisle(" CBACABC ", "  E F E  ", "  EF FE  ", "  B   B  ", " AC   CA ", "  C   C  ", "  C   C  ", "   C C   ", "    C    ", "         ", "         ", "         ", "         ")
                .aisle(" CBCGCBC ", "  EF FE  ", "  E   E  ", "  B   B  ", " AC   CA ", "  D   D  ", "  C   C  ", "  C   C  ", "   CGC   ", "    C    ", "    C    ", "    C    ", "    G    ")
                .aisle(" CBACABC ", "  E F E  ", "  EF FE  ", "  B   B  ", " AC   CA ", "  C   C  ", "  C   C  ", "   C C   ", "    C    ", "         ", "         ", "         ", "         ")
                .aisle("ACABBBACA", " CAEEEAC ", "  AEEEA  ", "  ABBBA  ", " AACCCAA ", "  CCDCC  ", "   CCC   ", "    C    ", "         ", "         ", "         ", "         ", "         ")
                .aisle("BCCCSCCCB", " CC   CC ", " C     C ", " C     C ", " DAAAAAD ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "         ")
                .aisle("ABA   ABA", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "         ")
                .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                .where('C', BotaniaStructureElements.manaCasing(BotaniaBlocks.livingrockBrick,
                        BotaniaRecipeMaps.PURE_DAISY_RECIPES))
                .where('A', Predicates.blocks(BotaniaBlocks.livingrockBrickChiseled))
                .where('B', MagicStructureElements.frame(GTMaterials.TungstenSteel))
                .where('D', Predicates.blocks(BotaniaBlocks.manaPylon))
                .where('E', Predicates.blocks(PollutionMagicBlocks.LAMINATED_GLASS.get()))
                .where('F', Predicates.blocks(BotaniaBlocks.whiteFloatingFlower))
                .where('G', Predicates.blocks(PollutionMagicBlocks.BEAM_CORE_4.get()))
                .where(' ', Predicates.any())
                .build();
    }

    private IndustrialPureDaisyPatterns() {}
}
