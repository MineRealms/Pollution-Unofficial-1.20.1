package meowmel.pollution.common.machine.single;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.TieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutputFluid;
import com.gregtechceu.gtceu.api.machine.feature.IDropSaveMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IInteractedMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.ProgressWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.DropSaved;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.annotation.RequireRerender;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import dev.tc4port.thaumcraft.api.ThaumcraftApiHelper;
import dev.tc4port.thaumcraft.api.aspect.AspectAmounts;
import dev.tc4port.thaumcraft.api.aspect.AspectApi;
import dev.tc4port.thaumcraft.api.aspect.AspectContainerView;
import dev.tc4port.thaumcraft.api.aspect.AspectId;
import dev.tc4port.thaumcraft.api.essentia.EssentiaApi;
import dev.tc4port.thaumcraft.api.essentia.EssentiaContainerApi;
import dev.tc4port.thaumcraft.api.essentia.EssentiaSource;
import dev.tc4port.thaumcraft.api.essentia.EssentiaTransferMode;
import dev.tc4port.thaumcraft.api.essentia.EssentiaTransport;
import meowmel.pollution.common.gui.MachineGuiWidgets;
import meowmel.pollution.common.lib.GTEssentiaHandler;
import meowmel.pollution.common.machine.PollutionMachines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
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
 *       The connectable faces are the machine's front face (upstream hard-coded
 *       {@code EnumFacing.UP}; the port uses the rotatable front so the port's
 *       rotation and overlay conventions apply) and the configurable output
 *       face, so tubes attached on either the input side or the rendered output
 *       overlay are discovered by TC4R.</li>
 *   <li>{@link EssentiaSource} - face-less extraction used by mirrors and
 *       {@code EssentiaApi.extract}.</li>
 *   <li>{@link AspectContainerView} - display view; the filter is reported as a
 *       visible filter.</li>
 * </ul>
 *
 * <p>Container items in the input slot are handled through
 * {@link EssentiaContainerApi} (jars and phials): a filled container is drained
 * into the tank and the emptied stack is moved to the output slot, an empty
 * container is filled from the tank and moved to the output slot. The upstream
 * "tank in tank" interaction is ported as well: an aspect tank machine item in
 * the input slot is drained into the block tank when it carries stored aspect,
 * or filled from the block tank up to its own tier capacity when empty. The
 * item state uses the machine's own {@code @DropSaved} persisted keys
 * ({@code aspectTag} / {@code amount}), so breaking and replacing a tank keeps
 * its contents.</p>
 *
 * <p>Deviations / not ported:</p>
 * <ul>
 *   <li>The screen is ported as a fancy GregTech UI (2026-09-19): the main page
 *       shows the stored aspect, amount/capacity, lock and auto-output/voiding
 *       state plus the in/out container slots, and the left configurator panel
 *       carries the auto-output, voiding and aspect-lock toggles. The upstream
 *       tool actions remain available: soft mallet toggles auto-output, sneak +
 *       soft mallet toggles voiding, wrench (non-sneak) sets/clears the output
 *       face, screwdriver on the output face toggles "input from output side",
 *       right-click with a labelled/filled essentia container sets the aspect
 *       filter and shift-right-click clears it. The fancy tooltip shows the
 *       stored aspect, amount, lock, auto-output and voiding state.</li>
 *   <li>Suction is re-derived for TC4R's pull rules, not kept from upstream,
 *       and selects the transfer direction. Upstream reported 32 base / 64 with
 *       a filter while the tank held less than 250 essentia and 0 above that,
 *       with {@code minimumSuction} 32/64; TC4R tubes only pull from a
 *       neighbour whose suction is strictly lower than their own and at least
 *       the neighbour's {@code minimumSuction}, so that contract made the tank
 *       look like a sink and essentia never left it. The port instead splits by
 *       mode: with auto-output <em>on</em> the tank is a source
 *       ({@code suctionAmount} {@link #SUCTION_IDLE} while it has room or is
 *       voiding, {@code minimumSuction} 0), so a tube fed by a warded jar
 *       (suction 32, tube 31) drains it and {@link #pushOutput(Level)} feeds
 *       the output face; with auto-output <em>off</em> it is a sink like the
 *       warded jar (32, or 64 with a lock) while it has room, so tubes
 *       propagate suction into it and {@link #fillTankFromTubes(Level)} pulls
 *       from them. {@code suctionType} stays {@code filter ?: stored}. See
 *       {@link #suctionAmount(Direction)} and {@link #minimumSuction()}.</li>
 *   <li>Transfers are amount-based and may be partial, as TC4R requires:
 *       {@code takeEssentia}/{@code extractEssentia} return as much as is
 *       available up to the request, while upstream
 *       {@code takeFromContainer(aspect, i)} was all-or-nothing at exactly
 *       {@code i}. {@code addEssentia} returns the inserted amount (the
 *       complement of upstream's returned remainder).</li>
 *   <li>"Tank in tank" item stacking is ported: the upstream
 *       {@code writeItemStackData} / {@code initFromItemStackData} round-trip
 *       maps onto GregTech's {@link IDropSaveMachine} plus {@code @DropSaved}
 *       on the aspect / amount / filter / voiding fields, and the upstream
 *       {@code checkItemIsMTE} container branches of
 *       {@code fillInternalTankFromAspectContainer} /
 *       {@code takeFromContainer(ItemStack, boolean)} are implemented as
 *       {@link #drainAspectTankItem(ItemStack)} and
 *       {@link #fillAspectTankItem(ItemStack, AspectId)}. Upstream limited the
 *       item to its own tier capacity and returned a fresh base stack; the port
 *       writes the item in place and preserves its remaining NBT (filter,
 *       voiding), so a carried filter survives the tank-to-tank transfer.</li>
 *   <li>{@code setSuction} does not exist in TC4R; tubes compute suction from
 *       the source's own values, so upstream's no-op setter is dropped.</li>
 *   <li>The 1.12 radius FX packet is replaced by TC4R's own essentia FX, and
 *       the upstream helper's otherwise unused radius scan is not wired to the
 *       tank: auto-output only pushes into the output face and input is handled
 *       by the jar-style tube drain, so the tank never pulls essentia from
 *       distant blocks.</li>
 * </ul>
 */
public class AspectTankMachine extends TieredMachine
        implements EssentiaTransport, EssentiaSource, AspectContainerView,
        IAutoOutputFluid, IInteractedMachine, IDropSaveMachine, IFancyUIMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            AspectTankMachine.class, MetaMachine.MANAGED_FIELD_HOLDER);

    /** Upstream capacity of the LV tank; doubles per tier. */
    public static final int BASE_CAPACITY = 10_000;

    /**
     * Source suction reported in output mode (auto-output on) while the tank
     * has room (or is voiding). TC4R tubes propagate suction as
     * {@code max(neighbour.suctionAmount) - 1} and only pull from a neighbour
     * whose suction is strictly lower than their own
     * ({@code EssentiaTubeBlockEntity.equalizeWithNeighbours}), so upstream's
     * 32/64 made the tank the highest-suction node in the network and the tube
     * next to it (suction 31) never pulled. This is the lowest positive value:
     * it still satisfies the golem deposit gate
     * ({@code GolemEssentiaGoal.accepts}: {@code suctionAmount(face) > 0}) while
     * a tube ignores it for propagation, because {@code calculateSuction} only
     * adopts a neighbour with {@code suctionAmount > this.suction + 1}.
     */
    private static final int SUCTION_IDLE = 1;

    /**
     * Sink suction reported in input mode (auto-output off) while the tank has
     * room, mirroring the warded jar: the adjacent tube adopts 31 from the
     * 32-suction tank and {@link #fillTankFromTubes(Level)} pulls from it,
     * because TC4R tubes never push. A locked tank raises it to
     * {@link #SUCTION_SINK_FILTERED} exactly like a labelled jar.
     */
    private static final int SUCTION_SINK = 32;
    private static final int SUCTION_SINK_FILTERED = 64;

    private final int maxCapacity;

    private final NotifiableItemStackHandler importItems;
    private final NotifiableItemStackHandler exportItems;

    /**
     * Persisted aspect state. {@code @DropSaved} is what makes the item
     * round-trip work: {@code IDropSaveMachine.saveToItem} writes exactly these
     * keys into the dropped stack and {@code loadFromItem} restores them on
     * placement. The constants below mirror those keys for the "tank in tank"
     * container interaction.
     */
    private static final String ITEM_TAG_ASPECT = "aspectTag";
    private static final String ITEM_TAG_AMOUNT = "amount";
    private static final String ITEM_TAG_FILTER = "filterTag";

    @Persisted
    @DescSynced
    @RequireRerender
    @DropSaved
    private String aspectTag = "";

    @Persisted
    @DescSynced
    @DropSaved
    private int amount;

    @Persisted
    @DescSynced
    @RequireRerender
    @DropSaved
    private String filterTag = "";

    @Persisted
    @DescSynced
    @DropSaved
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
     * tank into the input container, then either output (auto-output on) or
     * input (auto-output off, jar-style tube drain).
     */
    private void tick() {
        Level level = getLevel();
        if (level == null || level.isClientSide) {
            return;
        }
        fillTankFromContainer(level);
        fillContainerFromTank(level);
        if (autoOutput) {
            pushOutput(level);
        } else {
            fillTankFromTubes(level);
        }
    }

    // ////////////////////////////////////
    // ***** container item interaction *****//
    // ////////////////////////////////////

    /**
     * Upstream {@code fillInternalTankFromAspectContainer}: drains the first
     * aspect of a filled jar/phial in the input slot into the tank. Like
     * upstream, the whole content must fit and the emptied container must fit
     * into the output slot, otherwise nothing happens. A stacked input is
     * processed one item at a time, as upstream's {@code extractItem(i, 1)} did.
     *
     * <p>The port additionally accepts a filled aspect tank item (upstream's
     * "tank in tank" jar-in-jar branch), which carries its stored aspect
     * through the machine's drop-saved item data instead of the TC4R container
     * API.</p>
     */
    private void fillTankFromContainer(Level level) {
        ItemStack input = importItems.getStackInSlot(0);
        if (input.isEmpty()) {
            return;
        }
        if (isAspectTankItem(input)) {
            drainAspectTankItem(input);
            return;
        }
        ItemStack container = input.copyWithCount(1);
        AspectAmounts contents = EssentiaContainerApi.contents(container);
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
        if (!canStoreAll(aspect, itemAmount) || !canInsertIntoExport(container)) {
            return;
        }
        ItemStack emptied = container.copy();
        int simulated = EssentiaContainerApi.extract(level, emptied, aspect, itemAmount,
                EssentiaTransferMode.SIMULATE);
        if (simulated < itemAmount) {
            return;
        }
        EssentiaContainerApi.extract(level, emptied, aspect, itemAmount, EssentiaTransferMode.EXECUTE);
        storeInternal(aspect, itemAmount);
        extractInputItem(input);
        insertIntoExport(emptied);
    }

    /**
     * Upstream {@code fillInternalTankFromAspectContainer} MTE branch: drains
     * the aspect stored in an aspect tank item into the block tank. All of the
     * item's content must fit and the emptied item must fit into the output
     * slot, otherwise the interaction is skipped.
     */
    private void drainAspectTankItem(ItemStack input) {
        AspectId aspect = readItemAspect(input);
        int itemAmount = readItemAmount(input);
        if (aspect == null || itemAmount <= 0 || !doesContainerAccept(aspect) || !canStoreAll(aspect, itemAmount)) {
            return;
        }
        ItemStack emptied = input.copyWithCount(1);
        clearItemState(emptied);
        if (!canInsertIntoExport(emptied)) {
            return;
        }
        storeInternal(aspect, itemAmount);
        extractInputItem(input);
        insertIntoExport(emptied);
    }

    /**
     * Upstream {@code takeInternalTankToAspectContainer}: fills an empty
     * jar/phial in the input slot from the tank. Upstream used the fixed jar
     * (250) / phial (10) sizes; the port uses the container's own capacity from
     * {@link EssentiaContainerApi} (TC4R phials hold 8).
     *
     * <p>An empty aspect tank item is filled up to its own tier capacity
     * (upstream's "tank in tank" branch), so a bigger tank item can carry more
     * than a jar.</p>
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
        if (isAspectTankItem(input)) {
            fillAspectTankItem(input, stored);
            return;
        }
        ItemStack container = input.copyWithCount(1);
        int capacity = EssentiaContainerApi.capacity(container);
        if (capacity <= 0 || !EssentiaContainerApi.contents(container).amounts().isEmpty()) {
            return;
        }
        if (!canInsertIntoExport(container)) {
            return;
        }
        int want = Math.min(amount, capacity);
        ItemStack filled = container.copy();
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
        extractInputItem(input);
        insertIntoExport(filled);
        markDirty();
    }

    /**
     * Upstream {@code takeFromContainer(ItemStack, boolean)} MTE branch: fills
     * an aspect tank item from the block tank. The item is written with the
     * same managed persistent keys the machine itself uses on drop, so placing
     * the filled item restores its contents into the next tank block.
     */
    private void fillAspectTankItem(ItemStack input, AspectId stored) {
        int itemCapacity = aspectTankItemCapacity(input);
        if (itemCapacity <= 0 || readItemAmount(input) > 0) {
            return;
        }
        AspectId itemFilter = readItemFilter(input);
        if (itemFilter != null && !itemFilter.equals(stored)) {
            return;
        }
        ItemStack filled = input.copyWithCount(1);
        if (!canInsertIntoExport(filled)) {
            return;
        }
        int inserted = Math.min(amount, itemCapacity);
        if (inserted <= 0) {
            return;
        }
        writeItemState(filled, stored, inserted);
        amount -= inserted;
        if (amount <= 0) {
            amount = 0;
            aspectTag = "";
        }
        extractInputItem(input);
        insertIntoExport(filled);
        markDirty();
    }

    /** Consumes exactly one item from the input slot, leaving any stack remainder. */
    private void extractInputItem(ItemStack input) {
        if (input.getCount() <= 1) {
            importItems.setStackInSlot(0, ItemStack.EMPTY);
        } else {
            ItemStack remainder = input.copy();
            remainder.shrink(1);
            importItems.setStackInSlot(0, remainder);
        }
    }

    private boolean canInsertIntoExport(ItemStack stack) {
        return exportItems.insertItemInternal(0, stack.copy(), true).isEmpty();
    }

    private void insertIntoExport(ItemStack stack) {
        exportItems.insertItemInternal(0, stack, false);
    }

    // ////////////////////////////////////
    // ***** aspect tank item ("tank in tank") *****//
    // ////////////////////////////////////

    /** True for the machine item of any registered aspect tank tier. */
    private static boolean isAspectTankItem(ItemStack stack) {
        if (!(stack.getItem() instanceof MetaMachineItem machineItem)) {
            return false;
        }
        MachineDefinition definition = machineItem.getDefinition();
        MachineDefinition[] tanks = PollutionMachines.ASPECT_TANK;
        if (definition == null || tanks == null) {
            return false;
        }
        for (MachineDefinition tank : tanks) {
            if (tank == definition) {
                return true;
            }
        }
        return false;
    }

    /** Storage capacity of the aspect tank item, taken from its definition tier. */
    private static int aspectTankItemCapacity(ItemStack stack) {
        if (!isAspectTankItem(stack) || !(stack.getItem() instanceof MetaMachineItem machineItem)) {
            return 0;
        }
        MachineDefinition definition = machineItem.getDefinition();
        MachineDefinition[] tanks = PollutionMachines.ASPECT_TANK;
        if (definition == null || tanks == null) {
            return 0;
        }
        int tier = definition.getTier();
        return tier >= 1 && tier < tanks.length ? capacityForTier(tier) : 0;
    }

    @Nullable
    private static AspectId readItemAspect(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? null : parseAspect(tag.getString(ITEM_TAG_ASPECT));
    }

    /** Item filter ("lock"), mirroring the block tank's own filter semantics. */
    @Nullable
    private static AspectId readItemFilter(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? null : parseAspect(tag.getString(ITEM_TAG_FILTER));
    }

    private static int readItemAmount(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return 0;
        }
        int capacity = aspectTankItemCapacity(stack);
        int stored = tag.getInt(ITEM_TAG_AMOUNT);
        if (stored <= 0) {
            return 0;
        }
        return capacity > 0 ? Math.min(stored, capacity) : stored;
    }

    private static void writeItemState(ItemStack stack, AspectId aspect, int itemAmount) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(ITEM_TAG_ASPECT, aspect.serialized());
        tag.putInt(ITEM_TAG_AMOUNT, itemAmount);
    }

    private static void clearItemState(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return;
        }
        tag.remove(ITEM_TAG_ASPECT);
        tag.remove(ITEM_TAG_AMOUNT);
        if (tag.isEmpty()) {
            stack.setTag(null);
        }
    }

    // ////////////////////////////////////
    // ***** auto output *****//
    // ////////////////////////////////////

    /**
     * Upstream {@code pushAspectIntoNearbyHandlers}: pushes one essentia into
     * the transport on the output facing, which is resolved with the
     * neighbour's own face toward the tank. Returns true when something was
     * pushed.
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
     * Input mode (auto-output off): TC4R tubes never push essentia, so the tank
     * pulls like the warded jar's {@code fillJar}. For every connectable face
     * the adjacent transport is asked for one unit while the tank's sink
     * suction is higher than the tube's own suction and meets the tube's
     * minimum. A voiding tank keeps pulling when full and discards the unit,
     * mirroring the void jar's overflow deletion.
     */
    private void fillTankFromTubes(Level level) {
        if (amount >= maxCapacity && !voiding) {
            return;
        }
        for (Direction side : Direction.values()) {
            if (!isConnectable(side)) {
                continue;
            }
            EssentiaTransport tube = ThaumcraftApiHelper.getConnectableTransport(level, getPos(), side);
            if (tube == null) {
                continue;
            }
            Direction tubeFace = side.getOpposite();
            if (!tube.canOutputTo(tubeFace)) {
                continue;
            }
            int tankSuction = suctionAmount(side);
            int tubeSuction = tube.suctionAmount(tubeFace);
            if (tubeSuction >= tankSuction || tankSuction < tube.minimumSuction()) {
                continue;
            }
            AspectId stored = getStoredAspect();
            AspectId selected = getAspectFilter();
            if (selected == null) {
                if (stored != null && amount > 0) {
                    selected = stored;
                } else if (tube.essentiaAmount(tubeFace) > 0) {
                    selected = tube.essentiaType(tubeFace);
                }
            }
            if (selected == null || !doesContainerAccept(selected)) {
                continue;
            }
            if (stored != null && !stored.equals(selected)) {
                continue;
            }
            int taken = EssentiaApi.take(level, tube, selected, 1, tubeFace, EssentiaTransferMode.EXECUTE);
            if (taken <= 0) {
                continue;
            }
            if (amount < maxCapacity) {
                storeInternal(selected, taken);
            }
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
        return side == getFrontFacing() || side == getOutputFacingFluids();
    }

    @Override
    public boolean canInputFrom(Direction side) {
        return isConnectable(side);
    }

    @Override
    public boolean canOutputTo(Direction side) {
        return isConnectable(side);
    }

    @Nullable
    @Override
    public AspectId suctionType(Direction side) {
        AspectId filter = getAspectFilter();
        return filter != null ? filter : getStoredAspect();
    }

    /**
     * Face- and mode-dependent suction. Output mode (auto-output on) reports
     * {@link #SUCTION_IDLE} so tubes can drain the tank and
     * {@link #pushOutput(Level)} can feed the output face; input mode
     * (auto-output off) reports the jar-like sink suction (32, or 64 with a
     * lock) while the tank has room, so tubes propagate suction into it and
     * {@link #fillTankFromTubes(Level)} can pull. A voiding tank keeps its
     * suction when full, mirroring the TC4R void jar, so golems can still feed
     * it.
     */
    @Override
    public int suctionAmount(Direction side) {
        if (!isConnectable(side)) {
            return 0;
        }
        if (autoOutput) {
            return voiding || amount < maxCapacity ? SUCTION_IDLE : 0;
        }
        if (voiding) {
            return getAspectFilter() != null ? SUCTION_SINK_FILTERED : SUCTION_SINK;
        }
        return amount < maxCapacity
                ? (getAspectFilter() != null ? SUCTION_SINK_FILTERED : SUCTION_SINK)
                : 0;
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

    /**
     * TC4R pullers reject a source whose minimum suction is above their own
     * suction; a tube fed by a plain warded jar only reaches 31 (jar 32 - 1),
     * so upstream's 32/64 floor made the tank undrainable. Zero mirrors the
     * arcane alembic: in output mode any puller with positive suction may drain
     * the tank, while in input mode the sink suction (32/64) keeps the tank
     * above the adjacent tube's 31/63 anyway.
     */
    @Override
    public int minimumSuction() {
        return 0;
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
     * Upstream GUI aspect picker, additionally available as a direct
     * interaction (the screen lock button cycles the same filter): right-click
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
    // ***** UI *****//
    // ////////////////////////////////////

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(198, 208, this, entityPlayer).widget(new FancyMachineUIWidget(this, 198, 208));
    }

    @Override
    public Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 190, 108);
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        group.addWidget(new ImageWidget(4, 4, 138, 100, GuiTextures.DISPLAY));
        group.addWidget(new LabelWidget(8, 8, self().getBlockState().getBlock().getDescriptionId()));
        group.addWidget(new LabelWidget(8, 20, this::storedAspectLabel).setTextColor(-1));
        group.addWidget(new LabelWidget(8, 30, this::amountLabel).setTextColor(-1));
        group.addWidget(new LabelWidget(8, 40, this::lockLabel).setTextColor(-1));
        group.addWidget(new LabelWidget(8, 50, this::stateLabel).setTextColor(-1));
        group.addWidget(new ProgressWidget(this::getFillFraction, 8, 64, 130, 12,
                GuiTextures.PROGRESS_BAR_ARROW));
        group.addWidget(MachineGuiWidgets.itemSlot(importItems, 0, 8, 82));
        group.addWidget(MachineGuiWidgets.itemSlot(exportItems, 0, 30, 82));
        return group;
    }

    @Override
    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        IFancyUIMachine.super.attachConfigurators(configuratorPanel);
        configuratorPanel.attachConfigurators(new IFancyConfiguratorButton.Toggle(
                GuiTextures.IO_CONFIG_FLUID_MODES_BUTTON, GuiTextures.BUTTON_FLUID_OUTPUT,
                this::isAutoOutputFluids,
                (clickData, pressed) -> {
                    if (!isRemote()) {
                        setAutoOutputFluids(pressed);
                    }
                }).setTooltipsSupplier(pressed -> List.of(Component.translatable(
                        "gtceu.gui.fluid_auto_output.tooltip." + (pressed ? "enabled" : "disabled")))));
        configuratorPanel.attachConfigurators(new IFancyConfiguratorButton.Toggle(
                GuiTextures.BUTTON_VOID, GuiTextures.BUTTON_VOID_MULTIBLOCK,
                this::isVoiding,
                (clickData, pressed) -> {
                    if (!isRemote()) {
                        setVoiding(pressed);
                    }
                }).setTooltipsSupplier(pressed -> List.of(
                        Component.translatable("pollution.machine.aspect_tank.tooltip.voiding"))));
        configuratorPanel.attachConfigurators(new IFancyConfiguratorButton.Toggle(
                GuiTextures.BUTTON_LOCK, GuiTextures.LOCK_WHITE,
                () -> getAspectFilter() != null,
                (clickData, pressed) -> {
                    if (!isRemote()) {
                        cycleAspectFilter();
                    }
                }).setTooltipsSupplier(pressed -> {
                    AspectId filter = getAspectFilter();
                    if (filter == null) {
                        return List.of(Component.literal("Aspect Lock: none (click to cycle)"));
                    }
                    return List.of(
                            Component.translatable("pollution.machine.aspect_tank.tooltip.locked",
                                    AspectApi.tooltipName(filter)),
                            Component.literal("Click to cycle the locked aspect"));
                }));
    }

    private double getFillFraction() {
        return maxCapacity <= 0 ? 0.0D : (double) amount / (double) maxCapacity;
    }

    private String storedAspectLabel() {
        AspectId stored = getStoredAspect();
        return stored == null ? "Aspect: -" : "Aspect: " + AspectApi.tooltipName(stored).getString();
    }

    private String amountLabel() {
        return "Amount: " + amount + " / " + maxCapacity;
    }

    private String lockLabel() {
        AspectId filter = getAspectFilter();
        return filter == null ? "Lock: none" : "Lock: " + AspectApi.tooltipName(filter).getString();
    }

    private String stateLabel() {
        return "Auto-Output: " + (autoOutput ? "On" : "Off") + " | Voiding: " + (voiding ? "On" : "Off");
    }

    /** Cycles the aspect lock through the registered aspects (null clears it). */
    private void cycleAspectFilter() {
        List<AspectId> aspects = new ArrayList<>(AspectApi.definitions().keySet());
        Collections.sort(aspects);
        AspectId current = getAspectFilter();
        int index = current == null ? 0 : aspects.indexOf(current) + 1;
        setAspectFilter(index < 0 || index >= aspects.size() ? null : aspects.get(index));
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
