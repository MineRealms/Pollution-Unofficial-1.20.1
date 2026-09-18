package meowmel.pollution.loaders.recipes;

import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.utils.GTUtil;
import meowmel.pollution.Pollution;
import meowmel.pollution.common.machine.multiblock.botania.BotaniaRecipeMaps;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Native Botania recipe transplant for the mana recipe maps.
 *
 * <p>Upstream ({@code meowmel.pollution.loaders.recipes.mods.Botania}, 1.12.2)
 * iterated Botania's runtime recipe lists
 * ({@code BotaniaAPI.pureDaisyRecipes/petalRecipes/runeAltarRecipes/
 * manaInfusionRecipes}) and registered one Pollution recipe per entry. Botania
 * 1.20.1 has no such runtime lists: its recipes are datapack JSONs shipped in
 * {@code data/botania/recipes/}. The port therefore transplants that data
 * verbatim from the Botania jar into the four maps re-created in
 * {@link BotaniaRecipeMaps}:</p>
 * <ul>
 *   <li>{@code petal_apothecary/} -&gt; {@code MANA_PETAL_RECIPES}</li>
 *   <li>{@code runic_altar/} -&gt; {@code MANA_RUNE_ALTAR_RECIPES}</li>
 *   <li>{@code mana_infusion/} -&gt; {@code MANA_INFUSION_RECIPES}</li>
 *   <li>{@code pure_daisy/} -&gt; {@code PURE_DAISY_RECIPES}</li>
 * </ul>
 *
 * <p>Upstream's numeric conversion is kept 1:1: petal/pure-daisy recipes run at
 * {@code EUt(100)} for {@code 200} ticks, runic-altar and mana-infusion recipes
 * at {@code EUt(mana)} for {@code 200 * GTUtil.getTierByVoltage(mana)} ticks.
 * The upstream converter paid Botania's mana through the recipe EUt and never
 * set the reserved {@code pollution.magic.mana_per_tick} property, so the
 * transplant does the same; hand-authored Pollution recipes (the custom rune
 * altars) remain the ones that draw from mana hatches.</p>
 *
 * <p>Items are resolved by registry name at recipe-build time through
 * {@link ForgeRegistries}, because the transplant covers ~200 entries of
 * Botania's own data files and registry names are exactly what those files
 * contain. Tag ingredients keep Botania's tags ({@code botania:petals/*},
 * {@code botania:mana_dusts}, {@code botania:manasteel_ingots}, ...). Any id
 * missing from the loaded registries (removed vanilla item, unported addon)
 * makes the affected recipe log and skip instead of crashing the data load.</p>
 *
 * <p>The petal-apothecary {@code reagent} entry
 * ({@code botania:seed_apothecary_reagent}) is ignored, matching the upstream
 * converter, which iterated only {@code RecipePetals#getInputs()}.</p>
 *
 * <p>Skipped entries (3): {@code petal_apothecary/vazkii_head} (NBT player-head
 * output), {@code runic_altar/head} ({@code runic_altar_head} special type with
 * alternative ingredients and a player-head output) and
 * {@code pure_daisy/snow_block} (input is the water fluid block, which has no
 * item form).</p>
 */
public final class BotaniaNativeRecipes {

    private BotaniaNativeRecipes() {}

    public static void init(Consumer<FinishedRecipe> provider) {
        petalApothecary(provider);
        runicAltar(provider);
        manaInfusion(provider);
        pureDaisy(provider);
    }

    private static void petalApothecary(Consumer<FinishedRecipe> provider) {
        petal(provider, "agricarnation", "botania:agricarnation", 1, "#botania:petals/lime",
                "#botania:petals/lime", "#botania:petals/green", "#botania:petals/yellow",
                "botania:rune_spring", "botania:redstone_root");
        petal(provider, "bellethorn", "botania:bellethorn", 1, "#botania:petals/red", "#botania:petals/red",
                "#botania:petals/red", "#botania:petals/cyan", "#botania:petals/cyan", "botania:redstone_root");
        petal(provider, "bergamute", "botania:bergamute", 1, "#botania:petals/orange", "#botania:petals/green",
                "#botania:petals/green", "botania:redstone_root");
        petal(provider, "bubbell", "botania:bubbell", 1, "#botania:petals/cyan", "#botania:petals/cyan",
                "#botania:petals/light_blue", "#botania:petals/light_blue", "#botania:petals/blue",
                "#botania:petals/blue", "botania:rune_water", "botania:rune_summer", "botania:pixie_dust");
        petal(provider, "clayconia", "botania:clayconia", 1, "#botania:petals/light_gray",
                "#botania:petals/light_gray", "#botania:petals/gray", "#botania:petals/cyan",
                "botania:rune_earth");
        petal(provider, "daffomill", "botania:daffomill", 1, "#botania:petals/white", "#botania:petals/white",
                "#botania:petals/brown", "#botania:petals/yellow", "botania:rune_air", "botania:redstone_root");
        petal(provider, "dandelifeon", "botania:dandelifeon", 1, "#botania:petals/purple",
                "#botania:petals/purple", "#botania:petals/lime", "#botania:petals/green",
                "botania:rune_water", "botania:rune_fire", "botania:rune_earth", "botania:rune_air",
                "botania:redstone_root", "botania:life_essence");
        petal(provider, "daybloom_motif", "botania:daybloom_motif", 1, "#botania:petals/yellow",
                "#botania:petals/yellow", "#botania:petals/orange", "#botania:petals/light_blue");
        petal(provider, "dreadthorn", "botania:dreadthorn", 1, "#botania:petals/black",
                "#botania:petals/black", "#botania:petals/black", "#botania:petals/cyan",
                "#botania:petals/cyan", "botania:redstone_root");
        petal(provider, "endoflame", "botania:endoflame", 1, "#botania:petals/brown", "#botania:petals/brown",
                "#botania:petals/red", "#botania:petals/light_gray");
        petal(provider, "entropinnyum", "botania:entropinnyum", 1, "#botania:petals/red",
                "#botania:petals/red", "#botania:petals/gray", "#botania:petals/gray", "#botania:petals/white",
                "#botania:petals/white", "botania:rune_wrath", "botania:rune_fire");
        petal(provider, "exoflame", "botania:exoflame", 1, "#botania:petals/red", "#botania:petals/red",
                "#botania:petals/gray", "#botania:petals/light_gray", "botania:rune_fire",
                "botania:rune_summer");
        petal(provider, "fallen_kanade", "botania:fallen_kanade", 1, "#botania:petals/white",
                "#botania:petals/white", "#botania:petals/yellow", "#botania:petals/yellow",
                "#botania:petals/orange", "botania:rune_spring");
        petal(provider, "gourmaryllis", "botania:gourmaryllis", 1, "#botania:petals/light_gray",
                "#botania:petals/light_gray", "#botania:petals/yellow", "#botania:petals/yellow",
                "#botania:petals/red", "botania:rune_fire", "botania:rune_summer");
        petal(provider, "heisei_dream", "botania:heisei_dream", 1, "#botania:petals/magenta",
                "#botania:petals/magenta", "#botania:petals/purple", "#botania:petals/pink",
                "botania:rune_wrath", "botania:pixie_dust");
        petal(provider, "hopperhock", "botania:hopperhock", 1, "#botania:petals/gray", "#botania:petals/gray",
                "#botania:petals/light_gray", "#botania:petals/light_gray", "botania:rune_air",
                "botania:redstone_root");
        petal(provider, "hyacidus", "botania:hyacidus", 1, "#botania:petals/purple", "#botania:petals/purple",
                "#botania:petals/magenta", "#botania:petals/magenta", "#botania:petals/green",
                "botania:rune_water", "botania:rune_autumn", "botania:redstone_root");
        petal(provider, "hydroangeas", "botania:hydroangeas", 1, "#botania:petals/blue",
                "#botania:petals/blue", "#botania:petals/cyan", "#botania:petals/cyan");
        petal(provider, "jaded_amaranthus", "botania:jaded_amaranthus", 1, "#botania:petals/purple",
                "#botania:petals/lime", "#botania:petals/green", "botania:rune_spring", "botania:redstone_root");
        petal(provider, "jiyuulia", "botania:jiyuulia", 1, "#botania:petals/pink", "#botania:petals/pink",
                "#botania:petals/purple", "#botania:petals/light_gray", "botania:rune_water",
                "botania:rune_air");
        petal(provider, "kekimurus", "botania:kekimurus", 1, "#botania:petals/white", "#botania:petals/white",
                "#botania:petals/orange", "#botania:petals/orange", "#botania:petals/brown",
                "#botania:petals/brown", "botania:rune_gluttony", "botania:pixie_dust");
        petal(provider, "labellia", "botania:labellia", 1, "#botania:petals/yellow", "#botania:petals/yellow",
                "#botania:petals/blue", "#botania:petals/white", "#botania:petals/black",
                "botania:rune_autumn", "botania:redstone_root", "botania:pixie_dust");
        petal(provider, "loonium", "botania:loonium", 1, "#botania:petals/green", "#botania:petals/green",
                "#botania:petals/green", "#botania:petals/green", "#botania:petals/gray", "botania:rune_sloth",
                "botania:rune_gluttony", "botania:rune_envy", "botania:redstone_root", "botania:pixie_dust");
        petal(provider, "manastar", "botania:manastar", 1, "#botania:petals/light_blue",
                "#botania:petals/green", "#botania:petals/red", "#botania:petals/cyan");
        petal(provider, "marimorphosis", "botania:marimorphosis", 1, "#botania:petals/gray",
                "#botania:petals/yellow", "#botania:petals/green", "#botania:petals/red", "botania:rune_earth",
                "botania:rune_fire", "botania:redstone_root");
        petal(provider, "medumone", "botania:medumone", 1, "#botania:petals/brown", "#botania:petals/brown",
                "#botania:petals/gray", "#botania:petals/gray", "botania:rune_earth", "botania:redstone_root");
        petal(provider, "munchdew", "botania:munchdew", 1, "#botania:petals/lime", "#botania:petals/lime",
                "#botania:petals/red", "#botania:petals/red", "#botania:petals/green", "botania:rune_gluttony");
        petal(provider, "narslimmus", "botania:narslimmus", 1, "#botania:petals/lime", "#botania:petals/lime",
                "#botania:petals/green", "#botania:petals/green", "#botania:petals/black",
                "botania:rune_summer", "botania:rune_water");
        petal(provider, "nightshade_motif", "botania:nightshade_motif", 1, "#botania:petals/black",
                "#botania:petals/black", "#botania:petals/purple", "#botania:petals/gray");
        petal(provider, "orechid", "botania:orechid", 1, "#botania:petals/gray", "#botania:petals/gray",
                "#botania:petals/yellow", "#botania:petals/green", "#botania:petals/red", "botania:rune_pride",
                "botania:rune_greed", "botania:redstone_root", "botania:pixie_dust"); // GoG variant skipped
        petal(provider, "orechid_ignem", "botania:orechid_ignem", 1, "#botania:petals/red",
                "#botania:petals/red", "#botania:petals/white", "#botania:petals/white",
                "#botania:petals/pink", "botania:rune_pride", "botania:rune_greed", "botania:redstone_root",
                "botania:pixie_dust");
        petal(provider, "pollidisiac", "botania:pollidisiac", 1, "#botania:petals/red", "#botania:petals/red",
                "#botania:petals/pink", "#botania:petals/pink", "#botania:petals/orange", "botania:rune_lust",
                "botania:rune_fire");
        petal(provider, "pure_daisy", "botania:pure_daisy", 1, "#botania:petals/white",
                "#botania:petals/white", "#botania:petals/white", "#botania:petals/white");
        petal(provider, "rafflowsia", "botania:rafflowsia", 1, "#botania:petals/purple",
                "#botania:petals/purple", "#botania:petals/green", "#botania:petals/green",
                "#botania:petals/black", "botania:rune_earth", "botania:rune_pride", "botania:pixie_dust");
        petal(provider, "rannuncarpus", "botania:rannuncarpus", 1, "#botania:petals/orange",
                "#botania:petals/orange", "#botania:petals/yellow", "botania:rune_earth",
                "botania:redstone_root");
        petal(provider, "rosa_arcana", "botania:rosa_arcana", 1, "#botania:petals/pink",
                "#botania:petals/pink", "#botania:petals/purple", "#botania:petals/purple",
                "#botania:petals/lime", "botania:rune_mana");
        petal(provider, "shulk_me_not", "botania:shulk_me_not", 1, "#botania:petals/purple",
                "#botania:petals/purple", "#botania:petals/magenta", "#botania:petals/magenta",
                "#botania:petals/light_gray", "botania:life_essence", "botania:rune_envy", "botania:rune_wrath");
        petal(provider, "solegnolia", "botania:solegnolia", 1, "#botania:petals/brown",
                "#botania:petals/brown", "#botania:petals/red", "#botania:petals/blue", "botania:redstone_root");
        petal(provider, "spectranthemum", "botania:spectranthemum", 1, "#botania:petals/white",
                "#botania:petals/white", "#botania:petals/light_gray", "#botania:petals/light_gray",
                "#botania:petals/cyan", "botania:rune_envy", "botania:rune_water", "botania:redstone_root",
                "botania:pixie_dust");
        petal(provider, "spectrolus", "botania:spectrolus", 1, "#botania:petals/red", "#botania:petals/red",
                "#botania:petals/green", "#botania:petals/green", "#botania:petals/blue",
                "#botania:petals/blue", "#botania:petals/white", "#botania:petals/white",
                "botania:rune_winter", "botania:rune_air", "botania:pixie_dust");
        petal(provider, "tangleberrie", "botania:tangleberrie", 1, "#botania:petals/cyan",
                "#botania:petals/cyan", "#botania:petals/gray", "#botania:petals/light_gray",
                "botania:rune_air", "botania:rune_earth");
        petal(provider, "thermalily", "botania:thermalily", 1, "#botania:petals/red", "#botania:petals/orange",
                "#botania:petals/orange", "botania:rune_earth", "botania:rune_fire");
        petal(provider, "tigerseye", "botania:tigerseye", 1, "#botania:petals/yellow", "#botania:petals/brown",
                "#botania:petals/orange", "#botania:petals/lime", "botania:rune_autumn");
        petal(provider, "vinculotus", "botania:vinculotus", 1, "#botania:petals/black",
                "#botania:petals/black", "#botania:petals/purple", "#botania:petals/purple",
                "#botania:petals/green", "botania:rune_water", "botania:rune_sloth", "botania:rune_lust",
                "botania:redstone_root");
        // 44 recipes ported
    }

    private static void runicAltar(Consumer<FinishedRecipe> provider) {
        runeAltar(provider, "air", "botania:rune_air", 2, 5200, "#botania:mana_dusts",
                "#botania:manasteel_ingots", "#minecraft:wool_carpets", "minecraft:feather", "minecraft:string");
        runeAltar(provider, "autumn", "botania:rune_autumn", 1, 8000, "botania:rune_fire", "botania:rune_air",
                "#minecraft:leaves", "#minecraft:leaves", "#minecraft:leaves", "minecraft:spider_eye");
        runeAltar(provider, "earth", "botania:rune_earth", 2, 5200, "#botania:mana_dusts",
                "#botania:manasteel_ingots", "minecraft:stone", "minecraft:coal_block",
                "minecraft:brown_mushroom|minecraft:red_mushroom");
        runeAltar(provider, "envy", "botania:rune_envy", 1, 12000, "#botania:mana_diamond_gems",
                "#botania:mana_diamond_gems", "botania:rune_winter", "botania:rune_water");
        runeAltar(provider, "fire", "botania:rune_fire", 2, 5200, "#botania:mana_dusts",
                "#botania:manasteel_ingots", "minecraft:nether_brick", "minecraft:gunpowder",
                "minecraft:nether_wart");
        runeAltar(provider, "gluttony", "botania:rune_gluttony", 1, 12000, "#botania:mana_diamond_gems",
                "#botania:mana_diamond_gems", "botania:rune_winter", "botania:rune_fire");
        runeAltar(provider, "greed", "botania:rune_greed", 1, 12000, "#botania:mana_diamond_gems",
                "#botania:mana_diamond_gems", "botania:rune_spring", "botania:rune_water");
        runeAltar(provider, "lust", "botania:rune_lust", 1, 12000, "#botania:mana_diamond_gems",
                "#botania:mana_diamond_gems", "botania:rune_summer", "botania:rune_air");
        runeAltar(provider, "mana", "botania:rune_mana", 1, 8000, "#botania:manasteel_ingots",
                "#botania:manasteel_ingots", "#botania:manasteel_ingots", "#botania:manasteel_ingots",
                "#botania:manasteel_ingots", "botania:mana_pearl");
        runeAltar(provider, "pride", "botania:rune_pride", 1, 12000, "#botania:mana_diamond_gems",
                "#botania:mana_diamond_gems", "botania:rune_summer", "botania:rune_fire");
        runeAltar(provider, "sloth", "botania:rune_sloth", 1, 12000, "#botania:mana_diamond_gems",
                "#botania:mana_diamond_gems", "botania:rune_autumn", "botania:rune_air");
        runeAltar(provider, "spring", "botania:rune_spring", 1, 8000, "botania:rune_water",
                "botania:rune_fire", "#minecraft:saplings", "#minecraft:saplings", "#minecraft:saplings",
                "minecraft:wheat");
        runeAltar(provider, "summer", "botania:rune_summer", 1, 8000, "botania:rune_earth", "botania:rune_air",
                "#minecraft:sand", "#minecraft:sand", "minecraft:slime_ball", "minecraft:melon_slice");
        runeAltar(provider, "water", "botania:rune_water", 2, 5200, "#botania:mana_dusts",
                "#botania:manasteel_ingots", "minecraft:bone_meal", "minecraft:sugar_cane",
                "minecraft:fishing_rod");
        runeAltar(provider, "winter", "botania:rune_winter", 1, 8000, "botania:rune_water",
                "botania:rune_earth", "minecraft:snow_block", "minecraft:snow_block", "#minecraft:wool",
                "minecraft:cake");
        runeAltar(provider, "wrath", "botania:rune_wrath", 1, 12000, "#botania:mana_diamond_gems",
                "#botania:mana_diamond_gems", "botania:rune_winter", "botania:rune_earth");
        // 16 recipes ported
    }

    private static void manaInfusion(Consumer<FinishedRecipe> provider) {
        infusion(provider, "acacia_leaves_dupe", "minecraft:acacia_leaves", "minecraft:acacia_leaves", 2, 2000,
                "botania:conjuration_catalyst");
        infusion(provider, "acacia_log_to_dark_oak_log", "minecraft:acacia_log", "minecraft:dark_oak_log", 1,
                40, "botania:alchemy_catalyst");
        infusion(provider, "acacia_sapling_to_dark_oak_sapling", "minecraft:acacia_sapling",
                "minecraft:dark_oak_sapling", 1, 120, "botania:alchemy_catalyst");
        infusion(provider, "agricarnation_chibi", "botania:agricarnation", "botania:agricarnation_chibi", 1,
                2500, "botania:alchemy_catalyst");
        infusion(provider, "allium_to_azure_bluet", "minecraft:allium", "minecraft:azure_bluet", 1, 400,
                "botania:alchemy_catalyst");
        infusion(provider, "andesite_to_diorite", "minecraft:andesite", "minecraft:diorite", 1, 200,
                "botania:alchemy_catalyst");
        infusion(provider, "apple_to_sweet_berries", "minecraft:apple", "minecraft:sweet_berries", 1, 240,
                "botania:alchemy_catalyst");
        infusion(provider, "azalea_leaves_dupe", "minecraft:azalea_leaves", "minecraft:azalea_leaves", 2, 2000,
                "botania:conjuration_catalyst");
        infusion(provider, "azure_bluet_to_red_tulip", "minecraft:azure_bluet", "minecraft:red_tulip", 1, 400,
                "botania:alchemy_catalyst");
        infusion(provider, "beetroot_seeds_to_melon_seeds", "minecraft:beetroot_seeds",
                "minecraft:melon_seeds", 1, 6000, "botania:alchemy_catalyst");
        infusion(provider, "bellethorn_chibi", "botania:bellethorn", "botania:bellethorn_chibi", 1, 2500,
                "botania:alchemy_catalyst");
        infusion(provider, "birch_leaves_dupe", "minecraft:birch_leaves", "minecraft:birch_leaves", 2, 2000,
                "botania:conjuration_catalyst");
        infusion(provider, "birch_log_to_jungle_log", "minecraft:birch_log", "minecraft:jungle_log", 1, 40,
                "botania:alchemy_catalyst");
        infusion(provider, "birch_sapling_to_jungle_sapling", "minecraft:birch_sapling",
                "minecraft:jungle_sapling", 1, 120, "botania:alchemy_catalyst");
        infusion(provider, "blaze_quartz_deconstruct", "botania:blaze_quartz", "botania:quartz_blaze", 4, 25,
                "botania:alchemy_catalyst");
        infusion(provider, "blaze_rod_to_nether_wart", "minecraft:blaze_rod", "minecraft:nether_wart", 1, 4000,
                "botania:alchemy_catalyst");
        infusion(provider, "blue_orchid_to_allium", "minecraft:blue_orchid", "minecraft:allium", 1, 400,
                "botania:alchemy_catalyst");
        infusion(provider, "book_to_name_tag", "minecraft:writable_book", "minecraft:name_tag", 1, 6000,
                "botania:alchemy_catalyst");
        infusion(provider, "brick_deconstruct", "minecraft:bricks", "minecraft:brick", 4, 25,
                "botania:alchemy_catalyst");
        infusion(provider, "bubbell_chibi", "botania:bubbell", "botania:bubbell_chibi", 1, 2500,
                "botania:alchemy_catalyst");
        infusion(provider, "cactus_to_slime", "minecraft:cactus", "minecraft:slime_ball", 1, 1200,
                "botania:alchemy_catalyst");
        infusion(provider, "calcite_to_deepslate", "minecraft:calcite", "minecraft:deepslate", 1, 200,
                "botania:alchemy_catalyst");
        infusion(provider, "carrot_to_beetroot_seeds", "minecraft:carrot", "minecraft:beetroot_seeds", 1, 6000,
                "botania:alchemy_catalyst");
        infusion(provider, "cherry_leaves_dupe", "minecraft:cherry_leaves", "minecraft:cherry_leaves", 2, 2000,
                "botania:conjuration_catalyst");
        infusion(provider, "cherry_log_to_oak_log", "minecraft:cherry_log", "minecraft:oak_log", 1, 40,
                "botania:alchemy_catalyst");
        infusion(provider, "cherry_sapling_to_oak_sapling", "minecraft:cherry_sapling",
                "minecraft:oak_sapling", 1, 120, "botania:alchemy_catalyst");
        infusion(provider, "chiseled_stone_bricks", "minecraft:stone_bricks",
                "minecraft:chiseled_stone_bricks", 1, 150, "botania:alchemy_catalyst");
        infusion(provider, "chorus_fruit_to_flower", "minecraft:popped_chorus_fruit",
                "minecraft:chorus_flower", 1, 10000, "botania:alchemy_catalyst");
        infusion(provider, "clay_deconstruct", "minecraft:clay", "minecraft:clay_ball", 4, 25,
                "botania:alchemy_catalyst");
        infusion(provider, "clayconia_chibi", "botania:clayconia", "botania:clayconia_chibi", 1, 2500,
                "botania:alchemy_catalyst");
        infusion(provider, "coal_dupe", "minecraft:coal", "minecraft:coal", 2, 2100,
                "botania:conjuration_catalyst");
        infusion(provider, "coarse_dirt", "minecraft:dirt", "minecraft:coarse_dirt", 1, 120,
                "botania:alchemy_catalyst");
        infusion(provider, "cobble_to_sand", "minecraft:cobblestone", "minecraft:sand", 1, 50,
                "botania:alchemy_catalyst");
        infusion(provider, "cocoa_beans_to_wheat_seeds", "minecraft:cocoa_beans", "minecraft:wheat_seeds", 1,
                6000, "botania:alchemy_catalyst");
        infusion(provider, "cod_to_salmon", "minecraft:cod", "minecraft:salmon", 1, 200,
                "botania:alchemy_catalyst");
        infusion(provider, "cornflower_to_lily_of_the_valley", "minecraft:cornflower",
                "minecraft:lily_of_the_valley", 1, 400, "botania:alchemy_catalyst");
        infusion(provider, "dandelion_to_poppy", "minecraft:dandelion", "minecraft:poppy", 1, 400,
                "botania:alchemy_catalyst");
        infusion(provider, "dark_oak_leaves_dupe", "minecraft:dark_oak_leaves", "minecraft:dark_oak_leaves", 2,
                2000, "botania:conjuration_catalyst");
        infusion(provider, "dark_oak_log_to_mangrove_log", "minecraft:dark_oak_log", "minecraft:mangrove_log",
                1, 40, "botania:alchemy_catalyst");
        infusion(provider, "dark_oak_sapling_to_mangrove_propagule", "minecraft:dark_oak_sapling",
                "minecraft:mangrove_propagule", 1, 120, "botania:alchemy_catalyst");
        infusion(provider, "dark_quartz_deconstruct", "botania:dark_quartz", "botania:quartz_dark", 4, 25,
                "botania:alchemy_catalyst");
        infusion(provider, "dead_bush_to_grass", "minecraft:dead_bush", "minecraft:grass", 1, 500,
                "botania:alchemy_catalyst");
        infusion(provider, "deepslate_to_tuff", "minecraft:deepslate", "minecraft:tuff", 1, 200,
                "botania:alchemy_catalyst");
        infusion(provider, "diorite_to_granite", "minecraft:diorite", "minecraft:granite", 1, 200,
                "botania:alchemy_catalyst");
        infusion(provider, "dripleaf_shrinking", "minecraft:big_dripleaf", "minecraft:small_dripleaf", 1, 500,
                "botania:alchemy_catalyst");
        infusion(provider, "elf_quartz_deconstruct", "botania:elf_quartz", "botania:quartz_elven", 4, 25,
                "botania:alchemy_catalyst");
        infusion(provider, "ender_pearl_from_ghast_tear", "minecraft:ghast_tear", "minecraft:ender_pearl", 1,
                28000, "botania:alchemy_catalyst");
        infusion(provider, "fern_to_dead_bush", "minecraft:fern", "minecraft:dead_bush", 1, 500,
                "botania:alchemy_catalyst");
        infusion(provider, "flint_to_gunpowder", "minecraft:flint", "minecraft:gunpowder", 1, 200,
                "botania:alchemy_catalyst");
        infusion(provider, "flowering_azalea_leaves_dupe", "minecraft:flowering_azalea_leaves",
                "minecraft:flowering_azalea_leaves", 2, 2000, "botania:conjuration_catalyst");
        infusion(provider, "glow_berries_to_apple", "minecraft:glow_berries", "minecraft:apple", 1, 240,
                "botania:alchemy_catalyst");
        infusion(provider, "glowstone_deconstruct", "minecraft:glowstone", "minecraft:glowstone_dust", 4, 25,
                "botania:alchemy_catalyst");
        infusion(provider, "glowstone_dupe", "minecraft:glowstone_dust", "minecraft:glowstone_dust", 2, 5000,
                "botania:conjuration_catalyst");
        infusion(provider, "glowstone_dust_to_redstone", "minecraft:glowstone_dust", "minecraft:redstone", 1,
                300, "botania:alchemy_catalyst");
        infusion(provider, "granite_to_andesite", "minecraft:granite", "minecraft:andesite", 1, 200,
                "botania:alchemy_catalyst");
        infusion(provider, "grass", "minecraft:grass", "minecraft:grass", 2, 800,
                "botania:conjuration_catalyst");
        infusion(provider, "grass_seeds", "minecraft:grass", "botania:grass_seeds", 1, 2500, null);
        infusion(provider, "grass_to_fern", "minecraft:grass", "minecraft:fern", 1, 500,
                "botania:alchemy_catalyst");
        infusion(provider, "gravel_dupe", "minecraft:gravel", "minecraft:gravel", 2, 720,
                "botania:conjuration_catalyst");
        infusion(provider, "gunpowder_to_flint", "minecraft:gunpowder", "minecraft:flint", 1, 200,
                "botania:alchemy_catalyst");
        infusion(provider, "hopperhock_chibi", "botania:hopperhock", "botania:hopperhock_chibi", 1, 2500,
                "botania:alchemy_catalyst");
        infusion(provider, "hydroangeas_motif", "botania:hydroangeas", "botania:hydroangeas_motif", 1, 2500,
                "botania:alchemy_catalyst");
        infusion(provider, "ice", "minecraft:snow_block", "minecraft:ice", 1, 2250, "botania:alchemy_catalyst");
        infusion(provider, "jiyuulia_chibi", "botania:jiyuulia", "botania:jiyuulia_chibi", 1, 2500,
                "botania:alchemy_catalyst");
        infusion(provider, "jungle_leaves_dupe", "minecraft:jungle_leaves", "minecraft:jungle_leaves", 2, 2000,
                "botania:conjuration_catalyst");
        infusion(provider, "jungle_log_to_acacia_log", "minecraft:jungle_log", "minecraft:acacia_log", 1, 40,
                "botania:alchemy_catalyst");
        infusion(provider, "jungle_sapling_to_acacia_sapling", "minecraft:jungle_sapling",
                "minecraft:acacia_sapling", 1, 120, "botania:alchemy_catalyst");
        infusion(provider, "lavender_quartz_deconstruct", "botania:lavender_quartz", "botania:quartz_lavender",
                4, 25, "botania:alchemy_catalyst");
        infusion(provider, "lilac_to_rose_bush", "minecraft:lilac", "minecraft:rose_bush", 1, 400,
                "botania:alchemy_catalyst");
        infusion(provider, "lily_of_the_valley_to_sunflower", "minecraft:lily_of_the_valley",
                "minecraft:sunflower", 1, 400, "botania:alchemy_catalyst");
        infusion(provider, "lily_pad_to_vine", "minecraft:lily_pad", "minecraft:vine", 1, 320,
                "botania:alchemy_catalyst");
        infusion(provider, "mana_bottle", "minecraft:glass_bottle", "botania:mana_bottle", 1, 5000, null);
        infusion(provider, "mana_cookie", "minecraft:cookie", "botania:mana_cookie", 1, 20000, null);
        infusion(provider, "mana_diamond", "minecraft:diamond", "botania:mana_diamond", 1, 10000, null);
        infusion(provider, "mana_diamond_block", "minecraft:diamond_block", "botania:mana_diamond_block", 1,
                90000, null);
        infusion(provider, "mana_glass", "minecraft:glass", "botania:mana_glass", 1, 150, null);
        infusion(provider, "mana_pearl", "minecraft:ender_pearl", "botania:mana_pearl", 1, 6000, null);
        infusion(provider, "mana_powder_dust",
                "minecraft:gunpowder|minecraft:redstone|minecraft:glowstone_dust|minecraft:sugar",
                "botania:mana_powder", 1, 500, null);
        infusion(provider, "mana_powder_dye",
                "minecraft:white_dye|minecraft:light_gray_dye|minecraft:gray_dye|minecraft:black_dye|minecraft:brown_dye|minecraft:red_dye|minecraft:orange_dye|minecraft:yellow_dye|minecraft:lime_dye|minecraft:green_dye|minecraft:cyan_dye|minecraft:light_blue_dye|minecraft:blue_dye|minecraft:purple_dye|minecraft:magenta_dye|minecraft:pink_dye",
                "botania:mana_powder", 1, 400, null);
        infusion(provider, "mana_quartz", "minecraft:quartz", "botania:quartz_mana", 1, 250, null);
        infusion(provider, "mana_quartz_deconstruct", "botania:mana_quartz", "botania:quartz_mana", 4, 25,
                "botania:alchemy_catalyst");
        infusion(provider, "mana_string", "minecraft:string", "botania:mana_string", 1, 1250, null);
        infusion(provider, "manasteel", "minecraft:iron_ingot", "botania:manasteel_ingot", 1, 3000, null);
        infusion(provider, "manasteel_block", "minecraft:iron_block", "botania:manasteel_block", 1, 27000, null);
        infusion(provider, "mangrove_leaves_dupe", "minecraft:mangrove_leaves", "minecraft:mangrove_leaves", 2,
                2000, "botania:conjuration_catalyst");
        infusion(provider, "mangrove_log_to_cherry_log", "minecraft:mangrove_log", "minecraft:cherry_log", 1,
                40, "botania:alchemy_catalyst");
        infusion(provider, "mangrove_propagule_to_cherry_sapling", "minecraft:mangrove_propagule",
                "minecraft:cherry_sapling", 1, 120, "botania:alchemy_catalyst");
        infusion(provider, "marimorphosis_chibi", "botania:marimorphosis", "botania:marimorphosis_chibi", 1,
                2500, "botania:alchemy_catalyst");
        infusion(provider, "melon_seeds_to_pumpkin_seeds", "minecraft:melon_seeds", "minecraft:pumpkin_seeds",
                1, 6000, "botania:alchemy_catalyst");
        infusion(provider, "mycel_seeds", "minecraft:red_mushroom|minecraft:brown_mushroom",
                "botania:mycelium_seeds", 1, 6500, null);
        infusion(provider, "netherrack_dupe", "minecraft:netherrack", "minecraft:netherrack", 2, 200,
                "botania:conjuration_catalyst");
        infusion(provider, "oak_leaves_dupe", "minecraft:oak_leaves", "minecraft:oak_leaves", 2, 2000,
                "botania:conjuration_catalyst");
        infusion(provider, "oak_log_to_spruce_log", "minecraft:oak_log", "minecraft:spruce_log", 1, 40,
                "botania:alchemy_catalyst");
        infusion(provider, "oak_sapling_to_spruce_sapling", "minecraft:oak_sapling",
                "minecraft:spruce_sapling", 1, 120, "botania:alchemy_catalyst");
        infusion(provider, "ochre_froglight_to_verdant_froglight", "minecraft:ochre_froglight",
                "minecraft:verdant_froglight", 1, 40, "botania:alchemy_catalyst");
        infusion(provider, "orange_tulip_to_white_tulip", "minecraft:orange_tulip", "minecraft:white_tulip", 1,
                400, "botania:alchemy_catalyst");
        infusion(provider, "oxeye_daisy_to_cornflower", "minecraft:oxeye_daisy", "minecraft:cornflower", 1,
                400, "botania:alchemy_catalyst");
        infusion(provider, "pearlescent_froglight_to_ochre_froglight", "minecraft:pearlescent_froglight",
                "minecraft:ochre_froglight", 1, 40, "botania:alchemy_catalyst");
        infusion(provider, "peony_to_dandelion", "minecraft:peony", "minecraft:dandelion", 1, 400,
                "botania:alchemy_catalyst");
        infusion(provider, "pink_tulip_to_oxeye_daisy", "minecraft:pink_tulip", "minecraft:oxeye_daisy", 1,
                400, "botania:alchemy_catalyst");
        infusion(provider, "piston_relay", "minecraft:piston", "botania:piston_relay", 1, 15000, null);
        infusion(provider, "podzol_seeds", "minecraft:dead_bush", "botania:podzol_seeds", 1, 2500, null);
        infusion(provider, "poppy_to_blue_orchid", "minecraft:poppy", "minecraft:blue_orchid", 1, 400,
                "botania:alchemy_catalyst");
        infusion(provider, "potato_to_carrot", "minecraft:potato", "minecraft:carrot", 1, 6000,
                "botania:alchemy_catalyst");
        infusion(provider, "potato_unpoison", "minecraft:poisonous_potato", "minecraft:potato", 1, 1200,
                "botania:alchemy_catalyst");
        infusion(provider, "pufferfish_to_cod", "minecraft:pufferfish", "minecraft:cod", 1, 200,
                "botania:alchemy_catalyst");
        infusion(provider, "pumpkin_seeds_to_cocoa_beans", "minecraft:pumpkin_seeds", "minecraft:cocoa_beans",
                1, 6000, "botania:alchemy_catalyst");
        infusion(provider, "quartz_deconstruct", "minecraft:quartz_block", "minecraft:quartz", 4, 25,
                "botania:alchemy_catalyst");
        infusion(provider, "quartz_dupe", "minecraft:quartz", "minecraft:quartz", 2, 2500,
                "botania:conjuration_catalyst");
        infusion(provider, "rannuncarpus_chibi", "botania:rannuncarpus", "botania:rannuncarpus_chibi", 1, 2500,
                "botania:alchemy_catalyst");
        infusion(provider, "red_quartz_deconstruct", "botania:red_quartz", "botania:quartz_red", 4, 25,
                "botania:alchemy_catalyst");
        infusion(provider, "red_tulip_to_orange_tulip", "minecraft:red_tulip", "minecraft:orange_tulip", 1,
                400, "botania:alchemy_catalyst");
        infusion(provider, "redstone_dupe", "minecraft:redstone", "minecraft:redstone", 2, 5000,
                "botania:conjuration_catalyst");
        infusion(provider, "redstone_to_glowstone_dust", "minecraft:redstone", "minecraft:glowstone_dust", 1,
                300, "botania:alchemy_catalyst");
        infusion(provider, "rose_bush_to_peony", "minecraft:rose_bush", "minecraft:peony", 1, 400,
                "botania:alchemy_catalyst");
        infusion(provider, "rotten_flesh_to_leather", "minecraft:rotten_flesh", "minecraft:leather", 1, 600,
                "botania:alchemy_catalyst");
        infusion(provider, "salmon_to_tropical_fish", "minecraft:salmon", "minecraft:tropical_fish", 1, 200,
                "botania:alchemy_catalyst");
        infusion(provider, "slime_to_cactus", "minecraft:slime_ball", "minecraft:cactus", 1, 1200,
                "botania:alchemy_catalyst");
        infusion(provider, "snowball_dupe", "minecraft:snowball", "minecraft:snowball", 2, 200,
                "botania:conjuration_catalyst");
        infusion(provider, "solegnolia_chibi", "botania:solegnolia", "botania:solegnolia_chibi", 1, 2500,
                "botania:alchemy_catalyst");
        infusion(provider, "soul_sand_dupe", "minecraft:soul_sand", "minecraft:soul_sand", 2, 1500,
                "botania:conjuration_catalyst");
        infusion(provider, "soul_soil", "minecraft:soul_sand", "minecraft:soul_soil", 1, 120,
                "botania:alchemy_catalyst");
        infusion(provider, "spruce_leaves_dupe", "minecraft:spruce_leaves", "minecraft:spruce_leaves", 2, 2000,
                "botania:conjuration_catalyst");
        infusion(provider, "spruce_log_to_birch_log", "minecraft:spruce_log", "minecraft:birch_log", 1, 40,
                "botania:alchemy_catalyst");
        infusion(provider, "spruce_sapling_to_birch_sapling", "minecraft:spruce_sapling",
                "minecraft:birch_sapling", 1, 120, "botania:alchemy_catalyst");
        infusion(provider, "stone_to_andesite", "minecraft:stone", "minecraft:andesite", 1, 200,
                "botania:alchemy_catalyst");
        infusion(provider, "sunflower_to_lilac", "minecraft:sunflower", "minecraft:lilac", 1, 400,
                "botania:alchemy_catalyst");
        infusion(provider, "sunny_quartz_deconstruct", "botania:sunny_quartz", "botania:quartz_sunny", 4, 25,
                "botania:alchemy_catalyst");
        infusion(provider, "sweet_berries_to_glow_berries", "minecraft:sweet_berries",
                "minecraft:glow_berries", 1, 240, "botania:alchemy_catalyst");
        infusion(provider, "tangleberrie_chibi", "botania:tangleberrie", "botania:tangleberrie_chibi", 1, 2500,
                "botania:alchemy_catalyst");
        infusion(provider, "terracotta_to_red_sand", "minecraft:terracotta", "minecraft:red_sand", 1, 50,
                "botania:alchemy_catalyst");
        infusion(provider, "tiny_potato", "minecraft:potato", "botania:tiny_potato", 1, 1337, null);
        infusion(provider, "tropical_fish_to_pufferfish", "minecraft:tropical_fish", "minecraft:pufferfish", 1,
                200, "botania:alchemy_catalyst");
        infusion(provider, "tuff_to_calcite", "minecraft:tuff", "minecraft:calcite", 1, 200,
                "botania:alchemy_catalyst");
        infusion(provider, "verdant_froglight_to_pearlescent_froglight", "minecraft:verdant_froglight",
                "minecraft:pearlescent_froglight", 1, 40, "botania:alchemy_catalyst");
        infusion(provider, "vine_to_lily_pad", "minecraft:vine", "minecraft:lily_pad", 1, 320,
                "botania:alchemy_catalyst");
        infusion(provider, "wheat_seeds_to_potato", "minecraft:wheat_seeds", "minecraft:potato", 1, 6000,
                "botania:alchemy_catalyst");
        infusion(provider, "white_tulip_to_pink_tulip", "minecraft:white_tulip", "minecraft:pink_tulip", 1,
                400, "botania:alchemy_catalyst");
        infusion(provider, "wool_deconstruct", "#minecraft:wool", "minecraft:string", 3, 100,
                "botania:alchemy_catalyst");
        // 139 recipes ported
    }

    private static void pureDaisy(Consumer<FinishedRecipe> provider) {
        pureDaisy(provider, "blue_ice", "minecraft:packed_ice", "minecraft:blue_ice");
        pureDaisy(provider, "cobblestone", "minecraft:netherrack", "minecraft:cobblestone");
        pureDaisy(provider, "end_stone_to_cobbled_deepslate", "minecraft:end_stone", "minecraft:cobbled_deepslate");
        pureDaisy(provider, "livingrock", "minecraft:stone", "botania:livingrock");
        pureDaisyTag(provider, "livingwood", "minecraft:logs", "botania:livingwood_log");
        pureDaisy(provider, "obsidian", "botania:blaze_block", "minecraft:obsidian");
        pureDaisy(provider, "packed_ice", "minecraft:ice", "minecraft:packed_ice");
        pureDaisy(provider, "sand", "minecraft:soul_sand", "minecraft:sand");
        // 8 recipes ported
    }
    // ////////////////////////////////////
    // ***** registration helpers *****//
    // ////////////////////////////////////

    private static void petal(Consumer<FinishedRecipe> provider, String id, String output, int count,
                              String... inputs) {
        ItemStack outputStack = stack(output, count);
        if (outputStack.isEmpty() || !allResolvable(inputs)) {
            Pollution.LOGGER.warn("[botania] skipping petal recipe {} (unresolved item)", id);
            return;
        }
        GTRecipeBuilder builder = GTRecipeBuilder
                .of(recipeId("petal_apothecary", id), BotaniaRecipeMaps.MANA_PETAL_RECIPES)
                .duration(200)
                .EUt(100);
        addInputs(builder, inputs);
        builder.outputItems(outputStack).save(provider);
    }

    private static void runeAltar(Consumer<FinishedRecipe> provider, String id, String output, int count,
                                  int mana, String... inputs) {
        ItemStack outputStack = stack(output, count);
        if (outputStack.isEmpty() || !allResolvable(inputs)) {
            Pollution.LOGGER.warn("[botania] skipping runic altar recipe {} (unresolved item)", id);
            return;
        }
        GTRecipeBuilder builder = GTRecipeBuilder
                .of(recipeId("runic_altar", id), BotaniaRecipeMaps.MANA_RUNE_ALTAR_RECIPES)
                .duration(200 * GTUtil.getTierByVoltage(mana))
                .EUt(mana);
        addInputs(builder, inputs);
        builder.outputItems(outputStack).save(provider);
    }

    private static void infusion(Consumer<FinishedRecipe> provider, String id, String input, String output,
                                 int count, int mana, String catalyst) {
        ItemStack outputStack = stack(output, count);
        if (outputStack.isEmpty() || !allResolvable(input) || !resolvable(catalyst)) {
            Pollution.LOGGER.warn("[botania] skipping mana infusion recipe {} (unresolved item)", id);
            return;
        }
        GTRecipeBuilder builder = GTRecipeBuilder
                .of(recipeId("mana_infusion", id), BotaniaRecipeMaps.MANA_INFUSION_RECIPES)
                .duration(200 * GTUtil.getTierByVoltage(mana))
                .EUt(mana);
        addInputs(builder, input);
        if (catalyst != null) {
            builder.notConsumable(catalystStack(catalyst));
        }
        builder.outputItems(outputStack).save(provider);
    }

    private static void pureDaisy(Consumer<FinishedRecipe> provider, String id, String input, String output) {
        pureDaisy(provider, id, new String[] { input }, output);
    }

    private static void pureDaisyTag(Consumer<FinishedRecipe> provider, String id, String inputTag, String output) {
        pureDaisy(provider, id, new String[] { "#" + inputTag }, output);
    }

    private static void pureDaisy(Consumer<FinishedRecipe> provider, String id, String[] inputs, String output) {
        ItemStack outputStack = stack(output, 1);
        if (outputStack.isEmpty() || !allResolvable(inputs)) {
            Pollution.LOGGER.warn("[botania] skipping pure daisy recipe {} (unresolved item)", id);
            return;
        }
        GTRecipeBuilder builder = GTRecipeBuilder
                .of(recipeId("pure_daisy", id), BotaniaRecipeMaps.PURE_DAISY_RECIPES)
                .duration(200)
                .EUt(100);
        addInputs(builder, inputs);
        builder.outputItems(outputStack).save(provider);
    }

    private static void addInputs(GTRecipeBuilder builder, String... specs) {
        for (String spec : specs) {
            if (spec.startsWith("#")) {
                builder.inputItems(TagKey.create(Registries.ITEM, parse(spec.substring(1))));
            } else if (spec.contains("|")) {
                List<ItemStack> alternatives = new ArrayList<>();
                for (String id : spec.split("\\|")) {
                    ItemStack candidate = stack(id, 1);
                    if (!candidate.isEmpty()) {
                        alternatives.add(candidate);
                    }
                }
                if (!alternatives.isEmpty()) {
                    builder.inputItems(Ingredient.of(alternatives.toArray(ItemStack[]::new)));
                }
            } else {
                builder.inputItems(stack(spec, 1));
            }
        }
    }

    private static boolean allResolvable(String... specs) {
        for (String spec : specs) {
            if (!resolvable(spec)) {
                return false;
            }
        }
        return true;
    }

    private static boolean resolvable(String spec) {
        if (spec == null || spec.startsWith("#")) {
            return true;
        }
        for (String id : spec.split("\\|")) {
            if (!ForgeRegistries.ITEMS.containsKey(parse(id))) {
                return false;
            }
        }
        return true;
    }

    private static ItemStack stack(String id, int count) {
        Item item = ForgeRegistries.ITEMS.getValue(parse(id));
        return item == null ? ItemStack.EMPTY : new ItemStack(item, count);
    }

    private static ItemStack catalystStack(String spec) {
        Block block = ForgeRegistries.BLOCKS.getValue(parse(spec));
        return block == null ? ItemStack.EMPTY : new ItemStack(block);
    }

    /** Parses a full {@code namespace:path} id; the deprecated constructor is isolated here. */
    @SuppressWarnings("removal")
    private static ResourceLocation parse(String id) {
        return new ResourceLocation(id);
    }

    private static ResourceLocation recipeId(String category, String id) {
        return ResourceLocation.fromNamespaceAndPath("pollution", "botania_native/" + category + "/" + id);
    }
}
