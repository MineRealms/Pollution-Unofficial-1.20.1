package meowmel.pollution.common.machine.multiblock.botania;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import meowmel.pollution.api.metatileentity.POMultiblockAbility;
import meowmel.pollution.common.block.PollutionMagicBlocks;
import meowmel.pollution.common.machine.multiblock.MagicStructureElements;
import net.minecraft.world.level.block.Blocks;
import vazkii.botania.common.block.BotaniaBlocks;

/**
 * Structure of the Endoflame magical power array, converted 1:1 from the
 * 1.12.2 aisles of {@code MetaTileEntityEndoflameArray}.
 *
 * <p>Deviations: the KQGold frame maps to a GTCEu TungstenSteel frame (same
 * substitution as the other Botania machines), the generic Botania floating
 * flower block maps to {@code whiteFloatingFlower} and the air characters use
 * {@link Predicates#air()}.</p>
 */
final class EndoflameArrayPatterns {

    static BlockPattern create(MultiblockMachineDefinition definition) {
        return FactoryBlockPattern.start()
                .aisle("AB   BA", " ABBBA ", "       ", "       ", "       ", "       ")
                .aisle("BABBBAB", "AAAAAAA", " BCCCB ", " D C D ", "       ", "       ")
                .aisle(" BAAAB ", "BAAEAAB", " CXFXC ", "  GXG  ", "   C   ", "       ")
                .aisle(" BAAAB ", "BAEAEAB", " CFXFC ", " CXXXC ", "  CXC  ", "   G   ")
                .aisle(" BAAAB ", "BAAEAAB", " CXFXC ", "  GXG  ", "   C   ", "       ")
                .aisle("BABBBAB", "AAAAAAA", " BCCCB ", " D C D ", "       ", "       ")
                .aisle("AB   BA", " ABSBA ", "       ", "       ", "       ", "       ")
                .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                .where('A', Predicates.blocks(PollutionMagicBlocks.TERRA_4_CASING.get())
                        .or(Predicates.abilities(PartAbility.IMPORT_ITEMS)
                                .setMinGlobalLimited(1).setMaxGlobalLimited(27))
                        .or(Predicates.abilities(POMultiblockAbility.MANA_OUTPUT_POOL).setMaxGlobalLimited(1))
                        .or(Predicates.abilities(PartAbility.MAINTENANCE).setMaxGlobalLimited(1)))
                .where('B', MagicStructureElements.frame(GTMaterials.TungstenSteel))
                .where('C', Predicates.blocks(PollutionMagicBlocks.AAMINATED_GLASS.get()))
                .where('D', Predicates.blocks(BotaniaBlocks.manaPylon))
                .where('E', Predicates.blocks(Blocks.DIRT))
                .where('F', Predicates.blocks(BotaniaBlocks.whiteFloatingFlower))
                .where('G', Predicates.blocks(PollutionMagicBlocks.BEAM_CORE_0.get()))
                .where('X', Predicates.air())
                .where(' ', Predicates.any())
                .build();
    }

    private EndoflameArrayPatterns() {}
}
