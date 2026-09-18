package meowmel.pollution.common.machine.part.mana;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.mana.ManaReceiver;

/**
 * Exposes a Pollution mana hatch as a Botania mana receiver.
 *
 * <p>Botania 1.20.1 resolves mana receivers through the Forge capability
 * {@link BotaniaForgeCapabilities#MANA_RECEIVER} instead of the 1.12
 * {@code instanceof IManaReceiver} test, so every mana part is wrapped by this
 * provider; see {@link ManaHatchCapabilityEvents}.</p>
 */
public class ManaReceiverCapabilityProvider implements ICapabilityProvider {

    private final LazyOptional<ManaReceiver> receiver;

    public ManaReceiverCapabilityProvider(ManaReceiver receiver) {
        this.receiver = LazyOptional.of(() -> receiver);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        if (capability == BotaniaForgeCapabilities.MANA_RECEIVER) {
            return BotaniaForgeCapabilities.MANA_RECEIVER.orEmpty(capability, receiver);
        }
        return LazyOptional.empty();
    }
}
