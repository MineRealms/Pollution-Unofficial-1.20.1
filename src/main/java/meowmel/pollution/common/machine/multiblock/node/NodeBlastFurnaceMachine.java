package meowmel.pollution.common.machine.multiblock.node;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import meowmel.pollution.api.unification.PollutionMaterials;
import meowmel.pollution.common.item.PackagedAuraNode;
import meowmel.pollution.common.machine.multiblock.MagicMultiblockController;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

/**
 * Node blast furnace: runs blast and forge-alchemy recipes on coil temperature,
 * using packaged aura nodes as an essentia catalyst.
 *
 * <p>Upstream converted the node's order/entropy essence into White/Black
 * Mansus fluids while a node burned. Those fluids are not part of the port;
 * they map to {@code InfusedLight} (white) and {@code InfusedDark} (black) at
 * the same {@code essence × 10} rate. One node is consumed every 30 seconds
 * while a valid recipe structure is formed.</p>
 */
public class NodeBlastFurnaceMachine extends MagicMultiblockController {

    private static final int NODE_LIFETIME_TICKS = 600;

    private TickableSubscription tickSubscription;
    private int nodeOrder;
    private int nodeEntropy;
    private int nodeTimer = -1;

    public NodeBlastFurnaceMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            tickSubscription = subscribeServerTick(this::tickFurnace);
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

    private void tickFurnace() {
        if (!(getLevel() instanceof ServerLevel) || !isFormed()) {
            return;
        }
        if (getOffsetTimer() % 20 != 0) {
            return;
        }
        ItemBusPartMachine items = findPart(ItemBusPartMachine.class);
        FluidHatchPartMachine fluids = findPart(FluidHatchPartMachine.class);
        if (items == null || fluids == null) {
            return;
        }

        if (nodeTimer < 0) {
            if (!consumeNode(items)) {
                return;
            }
        } else {
            nodeTimer += 20;
            if (nodeTimer >= NODE_LIFETIME_TICKS) {
                nodeTimer = -1;
                nodeOrder = 0;
                nodeEntropy = 0;
                return;
            }
        }

        if (nodeOrder > 0) {
            fillFluid(fluids, PollutionMaterials.InfusedLight.getFluid(nodeOrder * 10));
        }
        if (nodeEntropy > 0) {
            fillFluid(fluids, PollutionMaterials.InfusedDark.getFluid(nodeEntropy * 10));
        }
    }

    private boolean consumeNode(ItemBusPartMachine items) {
        var inventory = items.getInventory().storage;
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!PackagedAuraNode.isNode(stack)) {
                continue;
            }
            nodeOrder = PackagedAuraNode.essence(stack, PackagedAuraNode.ESSENCE_ORDER);
            nodeEntropy = PackagedAuraNode.essence(stack, PackagedAuraNode.ESSENCE_ENTROPY);
            inventory.extractItem(slot, 1, false);
            nodeTimer = 0;
            return true;
        }
        return false;
    }

    private void fillFluid(FluidHatchPartMachine fluids, FluidStack stack) {
        if (!stack.isEmpty()) {
            fluids.tank.fill(stack, IFluidHandler.FluidAction.EXECUTE);
        }
    }

    private <T> T findPart(Class<T> type) {
        for (IMultiPart part : getParts()) {
            if (type.isInstance(part.self())) {
                return type.cast(part.self());
            }
        }
        return null;
    }

    public static BlockPattern createPattern(MultiblockMachineDefinition definition) {
        return NodeBlastFurnacePatterns.create(definition);
    }
}
