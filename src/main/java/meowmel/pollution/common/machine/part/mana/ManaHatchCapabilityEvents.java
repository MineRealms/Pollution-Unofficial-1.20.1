package meowmel.pollution.common.machine.part.mana;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import meowmel.pollution.Pollution;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import vazkii.botania.api.mana.ManaReceiver;

/**
 * Attaches Botania's mana receiver capability to Pollution mana hatches.
 *
 * <p>Deviation: 1.12 Botania tested {@code instanceof IManaReceiver} on the
 * tile entity, while the 1.20.1 API is capability-based. The port therefore
 * registers a provider for every machine that implements {@link ManaReceiver},
 * which covers all four mana hatch parts.</p>
 */
@Mod.EventBusSubscriber(modid = Pollution.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ManaHatchCapabilityEvents {

    private static final ResourceLocation MANA_RECEIVER_ID = ResourceLocation
            .fromNamespaceAndPath(Pollution.MOD_ID, "mana_receiver");

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<BlockEntity> event) {
        if (event.getObject() instanceof IMachineBlockEntity holder
                && holder.getMetaMachine() instanceof ManaReceiver receiver) {
            event.addCapability(MANA_RECEIVER_ID, new ManaReceiverCapabilityProvider(receiver));
        }
    }

    private ManaHatchCapabilityEvents() {}
}
