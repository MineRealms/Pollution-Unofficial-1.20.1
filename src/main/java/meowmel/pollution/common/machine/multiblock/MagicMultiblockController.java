package meowmel.pollution.common.machine.multiblock;

import com.gregtechceu.gtceu.api.block.ICoilType;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import meowmel.pollution.api.capability.IVisHatch;
import meowmel.pollution.api.magic.PollutionAspectMapping;
import meowmel.pollution.api.recipes.properties.MagicRecipeProperties;
import meowmel.pollution.common.machine.part.InfusedFluidHatchMachine;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

import java.util.HashSet;
import java.util.Set;

/**
 * Base controller of the magic multiblocks.
 *
 * <p>Upstream ({@code MagicRecipeMapMultiblockController}) extended the 1.12
 * {@code MultiblockRecipeMapController} and cached one vis hatch, one infused
 * fluid tank and the external-mod hatches (mana / blood / astral / tarot). The
 * port is built on modern {@link WorkableMultiblockMachine}: recipe handler
 * collection is done by GregTech itself, while the magic resources are read
 * directly from the parts collected here.</p>
 *
 * <p>Scope (2026-09-18): Thaumcraft-facing resources (vis, infused fluids) are
 * functional. Mana / life essence / astral / tarot stay reserved until their
 * systems are ported; recipes that ask for them fail the requirement check
 * exactly like upstream did when the hatch was missing.</p>
 */
public abstract class MagicMultiblockController extends WorkableMultiblockMachine {

    protected IVisHatch visHatch;
    protected InfusedFluidHatchMachine infusedFluidHatch;
    protected ICoilType coilType;

    private Set<Fluid> infusedFluids;

    protected MagicMultiblockController(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    protected RecipeLogic createRecipeLogic(Object... args) {
        return new MagicRecipeLogic(this);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        visHatch = null;
        infusedFluidHatch = null;
        for (IMultiPart part : getParts()) {
            if (visHatch == null && part.self() instanceof IVisHatch hatch) {
                visHatch = hatch;
            }
            if (infusedFluidHatch == null && part.self() instanceof InfusedFluidHatchMachine hatch) {
                infusedFluidHatch = hatch;
            }
        }
        Object matchedCoil = getMultiblockState().getMatchContext().get("CoilType");
        coilType = matchedCoil instanceof ICoilType coil ? coil : null;
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        visHatch = null;
        infusedFluidHatch = null;
        coilType = null;
        if (recipeLogic instanceof MagicRecipeLogic magicLogic) {
            magicLogic.resetMagicState();
        }
    }

    // ////////////////////////////////////
    // ***** Heating coils *****//
    // ////////////////////////////////////

    /** True when the formed structure contains GregTech heating coils. */
    public boolean hasCoil() {
        return coilType != null;
    }

    /**
     * Working temperature of the structure, taken from the matched GregTech
     * coil. Upstream added a custom coil-tier ramp because its coils were
     * custom blocks; the port uses the standard coil temperature instead.
     */
    public int getCurrentTemperature() {
        return coilType == null ? 0 : coilType.getCoilTemperature();
    }

    // ////////////////////////////////////
    // ***** Vis *****//
    // ////////////////////////////////////

    public int getVisCapacity() {
        return visHatch == null ? 0 : visHatch.getMaxVisStore();
    }

    public int getVisStore() {
        return visHatch == null ? 0 : visHatch.getVisStore();
    }

    public boolean consumeVis(int vis, boolean simulate) {
        if (vis <= 0) {
            return true;
        }
        return visHatch != null && visHatch.drainVis(vis, simulate);
    }

    // ////////////////////////////////////
    // ***** Infused fluids *****//
    // ////////////////////////////////////

    public boolean drainInfusedFluid(int amount, boolean simulate) {
        if (amount <= 0) {
            return true;
        }
        if (infusedFluidHatch == null) {
            return false;
        }
        FluidStack stored = infusedFluidHatch.tank.getFluidInTank(0);
        if (stored.isEmpty() || stored.getAmount() < amount || !isInfusedFluid(stored)) {
            return false;
        }
        if (!simulate) {
            infusedFluidHatch.tank.drain(amount, FluidAction.EXECUTE);
        }
        return true;
    }

    /** Accepts any fluid of a material that carries a Pollution aspect mapping. */
    public boolean isInfusedFluid(FluidStack stack) {
        if (infusedFluids == null) {
            Set<Fluid> fluids = new HashSet<>();
            for (var material : PollutionAspectMapping.all().keySet()) {
                if (material != null && material.hasFluid()) {
                    Fluid fluid = material.getFluid();
                    if (fluid != null) {
                        fluids.add(fluid);
                    }
                }
            }
            infusedFluids = fluids;
        }
        return infusedFluids.contains(stack.getFluid());
    }

    // ////////////////////////////////////
    // ***** Reserved systems *****//
    // ////////////////////////////////////

    /** Botania mana hatch is not ported yet. */
    public boolean consumeMana(long amount, boolean simulate) {
        return amount <= 0;
    }

    /** Blood Magic life essence hatch is not ported yet. */
    public boolean consumeLifeEssence(int amount, boolean simulate) {
        return amount <= 0;
    }

    /**
     * Validates non-consumable magic authorizations before a recipe starts
     * (port of upstream {@code checkMagicRequirements}, Thaumcraft subset).
     */
    public boolean checkMagicRequirements(GTRecipe recipe) {
        if (MagicRecipeProperties.getManaPerTick(recipe) > 0) {
            return false;
        }
        if (MagicRecipeProperties.getLifeEssencePerTick(recipe) > 0) {
            return false;
        }
        if (MagicRecipeProperties.hasVisCost(recipe) && visHatch == null) {
            return false;
        }
        if (MagicRecipeProperties.getInfusedFluidPerTick(recipe) > 0 && infusedFluidHatch == null) {
            return false;
        }
        if (!MagicRecipeProperties.getTarot(recipe).isEmpty()) {
            return false;
        }
        return !recipe.data.contains(MagicRecipeProperties.ASTRAL_CONDITION);
    }
}
