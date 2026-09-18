package meowmel.pollution.common.machine.multiblock;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import meowmel.pollution.api.recipes.properties.MagicRecipeProperties;
import net.minecraft.network.chat.Component;

/**
 * Recipe logic of the magic multiblocks.
 *
 * <p>Upstream ({@code MagicMultiblockRecipeLogic}) overrode the 1.12
 * {@code MultiblockRecipeLogic} progress update and paid vis / mana / life
 * essence / infused fluid itself. Modern GregTech drives recipes through
 * {@link RecipeLogic}; this port hooks the same points:</p>
 * <ul>
 *   <li>{@link #checkRecipe}: resource availability simulation (vis) plus
 *       {@code MagicMultiblockController#checkMagicRequirements};</li>
 *   <li>{@link #handleTickRecipe}: per-tick infused fluid draws and the
 *       per-craft vis payment (paid once, tracked by {@link #visPaidThisCraft});</li>
 *   <li>{@link #setupRecipe} / {@link #onRecipeFinish} / {@link #resetRecipeLogic}:
 *       payment-state cleanup.</li>
 * </ul>
 *
 * <p>Amplification (tarot / constellation bonuses) and the crystal transform
 * recipes of the upstream logic are intentionally out of scope until their
 * systems are ported.</p>
 */
public class MagicRecipeLogic extends RecipeLogic {

    private final MagicMultiblockController controller;

    private boolean visPaidThisCraft;

    public MagicRecipeLogic(MagicMultiblockController machine) {
        super(machine);
        this.controller = machine;
    }

    /** Clears per-craft payment state; called when the structure changes. */
    public void resetMagicState() {
        visPaidThisCraft = false;
    }

    @Override
    public void resetRecipeLogic() {
        super.resetRecipeLogic();
        resetMagicState();
    }

    @Override
    public void setupRecipe(GTRecipe recipe) {
        resetMagicState();
        super.setupRecipe(recipe);
    }

    @Override
    public void onRecipeFinish() {
        resetMagicState();
        super.onRecipeFinish();
    }

    @Override
    protected ActionResult checkRecipe(GTRecipe recipe) {
        ActionResult result = super.checkRecipe(recipe);
        if (!result.isSuccess()) {
            return result;
        }
        if (!controller.checkMagicRequirements(recipe)) {
            return ActionResult.fail(Component.translatable("pollution.magic.failure.hatches"), null, null);
        }
        int vis = MagicRecipeProperties.getVisPerCraft(recipe);
        if (vis > 0 && !controller.consumeVis(vis, true)) {
            return ActionResult.fail(Component.translatable("pollution.magic.failure.vis"), null, null);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public ActionResult handleTickRecipe(GTRecipe recipe) {
        ActionResult result = super.handleTickRecipe(recipe);
        if (!result.isSuccess()) {
            return result;
        }

        int infusedFluid = MagicRecipeProperties.getInfusedFluidPerTick(recipe);
        if (infusedFluid > 0) {
            if (!controller.drainInfusedFluid(infusedFluid, true)) {
                return ActionResult.fail(
                        Component.translatable("pollution.magic.failure.infused_fluid"), null, null);
            }
            controller.drainInfusedFluid(infusedFluid, false);
        }

        long mana = MagicRecipeProperties.getManaPerTick(recipe);
        if (mana > 0 && !controller.consumeMana(mana, false)) {
            return ActionResult.fail(Component.translatable("pollution.magic.failure.mana"), null, null);
        }

        int lifeEssence = MagicRecipeProperties.getLifeEssencePerTick(recipe);
        if (lifeEssence > 0 && !controller.consumeLifeEssence(lifeEssence, false)) {
            return ActionResult.fail(
                    Component.translatable("pollution.magic.failure.life_essence"), null, null);
        }

        int vis = MagicRecipeProperties.getVisPerCraft(recipe);
        if (vis > 0 && !visPaidThisCraft) {
            if (!controller.consumeVis(vis, false)) {
                return ActionResult.fail(Component.translatable("pollution.magic.failure.vis"), null, null);
            }
            visPaidThisCraft = true;
        }
        return ActionResult.SUCCESS;
    }
}
