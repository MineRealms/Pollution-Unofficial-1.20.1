#!/usr/bin/env python3
"""Generates placeholder machine models for the Pollution port.

GregTech's datagen-time ExistingFileHelper does not see assets inside the GTCEu
jar, so addon machines cannot use GT's `tieredHullModel`/`simpleGeneratorModel`
initialisers (they call getExistingFile on GT models). Instead each machine owns
a small model file in this repository that parents to a GregTech template; the
parent and textures resolve at runtime from the GTCEu jar.

Covered machines (block model key -> tiers):
  vis_generator_<tier>            1..6   (LV..LuV)
  vis_provider_<tier>             1..9   (LV..UHV)
  small_node_generator_<tier>     6..9   (LuV..UHV, placeholder)
  magic_energy_absorber_<tier>    1..5
  flux_scrubber_<tier>            1..5
  flux_fuel_cell_<tier>           1..5
  mana_generator_<tier>           1..5
  source_charge                  (placeholder)
  vis_hatch_<tier>                1..9   (multiblock part)
  infused_fluid_hatch_<tier>      1..9   (multiblock part)
  flux_muffler_<tier>             1..9   (multiblock part)
  mana_(input|output)_hatch_<amp>a_<tier>      1..9 x 1,4,16,64 (multiblock part)
  wireless_mana_(input|output)_hatch_<amp>a_<tier>  1..9 x 1,4,16,64
  mana_pool_(input|output)_hatch_<pool>        diluted/normal/mythic
  wireless_mana_pool_(input|output)_hatch_<pool>
  solar_plate_<tier>_<kind>       1..3 x 1..6
  magic_macerator                (multiblock, placeholder)
  ... all remaining magic/node/botania multiblocks

Overlays
--------
Every model keeps the GT generator template and the GT voltage casing base
textures; only the overlay textures change. `MACHINE_OVERLAYS` maps the model
key (the names used by this generator, e.g. `vis_generator_lv` or
`magic_assembler`) to the texture upstream returned from `getBaseTexture()`:

  * SimpleOverlayRenderer textures (MANA_BASIC, SPELL_PRISM_*, VIS_HATCH,
    MANA_HATCH_*, ...) become `pollution:block/<path>` on the five overlay
    faces. Their emissive layers are `gtceu:block/void` because the upstream
    1.12 textures are plain casings with no emissive pass.
  * Solar plates combine the tier OrientedOverlayRenderer directory
    (`machines/solar_i`, only `overlay_top`/`overlay_bottom` exist) with the
    kind element overlay on the four horizontal faces (`AIR`..`WATER` ->
    `machines/solars/*side`); see MACHINE_TOP/BOTTOM_OVERLAYS.
  * Machines whose upstream renderer is a GregTech standard (`Textures.*`,
    `Textures.HPCA_OVERLAY`, ...) have no Pollution texture to restore and stay
    on the lava-boiler placeholder: `vis_generator`, `vis_provider`,
    `small_node_generator`, `magic_energy_absorber`, `flux_scrubber`,
    `flux_fuel_cell`, `mana_generator`, `flux_muffler`, `source_charge`.
    `gtceu:block/machines/overlay_front` does not exist in gtceu-1.20.1-7.5.3,
    hence the boiler fallback.

small_node_generator_<tier> and source_charge are not wired yet: upstream has
no dedicated textures for them (1.12 MetaTileEntitySmallNodeGenerator renders
with Textures.MAGIC_ENERGY_ABSORBER, MetaTileEntitySourceCharge has no
renderer), so PollutionMachines.java still points these machines at
vis_provider_<tier> and magic_energy_absorber_lv respectively. The dedicated
placeholder models are generated here for the future asset pass, which must
update the simpleModel(...) calls in common/machine/PollutionMachines.java.
"""

from __future__ import annotations

import json
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parents[1]
MODEL_DIR = PROJECT_ROOT / "src" / "main" / "resources" / "assets" / "pollution" / "models" / "block" / "machine"

TIER_NAMES = {
    1: "lv",
    2: "mv",
    3: "hv",
    4: "ev",
    5: "iv",
    6: "luv",
    7: "zpm",
    8: "uv",
    9: "uhv",
}

