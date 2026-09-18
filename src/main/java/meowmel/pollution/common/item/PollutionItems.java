package meowmel.pollution.common.item;

import com.tterrag.registrate.util.entry.ItemEntry;
import meowmel.pollution.compat.gtceu.PollutionGTAddon;
import net.minecraft.world.item.Item;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Items of the Pollution port.
 *
 * <p>The 1.12 mod used GregTech's MetaItem system. The port registers plain
 * items under the upstream registry names (dots kept, e.g.
 * {@code pollution:magic_circuit.lv}) and looks them up through
 * {@link #get(String)}. Special behaviours (filters, pills, baubles) arrive
 * with the item-behaviour pass.</p>
 */
public final class PollutionItems {

    private static final Map<String, ItemEntry<Item>> ITEMS = new LinkedHashMap<>();

    /** Packaged aura node: the fuel/catalyst of the node machine family. */
    public static final ItemEntry<Item> PACKAGED_AURA_NODE = register("packaged_aura_node");

    // ////////////////////////////////////
    // ***** registered set *****//
    // ////////////////////////////////////

    public static final ItemEntry<Item> MAGIC_BATTERY_HULL_LV = register("magic_battery.hull.lv");
    public static final ItemEntry<Item> MAGIC_BATTERY_HULL_MV = register("magic_battery.hull.mv");
    public static final ItemEntry<Item> MAGIC_BATTERY_HULL_HV = register("magic_battery.hull.hv");
    public static final ItemEntry<Item> MAGIC_BATTERY_HULL_EV = register("magic_battery.hull.ev");
    public static final ItemEntry<Item> MAGIC_BATTERY_HULL_IV = register("magic_battery.hull.iv");
    public static final ItemEntry<Item> MAGIC_BATTERY_HULL_LUV = register("magic_battery.hull.luv");
    public static final ItemEntry<Item> MAGIC_BATTERY_HULL_ZPM = register("magic_battery.hull.zpm");
    public static final ItemEntry<Item> MAGIC_BATTERY_HULL_UV = register("magic_battery.hull.uv");

    public static final ItemEntry<Item> MAGIC_CIRCUIT_ULV = register("magic_circuit.ulv");
    public static final ItemEntry<Item> MAGIC_CIRCUIT_LV = register("magic_circuit.lv");
    public static final ItemEntry<Item> MAGIC_CIRCUIT_MV = register("magic_circuit.mv");
    public static final ItemEntry<Item> MAGIC_CIRCUIT_HV = register("magic_circuit.hv");
    public static final ItemEntry<Item> MAGIC_CIRCUIT_EV = register("magic_circuit.ev");
    public static final ItemEntry<Item> MAGIC_CIRCUIT_IV = register("magic_circuit.iv");
    public static final ItemEntry<Item> MAGIC_CIRCUIT_LUV = register("magic_circuit.luv");
    public static final ItemEntry<Item> MAGIC_CIRCUIT_ZPM = register("magic_circuit.zpm");
    public static final ItemEntry<Item> MAGIC_CIRCUIT_UV = register("magic_circuit.uv");
    public static final ItemEntry<Item> MAGIC_CIRCUIT_UHV = register("magic_circuit.uhv");

    public static final ItemEntry<Item> FILTER_I = register("filter.i");
    public static final ItemEntry<Item> FILTER_II = register("filter.ii");
    public static final ItemEntry<Item> FILTER_III = register("filter.iii");
    public static final ItemEntry<Item> FILTER_IV = register("filter.iv");
    public static final ItemEntry<Item> FILTER_V = register("filter.v");

    public static final ItemEntry<Item> BLANK_CATALYST_CORE = register("blank_catalyst_core");
    public static final ItemEntry<Item> HOT_CATALYST_CORE = register("hot_catalyst_core");
    public static final ItemEntry<Item> COLD_CATALYST_CORE = register("cold_catalyst_core");
    public static final ItemEntry<Item> INTEGRATION_CATALYST_CORE = register("integration_catalyst_core");
    public static final ItemEntry<Item> SEGREGATION_CATALYST_CORE = register("segregation_catalyst_core");
    public static final ItemEntry<Item> COKING_CATALYST_CORE = register("coking_catalyst_core");
    public static final ItemEntry<Item> EVOLUTION_CATALYST_CORE = register("evolution_catalyst_core");

    public static final ItemEntry<Item> WHITE_RUNE = register("white_rune");
    public static final ItemEntry<Item> BLACK_RUNE = register("black_rune");
    public static final ItemEntry<Item> STARRY_RUNE = register("starry_rune");

    public static final ItemEntry<VisCheckerItem> VIS_CHECKER = PollutionGTAddon.REGISTRATE
            .item("vis_checker", VisCheckerItem::new)
            .lang("Vis Checker")
            .register();
    public static final ItemEntry<Item> ENERGY_REDUCE = register("energy_reduce");
    public static final ItemEntry<Item> TIME_INCREASE = register("time_increase");
    public static final ItemEntry<Item> OVERCLOCKING_ENHANCE = register("overclocking_enhance");
    public static final ItemEntry<Item> PARALLEL_ENHANCE = register("parallel_enhance");
    public static final ItemEntry<Item> TRANSFORM_ENHANCE = register("transform_enhance");

    private static ItemEntry<Item> register(String name) {
        ItemEntry<Item> entry = PollutionGTAddon.REGISTRATE
                .item(name, Item::new)
                .lang(displayName(name))
                .register();
        ITEMS.put(name, entry);
        return entry;
    }

    /** Lookup by upstream registry name (e.g. {@code "magic_circuit.lv"}). */
    public static ItemEntry<Item> get(String name) {
        return ITEMS.get(name);
    }

    public static Map<String, ItemEntry<Item>> all() {
        return Collections.unmodifiableMap(ITEMS);
    }

    private static String displayName(String name) {
        StringBuilder display = new StringBuilder();
        for (String part : name.split("[._]")) {
            if (display.length() > 0) {
                display.append(' ');
            }
            display.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return display.toString();
    }

    /** Forces class initialisation from the mod constructor (own mod bus). */
    public static void init() {}

    private PollutionItems() {}
}
