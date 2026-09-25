package meowmel.pollution.common.machine.part;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.TankWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fluids.FluidStack;

/**
 * Infused fluid hatch: the fluid buffer of magic multiblocks.
 *
 * <p>Upstream ({@code MetaTileEntityInfusedFluidHatch}) held a single tank of
 * {@code 8000 * 2^tier} mB plus one canister slot in / one out. Containers move through separate in/out slots; transfer is simulated before
 * touching the fluid or item inventory.</p>
 */
public class InfusedFluidHatchMachine extends TieredPartMachine implements com.gregtechceu.gtceu.api.machine.feature.IMachineLife {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            InfusedFluidHatchMachine.class, MultiblockPartMachine.MANAGED_FIELD_HOLDER);

    public static final int INITIAL_TANK_CAPACITY = 8000;

    @Persisted
    public final NotifiableFluidTank tank;
    @com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
    public final com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler containerInput;
    @com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
    public final com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler containerOutput;
    private com.gregtechceu.gtceu.api.machine.TickableSubscription containerSubscription;

    public InfusedFluidHatchMachine(IMachineBlockEntity holder, int tier) {
        super(holder, tier);
        this.tank = new NotifiableFluidTank(this, 1, getTankCapacity(tier), IO.BOTH, IO.BOTH);
        containerInput = new com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler(this, 1, IO.NONE, IO.IN);
        containerOutput = new com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler(this, 1, IO.NONE, IO.OUT);
    }

    @Override public void onLoad() {
        super.onLoad();
        if (!isRemote()) containerSubscription = subscribeServerTick(this::transferContainer);
    }

    @Override public void onUnload() {
        if (containerSubscription != null) { containerSubscription.unsubscribe(); containerSubscription = null; }
        super.onUnload();
    }

    @Override public void onMachineRemoved() {
        clearInventory(containerInput.storage);
        clearInventory(containerOutput.storage);
    }

    private void transferContainer() {
        if (getOffsetTimer() % 5 != 0 || containerInput.getStackInSlot(0).isEmpty()) return;
        var stack = containerInput.getStackInSlot(0).copyWithCount(1);
        var result = net.minecraftforge.fluids.FluidUtil.tryEmptyContainer(stack, tank, Integer.MAX_VALUE, null, false);
        boolean emptying = result.isSuccess();
        if (!emptying) result = net.minecraftforge.fluids.FluidUtil.tryFillContainer(stack, tank, Integer.MAX_VALUE, null, false);
        if (!result.isSuccess() || !containerOutput.storage.insertItem(0, result.getResult(), true).isEmpty()) return;
        var actual = emptying ? net.minecraftforge.fluids.FluidUtil.tryEmptyContainer(stack, tank, Integer.MAX_VALUE, null, true)
                : net.minecraftforge.fluids.FluidUtil.tryFillContainer(stack, tank, Integer.MAX_VALUE, null, true);
        if (actual.isSuccess()) {
            containerInput.storage.extractItem(0, 1, false);
            containerOutput.storage.insertItem(0, actual.getResult(), false);
        }
    }

    /** Upstream {@code INITIAL_INVENTORY_SIZE << tier}, capped at the GT shift limit. */
    public static int getTankCapacity(int tier) {
        return INITIAL_TANK_CAPACITY * (1 << Math.min(9, Math.max(0, tier)));
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    // ////////////////////////////////////
    // ***** UI *****//
    // ////////////////////////////////////

    /**
     * Single-slot fluid hatch screen, mirroring GT's
     * {@code FluidHatchPartMachine#createSingleSlotGUI}: display background,
     * bucket fill / drain tank widget and amount / fluid name labels.
     */
    @Override
    public Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 89, 87);
        group.addWidget(new ImageWidget(4, 4, 81, 55, GuiTextures.DISPLAY));
        group.addWidget(new TankWidget(tank.getStorages()[0], 67, 22, 18, 18, true, true)
                .setShowAmount(true).setDrawHoverTips(true).setBackground(GuiTextures.FLUID_SLOT));
        group.addWidget(new LabelWidget(8, 8, "gtceu.gui.fluid_amount"));
        group.addWidget(new LabelWidget(8, 18, this::getFluidAmountText));
        group.addWidget(new LabelWidget(8, 28, () -> getFluidNameText().getString()));
        group.addWidget(meowmel.pollution.common.gui.MachineGuiWidgets.itemSlot(containerInput, 0, 24, 65));
        group.addWidget(meowmel.pollution.common.gui.MachineGuiWidgets.itemSlot(containerOutput, 0, 48, 65).setCanPutItems(false));
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        return group;
    }

    private String getFluidAmountText() {
        return String.format("%,d", tank.getFluidInTank(0).getAmount());
    }

    private Component getFluidNameText() {
        FluidStack stored = tank.getFluidInTank(0);
        return stored.isEmpty() ? Component.empty() : stored.getDisplayName();
    }
}
