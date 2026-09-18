#!/usr/bin/env python3
"""Suffix-matches the remaining placeholder textures against the imported
upstream pool (which keeps path prefixes such as items_metaitems_magic_components_)
and copies the real texture plus its animation metadata.

A small manual table covers names that do not share a suffix with upstream
(block-family textures, stones, wings, ...).
"""
from __future__ import annotations
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
TEX = ROOT / "src/main/resources/assets/pollution/textures/item"
BTEX = ROOT / "src/main/resources/assets/pollution/textures/block"

MANUAL = {
    "eldritch_eye": "eldritch_eye_closed",
    "tentacle": "tentacle_mid",
    "portal": "portal_barrier",
    "flesh_heart": "flesh_heart",
    "heart_fruit_i": "heart_fruit",
    "rainbow_leaves": "rainbow_leaves",
    "rainbow_sapling": "rainbow_sapling",
    "stone_of_philosopher_1": "stone_1",
    "stone_of_philosopher_2": "stone_2",
    "stone_of_philosopher_3": "stone_3",
    "stone_of_philosopher_4": "stone_4",
    "nano_goggles": "goggles",
    "quantum_goggles": "goggles_quantum",
    "wing_nano": "wing",
    "wing_quantum": "wing_quantum",
}

def main() -> int:
    placeholder = (TEX / "heart_fruit.png").read_bytes()
    pool = {}
    for folder in (TEX, BTEX):
        for png in folder.glob("*.png"):
            pool.setdefault(png.stem, png)
    matched = manual_hits = still = 0
    remaining = []
    for png in sorted(TEX.glob("*.png")):
        if png.read_bytes() != placeholder:
            continue
        name = png.stem
        source = None
        if name in MANUAL:
            source = pool.get(MANUAL[name])
            manual_hits += 1 if source else 0
        if source is None:
            for up, candidate in pool.items():
                if up != name and (up.endswith("_" + name) or name.endswith("_" + up)):
                    source = candidate
                    break
        if source is None or source == png:
            still += 1
            remaining.append(name)
            continue
        shutil.copyfile(source, png)
        mcmeta = source.with_suffix(".png.mcmeta")
        if mcmeta.exists():
            shutil.copyfile(mcmeta, png.with_suffix(".png.mcmeta"))
        matched += 1
    print(f"matched: {matched} (manual: {manual_hits}), still placeholder: {still}")
    for name in remaining:
        print(f"  {name}")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())

