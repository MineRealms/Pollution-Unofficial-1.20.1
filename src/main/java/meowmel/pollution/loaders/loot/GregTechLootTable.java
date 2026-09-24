package meowmel.pollution.loaders.loot;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.config.ConfigHolder;
import meowmel.pollution.Pollution;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * Modern equivalent of the 1.12 GregTech loot loader.
 *
 * <p>The old loader appended entries to GregTech's {@code ChestGenHooks} list,
 * which only works for tables with a pool named {@code main}.  Vanilla 1.20
 * uses several pool names (usually {@code pool0}), so this port adds one
 * independent, named pool instead.  The pool has one roll and uses the same
 * item counts and weights as upstream.  Missing optional The Betweenlands
 * tables are harmless: Forge simply never emits an event for them.</p>
 */
@Mod.EventBusSubscriber(modid = Pollution.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class GregTechLootTable {

    private static final String POOL_NAME = "pollution:gregtech_1_12_port";

    private static final List<ResourceLocation> TARGETS = List.of(
            // The Betweenlands IDs are intentionally retained.  The mod is not
            // a dependency of this port, therefore absent namespaces are no-op.
            id("thebetweenlands", "loot/loot/common_chest_loot"),
            id("thebetweenlands", "loot/loot/common_pot_loot"),
            id("thebetweenlands", "loot/loot/cragrock_tower_chest"),
            id("thebetweenlands", "loot/loot/cragrock_tower_pot"),
            id("thebetweenlands", "loot/loot/dungeon_chest_loot"),
            id("thebetweenlands", "loot/loot/dungeon_pot_loot"),
            id("thebetweenlands", "loot/loot/weight_fortress_chest"),
            id("thebetweenlands", "loot/loot/weight_fortress_loot"),
            BuiltInLootTables.SPAWN_BONUS_CHEST,
            BuiltInLootTables.END_CITY_TREASURE,
            BuiltInLootTables.SIMPLE_DUNGEON,
            // 1.12 CHESTS_VILLAGE_BLACKSMITH maps to the 1.20 weaponsmith table.
            BuiltInLootTables.VILLAGE_WEAPONSMITH,
            BuiltInLootTables.ABANDONED_MINESHAFT,
            BuiltInLootTables.NETHER_BRIDGE,
            BuiltInLootTables.STRONGHOLD_LIBRARY,
            BuiltInLootTables.STRONGHOLD_CROSSING,
            BuiltInLootTables.STRONGHOLD_CORRIDOR,
            BuiltInLootTables.DESERT_PYRAMID,
            BuiltInLootTables.JUNGLE_TEMPLE,
            BuiltInLootTables.JUNGLE_TEMPLE_DISPENSER,
            BuiltInLootTables.IGLOO_CHEST,
            BuiltInLootTables.WOODLAND_MANSION);

    private GregTechLootTable() {}

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        if (ConfigHolder.INSTANCE == null || !ConfigHolder.INSTANCE.worldgen.addLoot
                || !TARGETS.contains(event.getName())) {
            return;
        }

        LootTable table = event.getTable();
        if (table.getPool(POOL_NAME) != null) {
            return;
        }

        LootPool.Builder pool = LootPool.lootPool()
                .name(POOL_NAME)
                .setRolls(ConstantValue.exactly(1.0F));
        addEntries(pool);
        table.addPool(pool.build());
    }

    private static void addEntries(LootPool.Builder pool) {
        add(pool, ChemicalHelper.get(TagPrefix.pipeSmallFluid, GTMaterials.Copper), 4, 8, 20);
        add(pool, ChemicalHelper.get(TagPrefix.pipeNormalFluid, GTMaterials.Copper), 2, 6, 15);
        add(pool, ChemicalHelper.get(TagPrefix.pipeLargeFluid, GTMaterials.Copper), 1, 5, 10);
        add(pool, ChemicalHelper.get(TagPrefix.pipeSmallItem, GTMaterials.Tin), 4, 8, 20);
        add(pool, ChemicalHelper.get(TagPrefix.pipeNormalItem, GTMaterials.Tin), 2, 6, 15);
        add(pool, ChemicalHelper.get(TagPrefix.pipeLargeItem, GTMaterials.Tin), 1, 5, 10);
        add(pool, ChemicalHelper.get(TagPrefix.ingot, GTMaterials.Invar), 2, 6, 10);
        add(pool, ChemicalHelper.get(TagPrefix.ingot, GTMaterials.Aluminium), 1, 4, 10);

        add(pool, ChemicalHelper.get(TagPrefix.plate, GTMaterials.Tin), 4, 8, 20);
        add(pool, ChemicalHelper.get(TagPrefix.plate, GTMaterials.Copper), 4, 8, 20);
        add(pool, ChemicalHelper.get(TagPrefix.plate, GTMaterials.Iron), 4, 8, 20);
        add(pool, ChemicalHelper.get(TagPrefix.plate, GTMaterials.RedAlloy), 2, 6, 15);
        add(pool, ChemicalHelper.get(TagPrefix.plate, GTMaterials.Lead), 2, 6, 15);
        add(pool, ChemicalHelper.get(TagPrefix.plate, GTMaterials.Gold), 1, 5, 10);
        add(pool, ChemicalHelper.get(TagPrefix.plate, GTMaterials.Silver), 1, 5, 10);

        add(pool, ChemicalHelper.get(TagPrefix.wireGtSingle, GTMaterials.Tin), 4, 8, 20);
        add(pool, ChemicalHelper.get(TagPrefix.wireGtSingle, GTMaterials.Copper), 4, 8, 20);
        add(pool, ChemicalHelper.get(TagPrefix.wireGtSingle, GTMaterials.RedAlloy), 2, 6, 15);
        add(pool, ChemicalHelper.get(TagPrefix.wireGtSingle, GTMaterials.Lead), 2, 6, 15);
        add(pool, ChemicalHelper.get(TagPrefix.wireGtSingle, GTMaterials.Gold), 1, 5, 10);

        add(pool, GTMachines.STEAM_ALLOY_SMELTER.left().asStack(), 1, 2, 5);
        add(pool, GTMachines.STEAM_SOLID_BOILER.left().asStack(), 1, 2, 5);
        add(pool, GTMachines.STEAM_LIQUID_BOILER.left().asStack(), 1, 2, 5);
        add(pool, GTMachines.STEAM_SOLAR_BOILER.left().asStack(), 1, 2, 5);
        add(pool, GTMachines.STEAM_EXTRACTOR.left().asStack(), 1, 2, 5);
        add(pool, GTMachines.STEAM_MACERATOR.left().asStack(), 1, 2, 5);
        add(pool, GTMachines.STEAM_COMPRESSOR.left().asStack(), 1, 2, 5);
        add(pool, GTMachines.STEAM_HAMMER.left().asStack(), 1, 2, 5);
        add(pool, GTMachines.STEAM_FURNACE.left().asStack(), 1, 2, 5);
        add(pool, GTMachines.STEAM_ROCK_CRUSHER.left().asStack(), 1, 2, 5);
        add(pool, GTMachines.STEAM_MINER.left().asStack(), 1, 2, 5);
        add(pool, GTMachines.STEAM_HATCH.asStack(), 1, 2, 10);
        add(pool, GTMachines.STEAM_IMPORT_BUS.asStack(), 1, 2, 10);
        add(pool, GTMachines.STEAM_EXPORT_BUS.asStack(), 1, 2, 10);

        add(pool, new ItemStack(Blocks.PISTON), 2, 4, 15);
        // PISTON_EXTENSION has no item in modern Minecraft and is omitted.
    }

    private static void add(LootPool.Builder pool, ItemStack stack, int min, int max, int weight) {
        if (stack.isEmpty() || weight <= 0 || min < 1 || max < min) {
            return;
        }
        pool.add(LootItem.lootTableItem(stack.getItem())
                .setWeight(weight)
                .apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max))));
    }

    private static ResourceLocation id(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }
}
