package meowmel.pollution;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.mojang.logging.LogUtils;
import com.tterrag.registrate.providers.ProviderType;
import meowmel.pollution.api.pollution.PollutionEngine;
import meowmel.pollution.common.PollutionCreativeTabs;
import meowmel.pollution.common.block.PollutionMagicBlocks;
import meowmel.pollution.common.block.PollutionMiscBlocks;
import meowmel.pollution.common.block.PollutionPlantBlocks;
import meowmel.pollution.common.starstream.StarstreamBlocks;
import meowmel.pollution.common.command.PollutionCommand;
import meowmel.pollution.common.machine.PollutionMachineEvents;
import meowmel.pollution.compat.gtceu.PollutionGTAddon;
import meowmel.pollution.dimension.worldgen.PollutionStructures;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(Pollution.MOD_ID)
public final class Pollution {

    public static final String MOD_ID = "pollution";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Pollution(FMLJavaModLoadingContext context) {
        context.registerConfig(ModConfig.Type.COMMON, PollutionConfig.SPEC);

        // Register the GTCEu addon registrate on this mod's own bus.
        PollutionGTAddon.REGISTRATE.registerRegistrate();

        // Casing blocks (magic multiblocks) are plain registrate blocks and can
        // be created here, on the Pollution bus, like the machine definitions.
        PollutionMagicBlocks.init();
        StarstreamBlocks.init(context.getModEventBus());
        PollutionPlantBlocks.init();
        PollutionMiscBlocks.init(context);
        meowmel.pollution.common.item.PollutionItems.init();
        meowmel.pollution.common.entity.PollutionEntities.init(context);
        meowmel.pollution.dimension.biome.POBiomeSources.init(context);
        PollutionStructures.init(context);
        PollutionCreativeTabs.init(context);

        // Recipe types are created from GregTech's own GTRecipeType RegisterEvent
        // (posted inside GTRecipeTypes.init, before the registry freezes).
        context.getModEventBus().addGenericListener(GTRecipeType.class,
                PollutionMachineEvents::onRecipeTypeRegister);

        // Machines are registered through GregTech's machine RegisterEvent, which
        // fires after GregTech's data is ready and before its registry freezes.
        context.getModEventBus().addGenericListener(MachineDefinition.class,
                PollutionMachineEvents::onMachineRegister);

        // Manual lang keys are generated through the registrate so they land in
        // the same datagen file as the generated material names.
        PollutionGTAddon.REGISTRATE.addDataGenerator(ProviderType.LANG, provider -> {
            provider.add("mod.pollution.name", "Pollution Unofficial");
            provider.add("itemGroup.pollution.main", "Pollution Unofficial");
            provider.add("pollution.command.get", "Chunk pollution: %s");
            provider.add("pollution.command.set", "Chunk pollution set to %s");
            provider.add("pollution.command.scrub", "Scrubbed %s pollution");
            provider.add("pollution.effect.warning", "The polluted air is making you sick");
            provider.add("chat.pollution.warp.countdownbomb.tick", "Warp bomb detonates in %s s");
            provider.add("chat.pollution.warp.countdownbomb.end", "The warp bomb releases a harmless blast");
            provider.add("pollution.machine.vis_generator.tooltip",
                    "Drains Thaumcraft vis to generate EU and industrial pollution");
            provider.add("pollution.machine.vis_provider.tooltip",
                    "Consumes EU to recharge the nearest Thaumcraft aura node");
            provider.add("pollution.machine.magic_energy_absorber.tooltip",
                    "Generates EU while a dragon egg is placed on top");
            provider.add("pollution.machine.flux_scrubber.tooltip",
                    "Consumes EU to scrub Thaumcraft flux in a 16 block radius");
            provider.add("pollution.machine.flux_fuel_cell.tooltip",
                    "Burns nearby Thaumcraft flux for EU; explodes above its flux ceiling");
            provider.add("pollution.machine.solar_plate.tooltip",
                    "Solar plate MK%s: works in daylight with a per-kind boost condition");
            provider.add("pollution.machine.vis_hatch.tooltip.capacity",
                    "Vis buffer: %s Vis");
            provider.add("pollution.machine.vis_hatch.tooltip.drain",
                    "Drains 0.05 Vis/s from the Thaumcraft 4R vis network");
            provider.add("pollution.machine.vis_hatch.tooltip.buffer",
                    "Stores %s Vis per successful drain");
            provider.add("pollution.machine.vis_hatch.gui.amount",
                    "Vis: %s / %s");
            provider.add("pollution.machine.infused_fluid_hatch.tooltip",
                    "Stores infused fluids and supplies them to magic multiblock recipes");
            provider.add("pollution.machine.flux_muffler.tooltip.recovery",
                    "Item recovery chance: %s%%");
            provider.add("pollution.machine.flux_muffler.tooltip",
                    "Magically filtered muffler: recovers machine byproducts; industrial pollution is vented here");
            provider.add("pollution.magic.failure.hatches",
                    "Missing required magic hatch");
            provider.add("pollution.magic.failure.vis",
                    "Not enough vis");
            provider.add("pollution.magic.failure.infused_fluid",
                    "Not enough infused fluid");
            provider.add("pollution.magic.failure.mana",
                    "Not enough mana");
            provider.add("pollution.magic.failure.life_essence",
                    "Not enough life essence");
            provider.add("pollution.magic.failure.coil",
                    "Requires GregTech heating coils");
            provider.add("pollution.magic.failure.temperature",
                    "Required temperature: %s K");
            provider.add("pollution.magic.failure.research",
                    "Missing Thaumcraft research: %s");
            provider.add("pollution.item.vis_checker.result",
                    "Warp — permanent: %s, sticky: %s, temporary: %s");
            provider.add("entity.pollution.basalz", "Basalz");
            provider.add("entity.pollution.blitz", "Blitz");
            provider.add("entity.pollution.blizz", "Blizz");
            // Block display names are generated by the registrate itself
            // (block.pollution.<name>); adding them here again aborts datagen
            // with "Duplicate translation key".
            provider.add("pollution.flesh_heart.bound",
                    "The flesh tree has bound itself to your soul...");
            provider.add("pollution.flesh_heart.level",
                    "Flesh tree level: %s/%s");
            provider.add("pollution.flesh_heart.next_growth",
                    "Next growth needs %s LP; the soul network holds %s LP");
            provider.add("pollution.flesh_heart.other",
                    "This tree is bound to %s's soul...");
            provider.add("pollution.mineral_extractor.enabled",
                    "Mineral extractor enabled");
            provider.add("pollution.mineral_extractor.disabled",
                    "Mineral extractor disabled");
            provider.add("pollution.mineral_extractor.mode",
                    "Mineral extractor mode: %s");

            // Item behaviour tooltips (upstream metaitem behaviour pass).
            provider.add("pollution.item.packaged_aura_node.header", "Node info:");
            provider.add("pollution.armor.goggles.food",
                    "Gluttony: consumes power to restore hunger");
            provider.add("pollution.armor.goggles.water",
                    "Aquatic: night vision mode grants water breathing");
            provider.add("pollution.armor.goggles.solar",
                    "Solar: recharges in daylight while night vision is off");
            provider.add("pollution.armor.goggles.vis_discount",
                    "Vis discount: %s");
            provider.add("pollution.tarot.the_fool.tooltip",
                    "Sneak + right-click to teleport to the world spawn");
            provider.add("pollution.bauble.source", "Source: %s/%s");
            provider.add("pollution.bauble.material", "Aspect: %s");
            provider.add("pollution.filter.durability", "Durability: %s/%s");
            provider.add("pollution.filter.material", "Primary material: %s");
            provider.add("pollution.filter.tier", "Filter tier: %s");
            provider.add("pollution.filter.expected", "Expected work: %s");
            provider.add("pollution.filter.remaining", "Until breakage: %s");
            provider.add("pollution.flux_scrubber.filter", "Filter durability: %s/%s");
            provider.add("pollution.flux_scrubber.filter_none", "No filter installed");
            provider.add("pollution.armor.wings.flight", "Flight: creative-style flying while charged");
            provider.add("pollution.armor.wings.nano_buffs",
                    "Sprinter I: speed boost while active");
            provider.add("pollution.armor.wings.quantum_buffs",
                    "Sprinter II: jump and speed boost; Haste: mining speed boost while active");
            provider.add("pollution.armor.wings.fall", "Fall damage reduction: %sx");
            provider.add("pollution.astral_data.unattuned", "No constellation data");
            provider.add("pollution.astral_data.constellation", "Constellation: %s");
            provider.add("pollution.astral_data.function", "Celestial bias: %s");
            provider.add("pollution.astral_data.function.life", "Life cultivation");
            provider.add("pollution.astral_data.function.processing", "Processing and destruction");
            provider.add("pollution.astral_data.function.stability", "Stability and protection");
            provider.add("pollution.astral_data.function.energy", "Energy and overclocking");
            provider.add("pollution.astral_data.function.time", "Timing and calibration");
            provider.add("pollution.astral_data.function.resonance", "General starlight resonance");
            provider.add("pollution.astral_data.nbt_preserved",
                    "Constellation identity is kept as native Astral Sorcery NBT");
            provider.add("pollution.crystal_quality.unselected", "No crystal quality data");
            provider.add("pollution.crystal_quality.purity", "Crystal purity: %s");
            provider.add("pollution.crystal_quality.stability", "Cultivation stability: %s");
            provider.add("pollution.crystal_quality.embryo", "In celestial cultivation stage");
            provider.add("pollution.crystal_quality.cultivated", "Cultivated crystal");
            provider.add("pollution.crystal_quality.grade", "Lens quality: grade %s (%s%%)");
            provider.add("gtceu.magic_blast_smelter", "Magic Alloy Blast Smelter");
            provider.add("gtceu.stove", "Stove");
            provider.add("gtceu.magic_fusion_reactor", "Magic Fusion Reactor");
            provider.add("gtceu.magic_assembler", "Magic Assembler");
            provider.add("gtceu.magic_greenhouse", "Magic Greenhouse");
            provider.add("gtceu.magic_turbine", "Magic Turbine");
            provider.add("gtceu.forge_alchemy", "Forge Alchemy");
            provider.add("gtceu.node_magic_fusion", "Node Fusion");
            provider.add("gtceu.mana_petal_recipes", "Mana Petal Apothecary");
            provider.add("gtceu.mana_rune_altar_recipes", "Mana Rune Altar");
            provider.add("gtceu.pure_daisy_recipes", "Pure Daisy");
            provider.add("gtceu.mana_infusion_recipes", "Mana Infusion");
            provider.add("gtceu.mana_gen_recipes", "Mana Generation");
            provider.add("gtceu.mana_to_eu", "Mana to EU");
            provider.add("gtceu.dan_de_life_on", "Dan De Life On");
            provider.add("gtceu.ability.pollution_vis_hatch", "Vis Hatch");
            provider.add("gtceu.ability.pollution_infused_fluid_hatch", "Infused Fluid Hatch");
            provider.add("gtceu.ability.pollution_mana_input_hatch", "Mana Input Hatch");
            provider.add("gtceu.ability.pollution_mana_output_hatch", "Mana Output Hatch");
            provider.add("gtceu.ability.pollution_mana_input_pool", "Mana Pool Input Hatch");
            provider.add("gtceu.ability.pollution_mana_output_pool", "Mana Pool Output Hatch");
            provider.add("gtceu.ability.pollution_tarot_hatch", "Tarot Hatch");
            provider.add("tagprefix.proxima_centauri_b", "Proxima Centauri B Stone");
            provider.add("material.gtceu.manasteel", "Manasteel");
            provider.add("material.gtceu.terrasteel", "Terrasteel");
            provider.add("fluid.pollution.advanced_battery_content", "Advanced Battery Content");
            provider.add("fluid.pollution.advanced_battery_hull_alloy", "Advanced Battery Hull Alloy");
            provider.add("fluid.pollution.advanced_substrate", "Advanced Substrate");
            provider.add("fluid.pollution.advanced_thaumic_superconductor", "Advanced Thaumic Superconductor");
            provider.add("fluid.pollution.aertitanium", "Aertitanium");
            provider.add("fluid.pollution.aetheric_dark_steel", "Aetheric Dark Steel");
            provider.add("fluid.pollution.alchemical_residue_1", "Alchemical Residue 1");
            provider.add("fluid.pollution.alchemical_residue_2", "Alchemical Residue 2");
            provider.add("fluid.pollution.alchemical_residue_3", "Alchemical Residue 3");
            provider.add("fluid.pollution.alchemical_residue_4", "Alchemical Residue 4");
            provider.add("fluid.pollution.alchemical_residue_5", "Alchemical Residue 5");
            provider.add("fluid.pollution.alchemical_residue_6", "Alchemical Residue 6");
            provider.add("fluid.pollution.alchemical_vapor_1", "Alchemical Vapor 1");
            provider.add("fluid.pollution.alchemical_vapor_2", "Alchemical Vapor 2");
            provider.add("fluid.pollution.alchemical_vapor_3", "Alchemical Vapor 3");
            provider.add("fluid.pollution.alchemical_vapor_4", "Alchemical Vapor 4");
            provider.add("fluid.pollution.alchemical_vapor_5", "Alchemical Vapor 5");
            provider.add("fluid.pollution.alchemical_vapor_6", "Alchemical Vapor 6");
            provider.add("fluid.pollution.amber", "Amber");
            provider.add("fluid.pollution.aquasilver", "Aquasilver");
            provider.add("fluid.pollution.arcane_ink", "Arcane Ink");
            provider.add("fluid.pollution.authority_lead", "Authority Lead");
            provider.add("fluid.pollution.basic_battery_content", "Basic Battery Content");
            provider.add("fluid.pollution.basic_battery_hull_alloy", "Basic Battery Hull Alloy");
            provider.add("fluid.pollution.basic_substrate", "Basic Substrate");
            provider.add("fluid.pollution.basic_thaumic_superconductor", "Basic Thaumic Superconductor");
            provider.add("fluid.pollution.binding_metal", "Binding Metal");
            provider.add("fluid.pollution.blackmansus", "Blackmansus");
            provider.add("fluid.pollution.crude_lk_99", "Crude Lk 99");
            provider.add("fluid.pollution.dimensional_transforming_agent", "Dimensional Transforming Agent");
            provider.add("fluid.pollution.dragon_pulse_fuel", "Dragon Pulse Fuel");
            provider.add("fluid.pollution.dragonstone", "Dragonstone");
            provider.add("fluid.pollution.dumb_tin", "Dumb Tin");
            provider.add("fluid.pollution.elven", "Elven");
            provider.add("fluid.pollution.elven_elementium", "Elven Elementium");
            provider.add("fluid.pollution.elven_quartz", "Elven Quartz");
            provider.add("fluid.pollution.embryo_magic_water", "Embryo Magic Water");
            provider.add("fluid.pollution.erich_aura", "Erich Aura");
            provider.add("fluid.pollution.ethyl_silicate", "Ethyl Silicate");
            provider.add("fluid.pollution.existing_nexus", "Existing Nexus");
            provider.add("fluid.pollution.fading_nexus", "Fading Nexus");
            provider.add("fluid.pollution.ferrous_chloride", "Ferrous Chloride");
            provider.add("fluid.pollution.filth", "Filth");
            provider.add("fluid.pollution.filth_water", "Filth Water");
            provider.add("fluid.pollution.flame_coal", "Flame Coal");
            provider.add("fluid.pollution.hafnocene_dichloride", "Hafnocene Dichloride");
            provider.add("fluid.pollution.highmana_stannous_sulfate", "Highmana Stannous Sulfate");
            provider.add("fluid.pollution.hydrazoic_acid", "Hydrazoic Acid");
            provider.add("fluid.pollution.hyper_substrate", "Hyper Substrate");
            provider.add("fluid.pollution.hyperdimensional_silver", "Hyperdimensional Silver");
            provider.add("fluid.pollution.ignissteel", "Ignissteel");
            provider.add("fluid.pollution.iizunamaru_electrum", "Iizunamaru Electrum");
            provider.add("fluid.pollution.impure_hyperdimensional_silver", "Impure Hyperdimensional Silver");
            provider.add("fluid.pollution.impure_mercuric_salt_solution", "Impure Mercuric Salt Solution");
            provider.add("fluid.pollution.impuremana", "Impuremana");
            provider.add("fluid.pollution.infernal_blaze_propellant", "Infernal Blaze Propellant");
            provider.add("fluid.pollution.infused_air", "Infused Air");
            provider.add("fluid.pollution.infused_alchemy", "Infused Alchemy");
            provider.add("fluid.pollution.infused_alien", "Infused Alien");
            provider.add("fluid.pollution.infused_animal", "Infused Animal");
            provider.add("fluid.pollution.infused_armor", "Infused Armor");
            provider.add("fluid.pollution.infused_aura", "Infused Aura");
            provider.add("fluid.pollution.infused_cold", "Infused Cold");
            provider.add("fluid.pollution.infused_craft", "Infused Craft");
            provider.add("fluid.pollution.infused_crystal", "Infused Crystal");
            provider.add("fluid.pollution.infused_dark", "Infused Dark");
            provider.add("fluid.pollution.infused_death", "Infused Death");
            provider.add("fluid.pollution.infused_earth", "Infused Earth");
            provider.add("fluid.pollution.infused_energy", "Infused Energy");
            provider.add("fluid.pollution.infused_entropy", "Infused Entropy");
            provider.add("fluid.pollution.infused_exchange", "Infused Exchange");
            provider.add("fluid.pollution.infused_fire", "Infused Fire");
            provider.add("fluid.pollution.infused_fly", "Infused Fly");
            provider.add("fluid.pollution.infused_greed", "Infused Greed");
            provider.add("fluid.pollution.infused_human", "Infused Human");
            provider.add("fluid.pollution.infused_instrument", "Infused Instrument");
            provider.add("fluid.pollution.infused_life", "Infused Life");
            provider.add("fluid.pollution.infused_light", "Infused Light");
            provider.add("fluid.pollution.infused_magic", "Infused Magic");
            provider.add("fluid.pollution.infused_mechanics", "Infused Mechanics");
            provider.add("fluid.pollution.infused_metal", "Infused Metal");
            provider.add("fluid.pollution.infused_motion", "Infused Motion");
            provider.add("fluid.pollution.infused_order", "Infused Order");
            provider.add("fluid.pollution.infused_plant", "Infused Plant");
            provider.add("fluid.pollution.infused_sense", "Infused Sense");
            provider.add("fluid.pollution.infused_soul", "Infused Soul");
            provider.add("fluid.pollution.infused_spatio", "Infused Spatio");
            provider.add("fluid.pollution.infused_taint", "Infused Taint");
            provider.add("fluid.pollution.infused_tempus", "Infused Tempus");
            provider.add("fluid.pollution.infused_thought", "Infused Thought");
            provider.add("fluid.pollution.infused_tinctura", "Infused Tinctura");
            provider.add("fluid.pollution.infused_trap", "Infused Trap");
            provider.add("fluid.pollution.infused_undead", "Infused Undead");
            provider.add("fluid.pollution.infused_void", "Infused Void");
            provider.add("fluid.pollution.infused_water", "Infused Water");
            provider.add("fluid.pollution.infused_weapon", "Infused Weapon");
            provider.add("fluid.pollution.keqinggold", "Keqinggold");
            provider.add("fluid.pollution.llp", "Llp");
            provider.add("fluid.pollution.lotus_dust", "Lotus Dust");
            provider.add("fluid.pollution.magic_activated_ferrous_chloride_ethanol_solution", "Magic Activated Ferrous Chloride Ethanol Solution");
            provider.add("fluid.pollution.magic_activated_iron_chloride_solution", "Magic Activated Iron Chloride Solution");
            provider.add("fluid.pollution.magic_nitrobenzene", "Magic Nitrobenzene");
            provider.add("fluid.pollution.magical_stannous_sulfate_solution", "Magical Stannous Sulfate Solution");
            provider.add("fluid.pollution.magical_sulfo_plumbic_salt", "Magical Sulfo Plumbic Salt");
            provider.add("fluid.pollution.magical_superconductive_liquid", "Magical Superconductive Liquid");
            provider.add("fluid.pollution.magical_tin_solution", "Magical Tin Solution");
            provider.add("fluid.pollution.melt_gold", "Melt Gold");
            provider.add("fluid.pollution.mercuric_salt_solution", "Mercuric Salt Solution");
            provider.add("fluid.pollution.moonlight_resin", "Moonlight Resin");
            provider.add("fluid.pollution.octine", "Octine");
            provider.add("fluid.pollution.oil_with_llp", "Oil With Llp");
            provider.add("fluid.pollution.optical_grade_aquamarine", "Optical Grade Aquamarine");
            provider.add("fluid.pollution.ordolead", "Ordolead");
            provider.add("fluid.pollution.perditioaluminium", "Perditioaluminium");
            provider.add("fluid.pollution.pixie_dust", "Pixie Dust");
            provider.add("fluid.pollution.pluto_zinc", "Pluto Zinc");
            provider.add("fluid.pollution.pure_tar", "Pure Tar");
            provider.add("fluid.pollution.purified_activated_ferrous_chloride", "Purified Activated Ferrous Chloride");
            provider.add("fluid.pollution.purified_activated_ferrous_chloride_ethanol_solution", "Purified Activated Ferrous Chloride Ethanol Solution");
            provider.add("fluid.pollution.pyrargyrite", "Pyrargyrite");
            provider.add("fluid.pollution.rich_aura", "Rich Aura");
            provider.add("fluid.pollution.rough_llp", "Rough Llp");
            provider.add("fluid.pollution.roughdraft", "Roughdraft");
            provider.add("fluid.pollution.salisundus", "Salisundus");
            provider.add("fluid.pollution.scabyst", "Scabyst");
            provider.add("fluid.pollution.sentient_metal", "Sentient Metal");
            provider.add("fluid.pollution.sodium_azide", "Sodium Azide");
            provider.add("fluid.pollution.sodium_cyclopentadienide", "Sodium Cyclopentadienide");
            provider.add("fluid.pollution.starlight_pollen", "Starlight Pollen");
            provider.add("fluid.pollution.starrymansus", "Starrymansus");
            provider.add("fluid.pollution.substrate", "Substrate");
            provider.add("fluid.pollution.sulfo_plumbic_salt", "Sulfo Plumbic Salt");
            provider.add("fluid.pollution.super_sticky_tar", "Super Sticky Tar");
            provider.add("fluid.pollution.syrmorite", "Syrmorite");
            provider.add("fluid.pollution.syrmorite_doped_magic_water_solution", "Syrmorite Doped Magic Water Solution");
            provider.add("fluid.pollution.terracopper", "Terracopper");
            provider.add("fluid.pollution.thaummix", "Thaummix");
            provider.add("fluid.pollution.u_oxo_bis_hafnocene_azide", "U Oxo Bis Hafnocene Azide");
            provider.add("fluid.pollution.unformed_embryo_magic_water", "Unformed Embryo Magic Water");
            provider.add("fluid.pollution.unstable_dimensional_silver", "Unstable Dimensional Silver");
            provider.add("fluid.pollution.valonite", "Valonite");
            provider.add("fluid.pollution.void_material", "Void Material");
            provider.add("fluid.pollution.void_water", "Void Water");
            provider.add("fluid.pollution.whitemansus", "Whitemansus");
            provider.add("pollution.machine.bot_gas_collector_beamLevel", "Beam level: %s");
            provider.add("pollution.machine.bot_gas_collector_essenceConsumptionSpeed", "Essence consumption: %s mB/t");
            provider.add("pollution.machine.bot_gas_collector_finalCollectionSpeed", "Collection rate: %s mB");
            provider.add("pollution.machine.bot_gas_collector_manaConsumptionSpeed", "Mana consumption: %s");
            provider.add("pollution.machine.endoflame_array.display.flowers", "Flowers: %s");
            provider.add("pollution.machine.endoflame_array.display.fuel_cache", "Fuel cache: %s / %s tick");
            provider.add("pollution.machine.endoflame_array.display.fuel_items", "Remaining fuel items: %s");
            provider.add("pollution.machine.endoflame_array.display.mana_pool", "Mana pool: %s / %s Mana");
            provider.add("pollution.machine.endoflame_array.display.output", "Actual mana output: %s Mana/t");
            provider.add("pollution.machine.mana_generator.tooltip", "Generates EU from a nearby Botania mana pool");
            provider.add("pollution.machine.mana_hatch.capacity", "Mana buffer: %s Mana");
            provider.add("pollution.machine.mana_hatch.gui.amount", "Mana: %s / %s");
            provider.add("pollution.machine.mana_hatch.gui.rate", "Transfer: %s Mana/t");
            provider.add("pollution.machine.mana_hatch.input_rate",
                    "Supplies the multiblock: up to %s Mana/t (%s, %sA); receives Botania mana bursts");
            provider.add("pollution.machine.mana_hatch.output_rate",
                    "Outputs to adjacent devices: up to %s Mana/t (%s, %sA)");
            provider.add("pollution.machine.mana_hatch.tooltip",
                    "Energy-type mana hatch: uses Botania mana as the GT energy interface of magic multiblocks (1 Mana = 1 EU internally)");
            provider.add("pollution.machine.mana_plate.speed", "Speed: %s | Mana: %s");
            provider.add("pollution.machine.mana_plate.tier", "Tier: %s | Mana: %s / %s");
            provider.add("pollution.machine.mana_pool_hatch.capacity", "Pure mana capacity: %s Mana");
            provider.add("pollution.machine.mana_pool_hatch.tooltip",
                    "Pure mana pool hatch: pays/receives recipe mana through IManaHatch; not a GT energy interface");
            provider.add("pollution.machine.mana_pool_hatch.transfer", "Max transfer rate: %s Mana/t");
            provider.add("pollution.machine.mana_pool_hatch.type", "Mana pool type: %s");
            provider.add("pollution.machine.mana_pool_hatch.type.diluted", "Diluted");
            provider.add("pollution.machine.mana_pool_hatch.type.mythic", "Mythic");
            provider.add("pollution.machine.mana_pool_hatch.type.normal", "Normal");
            provider.add("pollution.machine.mana_pool_input_hatch.tooltip",
                    "Mana pool input hatch: supplies the multiblock; receives mana from Botania bursts/sparks and adjacent output hatches");
            provider.add("pollution.machine.mana_pool_output_hatch.tooltip",
                    "Mana pool output hatch: receives multiblock mana and pushes it to adjacent Botania receivers or input hatches");
            provider.add("pollution.machine.mega_mana_turbine.catalyst", "Catalyst tier: %s");
            provider.add("pollution.machine.mega_mana_turbine.max_output", "Max output: %s");
            provider.add("pollution.machine.mega_mana_turbine.parallel", "Parallels: %s | Coil tier: %s");
            provider.add("pollution.machine.pollution_multi_dan_de_life_on.buffer", "Garden EU buffer: %s");
            provider.add("pollution.machine.pollution_multi_dan_de_life_on.energy", "Garden energy: %s / %s");
            provider.add("pollution.machine.pollution_multi_dan_de_life_on.mode", "Garden mode: %s");
            provider.add("pollution.machine.pollution_multi_dan_de_life_on.mode0", "Energy output");
            provider.add("pollution.machine.pollution_multi_dan_de_life_on.mode1", "Fluid output");
            provider.add("pollution.machine.wireless_mana_hatch.tooltip",
                    "Wireless mana hatch: output hatches deposit mana into the per-dimension energy-mana network; input hatches draw from it");
            provider.add("pollution.machine.wireless_mana_pool_hatch.tooltip",
                    "Wireless mana pool hatch: output hatches deposit pure mana into the per-dimension pool network; input hatches draw from it");
            provider.add("pollution.machine.mana_plate.throttle_modify", "Speed throttle: ");
            provider.add("pollution.machine.magic_turbine.no_rotor_holder", "Missing rotor holder");
            provider.add("pollution.machine.magic_turbine.no_rotor",
                    "No rotor in the holder - insert a rotor to run");
            provider.add("pollution.magic.failure.catalyst", "Missing required magic catalyst");
            provider.add("pollution.modeChanged.message", "Machine mode switched");
            provider.add("pollution.item.vis_checker.tooltip",
                    "Right-click to display your Thaumcraft warp");
            provider.add("pollution.item.starstream_linker.mode.input",
                    "Starstream Linker: input mode");
            provider.add("pollution.item.starstream_linker.mode.network",
                    "Starstream Linker: network mode");
            provider.add("pollution.item.starstream_linker.tooltip.toggle",
                    "Sneak + right-click air to switch mode");
            provider.add("pollution.item.starstream_linker.tooltip.input",
                    "Input mode: link constellation towers or relays to the starstream hub");
            provider.add("pollution.item.starstream_linker.tooltip.network",
                    "Network mode: configure relays, wireless terminals and cross-dimensional gateways");
            provider.add("pollution.item.starstream_linker.unported",
                    "The upstream constellation tower producer is excluded; the standalone network is available");
            provider.add("block.pollution.starstream_operation_core", "Starstream Operation Core");
            provider.add("block.pollution.starstream_relay", "Starstream Relay");
            provider.add("block.pollution.starstream_interdimensional_relay", "Starstream Interdimensional Relay");
            provider.add("block.pollution.starstream_chunk_anchor", "Starstream Chunk Anchor");
            provider.add("block.pollution.starstream_nexus_core", "Starstream Nexus Core");
            provider.add("pollution.machine.aspect_tank.tooltip",
                    "Single-block aspect storage: the front face is the aspect port; wrench sets the output face, soft mallet toggles auto-output, sneak + soft mallet toggles voiding");
            provider.add("pollution.machine.aspect_tank.help",
                    "Insert aspect ampoules, jars or other aspect containers into the input slot to store or retrieve aspects");
            provider.add("pollution.machine.aspect_tank.tooltip.auto_output",
                    "§aAuto-output enabled");
            provider.add("pollution.machine.aspect_tank.tooltip.capacity",
                    "§5Aspect capacity: §f%s");
            provider.add("pollution.machine.aspect_tank.tooltip.locked",
                    "§f%1$s §cis locked!");
            provider.add("pollution.machine.aspect_tank.tooltip.stored",
                    "§9Stored aspects: §f%1$s §7× %2$s");
            provider.add("pollution.machine.aspect_tank.tooltip.voiding",
                    "§cVoiding enabled");
            provider.add("pollution.machine.small_node_generator.tooltip",
                    "Burns packaged aura nodes to generate EU; the multiplier depends on the node's properties and the machine tier");
            provider.add("pollution.machine.source_charge.tooltip",
                    "Consumes 1 mB/t of the matching aspect fluid to restore 1 charge to the bauble");
            provider.add("pollution.machine.node_producer.tooltip.1",
                    "Creates packaged aura nodes from energy essence fluid");
            provider.add("pollution.machine.node_producer.tooltip.2",
                    "Per tick it drains the energy hatch's voltage in EU; each node needs 144 × 2^(EU tier - 4) mB of Infused Energy");
            provider.add("pollution.machine.node_producer.tooltip.3",
                    "Base creation time is 30 s; every tier above EV shortens it (ceil(30 / (tier - 3)) seconds)");
            provider.add("pollution.machine.node_producer.tooltip.4",
                    "Cutting the energy essence supply mid-process aborts the node and wastes the progress");
            provider.add("pollution.machine.node_producer.tooltip.5",
                    "Higher coil tiers raise the node's Ignis value, higher EU tiers raise its Ordo value");
            provider.add("pollution.machine.node_producer_duration", "Creation time per node: %s");
            provider.add("pollution.machine.node_producer_infusedcost", "Energy essence per tick: %s");
            provider.add("pollution.machine.node_washer.tooltip.1",
                    "Washes packaged aura nodes in place");
            provider.add("pollution.machine.node_washer.tooltip.2",
                    "Consumes EU and Infused Water to strip one batch of Entropy (coil tier × energy tier × 25)");
            provider.add("pollution.machine.node_washer.tooltip.3",
                    "Also scrubs a little flux around the machine");
            provider.add("pollution.machine.node_washer.tooltip.4",
                    "Process durations are random; losing power mid-operation wastes the batch");
            provider.add("pollution.machine.node_blast_furnace.tooltip.1",
                    "Runs blast furnace and forge alchemy recipes, gated by the coil temperature");
            provider.add("pollution.machine.node_blast_furnace.tooltip.2",
                    "Uses packaged aura nodes as an essentia catalyst, consuming one node every 30 seconds");
            provider.add("pollution.machine.node_blast_furnace.tooltip.3",
                    "Converts the node's Ordo/Perditio essence into Infused Light/Dark at ×10");
            provider.add("pollution.machine.node_blast_furnace.tooltip.4",
                    "Interrupting the node supply pauses the recipes");
            provider.add("pollution.machine.node_fusion_reactor.tooltip.1",
                    "Node fusion: fluids in, fluids out (LuV / ZPM / UV)");
            provider.add("pollution.machine.node_fusion_reactor.tooltip.2",
                    "Supports parallels and the clean-vis check");
            provider.add("pollution.machine.node_fusion_reactor.tooltip.3",
                    "See the Node Fusion recipe category for the recipes");
            provider.add("pollution.machine.central_vis_tower.tooltip.1",
                    "Drains the excess vis of nearby nodes into Infused Light");
            provider.add("pollution.machine.central_vis_tower.tooltip.2",
                    "Scrubs surrounding flux into Infused Dark");
            provider.add("pollution.machine.central_vis_tower.tooltip.3",
                    "Upkeep: EU plus a small amount of Infused Aura (replaces the Botania mana upkeep)");
            provider.add("pollution.machine.central_vis_tower.tooltip.4",
                    "Structure needs 3..8 output fluid hatches and 1 input energy hatch");
            provider.add("pollution.machine.mana_plate.tooltip.1",
                    "Consumes mana to accelerate GT machines above the 11×11 plate");
            provider.add("pollution.machine.mana_plate.tooltip.2",
                    "Speed = mana pool hatch tier; each machine costs 2^(speed-1) mana per tick");
            provider.add("pollution.machine.endoflame_array.tooltip.1",
                    "Endoflame flowers + furnace fuels → mana (the flowers are not consumed)");
            provider.add("pollution.machine.endoflame_array.tooltip.2",
                    "1.5 mana per burn tick per flower, output into a mana output pool hatch");
            provider.add("pollution.machine.mega_mana_turbine.tooltip.1",
                    "Burns the 7 mana fluids (100 mB → -8192 EU/t)");
            provider.add("pollution.machine.mega_mana_turbine.tooltip.2",
                    "Catalyst pairs raise the output cap (black/white mansus, engraved gold + hyperdimensional silver, sentient + binding metal)");
            provider.add("pollution.machine.mega_mana_turbine.tooltip.3",
                    "Parallels ramp from 1 to 32768 while the machine runs continuously");
            provider.add("pollution.machine.magic_large_turbine.tooltip.1",
                    "Burns the MAGIC_TURBINE_FUELS map: aspect fluids, compounds and the two propellants");
            provider.add("pollution.machine.magic_large_turbine.tooltip.2",
                    "Structure: 1 rotor holder (a rotor is required and wears out while running) + 1 mana output hatch + 1 maintenance hatch");
            provider.add("pollution.machine.pollution_multi_dan_de_life_on.tooltip.1",
                    "Runs Conway's Game of Life on the 31×31 cell board above the controller");
            provider.add("pollution.machine.pollution_multi_dan_de_life_on.tooltip.2",
                    "Dead cells are banked by age; mode 0 outputs EU, mode 1 outputs mana fluid");
            provider.add("pollution.machine.tarot_hatch.tooltip.1",
                    "Insert one Major Arcana tarot card to authorize the corresponding ritual recipe");
            provider.add("pollution.machine.tarot_hatch.tooltip.2",
                    "The tarot card is an authorization medium and is never consumed by recipes");
            provider.add("pollution.machine.tarot_hatch.tooltip.3",
                    "Crafted from an LV machine hull, a sensor, a field generator, a blank tarot card and an arcane ink capsule");
            provider.add("pollution.machine.tarot_hatch.active", "Active tarot: %s");
            provider.add("pollution.jei.machine_info.title", "Magic Machine Info");
            provider.add("pollution.jei.magic_hatch.title", "Magic Hatch Info");
            provider.add("pollution.jei.magic_amplification.title", "Amplification Info");
            provider.add("pollution.jei.magic_amplification.tags", "Process tags: %s");
            provider.add("pollution.jei.magic_amplification.constellation",
                    "Constellation (wafer) amplification:");
            provider.add("pollution.jei.magic_amplification.tarot",
                    "Tarot (tarot hatch) amplification:");
            provider.add("pollution.jei.magic_amplification.footer",
                    "Install the matching constellation wafer or tarot hatch to gain the amplification");
            provider.add("pollution.jei.recipe.cost", "Magic cost: %s");
            provider.add("pollution.jei.recipe.cost.vis", "Vis %s/craft");
            provider.add("pollution.jei.recipe.cost.infused", "Infused fluid %s mB/t");
            provider.add("pollution.jei.recipe.cost.mana", "Mana %s/t");
            provider.add("pollution.jei.recipe.cost.life", "Life essence %s/t");
            provider.add("pollution.jei.recipe.gate", "Recipe gate: %s");
            provider.add("pollution.jei.recipe.gate.research", "Research %s");
            provider.add("pollution.jei.recipe.gate.tarot", "Tarot %s");
            provider.add("pollution.jei.recipe.gate.astral", "Astral condition");
            provider.add("pollution.jei.recipe.gate.catalyst", "Catalyst protection input %s");
            provider.add("pollution.jei.recipe.process_tags", "Process tags: %s");
            // Static handbook pages from the 1.12 MagicGuideRecipes. They are
            // JEI explanations; the Astral-dependent lines state the port scope.
            String[][] guideText = {
                    {"seed", "Rock crystal seed", "Grow the seed in a crystal growth environment.",
                            "Growth speed and quality depend on the crystal medium.",
                            "The seed page is informational in this port.",
                            "Astral Sorcery growth machines are excluded from the dependency set.",
                            "Use the matching Astral integration when it is installed.",
                            "See the item tooltip for the stored crystal data."},
                    {"embryo", "Celestial crystal embryo", "The embryo is a celestial crystal growth input.",
                            "Its growth and quality rules belong to Astral Sorcery.",
                            "This page preserves the upstream handbook reference.",
                            "No Astral machine is registered by Pollution Unofficial 1.20.1.",
                            "The item remains available for compatible integrations.",
                            "Use the matching Astral integration when it is installed.",
                            "This page does not create a replacement survival recipe."},
                    {"wafer", "Constellation data wafer", "A wafer stores one constellation id and celestial function.",
                            "The pollution amplification diagnostic can inspect its NBT.",
                            "Matching recipes may read the wafer as a gate.",
                            "Constellation tower production belongs to Astral Sorcery.",
                            "The standalone Starstream network accepts energy through its API.",
                            "No automatic EU-to-constellation conversion is provided.",
                            "The remaining lines describe the upstream constellation channels.",
                            "Aevitas: life and growth.", "Evorsio: processing and change.",
                            "Armara: stability and preservation.", "Discidia: energy and force.",
                            "Vicio: corruption and entropy.", "Mineralis: mineral resonance.",
                            "Fornax: heat and combustion.", "Horologium: time and cycles.",
                            "Lucerna: light and illumination.", "Octans: navigation and geometry.",
                            "Bootes: harvest and collection.", "Pelotrio: motion and exchange.",
                            "Gelu: cold and crystallization.", "Ulteria: distance and travel."},
                    {"tarot", "Major Arcana tarot", "Tarot cards authorize selected magic processes.",
                            "A tarot hatch holds the authorization card without consuming it.",
                            "Recipe data may expose the required tarot id in JEI.",
                            "The Fool: unstable experimental processing.", "The Magician: controlled transformation.",
                            "The High Priestess: research and knowledge.", "The Empress: biological growth.",
                            "The Emperor: structure and control.", "The Hierophant: ritual guidance.",
                            "The Lovers: paired materials.", "The Chariot: motion and transport.",
                            "Strength: high power.", "The Hermit: isolation and purification.",
                            "Wheel of Fortune: chance outputs.", "Justice: balancing inputs.",
                            "The Hanged Man: conversion and sacrifice.", "Death: decomposition.",
                            "Temperance: fluid blending.", "The Devil: dangerous catalysts.",
                            "The Tower: destructive high energy.", "The Star: celestial resonance.",
                            "The Moon: night alchemy.", "The Sun: daylight processes.",
                            "Judgement: renewal and restoration.", "The World: multi-system synthesis.",
                            "Tarot effects are data gates and do not alter card ownership."}
            };
            for (String[] page : guideText) {
                for (int index = 1; index < page.length; index++) {
                    provider.add("pollution.magic.guide." + page[0] + "." + index, page[index]);
                }
            }
        });

        meowmel.pollution.common.warp.PollutionWarpEvents.init();
        meowmel.pollution.common.warp.WarpNetwork.init();

        MinecraftForge.EVENT_BUS.addListener(Pollution::onRegisterCommands);
        MinecraftForge.EVENT_BUS.addListener(PollutionEngine::onServerTick);
        MinecraftForge.EVENT_BUS.addListener(meowmel.pollution.common.warp.WarpEventHandler::onServerTick);

        LOGGER.info("Pollution Unofficial booting: GregTech CEu Modern x Thaumcraft 4R integration");
    }

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        PollutionCommand.register(event.getDispatcher());
    }
}