TIERED_MACHINES = {
    "vis_generator": [1, 2, 3, 4, 5, 6],
    "vis_provider": [1, 2, 3, 4, 5, 6, 7, 8, 9],
    "small_node_generator": [6, 7, 8, 9],
    "magic_energy_absorber": [1, 2, 3, 4, 5],
    "flux_scrubber": [1, 2, 3, 4, 5],
    "flux_fuel_cell": [1, 2, 3, 4, 5],
    "mana_generator": [1, 2, 3, 4, 5],
    "vis_hatch": [1, 2, 3, 4, 5, 6, 7, 8, 9],
    "infused_fluid_hatch": [1, 2, 3, 4, 5, 6, 7, 8, 9],
    "flux_muffler": [1, 2, 3, 4, 5, 6, 7, 8, 9],
    "mana_input_hatch_1a": [1, 2, 3, 4, 5, 6, 7, 8, 9],
    "mana_input_hatch_4a": [1, 2, 3, 4, 5, 6, 7, 8, 9],
    "mana_input_hatch_16a": [1, 2, 3, 4, 5, 6, 7, 8, 9],
    "mana_input_hatch_64a": [1, 2, 3, 4, 5, 6, 7, 8, 9],
    "mana_output_hatch_1a": [1, 2, 3, 4, 5, 6, 7, 8, 9],
    "mana_output_hatch_4a": [1, 2, 3, 4, 5, 6, 7, 8, 9],
    "mana_output_hatch_16a": [1, 2, 3, 4, 5, 6, 7, 8, 9],
    "mana_output_hatch_64a": [1, 2, 3, 4, 5, 6, 7, 8, 9],
    "wireless_mana_input_hatch_1a": [1, 2, 3, 4, 5, 6, 7, 8, 9],
    "wireless_mana_input_hatch_4a": [1, 2, 3, 4, 5, 6, 7, 8, 9],
    "wireless_mana_input_hatch_16a": [1, 2, 3, 4, 5, 6, 7, 8, 9],
    "wireless_mana_input_hatch_64a": [1, 2, 3, 4, 5, 6, 7, 8, 9],
    "wireless_mana_output_hatch_1a": [1, 2, 3, 4, 5, 6, 7, 8, 9],
    "wireless_mana_output_hatch_4a": [1, 2, 3, 4, 5, 6, 7, 8, 9],
    "wireless_mana_output_hatch_16a": [1, 2, 3, 4, 5, 6, 7, 8, 9],
    "wireless_mana_output_hatch_64a": [1, 2, 3, 4, 5, 6, 7, 8, 9],
}

SOLAR_TIERS = [1, 2, 3]
SOLAR_KINDS = [1, 2, 3, 4, 5, 6]

MULTIBLOCKS = {
    "magic_macerator": "hv",
    "magic_bender": "hv",
    "magic_centrifuge": "hv",
    "magic_wiremill": "mv",
    "magic_autoclave": "hv",
    "magic_electrolyzer": "hv",
    "magic_extruder": "hv",
    "magic_mixer": "hv",
    "magic_sifter": "hv",
    "magic_solidifier": "hv",
    "magic_brewery": "hv",
    "magic_cutter": "hv",
    "magic_green_house": "hv",
    "magic_electric_blast_furnace": "hv",
    "magic_alloy_blast": "ev",
    "magic_chemical_bath": "hv",
    "magic_chemical_reactor": "ev",
    "magic_distillery": "ev",
    "magic_assembler": "hv",
    "infused_exchange": "hv",
    "essence_smelter": "hv",
    "node_producer": "ev",
    "large_node_generator": "iv",
    "node_washer": "ev",
    "node_blast_furnace": "iv",
    "luv_node_fusion_reactor": "luv",
    "zpm_node_fusion_reactor": "zpm",
    "uv_node_fusion_reactor": "uv",
    "central_vis_tower": "iv",
    "gt_essence_smelter": "hv",
    "essence_collector": "ev",
    "industrial_infusion": "ev",
    "small_chemical_plant": "ev",
    "magic_fusion_reactor": "luv",
    "magic_battery": "iv",
    "magic_large_turbine": "hv",
    "magic_mega_turbine": "ev",
    "mana_plate": "lv",
    "mana_petal_apothecary": "ev",
    "mana_rune_altar": "ev",
    "industrial_pure_daisy": "hv",
    "bot_distillery": "ev",
    "bot_vacuum_freezer": "iv",
    "bot_circuit_assembler": "ev",
    "bot_gas_collector": "iv",
    "endoflame_array": "iv",
    "mana_infusion_reactor": "ev",
    "mega_mana_turbine": "zpm",
    "pollution_multi_dan_de_life_on": "uv",
    "mana_pool_input_hatch_diluted": "lv",
    "mana_pool_input_hatch_normal": "luv",
    "mana_pool_input_hatch_mythic": "uev",
    "mana_pool_output_hatch_diluted": "lv",
    "mana_pool_output_hatch_normal": "luv",
    "mana_pool_output_hatch_mythic": "uev",
    "wireless_mana_pool_input_hatch_diluted": "lv",
    "wireless_mana_pool_input_hatch_normal": "luv",
    "wireless_mana_pool_input_hatch_mythic": "uev",
    "wireless_mana_pool_output_hatch_diluted": "lv",
    "wireless_mana_pool_output_hatch_normal": "luv",
    "wireless_mana_pool_output_hatch_mythic": "uev",
}

