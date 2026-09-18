package meowmel.pollution.common.machine.part.mana;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import meowmel.pollution.api.capability.IManaHatch;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import vazkii.botania.api.mana.ManaReceiver;

/**
 * Mana pool hatch: buffers pure Botania mana in a {@link NotifiableManaContainer}.
 *
 * <p>Upstream origin: {@code MetaTileEntityManaPoolHatch} (1.12.2). The three
 * upstream pool types (capacity / machine tier) and the per-tick transfer rate
 * ({@code V[tier]}) are preserved. Input pools throttle external intake and
 * output pools throttle neighbour transfers to the transfer rate, as upstream
 * did with its tick counters.</p>
 *
 * <p>Deviation: Botania capability callers (bursts, sparks) are treated as
 * external mana and go through the throttled intake, because 1.12 never routed
 * them to this part at all (it only implemented Pollution's own
 * {@code IManaHatch}); the raw {@code IManaHatch.receiveMana} path used by
 * internal transfers stays unthrottled.</p>
 */
public class ManaPoolHatchMachine extends TieredPartMachine implements IManaHatch, ManaReceiver {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            ManaPoolHatchMachine.class, MultiblockPartMachine.MANAGED_FIELD_HOLDER);

    protected final boolean isExport;
    protected final PoolType poolType;

    @Persisted
    public final NotifiableManaContainer manaContainer;

    private TickableSubscription transferSubscription;

    public ManaPoolHatchMachine(IMachineBlockEntity holder, PoolType poolType, boolean isExport) {
        super(holder, poolType.getMachineTier());
        this.poolType = poolType;
        this.isExport = isExport;
        this.manaContainer = new NotifiableManaContainer(
                this, poolType.getCapacity(), poolType.getMachineTier(), poolType.getTransferRate());
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
        if (!isExport) return;
        Level level = getLevel();
        if (level == null) return;
        long available = Math.min(manaContainer.getMana(), manaContainer.getRemainingOutputTransferRate());
        if (available <= 0L) return;
        for (Direction direction : Direction.values()) {
            long accepted = ManaReceiverLookup.pushMana(level, getPos().relative(direction), direction, available);
            if (accepted <= 0L) continue;
            long removed = manaContainer.removeMana(accepted);
            manaContainer.noteOutputTransfer(removed);
            return;
        }
    }

    @Override
    public long getMaxMana() {
        return manaContainer.getMaxMana();
    }

    @Override
    public long getMana() {
        return manaContainer.getMana();
    }

    @Override
    public boolean isFull() {
        return manaContainer.isFull();
    }

    @Override
    public void receiveMana(long mana) {
        manaContainer.receiveMana(mana);
    }

    @Override
    public boolean consumeMana(long amount, boolean simulate) {
        return manaContainer.consumeMana(amount, simulate);
    }

    @Override
    public boolean canReceiveManaFromBursts() {
        return !isExport && manaContainer.canReceiveManaFromBursts();
    }

    @Override
    public void receiveManaFromBursts(int mana) {
        if (!isExport) {
            manaContainer.receiveManaFromBursts(mana);
        }
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
        if (!isExport) {
            manaContainer.receiveManaFromBursts(mana);
        }
    }

    /**
     * Upstream pool types: diluted (LV, 10k), normal (LuV, 1M) and mythic
     * (UEV, 1M). The mythic pool shares the normal capacity upstream but uses
     * the UEV transfer rate.
     */
    public enum PoolType {

        DILUTED("diluted", GTValues.LV, 10_000L),
        NORMAL("normal", GTValues.LuV, 1_000_000L),
        MYTHIC("mythic", GTValues.UEV, 1_000_000L);

        private final String name;
        private final int machineTier;
        private final long capacity;

        PoolType(String name, int machineTier, long capacity) {
            this.name = name;
            this.machineTier = machineTier;
            this.capacity = capacity;
        }

        public String getName() {
            return name;
        }

        public int getMachineTier() {
            return machineTier;
        }

        public long getCapacity() {
            return capacity;
        }

        public long getTransferRate() {
            return GTValues.V[machineTier];
        }
    }
}
