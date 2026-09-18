package meowmel.pollution.common.machine.single;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.TieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutputFluid;
import com.gregtechceu.gtceu.api.machine.feature.IDropSaveMachine;
import com.gregtechceu.gtceu.api.machine.feature.IInteractedMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.annotation.RequireRerender;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import dev.tc4port.thaumcraft.api.aspect.AspectAmounts;
import dev.tc4port.thaumcraft.api.aspect.AspectApi;
import dev.tc4port.thaumcraft.api.aspect.AspectContainerView;
import dev.tc4port.thaumcraft.api.aspect.AspectId;
import dev.tc4port.thaumcraft.api.essentia.EssentiaContainerApi;
import dev.tc4port.thaumcraft.api.essentia.EssentiaSource;
import dev.tc4port.thaumcraft.api.essentia.EssentiaTransferMode;
import dev.tc4port.thaumcraft.api.essentia.EssentiaTransport;
import meowmel.pollution.common.lib.GTEssentiaHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Aspect tank: tiered single-block essentia storage (port of upstream
 * {@code MetaTileEntityAspectTank}, 1.12.2).
 *
 * <p>Storage semantics are kept: one aspect + amount, capacity
 * {@code 10_000 << (tier - 1)} (10 000 at LV doubling to 2 560 000 at UHV), an
 * optional aspect filter ("lock"), a voiding mode, a configurable output face
 * and a 1 essentia/tick auto-output. Registered for tiers LV..UHV as
 * {@code lv_aspect_tank} .. {@code uhv_aspect_tank}.</p>
 *
 * <p>The 1.12 {@code IAspectSource} container contract maps onto three TC4R
 * interfaces, all implemented here and exposed to TC4R through
 * {@link AspectTankBlockEntity}:</p>
 * <ul>
 *   <li>{@link EssentiaTransport} - directional tube contract
 *       ({@code isConnectable/canInputFrom/canOutputTo/suctionType/suctionAmount/
 *       takeEssentia/addEssentia/essentiaType/essentiaAmount/minimumSuction}).
 *       The connectable face is the machine's front face (upstream hard-coded
 *       {@code EnumFacing.UP}; the port uses the rotatable front so the port's
 *       rotation and overlay conventions apply).</li>
 *   <li>{@link EssentiaSource} - face-less extraction used by mirrors and
 *       {@code EssentiaApi.extract}.</li>
 *   <li>{@link AspectContainerView} - display view; the filter is reported as a
 *       visible filter.</li>
 * </ul>
 *
 * <p>Container items in the input slot are handled through
 * {@link EssentiaContainerApi} (jars and phials): a filled container is drained
 * into the tank and the emptied stack is moved to the output slot, an empty
 * container is filled from the tank and moved to the output slot.</p>
 *
 * <p>Deviations / not ported:</p>
 * <ul>
 *   <li>No ModularUI screen (port convention for single machines). The upstream
 *       GUI toggles are mapped to tool actions: soft mallet toggles auto-output,
 *       sneak + soft mallet toggles voiding, wrench (non-sneak) sets/clears the
 *       output face, screwdriver on the output face toggles "input from output
 *       side", right-click with a labelled/filled essentia container sets the
 *       aspect filter and shift-right-click clears it. The fancy tooltip shows
 *       the stored aspect, amount, lock, auto-output and voiding state.</li>
 *   <li>Suction constants are upstream's: 32 base / 64 with a filter while the
 *       tank holds less than 250 essentia, 0 above that;
 *       {@code minimumSuction} is 32/64.</li>
 *   <li>Transfers are amount-based and may be partial, as TC4R requires:
 *       {@code takeEssentia}/{@code extractEssentia} return as much as is
 *       available up to the request, while upstream
 *       {@code takeFromContainer(aspect, i)} was all-or-nothing at exactly
 *       {@code i}. {@code addEssentia} returns the inserted amount (the
 *       complement of upstream's returned remainder).</li>
 *   <li>"Tank in tank" item stacking (upstream {@code writeItemStackData} into
 *       another tank item) is not ported: the item round-trip itself works
 *       through {@link IDropSaveMachine} (breaking the block stores aspect,
 *       filter and voiding into the dropped stack, placement restores them),
 *       but filling an aspect-tank item from an aspect tank is left as a TODO
 *       because GTCEu machine items have no generic block-entity-data API.</li>
 *   <li>{@code setSuction} does not exist in TC4R; tubes compute suction from
 *       the source's own values, so upstream's no-op setter is dropped.</li>
 *   <li>The 1.12 radius FX packet is replaced by TC4R's own essentia FX. The
 *       native search ({@link GTEssentiaHandler#pullEssentiaFromNearby}) is used
 *       as the auto-output fallback when no transport accepts on the output
 *       face, mirroring the upstream helper's otherwise unused radius scan.</li>
 * </ul>
 */
public class AspectTankMachine extends TieredMachine
        implements EssentiaTransport, EssentiaSource, AspectContainerView,
        IAutoOutputFluid, IInteractedMachine, IDropSaveMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            AspectTankMachine.class, MetaMachine.MANAGED_FIELD_HOLDER);

    /** Upstream capacity of the LV tank; doubles per tier. */
    public static final int BASE_CAPACITY = 10_000;

    /** Upstream suction threshold: below this amount the tank still sucks. */
    private static final int SUCTION_AMOUNT_THRESHOLD = 250;
    private static final int SUCTION_WITHOUT_FILTER = 32;
    private static final int SUCTION_WITH_FILTER = 64;

    /** Radius of the native essentia search used by the auto-output fallback. */
    private static final int SEARCH_RANGE = GTEssentiaHandler.DEFAULT_SEARCH_RANGE;

    private final int maxCapacity;

    private final NotifiableItemStackHandler importItems;
    private final NotifiableItemStackHandler exportItems;

    @Persisted
    @DescSynced
    @RequireRerender
    private String aspectTag = "";

    @Persisted
    @DescSynced
    private int amount;

    @Persisted
    @DescSynced
    @RequireRerender
    private String filterTag = "";

    @Persisted
    @DescSynced
    private boolean voiding;

    @Persisted
    @DescSynced
    private boolean autoOutput;

    @Persisted
    @DescSynced
    @RequireRerender
    @Nullable
    private Direction outputFacing;

    @Persisted
    @DescSynced
    private boolean allowInputFromOutputSide;

    private TickableSubscription tickSubscription;

    public AspectTankMachine(IMachineBlockEntity holder, int tier) {
        super(holder, tier);
        this.maxCapacity = capacityForTier(tier);
        this.importItems = new NotifiableItemStackHandler(this, 1, IO.IN, IO.IN);
        this.exportItems = new NotifiableItemStackHandler(this, 1, IO.OUT, IO.OUT);
    }

    /** Upstream capacities: {@code 10_000 << (tier - 1)}. */
    public static int capacityForTier(int tier) {
        return BASE_CAPACITY << (tier - 1);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    public int getMaxAspectCapacity() {
        return maxCapacity;
    }

    public int getAspectAmount() {
        return amount;
    }

    @Nullable
    public AspectId getStoredAspect() {
        return parseAspect(aspectTag);
    }

    @Nullable
    public AspectId getAspectFilter() {
        return parseAspect(filterTag);
    }

    public boolean isVoiding() {
        return voiding;
    }

    public void setVoiding(boolean voiding) {
        if (this.voiding != voiding) {
            this.voiding = voiding;
            markDirty();
        }
    }

    public void setAspectFilter(@Nullable AspectId filter) {
        this.filterTag = filter == null ? "" : filter.serialized();
        markDirty();
    }

    // ////////////////////////////////////
    // ***** lifecycle / tick *****//
    // ////////////////////////////////////

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            tickSubscription = subscribeServerTick(this::tick);
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

    /**
     * Upstream {@code update()} order: fill from the input container, drain the
     * tank into the input container, then auto-output.
     */
    private void tick() {
        Level level = getLevel();
        if (level == null || level.isClientSide) {
            return;
        }
        fillTankFromContainer(level);
        fillContainerFromTank(level);
        if (autoOutput) {
            if (!pushOutput(level)) {
                pullNearby(level);
            }
        }
    }

    // ////////////////////////////////////
    // ***** container item interaction *****//
    // ////////////////////////////////////

    /**
     * Upstream {@code fillInternalTankFromAspectContainer}: drains the first
     * aspect of a filled jar/phial in the input slot into the tank. Like
     * upstream, the whole content must fit and the emptied container must fit
     * into the output slot, otherwise nothing happens.
     */
    private void fillTankFromContainer(Level level) {
        ItemStack input = importItems.getStackInSlot(0);
        if (input.isEmpty()) {
            return;
        }
        AspectAmounts contents = EssentiaContainerApi.contents(input);
        if (contents.amounts().isEmpty()) {
            return;
        }
        Iterator<Map.Entry<AspectId, Integer>> iterator = contents.amounts().entrySet().iterator();
        Map.Entry<AspectId, Integer> entry = iterator.next();
        AspectId aspect = entry.getKey();
        int itemAmount = entry.getValue() == null ? 0 : entry.getValue();
        if (itemAmount <= 0 || !doesContainerAccept(aspect)) {
            return;
        }
        if (!canStoreAll(aspect, itemAmount) || !canInsertIntoExport(input)) {
            return;
        }
        ItemStack emptied = input.copy();
        int simulated = EssentiaContainerApi.extract(level, emptied, aspect, itemAmount,
                EssentiaTransferMode.SIMULATE);
        if (simulated < itemAmount) {
            return;
        }
        EssentiaContainerApi.extract(level, emptied, aspect, itemAmount, EssentiaTransferMode.EXECUTE);
        storeInternal(aspect, itemAmount);
        importItems.setStackInSlot(0, ItemStack.EMPTY);
        insertIntoExport(emptied);
    }

    /**
     * Upstream {@code takeInternalTankToAspectContainer}: fills an empty
     * jar/phial in the input slot from the tank. Upstream used the fixed jar
     * (250) / phial (10) sizes; the port uses the container's own capacity from
     * {@link EssentiaContainerApi} (TC4R phials hold 8).
     */
    private void fillContainerFromTank(Level level) {
        if (amount <= 0) {
            return;
        }
        AspectId stored = getStoredAspect();
        if (stored == null) {
            return;
        }
        ItemStack input = importItems.getStackInSlot(0);
        if (input.isEmpty()) {
            return;
        }
        int capacity = EssentiaContainerApi.capacity(input);
        if (capacity <= 0 || !EssentiaContainerApi.contents(input).amounts().isEmpty()) {
            return;
        }
        if (!canInsertIntoExport(input)) {
            return;
        }
        int want = Math.min(amount, capacity);
        ItemStack filled = input.copy();
        int inserted = EssentiaContainerApi.insert(level, filled, stored, want, EssentiaTransferMode.SIMULATE);
        if (inserted <= 0) {
            return;
        }
        EssentiaContainerApi.insert(level, filled, stored, inserted, EssentiaTransferMode.EXECUTE);
        amount -= inserted;
        if (amount <= 0) {
            amount = 0;
            aspectTag = "";
        }
        importItems.setStackInSlot(0, ItemStack.EMPTY);
        insertIntoExport(filled);
        markDirty();
    }

    private boolean canInsertIntoExport(ItemStack stack) {
        return exportItems.insertItemInternal(0, stack.copy(), true).isEmpty();
    }

    private void insertIntoExport(ItemStack stack) {
        exportItems.insertItemInternal(0, stack, false);
    }

    // ////////////////////////////////////
    // ***** auto output *****//
    // ////////////////////////////////////

    /**
     * Upstream {@code pushAspectIntoNearbyHandlers}: pushes one essentia into
     * the output facing. Returns true when something was pushed.
     */
    private boolean pushOutput(Level level) {
        AspectId stored = getStoredAspect();
        if (stored == null || amount <= 0) {
            return false;
        }
        Direction facing = getOutputFacingFluids();
        int moved = GTEssentiaHandler.addEssentiaToTile(level, getPos(), stored, facing, 1,
                EssentiaTransferMode.EXECUTE);
        if (moved <= 0) {
            moved = GTEssentiaHandler.addEssentiaToMachine(level, getPos(), stored, facing, 1,
                    EssentiaTransferMode.EXECUTE);
        }
        if (moved <= 0) {
            return false;
        }
        removeInternal(moved);
        return true;
    }

    /**
     * Native replacement for upstream {@code GTEssentiaHandler.addEssentia}
     * (its radius scan was never called by the tank upstream). Used when the
     * output face cannot accept essentia: the tank draws one essentia from the
     * nearest source in range, using its own aspect or, while empty, its lock.
     */
    private void pullNearby(Level level) {
        AspectId target = getStoredAspect();
        if (target == null) {
            target = getAspectFilter();
        }
        if (target == null || amount >= maxCapacity) {
            return;
        }
        int pulled = GTEssentiaHandler.pullEssentiaFromNearby(level, getPos(), target, 1, SEARCH_RANGE,
                EssentiaTransferMode.EXECUTE);
        if (pulled > 0) {
            storeInternal(target, pulled);
        }
    }

    // ////////////////////////////////////
    // ***** internal storage *****//
    // ////////////////////////////////////

    private boolean canStoreAll(AspectId aspect, int added) {
        AspectId stored = getStoredAspect();
        if (stored == null) {
            return added <= maxCapacity;
        }
        return stored.equals(aspect) && amount + added <= maxCapacity;
    }

    private void storeInternal(AspectId aspect, int added) {
        if (added <= 0) {
            return;
        }
        if (getStoredAspect() == null) {
            aspectTag = aspect.serialized();
        }
        amount += added;
        markDirty();
    }

    private void removeInternal(int removed) {
        if (removed <= 0) {
            return;
        }
        amount -= removed;
        if (amount <= 0) {
            amount = 0;
            aspectTag = "";
        }
        markDirty();
    }

    // ////////////////////////////////////
    // ***** IAspectSource split: EssentiaTransport *****//
    // ////////////////////////////////////

    @Override
    public boolean isConnectable(Direction side) {
        return side == getFrontFacing();
    }

    @Override
    public boolean canInputFrom(Direction side) {
        return side == getFrontFacing();
    }

    @Override
    public boolean canOutputTo(Direction side) {
        return side == getFrontFacing();
    }

    @Nullable
    @Override
    public AspectId suctionType(Direction side) {
        AspectId filter = getAspectFilter();
        return filter != null ? filter : getStoredAspect();
    }

    @Override
    public int suctionAmount(Direction side) {
        if (amount >= SUCTION_AMOUNT_THRESHOLD) {
            return 0;
        }
        return getAspectFilter() != null ? SUCTION_WITH_FILTER : SUCTION_WITHOUT_FILTER;
    }

    @Override
    public int takeEssentia(AspectId aspect, int amount, Direction side, EssentiaTransferMode mode) {
        if (!canOutputTo(side) || !matchesStored(aspect)) {
            return 0;
        }
        int taken = Math.min(amount, this.amount);
        if (taken <= 0) {
            return 0;
        }
        if (mode.executes()) {
            removeInternal(taken);
        }
        return taken;
    }

    @Override
    public int addEssentia(AspectId aspect, int amount, Direction side, EssentiaTransferMode mode) {
        if (!canInputFrom(side) || amount <= 0 || !doesContainerAccept(aspect)) {
            return 0;
        }
        AspectId stored = getStoredAspect();
        int space = stored == null || stored.equals(aspect) ? maxCapacity - this.amount : 0;
        if (space <= 0) {
            // Upstream voiding: a full tank with a matching aspect swallows the input.
            if (voiding && stored != null && stored.equals(aspect)) {
                return amount;
            }
            return 0;
        }
        int added = Math.min(amount, space);
        if (mode.executes()) {
            storeInternal(aspect, added);
        }
        return added;
    }

    @Nullable
    @Override
    public AspectId essentiaType(Direction side) {
        return getStoredAspect();
    }

    @Override
    public int essentiaAmount(Direction side) {
        return amount;
    }

    @Nullable
    @Override
    public AspectId extractableAspect(Direction side) {
        return canOutputTo(side) ? getStoredAspect() : null;
    }

    @Override
    public int minimumSuction() {
        return getAspectFilter() != null ? SUCTION_WITH_FILTER : SUCTION_WITHOUT_FILTER;
    }

    @Override
    public boolean renderExtendedTube() {
        return false;
    }

    // ////////////////////////////////////
    // ***** IAspectSource split: EssentiaSource *****//
    // ////////////////////////////////////

    @Override
    public int extractEssentia(AspectId aspect, int amount, EssentiaTransferMode mode) {
        if (!matchesStored(aspect) || amount <= 0) {
            return 0;
        }
        int taken = Math.min(amount, this.amount);
        if (taken > 0 && mode.executes()) {
            removeInternal(taken);
        }
        return taken;
    }

    // ////////////////////////////////////
    // ***** IAspectSource split: AspectContainerView *****//
    // ////////////////////////////////////

    @Override
    public AspectAmounts visibleAspects() {
        AspectId stored = getStoredAspect();
        if (stored == null || amount <= 0) {
            return AspectAmounts.EMPTY;
        }
        return AspectAmounts.of(stored, amount);
    }

    @Override
    public Set<AspectId> visibleAspectFilters() {
        AspectId filter = getAspectFilter();
        return filter == null ? Set.of() : Set.of(filter);
    }

    // ////////////////////////////////////
    // ***** IAutoOutputFluid (aspect output) *****//
    // ////////////////////////////////////

    @Override
    public boolean isAutoOutputFluids() {
        return autoOutput;
    }

    @Override
    public void setAutoOutputFluids(boolean autoOutput) {
        if (this.autoOutput != autoOutput) {
            this.autoOutput = autoOutput;
            markDirty();
        }
    }

    @Override
    public boolean isAllowInputFromOutputSideFluids() {
        return allowInputFromOutputSide;
    }

    @Override
    public void setAllowInputFromOutputSideFluids(boolean allow) {
        this.allowInputFromOutputSide = allow;
        markDirty();
    }

    /** Upstream: the output face, defaulting to the back of the machine. */
    @Override
    public Direction getOutputFacingFluids() {
        return outputFacing == null ? getFrontFacing().getOpposite() : outputFacing;
    }

    @Override
    public void setOutputFacingFluids(@Nullable Direction facing) {
        this.outputFacing = facing;
        markDirty();
    }

    @Override
    public boolean isFacingValid(Direction facing) {
        return facing != getOutputFacingFluids() && super.isFacingValid(facing);
    }

    // ////////////////////////////////////
    // ***** interaction *****//
    // ////////////////////////////////////

    @Override
    protected InteractionResult onWrenchClick(Player player, InteractionHand hand, Direction gridSide,
                                              BlockHitResult hitResult) {
        if (!player.isShiftKeyDown()) {
            if (!isRemote() && gridSide != getFrontFacing()) {
                if (gridSide == getOutputFacingFluids()) {
                    setOutputFacingFluids(null);
                } else {
                    setOutputFacingFluids(gridSide);
                }
            }
            return InteractionResult.sidedSuccess(isRemote());
        }
        return super.onWrenchClick(player, hand, gridSide, hitResult);
    }

    /**
     * GTCEu tank parity: toggles the "input from output side" flag on the output
     * face. Like upstream, {@link #canInputFrom} only accepts the connectable
     * face, so this flag is stored for the item round-trip only.
     */
    @Override
    protected InteractionResult onScrewdriverClick(Player player, InteractionHand hand, Direction gridSide,
                                                   BlockHitResult hitResult) {
        if (!isRemote() && gridSide == getOutputFacingFluids()) {
            setAllowInputFromOutputSideFluids(!isAllowInputFromOutputSideFluids());
            player.sendSystemMessage(Component.translatable(isAllowInputFromOutputSideFluids()
                    ? "gtceu.machine.basic.input_from_output_side.allow"
                    : "gtceu.machine.basic.input_from_output_side.disallow"));
            return InteractionResult.SUCCESS;
        }
        return super.onScrewdriverClick(player, hand, gridSide, hitResult);
    }

    /**
     * Upstream GUI buttons, mapped to the GTCEu soft mallet convention: soft
     * mallet toggles auto-output, sneak + soft mallet toggles voiding.
     */
    @Override
    protected InteractionResult onSoftMalletClick(Player player, InteractionHand hand, Direction gridSide,
                                                  BlockHitResult hitResult) {
        if (!isRemote()) {
            if (player.isShiftKeyDown()) {
                setVoiding(!voiding);
            } else {
                setAutoOutputFluids(!autoOutput);
            }
        }
        return InteractionResult.sidedSuccess(isRemote());
    }

    /**
     * Upstream GUI aspect picker, mapped to a UI-less interaction: right-click
     * with a labelled jar or a filled essentia container sets the lock to that
     * aspect, shift-right-click clears it.
     */
    @Override
    public InteractionResult onUse(BlockState state, Level level, BlockPos pos, Player player,
                                   InteractionHand hand, BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                setAspectFilter(null);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        ItemStack held = player.getItemInHand(hand);
        Optional<AspectId> label = EssentiaContainerApi.labelAspect(held);
        if (label.isPresent()) {
            if (!level.isClientSide) {
                setAspectFilter(label.get());
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        AspectAmounts contents = EssentiaContainerApi.contents(held);
        if (!contents.amounts().isEmpty()) {
            if (!level.isClientSide) {
                setAspectFilter(contents.amounts().keySet().iterator().next());
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    // ////////////////////////////////////
    // ***** item round-trip (IDropSaveMachine) *****//
    // ////////////////////////////////////

    /** Upstream always wrote aspect/filter/voiding into the dropped item. */
    @Override
    public boolean saveBreak() {
        return true;
    }

    /** Upstream creative pick-block also carried the tank data. */
    @Override
    public boolean savePickClone() {
        return true;
    }

    // ////////////////////////////////////
    // ***** tooltips *****//
    // ////////////////////////////////////

    @Override
    public void onAddFancyInformationTooltip(List<Component> tooltip) {
        super.onAddFancyInformationTooltip(tooltip);
        AspectId stored = getStoredAspect();
        if (stored != null && amount > 0) {
            tooltip.add(Component.translatable("pollution.machine.aspect_tank.tooltip.stored",
                    AspectApi.tooltipName(stored), amount));
        }
        AspectId filter = getAspectFilter();
        if (filter != null) {
            tooltip.add(Component.translatable("pollution.machine.aspect_tank.tooltip.locked",
                    AspectApi.tooltipName(filter)));
        }
        if (autoOutput) {
            tooltip.add(Component.translatable("pollution.machine.aspect_tank.tooltip.auto_output"));
        }
        if (voiding) {
            tooltip.add(Component.translatable("pollution.machine.aspect_tank.tooltip.voiding"));
        }
    }

    // ////////////////////////////////////
    // ***** helpers *****//
    // ////////////////////////////////////

    @Nullable
    private static AspectId parseAspect(String tag) {
        if (tag == null || tag.isEmpty()) {
            return null;
        }
        try {
            AspectId aspect = AspectId.parse(tag);
            return AspectApi.contains(aspect) ? aspect : null;
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private boolean matchesStored(@Nullable AspectId aspect) {
        AspectId stored = getStoredAspect();
        return stored != null && stored.equals(aspect);
    }

    /** Upstream {@code doesContainerAccept}: no filter accepts everything. */
    public boolean doesContainerAccept(AspectId aspect) {
        AspectId filter = getAspectFilter();
        return filter == null || filter.equals(aspect);
    }
}
