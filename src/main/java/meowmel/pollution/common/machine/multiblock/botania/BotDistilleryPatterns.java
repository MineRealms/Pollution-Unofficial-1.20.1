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
 * Generated from the 1.12.2 aisles of {@code MetaTileEntityBotDistillery.java} by the one-off
 * batch-1 generator. Predicate characters are mapped to the modern blocks and
 * to {@link BotaniaStructureElements}; see the machine class for deviations.
 */
final class BotDistilleryPatterns {

    static BlockPattern create(MultiblockMachineDefinition definition) {
        return FactoryBlockPattern.start()
                .aisle("___XXX___", "___XXX___", "__XXXXX__", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "__XXXXX__", "___XXX___", "___XXX___")
                .aisle("_________", "___XSX___", "__XXXXX__", "_XXXXXXX_", "_XXXZXXX_", "_XXXXXXX_", "__XXXXX__", "___XXX___", "_________")
                .aisle("_________", "____X____", "___XXX___", "__XXXXX__", "_XXXZXXX_", "__XXXXX__", "___XXX___", "____X____", "_________")
                .aisle("_________", "_________", "___M_M___", "__Y___Y__", "____Z____", "__Y___Y__", "___Y_Y___", "_________", "_________")
                .setRepeatable(1, 12)
                .aisle("_________", "_________", "___Y_Y___", "__Y___Y__", "____Z____", "__Y___Y__", "___Y_Y___", "_________", "_________")
                .aisle("_________", "_________", "_________", "_________", "____Z____", "_________", "_________", "_________", "_________")
                .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                .where('X', Predicates.blocks(PollutionMagicBlocks.TERRA_WATERTIGHT_CASING.get())
                        .setMinGlobalLimited(28)
                        .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(2))
                        .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setExactLimit(1))
                        .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setExactLimit(1))
                        .or(Predicates.abilities(POMultiblockAbility.MANA_INPUT_HATCH).setExactLimit(1))
                        .or(Predicates.abilities(POMultiblockAbility.MANA_INPUT_POOL).setMaxGlobalLimited(1))
                        .or(Predicates.abilities(PartAbility.PARALLEL_HATCH).setExactLimit(1))
                        .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                        .or(Predicates.abilities(PartAbility.MUFFLER).setExactLimit(1)))
                .where('M', Predicates.blocks(PollutionMagicBlocks.TERRA_WATERTIGHT_CASING.get())
                        .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS)
                                .setMinLayerLimited(1).setMaxLayerLimited(1)))
                .where('Y', Predicates.blocks(PollutionMagicBlocks.CAMINATED_GLASS.get()))
                .where('Z', Predicates.blocks(GTBlocks.CASING_TEMPERED_GLASS.get()))
                .where('_', Predicates.any())
                .build();
    }

    private BotDistilleryPatterns() {}
}
