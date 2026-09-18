package meowmel.pollution.common.machine.multiblock.magic;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import meowmel.pollution.api.unification.PollutionMaterials;
import meowmel.pollution.common.block.PollutionMagicBlocks;
import meowmel.pollution.common.machine.multiblock.MagicMultiblockController;
import meowmel.pollution.common.machine.multiblock.MagicStructureElements;

/**
 * Magic distillery.
 *
 * <p>Upstream allowed magic hatches on the {@code Y} casing and restricted the
 * fluid export hatches to specific layers of the {@code X} rows
 * ({@code Elements.abilitiesPerLayer}). The port keeps the casing/hatch
 * semantics with {@code magicCasing} on {@code Y} and a single global export
 * fluid hatch limit on {@code X}; the per-layer restriction is approximated
 * with a global cap (TODO if a faithful per-layer predicate is needed).</p>
 */
public class MagicDistilleryMachine extends MagicMultiblockController {

    public MagicDistilleryMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    public Material getMaterial() {
        return PollutionMaterials.InfusedWater;
    }

    public static BlockPattern createPattern(MultiblockMachineDefinition definition) {
        return FactoryBlockPattern.start()
                .aisle("#####", "#ZZZ#", "#ZCZ#", "#ZZZ#", "#####")
                .aisle("##X##", "#XAX#", "XAPAX", "#XAX#", "##X##")
                .aisle("#YSY#", "YAAAY", "YATAY", "YAAAY", "#YYY#")
                .aisle("#YYY#", "YYYYY", "YYYYY", "YYYYY", "#YYY#")
                .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                .where('Y', MagicStructureElements.magicCasing(PollutionMagicBlocks.SPELL_PRISM_COLD.get(),
                        GTRecipeTypes.DISTILLATION_RECIPES, GTRecipeTypes.DISTILLERY_RECIPES))
                .where('X', Predicates.blocks(PollutionMagicBlocks.SPELL_PRISM_COLD.get())
                        .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setMaxGlobalLimited(1)))
                .where('Z', Predicates.blocks(PollutionMagicBlocks.SPELL_PRISM_COLD.get()))
                .where('P', Predicates.blocks(PollutionMagicBlocks.TITANIUM_PIPE.get()))
                .where('A', Predicates.blocks(PollutionMagicBlocks.BEAM_CORE_2.get()))
                .where('T', Predicates.blocks(PollutionMagicBlocks.TITANIUM_GEARBOX.get()))
                .where('#', Predicates.any())
                .build();
    }
}
