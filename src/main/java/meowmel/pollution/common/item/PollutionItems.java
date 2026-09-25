package meowmel.pollution.common.item;

import com.gregtechceu.gtceu.api.GTValues;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import meowmel.pollution.common.item.astral.ConstellationDataItem;
import meowmel.pollution.common.item.astral.CrystalQualityItem;
import meowmel.pollution.common.item.bauble.GogglesItem;
import meowmel.pollution.common.item.bauble.ItemWaterRing;
import meowmel.pollution.common.item.bauble.WingItem;
import meowmel.pollution.compat.gtceu.PollutionGTAddon;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
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
 * {@link #get(String)}. Items with a ported behaviour are separate typed
 * entries: goggles, tarots, the water ring, the heart fruit, the rechargeable
 * batteries, filters, the packaged aura node and the astral data carriers.</p>
 *
 * <p>The remaining plain registry entries are limited to behaviours owned by
 * the explicitly excluded Blood Magic and Astral Sorcery systems. Starstream
 * networking and the wing/jetpack behaviour are implemented by their own
 * ported handlers.</p>
 */
public final class PollutionItems {

    private static final Map<String, ItemEntry<Item>> ITEMS = new LinkedHashMap<>();

    /** Upstream food values: 6 hunger, 1.2 saturation, always edible, with regen / strength / nausea. */
    private static final FoodProperties HEART_FRUIT_FOOD = new FoodProperties.Builder()
            .nutrition(6)
            .saturationMod(1.2F)
            .alwaysEat()
            .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 200, 1), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 0), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 60, 0), 1.0F)
            .build();

    // ////////////////////////////////////
    // ***** behaviour items *****//
    // ////////////////////////////////////

    /** Packaged aura node: fuel of the node machine family, shows its stored NBT. */
    public static final ItemEntry<PackagedAuraNodeItem> PACKAGED_AURA_NODE = register(
            "packaged_aura_node",
            properties -> new PackagedAuraNodeItem(properties.stacksTo(1)),
            "Packaged Aura Node");

    /** Nano goggles: Curios head slot, feeds the wearer, night vision + water breathing, vis discount 5. */
    public static final ItemEntry<GogglesItem> NANO_GOGGLES = register(
            "nano_goggles",
            properties -> new GogglesItem(properties.stacksTo(1), 160_000L, GTValues.LV, 2, false),
            "Nano Goggles");

    /** Quantum goggles: as nano, plus solar recharge while night vision is off. */
    public static final ItemEntry<GogglesItem> QUANTUM_GOGGLES = register(
            "quantum_goggles",
            properties -> new GogglesItem(properties.stacksTo(1), 1_280_000L, GTValues.LV, 2, true),
            "Quantum Goggles");

    /** Water ring: Curios ring, upstream Baubles behaviour (swim boost, night vision, mana air). */
    public static final ItemEntry<ItemWaterRing> BAUBLES_WATER_RING = register(
            "baubles.water_ring",
            properties -> new ItemWaterRing(properties.stacksTo(1), 1, 120_000,
                    "material.pollution.infused_water"),
            "Water Ring");

    /** Heart fruit: food item with upstream effects; the Blood Magic LP bonus is stubbed. */
    public static final ItemEntry<ItemHeartFruit> HEART_FRUIT_I = register(
            "heart_fruit_i",
            properties -> new ItemHeartFruit(properties.food(HEART_FRUIT_FOOD)),
            "Heart Fruit");

    /** The Fool tarot: sneak + right-click teleports to the level spawn (upstream behaviour). */
    public static final ItemEntry<TarotTheFoolItem> TAROT_THE_FOOL = register(
            "the_fool",
            properties -> new TarotTheFoolItem(properties.stacksTo(64)),
            "The Fool");

    /** Celestial calibration core: shows the stored constellation / function data. */
    public static final ItemEntry<ConstellationDataItem> CELESTIAL_CALIBRATION_CORE = register(
            "celestial_calibration_core",
            properties -> new ConstellationDataItem(properties.stacksTo(16)),
            "Celestial Calibration Core");

    /** Constellation data wafer: shows the stored constellation / function data. */
    public static final ItemEntry<ConstellationDataItem> CONSTELLATION_DATA_WAFER = register(
            "constellation_data_wafer",
            properties -> new ConstellationDataItem(properties.stacksTo(16)),
            "Constellation Data Wafer");

    /** Rock crystal seed: shows the stored purity / stability quality data. */
    public static final ItemEntry<CrystalQualityItem> ROCK_CRYSTAL_SEED = register(
            "rock_crystal_seed",
            properties -> new CrystalQualityItem(properties.stacksTo(1)),
            "Rock Crystal Seed");

    /** Celestial crystal embryo: shows the stored purity / stability quality data. */
    public static final ItemEntry<CrystalQualityItem> CELESTIAL_CRYSTAL_EMBRYO = register(
            "celestial_crystal_embryo",
            properties -> new CrystalQualityItem(properties.stacksTo(1)),
            "Celestial Crystal Embryo");

    /** Cultivated crystal: shows quality, grade and optical quality. */
    public static final ItemEntry<CrystalQualityItem> CULTIVATED_CRYSTAL = register(
            "cultivated_crystal",
            properties -> new CrystalQualityItem(properties.stacksTo(1)),
            "Cultivated Crystal");

    // ////////////////////////////////////
    // ***** rechargeable batteries *****//
    // ////////////////////////////////////

    public static final ItemEntry<MagicBatteryItem> MAGIC_BATTERY_LV = magicBattery("battery.lv.magic", 112_500L, GTValues.LV);
    public static final ItemEntry<MagicBatteryItem> MAGIC_BATTERY_MV = magicBattery("battery.mv.magic", 450_000L, GTValues.MV);
    public static final ItemEntry<MagicBatteryItem> MAGIC_BATTERY_HV = magicBattery("battery.hv.magic", 1_800_000L, GTValues.HV);
    public static final ItemEntry<MagicBatteryItem> MAGIC_BATTERY_EV = magicBattery("battery.ev.magic", 7_200_000L, GTValues.EV);
    public static final ItemEntry<MagicBatteryItem> MAGIC_BATTERY_IV = magicBattery("battery.iv.magic", 28_800_000L, GTValues.IV);
    public static final ItemEntry<MagicBatteryItem> MAGIC_BATTERY_LUV = magicBattery("battery.luv.magic", 115_200_000L, GTValues.LuV);
    public static final ItemEntry<MagicBatteryItem> MAGIC_BATTERY_ZPM = magicBattery("battery.zpm.magic", 460_800_000L, GTValues.ZPM);
    public static final ItemEntry<MagicBatteryItem> MAGIC_BATTERY_UV = magicBattery("battery.uv.magic", 1_843_200_000L, GTValues.UV);

    // ////////////////////////////////////
    // ***** filters *****//
    // ////////////////////////////////////

    public static final ItemEntry<FilterItem> FILTER_I = filter("filter.i", 240_000, 1, "material.pollution.infused_earth");
    public static final ItemEntry<FilterItem> FILTER_II = filter("filter.ii", 360_000, 2, "material.pollution.infused_water");
    public static final ItemEntry<FilterItem> FILTER_III = filter("filter.iii", 480_000, 3, "material.pollution.syrmorite");
    public static final ItemEntry<FilterItem> FILTER_IV = filter("filter.iv", 640_000, 4, "material.gtceu.thaumium");
    public static final ItemEntry<FilterItem> FILTER_V = filter("filter.v", 720_000, 5, "material.pollution.octine");

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
    public static final ItemEntry<Item> MAGIC_CIRCUIT_UEV = register("magic_circuit.uev");
    public static final ItemEntry<Item> MAGIC_CIRCUIT_UIV = register("magic_circuit.uiv");
    public static final ItemEntry<Item> MAGIC_CIRCUIT_UXV = register("magic_circuit.uxv");
    public static final ItemEntry<Item> MAGIC_CIRCUIT_OPV = register("magic_circuit.opv");
    public static final ItemEntry<Item> MAGIC_CIRCUIT_MAX = register("magic_circuit.max");

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

    // ////////////////////////////////////
    // ***** plain registry completion *****//
    // ////////////////////////////////////

    // Upstream slimes (plain items).
    public static final ItemEntry<Item> TAR_SLIME = register("tar_slime");
    public static final ItemEntry<Item> SUGAR_SLIME = register("sugar_slime");
    public static final ItemEntry<Item> GLUE_SLIME = register("glue_slime");
    public static final ItemEntry<Item> GLYCEROL_SLIME = register("glycerol_slime");
    public static final ItemEntry<Item> RUBBER_SLIME = register("rubber_slime");

    // Upstream philosophical stone chain (stage 4).
    public static final ItemEntry<Item> STONE_OF_PHILOSOPHER_4 = register("stone_of_philosopher_4");

    // Upstream blood culture templates (plain; Blood Magic is not ported).
    public static final ItemEntry<Item> BLOOD_CIRCUIT = register("blood_circuit");
    public static final ItemEntry<Item> BLOOD_CIRCUIT_ADVANCED = register("blood_circuit_advanced");
    public static final ItemEntry<Item> BLOOD_CIRCUIT_ULTIMATE = register("blood_circuit_ultimate");
    public static final ItemEntry<Item> BLOOD_CIRCUIT_SUPREME = register("blood_circuit_supreme");

    // Upstream blood circuit boards MV..MAX (culture templates, not ore:circuit entries).
    public static final ItemEntry<Item> BLOOD_CIRCUIT_MV = register("blood_circuit.0");
    public static final ItemEntry<Item> BLOOD_CIRCUIT_HV = register("blood_circuit.1");
    public static final ItemEntry<Item> BLOOD_CIRCUIT_EV = register("blood_circuit.2");
    public static final ItemEntry<Item> BLOOD_CIRCUIT_IV = register("blood_circuit.3");
    public static final ItemEntry<Item> BLOOD_CIRCUIT_LUV = register("blood_circuit.4");
    public static final ItemEntry<Item> BLOOD_CIRCUIT_ZPM = register("blood_circuit.5");
    public static final ItemEntry<Item> BLOOD_CIRCUIT_UV = register("blood_circuit.6");
    public static final ItemEntry<Item> BLOOD_CIRCUIT_UHV = register("blood_circuit.7");
    public static final ItemEntry<Item> BLOOD_CIRCUIT_UEV = register("blood_circuit.8");
    public static final ItemEntry<Item> BLOOD_CIRCUIT_UIV = register("blood_circuit.9");
    public static final ItemEntry<Item> BLOOD_CIRCUIT_UXV = register("blood_circuit.10");
    public static final ItemEntry<Item> BLOOD_CIRCUIT_OPV = register("blood_circuit.11");
    public static final ItemEntry<Item> BLOOD_CIRCUIT_MAX = register("blood_circuit.12");

    // Upstream blood processing materials (plain; Blood Magic is not ported).
    public static final ItemEntry<Item> BLOOD_PRIMITIVE_MEAT = register("primitive_meat");
    public static final ItemEntry<Item> BLOOD_RATS_BRAIN = register("rat_brain");
    public static final ItemEntry<Item> BLOOD_MITOCHONDRION_POWER = register("mitochondrion_power");
    public static final ItemEntry<Item> BLOOD_ENDORPHINS_STABILIZER = register("endorphins_stabilizer");
    public static final ItemEntry<Item> BLOOD_FREEZE_COOLER = register("freeze_cooler");
    public static final ItemEntry<Item> BLOOD_LYSOSOME_STABILIZER = register("lysosome_stabilizer");
    public static final ItemEntry<Item> BLOOD_IPS_HUMAN_BRAIN = register("ips_human_brain");
    public static final ItemEntry<Item> BLOOD_PORT = register("blood_port");

    // Magic integration cores / processing intermediates (plain until the astral system lands).
    public static final ItemEntry<Item> ASTRAL_LENS_BASIC = register("astral_lens_basic");
    public static final ItemEntry<Item> ASTRAL_LENS_ADVANCED = register("astral_lens_advanced");
    public static final ItemEntry<Item> HARMONIZING_RUNE_CORE = register("harmonizing_rune_core");
    public static final ItemEntry<Item> ASTRAL_BLOOD_CATALYST = register("astral_blood_catalyst");
    public static final ItemEntry<Item> ASTRAL_NEURAL_BUNDLE = register("astral_neural_bundle");
    public static final ItemEntry<Item> PRIMORDIAL_STAR_BLOOD_CRYSTAL = register("primordial_star_blood_crystal");
    public static final ItemEntry<Item> CAUSALITY_CATALYST = register("causality_catalyst");
    public static final ItemEntry<Item> SILVERED_GLASS_LENS = register("silvered_glass_lens");
    public static final ItemEntry<Item> ASTRAL_RESONANCE_COIL = register("astral_resonance_coil");
    public static final ItemEntry<Item> MANA_RESONANCE_COIL = register("mana_resonance_coil");
    public static final ItemEntry<Item> STERILE_SLATE_BLANK = register("sterile_slate_blank");
    public static final ItemEntry<Item> PRECISION_RUNE_BLANK = register("precision_rune_blank");
    public static final ItemEntry<Item> NATURAL_INFUSED_COIL = register("natural_infused_coil");
    public static final ItemEntry<Item> MAGIC_CONTROL_ASSEMBLY = register("magic_control_assembly_ev");
    public static final ItemEntry<Item> NODE_STABILIZATION_FRAME = register("node_stabilization_frame");
    public static final ItemEntry<Item> BLANK_TAROT_CARD = register("blank_tarot_card");
    public static final ItemEntry<Item> ARCANE_INK_CAPSULE = register("arcane_ink_capsule");

    // Animated magic circuit boards, one per voltage tier.
    public static final ItemEntry<Item> MAGIC_CIRCUIT_BOARD_ULV = register("magic_circuit_board.ulv");
    public static final ItemEntry<Item> MAGIC_CIRCUIT_BOARD_LV = register("magic_circuit_board.lv");
    public static final ItemEntry<Item> MAGIC_CIRCUIT_BOARD_MV = register("magic_circuit_board.mv");
    public static final ItemEntry<Item> MAGIC_CIRCUIT_BOARD_HV = register("magic_circuit_board.hv");
    public static final ItemEntry<Item> MAGIC_CIRCUIT_BOARD_EV = register("magic_circuit_board.ev");
    public static final ItemEntry<Item> MAGIC_CIRCUIT_BOARD_IV = register("magic_circuit_board.iv");
    public static final ItemEntry<Item> MAGIC_CIRCUIT_BOARD_LUV = register("magic_circuit_board.luv");
    public static final ItemEntry<Item> MAGIC_CIRCUIT_BOARD_ZPM = register("magic_circuit_board.zpm");
    public static final ItemEntry<Item> MAGIC_CIRCUIT_BOARD_UV = register("magic_circuit_board.uv");
    public static final ItemEntry<Item> MAGIC_CIRCUIT_BOARD_UHV = register("magic_circuit_board.uhv");
    public static final ItemEntry<Item> MAGIC_CIRCUIT_BOARD_UEV = register("magic_circuit_board.uev");
    public static final ItemEntry<Item> MAGIC_CIRCUIT_BOARD_UIV = register("magic_circuit_board.uiv");
    public static final ItemEntry<Item> MAGIC_CIRCUIT_BOARD_UXV = register("magic_circuit_board.uxv");
    public static final ItemEntry<Item> MAGIC_CIRCUIT_BOARD_OPV = register("magic_circuit_board.opv");
    public static final ItemEntry<Item> MAGIC_CIRCUIT_BOARD_MAX = register("magic_circuit_board.max");

    // Recipe-first functional carriers.
    public static final ItemEntry<Item> ATTUNED_CRYSTAL_WAFER = register("attuned_crystal_wafer");
    public static final ItemEntry<Item> LIVING_MAGIC_BIOFILM = register("living_magic_biofilm");
    public static final ItemEntry<Item> DEPLETED_MAGIC_CORE = register("depleted_magic_core");

    /** Starstream linker: mode toggle + tooltips ported; binding deferred with the starstream network. */
    public static final ItemEntry<StarstreamLinkerItem> STARSTREAM_LINKER = register(
            "starstream_linker",
            properties -> new StarstreamLinkerItem(properties.stacksTo(1)),
            "Starstream Linker");

    /** Magic sweep: creative flight + damage immunity while carried (see {@link MagicSweepEvents}). */
    public static final ItemEntry<Item> MAGIC_SWEEP = register("magic_sweep");

    /** Nano wings: Curios back slot, electric flight + Speed II buff, 2x fall damage reduction. */
    public static final ItemEntry<WingItem> WING_NANO = register(
            "wing_nano",
            properties -> new WingItem(properties.stacksTo(1), 6_400_000L, GTValues.HV, 30, false, 2.0F),
            "Nano Wings");

    /** Quantum wings: as nano with haste/jump buffs and 4x fall reduction (upstream {@code MetaArmor}). */
    public static final ItemEntry<WingItem> WING_QUANTUM = register(
            "wing_quantum",
            properties -> new WingItem(properties.stacksTo(1), 25_600_000L, GTValues.IV, 120, true, 4.0F),
            "Quantum Wings");

    // Utilities.
    public static final ItemEntry<Item> PESTICIDE_EMPTY = register("pesticide.empty");
    public static final ItemEntry<Item> PESTICIDE = register("pesticide.full");
    public static final ItemEntry<Item> DEVAY_PILL_EMPTY = register("devay_pill.empty");
    public static final ItemEntry<Item> DEVAY_PILL_1 = register("devay_pill.1");
    public static final ItemEntry<Item> DEVAY_PILL_5 = register("devay_pill.5");
    public static final ItemEntry<Item> DEVAY_PILL_10 = register("devay_pill.10");
    public static final ItemEntry<Item> DEVAY_PILL_20 = register("devay_pill.20");

    // Tarots (plain, the Fool carries the teleport behaviour).
    public static final ItemEntry<Item> TEST_ITEM = register("test_item");
    public static final ItemEntry<Item> TAROT_THE_MAGICIAN = register("the_magician");
    public static final ItemEntry<Item> TAROT_THE_HIGH_PRIESTESS = register("the_high_priestess");
    public static final ItemEntry<Item> TAROT_THE_EMPRESS = register("the_empress");
    public static final ItemEntry<Item> TAROT_THE_EMPEROR = register("the_emperor");
    public static final ItemEntry<Item> TAROT_THE_HIGHOPHANT = register("the_highophant");
    public static final ItemEntry<Item> TAROT_THE_LOVERS = register("the_lovers");
    public static final ItemEntry<Item> TAROT_THE_CHARIOT = register("the_chariot");
    public static final ItemEntry<Item> TAROT_THE_STRENGTH = register("the_strength");
    public static final ItemEntry<Item> TAROT_THE_HERMIT = register("the_hermit");
    public static final ItemEntry<Item> TAROT_THE_WHEEL_OF_FORTUNE = register("the_wheel_of_fortune");
    public static final ItemEntry<Item> TAROT_JUSTICE = register("the_justice");
    public static final ItemEntry<Item> TAROT_THE_HANGED_MAN = register("the_hanged_man");
    public static final ItemEntry<Item> TAROT_DEATH = register("the_death");
    public static final ItemEntry<Item> TAROT_TEMPERANCE = register("the_temperance");
    public static final ItemEntry<Item> TAROT_THE_DEVIL = register("the_devil");
    public static final ItemEntry<Item> TAROT_THE_TOWER = register("the_tower");
    public static final ItemEntry<Item> TAROT_THE_STAR = register("the_star");
    public static final ItemEntry<Item> TAROT_THE_MOON = register("the_moon");
    public static final ItemEntry<Item> TAROT_THE_SUN = register("the_sun");
    public static final ItemEntry<Item> TAROT_JUDGEMENT = register("the_judgement");
    public static final ItemEntry<Item> TAROT_THE_WORLD = register("the_world");

    public static final ItemEntry<Item> TEST = register("test");

    static {
        for (String name : new String[]{
                "core_of_idea", "bottle_of_phlogistonic_oneness", "auto_elenchus_device",
                "elucidator_of_four_causes", "symptomatic_vis_data_link",
                "needle_of_mystic_interpellation", "cogito_defibrillator", "ball_in_itself",
                "stone_of_philosopher_1", "stone_of_philosopher_2", "stone_of_philosopher_3",
                "stone_of_philosopher_final"}) {
            register(name);
        }
    }

    // ////////////////////////////////////
    // ***** helpers *****//
    // ////////////////////////////////////

    private static ItemEntry<MagicBatteryItem> magicBattery(String name, long maxCharge, int tier) {
        return register(name, properties -> new MagicBatteryItem(properties.stacksTo(1), maxCharge, tier),
                displayName(name));
    }

    private static ItemEntry<FilterItem> filter(String name, int maxDurability, int filterTier, String materialKey) {
        return register(name, properties -> new FilterItem(properties.stacksTo(1), maxDurability, filterTier, materialKey),
                displayName(name));
    }

    private static ItemEntry<Item> register(String name) {
        ItemEntry<Item> entry = PollutionGTAddon.REGISTRATE
                .item(name, Item::new)
                .lang(displayName(name))
                .register();
        ITEMS.put(name, entry);
        return entry;
    }

    private static <T extends Item> ItemEntry<T> register(String name, NonNullFunction<Item.Properties, T> factory, String lang) {
        return PollutionGTAddon.REGISTRATE
                .item(name, factory)
                .lang(lang)
                .register();
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
