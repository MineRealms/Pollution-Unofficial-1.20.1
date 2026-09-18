#!/usr/bin/env python3
"""Generates placeholder machine models for the Pollution port.

GregTech's datagen-time ExistingFileHelper does not see assets inside the GTCEu
jar, so addon machines cannot use GT's `tieredHullModel`/`simpleGeneratorModel`
initialisers (they call getExistingFile on GT models). Instead each machine owns
a small model file in this repository that parents to a GregTech template; the
parent and textures resolve at runtime from the GTCEu jar.

Current output: vis_generator_<tier>.json for LV..LuV, using GregTech's voltage
casing textures and the lava boiler front overlay as a placeholder until ported
Pollution textures exist.
"""

from __future__ import annotations

import json
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parents[1]
MODEL_DIR = PROJECT_ROOT / "src" / "main" / "resources" / "assets" / "pollution" / "models" / "block" / "machine"

TIERS = ["lv", "mv", "hv", "ev", "iv", "luv"]

OVERLAY = "gtceu:block/generators/boiler/lava/overlay_front"


def machine_model(tier: str) -> dict:
    casing = f"gtceu:block/casings/voltage/{tier}"
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


def main() -> int:
    MODEL_DIR.mkdir(parents=True, exist_ok=True)
    for tier in TIERS:
        target = MODEL_DIR / f"vis_generator_{tier}.json"
        target.write_text(json.dumps(machine_model(tier), indent=2) + "\n", encoding="utf-8")
        print(f"wrote {target.relative_to(PROJECT_ROOT)}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