SINGLE_MACHINES = {
    "source_charge": "lv",
}

# ---------------------------------------------------------------------------
# Overlays
# ---------------------------------------------------------------------------

# `gtceu:block/machines/overlay_front` does not exist in gtceu-1.20.1-7.5.3
# (checked in the jar), so the lava boiler stays the neutral fallback for
# machines whose upstream renderer is a GregTech standard (Textures.*).
DEFAULT_OVERLAY = "gtceu:block/generators/boiler/lava/overlay_front"
# GTCEu's fully transparent texture: used for the emissive layers of machines
# restored from 1.12 SimpleOverlayRenderer textures (no emissive pass upstream).
TRANSPARENT_OVERLAY = "gtceu:block/void"


def pollution(path: str) -> str:
    """Qualify a texture path with the Pollution namespace."""
    return f"pollution:block/{path}"


# Upstream multiblock getBaseTexture() -> POTextures constant -> texture path:
#   MetaTileEntityMagicMacerator          SPELL_PRISM_EARTH   magicblock/spell_prism_earth
#   MetaTileEntityMagicBender             SPELL_PRISM_ORDER   magicblock/spell_prism_order
#   MetaTileEntityMagicCentrifuge         SPELL_PRISM_AIR     magicblock/spell_prism_air
#   MetaTileEntityMagicWireMill           SPELL_PRISM_WATER   magicblock/spell_prism_water
#   MetaTileEntityMagicAutoclave          SPELL_PRISM_AIR     magicblock/spell_prism_air
#   MetaTileEntityMagicElectrolyzer       SPELL_PRISM_ORDER   magicblock/spell_prism_order
#   MetaTileEntityMagicExtruder           SPELL_PRISM_ORDER   magicblock/spell_prism_order
#   MetaTileEntityMagicMixer              SPELL_PRISM_VOID    magicblock/spell_prism_void
#   MetaTileEntityMagicSifter             SPELL_PRISM_EARTH   magicblock/spell_prism_earth
#   MetaTileEntityMagicSolidifier         SPELL_PRISM_ORDER   magicblock/spell_prism_order
#   MetaTileEntityMagicBrewery            SPELL_PRISM_COLD    magicblock/spell_prism_cold
#   MetaTileEntityMagicCutter             SPELL_PRISM_HOT     magicblock/spell_prism_hot
#   MetaTileEntityMagicGreenHouse         SPELL_PRISM_WATER   magicblock/spell_prism_water
#   MetaTileEntityMagicElectricBlastFurnace SPELL_PRISM_HOT   magicblock/spell_prism_hot
#   MetaTileEntityMagicAlloyBlastSmelter  SPELL_PRISM_HOT     magicblock/spell_prism_hot
#   MetaTileEntityMagicChemicalBath       SPELL_PRISM_WATER   magicblock/spell_prism_water
#   MetaTileEntityMagicChemicalReactor    SPELL_PRISM_WATER   magicblock/spell_prism_water
#   MetaTileEntityMagicDistillery         SPELL_PRISM_COLD    magicblock/spell_prism_cold
#   MetaTileEntityMagicAssembler          MANA_BASIC          magicblock/mana_basic
#   MetaTileEntityInfusedExchange         VOID_PRISM          magicblock/void_prism
#   MetaTileEntityEssenceSmelter          SPELL_PRISM_VOID    magicblock/spell_prism_void
#   MetaTileEntityNodeProducer            FRAME_I             fusion_reactor/frame_ii
#   MetaTileEntityLargeNodeGenerator      FRAME_I             fusion_reactor/frame_ii
#   MetaTileEntityNodeWasher              SPELL_PRISM_HOT     magicblock/spell_prism_hot
#   MetaTileEntityNodeBlastFurnace        MANA_BASIC          magicblock/mana_basic
#   MetaTileEntityNodeFusionReactor       HYPER_1/2/3         hyper/hyper_1..3
#   MetaTileEntityCentralVisTower         MANA_BASIC          magicblock/mana_basic
#   MetaTileEntityGtEssenceSmelter        SPELL_PRISM_VOID    magicblock/spell_prism_void
#   MetaTileEntityEssenceCollector        SPELL_PRISM         magicblock/spell_prism
#   MetaTileEntityIndustrialInfusion      SPELL_PRISM_VOID    magicblock/spell_prism_void
#   MetaTileEntitySmallChemicalPlant      TERRA_WATERTIGHT_CASING botblock/terra_watertight_casing
#   MetaTileEntityMagicFusionReactor      FRAME_I             fusion_reactor/frame_ii
#   MetaTileEntityMagicBattery            MAGIC_BATTERY       magicblock/magic_battery
#   MetaTileEntityMagicLargeTurbine       SPELL_PRISM_HOT     magicblock/spell_prism_hot
#   MetaTileEntityMagicMegaTurbine        SPELL_PRISM_HOT     magicblock/spell_prism_hot
#   MetaTileEntityManaPlate               MANA_BASIC          magicblock/mana_basic
#   MetaTileEntityManaPetalApothecary     Livingrock_0        botblock/livingrock0
#   MetaTileEntityManaRuneAltar           Livingrock_0        botblock/livingrock0
#   MetaTileEntityIndustrialPureDaisy     Livingrock_0        botblock/livingrock0
#   MetaTileEntityBotDistillery           TERRA_WATERTIGHT_CASING botblock/terra_watertight_casing
#   MetaTileEntityBotVacuumFreezer        MANA_4              magicblock/mana_4
#   MetaTileEntityBotCircuitAssembler     MANA_5              magicblock/mana_5
#   MetaTileEntityBotGasCollector         TERRA_5_CASING      botblock/terra_5_casing
#   MetaTileEntityEndoflameArray          TERRA_4_CASING      botblock/terra_4_casing
#   MetaTileEntityManaInfusionReactor     Livingrock_0        botblock/livingrock0
#   MetaTileEntityMegaManaTurbine         MANA_5              magicblock/mana_5
#   MetaTileEntityMultiDanDeLifeOn        MANA_4              magicblock/mana_4
#   MetaTileEntityManaPoolHatch           MANA_POOL_HATCH_*   overlay/machine/magic_hatch/mana_pool_*
MULTIBLOCK_OVERLAYS = {
    "magic_macerator": "magicblock/spell_prism_earth",
    "magic_bender": "magicblock/spell_prism_order",
    "magic_centrifuge": "magicblock/spell_prism_air",
    "magic_wiremill": "magicblock/spell_prism_water",
    "magic_autoclave": "magicblock/spell_prism_air",
    "magic_electrolyzer": "magicblock/spell_prism_order",
    "magic_extruder": "magicblock/spell_prism_order",
    "magic_mixer": "magicblock/spell_prism_void",
    "magic_sifter": "magicblock/spell_prism_earth",
    "magic_solidifier": "magicblock/spell_prism_order",
    "magic_brewery": "magicblock/spell_prism_cold",
    "magic_cutter": "magicblock/spell_prism_hot",
    "magic_green_house": "magicblock/spell_prism_water",
    "magic_electric_blast_furnace": "magicblock/spell_prism_hot",
    "magic_alloy_blast": "magicblock/spell_prism_hot",
    "magic_chemical_bath": "magicblock/spell_prism_water",
    "magic_chemical_reactor": "magicblock/spell_prism_water",
    "magic_distillery": "magicblock/spell_prism_cold",
    "magic_assembler": "magicblock/mana_basic",
    "infused_exchange": "magicblock/void_prism",
    "essence_smelter": "magicblock/spell_prism_void",
    "node_producer": "fusion_reactor/frame_ii",
    "large_node_generator": "fusion_reactor/frame_ii",
    "node_washer": "magicblock/spell_prism_hot",
    "node_blast_furnace": "magicblock/mana_basic",
    "luv_node_fusion_reactor": "hyper/hyper_1",
    "zpm_node_fusion_reactor": "hyper/hyper_2",
    "uv_node_fusion_reactor": "hyper/hyper_3",
    "central_vis_tower": "magicblock/mana_basic",
    "gt_essence_smelter": "magicblock/spell_prism_void",
    "essence_collector": "magicblock/spell_prism",
    "industrial_infusion": "magicblock/spell_prism_void",
    "small_chemical_plant": "botblock/terra_watertight_casing",
    "magic_fusion_reactor": "fusion_reactor/frame_ii",
    "magic_battery": "magicblock/magic_battery",
    "magic_large_turbine": "magicblock/spell_prism_hot",
    "magic_mega_turbine": "magicblock/spell_prism_hot",
    "mana_plate": "magicblock/mana_basic",
    "mana_petal_apothecary": "botblock/livingrock0",
    "mana_rune_altar": "botblock/livingrock0",
    "industrial_pure_daisy": "botblock/livingrock0",
    "bot_distillery": "botblock/terra_watertight_casing",
    "bot_vacuum_freezer": "magicblock/mana_4",
    "bot_circuit_assembler": "magicblock/mana_5",
    "bot_gas_collector": "botblock/terra_5_casing",
    "endoflame_array": "botblock/terra_4_casing",
    "mana_infusion_reactor": "botblock/livingrock0",
    "mega_mana_turbine": "magicblock/mana_5",
    "pollution_multi_dan_de_life_on": "magicblock/mana_4",
    "mana_pool_input_hatch_diluted": "overlay/machine/magic_hatch/mana_pool_input",
    "mana_pool_input_hatch_normal": "overlay/machine/magic_hatch/mana_pool_input",
    "mana_pool_input_hatch_mythic": "overlay/machine/magic_hatch/mana_pool_input",
    "mana_pool_output_hatch_diluted": "overlay/machine/magic_hatch/mana_pool_output",
    "mana_pool_output_hatch_normal": "overlay/machine/magic_hatch/mana_pool_output",
    "mana_pool_output_hatch_mythic": "overlay/machine/magic_hatch/mana_pool_output",
    "wireless_mana_pool_input_hatch_diluted": "overlay/machine/magic_hatch/wireless_mana_pool_input",
    "wireless_mana_pool_input_hatch_normal": "overlay/machine/magic_hatch/wireless_mana_pool_input",
    "wireless_mana_pool_input_hatch_mythic": "overlay/machine/magic_hatch/wireless_mana_pool_input",
    "wireless_mana_pool_output_hatch_diluted": "overlay/machine/magic_hatch/wireless_mana_pool_output",
    "wireless_mana_pool_output_hatch_normal": "overlay/machine/magic_hatch/wireless_mana_pool_output",
    "wireless_mana_pool_output_hatch_mythic": "overlay/machine/magic_hatch/wireless_mana_pool_output",
}

