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
 * Generated from the 1.12.2 aisles of {@code MetaTileEntityManaPetalApothecary.java} by the one-off
 * batch-1 generator. Predicate characters are mapped to the modern blocks and
 * to {@link BotaniaStructureElements}; see the machine class for deviations.
 */
final class ManaPetalApothecaryPatterns {

    static BlockPattern create(MultiblockMachineDefinition definition) {
        return FactoryBlockPattern.start()
                .aisle("       A       ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ")
                .aisle("      AAA      ", "       A       ", "       B       ", "       C       ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ")
                .aisle("       A       ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ")
                .aisle("       D       ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "   AEEEEEEEA   ", "   AFFFFFFFA   ", "   GADDDDDAG   ")
                .aisle("     AAAAA     ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "    AEEEEEA    ", "   E       E   ", "   F       F   ", "   AAEEEEEAA   ")
                .aisle("    AEEEEEA    ", "     EEEEE     ", "               ", "               ", "               ", "               ", "               ", "     AAAAA     ", "    E     E    ", "   E       E   ", "   F       F   ", "   DE     ED   ")
                .aisle(" A  AEEEEEA  A ", "     EEEEE     ", "      ADA      ", "      ADA      ", "      ADA      ", "      ADA      ", "      ADA      ", "     AEEEA     ", "    E     E    ", "   E       E   ", "   F       F   ", "   DE     ED   ")
                .aisle("AAADAEEEEEADAAA", " A   EEEEE   A ", " B    D D    A ", " C    D D    C ", "      D D      ", "      D D      ", "      D D      ", "     AEEEA     ", "    E     E    ", "   E       E   ", "   F       F   ", "   DE     ED   ")
                .aisle(" A  AEEEEEA  A ", "     EEEEE     ", "      ADA      ", "      ADA      ", "      ADA      ", "      ADA      ", "      ADA      ", "     AEEEA     ", "    E     E    ", "   E       E   ", "   F       F   ", "   DE     ED   ")
                .aisle("    AEEEEEA    ", "     EESEE     ", "               ", "               ", "               ", "               ", "               ", "     AAAAA     ", "    E     E    ", "   E       E   ", "   F       F   ", "   DE     ED   ")
                .aisle("     AAAAA     ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "    AEEEEEA    ", "   E       E   ", "   F       F   ", "   AAEEEEEAA   ")
                .aisle("       D       ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "   AEEEEEEEA   ", "   AFFFFFFFA   ", "   GADDDDDAG   ")
                .aisle("       A       ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ")
                .aisle("      AAA      ", "       A       ", "       B       ", "       C       ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ")
                .aisle("       A       ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ")
                .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                .where('A', Predicates.blocks(BotaniaBlocks.livingrockBrickChiseled))
                .where('B', Predicates.blocks(PollutionMagicBlocks.BEAM_CORE_4.get()))
                .where('C', Predicates.blocks(BotaniaBlocks.whiteFloatingFlower))
                .where('D', MagicStructureElements.frame(GTMaterials.TungstenSteel))
                .where('E', BotaniaStructureElements.manaCasing(BotaniaBlocks.livingrockBrick,
                        BotaniaRecipeMaps.MANA_PETAL_RECIPES))
                .where('F', Predicates.blocks(BotaniaBlocks.livingwoodPlanks))
                .where('G', Predicates.blocks(BotaniaBlocks.manaPylon))
                .where(' ', Predicates.any())
                .build();
    }

    private ManaPetalApothecaryPatterns() {}
}
