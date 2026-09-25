package meowmel.pollution.common.machine.multiblock.magic;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.common.machine.multiblock.part.EnergyHatchPartMachine;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import meowmel.pollution.common.machine.multiblock.AbstractDisplayMultiblockMachine;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/** Persistent energy bank with the upstream core/coil capacity and transfer formulas. */
public class MagicBatteryMachine extends AbstractDisplayMultiblockMachine implements IControllable {
    private static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            MagicBatteryMachine.class, AbstractDisplayMultiblockMachine.MANAGED_FIELD_HOLDER);

    @Persisted @DescSynced private long storedEnergy;
    @Persisted @DescSynced private boolean workingEnabled = true;
    @DescSynced private int coreTier;
    @DescSynced private int coilTier;
    @DescSynced private long inputPerTick;
    @DescSynced private long outputPerTick;
    private EnergyContainerList inputs = new EnergyContainerList(List.of());
    private EnergyContainerList outputs = new EnergyContainerList(List.of());
    private TickableSubscription tickSubscription;

    public MagicBatteryMachine(IMachineBlockEntity holder) { super(holder); }
    @Override public ManagedFieldHolder getFieldHolder() { return MANAGED_FIELD_HOLDER; }
    @Override public boolean isWorkingEnabled() { return workingEnabled; }
    @Override public void setWorkingEnabled(boolean enabled) { workingEnabled = enabled; markDirty(); }
    public long getStoredEnergy() { return storedEnergy; }
    public long getCapacity() { return Math.max(storedEnergy, 250000L * coreTier * coilTier); }
    public long getTransferRate() { return coreTier == 0 ? 0 : GTValues.VA[Math.min(GTValues.MAX, coreTier * 2)]; }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        coreTier = (Integer) getMultiblockState().getMatchContext().get("BatteryCoreTier");
        coilTier = (Integer) getMultiblockState().getMatchContext().get("BatteryCoilTier");
        List<IEnergyContainer> inputHatches = new ArrayList<>();
        List<IEnergyContainer> outputHatches = new ArrayList<>();
        for (var part : getParts()) {
            if (part.self() instanceof EnergyHatchPartMachine hatch) {
                if (PartAbility.INPUT_ENERGY.isApplicable(hatch.getBlockState().getBlock())) inputHatches.add(hatch.energyContainer);
                if (PartAbility.OUTPUT_ENERGY.isApplicable(hatch.getBlockState().getBlock())) outputHatches.add(hatch.energyContainer);
            }
        }
        inputs = new EnergyContainerList(inputHatches);
        outputs = new EnergyContainerList(outputHatches);
        if (tickSubscription == null) tickSubscription = subscribeServerTick(this::tickBattery);
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        stopTicking();
        inputs = new EnergyContainerList(List.of());
        outputs = new EnergyContainerList(List.of());
        inputPerTick = outputPerTick = 0;
    }

    @Override
    public void onUnload() {
        stopTicking();
        super.onUnload();
    }

    private void stopTicking() {
        if (tickSubscription != null) tickSubscription.unsubscribe();
        tickSubscription = null;
    }

    private void tickBattery() {
        inputPerTick = outputPerTick = 0;
        if (!isFormed() || !workingEnabled) return;
        long intake = Math.min(getTransferRate(), Math.min(inputs.getEnergyStored(), getCapacity() - storedEnergy));
        if (intake > 0) {
            inputPerTick = -inputs.changeEnergy(-intake);
            storedEnergy += inputPerTick;
        }
        long output = Math.min(getTransferRate(), Math.min(storedEnergy,
                outputs.getEnergyCapacity() - outputs.getEnergyStored()));
        if (output > 0) {
            outputPerTick = outputs.changeEnergy(output);
            storedEnergy -= outputPerTick;
        }
        if (inputPerTick != 0 || outputPerTick != 0) markDirty();
    }

    @Override
    public void attachConfigurators(ConfiguratorPanel panel) {
        super.attachConfigurators(panel);
        panel.attachConfigurators(new IFancyConfiguratorButton.Toggle(
                GuiTextures.BUTTON_POWER.getSubTexture(0, 0, 1, 0.5),
                GuiTextures.BUTTON_POWER.getSubTexture(0, 0.5, 1, 0.5),
                this::isWorkingEnabled, (click, enabled) -> setWorkingEnabled(enabled))
                .setTooltipsSupplier(enabled -> List.of(Component.translatable("pollution.machine.battery.enabled", enabled))));
    }

    public static BlockPattern createPattern(MultiblockMachineDefinition definition) {
        return MagicBatteryPatterns.create(definition);
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        textList.add(Component.translatable("pollution.machine.battery.energy", storedEnergy, getCapacity()));
        textList.add(Component.translatable("pollution.machine.battery.tiers", coreTier, coilTier));
        textList.add(Component.translatable("pollution.machine.battery.transfer", inputPerTick, outputPerTick, getTransferRate()));
    }
}
