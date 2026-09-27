package meowmel.pollution.common.machine;

import com.gregtechceu.gtceu.common.data.GTBlocks;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import meowmel.pollution.common.block.PollutionMagicBlocks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import vazkii.botania.common.block.BotaniaBlocks;

/** Casing identity shared by the controller model, formed hatch skins and CTM. */
public final class PollutionMachineAppearance {
    private PollutionMachineAppearance() {}

    public static Block casing(String name) {
        if (name.startsWith("constellation_tower")) return PollutionMagicBlocks.STARSTREAM_CASING.get();
        if (name.endsWith("node_fusion_reactor")) return PollutionMagicBlocks.SPELL_PRISM_VOID.get();
        return switch (name) {
            case "industrial_starlight_infuser", "industrial_lightwell", "celestial_observation_array",
                    "celestial_calibration_matrix", "celestial_crystal_growth_array" -> BlocksAS.MARBLE_BRICKS.get();
            case "magic_macerator", "magic_sifter" -> PollutionMagicBlocks.SPELL_PRISM_EARTH.get();
            case "magic_bender", "magic_electrolyzer", "magic_extruder", "magic_solidifier" -> PollutionMagicBlocks.SPELL_PRISM_ORDER.get();
            case "magic_centrifuge", "magic_autoclave" -> PollutionMagicBlocks.SPELL_PRISM_AIR.get();
            case "magic_wiremill", "magic_chemical_bath", "magic_chemical_reactor", "magic_green_house" -> PollutionMagicBlocks.SPELL_PRISM_WATER.get();
            case "magic_brewery", "magic_distillery" -> PollutionMagicBlocks.SPELL_PRISM_COLD.get();
            case "magic_cutter", "magic_electric_blast_furnace", "magic_alloy_blast", "node_washer",
                    "magic_large_turbine", "magic_mega_turbine" -> PollutionMagicBlocks.SPELL_PRISM_HOT.get();
            case "magic_mixer", "essence_smelter", "gt_essence_smelter", "industrial_infusion" -> PollutionMagicBlocks.SPELL_PRISM_VOID.get();
            case "magic_assembler", "node_blast_furnace", "central_vis_tower", "mana_plate" -> PollutionMagicBlocks.MANA_BASIC.get();
            case "infused_exchange" -> PollutionMagicBlocks.VOID_PRISM.get();
            case "essence_collector" -> PollutionMagicBlocks.SPELL_PRISM.get();
            case "node_producer", "large_node_generator" -> GTBlocks.FUSION_CASING.get();
            case "small_chemical_plant", "bot_distillery" -> PollutionMagicBlocks.TERRA_WATERTIGHT_CASING.get();
            case "magic_battery" -> PollutionMagicBlocks.MAGIC_BATTERY_CASING.get();
            case "mana_petal_apothecary", "mana_rune_altar", "industrial_pure_daisy", "mana_infusion_reactor" -> BotaniaBlocks.livingrockBrick;
            case "bot_vacuum_freezer", "pollution_multi_dan_de_life_on" -> PollutionMagicBlocks.MANA_4.get();
            case "bot_circuit_assembler", "mega_mana_turbine", "mega_mana_rotor_turbine" -> PollutionMagicBlocks.MANA_5.get();
            case "large_mana_turbine" -> PollutionMagicBlocks.MANA_3.get();
            case "bot_gas_collector" -> PollutionMagicBlocks.TERRA_5_CASING.get();
            case "endoflame_array" -> PollutionMagicBlocks.TERRA_4_CASING.get();
            // The legacy frame has no opaque hull. Retain its upstream controller skin.
            case "magic_fusion_reactor" -> com.gregtechceu.gtceu.common.data.GTMaterialBlocks.MATERIAL_BLOCKS.get(
                    com.gregtechceu.gtceu.api.data.tag.TagPrefix.frameGt,
                    com.gregtechceu.gtceu.common.data.GTMaterials.TungstenSteel).get();
            default -> throw new IllegalArgumentException("Missing multiblock casing: " + name);
        };
    }

    public static ResourceLocation texture(String name) {
        if (name.startsWith("constellation_tower")) return id("pollution", "block/starstream/starstream_casing");
        // Model registration runs before deferred blocks can safely be dereferenced.
        if (name.equals("node_producer") || name.equals("large_node_generator")) return id("gtceu", "block/casings/fusion/fusion_casing");
        if (java.util.Set.of("mana_petal_apothecary", "mana_rune_altar", "industrial_pure_daisy", "mana_infusion_reactor").contains(name)) {
            return id("botania", "block/livingrock_bricks");
        }
        if (name.equals("magic_fusion_reactor")) return id("pollution", "block/fusion_reactor/frame_ii");
        if (name.startsWith("industrial_star") || name.equals("industrial_lightwell") || name.startsWith("celestial_")) {
            return id("astralsorcery", "block/marble_bricks");
        }
        String path = name.endsWith("node_fusion_reactor") ? "spell_prism_void" : switch (name) {
            case "magic_macerator", "magic_sifter" -> "spell_prism_earth";
            case "magic_bender", "magic_electrolyzer", "magic_extruder", "magic_solidifier" -> "spell_prism_order";
            case "magic_centrifuge", "magic_autoclave" -> "spell_prism_air";
            case "magic_wiremill", "magic_chemical_bath", "magic_chemical_reactor", "magic_green_house" -> "spell_prism_water";
            case "magic_brewery", "magic_distillery" -> "spell_prism_cold";
            case "magic_cutter", "magic_electric_blast_furnace", "magic_alloy_blast", "node_washer", "magic_large_turbine", "magic_mega_turbine" -> "spell_prism_hot";
            case "magic_mixer", "essence_smelter", "gt_essence_smelter", "industrial_infusion" -> "spell_prism_void";
            case "magic_assembler", "node_blast_furnace", "central_vis_tower", "mana_plate" -> "mana_basic";
            case "infused_exchange" -> "void_prism";
            case "essence_collector" -> "spell_prism";
            case "small_chemical_plant", "bot_distillery" -> "terra_watertight_casing";
            case "magic_battery" -> "magic_battery";
            case "bot_vacuum_freezer", "pollution_multi_dan_de_life_on" -> "mana_4";
            case "bot_circuit_assembler", "mega_mana_turbine", "mega_mana_rotor_turbine" -> "mana_5";
            case "large_mana_turbine" -> "mana_3";
            case "bot_gas_collector" -> "terra_5_casing";
            case "endoflame_array" -> "terra_4_casing";
            default -> throw new IllegalArgumentException("Missing multiblock texture: " + name);
        };
        return id("pollution", "block/" + (path.startsWith("terra_") ? "botblock/" : "magicblock/") + path);
    }

    private static ResourceLocation id(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }
}
