package meowmel.pollution.common.machine.multiblock;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import meowmel.pollution.common.machine.multiblock.mana.ManaMultiblockController;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fluids.capability.IFluidHandler;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import static com.gregtechceu.gtceu.api.pattern.util.RelativeDirection.UP;

/** Shared fraction routing for magic and Botania towers, in the pattern's local upward direction. */
public abstract class LayeredMagicTowerMachine extends ManaMultiblockController {
    protected LayeredMagicTowerMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    private List<IFluidHandler> layerOutputs = List.of();

    @Override
    protected RecipeLogic createRecipeLogic(Object... args) {
        return new LayeredRecipeLogic(this);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        List<IFluidHandler> outputs = new ArrayList<>();
        getParts().stream()
                .filter(part -> PartAbility.EXPORT_FLUIDS.isApplicable(part.self().getBlockState().getBlock()))
                .sorted(Comparator.comparing(part -> part.self().getPos(),
                        UP.getSorter(getFrontFacing(), getUpwardsFacing(), isFlipped())))
                .forEach(part -> part.getRecipeHandlers().stream()
                        .flatMap(handler -> handler.getCapability(FluidRecipeCapability.CAP).stream())
                        .filter(IFluidHandler.class::isInstance).map(IFluidHandler.class::cast)
                        .findFirst().ifPresent(outputs::add));
        layerOutputs = List.copyOf(outputs);
    }

    @Override
    public void onStructureInvalid() {
        layerOutputs = List.of();
        super.onStructureInvalid();
    }

    private boolean outputFluids(GTRecipe recipe, IFluidHandler.FluidAction action) {
        var fluids = recipe.getOutputContents(FluidRecipeCapability.CAP);
        // Never silently void fractions when the tower is too short.
        if (fluids.size() > layerOutputs.size()) return false;
        for (int i = 0; i < fluids.size(); i++) {
            var stacks = FluidRecipeCapability.CAP.of(fluids.get(i).getContent()).getStacks();
            if (stacks.length == 0) return false;
            var stack = stacks[0];
            var handler = layerOutputs.get(i);
            int filled = handler instanceof NotifiableFluidTank tank
                    ? tank.fillInternal(stack, action) : handler.fill(stack, action);
            if (filled != stack.getAmount()) return false;
        }
        return true;
    }

    /** Constrain a parallel batch by each fraction's destination, not the aggregate tank space. */
    public int outputParallelLimit(GTRecipe recipe, int maximum) {
        var fluids = recipe.getOutputContents(FluidRecipeCapability.CAP);
        if (fluids.size() > layerOutputs.size()) return 0;
        for (int i = 0; i < fluids.size(); i++) {
            var stacks = FluidRecipeCapability.CAP.of(fluids.get(i).getContent()).getStacks();
            if (stacks.length == 0 || stacks[0].isEmpty()) return 0;
            var probe = stacks[0].copy();
            probe.setAmount(Integer.MAX_VALUE);
            var handler = layerOutputs.get(i);
            int space = handler instanceof NotifiableFluidTank tank ? tank.fillInternal(probe, IFluidHandler.FluidAction.SIMULATE)
                    : handler.fill(probe, IFluidHandler.FluidAction.SIMULATE);
            maximum = Math.min(maximum, space / stacks[0].getAmount());
        }
        return maximum;
    }

    private static class LayeredRecipeLogic extends MagicRecipeLogic {
        private final LayeredMagicTowerMachine tower;

        private LayeredRecipeLogic(LayeredMagicTowerMachine tower) {
            super(tower);
            this.tower = tower;
        }

        private static GTRecipe withoutFluidOutputs(GTRecipe recipe) {
            GTRecipe copy = recipe.copy();
            copy.outputs.remove(FluidRecipeCapability.CAP);
            return copy;
        }

        private static ActionResult fullOutput() {
            return ActionResult.fail(Component.translatable("gtceu.recipe_logic.insufficient_out"),
                    FluidRecipeCapability.CAP, IO.OUT);
        }

        @Override
        public void onRecipeFinish() {
            if (getLastRecipe() != null && !tower.outputFluids(getLastRecipe(), IFluidHandler.FluidAction.SIMULATE)) {
                setWaiting(fullOutput().reason());
                return;
            }
            super.onRecipeFinish();
        }

        @Override
        protected ActionResult matchRecipe(GTRecipe recipe) {
            if (!tower.outputFluids(recipe, IFluidHandler.FluidAction.SIMULATE)) return fullOutput();
            return super.matchRecipe(withoutFluidOutputs(recipe));
        }

        @Override
        protected ActionResult handleRecipeIO(GTRecipe recipe, IO io) {
            if (io != IO.OUT) return super.handleRecipeIO(recipe, io);
            if (!tower.outputFluids(recipe, IFluidHandler.FluidAction.SIMULATE)) return fullOutput();
            ActionResult result = super.handleRecipeIO(withoutFluidOutputs(recipe), io);
            if (!result.isSuccess()) return result;
            return tower.outputFluids(recipe, IFluidHandler.FluidAction.EXECUTE)
                    ? ActionResult.SUCCESS : fullOutput();
        }
    }

}
