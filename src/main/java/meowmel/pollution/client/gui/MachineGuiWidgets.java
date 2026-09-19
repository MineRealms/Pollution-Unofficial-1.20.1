package meowmel.pollution.client.gui;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.gui.widget.TankWidget;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib.gui.widget.ProgressWidget;

import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.function.DoubleSupplier;

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
}
