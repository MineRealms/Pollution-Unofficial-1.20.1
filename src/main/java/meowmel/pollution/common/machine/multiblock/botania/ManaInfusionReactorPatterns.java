package meowmel.pollution.common.machine.multiblock.botania;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import dev.tc4port.thaumcraft.registry.TCBlocks;
import meowmel.pollution.common.block.PollutionMagicBlocks;
import vazkii.botania.common.block.BotaniaBlocks;

/**
 * Structure of the mana infusion reactor, converted 1:1 from the 1.12.2 aisles
 * of {@code MetaTileEntityManaInfusionReactor}.
 *
 * <p>Deviation: the Thaumcraft 1.12 blocks ({@code stoneArcaneBrick},
 * {@code infusionMatrix}) map to their Thaumcraft 4R 1.20.1 equivalents; the
 * primary casing uses {@link BotaniaStructureElements#manaCasing} exactly like
 * upstream's {@code configureManaRecipeCasing}.</p>
 */
final class ManaInfusionReactorPatterns {

    static BlockPattern create(MultiblockMachineDefinition definition) {
        return FactoryBlockPattern.start()
                .aisle(" ABBBBBBBA ", "    C C    ", "    D D    ", "           ", "           ", "           ", "           ", "           ", "           ")
                .aisle("ACEEEEEEECA", " C       C ", " C       C ", " C       C ", " C       C ", " C       C ", " D       D ", " F       F ", " G       G ")
                .aisle("BEHHHHHHHEB", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ")
                .aisle("BEHAAAAAHEB", "     F     ", "     G     ", "           ", "           ", "           ", "           ", "           ", "           ")
                .aisle("BEHAAAAAHEB", "C         C", "D         D", "           ", "           ", "           ", "           ", "           ", "           ")
                .aisle("BEHAAAAAHEB", "   F A F   ", "   G A G   ", "     A     ", "     F     ", "     G     ", "           ", "           ", "           ")
                .aisle("BEHAAAAAHEB", "C         C", "D         D", "           ", "           ", "           ", "           ", "           ", "           ")
                .aisle("BEHAAAAAHEB", "     F     ", "     G     ", "           ", "           ", "           ", "           ", "           ", "           ")
                .aisle("BEHHHHHHHEB", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ")
                .aisle("ACEEEEEEECA", " C       C ", " C       C ", " C       C ", " C       C ", " C       C ", " D       D ", " F       F ", " G       G ")
                .aisle(" ABBBSBBBA ", "    C C    ", "    D D    ", "           ", "           ", "           ", "           ", "           ", "           ")
                .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                .where('A', Predicates.blocks(TCBlocks.ARCANE_STONE_BRICKS.get()))
                .where('B', BotaniaStructureElements.manaCasing(BotaniaBlocks.livingrockBrick,
                        BotaniaRecipeMaps.MANA_INFUSION_RECIPES))
                .where('C', Predicates.blocks(BotaniaBlocks.livingrockBrickChiseled))
                .where('D', Predicates.blocks(TCBlocks.RUNIC_MATRIX.get()))
                .where('E', Predicates.blocks(PollutionMagicBlocks.LAMINATED_GLASS.get()))
                .where('F', Predicates.blocks(PollutionMagicBlocks.BEAM_CORE_4.get()))
                .where('G', Predicates.blocks(BotaniaBlocks.manaPylon))
                .where('H', Predicates.blocks(BotaniaBlocks.livingwoodPlanks))
                .where(' ', Predicates.any())
                .build();
    }

    private ManaInfusionReactorPatterns() {}
}
