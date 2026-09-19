package meowmel.pollution.common.machine.part;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import meowmel.pollution.api.capability.ITarotHatch;
import meowmel.pollution.api.recipes.properties.TarotCards;
import net.minecraft.world.item.ItemStack;

/**
 * Non-consumable major-arcana selector for ritual-grade magic recipes.
 *
 * <p>Port of upstream {@code MetaTileEntityTarotHatch}: one filtered focus slot
 * that only accepts tarot cards and is never consumed by recipes. The
 * controller discovers the formed part through {@link ITarotHatch} and hands
 * it to the amplification engine, which reads the active card through
 * {@link #getActiveTarot()}. The slot and focus-lock toggle come from
 * {@link MagicItemHatchMachine}.</p>
 */
public class TarotHatchMachine extends MagicItemHatchMachine implements ITarotHatch {

    public TarotHatchMachine(IMachineBlockEntity holder, int tier) {
        super(holder, tier);
    }

    @Override
    protected boolean isAcceptedStack(ItemStack stack) {
        return TarotCards.isTarot(stack);
    }

    @Override
    public String getActiveTarot() {
        String tarot = TarotCards.getId(getFocusStack());
        return tarot == null ? "" : tarot;
    }

    @Override
    public boolean hasTarot(String tarotId) {
        return TarotCards.matches(getFocusStack(), tarotId);
    }
}
