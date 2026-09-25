package meowmel.pollution.common.machine.multiblock.magic;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IRotorHolderMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.machine.multiblock.generator.LargeTurbineMachine;
import javax.annotation.Nullable;

/**
 * Magic turbines use GT's rotor power, speed, efficiency and fuel-parallel logic.
 * The mega variant has twelve rotors and sixteen times the base generation.
 * All installed rotors must meet the controller tier and remain intact.
 */
public abstract class AbstractMagicTurbineMachine extends LargeTurbineMachine {
    private final int expectedRotors;
    private final int outputMultiplier;

    /** Restrict both structure validation and JEI previews to usable holder tiers. */
    protected static com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate rotorsAtLeast(int tier) {
        return com.gregtechceu.gtceu.api.pattern.Predicates.blocks(java.util.Arrays.stream(
                        com.gregtechceu.gtceu.common.data.GTMachines.ROTOR_HOLDER)
                .filter(java.util.Objects::nonNull).filter(definition -> definition.getTier() >= tier)
                .map(com.gregtechceu.gtceu.api.machine.MachineDefinition::getBlock)
                .toArray(net.minecraft.world.level.block.Block[]::new));
    }

    protected AbstractMagicTurbineMachine(IMachineBlockEntity holder, int tier, int expectedRotors, int outputMultiplier) {
        super(holder, tier);
        this.expectedRotors = expectedRotors;
        this.outputMultiplier = outputMultiplier;
    }

    @Nullable
    public IRotorHolderMachine getRotorHolder() {
        return getParts().stream().filter(IRotorHolderMachine.class::isInstance)
                .map(IRotorHolderMachine.class::cast).findFirst().orElse(null);
    }

    @Override
    public boolean hasRotor() {
        var rotors = getParts().stream().filter(IRotorHolderMachine.class::isInstance)
                .map(IRotorHolderMachine.class::cast).toList();
        return rotors.size() == expectedRotors && rotors.stream().allMatch(rotor -> rotor.hasRotor()
                && rotor.self() instanceof ITieredMachine tiered && tiered.getTier() >= getTier());
    }

    @Override
    public long getOverclockVoltage() {
        return hasRotor() ? super.getOverclockVoltage() * outputMultiplier : 0;
    }

    @Override
    public boolean beforeWorking(@Nullable GTRecipe recipe) {
        return hasRotor() && super.beforeWorking(recipe);
    }

    @Override
    public boolean onWorking() {
        return hasRotor() && super.onWorking();
    }
}
