#!/usr/bin/env python3
"""Full placeholder audit: models, textures, missing references.

Checks:
  1. item models whose layer0 points at minecraft: textures (vanilla placeholder smell)
  2. block models whose textures point at minecraft: (list for review)
  3. block models still using gtceu voltage casings (machines are expected; others flagged)
  4. every pollution:item/... and pollution:block/... texture referenced by a model exists
  5. texture files identical to the known placeholder (heart_fruit.png)
  6. blockstate model targets exist
"""

from __future__ import annotations

import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/pollution"
GEN = ROOT / "src/generated/resources/assets/pollution"

VANILLA_OK = {
    "particle", "overlay", "all", "side", "top", "bottom", "front", "back", "up", "down",
    "north", "south", "east", "west", "cross", "plant", "torch", "wall", "end",
    "texture", "layer0",
}


def model_files() -> list[Path]:
    files = list(ASSETS.rglob("models/**/*.json")) + list(GEN.rglob("models/**/*.json"))
    return files


def texture_exists(ref: str) -> bool:
    ns, _, path = ref.partition(":")
    if ns != "pollution":
        return True
    for base in (ASSETS, GEN):
        if (base / "textures" / f"{path}.png").exists():
            return True
    return False


def main() -> int:
    vanilla_item, vanilla_block, voltage, missing_tex = [], [], [], []
    for path in model_files():
        try:
            data = json.loads(path.read_text(encoding="utf-8"))
        except json.JSONDecodeError:
            continue
        rel = path.relative_to(ROOT)
        textures = data.get("textures", {})
        values = [v for v in textures.values() if isinstance(v, str)]
        if "models/item" in str(path).replace("\\", "/"):
            for value in values:
                if value.startswith("minecraft:") and "block/" not in value:
                    vanilla_item.append((str(rel), value))
        if "models/block" in str(path).replace("\\", "/"):
            for key, value in textures.items():
                if value.startswith("minecraft:block/"):
                    vanilla_block.append((str(rel), key, value))
                if value.startswith("gtceu:block/casings/voltage"):
                    voltage.append(str(rel))
                if value.startswith("pollution:") and not texture_exists(value):
                    missing_tex.append((str(rel), value))
    placeholder = (ASSETS / "textures/item/heart_fruit.png").read_bytes()
    placeholders = sorted(p.name for p in (ASSETS / "textures/item").glob("*.png")
                          if p.read_bytes() == placeholder and p.stem != "heart_fruit")

    missing_models = []
    for path in list(ASSETS.rglob("blockstates/*.json")) + list(GEN.rglob("blockstates/*.json")):
        try:
            data = json.loads(path.read_text(encoding="utf-8"))
        except json.JSONDecodeError:
            continue
        text = json.dumps(data)
        for ref in set(re.findall(r'"(pollution:block/[A-Za-z0-9_./\-]+)"', text)):
            _, _, sub = ref.partition(":")
            found = any((base / "models" / f"{sub}.json").exists() for base in (ASSETS, GEN))
            if not found:
                missing_models.append((str(path.relative_to(ROOT)), ref))

    print(f"1. item models with vanilla layer0: {len(vanilla_item)}")
    for rel, value in vanilla_item[:20]:
        print(f"   {rel} -> {value}")
    print(f"2. block models with minecraft:block textures: {len(vanilla_block)}")
    for rel, key, value in vanilla_block[:25]:
        print(f"   {rel} [{key}] -> {value}")
    print(f"3. block models on gtceu voltage casings: {len(voltage)}")
    for rel in voltage[:15]:
        print(f"   {rel}")
    print(f"4. missing pollution textures: {len(missing_tex)}")
    for rel, value in missing_tex[:15]:
        print(f"   {rel} -> {value}")
    print(f"5. remaining placeholder textures: {len(placeholders)} {placeholders[:10]}")
    print(f"6. missing model targets: {len(missing_models)}")
    for rel, value in missing_models[:15]:
        print(f"   {rel} -> {value}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
