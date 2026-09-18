package meowmel.pollution.common.machine.part;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

/**
 * Infused fluid hatch: the fluid buffer of magic multiblocks.
 *
 * <p>Upstream ({@code MetaTileEntityInfusedFluidHatch}) held a single tank of
 * {@code 8000 * 2^tier} mB plus one canister slot in / one out. The slot
 * interaction is pending the item stage; the tank itself is functional.</p>
 */
public class InfusedFluidHatchMachine extends TieredPartMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            InfusedFluidHatchMachine.class, MultiblockPartMachine.MANAGED_FIELD_HOLDER);

    public static final int INITIAL_TANK_CAPACITY = 8000;

    @Persisted
    public final NotifiableFluidTank tank;

    public InfusedFluidHatchMachine(IMachineBlockEntity holder, int tier) {
        super(holder, tier);
        this.tank = new NotifiableFluidTank(this, 1, getTankCapacity(tier), IO.BOTH, IO.BOTH);
    }

    /** Upstream {@code INITIAL_INVENTORY_SIZE << tier}, capped at the GT shift limit. */
    public static int getTankCapacity(int tier) {
        return INITIAL_TANK_CAPACITY * (1 << Math.min(9, Math.max(0, tier)));
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}
