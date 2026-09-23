package meowmel.pollution.common.machine.single;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import dev.tc4port.thaumcraft.api.aspect.VisAction;
import meowmel.pollution.PollutionConfig;
import meowmel.pollution.compat.tc4r.TC4RBridge;
import meowmel.pollution.common.gui.MachineGuiWidgets;
import meowmel.pollution.common.item.FilterItem;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Flux scrubber: consumes EU to remove Thaumcraft flux near the machine.
 *
 * <p>Port of upstream {@code MetaTileEntityFluxClear} (single block). Like the
 * upstream machine it requires an air filter cartridge in its input slot and
 * wears it by 1 durability point per scrubbing tick
 * ({@code FilterBehavior#applyDamage}); without a filter the machine idles.
 * The rate formula {@code 2^(tier-1) * fluxScrubberMultiplier} matches
 * upstream; the filter tier only affects durability, not the rate.</p>
 */
public class FluxScrubberMachine extends PollutionEnergyMachine {

    private final NotifiableItemStackHandler filterInventory;

    private double scrubBuffer;

    public FluxScrubberMachine(IMachineBlockEntity info, int tier) {
        super(info, tier);
        this.filterInventory = new NotifiableItemStackHandler(this, 1, IO.BOTH, IO.BOTH);
    }

    @Override
    protected void pollutionTick() {
        if (!(getLevel() instanceof ServerLevel level)) {
            return;
        }
        ItemStack filter = filterInventory.getStackInSlot(0);
        FilterItem filterItem = FilterItem.getInstanceFor(filter);
        if (filterItem == null) {
            scrubBuffer = 0.0D;
            return;
        }
        if (TC4RBridge.scrubFlux(level, getPos(), 1, VisAction.SIMULATE) <= 0) {
            scrubBuffer = 0.0D;
            return;
        }
        long cost = GTValues.VA[getTier()];
        if (energyContainer.getEnergyStored() < cost) {
            return;
        }
        scrubBuffer += Math.pow(2, getTier() - 1) * PollutionConfig.FLUX_SCRUBBER_MULTIPLIER.get();
        int quanta = (int) scrubBuffer;
        if (quanta <= 0) {
            return;
        }
        int removed = TC4RBridge.scrubFlux(level, getPos(), quanta);
        if (removed <= 0) {
            return;
        }
        scrubBuffer -= removed;
        energyContainer.removeEnergy(cost);
        filterItem.applyDamage(filterInventory.getStackInSlot(0), 1);
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        ItemStack filter = filterInventory.getStackInSlot(0);
        FilterItem filterItem = FilterItem.getInstanceFor(filter);
        if (filterItem != null) {
            textList.add(Component.translatable("pollution.flux_scrubber.filter",
                    filterItem.getPartMaxDurability(filter) - filterItem.getPartDamage(filter),
                    filterItem.getPartMaxDurability(filter)));
        } else {
            textList.add(Component.translatable("pollution.flux_scrubber.filter_none"));
        }
        textList.add(Component.literal("Scrub Buffer: " + String.format("%.2f", scrubBuffer)));
    }

    @Override
    public Widget createUIWidget() {
        var group = (WidgetGroup) super.createUIWidget();
        group.addWidget(MachineGuiWidgets.itemSlot(filterInventory, 0, 116, 78));
        return group;
    }
}
