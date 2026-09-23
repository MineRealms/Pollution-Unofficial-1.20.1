package meowmel.pollution.common.machine.multiblock.magic;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IRotorHolderMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import meowmel.pollution.common.machine.multiblock.MagicMultiblockController;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Shared rotor-holder behaviour of the magic turbines.
 *
 * <p>Upstream ({@code MetaTileEntityMagicLargeTurbine} /
 * {@code MetaTileEntityMagicMegaTurbine}) inherited GregTech's large turbine
 * workable handlers, which required a rotor in a tiered rotor holder, spun it
 * up, damaged it every second and aborted the craft when it broke. The modern
 * GTCEu equivalent is the {@link IRotorHolderMachine} part: while the host
 * controller works, {@code RotorHolderPartMachine#onWorking} raises the rotor
 * speed and applies {@code 1 + maintenanceProblems} durability damage every
 * 20 working ticks, deleting the rotor stack once its durability is exhausted.
 * Because {@link MagicMultiblockController} is a {@code WorkableMultiblockMachine},
 * that per-part hook already runs automatically for the magic turbines; this
 * base class adds the missing gate around it:</p>
 *
 * <ul>
 *   <li>{@link #beforeWorking} refuses to start (or resume) a craft while the
 *       holder is missing or empty, which also covers the persisted
 *       {@code lastRecipe} fast path that skips the rotor holder's own
 *       {@code modifyRecipe} check;</li>
 *   <li>{@link #onWorking} interrupts the running craft as soon as the rotor
 *       disappears mid-operation. Modern {@code RecipeLogic#interruptRecipe}
 *       resets the progress, effectively voiding the inputs consumed by the
 *       unfinished craft.</li>
 * </ul>
 *
 * <p>Deviation from upstream: durability damage is applied by the GT rotor
 * holder part at its standard rate (1 + maintenance problems per second of
 * working time); the magic turbine itself does not scale damage by parallel
 * runs, exactly like GT's modern {@code LargeTurbineMachine}. Upstream's
 * generation/parallel scaling by rotor speed, power and efficiency is still
 * not ported - only the durability gate is.</p>
 */
public abstract class AbstractMagicTurbineMachine extends MagicMultiblockController {

    private static final int MIN_DURABILITY_TO_WARN = 10;

    @Nullable
    private IRotorHolderMachine rotorHolder;

    protected AbstractMagicTurbineMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        rotorHolder = findRotorHolder();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        rotorHolder = null;
    }

    @Nullable
    protected IRotorHolderMachine findRotorHolder() {
        for (IMultiPart part : getParts()) {
            if (part instanceof IRotorHolderMachine holder) {
                return holder;
            }
        }
        return null;
    }

    @Nullable
    public IRotorHolderMachine getRotorHolder() {
        return rotorHolder;
    }

    public boolean hasRotor() {
        return rotorHolder != null && rotorHolder.hasRotor();
    }

    @Override
    public boolean beforeWorking(@Nullable GTRecipe recipe) {
        if (!hasRotor()) {
            return false;
        }
        return super.beforeWorking(recipe);
    }

    @Override
    public boolean onWorking() {
        if (!hasRotor()) {
            return false;
        }
        return super.onWorking();
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (!isFormed()) {
            return;
        }
        if (rotorHolder == null) {
            textList.add(Component.translatable("pollution.machine.magic_turbine.no_rotor_holder")
                    .withStyle(ChatFormatting.RED));
            return;
        }
        if (!rotorHolder.hasRotor()) {
            textList.add(Component.translatable("pollution.machine.magic_turbine.no_rotor")
                    .withStyle(ChatFormatting.RED));
            return;
        }
        textList.add(Component.translatable("gtceu.multiblock.turbine.rotor_speed",
                rotorHolder.getRotorSpeed(), rotorHolder.getMaxRotorHolderSpeed()));
        int durability = rotorHolder.getRotorDurabilityPercent();
        Component durabilityLine = Component.translatable("gtceu.multiblock.turbine.rotor_durability", durability);
        if (durability <= MIN_DURABILITY_TO_WARN) {
            durabilityLine = durabilityLine.copy().withStyle(ChatFormatting.RED);
        }
        textList.add(durabilityLine);
    }
}
