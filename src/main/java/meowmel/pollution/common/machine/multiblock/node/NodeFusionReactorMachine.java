package meowmel.pollution.common.machine.multiblock.node;

import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import meowmel.pollution.api.capability.ICleanVis;
import meowmel.pollution.api.pollution.PollutionEngine;
import meowmel.pollution.api.recipes.PORecipeMaps;
import meowmel.pollution.api.unification.PollutionMaterials;
import meowmel.pollution.common.item.PackagedAuraNode;
import meowmel.pollution.common.machine.multiblock.MagicMultiblockController;
import meowmel.pollution.common.machine.multiblock.MagicRecipeLogic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Node fusion reactor (LuV/ZPM/UV): runs fusion and node-fusion recipes with
 * packaged aura nodes as the catalyst.
 *
 * <p>Upstream burned White/Black/Starry Mansus fluids; the port maps them to
 * {@code InfusedLight}/{@code InfusedDark}/{@code InfusedAura}. The TC6
 * ambient-aura cleanliness check becomes an industrial-pollution check
 * ({@code PollutionEngine.get <= 4.2}). Node effect tables (parallel, energy,
 * progress, mansus counts) keep the upstream fall-through switch semantics.</p>
 *
 * <p>Fusion startup cost: upstream kept an internal energy buffer fed from the
 * input energy hatches ({@code energyHatches × 2^(tier-6) × 10M EU}) plus a
 * persisted {@code heat} counter. A recipe could only start when
 * {@code euToStart <= buffer capacity}, and the missing
 * {@code euToStart - heat} EU was drained from the buffer first (upstream's
 * {@code heat = heatDiff} store bug is fixed to the modern
 * {@code heat += heatDiff}). The port mirrors that: the recipe logic charges
 * the startup EU before the recipe starts and blocks otherwise; heat and the
 * buffer charge are saved to NBT.</p>
 */
public class NodeFusionReactorMachine extends MagicMultiblockController implements ICleanVis {

    private static final long INTERNAL_ENERGY_PER_HATCH = 10_000_000L;

    private final int tier;
    private final NotifiableEnergyContainer internalEnergyContainer;

    private TickableSubscription tickSubscription;
    private int overallParallelAmount = 1;
    private int overallBlackAmount;
    private int overallWhiteAmount;
    private int overallStarryAmount;

    /** Persisted fusion startup heat (EU) accumulated from the recipes. */
    private long heat;
    /** Persisted internal buffer charge, kept across structure re-forms. */
    private long storedInternalEnergy;
    @Nullable
    private EnergyContainerList inputEnergyContainers;

    public NodeFusionReactorMachine(IMachineBlockEntity holder, int tier) {
        super(holder);
        this.tier = tier;
        this.internalEnergyContainer = new NotifiableEnergyContainer(this, 0L, 0L, 0L, 0L, 0L);
        this.internalEnergyContainer.setCapabilityValidator(side -> side == null);
    }

    public int getTier() {
        return tier;
    }

    public Material getMaterial() {
        return PollutionMaterials.InfusedMagic;
    }

    @Override
    protected RecipeLogic createRecipeLogic(Object... args) {
        return new NodeFusionReactorRecipeLogic(this);
    }

    @Override
    protected GTRecipe getRealRecipe(GTRecipe recipe) {
        GTRecipe modified = super.getRealRecipe(recipe);
        if (modified == null) {
            return null;
        }
        int limit = 1;
        if (recipe.recipeType == GTRecipeTypes.FUSION_RECIPES) {
            limit = overallParallelAmount * 2;
        } else if (recipe.recipeType == PORecipeMaps.NODE_MAGIC_FUSION_RECIPES) {
            limit = overallParallelAmount;
        }
        if (limit <= 1) {
            return modified;
        }
        int parallels = ParallelLogic.getParallelAmount(self(), modified, limit);
        if (parallels <= 1) {
            return modified;
        }
        return ModifierFunction.builder()
                .modifyAllContents(ContentModifier.multiplier(parallels))
                .parallels(parallels)
                .build()
                .apply(modified);
    }

    @Override
    public boolean isCleanVis() {
        return !(getLevel() instanceof ServerLevel level) || PollutionEngine.get(level, getPos()) <= 4.2;
    }

