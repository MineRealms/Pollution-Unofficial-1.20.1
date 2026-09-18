#!/usr/bin/env python3
"""Ensures every Pollution biome JSON has the four required effects colors.

1.20.1 biome `effects` requires sky_color, fog_color, water_color and
water_fog_color; missing keys abort world/registry loading.
"""

from __future__ import annotations

import json
from pathlib import Path

BIOMES = (Path(__file__).resolve().parents[1] / "src" / "main" / "resources" /
          "data" / "pollution" / "worldgen" / "biome")

DEFAULTS = {
    "sky_color": 7907327,
    "fog_color": 12638463,
    "water_color": 4159204,
    "water_fog_color": 329011,
}


def main() -> int:
    fixed = 0
    for path in sorted(BIOMES.glob("*.json")):
        data = json.loads(path.read_text(encoding="utf-8"))
        effects = data.setdefault("effects", {})
        changed = False
        for key, value in DEFAULTS.items():
            if key not in effects:
                effects[key] = value
                changed = True
        if changed:
            path.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")
            fixed += 1
            print(f"fixed {path.name}")
    print(f"{fixed} biome files updated")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
