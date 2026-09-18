#!/usr/bin/env python3
"""Generates block assets for the Pollution magic casings.

Modern GregTech has no variant-block helper for addon casings, so the upstream
variant blocks are split into one block per variant. Models are placeholders:
metal casings reuse GregTech voltage-casing textures, the glass family uses the
vanilla glass texture, until the 1.12 textures are converted in the asset pass.
"""

from __future__ import annotations

import json
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parents[1]
ASSETS = PROJECT_ROOT / "src" / "main" / "resources" / "assets" / "pollution"
BLOCKSTATES = ASSETS / "blockstates"
BLOCK_MODELS = ASSETS / "models" / "block"
ITEM_MODELS = ASSETS / "models" / "item"

GLASS_TEXTURE = "minecraft:block/glass"

CASINGS = {
    # POMagicBlock variants (upstream "magic_block")
    "void_prism": "gtceu:block/casings/voltage/uv/side",
    "spell_prism": "gtceu:block/casings/voltage/hv/side",
    "spell_prism_cold": "gtceu:block/casings/voltage/iv/side",
    "spell_prism_hot": "gtceu:block/casings/voltage/ev/side",
    "spell_prism_water": "gtceu:block/casings/voltage/mv/side",
    "spell_prism_order": "gtceu:block/casings/voltage/zpm/side",
    "spell_prism_air": "gtceu:block/casings/voltage/luv/side",
    "spell_prism_earth": "gtceu:block/casings/voltage/hv/side",
    "spell_prism_void": "gtceu:block/casings/voltage/uv/side",
    "alloy_blast_casing": "gtceu:block/casings/voltage/ev/side",
    "magic_battery_casing": "gtceu:block/casings/voltage/iv/side",
    # POMBeamCore variants
    "beam_core_0": "gtceu:block/casings/voltage/lv/side",
    "beam_core_1": "gtceu:block/casings/voltage/mv/side",
    "beam_core_2": "gtceu:block/casings/voltage/hv/side",
    "beam_core_3": "gtceu:block/casings/voltage/ev/side",
    "beam_core_4": "gtceu:block/casings/voltage/iv/side",
    "filter_1": "gtceu:block/casings/voltage/lv/side",
    "filter_2": "gtceu:block/casings/voltage/mv/side",
    "filter_3": "gtceu:block/casings/voltage/hv/side",
    "filter_4": "gtceu:block/casings/voltage/ev/side",
    "filter_5": "gtceu:block/casings/voltage/iv/side",
    # POGlass variants
    "laminated_glass": GLASS_TEXTURE,
    "aaminated_glass": GLASS_TEXTURE,
    "baminated_glass": GLASS_TEXTURE,
    "caminated_glass": GLASS_TEXTURE,
    "daminated_glass": GLASS_TEXTURE,
}


def write_json(path: Path, payload: dict) -> None:
    path.write_text(json.dumps(payload, indent=2) + "\n", encoding="utf-8")


def main() -> int:
    for directory in (BLOCKSTATES, BLOCK_MODELS, ITEM_MODELS):
        directory.mkdir(parents=True, exist_ok=True)
    for name, texture in CASINGS.items():
        write_json(BLOCKSTATES / f"{name}.json", {
            "variants": {"": {"model": f"pollution:block/{name}"}},
        })
        write_json(BLOCK_MODELS / f"{name}.json", {
            "parent": "minecraft:block/cube_all",
            "textures": {"all": texture},
        })
        write_json(ITEM_MODELS / f"{name}.json", {
            "parent": f"pollution:block/{name}",
        })
        print(f"wrote casing assets for {name}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
