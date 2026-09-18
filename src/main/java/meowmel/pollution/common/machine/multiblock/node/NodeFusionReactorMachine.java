package meowmel.pollution.common.machine.multiblock.node;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import meowmel.pollution.api.capability.ICleanVis;
import meowmel.pollution.api.pollution.PollutionEngine;
import meowmel.pollution.api.unification.PollutionMaterials;
import meowmel.pollution.common.item.PackagedAuraNode;
import meowmel.pollution.common.machine.multiblock.MagicMultiblockController;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

/**
 * Node fusion reactor (LuV/ZPM/UV): runs fusion and node-fusion recipes with
 * packaged aura nodes as the catalyst.
 *
 * <p>Upstream burned White/Black/Starry Mansus fluids; the port maps them to
 * {@code InfusedLight}/{@code InfusedDark}/{@code InfusedAura}. The TC6
 * ambient-aura cleanliness check becomes an industrial-pollution check
 * ({@code PollutionEngine.get <= 4.2}). Node effect tables (parallel, energy,
 * progress, mansus counts) keep the upstream fall-through switch semantics.
 * Fusion start-cost handling of modern GT is not reimplemented; recipes still
 * run off the energy hatches (documented limitation).</p>
 */
public class NodeFusionReactorMachine extends MagicMultiblockController implements ICleanVis {

    private final int tier;

    private TickableSubscription tickSubscription;
    private int overallParallelAmount = 1;
    private int overallBlackAmount;
    private int overallWhiteAmount;
    private int overallStarryAmount;

    public NodeFusionReactorMachine(IMachineBlockEntity holder, int tier) {
        super(holder);
        this.tier = tier;
    }

    public int getTier() {
        return tier;
    }

    public Material getMaterial() {
        return PollutionMaterials.InfusedMagic;
    }

    @Override
    public boolean isCleanVis() {
        return !(getLevel() instanceof ServerLevel level) || PollutionEngine.get(level, getPos()) <= 4.2;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            tickSubscription = subscribeServerTick(this::tickReactor);
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

    private void tickReactor() {
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
        readNodeInfo(items);

        if (overallBlackAmount == 0 && overallWhiteAmount == 0 && overallStarryAmount == 0) {
            return;
        }
        boolean supplied = hasMansus(fluids, PollutionMaterials.InfusedDark, overallBlackAmount)
                && hasMansus(fluids, PollutionMaterials.InfusedLight, overallWhiteAmount)
                && hasMansus(fluids, PollutionMaterials.InfusedAura, overallStarryAmount);
        if (supplied && recipeLogic.isWorking()) {
            drainMansus(fluids, PollutionMaterials.InfusedDark, overallBlackAmount);
            drainMansus(fluids, PollutionMaterials.InfusedLight, overallWhiteAmount);
            drainMansus(fluids, PollutionMaterials.InfusedAura, overallStarryAmount);
        }
    }

    private void readNodeInfo(ItemBusPartMachine items) {
        int parallelAmount = 1;
        int blackAmount = 0;
        int whiteAmount = 0;
        int starryAmount = 0;
        var inventory = items.getInventory().storage;
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!PackagedAuraNode.isNode(stack)) {
                continue;
            }
            switch (PackagedAuraNode.type(stack)) {
                case "Ominous":
                    parallelAmount += 1;
                    blackAmount += 1;
                    whiteAmount += 1;
                case "Pure":
                    blackAmount -= 1;
                    whiteAmount -= 1;
                case "Voracious":
                    parallelAmount += 2;
                    starryAmount += 1;
                case "Concussive":
                    blackAmount += 1;
                    whiteAmount += 1;
                default:
            }
            switch (PackagedAuraNode.tier(stack)) {
                case "Bright":
                    blackAmount -= 1;
                    whiteAmount -= 1;
                case "Withering":
                    parallelAmount -= 1;
                default:
            }
        }
        if (parallelAmount < 1) {
            parallelAmount = 1;
        }
        overallParallelAmount = parallelAmount;
        overallBlackAmount = blackAmount;
        overallWhiteAmount = whiteAmount;
        overallStarryAmount = starryAmount;
    }

    private boolean hasMansus(FluidHatchPartMachine fluids, Material material, int amount) {
        if (amount <= 0 || !material.hasFluid()) {
            return true;
        }
        FluidStack stack = material.getFluid(amount);
        return fluids.tank.drain(stack, IFluidHandler.FluidAction.SIMULATE).getAmount() >= amount;
    }

    private void drainMansus(FluidHatchPartMachine fluids, Material material, int amount) {
        if (amount <= 0 || !material.hasFluid()) {
            return;
        }
        fluids.tank.drain(material.getFluid(amount), IFluidHandler.FluidAction.EXECUTE);
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
        return NodeFusionReactorPatterns.create(definition);
    }
}
