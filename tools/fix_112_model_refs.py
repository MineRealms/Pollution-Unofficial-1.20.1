#!/usr/bin/env python3
"""Fixes 1.12-era texture references left inside converted model JSONs.

Maps vanilla 1.12 texture names to their 1.20.1 equivalents (leaves_oak ->
oak_leaves, grass_top -> grass_block_top, flower_rose -> poppy, portal ->
nether_portal, ...) and normalises pollution:blocks/ -> pollution:block/.
"""

from __future__ import annotations

import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/pollution"

VANILLA = {
    "leaves_oak": "oak_leaves",
    "leaves_big_oak": "dark_oak_leaves",
    "leaves_birch": "birch_leaves",
    "leaves_spruce": "spruce_leaves",
    "leaves_jungle": "jungle_leaves",
    "leaves_acacia": "acacia_leaves",
    "sapling_oak": "oak_sapling",
    "sapling_big_oak": "dark_oak_sapling",
    "sapling_birch": "birch_sapling",
    "sapling_spruce": "spruce_sapling",
    "grass_top": "grass_block_top",
    "grass_side": "grass_block_side",
    "grass_side_overlay": "grass_block_side_overlay",
    "flower_rose": "poppy",
    "flower_dandelion": "dandelion",
    "flower_blue_orchid": "blue_orchid",
    "portal": "nether_portal",
    "log_oak": "oak_log",
    "log_oak_top": "oak_log_top",
    "planks_oak": "oak_planks",
    "planks_big_oak": "dark_oak_planks",
    "water_still": "water_still",
    "lava_still": "lava_still",
    "mycelium": "mycelium",
    "nether_brick": "nether_bricks",
    "stonebrick": "stone_bricks",
    "stonebrick_carved": "chiseled_stone_bricks",
    "stonebrick_cracked": "cracked_stone_bricks",
    "stonebrick_mossy": "mossy_stone_bricks",
    "cobblestone": "cobblestone",
    "end_stone": "end_stone",
    "obsidian": "obsidian",
    "quartz_block_side": "quartz_block_side",
    "quartz_block_top": "quartz_block_top",
    "glowstone": "glowstone",
}


def fix_text(text: str) -> str:
    text = text.replace("pollution:blocks/", "pollution:block/")
    text = text.replace("minecraft:blocks/", "minecraft:block/")
    for old, new in VANILLA.items():
        text = text.replace(f"minecraft:block/{old}", f"minecraft:block/{new}")
        text = re.sub(rf'"(?:minecraft:)?block/{re.escape(old)}"', f'"minecraft:block/{new}"', text)
    return text


def main() -> int:
    changed = 0
    for folder in ("models/block", "models/item", "blockstates"):
        for path in (ASSETS / folder).rglob("*.json"):
            text = path.read_text(encoding="utf-8")
            fixed = fix_text(text)
            if fixed != text:
                try:
                    json.loads(fixed)
                except json.JSONDecodeError:
                    continue
                path.write_text(fixed, encoding="utf-8")
                changed += 1
    print(f"model files fixed: {changed}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
