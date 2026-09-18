package meowmel.pollution.common.machine.part;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;
import dev.tc4port.thaumcraft.api.aspect.VisChannel;
import meowmel.pollution.api.capability.IVisHatch;
import meowmel.pollution.compat.tc4r.TC4RBridge;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;

/**
 * Vis hatch: a buffered vis source for magic multiblocks.
 *
 * <p>Upstream ({@code MetaTileEntityVisHatch}) drained 0.05 vis from the TC6
 * ambient aura every 20 ticks and stored one {@code tier} unit, with a capacity
 * of {@code tier * 2000}. The port keeps those numbers but drains real centivis
 * from the TC4R vis network, cycling the six vis channels, because TC4R vis only
 * exists inside nodes and the relay network.</p>
 */
public class VisHatchMachine extends TieredPartMachine implements IVisHatch {

    private static final String TAG_VIS_STORED = "VisStored";

    private static final int DRAIN_PERIOD_TICKS = 20;
    private static final int DRAIN_QUANTA = 5;

    private final VisChannel[] channels = VisChannel.values();

    private int visStored;

    private TickableSubscription tickSubscription;

    public VisHatchMachine(IMachineBlockEntity holder, int tier) {
        super(holder, tier);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            tickSubscription = subscribeServerTick(this::tickHatch);
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

    private void tickHatch() {
        if (!(getLevel() instanceof ServerLevel level)) {
            return;
        }
        long timer = getOffsetTimer();
        if (timer % DRAIN_PERIOD_TICKS != 0) {
            return;
        }
        if (visStored >= getMaxVisStore()) {
            return;
        }
        VisChannel channel = channels[(int) ((timer / DRAIN_PERIOD_TICKS) % channels.length)];
        int drained = TC4RBridge.drainVis(level, getPos(), channel, DRAIN_QUANTA);
        if (drained > 0) {
            visStored = Math.min(getMaxVisStore(), visStored + getTier());
            markDirty();
        }
    }

    @Override
    public void saveCustomPersistedData(CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        tag.putInt(TAG_VIS_STORED, visStored);
    }

    @Override
    public void loadCustomPersistedData(CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        visStored = tag.getInt(TAG_VIS_STORED);
    }

    @Override
    public int getVisStore() {
        return visStored;
    }

    @Override
    public int getMaxVisStore() {
        return getTier() * 2000;
    }

    @Override
    public boolean drainVis(int amount, boolean simulate) {
        if (amount <= 0 || visStored < amount) {
            return false;
        }
        if (!simulate) {
            visStored -= amount;
            markDirty();
        }
        return true;
    }
}
