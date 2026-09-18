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
 * Generated from the 1.12.2 aisles of {@code MetaTileEntityManaRuneAltar.java} by the one-off
 * batch-1 generator. Predicate characters are mapped to the modern blocks and
 * to {@link BotaniaStructureElements}; see the machine class for deviations.
 */
final class ManaRuneAltarPatterns {

    static BlockPattern create(MultiblockMachineDefinition definition) {
        return FactoryBlockPattern.start()
                .aisle("    AABAA    ", "    ACACA    ", "     C C     ", "     C C     ", "     C C     ", "     C C     ", "     D D     ", "      D      ", "      B      ")
                .aisle("  AA  A  AA  ", "  AAEEDFFAA  ", "   C     C   ", "   C     C   ", "   C     C   ", "   C     C   ", "   DG   GD   ", "   H     H   ", "             ")
                .aisle(" A    A    A ", " ACEEEDFFFCA ", "             ", "             ", "             ", "             ", "  G       G  ", "             ", "             ")
                .aisle(" A    A    A ", " AECEEDFFCFA ", " C         C ", " C         C ", " C         C ", " C         C ", " D         D ", " H         H ", "             ")
                .aisle("A     A     A", "AEEECEDFCFFFA", "             ", "             ", "             ", "             ", " G         G ", "             ", "             ")
                .aisle("A     A     A", "CEEEECDCFFFFC", "C           C", "C           C", "C           C", "C           C", "D           D", "             ", "             ")
                .aisle("BAAAAAAAAAAAB", "ADDDDDBDDDDDA", "             ", "             ", "             ", "             ", "             ", "D           D", "B           B")
                .aisle("A     A     A", "CIIIICDCJJJJC", "C           C", "C           C", "C           C", "C           C", "D           D", "             ", "             ")
                .aisle("A     A     A", "AIIICIDJCJJJA", "             ", "             ", "             ", "             ", " G         G ", "             ", "             ")
                .aisle(" A    A    A ", " AICIIDJJCJA ", " C         C ", " C         C ", " C         C ", " C         C ", " D         D ", " H         H ", "             ")
                .aisle(" A    A    A ", " ACIIIDJJJCA ", "             ", "             ", "             ", "             ", "  G       G  ", "             ", "             ")
                .aisle("  AA  A  AA  ", "  AAIIDJJAA  ", "   C     C   ", "   C     C   ", "   C     C   ", "   C     C   ", "   DG   GD   ", "   H     H   ", "             ")
                .aisle("    AABAA    ", "    ACSCA    ", "     C C     ", "     C C     ", "     C C     ", "     C C     ", "     D D     ", "      D      ", "      B      ")
                .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                .where('A', BotaniaStructureElements.manaCasing(BotaniaBlocks.livingrockBrick,
                        BotaniaRecipeMaps.MANA_RUNE_ALTAR_RECIPES))
                .where('B', Predicates.blocks(PollutionMagicBlocks.BEAM_CORE_4.get()))
                .where('C', MagicStructureElements.frame(GTMaterials.TungstenSteel))
                .where('D', Predicates.blocks(BotaniaBlocks.livingrockBrickChiseled))
                .where('E', Predicates.blocks(PollutionMagicBlocks.AAMINATED_GLASS.get()))
                .where('F', Predicates.blocks(PollutionMagicBlocks.BAMINATED_GLASS.get()))
                .where('G', Predicates.blocks(BotaniaBlocks.whiteFloatingFlower))
                .where('H', Predicates.blocks(BotaniaBlocks.manaPylon))
                .where('I', Predicates.blocks(PollutionMagicBlocks.CAMINATED_GLASS.get()))
                .where('J', Predicates.blocks(PollutionMagicBlocks.DAMINATED_GLASS.get()))
                .where(' ', Predicates.any())
                .build();
    }

    private ManaRuneAltarPatterns() {}
}
