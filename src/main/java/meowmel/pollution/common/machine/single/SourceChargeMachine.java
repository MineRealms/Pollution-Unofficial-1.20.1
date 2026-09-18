package meowmel.pollution.common.machine.single;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import meowmel.pollution.api.unification.PollutionMaterials;
import meowmel.pollution.common.item.bauble.ItemWaterRing;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

/**
 * Source charge: fills ported source baubles from matching infused fluids.
 *
 * <p>Upstream origin: {@code MetaTileEntitySourceCharge} (1.12.2, single
 * block, registered as {@code source_charge}). It held one item slot and one
 * 4000 mB import tank. Every tick, when the slot contained a Baubles
 * {@code ItemBaubleBehavior} whose {@code Material} mapped to an entry of
 * {@code infusedFluidStack.STACK_MAP}, and the tank held at least 1 mB of that
 * fluid, it added 1 point of source to the item (simulated first) and drained
 * 1 mB.</p>
 *
 * <p>Deviations:</p>
 * <ul>
 *   <li>Upstream's generic {@code ItemBaubleBehavior}/{@code SourceMaterialItem}
 *       contract was not ported; only the Curios water ring
 *       ({@link ItemWaterRing}) survives in the port. The machine therefore
 *       recognises that ring and charges it with
 *       {@link PollutionMaterials#InfusedWater}. This is exactly the upstream
 *       pairing: the water ring's behaviour was constructed with
 *       {@code PollutionMaterials.InfusedWater}, whose {@code STACK_MAP} entry
 *       is the material's own 1 mB fluid, so no substitution from the porting
 *       table is needed for this machine (none of its upstream items or fluids
 *       are missing).</li>
 *   <li>The source store NBT key ({@code source}) and the 120000 point cap are
 *       unchanged, as is the 1 point per 1 mB conversion.</li>
 *   <li>The ModularUI screen is not ported (consistent with the other ported
 *       single-block machines); the item slot and the tank are exposed as
 *       input-only capabilities, so they can be loaded with hoppers or pipes.</li>
 * </ul>
 */
public class SourceChargeMachine extends MetaMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            SourceChargeMachine.class, MetaMachine.MANAGED_FIELD_HOLDER);

    public static final int TANK_CAPACITY = 4000;

    private static final int SOURCE_PER_OPERATION = 1;
    private static final int FLUID_PER_OPERATION = 1;

    private final NotifiableItemStackHandler inventory;
    private final NotifiableFluidTank tank;

    private TickableSubscription tickSubscription;

    public SourceChargeMachine(IMachineBlockEntity holder) {
        super(holder);
        this.inventory = new NotifiableItemStackHandler(this, 1, IO.IN, IO.IN);
        this.tank = new NotifiableFluidTank(this, 1, TANK_CAPACITY, IO.IN, IO.IN);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            tickSubscription = subscribeServerTick(this::chargeTick);
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

    private void chargeTick() {
        ItemStack stack = inventory.getStackInSlot(0);
        if (!(stack.getItem() instanceof ItemWaterRing ring)) {
            return;
        }
        FluidStack required = requiredFluid(stack);
        if (required.isEmpty()) {
            return;
        }
        FluidStack stored = tank.getFluidInTank(0);
        if (stored.isEmpty() || !stored.isFluidEqual(required) || stored.getAmount() < FLUID_PER_OPERATION) {
            return;
        }
        if (!ring.addSource(SOURCE_PER_OPERATION, true, stack)) {
            return;
        }
        ring.addSource(SOURCE_PER_OPERATION, false, stack);
        tank.drainInternal(FLUID_PER_OPERATION, IFluidHandler.FluidAction.EXECUTE);
    }

    /**
     * The fluid accepted for the item in the slot. Upstream looked this up in
     * {@code STACK_MAP} through the bauble's material; in the port the only
     * ported source bauble is the water ring, charged by infused water.
     */
    private static FluidStack requiredFluid(ItemStack stack) {
        if (stack.getItem() instanceof ItemWaterRing) {
            return PollutionMaterials.InfusedWater.getFluid(FLUID_PER_OPERATION);
        }
        return FluidStack.EMPTY;
    }
}
