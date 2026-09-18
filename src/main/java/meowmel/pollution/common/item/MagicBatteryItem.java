package meowmel.pollution.common.item;

import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.component.ElectricStats;

/**
 * Magic rechargeable battery: behaviour port of upstream
 * {@code PollutionBattery}'s {@code ElectricStats.createRechargeableBattery}.
 *
 * <p>The eight tiered batteries use the same capacities and tiers as upstream
 * (112,500 EU at LV doubling each tier up to 1,843,200,000 EU at UV) and are
 * exposed to GregTech machines through the standard {@code IElectricItem}
 * capability, so they can be charged in a charger and discharged in battery
 * buffers exactly like GT batteries. The charge bar and charge tooltip come
 * from {@link ElectricStats}, as on GT's own batteries.</p>
 */
public class MagicBatteryItem extends ComponentItem {

    public MagicBatteryItem(Properties properties, long maxCharge, int tier) {
        super(properties);
        attachComponents(ElectricStats.createRechargeableBattery(maxCharge, tier));
    }
}
