package meowmel.pollution.common.machine.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMufflerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

/**
 * Flux muffler: magically filtered muffler hatch.
 *
 * <p>Upstream ({@code MetaTileEntityFluxMuffler}) differed from the GT muffler
 * in two ways: the recovery chance is {@code min((tier-1)*10, 100)} instead of
 * GT's {@code tier*10}, and the recovery inventory is
 * {@code (1 + min(UHV, tier))^2} slots. Upstream also overrode its old
 * pollution hook to report 0, i.e. the muffler itself adds no industrial
 * pollution on recovery; kept as a comment because modern GT removed that
 * hook.</p>
 */
public class FluxMufflerMachine extends TieredPartMachine implements IMufflerMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            FluxMufflerMachine.class, MultiblockPartMachine.MANAGED_FIELD_HOLDER);

    private final int recoveryChance;

    @Persisted
    private final CustomItemStackHandler inventory;

    public FluxMufflerMachine(IMachineBlockEntity holder, int tier) {
        super(holder, tier);
        this.recoveryChance = Math.min((tier - 1) * 10, 100);
        int sizeRoot = 1 + Math.min(GTValues.UHV, getTier());
        this.inventory = new CustomItemStackHandler(sizeRoot * sizeRoot);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    public int getRecoveryChance() {
        return recoveryChance;
    }

    @Override
    public void recoverItemsTable(ItemStack... recoveryItems) {
        int rolls = Math.min(recoveryItems.length, inventory.getSlots());
        for (int slot = 0; slot < rolls; slot++) {
            if (calculateChance()) {
                ItemHandlerHelper.insertItemStacked(inventory, recoveryItems[slot].copy(), false);
            }
        }
    }

    private boolean calculateChance() {
        return recoveryChance >= 100 || recoveryChance > GTValues.RNG.nextInt(100);
    }
}
