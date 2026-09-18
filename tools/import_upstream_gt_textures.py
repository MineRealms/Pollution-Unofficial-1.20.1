#!/usr/bin/env python3
"""Imports the upstream gregtech-namespace textures and re-points our models.

1. copies assets/gregtech/textures/blocks/** from upstream into
   assets/pollution/textures/block/<subpath>/<name>.png
2. rewrites the casing block models (cube_all) to use those textures instead of
   the GTCEu voltage casings
3. rewrites 1.12 vanilla texture names (leaves_oak, sapling_oak, ...) that the
   converted models still reference to their 1.20.1 names
"""

from __future__ import annotations

import json
import re
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
UP_GT = Path(r"H:\MinecraftMods\Pollution\src\main\resources\assets\gregtech\textures\blocks")
DEST = ROOT / "src/main/resources/assets/pollution"
MODELS = DEST / "models/block"

CASING_TEX = {
    "void_prism": "magicblock/void_prism",
    "spell_prism": "magicblock/spell_prism",
    "spell_prism_cold": "magicblock/spell_prism_cold",
    "spell_prism_hot": "magicblock/spell_prism_hot",
    "spell_prism_water": "magicblock/spell_prism_water",
    "spell_prism_order": "magicblock/spell_prism_order",
    "spell_prism_air": "magicblock/spell_prism_air",
    "spell_prism_earth": "magicblock/spell_prism_earth",
    "spell_prism_void": "magicblock/spell_prism_void",
    "alloy_blast_casing": "magicblock/alloy_blast_casing",
    "magic_battery_casing": "magicblock/magic_battery",
    "mana_basic": "magicblock/mana_basic",
    "mana_1": "magicblock/mana_1",
    "mana_2": "magicblock/mana_2",
    "mana_3": "magicblock/mana_3",
    "mana_4": "magicblock/mana_4",
    "mana_5": "magicblock/mana_5",
    "beam_core_0": "beamcore/beam_core_0",
    "beam_core_1": "beamcore/beam_core_1",
    "beam_core_2": "beamcore/beam_core_2",
    "beam_core_3": "beamcore/beam_core_3",
    "beam_core_4": "beamcore/beam_core_4",
    "filter_1": "beamcore/filter_1",
    "filter_2": "beamcore/filter_2",
    "filter_3": "beamcore/filter_3",
    "filter_4": "beamcore/filter_4",
    "filter_5": "beamcore/filter_5",
    "terra_watertight_casing": "botblock/terra_watertight_casing",
    "terra_1_casing": "botblock/terra_1_casing",
    "terra_2_casing": "botblock/terra_2_casing",
    "terra_3_casing": "botblock/terra_3_casing",
    "terra_4_casing": "botblock/terra_4_casing",
    "terra_5_casing": "botblock/terra_5_casing",
    "terra_6_casing": "botblock/terra_6_casing",
    "bronze_gearbox": "turbine/machine_casing_gearbox_bronze",
    "steel_gearbox": "turbine/machine_casing_gearbox_steel",
    "stainless_steel_gearbox": "turbine/machine_casing_gearbox_stainless_steel",
    "titanium_gearbox": "turbine/machine_casing_gearbox_titanium",
    "tungstensteel_gearbox": "turbine/machine_casing_gearbox_tungstensteel",
    "bronze_pipe": "turbine/machine_casing_pipe_bronze",
    "steel_pipe": "turbine/machine_casing_pipe_steel",
    "titanium_pipe": "turbine/machine_casing_pipe_titanium",
    "tungstensteel_pipe": "turbine/machine_casing_pipe_tungstensteel",
    "polytetrafluoroethylene_pipe": "turbine/machine_casing_pipe_polytetrafluoroethylene",
    "laminated_glass": "glass/laminated_glass",
    "aaminated_glass": "glass/aaminated_glass",
    "baminated_glass": "glass/baminated_glass",
    "caminated_glass": "glass/caminated_glass",
    "daminated_glass": "glass/daminated_glass",
}

VANILLA_112 = {
    "leaves_oak": "oak_leaves",
    "leaves_big_oak": "dark_oak_leaves",
    "leaves_birch": "birch_leaves",
    "leaves_spruce": "spruce_leaves",
    "sapling_oak": "oak_sapling",
    "sapling_big_oak": "dark_oak_sapling",
    "planks_oak": "oak_planks",
    "planks_big_oak": "dark_oak_planks",
    "log_oak": "oak_log",
    "log_oak_top": "oak_log_top",
    "dirt": "dirt",
    "grass": "grass_block_top",
    "water_still": "water_still",
    "lava_still": "lava_still",
    "stone": "stone",
    "cobblestone": "cobblestone",
    "gravel": "gravel",
    "sand": "sand",
    "soul_sand": "soul_sand",
    "netherrack": "netherrack",
}


def main() -> int:
    copied = 0
    for png in UP_GT.rglob("*.png"):
        rel = png.relative_to(UP_GT)
        target = DEST / "textures/block" / rel
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(png, target)
        copied += 1

    rewritten = 0
    for name, subpath in CASING_TEX.items():
        model = MODELS / f"{name}.json"
        if not model.exists():
            continue
        payload = {
            "parent": "block/cube_all",
            "textures": {"all": f"pollution:block/{subpath}"},
        }
        model.write_text(json.dumps(payload, indent=2) + "\n", encoding="utf-8")
        rewritten += 1

    fixed = 0
    for model in MODELS.glob("*.json"):
        text = model.read_text(encoding="utf-8")
        original = text
        for old, new in VANILLA_112.items():
            text = text.replace(f"minecraft:block/{old}", f"minecraft:block/{new}")
        if text != original:
            model.write_text(text, encoding="utf-8")
            fixed += 1

    print(f"gregtech textures copied: {copied}, casing models rewritten: {rewritten}, "
          f"vanilla-name models fixed: {fixed}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
