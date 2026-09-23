package meowmel.pollution.common.machine.multiblock.magic;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.common.machine.multiblock.part.EnergyHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import dev.tc4port.thaumcraft.api.aspect.AspectAmounts;
import dev.tc4port.thaumcraft.api.aspect.AspectApi;
import dev.tc4port.thaumcraft.api.aspect.AspectId;
import dev.tc4port.thaumcraft.api.aspect.VisChannel;
import dev.tc4port.thaumcraft.api.node.AuraNodeState;
import dev.tc4port.thaumcraft.api.node.NodeVis;
import dev.tc4port.thaumcraft.block.entity.AuraNodeBlockEntity;
import meowmel.pollution.api.pollution.PollutionEngine;
import meowmel.pollution.api.unification.PollutionMaterials;
import meowmel.pollution.common.machine.multiblock.AbstractDisplayMultiblockMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Essence collector: condenses the local Thaumcraft energy field into pure
 * infused fluids.
 *
 * <p>Upstream read the TC6 ambient chunk aura: it needed {@code flux < vis}
 * and produced six infused fluids each tick at
 * {@code ceil(0.025 × (1+coil) × 2^tier × (ln(vis) − ln(1+flux/vis)))}.
 * TC4R has no ambient aura, so the port substitutes the aura node's current vis
 * for {@code vis} and the industrial pollution of the chunk for {@code flux}.</p>
 *
 * <p>Upstream focus mode: an essence crystal in the first slot of the input bus
 * selected a single element; the collector then emitted only that element's
 * infused fluid at {@code 3 × speed} (half of the normal total output of six
 * fluids). The port maps the TC6 crystal translation keys to the TC4R
 * {@code thaumcraft:crystal_essence} item and reads its stored primal aspect
 * through the TC4R aspect API. The crystal is a filter and is never consumed.
 * Focus state is persisted and shown in the display panel.</p>
 */
public class EssenceCollectorMachine extends AbstractDisplayMultiblockMachine {

    private static final float BASIC_SPEED_PER_TICK = 0.025F;
    private static final int NODE_RADIUS = 8;
    private static final ResourceLocation ESSENCE_CRYSTAL_ID =
            ResourceLocation.fromNamespaceAndPath("thaumcraft", "crystal_essence");

    private TickableSubscription tickSubscription;
    private boolean isFocused;
    private String focusedChannel = "";

    public EssenceCollectorMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            tickSubscription = subscribeServerTick(this::tickCollector);
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

    private void tickCollector() {
        if (!(getLevel() instanceof ServerLevel level) || !isFormed()) {
            return;
        }
        EnergyHatchPartMachine energy = findPart(EnergyHatchPartMachine.class);
        if (energy == null) {
            return;
        }
        List<FluidHatchPartMachine> outputs = new ArrayList<>();
        for (IMultiPart part : getParts()) {
            if (part.self() instanceof FluidHatchPartMachine hatch) {
                outputs.add(hatch);
            }
        }
        if (outputs.isEmpty()) {
            return;
        }

        long voltage = Math.max(1, energy.energyContainer.getInputVoltage());
        if (energy.energyContainer.getEnergyStored() < voltage) {
            return;
        }
        int euTier = Math.max(1, (int) Math.ceil(Math.log((double) voltage / 32) / Math.log(4) + 1));

        float vis = 0;
        BlockPos nodePos = findNode(level);
        if (nodePos != null && level.getBlockEntity(nodePos) instanceof AuraNodeBlockEntity node) {
            vis = total(node.nodeState().currentVis());
        }
        float flux = (float) PollutionEngine.get(level, getPos());
        int speed = calculateSpeed(vis, flux, euTier);
        if (speed <= 0) {
            isFocused = false;
            focusedChannel = "";
            return;
        }

        ItemBusPartMachine items = findPart(ItemBusPartMachine.class);
        Focus focus = items == null ? null : readFocus(items);

        energy.energyContainer.changeEnergy(-voltage);
        if (focus == null) {
            isFocused = false;
            focusedChannel = "";
            writeFluids(outputs, speed);
        } else {
            isFocused = true;
            focusedChannel = focus.channel().name();
            writeFluid(outputs, focus.material(), speed * 3);
        }
    }

    /** Focus filter read from the first slot of the input bus. */
    private record Focus(VisChannel channel, Material material) {}

    /**
     * Reads the focus crystal from slot 0 of the input bus. Only the TC4R
     * crystal essence with exactly one stored primal aspect selects a focus;
     * any other item (including empty slots) falls back to normal mode.
     */
    @Nullable
    private Focus readFocus(ItemBusPartMachine items) {
        var inventory = items.getInventory().storage;
        if (inventory.getSlots() <= 0) {
            return null;
        }
        ItemStack stack = inventory.getStackInSlot(0);
        if (stack.isEmpty() || !isEssenceCrystal(stack)) {
            return null;
        }
        AspectAmounts aspects = AspectApi.getItemAspects(stack);
        if (aspects.amounts().size() != 1) {
            return null;
        }
        AspectId aspect = aspects.amounts().keySet().iterator().next();
        VisChannel channel = VisChannel.fromAspectId(aspect).orElse(null);
        if (channel == null) {
            return null;
        }
        Material material = materialFor(channel);
        return material == null || !material.hasFluid() ? null : new Focus(channel, material);
    }