# model key -> overlay texture; keys not present use DEFAULT_OVERLAY.
MACHINE_OVERLAYS: dict[str, str] = {
    name: pollution(path) for name, path in MULTIBLOCK_OVERLAYS.items()
}

# Optional per-face overrides (model key -> top/bottom texture). Solar plates
# combine the tier OrientedOverlayRenderer ("machines/solar_<tier>", only
# overlay_top/overlay_bottom exist) with the kind element overlay on the four
# horizontal faces (POTextures.AIR..WATER -> machines/solars/*side).
MACHINE_TOP_OVERLAYS: dict[str, str] = {}
MACHINE_BOTTOM_OVERLAYS: dict[str, str] = {}
SOLAR_SIDE_OVERLAYS = {
    1: "machines/solars/airside",
    2: "machines/solars/darkside",
    3: "machines/solars/earthside",
    4: "machines/solars/fireside",
    5: "machines/solars/orderside",
    6: "machines/solars/waterside",
}
for tier, directory in ((1, "solar_i"), (2, "solar_ii"), (3, "solar_iii")):
    for kind in SOLAR_KINDS:
        key = f"solar_plate_{tier}_{kind}"
        MACHINE_OVERLAYS[key] = pollution(SOLAR_SIDE_OVERLAYS[kind])
        MACHINE_TOP_OVERLAYS[key] = pollution(f"machines/{directory}/overlay_top")
        MACHINE_BOTTOM_OVERLAYS[key] = pollution(f"machines/{directory}/overlay_bottom")


