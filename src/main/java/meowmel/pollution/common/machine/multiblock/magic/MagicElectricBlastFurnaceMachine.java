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
 * Magic electric blast furnace.
 *
 * <p>Upstream used custom tiered coil casings with its own temperature ramp.
 * The port matches standard GregTech heating coils
 * ({@code Predicates.heatingCoils()}); {@code MagicRecipeLogic} rejects recipes
 * whose {@code ebf_temp} exceeds the matched coil temperature, mirroring
 * modern {@code GTRecipeModifiers.ebfOverclock}.</p>
 */
public class MagicElectricBlastFurnaceMachine extends MagicMultiblockController {

    public MagicElectricBlastFurnaceMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    public Material getMaterial() {
        return PollutionMaterials.InfusedFire;
    }

    public static BlockPattern createPattern(MultiblockMachineDefinition definition) {
        return FactoryBlockPattern.start()
                .aisle("XXX", "CCC", "CCC", "XXX")
                .aisle("XXX", "C#C", "C#C", "XMX")
                .aisle("XSX", "CCC", "CCC", "XXX")
                .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                .where('X', MagicStructureElements.magicCasing(PollutionMagicBlocks.SPELL_PRISM_HOT.get(),
                        GTRecipeTypes.BLAST_RECIPES))
                .where('C', Predicates.heatingCoils())
                .where('M', Predicates.abilities(PartAbility.MUFFLER))
                .where('#', Predicates.air())
                .build();
    }
}
