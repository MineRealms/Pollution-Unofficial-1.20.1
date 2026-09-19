package meowmel.pollution.common.machine.single;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import meowmel.pollution.client.gui.MachineGuiWidgets;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.List;

/**
 * Shared ticking base for the ported single-block magic machines.
 *
 * <p>Upstream machines overrode {@code MetaTileEntity#update()}; in GregTech CEu
 * Modern {@code MetaMachine#serverTick()} is final, so machines subscribe a
 * server tick callback from {@code onLoad} instead.</p>
 *
 * <p>The base also provides the fancy screen shared by all of them: a display
 * panel with the machine status and a GT energy bar, following
 * {@code SimpleTieredMachine}'s fancy UI conventions. Subclasses add their own
 * lines by overriding {@link #addDisplayText(List)}.</p>
 */
public abstract class PollutionEnergyMachine extends TieredEnergyMachine implements IFancyUIMachine {

    private TickableSubscription tickSubscription;

    protected PollutionEnergyMachine(IMachineBlockEntity info, int tier) {
        super(info, tier);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            tickSubscription = subscribeServerTick(this::pollutionTick);
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

    protected abstract void pollutionTick();

    // ////////////////////////////////////
    // ***** UI *****//
    // ////////////////////////////////////

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(198, 208, this, entityPlayer).widget(new FancyMachineUIWidget(this, 198, 208));
    }

    @Override
    public Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 190, 104);
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        group.addWidget(new ImageWidget(4, 4, 138, 96, GuiTextures.DISPLAY));
        group.addWidget(new LabelWidget(8, 8, self().getBlockState().getBlock().getDescriptionId()));
        group.addWidget(new ComponentPanelWidget(8, 20, this::addDisplayText)
                .textSupplier(isRemote() ? null : this::addDisplayText)
                .setMaxWidthLimit(128));
        group.addWidget(MachineGuiWidgets.energyBar(energyContainer, 146, 4, 18, 96));
        return group;
    }

    /** Status lines shown on the machine screen; subclasses append their own. */
    public void addDisplayText(List<Component> textList) {
        textList.add(Component.literal("Energy: " + energyContainer.getEnergyStored() + " / "
                + energyContainer.getEnergyCapacity() + " EU"));
    }
}
