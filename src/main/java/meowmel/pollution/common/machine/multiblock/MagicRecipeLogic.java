package meowmel.pollution.common.machine.multiblock;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMufflerMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack;
import com.gregtechceu.gtceu.utils.GTTransferUtils;
import meowmel.pollution.api.amplification.AstralAmplifierSnapshot;
import meowmel.pollution.api.amplification.MagicAmplificationEngine;
import meowmel.pollution.api.amplification.MagicAmplificationResult;
import meowmel.pollution.api.amplification.MagicOutputProcessor;
import meowmel.pollution.api.pollution.MachinePollution;
import meowmel.pollution.api.recipes.properties.MagicRecipeProperties;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * <h2>Amplification wiring (mirrors upstream {@code MagicMultiblockRecipeLogic})</h2>
 * <p>{@link #setupRecipe} computes the amplification result through
 * {@link MagicAmplificationEngine} and stores it in {@link #activeAmplification}
 * for the lifetime of the craft. The result is applied as follows:</p>
 * <ul>
 *   <li><b>Duration</b>: after {@code super.setupRecipe} the effective
 *       {@link #duration} is scaled by {@code 1 - durationReduction} (once per
 *       craft), so progress and the UI see the shortened time.</li>
 *   <li><b>EU</b>: the tick draw is scaled by {@code 1 - eutReduction} by
 *       routing {@link #handleTickRecipe} through a copy of the recipe with a
 *       reduced EU tick input. Recipe matching still requires the unamplified
 *       EU/t, matching upstream's conservative search behaviour.</li>
 *   <li><b>Magic cost</b>: vis / infused fluid / mana / life essence draws get
 *       the upstream {@code magicCostReduction} discount and are scaled by the
 *       number of parallel runs.</li>
 *   <li><b>Output bonus and chance rerolls</b>: settled on completion through
 *       {@link MagicOutputProcessor#settle} and inserted into the output item
 *       handlers.</li>
 *   <li><b>Catalyst save chance</b>: {@code CONSUMABLE_CATALYST_INPUTS} entries
 *       that would be consumed are rolled once per craft and restored after the
 *       input IO pass.</li>
 *   <li><b>Progress retention</b>: while resources are missing, progress is
 *       held for {@code progressRetentionTicks} instead of regressing.</li>
 * </ul>
 *
 * <h2>Not wired (documented gaps)</h2>
 * <ul>
 *   <li><b>Extra parallel</b>: modern {@link RecipeLogic} has no
 *       {@code getParallelLimit}; parallel work is a machine recipe modifier
 *       ({@code recipe.parallels} + content scaling) and the magic machine
 *       definitions register neither a parallel hatch nor a modifier, so
 *       {@link MagicAmplificationResult#getExtraParallel()} cannot be added
 *       without touching off-limits registration code.</li>
 *   <li><b>Astral hatch</b>: the astral lens hatch
 *       ({@code MagicMultiblockController#getAstralLensHatch()}) is read from
 *       the formed structure. No lens machine exists in the port yet, so the
 *       lookup is {@code null} and the engine sees an uncalibrated lens; the
 *       recipe gate also fails for astral-gated recipes, matching upstream
 *       when its hatch was missing. The tarot hatch is ported and is read from
 *       the formed controller ({@code controller#getTarotHatch()}), but
 *       upstream gating means its bonuses only apply together with an astral
 *       data wafer.</li>
 *   <li><b>Furnace temperature bonus</b> and the <b>star afterglow</b>
 *       natural-sky match are not applied (no consumers yet).</li>
 * </ul>
 */
public class MagicRecipeLogic extends RecipeLogic {

    private final MagicMultiblockController controller;

    private boolean visPaidThisCraft;

    /** Result computed for the running craft; {@link MagicAmplificationResult#NONE} while idle. */
    private MagicAmplificationResult activeAmplification = MagicAmplificationResult.NONE;

    /**
     * Copy of the running recipe with the EU tick input scaled by the
     * amplification's EU reduction. Built lazily so it can be rebuilt after a
     * world reload (only the result, not the recipe, is persisted).
     */
    private GTRecipe amplifiedTickRecipe;

    private int progressRetentionTicks;

    private int chariotStacks;
    private ResourceLocation lastCompletedRecipeId;

    private final Map<String, Double> fractionalOutputRemainders = new HashMap<>();

    public MagicRecipeLogic(MagicMultiblockController machine) {
        super(machine);
        this.controller = machine;
    }

    /**
     * Clears per-craft payment and amplification state; called when the
     * structure changes, when a new craft is prepared and when a craft finishes
     * without a successor.
     */
    public void resetMagicState() {
        visPaidThisCraft = false;
        activeAmplification = MagicAmplificationResult.NONE;
        amplifiedTickRecipe = null;
        progressRetentionTicks = 0;
        if (controller != null) {
            controller.setMagicFocusLocked(false);
        }
    }

    @Override
    public void resetRecipeLogic() {
        super.resetRecipeLogic();
        resetMagicState();
    }

    /**
     * Computes and stores the amplification for the craft, then applies the
     * duration reduction to the effective duration. The EU reduction is applied
     * later, per tick, through {@link #handleTickRecipe}.
     */
    @Override
    public void setupRecipe(GTRecipe recipe) {
        resetMagicState();
        activeAmplification = calculateAmplification(recipe);
        super.setupRecipe(recipe);
        if (getLastRecipe() != recipe) {
            // Recipe was rejected (missing inputs or beforeWorking returned false).
            resetMagicState();
            return;
        }
        if (activeAmplification.getDurationReduction() > 0.0D) {
            duration = Math.max(1,
                    (int) Math.ceil(duration * (1.0D - activeAmplification.getDurationReduction())));
        }
        if (activeAmplification.getEutReduction() > 0.0D) {
            amplifiedTickRecipe = buildEutReducedRecipe(recipe);
        }
        if (activeAmplification.isActive() && !activeAmplification.getTarot().isEmpty()) {
            controller.setMagicFocusLocked(true);
        }
    }

    /**
     * Settles the amplification output bonus after the regular outputs were
     * produced. Runs after {@code super.onRecipeFinish} because modern GT
     * handles recipe outputs there (and may immediately set up the next craft,
     * in which case {@link #setupRecipe} already installed the next result).
     */
    @Override
    public void onRecipeFinish() {
        GTRecipe finished = getLastRecipe();
        MagicAmplificationResult result = activeAmplification;
        int parallel = finished == null ? 1 : Math.max(1, finished.getTotalRuns());
        updateChariotStacks(finished, result);
        super.onRecipeFinish();
        if (finished != null) {
            emitMufflerPollution();
        }
        if (finished != null && result.isActive()) {
            List<ItemStack> extras = MagicOutputProcessor.settle(finished, parallel, result,
                    fractionalOutputRemainders);
            if (!extras.isEmpty()) {
                insertBonusOutputs(extras);
            }
        }
        if (getLastRecipe() == null || getLastRecipe() == finished) {
            resetMagicState();
        }
    }

    /**
     * Adds the muffler hatch pollution of the completed operation.
     *
     * <p>Upstream emitted this from {@code MetaTileEntity#pollution}, which
     * modern GregTech removed. The port re-hooks recipe completion: when the
     * formed structure contains a muffler hatch, its per-operation output
     * ({@link IMufflerMachine#getHazardStrengthPerOperation()}, the modern
     * equivalent of the old pollution value) is added to the chunk pollution
     * scaled by
     * {@link meowmel.pollution.PollutionConfig#MUFFLER_POLLUTION_MULTIPLIER}.
     * Runs after {@code super.onRecipeFinish()} so it happens alongside the
     * muffler's own environmental hazard pass.</p>
     */
    private void emitMufflerPollution() {
        if (!(controller.getLevel() instanceof ServerLevel level)) {
            return;
        }
        for (IMultiPart part : controller.getParts()) {
            if (part.self() instanceof IMufflerMachine muffler) {
                MachinePollution.addMufflerPollution(level, controller.getPos(),
                        muffler.getHazardStrengthPerOperation());
                return;
            }
        }
    }

    /**
     * Adds the catalyst-save protection around the regular input pass: eligible
     * consumable catalysts are rolled once per craft and restored after GT
     * consumed the recipe inputs.
     */
    @Override
    protected ActionResult handleRecipeIO(GTRecipe recipe, IO io) {
        if (io != IO.IN) {
            return super.handleRecipeIO(recipe, io);
        }
        List<SavedCatalyst> protectedCatalysts = collectProtectedCatalysts(recipe);
        ActionResult result = super.handleRecipeIO(recipe, io);
        if (result.isSuccess()) {
            for (SavedCatalyst catalyst : protectedCatalysts) {
                GTTransferUtils.insertItem(catalyst.inventory(), catalyst.stack(), false);
            }
        }
        return result;
    }

    @Override
    protected ActionResult checkRecipe(GTRecipe recipe) {
        ActionResult result = super.checkRecipe(recipe);
        if (!result.isSuccess()) {
            return result;
        }
        Component requirementFailure = controller.getMagicRequirementFailure(recipe);
        if (requirementFailure != null) {
            return ActionResult.fail(requirementFailure, null, null);
        }
        int vis = MagicRecipeProperties.getVisPerCraft(recipe);
        if (vis > 0 && !controller.consumeVis(vis, true)) {
            return ActionResult.fail(Component.translatable("pollution.magic.failure.vis"), null, null);
        }
        if (recipe.data.contains("ebf_temp")) {
            if (!controller.hasCoil()) {
                return ActionResult.fail(Component.translatable("pollution.magic.failure.coil"), null, null);
            }
            int requiredTemperature = recipe.data.getInt("ebf_temp");
            if (requiredTemperature > controller.getCurrentTemperature()) {
                return ActionResult.fail(
                        Component.translatable("pollution.magic.failure.temperature", requiredTemperature),
                        null, null);
            }
        }
        return ActionResult.SUCCESS;
    }

    /**
     * Draws the magic resources and, when the EU reduction is active, performs
     * the energy IO against a copy of the recipe whose EU tick input is scaled
     * down. The magic draws mirror the upstream parallel scaling and
     * {@code magicCostReduction} discount. Like upstream
     * {@code updateRecipeProgress}, the non-consumable authorizations (astral
     * sky, tarot focus) are re-validated every tick, and mana / life essence
     * are simulated before being drawn so a failed draw never partially drains
     * a provider.
     */
    @Override
    public ActionResult handleTickRecipe(GTRecipe recipe) {
        GTRecipe tickRecipe = recipe;
        if (recipe == getLastRecipe() && activeAmplification.getEutReduction() > 0.0D) {
            if (amplifiedTickRecipe == null) {
                amplifiedTickRecipe = buildEutReducedRecipe(recipe);
            }
            if (amplifiedTickRecipe != null) {
                tickRecipe = amplifiedTickRecipe;
            }
        }
        ActionResult result = super.handleTickRecipe(tickRecipe);
        if (!result.isSuccess()) {
            return result;
        }
        Component requirementFailure = controller.getMagicRequirementFailure(recipe);
        if (requirementFailure != null) {
            return ActionResult.fail(requirementFailure, null, null);
        }

        int parallel = Math.max(1, recipe.getTotalRuns());

        int infusedFluid = discount(scaleByParallel(MagicRecipeProperties.getInfusedFluidPerTick(recipe), parallel));
        if (infusedFluid > 0) {
            if (!controller.drainInfusedFluid(infusedFluid, true)) {
                return ActionResult.fail(
                        Component.translatable("pollution.magic.failure.infused_fluid"), null, null);
            }
            controller.drainInfusedFluid(infusedFluid, false);
        }

        long mana = discount(scaleByParallel(MagicRecipeProperties.getManaPerTick(recipe), parallel));
        if (mana > 0) {
            if (!controller.consumeMana(mana, true)) {
                return ActionResult.fail(Component.translatable("pollution.magic.failure.mana"), null, null);
            }
            controller.consumeMana(mana, false);
        }

        int lifeEssence = discount(
                scaleByParallel(MagicRecipeProperties.getLifeEssencePerTick(recipe), parallel));
        if (lifeEssence > 0) {
            if (!controller.consumeLifeEssence(lifeEssence, true)) {
                return ActionResult.fail(
                        Component.translatable("pollution.magic.failure.life_essence"), null, null);
            }
            controller.consumeLifeEssence(lifeEssence, false);
        }

        int vis = discount(scaleByParallel(MagicRecipeProperties.getVisPerCraft(recipe), parallel));
        if (vis > 0 && !visPaidThisCraft) {
            if (!controller.consumeVis(vis, false)) {
                return ActionResult.fail(Component.translatable("pollution.magic.failure.vis"), null, null);
            }
            visPaidThisCraft = true;
        }

        progressRetentionTicks = 0;
        return ActionResult.SUCCESS;
    }

    /**
     * Upstream held progress while waiting for resources when the active
     * amplification granted retention ticks. Modern GT regresses progress in
     * {@link #regressRecipe}; this override consumes the retention budget first.
     */
    @Override
    protected void regressRecipe() {
        if (progress > 0 && activeAmplification.getProgressRetentionTicks() > 0
                && progressRetentionTicks < activeAmplification.getProgressRetentionTicks()) {
            progressRetentionTicks++;
            return;
        }
        super.regressRecipe();
    }

    /** Result of the running craft, for UI / diagnostics. */
    public MagicAmplificationResult getActiveAmplification() {
        return activeAmplification;
    }

    // ////////////////////////////////////
    // ***** Amplification internals *****//
    // ////////////////////////////////////

    private MagicAmplificationResult calculateAmplification(GTRecipe recipe) {
        // from(null) yields the empty snapshot: no astral hatch is installed in
        // the port yet, so the engine keeps returning its uncalibrated result.
        AstralAmplifierSnapshot snapshot = AstralAmplifierSnapshot.from(controller.getAstralLensHatch());
        int stacks = recipe.id != null && recipe.id.equals(lastCompletedRecipeId) ? chariotStacks : 0;
        boolean singleParallel = recipe.getTotalRuns() <= 1;
        return MagicAmplificationEngine.calculate(processTagsFor(recipe), recipe.duration, snapshot,
                controller.getTarotHatch(), stacks, singleParallel);
    }

    /**
     * Explicit recipe tags win; legacy recipes fall back to the machine's
     * registered profile so they still receive the safe first-batch bonuses.
     * Shared with {@code MagicMultiblockController#checkMagicRequirements} so
     * the recipe gate and the amplification engine read the same tag mask.
     */
    private long processTagsFor(GTRecipe recipe) {
        return controller.getMagicProcessTags(recipe);
    }

    private void updateChariotStacks(GTRecipe finished, MagicAmplificationResult result) {
        ResourceLocation id = finished == null ? null : finished.id;
        boolean chariot = "the_chariot".equals(result.getTarot());
        if (chariot && id != null && id.equals(lastCompletedRecipeId)) {
            chariotStacks = Math.min(5, chariotStacks + 1);
        } else if (chariot) {
            chariotStacks = 1;
        } else {
            chariotStacks = 0;
        }
        lastCompletedRecipeId = id;
    }

    /**
     * Copies the recipe and scales only its EU tick input. The original recipe
     * instance is never mutated: it stays the one GT matches and re-uses.
     */
    private GTRecipe buildEutReducedRecipe(GTRecipe recipe) {
        GTRecipe reduced = recipe.copy();
        List<Content> euInputs = reduced.tickInputs.get(EURecipeCapability.CAP);
        if (euInputs == null || euInputs.isEmpty()) {
            return null;
        }
        double factor = 1.0D - activeAmplification.getEutReduction();
        List<Content> scaled = new ArrayList<>(euInputs.size());
        for (Content content : euInputs) {
            EnergyStack stack = EURecipeCapability.CAP.of(content.getContent());
            long voltage = stack.voltage() <= 0L ? 0L
                    : Math.max(1L, (long) Math.ceil(stack.voltage() * factor));
            scaled.add(new Content(new EnergyStack(voltage, stack.amperage()),
                    content.chance, content.maxChance, content.tierChanceBoost));
        }
        reduced.tickInputs.put(EURecipeCapability.CAP, scaled);
        return reduced;
    }

    private List<SavedCatalyst> collectProtectedCatalysts(GTRecipe recipe) {
        List<SavedCatalyst> saved = new ArrayList<>();
        double chance = activeAmplification.getCatalystSaveChance();
        if (chance <= 0.0D) {
            return saved;
        }
        String configured = MagicRecipeProperties.getConsumableCatalystInputs(recipe);
        if (configured.isEmpty()) {
            return saved;
        }
        List<Content> itemInputs = recipe.getInputContents(ItemRecipeCapability.CAP);
        for (String token : configured.split(",")) {
            int index;
            try {
                index = Integer.parseInt(token.trim());
            } catch (NumberFormatException ignored) {
                // Invalid pack data must not block an otherwise valid recipe.
                continue;
            }
            if (index < 0 || index >= itemInputs.size()) {
                continue;
            }
            Content content = itemInputs.get(index);
            // Non-consumable (chance 0) and chanced inputs are handled by GT's own chance logic.
            if (content.chance < content.maxChance) {
                continue;
            }
            Ingredient ingredient = ItemRecipeCapability.CAP.of(content.getContent());
            ItemStack[] candidates = ingredient.getItems();
            if (candidates.length == 0 || candidates[0].getCount() <= 0) {
                continue;
            }
            if (GTValues.RNG.nextDouble() >= chance) {
                continue;
            }
            for (IRecipeHandler<?> handler : machine.getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP)) {
                if (!(handler instanceof IItemHandlerModifiable inventory)) {
                    continue;
                }
                ItemStack source = findMatchingStack(inventory, ingredient);
                if (source.isEmpty()) {
                    continue;
                }
                int amount = Math.min(candidates[0].getCount(), source.getMaxStackSize());
                saved.add(new SavedCatalyst(inventory, source.copyWithCount(amount)));
                break;
            }
        }
        return saved;
    }

    private static ItemStack findMatchingStack(IItemHandlerModifiable inventory, Ingredient ingredient) {
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.isEmpty() && ingredient.test(stack)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * Inserts the amplification extras into the output item handlers. Stacks
     * that do not fit are dropped, mirroring the upstream behaviour when the
     * machine output overflowed.
     */
    private void insertBonusOutputs(List<ItemStack> extras) {
        List<ItemStack> remaining = new ArrayList<>(extras);
        for (IRecipeHandler<?> handler : machine.getCapabilitiesFlat(IO.OUT, ItemRecipeCapability.CAP)) {
            if (remaining.isEmpty()) {
                return;
            }
            if (!(handler instanceof IItemHandlerModifiable inventory)) {
                continue;
            }
            for (int index = 0; index < remaining.size(); ) {
                ItemStack leftover = GTTransferUtils.insertItem(inventory, remaining.get(index), false);
                if (leftover.isEmpty()) {
                    remaining.remove(index);
                } else {
                    remaining.set(index, leftover);
                    index++;
                }
            }
        }
    }

    private int scaleByParallel(int amount, int parallel) {
        if (amount <= 0 || parallel <= 1) {
            return Math.max(0, amount);
        }
        long result = (long) amount * parallel;
        return result > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) result;
    }

    private long scaleByParallel(long amount, int parallel) {
        if (amount <= 0L || parallel <= 1) {
            return Math.max(0L, amount);
        }
        return amount > Long.MAX_VALUE / parallel ? Long.MAX_VALUE : amount * parallel;
    }

    private int discount(int amount) {
        if (amount <= 0) {
            return 0;
        }
        return Math.max(1, (int) Math.ceil(amount * (1.0D - activeAmplification.getMagicCostReduction())));
    }

    private long discount(long amount) {
        if (amount <= 0L) {
            return 0L;
        }
        return Math.max(1L, (long) Math.ceil(amount * (1.0D - activeAmplification.getMagicCostReduction())));
    }

    private record SavedCatalyst(IItemHandlerModifiable inventory, ItemStack stack) {}

    // ////////////////////////////////////
    // ***** Persistence *****//
    // ////////////////////////////////////

    @Override
    public void saveCustomPersistedData(CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        tag.put("MagicAmplification", activeAmplification.serializeSnapshot());
        tag.putInt("MagicChariotStacks", chariotStacks);
        tag.putInt("MagicProgressRetention", progressRetentionTicks);
        CompoundTag remainders = new CompoundTag();
        for (Map.Entry<String, Double> entry : fractionalOutputRemainders.entrySet()) {
            remainders.putDouble(entry.getKey(), entry.getValue());
        }
        tag.put("MagicOutputRemainders", remainders);
    }

    @Override
    public void loadCustomPersistedData(CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        activeAmplification = MagicAmplificationResult.deserializeSnapshot(tag.getCompound("MagicAmplification"));
        chariotStacks = Math.max(0, Math.min(5, tag.getInt("MagicChariotStacks")));
        progressRetentionTicks = Math.max(0, tag.getInt("MagicProgressRetention"));
        fractionalOutputRemainders.clear();
        CompoundTag remainders = tag.getCompound("MagicOutputRemainders");
        for (String key : remainders.getAllKeys()) {
            fractionalOutputRemainders.put(key, remainders.getDouble(key));
        }
        // The recipe itself is not persisted here; rebuild the reduced-EU copy lazily.
        amplifiedTickRecipe = null;
    }
}