    // ////////////////////////////////////
    // ***** Fusion startup buffer *****//
    // ////////////////////////////////////

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        List<IEnergyContainer> containers = new ArrayList<>();
        for (IRecipeHandler<?> handler : getCapabilitiesFlat(IO.IN, EURecipeCapability.CAP)) {
            if (handler instanceof IEnergyContainer container && container != internalEnergyContainer) {
                containers.add(container);
            }
        }
        inputEnergyContainers = new EnergyContainerList(containers);
        internalEnergyContainer.resetBasicInfo(calculateEnergyStorageFactor(containers.size()),
                0L, 0L, 0L, 0L);
        internalEnergyContainer.setEnergyStored(
                Math.min(storedInternalEnergy, internalEnergyContainer.getEnergyCapacity()));
    }

    @Override
    public void onStructureInvalid() {
        storedInternalEnergy = internalEnergyContainer.getEnergyStored();
        internalEnergyContainer.resetBasicInfo(0L, 0L, 0L, 0L, 0L);
        internalEnergyContainer.setEnergyStored(0L);
        inputEnergyContainers = null;
        super.onStructureInvalid();
    }

    /** Upstream: {@code energyHatches × 2^(tier-6) × 10M EU} internal capacity. */
    private long calculateEnergyStorageFactor(int energyInputAmount) {
        return energyInputAmount * (long) Math.pow(2.0, tier - 6) * INTERNAL_ENERGY_PER_HATCH;
    }

    /**
     * Moves energy from the input hatches into the internal startup buffer, so
     * the reactor can accumulate the fusion {@code eu_to_start} cost over time.
     */
    private void chargeInternalEnergyBuffer() {
        if (inputEnergyContainers == null) {
            return;
        }
        long space = internalEnergyContainer.getEnergyCapacity() - internalEnergyContainer.getEnergyStored();
        if (space <= 0L) {
            return;
        }
        internalEnergyContainer.addEnergy(inputEnergyContainers.removeEnergy(space));
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            tickSubscription = subscribeServerTick(this::tickReactor);
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (tickSubscription != null) {
            tickSubscription.unsubscribe();
            tickSubscription = null;
        }
    }

    private void tickReactor() {
        if (!(getLevel() instanceof ServerLevel)) {
            return;
        }
        chargeInternalEnergyBuffer();
        if (!isFormed()) {
            return;
        }
        if (getOffsetTimer() % 20 != 0) {
            return;
        }
        ItemBusPartMachine items = findPart(ItemBusPartMachine.class);
        FluidHatchPartMachine fluids = findFluidHatch(PartAbility.IMPORT_FLUIDS);
        if (items == null || fluids == null) {
            return;
        }
        readNodeInfo(items);

        if (overallBlackAmount == 0 && overallWhiteAmount == 0 && overallStarryAmount == 0) {
            return;
        }
        boolean supplied = hasMansus(fluids, PollutionMaterials.InfusedDark, overallBlackAmount)
                && hasMansus(fluids, PollutionMaterials.InfusedLight, overallWhiteAmount)
                && hasMansus(fluids, PollutionMaterials.InfusedAura, overallStarryAmount);
        if (supplied && recipeLogic.isWorking()) {
            drainMansus(fluids, PollutionMaterials.InfusedDark, overallBlackAmount);
            drainMansus(fluids, PollutionMaterials.InfusedLight, overallWhiteAmount);
            drainMansus(fluids, PollutionMaterials.InfusedAura, overallStarryAmount);
        }
    }

    private void readNodeInfo(ItemBusPartMachine items) {
        int parallelAmount = 1;
        int blackAmount = 0;
        int whiteAmount = 0;
        int starryAmount = 0;
        var inventory = items.getInventory().storage;
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!PackagedAuraNode.isNode(stack)) {
                continue;
            }
            switch (PackagedAuraNode.type(stack)) {
                case "Ominous":
                    parallelAmount += 1;
                    blackAmount += 1;
                    whiteAmount += 1;
                case "Pure":
                    blackAmount -= 1;
                    whiteAmount -= 1;
                case "Voracious":
                    parallelAmount += 2;
                    starryAmount += 1;
                case "Concussive":
                    blackAmount += 1;
                    whiteAmount += 1;
                default:
            }
            switch (PackagedAuraNode.tier(stack)) {
                case "Bright":
                    blackAmount -= 1;
                    whiteAmount -= 1;
                case "Withering":
                    parallelAmount -= 1;
                default:
            }
        }
        if (parallelAmount < 1) {
            parallelAmount = 1;
        }
        overallParallelAmount = parallelAmount;
        overallBlackAmount = blackAmount;
        overallWhiteAmount = whiteAmount;
        overallStarryAmount = starryAmount;
    }

    private boolean hasMansus(FluidHatchPartMachine fluids, Material material, int amount) {
        if (amount <= 0 || !material.hasFluid()) {
            return true;
        }
        FluidStack stack = material.getFluid(amount);
        return fluids.tank.drain(stack, IFluidHandler.FluidAction.SIMULATE).getAmount() >= amount;
    }

    private void drainMansus(FluidHatchPartMachine fluids, Material material, int amount) {
        if (amount <= 0 || !material.hasFluid()) {
            return;
        }
        fluids.tank.drain(material.getFluid(amount), IFluidHandler.FluidAction.EXECUTE);
    }

    private <T> T findPart(Class<T> type) {
        for (IMultiPart part : getParts()) {
            if (type.isInstance(part.self())) {
                return type.cast(part.self());
            }
        }
        return null;
    }

    private FluidHatchPartMachine findFluidHatch(PartAbility ability) {
        for (IMultiPart part : getParts()) {
            if (part.self() instanceof FluidHatchPartMachine hatch
                    && ability.isApplicable(hatch.getBlockState().getBlock())) {
                return hatch;
            }
        }
        return null;
    }

    public static BlockPattern createPattern(MultiblockMachineDefinition definition) {
        return NodeFusionReactorPatterns.create(definition);
    }

    // ////////////////////////////////////
    // ***** Persistence / UI *****//
    // ////////////////////////////////////

    @Override
    public void saveCustomPersistedData(CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        if (isFormed()) {
            storedInternalEnergy = internalEnergyContainer.getEnergyStored();
        }
        tag.putLong("NodeFusionHeat", heat);
        tag.putLong("NodeFusionStoredEnergy", storedInternalEnergy);
    }

    @Override
    public void loadCustomPersistedData(CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        heat = tag.getLong("NodeFusionHeat");
        storedInternalEnergy = tag.getLong("NodeFusionStoredEnergy");
        internalEnergyContainer.setEnergyStored(storedInternalEnergy);
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (isFormed()) {
            textList.add(Component.literal("Fusion Heat: " + heat + " EU"));
            textList.add(Component.literal("Fusion Startup Buffer: " + internalEnergyContainer.getEnergyStored()
                    + " / " + internalEnergyContainer.getEnergyCapacity() + " EU"));
        }
    }

    protected class NodeFusionReactorRecipeLogic extends MagicRecipeLogic {

        private boolean blockedByCleanVis;

        public NodeFusionReactorRecipeLogic(NodeFusionReactorMachine machine) {
            super(machine);
        }

        @Override
        protected ActionResult checkRecipe(GTRecipe recipe) {
            ActionResult result = super.checkRecipe(recipe);
            if (!result.isSuccess()) {
                return result;
            }
            if (recipe.recipeType == PORecipeMaps.NODE_MAGIC_FUSION_RECIPES
                    && !NodeFusionReactorMachine.this.isCleanVis()) {
                return ActionResult.FAIL_NO_REASON;
            }
            if (!chargeFusionStartup(recipe)) {
                return ActionResult.FAIL_NO_REASON;
            }
            return ActionResult.SUCCESS;
        }

        /**
         * Upstream fusion startup cost: a recipe may only start when its
         * {@code eu_to_start} fits the internal buffer capacity; the missing
         * {@code eu_to_start - heat} EU is then drained from the buffer and
         * added to the heat counter, blocking the start otherwise.
         */
        private boolean chargeFusionStartup(GTRecipe recipe) {
            if (!recipe.data.contains("eu_to_start")) {
                return true;
            }
            long euToStart = recipe.data.getLong("eu_to_start");
            if (euToStart <= 0L) {
                return true;
            }
            if (euToStart > internalEnergyContainer.getEnergyCapacity()) {
                return false;
            }
            long heatDiff = euToStart - heat;
            if (heatDiff <= 0L) {
                return true;
            }
            if (internalEnergyContainer.getEnergyStored() < heatDiff) {
                return false;
            }
            internalEnergyContainer.removeEnergy(heatDiff);
            heat += heatDiff;
            return true;
        }

        @Override
        public ActionResult handleTickRecipe(GTRecipe recipe) {
            ActionResult result = super.handleTickRecipe(recipe);
            if (!result.isSuccess()) {
                blockedByCleanVis = false;
                return result;
            }
            if (!NodeFusionReactorMachine.this.isCleanVis()) {
                blockedByCleanVis = true;
                return ActionResult.FAIL_NO_REASON;
            }
            blockedByCleanVis = false;
            return ActionResult.SUCCESS;
        }

        @Override
        protected void regressRecipe() {
            if (blockedByCleanVis) {
                return;
            }
            super.regressRecipe();
        }
    }
}