    private static boolean isEssenceCrystal(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(ESSENCE_CRYSTAL_ID);
    }

    @Nullable
    private static Material materialFor(VisChannel channel) {
        return switch (channel) {
            case AER -> PollutionMaterials.InfusedAir;
            case IGNIS -> PollutionMaterials.InfusedFire;
            case TERRA -> PollutionMaterials.InfusedEarth;
            case AQUA -> PollutionMaterials.InfusedWater;
            case ORDO -> PollutionMaterials.InfusedOrder;
            case PERDITIO -> PollutionMaterials.InfusedEntropy;
        };
    }

    @Nullable
    private Material focusedMaterial() {
        if (focusedChannel.isEmpty()) {
            return null;
        }
        try {
            return materialFor(VisChannel.valueOf(focusedChannel));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private void writeFluid(List<FluidHatchPartMachine> outputs, Material material, int amount) {
        if (material == null || !material.hasFluid() || amount <= 0) {
            return;
        }
        FluidStack fluid = material.getFluid(amount);
        if (fluid.isEmpty()) {
            return;
        }
        for (FluidHatchPartMachine hatch : outputs) {
            if (hatch.tank.fill(fluid, IFluidHandler.FluidAction.EXECUTE) > 0) {
                return;
            }
        }
    }

    private int calculateSpeed(float vis, float flux, int euTier) {
        if (vis <= 0 || flux >= vis) {
            return 0;
        }
        double ratio = flux / vis;
        double speedFactor = Math.log(vis) - Math.log(1 + ratio);
        if (speedFactor <= 0) {
            return 0;
        }
        int coilLevel = hasCoil() ? Math.max(1, getCoilLevel()) : 0;
        return (int) Math.ceil(BASIC_SPEED_PER_TICK * (1 + coilLevel) * Math.pow(2, euTier) * speedFactor);
    }

    private void writeFluids(List<FluidHatchPartMachine> outputs, int amount) {
        FluidStack[] fluids = {
                PollutionMaterials.InfusedAir.getFluid(amount),
                PollutionMaterials.InfusedFire.getFluid(amount),
                PollutionMaterials.InfusedEarth.getFluid(amount),
                PollutionMaterials.InfusedWater.getFluid(amount),
                PollutionMaterials.InfusedOrder.getFluid(amount),
                PollutionMaterials.InfusedEntropy.getFluid(amount),
        };
        for (FluidStack fluid : fluids) {
            if (fluid.isEmpty()) {
                continue;
            }
            for (FluidHatchPartMachine hatch : outputs) {
                if (hatch.tank.fill(fluid, IFluidHandler.FluidAction.EXECUTE) > 0) {
                    break;
                }
            }
        }
    }

    private BlockPos findNode(ServerLevel level) {
        for (BlockPos pos : BlockPos.betweenClosed(
                getPos().offset(-NODE_RADIUS, -NODE_RADIUS, -NODE_RADIUS),
                getPos().offset(NODE_RADIUS, NODE_RADIUS, NODE_RADIUS))) {
            if (level.getBlockEntity(pos) instanceof AuraNodeBlockEntity) {
                return pos.immutable();
            }
        }
        return null;
    }

    private static int total(NodeVis vis) {
        int sum = 0;
        for (Integer amount : vis.amounts().values()) {
            if (amount != null && amount > 0) {
                sum += amount;
            }
        }
        return sum;
    }

    private int getCoilLevel() {
        var coil = getMultiblockState().getMatchContext().get("CoilType");
        if (coil instanceof com.gregtechceu.gtceu.api.block.ICoilType coilType) {
            return coilType.getLevel();
        }
        return 0;
    }

    private boolean hasCoil() {
        return getCoilLevel() > 0;
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (isFormed()) {
            textList.add(Component.literal("Heating Coil Level: " + getCoilLevel()));
            Material focus = focusedMaterial();
            textList.add(Component.literal("Focus Mode: " + (isFocused
                    ? "On (" + (focus == null ? focusedChannel : focus.getLocalizedName().getString()) + ")"
                    : "Off")));
        }
    }

    @Override
    public void saveCustomPersistedData(CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        tag.putBoolean("FocusMode", isFocused);
        tag.putString("FocusChannel", focusedChannel);
    }

    @Override
    public void loadCustomPersistedData(CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        isFocused = tag.getBoolean("FocusMode");
        focusedChannel = tag.getString("FocusChannel");
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
        return EssenceCollectorPatterns.create(definition);
    }
}
