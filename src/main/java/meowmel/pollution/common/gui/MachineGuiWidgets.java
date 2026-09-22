package meowmel.pollution.common.gui;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.gui.widget.TankWidget;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.ProgressWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

/**
 * Shared LDLib / GregTech widgets for the ported machine screens.
 *
 * <p>These helpers only assemble widgets exactly like GregTech's own machines
 * do (see {@code TieredEnergyMachine#createEnergyBar}, {@code SimpleTieredMachine}
 * and {@code MultiblockTankMachine}). The classes referenced here are common
 * code: GregTech builds the same widgets on the server when a UI is opened, so
 * nothing client-only is touched.</p>
 */
public final class MachineGuiWidgets {

    private MachineGuiWidgets() {}

    /** Vertical energy bar bound to an energy container (GT's own bar style). */
    public static ProgressWidget energyBar(NotifiableEnergyContainer container, int x, int y, int width, int height) {
        return fractionBar(() -> container.getEnergyCapacity() <= 0L ? 0.0D :
                (double) container.getEnergyStored() / (double) container.getEnergyCapacity(),
                x, y, width, height);
    }

    /** Horizontal/vertical recipe progress bar driven by a {@link RecipeLogic}. */
    public static ProgressWidget progressBar(RecipeLogic logic, int x, int y, int width, int height) {
        return new ProgressWidget(logic::getProgressPercent, x, y, width, height, GuiTextures.PROGRESS_BAR_ARROW);
    }

    /** Generic vertical fill bar using the GT energy bar textures. */
    public static ProgressWidget fractionBar(DoubleSupplier supplier, int x, int y, int width, int height) {
        ProgressWidget bar = new ProgressWidget(supplier, x, y, width, height,
                new ProgressTexture(IGuiTexture.EMPTY, GuiTextures.ENERGY_BAR_BASE));
        bar.setFillDirection(ProgressTexture.FillDirection.DOWN_TO_UP);
        bar.setBackground(GuiTextures.ENERGY_BAR_BACKGROUND);
        return bar;
    }

    /** Vertical fill bar using the GT fluid tank textures. */
    public static ProgressWidget fluidBar(DoubleSupplier supplier, int x, int y, int width, int height) {
        ProgressWidget bar = new ProgressWidget(supplier, x, y, width, height,
                new ProgressTexture(IGuiTexture.EMPTY, GuiTextures.FLUID_TANK_OVERLAY));
        bar.setFillDirection(ProgressTexture.FillDirection.DOWN_TO_UP);
        bar.setBackground(GuiTextures.FLUID_TANK_BACKGROUND);
        return bar;
    }

    /** GT fluid tank slot bound to a Forge fluid handler. */
    public static TankWidget fluidTank(IFluidHandler handler, int tank, int x, int y, int width, int height) {
        TankWidget widget = new TankWidget(handler, tank, x, y, width, height, true, true);
        widget.setBackground(GuiTextures.FLUID_SLOT);
        widget.setFillDirection(ProgressTexture.FillDirection.DOWN_TO_UP);
        return widget;
    }

    /** GT item slot bound to a Forge item handler. */
    public static SlotWidget itemSlot(IItemHandlerModifiable handler, int slot, int x, int y) {
        SlotWidget widget = new SlotWidget(handler, slot, x, y, true, true);
        widget.setBackground(GuiTextures.SLOT);
        return widget;
    }

    /**
     * GT-style status panel: DISPLAY background, a title line, stacked text
     * lines and a vertical fill bar on the right. Used by the mana / vis
     * hatches whose buffers are not Forge fluids and therefore cannot use a
     * {@link TankWidget}.
     */
    public static WidgetGroup infoPanel(int width, int height, Supplier<String> title,
                                        List<Supplier<String>> lines, DoubleSupplier fillFraction) {
        WidgetGroup group = new WidgetGroup(0, 0, width, height);
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        group.addWidget(new ImageWidget(4, 4, width - 28, height - 8, GuiTextures.DISPLAY));
        group.addWidget(new LabelWidget(8, 8, title));
        int lineY = 20;
        for (Supplier<String> line : lines) {
            group.addWidget(new LabelWidget(8, lineY, line));
            lineY += 12;
        }
        group.addWidget(fractionBar(fillFraction, width - 22, 4, 14, height - 8));
        return group;
    }
}