def _tiered_overlay(machine: str, path: str) -> None:
    for tier in TIERED_MACHINES[machine]:
        MACHINE_OVERLAYS[f"{machine}_{TIER_NAMES[tier]}"] = pollution(path)


# multiblock parts: upstream getBaseTexture()/renderMetaTileEntity() overlays.
_tiered_overlay("vis_hatch", "overlay/machine/magic_hatch/vis_hatch")
_tiered_overlay("infused_fluid_hatch", "overlay/machine/magic_hatch/infused_fluid_hatch")
for amp in ("1a", "4a", "16a", "64a"):
    _tiered_overlay(f"mana_input_hatch_{amp}", f"overlay/machine/magic_hatch/mana_input_{amp}")
    _tiered_overlay(f"mana_output_hatch_{amp}", f"overlay/machine/magic_hatch/mana_output_{amp}")
    _tiered_overlay(f"wireless_mana_input_hatch_{amp}", "overlay/machine/magic_hatch/wireless_mana_input")
    _tiered_overlay(f"wireless_mana_output_hatch_{amp}", "overlay/machine/magic_hatch/wireless_mana_output")


def machine_model(key: str, casing_tier: str) -> dict:
    casing = f"gtceu:block/casings/voltage/{casing_tier}"
    mapped = key in MACHINE_OVERLAYS
    overlay = MACHINE_OVERLAYS.get(key, DEFAULT_OVERLAY)
    top = MACHINE_TOP_OVERLAYS.get(key, overlay)
    bottom = MACHINE_BOTTOM_OVERLAYS.get(key, overlay)
    emissive = TRANSPARENT_OVERLAY if mapped else DEFAULT_OVERLAY
    return {
        "parent": "gtceu:block/machine/template/generator_machine",
        "textures": {
            "bottom": f"{casing}/bottom",
            "top": f"{casing}/top",
            "side": f"{casing}/side",
            "overlay_front": overlay,
            "overlay_back": overlay,
            "overlay_top": top,
            "overlay_bottom": bottom,
            "overlay_side": overlay,
            "overlay_front_emissive": emissive,
            "overlay_back_emissive": emissive,
            "overlay_top_emissive": emissive,
            "overlay_bottom_emissive": emissive,
            "overlay_side_emissive": emissive,
        },
    }


def write_model(key: str, casing_tier: str) -> None:
    target = MODEL_DIR / f"{key}.json"
    target.write_text(json.dumps(machine_model(key, casing_tier), indent=2) + "\n", encoding="utf-8")
    print(f"wrote {target.relative_to(PROJECT_ROOT)}")


def main() -> int:
    MODEL_DIR.mkdir(parents=True, exist_ok=True)
    for name, tiers in TIERED_MACHINES.items():
        for tier in tiers:
            write_model(f"{name}_{TIER_NAMES[tier]}", TIER_NAMES[tier])
    for tier in SOLAR_TIERS:
        for kind in SOLAR_KINDS:
            write_model(f"solar_plate_{tier}_{kind}", TIER_NAMES[tier])
    for name, casing_tier in MULTIBLOCKS.items():
        write_model(name, casing_tier)
    for name, casing_tier in SINGLE_MACHINES.items():
        write_model(name, casing_tier)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
