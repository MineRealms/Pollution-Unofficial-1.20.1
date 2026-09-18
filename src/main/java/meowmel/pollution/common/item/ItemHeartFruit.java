package meowmel.pollution.common.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Heart fruit: behaviour port of upstream {@code ItemHeartFruit}.
 *
 * <p>Upstream food values (6 hunger, 1.2 saturation, always edible) and the
 * regeneration / strength / nausea effects are carried by the food properties
 * registered in {@code PollutionItems}; this class only owns the on-eat hook.</p>
 *
 * <p>TODO(port): upstream also added 50,000 LP to the eater's Blood Magic soul
 * network. Blood Magic is not part of the 1.20.1 port yet, so that bonus is a
 * documented stub.</p>
 */
public class ItemHeartFruit extends Item {

    public ItemHeartFruit(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide && entity instanceof Player) {
            // TODO(port): Blood Magic soul network bonus (+50000 LP) once that system is ported.
        }
        return result;
    }
}
