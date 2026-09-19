package meowmel.pollution.common.machine.multiblock.botania;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import meowmel.pollution.api.unification.PollutionMaterials;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Terra (Botania) gas collector.
 *
 * <p>Upstream origin: {@code MetaTileEntityBotGasCollector} (1.12.2). The
 * shell (Terra casing V, tiered beam cores, baminated glass, mana plate II,
 * KQGold frame) and the collection math are kept: liquid essence
 * (air/fire/alien) plus a mana catalyst is converted into the corresponding
 * GregTech air, at {@code 100 x manaCost} mB per operation, with
 * {@code manaCost = 4 * 2^(tier-2)} and essence cost
 * {@code (14 - beamLevel) * 0.5 * (1 - 0.05 * tier)} per tick.</p>
 *
 * <p>Deviations:</p>
 * <ul>
 *   <li>Upstream consumed the GTQT {@code WhiteMansus} fluid every 100
 *       operations. That material is not part of the port; the machine uses
 *       {@code InfusedMagic} as the mana catalyst (documented substitution).
 *       The upstream counter bug (the catalyst was only drained when the
 *       counter was already reset) is fixed by draining every 100 successful
 *       operations.</li>
 *   <li>The upstream {@code tier} field was never assigned; the port keeps the
 *       field NBT-persisted and defaults to 0, matching upstream behaviour.</li>
 *   <li>The KQGold frame is substituted with a GTCEu TungstenSteel frame.</li>
 * </ul>
 */
public class BotGasCollectorMachine extends AbstractManaControlMachine implements IDisplayUIMachine {

    private static final Map<net.minecraft.world.level.material.Fluid, net.minecraft.world.level.material.Fluid> GAS_MAP =
            new LinkedHashMap<>();

    static {
        GAS_MAP.put(PollutionMaterials.InfusedAir.getFluid(), GTMaterials.Air.getFluid());
        GAS_MAP.put(PollutionMaterials.InfusedFire.getFluid(), GTMaterials.NetherAir.getFluid());
        GAS_MAP.put(PollutionMaterials.InfusedAlien.getFluid(), GTMaterials.EnderAir.getFluid());
    }

    private int times;
    private int beamLevel;
    private int essenceConsumptionSpeed;
    private int manaConsumptionSpeed;
    private int finalCollectionSpeed;
    private int tier;
    private TickableSubscription tickSubscription;

    public BotGasCollectorMachine(IMachineBlockEntity holder) {
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
        if (!(getLevel() instanceof ServerLevel) || !isFormed()) {
            return;
        }
        beamLevel = Math.max(1, BotaniaStructureElements.beamCoreTier(this));
        essenceConsumptionSpeed = Math.max(1,
                (int) ((14 - beamLevel) * 0.5 * (1 - 0.05 * tier)));
        manaConsumptionSpeed = (int) (4 * Math.pow(2, Math.max(0, tier - 2)));
        finalCollectionSpeed = manaConsumptionSpeed * 100;

        List<FluidHatchPartMachine> inputs = fluidHatches(true);
        List<FluidHatchPartMachine> outputs = fluidHatches(false);
        if (inputs.isEmpty() || outputs.isEmpty()) {
            return;
        }

        FluidStack catalyst = PollutionMaterials.InfusedMagic.getFluid(essenceConsumptionSpeed * 10);
        boolean catalystReady = !catalyst.isEmpty() && hasFluid(inputs, catalyst);

        for (Map.Entry<net.minecraft.world.level.material.Fluid, net.minecraft.world.level.material.Fluid> entry
                : GAS_MAP.entrySet()) {
            FluidStack essence = new FluidStack(entry.getKey(), essenceConsumptionSpeed);
            if (!hasFluid(inputs, essence)) {
                continue;
            }
            if (!catalystReady) {
                return;
            }
            if (!consumeMana(manaConsumptionSpeed, true)) {
                return;
            }
            consumeMana(manaConsumptionSpeed, false);
            drainFluid(inputs, essence);
            FluidStack gas = new FluidStack(entry.getValue(), finalCollectionSpeed);
            if (fillFluid(outputs, gas)) {
                times++;
                if (times >= 100) {
                    times = 0;
                    drainFluid(inputs, catalyst);
                }
            }
            return;
        }
    }

    private List<FluidHatchPartMachine> fluidHatches(boolean input) {
        PartAbility ability = input ? PartAbility.IMPORT_FLUIDS : PartAbility.EXPORT_FLUIDS;
        List<FluidHatchPartMachine> hatches = new ArrayList<>();
        for (IMultiPart part : getParts()) {
            if (part.self() instanceof FluidHatchPartMachine hatch
                    && ability.isApplicable(hatch.getBlockState().getBlock())) {
                hatches.add(hatch);
            }
        }
        return hatches;
    }

    private static boolean hasFluid(List<FluidHatchPartMachine> hatches, FluidStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        for (FluidHatchPartMachine hatch : hatches) {
            FluidStack stored = hatch.tank.getFluidInTank(0);
            if (!stored.isEmpty() && stored.getFluid() == stack.getFluid()
                    && stored.getAmount() >= stack.getAmount()) {
                return true;
            }
        }
        return false;
    }

    private static boolean drainFluid(List<FluidHatchPartMachine> hatches, FluidStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        for (FluidHatchPartMachine hatch : hatches) {
            FluidStack stored = hatch.tank.getFluidInTank(0);
            if (!stored.isEmpty() && stored.getFluid() == stack.getFluid()
                    && stored.getAmount() >= stack.getAmount()) {
                return hatch.tank.drain(stack, IFluidHandler.FluidAction.EXECUTE).getAmount() > 0;
            }
        }
        return false;
    }

    private static boolean fillFluid(List<FluidHatchPartMachine> hatches, FluidStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        for (FluidHatchPartMachine hatch : hatches) {
            if (hatch.tank.fill(stack, IFluidHandler.FluidAction.EXECUTE) > 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (isFormed()) {
            textList.add(Component.translatable("pollution.machine.bot_gas_collector_beamLevel", beamLevel));
            textList.add(Component.translatable("pollution.machine.bot_gas_collector_essenceConsumptionSpeed",
                    essenceConsumptionSpeed));
            textList.add(Component.translatable("pollution.machine.bot_gas_collector_manaConsumptionSpeed",
                    manaConsumptionSpeed));
            textList.add(Component.translatable("pollution.machine.bot_gas_collector_finalCollectionSpeed",
                    finalCollectionSpeed));
        }
    }

    @Override
    public void saveCustomPersistedData(CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        tag.putInt("CollectorTimes", times);
        tag.putInt("CollectorTier", tier);
    }

    @Override
    public void loadCustomPersistedData(CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        times = tag.getInt("CollectorTimes");
        tier = tag.getInt("CollectorTier");
    }

    public static BlockPattern createPattern(MultiblockMachineDefinition definition) {
        return BotGasCollectorPatterns.create(definition);
    }
}
