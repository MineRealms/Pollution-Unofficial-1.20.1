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
 * Generated from the 1.12.2 aisles of {@code MetaTileEntityBotVacuumFreezer.java} by the one-off
 * batch-1 generator. Predicate characters are mapped to the modern blocks and
 * to {@link BotaniaStructureElements}; see the machine class for deviations.
 */
final class BotVacuumFreezerPatterns {

    static BlockPattern create(MultiblockMachineDefinition definition) {
        return FactoryBlockPattern.start()
                .aisle("      AXXXA      ", "       XXX       ", "        X        ", "                 ", "                 ", "                 ", "                 ", "                 ", "                 ")
                .aisle("     BABBBAB     ", "     C     C     ", "     D     D     ", "                 ", "                 ", "                 ", "                 ", "                 ", "                 ")
                .aisle("    BBBAAABBB    ", "    CCC   CCC    ", "    DED   DED    ", "     D     D     ", "                 ", "     F     F     ", "                 ", "                 ", "                 ")
                .aisle("     B AAA B     ", "     C     C     ", "     D     D     ", "                 ", "                 ", "                 ", "                 ", "                 ", "                 ")
                .aisle("       GAG       ", "                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "                 ")
                .aisle("      GBBBG      ", "       DHD       ", "       DHD       ", "       DHD       ", "       DHD       ", "       GGG       ", "       ADA       ", "       GGG       ", "                 ")
                .aisle("     GBBCBBG     ", "      DE ED      ", "      DE ED      ", "      DE ED      ", "      DEBED      ", "      GGBGG      ", "      A   A      ", "      GGCGG      ", "       GGG       ")
                .aisle(" BAAGBBCCCBBGAAB ", " C   DE I ED   C ", " D   DE J ED   D ", "     DE   ED     ", "     DE   ED     ", "     GGBBBGG     ", "     A K K A     ", "     GGCCCGG     ", "      GGHGG      ")
                .aisle("BBBAABCCCCCBAABBB", "CCC  H IEI H  CCC", "DED  H JEJ H  DED", " D   H  E  H   D ", "     HB E BH     ", " F   GBBBBBG   F ", "     D  K  D     ", "     GCCCCCG     ", "      GHHHG      ")
                .aisle(" BAAGBBCCCBBGAAB ", " C   DE I ED   C ", " D   DE J ED   D ", "     DE   ED     ", "     DE   ED     ", "     GGBBBGG     ", "     A K K A     ", "     GGCCCGG     ", "      GGHGG      ")
                .aisle("     GBBCBBG     ", "      DE ED      ", "      DE ED      ", "      DE ED      ", "      DEBED      ", "      GGBGG      ", "      A   A      ", "      GGCGG      ", "       GGG       ")
                .aisle("      GBBBG      ", "       DHD       ", "       DHD       ", "       DHD       ", "       DHD       ", "       GGG       ", "       ADA       ", "       GGG       ", "                 ")
                .aisle("       GAG       ", "                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "                 ")
                .aisle("     B AAA B     ", "     C     C     ", "     D     D     ", "                 ", "                 ", "                 ", "                 ", "                 ", "                 ")
                .aisle("    BBBAAABBB    ", "    CCC   CCC    ", "    DED   DED    ", "     D     D     ", "                 ", "     F     F     ", "                 ", "                 ", "                 ")
                .aisle("     BABBBAB     ", "     C     C     ", "     D     D     ", "                 ", "                 ", "                 ", "                 ", "                 ", "                 ")
                .aisle("      AXXXA      ", "       XSX       ", "        X        ", "                 ", "                 ", "                 ", "                 ", "                 ", "                 ")
                .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                .where('A', MagicStructureElements.frame(GTMaterials.NaquadahAlloy))
                .where('X', Predicates.blocks(PollutionMagicBlocks.MANA_4.get())
                        // 上游 abilityGroup(MANA_INPUT_HATCH, 1, 2) 覆盖魔力能源仓 + 能源仓
                        .or(Predicates.abilities(POMultiblockAbility.MANA_INPUT_HATCH, PartAbility.INPUT_ENERGY)
                                .setMinGlobalLimited(1).setMaxGlobalLimited(2))
                        .or(Predicates.abilities(POMultiblockAbility.MANA_INPUT_POOL).setMaxGlobalLimited(1))
                        .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                        .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(13))
                        .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(13))
                        .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setMaxGlobalLimited(13))
                        .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setMaxGlobalLimited(13)))
                .where('B', Predicates.blocks(PollutionMagicBlocks.MANA_4.get()))
                .where('C', Predicates.blocks(GTBlocks.CASING_ALUMINIUM_FROSTPROOF.get()))
                .where('D', Predicates.blocks(PollutionMagicBlocks.CAMINATED_GLASS.get()))
                .where('E', Predicates.blocks(PollutionMagicBlocks.BEAM_CORE_2.get()))
                .where('F', Predicates.blocks(BotaniaBlocks.manaPylon))
                .where('G', Predicates.blocks(PollutionMagicBlocks.TERRA_1_CASING.get()))
                .where('H', Predicates.heatingCoils())
                .where('I', Predicates.blocks(Blocks.DIRT))
                .where('J', Predicates.blocks(BotaniaBlocks.whiteFloatingFlower))
                .where('K', Predicates.blocks(PollutionMagicBlocks.BEAM_CORE_4.get()))
                .where(' ', Predicates.any())
                .build();
    }

    private BotVacuumFreezerPatterns() {}
}
