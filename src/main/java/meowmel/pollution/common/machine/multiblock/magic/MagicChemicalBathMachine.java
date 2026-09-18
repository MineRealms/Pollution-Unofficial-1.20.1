package meowmel.pollution.common.machine.multiblock.magic;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import meowmel.pollution.api.unification.PollutionMaterials;
import meowmel.pollution.common.block.PollutionMagicBlocks;
import meowmel.pollution.common.machine.multiblock.MagicMultiblockController;
import meowmel.pollution.common.machine.multiblock.MagicStructureElements;
import net.minecraft.world.level.material.Fluids;

/**
 * Magic chemical bath.
 *
 * <p>Upstream registered the water positions of the pattern and slowly filled
 * them with source blocks after forming. The port requires the water to be
 * present at formation ({@code Predicates.fluids(Fluids.WATER)}) because
 * modern GregTech matches the fluid directly; the timed auto-fill behaviour is
 * recorded as a TODO.</p>
 */
public class MagicChemicalBathMachine extends MagicMultiblockController {

    public MagicChemicalBathMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    public Material getMaterial() {
        return PollutionMaterials.InfusedWater;
    }

    public static BlockPattern createPattern(MultiblockMachineDefinition definition) {
        return FactoryBlockPattern.start()
                .aisle("XXXXXXX", "XXXXXXX", "XXXXXXX")
                .aisle("XXXXXXX", "XDDCDDX", "XAAAAAX")
                .aisle("XXXXXXX", "XDDCDDX", "XAAAAAX")
                .aisle("XXXXXXX", "XCCCCCX", "XAAAAAX")
                .aisle("XXXXXXX", "XDDCDDX", "XAAAAAX")
                .aisle("XXXXXXX", "XDDCDDX", "XAAAAAX")
                .aisle("XXXXXXX", "XXXSXXX", "XXXXXXX")
                .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                .where('X', MagicStructureElements.magicCasing(PollutionMagicBlocks.SPELL_PRISM_WATER.get(),
                        GTRecipeTypes.CHEMICAL_BATH_RECIPES, GTRecipeTypes.ORE_WASHER_RECIPES))
                .where('C', Predicates.blocks(PollutionMagicBlocks.TUNGSTENSTEEL_GEARBOX.get()))
                .where('D', Predicates.blocks(PollutionMagicBlocks.CAMINATED_GLASS.get()))
                .where('A', Predicates.fluids(Fluids.WATER))
                .build();
    }
}
