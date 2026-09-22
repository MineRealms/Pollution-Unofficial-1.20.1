package meowmel.pollution.common.machine.part.mana;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import meowmel.pollution.api.capability.IManaHatch;
import meowmel.pollution.common.gui.MachineGuiWidgets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import vazkii.botania.api.mana.ManaReceiver;

import java.util.List;

/**
 * Mana energy hatch: stores Botania mana inside a GregTech energy container.
 *
 * <p>Upstream origin: {@code MetaTileEntityManaHatch} (1.12.2), an
 * {@code IEnergyContainer} multiblock part that is the EU interface of mana
 * multiblocks. The port keeps the upstream numbers: input capacity
 * {@code V[tier] * 16 * amperage}, output capacity {@code V[tier] * 64 * amperage}
 * and a neighbour transfer of {@code V[tier] * amperage} per tick.</p>
 *
 * <p>Deviation: 1.12 Botania detected receivers with
 * {@code instanceof IManaReceiver}; the port implements Botania's
 * {@link ManaReceiver} and is exposed through the {@code MANA_RECEIVER}
 * capability (see {@link ManaHatchCapabilityEvents}) so mana bursts and sparks
 * can charge the input hatch.</p>
 */
public class ManaHatchMachine extends TieredPartMachine implements IManaHatch, ManaReceiver {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            ManaHatchMachine.class, MultiblockPartMachine.MANAGED_FIELD_HOLDER);

    protected final boolean isExportHatch;
    protected final int amperage;

    @Persisted
    public final NotifiableEnergyContainer energyContainer;

    private TickableSubscription transferSubscription;

    public ManaHatchMachine(IMachineBlockEntity holder, int tier, int amperage, boolean isExport) {
        super(holder, tier);
        this.isExportHatch = isExport;
        this.amperage = amperage;
        if (isExport) {
            this.energyContainer = NotifiableEnergyContainer.emitterContainer(
                    this, GTValues.V[tier] * 64L * amperage, GTValues.V[tier], amperage);
            this.energyContainer.setSideOutputCondition(side -> side == getFrontFacing());
        } else {
            this.energyContainer = NotifiableEnergyContainer.receiverContainer(
                    this, GTValues.V[tier] * 16L * amperage, GTValues.V[tier], amperage);
        }
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            transferSubscription = subscribeServerTick(this::pushManaToNeighbours);
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (transferSubscription != null) {
            transferSubscription.unsubscribe();
            transferSubscription = null;
        }
    }

    private void pushManaToNeighbours() {
        if (!isExportHatch) return;
        Level level = getLevel();
        if (level == null) return;
        long available = energyContainer.getEnergyStored();
        if (available <= 0L) return;
        long rate = Math.min(available, GTValues.V[getTier()] * (long) amperage);
        if (rate <= 0L) return;
        for (Direction direction : Direction.values()) {
            long accepted = ManaReceiverLookup.pushMana(level, getPos().relative(direction), direction, rate);
            if (accepted <= 0L) continue;
            energyContainer.removeEnergy(accepted);
            return;
        }
    }

    @Override
    public long getMaxMana() {
        return energyContainer.getEnergyCapacity();
    }

    @Override
    public long getMana() {
        return energyContainer.getEnergyStored();
    }

    @Override
    public boolean isFull() {
        return getMana() >= getMaxMana();
    }

    @Override
    public void receiveMana(long mana) {
        if (mana <= 0L || isFull()) return;
        energyContainer.addEnergy(mana);
    }

    @Override
    public boolean consumeMana(long amount, boolean simulate) {
        if (amount <= 0L) return true;
        if (simulate) return energyContainer.getEnergyStored() >= amount;
        return energyContainer.removeEnergy(amount) > 0L;
    }

    @Override
    public boolean canReceiveManaFromBursts() {
        return !isExportHatch && !isFull();
    }

    @Override
    public Level getManaReceiverLevel() {
        return getLevel();
    }

    @Override
    public BlockPos getManaReceiverPos() {
        return getPos();
    }

    @Override
    public int getCurrentMana() {
        return (int) Math.min(getMana(), Integer.MAX_VALUE);
    }

    @Override
    public void receiveMana(int mana) {
        if (!isExportHatch) {
            receiveMana((long) mana);
        }
    }

    // ////////////////////////////////////
    // ***** UI *****//
    // ////////////////////////////////////

    /**
     * GT-style status panel; mana is not a Forge fluid, so it is rendered with
     * labels plus the GT energy bar instead of a tank widget.
     */
    @Override
    public Widget createUIWidget() {
        return MachineGuiWidgets.infoPanel(150, 56,
                () -> self().getBlockState().getBlock().getDescriptionId(),
                List.of(
                        () -> Component.translatable("pollution.machine.mana_hatch.gui.amount",
                                String.format("%,d", getMana()), String.format("%,d", getMaxMana())).getString(),
                        () -> Component.translatable("pollution.machine.mana_hatch.gui.rate",
                                String.format("%,d", GTValues.V[getTier()] * amperage)).getString()),
                this::getFillFraction);
    }

    private double getFillFraction() {
        long capacity = getMaxMana();
        return capacity <= 0L ? 0.0D : (double) getMana() / (double) capacity;
    }
}
