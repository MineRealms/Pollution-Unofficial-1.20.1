package meowmel.pollution.common.machine.multiblock.magic;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import meowmel.pollution.api.recipes.PORecipeMaps;
import meowmel.pollution.api.unification.PollutionMaterials;
import meowmel.pollution.common.block.PollutionMagicBlocks;
import meowmel.pollution.common.machine.multiblock.MagicMultiblockController;
import meowmel.pollution.common.machine.multiblock.MagicStructureElements;

/**
 * Magic chemical reactor: the large tower structure running GregTech chemical
 * recipes plus the Pollution magic chemical map. The PTFE pipe row of the
 * upstream 1.12 boiler casing maps to GregTech's modern
 * {@code CASING_POLYTETRAFLUOROETHYLENE_PIPE} block.
 */
public class MagicChemicalReactorMachine extends MagicMultiblockController {

    public MagicChemicalReactorMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    public Material getMaterial() {
        return PollutionMaterials.InfusedAlchemy;
    }

    public static BlockPattern createPattern(MultiblockMachineDefinition definition) {
        return FactoryBlockPattern.start()
                .aisle("   AAAAA   ", "    B      ", "     B     ", "           ", "           ", "           ",
                        "           ", "           ", "           ", "           ", "           ", "           ",
                        "   AAAAA   ")
                .aisle("  AACCCAA  ", "           ", "           ", "      B    ", "           ", "           ",
                        "    B      ", "     B     ", "           ", "           ", "           ", "           ",
                        "  AACCCAA  ")
                .aisle(" AADCACDAA ", "    D D    ", "           ", "           ", "       B   ", "   B       ",
                        "           ", "           ", "      B    ", "           ", "           ", "    D D    ",
                        " AADCACDAA ")
                .aisle("AADCCACCDAA", "           ", "           ", "           ", "  B        ", "        B  ",
                        "           ", "           ", "           ", "   B   B   ", "    B      ", "     B     ",
                        "AADCCACCDAA")
                .aisle("ACCCCACCCCA", "  D  E  D B", "     E     ", " B   E     ", "     E     ", "     E     ",
                        "     E   B ", "     E     ", "  B  E     ", "     E     ", "     E B   ", "  D  E  D  ",
                        "ACCCCACCCCA")
                .aisle("ACAAAAAAACA", "    EEE    ", "B   EEE   B", "    EEE    ", "    EEE    ", "    EEE    ",
                        "    EEE    ", " B  EEE  B ", "    EEE    ", "    EEE    ", "    EEE    ", "   BEEEB   ",
                        "ACAAAAAAACA")
                .aisle("ACCCCACCCCA", "B D  E  D  ", "     E     ", "     E   B ", "     E     ", "     E     ",
                        " B   E     ", "     E     ", "     E  B  ", "     E     ", "   B E     ", "  D  E  D  ",
                        "ACCCCACCCCA")
                .aisle("AADCCACCDAA", "           ", "           ", "           ", "        B  ", "  B        ",
                        "           ", "           ", "           ", "   B   B   ", "      B    ", "     B     ",
                        "AADCCACCDAA")
                .aisle(" AADCACDAA ", "    D D    ", "           ", "           ", "   B       ", "       B   ",
                        "           ", "           ", "    B      ", "           ", "           ", "    D D    ",
                        " AADCACDAA ")
                .aisle("  AACCCAA  ", "           ", "           ", "    B      ", "           ", "           ",
                        "      B    ", "     B     ", "           ", "           ", "           ", "           ",
                        "  AACCCAA  ")
                .aisle("   AASAA   ", "      B    ", "     B     ", "           ", "           ", "           ",
                        "           ", "           ", "           ", "           ", "           ", "           ",
                        "   AAAAA   ")
                .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                .where('A', MagicStructureElements.magicCasing(PollutionMagicBlocks.SPELL_PRISM_WATER.get(),
                        GTRecipeTypes.CHEMICAL_RECIPES, PORecipeMaps.MAGIC_CHEMICAL_REACTOR_RECIPES))
                .where('B', Predicates.blocks(PollutionMagicBlocks.BEAM_CORE_4.get()))
                .where('C', Predicates.blocks(PollutionMagicBlocks.CAMINATED_GLASS.get()))
                .where('D', Predicates.blocks(PollutionMagicBlocks.BEAM_CORE_2.get()))
                .where('E', Predicates.blocks(GTBlocks.CASING_POLYTETRAFLUOROETHYLENE_PIPE.get()))
                .where(' ', Predicates.any())
                .build();
    }
}
