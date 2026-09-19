package meowmel.pollution.common.machine.multiblock.magic;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import meowmel.pollution.api.recipes.PORecipeMaps;
import meowmel.pollution.api.unification.PollutionMaterials;
import meowmel.pollution.common.block.PollutionMagicBlocks;
import meowmel.pollution.common.machine.multiblock.MagicMultiblockController;
import meowmel.pollution.common.machine.multiblock.MagicStructureElements;

/**
 * Magic assembler.
 *
 * <p>The upstream structure used GTQT frame materials
 * ({@code HyperdimensionalSilver}, {@code KQGold}) which are not part of the
 * port; they are substituted with GTCEu frames of comparable progression
 * (NaquadahAlloy for the advanced frame, TungstenSteel for the working frame).
 * See {@code MagicStructureElements#frame}.</p>
 */
public class MagicAssemblerMachine extends MagicMultiblockController {

    public MagicAssemblerMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    public Material getMaterial() {
        return PollutionMaterials.InfusedCraft;
    }

    public static BlockPattern createPattern(MultiblockMachineDefinition definition) {
        return FactoryBlockPattern.start()
                .aisle(" ABABA ", "  CDC  ", "  CDC  ", "  EDE  ", "  EDE  ", "  AAA  ")
                .aisle("AAAAAAA", " D   D ", " D   D ", " D   D ", " DBBBD ", " AAAAA ")
                .aisle("AAABAAA", "C F F C", "C F F C", "E F F E", "EDB BDE", "AAAAAAA")
                .aisle("AAABAAA", "D     D", "D     D", "D     D", "DDB BDD", "AAAAAAA")
                .aisle("AAABAAA", "D F F D", "D F F D", "D F F D", "DBB BBD", "AAAAAAA")
                .aisle("SABBBAA", "D     D", "D     D", "D     D", "DB   BD", "AAAAAAA")
                .aisle("AAABAAA", "D F F D", "D F F D", "D F F D", "DBB BBD", "AAAAAAA")
                .aisle("AAABAAA", "D     D", "D     D", "D     D", "DDB BDD", "AAAAAAA")
                .aisle("AAABAAA", "C F F C", "C F F C", "E F F E", "EDB BDE", "AAAAAAA")
                .aisle("AAAAAAA", " D   D ", " D   D ", " D   D ", " DBBBD ", " AAAAA ")
                .aisle(" ABABA ", "  CDC  ", "  CDC  ", "  EDE  ", "  EDE  ", "  AAA  ")
                .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                .where('A', MagicStructureElements.magicCasing(PollutionMagicBlocks.MANA_BASIC.get(),
                        GTRecipeTypes.ASSEMBLER_RECIPES, PORecipeMaps.MAGIC_ASSEMBLER_RECIPES))
                .where('B', Predicates.blocks(PollutionMagicBlocks.TERRA_WATERTIGHT_CASING.get()))
                .where('C', MagicStructureElements.frame(GTMaterials.NaquadahAlloy))
                .where('D', Predicates.blocks(PollutionMagicBlocks.LAMINATED_GLASS.get()))
                .where('E', MagicStructureElements.frame(GTMaterials.TungstenSteel))
                // 上游: tieredCasing('F', beamCores) -> 本移植版: 单档光束核心
                .where('F', Predicates.blocks(PollutionMagicBlocks.BEAM_CORE_1.get()))
                .where(' ', Predicates.any())
                .build();
    }
}
