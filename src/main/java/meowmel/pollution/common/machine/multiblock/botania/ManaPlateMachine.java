package meowmel.pollution.common.machine.multiblock.botania;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;

import java.util.List;

/**
 * Mana plate: consumes pure mana to accelerate machines sitting on top of it.
 *
 * <p>Upstream origin: {@code MetaTileEntityManaPlate} (1.12.2). Upstream formed
 * an 11x11 mana-plate floor, exposed a throttle (0..mana tier) and called
 * {@code update()} on every {@code MetaTileEntity} above the 11x11 area
 * {@code speed} extra times per tick, paying {@code 2^(speed-1)} mana per
 * accelerated machine.</p>
 *
 * <p>Deviations:</p>
 * <ul>
 *   <li>Modern GregTech machines tick through a final {@code serverTick}, so
 *       extra ticks cannot be injected. The port advances the recipe progress
 *       of working GregTech machines in the area instead, which is the
 *       observable effect of the upstream acceleration.</li>
 *   <li>The upstream throttle flex buttons are ported as clickable display
 *       buttons: the display shows the current throttle plus {@code [-]}/{@code [+]},
 *       clamped to {@code 1..mana hatch tier} (upstream's {@code 0} = off step
 *       is dropped, the plate is never completely disabled). The setting is
 *       persisted in NBT and survives structure changes. A plate that was never
 *       throttled defaults to full speed, so existing setups keep the old
 *       behaviour.</li>
 *   <li>Non-GregTech tile entities are not accelerated.</li>
 * </ul>
 */
public class ManaPlateMachine extends AbstractManaControlMachine implements IDisplayUIMachine {

    private static final int AREA_RADIUS = 5;
    private static final String ACTION_THROTTLE_DOWN = "pollution.mana_plate.throttle_down";
    private static final String ACTION_THROTTLE_UP = "pollution.mana_plate.throttle_up";

    private int speed;
    private int speedMax = 1;
    private TickableSubscription tickSubscription;

    public ManaPlateMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            tickSubscription = subscribeServerTick(this::tickPlate);
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

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        speedMax = Math.max(1, getManaHandler().getTier());
        if (speed <= 0) {
            speed = speedMax;
        } else {
            speed = Math.min(speed, speedMax);
        }
    }

    private void tickPlate() {
        if (!(getLevel() instanceof ServerLevel) || !isFormed() || speed <= 0) {
            return;
        }
        Direction facing = getFrontFacing().getOpposite();
        BlockPos center = getPos().offset(facing.getStepX() * AREA_RADIUS, 0, facing.getStepZ() * AREA_RADIUS);
        long cost = manaCost();
        for (int x = -AREA_RADIUS; x <= AREA_RADIUS; x++) {
            for (int z = -AREA_RADIUS; z <= AREA_RADIUS; z++) {
                BlockPos target = center.offset(x, 1, z);
                if (!(getLevel().getBlockEntity(target) instanceof IMachineBlockEntity machineHolder)) {
                    continue;
                }
                MetaMachine machine = machineHolder.getMetaMachine();
                if (!(machine instanceof IRecipeLogicMachine logicMachine)) {
                    continue;
                }
                RecipeLogic logic = logicMachine.getRecipeLogic();
                if (logic == null || !logic.isWorking()) {
                    continue;
                }
                if (!consumeMana(cost, false)) {
                    return;
                }
                logic.setProgress(logic.getProgress() + speed);
            }
        }
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (isFormed()) {
            textList.add(Component.translatable("pollution.machine.mana_plate.tier",
                    getManaHandler().getTier(), getMana(), getMaxMana()));
            textList.add(Component.translatable("pollution.machine.mana_plate.speed",
                    speed, manaCost()));
            textList.add(throttleButtons());
        }
    }

    private Component throttleButtons() {
        MutableComponent text = Component.translatable("pollution.machine.mana_plate.throttle_modify");
        text.append(" ");
        text.append(ComponentPanelWidget.withButton(Component.literal("[-]"), ACTION_THROTTLE_DOWN));
        text.append(" ");
        text.append(ComponentPanelWidget.withButton(Component.literal("[+]"), ACTION_THROTTLE_UP));
        return text;
    }

    @Override
    public void handleDisplayClick(String componentData, ClickData clickData) {
        if (clickData.isRemote) {
            return;
        }
        if (ACTION_THROTTLE_DOWN.equals(componentData)) {
            setThrottle(speed - 1);
        } else if (ACTION_THROTTLE_UP.equals(componentData)) {
            setThrottle(speed + 1);
        }
    }

    public int getThrottle() {
        return speed;
    }

    public void setThrottle(int value) {
        int clamped = Math.max(1, Math.min(speedMax, value));
        if (clamped != speed) {
            speed = clamped;
            markDirty();
        }
    }

    private long manaCost() {
        return speed <= 1 ? 1L : 1L << (speed - 1);
    }

    @Override
    public void saveCustomPersistedData(CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        tag.putInt("PlateSpeed", speed);
    }

    @Override
    public void loadCustomPersistedData(CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        speed = tag.getInt("PlateSpeed");
    }

    public static BlockPattern createPattern(MultiblockMachineDefinition definition) {
        return ManaPlatePatterns.create(definition);
    }
}
