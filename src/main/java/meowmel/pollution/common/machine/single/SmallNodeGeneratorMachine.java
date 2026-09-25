package meowmel.pollution.common.machine.single;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import meowmel.pollution.common.gui.MachineGuiWidgets;
import meowmel.pollution.common.item.PackagedAuraNode;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Small node generator: burns a packaged aura node for EU, tiers LuV..UHV.
 *
 * <p>Upstream origin: {@code MetaTileEntitySmallNodeGenerator} (1.12.2, single
 * block, registered as {@code pollution_small_node_generator.luv} ..
 * {@code .uhv}). Every tick it read the node stack from its one-slot inventory,
 * multiplied a base capacity of 8192 by the node tier/type/essence multiplier
 * and the machine tier, and added the result to its energy container
 * ({@code V[tier] * 64} buffer, 1A emitter).</p>
 *
 * <p>Deviations:</p>
 * <ul>
 *   <li>Upstream stored the capacity multiplier and kept generating at the last
 *       node's rate after the node was removed (the multiplier was never
 *       reset). The port only generates while a valid packaged aura node sits
 *       in the slot.</li>
 *   <li>Upstream used the GT MetaItem {@code PACKAGED_AURA_NODE} with a
 *       metadata value; the port uses the plain {@code packaged_aura_node} item
 *       through the shared {@link PackagedAuraNode} NBT contract.</li>
 *   <li>The screen is ported (2026-09-19): the fancy UI shows the node
 *       tier/type and the capacity multiplier and exposes the fuel slot; the
 *       slot is also an input-only item capability, so it can be loaded with
 *       hoppers or pipes.</li>
 * </ul>
 */
public class SmallNodeGeneratorMachine extends PollutionEnergyMachine implements com.gregtechceu.gtceu.api.machine.feature.IMachineLife {

    private static final com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder FIELDS =
            new com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder(SmallNodeGeneratorMachine.class, MANAGED_FIELD_HOLDER);
    @Override public com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder getFieldHolder() { return FIELDS; }
    @Override public void onMachineRemoved() { clearInventory(inventory.storage); }

    private static final float BASIC_CAPACITY = 8192.0F;

    @com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
    private final NotifiableItemStackHandler inventory;

    public SmallNodeGeneratorMachine(IMachineBlockEntity info, int tier) {
        super(info, tier);
        this.inventory = new NotifiableItemStackHandler(this, 1, IO.IN, IO.IN);
    }

    @Override
    protected boolean isEnergyEmitter() {
        return true;
    }

    @Override
    protected void pollutionTick() {
        ItemStack stack = inventory.getStackInSlot(0);
        if (!PackagedAuraNode.isNode(stack)) {
            return;
        }
        float capacity = BASIC_CAPACITY * nodeCapacityMultiplier(stack) * getTier();
        if (capacity > 0.0F) {
            energyContainer.addEnergy((long) capacity);
        }
    }

    /**
     * Upstream multiplier tables: node tier (Withering 0.25, Pale 0.5, Bright
     * 4.0), node type (Ominous/Pure 4.0, Concussive 8.0, Voracious 16.0),
     * entropy penalty {@code max(1.2 - 0.005 * entropy, 0)} and the
     * fire/order geometric bonus.
     */
    private static float nodeCapacityMultiplier(ItemStack node) {
        float multiplier = 1.0F;
        switch (PackagedAuraNode.tier(node)) {
            case "Withering" -> multiplier *= 0.25F;
            case "Pale" -> multiplier *= 0.5F;
            case "Bright" -> multiplier *= 4.0F;
            default -> {}
        }
        switch (PackagedAuraNode.type(node)) {
            case "Ominous", "Pure" -> multiplier *= 4.0F;
            case "Concussive" -> multiplier *= 8.0F;
            case "Voracious" -> multiplier *= 16.0F;
            default -> {}
        }
        multiplier *= Math.max(1.2F - 0.005F
                * PackagedAuraNode.essence(node, PackagedAuraNode.ESSENCE_ENTROPY), 0.0F);
        multiplier *= (float) (1.0D + 0.02D * Math.sqrt(
                PackagedAuraNode.essence(node, PackagedAuraNode.ESSENCE_FIRE)
                        * PackagedAuraNode.essence(node, PackagedAuraNode.ESSENCE_ORDER)));
        return multiplier;
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        ItemStack node = inventory.getStackInSlot(0);
        if (PackagedAuraNode.isNode(node)) {
            textList.add(Component.literal("Node: " + PackagedAuraNode.tier(node) + " "
                    + PackagedAuraNode.type(node)));
            textList.add(Component.literal("Capacity Multiplier: "
                    + String.format("%.2f", nodeCapacityMultiplier(node) * getTier())));
        } else {
            textList.add(Component.literal("Node: none"));
        }
    }

    @Override
    public Widget createUIWidget() {
        var group = (WidgetGroup) super.createUIWidget();
        group.addWidget(MachineGuiWidgets.itemSlot(inventory, 0, 116, 78));
        return group;
    }
}
