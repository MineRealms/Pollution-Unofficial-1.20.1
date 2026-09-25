package meowmel.pollution.common.item.bauble;

import meowmel.pollution.Pollution;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * Fall damage reduction of the wings (upstream {@code Jetpack#getFallDamageReduction}:
 * nano 2x, quantum 4x). The worn {@link WingItem} in the Curios back slot divides the
 * fall distance; below the vanilla safe distance the event is cancelled outright.
 */
@Mod.EventBusSubscriber(modid = Pollution.MOD_ID)
public class WingFallEvents {

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide) {
            return;
        }
        float reduction = wornFallDamageReduction(player);
        if (reduction > 1.0F) {
            event.setDistance(event.getDistance() / reduction);
        }
    }

    /** Highest fall damage reduction among worn wings; 1.0 when none is worn. */
    public static float wornFallDamageReduction(Player player) {
        float armorReduction = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST)
                .getItem() instanceof WingItem wing ? wing.getFallDamageReduction() : 1.0F;
        if (!ModList.get().isLoaded("curios")) {
            return armorReduction;
        }
        return CuriosApi.getCuriosInventory(player)
                .map(handler -> {
                    float reduction = armorReduction;
                    for (var stacksHandler : handler.getCurios().values()) {
                        var stacks = stacksHandler.getStacks();
                        for (int i = 0; i < stacks.getSlots(); i++) {
                            if (stacks.getStackInSlot(i).getItem() instanceof WingItem wing) {
                                reduction = Math.max(reduction, wing.getFallDamageReduction());
                            }
                        }
                    }
                    return reduction;
                })
                .orElse(armorReduction);
    }
}
