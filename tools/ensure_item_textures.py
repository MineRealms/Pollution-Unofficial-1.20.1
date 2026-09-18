#!/usr/bin/env python3
"""Guarantees a texture for every registered item/block id.

Prefers the real upstream texture (already imported flat), falls back to a
placeholder copy so Registrate datagen never aborts on a missing texture.
"""
from __future__ import annotations
import re
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/pollution"
ITEM_TEX = ASSETS / "textures/item"
BLOCK_TEX = ASSETS / "textures/block"
JAVA = ROOT / "src/main/java/meowmel/pollution"

ROMAN = {"i": "1", "ii": "2", "iii": "3", "iv": "4", "v": "5"}

def norm(name: str) -> str:
    parts = [ROMAN.get(p.lower(), p) for p in re.split(r"[.\-/]", name)]
    return re.sub(r"[^a-z0-9_]+", "_", "_".join(parts).lower()).strip("_")

def main() -> int:
    ids: set[str] = set()
    for path in JAVA.rglob("*.java"):
        text = path.read_text(encoding="utf-8", errors="ignore")
        ids.update(re.findall(r'\.item\(\s*"([a-z0-9_.]+)"', text))
        ids.update(re.findall(r'\.block\(\s*"([a-z0-9_.]+)"', text))
        ids.update(re.findall(r'register\(\s*"([a-z0-9_.]+)"', text))
        ids.update(re.findall(r'ITEMS\.put\(\s*"([a-z0-9_.]+)"', text))
    items_file = JAVA / "common/item/PollutionItems.java"
    if items_file.exists():
        text = items_file.read_text(encoding="utf-8", errors="ignore")
        ids.update(re.findall(r'"([a-z0-9_]{3,})"\s*,', text))

    placeholder = ITEM_TEX / "heart_fruit.png"
    created = matched = 0
    for raw in sorted(ids):
        name = raw.replace(".", "_")
        for folder, src_dir in ((ITEM_TEX, ITEM_TEX), (BLOCK_TEX, BLOCK_TEX)):
            target = folder / f"{name}.png"
            if target.exists():
                break
        else:
            flat = norm(raw)
            candidate = src_dir / f"{flat}.png"
            if candidate.exists():
                shutil.copyfile(candidate, ITEM_TEX / f"{name}.png")
                matched += 1
            else:
                shutil.copyfile(placeholder, ITEM_TEX / f"{name}.png")
                created += 1
    print(f"registered ids: {len(ids)}, upstream-matched: {matched}, placeholder: {created}")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
