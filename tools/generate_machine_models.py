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
  magic_energy_absorber_<tier>    1..5
  flux_scrubber_<tier>            1..5
  flux_fuel_cell_<tier>           1..5
  vis_hatch_<tier>                1..9   (multiblock part)
  infused_fluid_hatch_<tier>      1..9   (multiblock part)
  flux_muffler_<tier>             1..9   (multiblock part)
  solar_plate_<tier>_<kind>       1..3 x 1..6
  magic_macerator                (multiblock, placeholder)

All placeholders use GregTech's voltage casing textures and the lava boiler
front overlay until ported Pollution textures exist.
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
    "magic_energy_absorber": [1, 2, 3, 4, 5],
    "flux_scrubber": [1, 2, 3, 4, 5],
    "flux_fuel_cell": [1, 2, 3, 4, 5],
    "vis_hatch": [1, 2, 3, 4, 5, 6, 7, 8, 9],
    "infused_fluid_hatch": [1, 2, 3, 4, 5, 6, 7, 8, 9],
    "flux_muffler": [1, 2, 3, 4, 5, 6, 7, 8, 9],
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
}

OVERLAY = "gtceu:block/generators/boiler/lava/overlay_front"


def machine_model(casing_tier: str) -> dict:
    casing = f"gtceu:block/casings/voltage/{casing_tier}"
    return {
        "parent": "gtceu:block/machine/template/generator_machine",
        "textures": {
            "bottom": f"{casing}/bottom",
            "top": f"{casing}/top",
            "side": f"{casing}/side",
            "overlay_front": OVERLAY,
            "overlay_back": OVERLAY,
            "overlay_top": OVERLAY,
            "overlay_bottom": OVERLAY,
            "overlay_side": OVERLAY,
            "overlay_front_emissive": OVERLAY,
            "overlay_back_emissive": OVERLAY,
            "overlay_top_emissive": OVERLAY,
            "overlay_bottom_emissive": OVERLAY,
            "overlay_side_emissive": OVERLAY,
        },
    }


def write_model(key: str, casing_tier: str) -> None:
    target = MODEL_DIR / f"{key}.json"
    target.write_text(json.dumps(machine_model(casing_tier), indent=2) + "\n", encoding="utf-8")
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
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
