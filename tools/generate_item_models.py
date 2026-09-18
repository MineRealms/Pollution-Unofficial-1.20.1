#!/usr/bin/env python3
"""Generates placeholder item models for the Pollution item registry."""

from __future__ import annotations

import json
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parents[1]
ITEM_MODELS = PROJECT_ROOT / "src" / "main" / "resources" / "assets" / "pollution" / "models" / "item"

PLACEHOLDER_TEXTURE = "minecraft:item/paper"

ITEMS = [
    "packaged_aura_node",
    "magic_battery.hull.lv", "magic_battery.hull.mv", "magic_battery.hull.hv",
    "magic_battery.hull.ev", "magic_battery.hull.iv", "magic_battery.hull.luv",
    "magic_battery.hull.zpm", "magic_battery.hull.uv",
    "magic_circuit.ulv", "magic_circuit.lv", "magic_circuit.mv", "magic_circuit.hv",
    "magic_circuit.ev", "magic_circuit.iv", "magic_circuit.luv", "magic_circuit.zpm",
    "magic_circuit.uv", "magic_circuit.uhv",
    "filter.i", "filter.ii", "filter.iii", "filter.iv", "filter.v",
    "blank_catalyst_core", "hot_catalyst_core", "cold_catalyst_core",
    "integration_catalyst_core", "segregation_catalyst_core", "coking_catalyst_core",
    "evolution_catalyst_core",
    "white_rune", "black_rune", "starry_rune",
    "vis_checker", "energy_reduce", "time_increase", "overclocking_enhance",
    "parallel_enhance", "transform_enhance",
    "core_of_idea", "bottle_of_phlogistonic_oneness", "auto_elenchus_device",
    "elucidator_of_four_causes", "symptomatic_vis_data_link",
    "needle_of_mystic_interpellation", "cogito_defibrillator", "ball_in_itself",
    "stone_of_philosopher_1", "stone_of_philosopher_2", "stone_of_philosopher_3",
    "stone_of_philosopher_final",
]


def main() -> int:
    ITEM_MODELS.mkdir(parents=True, exist_ok=True)
    for name in ITEMS:
        payload = {
            "parent": "minecraft:item/generated",
            "textures": {"layer0": PLACEHOLDER_TEXTURE},
        }
        (ITEM_MODELS / f"{name}.json").write_text(json.dumps(payload, indent=2) + "\n", encoding="utf-8")
    print(f"wrote {len(ITEMS)} item models")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
