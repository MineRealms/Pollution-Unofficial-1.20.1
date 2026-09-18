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

public class MagicBreweryMachine extends MagicMultiblockController {

    public MagicBreweryMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    public Material getMaterial() {
        return PollutionMaterials.InfusedWater;
    }

    public static BlockPattern createPattern(MultiblockMachineDefinition definition) {
        return FactoryBlockPattern.start()
                .aisle("#XXX#", "#XXX#", "#XXX#", "#XXX#", "#####")
                .aisle("XXXXX", "XCCCX", "XAAAX", "XXAXX", "##X##")
                .aisle("XXXXX", "XCPCX", "XAPAX", "XAPAX", "#XMX#")
                .aisle("XXXXX", "XCCCX", "XAAAX", "XXAXX", "##X##")
                .aisle("#XXX#", "#XSX#", "#XXX#", "#XXX#", "#####")
                .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                .where('X', MagicStructureElements.magicCasing(PollutionMagicBlocks.SPELL_PRISM_COLD.get(),
                        GTRecipeTypes.BREWING_RECIPES, GTRecipeTypes.FERMENTING_RECIPES,
                        GTRecipeTypes.FLUID_HEATER_RECIPES))
                .where('C', Predicates.blocks(PollutionMagicBlocks.BEAM_CORE_2.get()))
                .where('P', Predicates.blocks(PollutionMagicBlocks.TUNGSTENSTEEL_PIPE.get()))
                .where('A', Predicates.blocks(PollutionMagicBlocks.TUNGSTENSTEEL_GEARBOX.get()))
                .where('M', Predicates.abilities(PartAbility.MUFFLER))
                .where('#', Predicates.any())
                .build();
    }
}
