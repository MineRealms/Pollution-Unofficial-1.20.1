#!/usr/bin/env python3
"""Creates placeholder item textures for every registrate item.

Registrate's default item model is item/generated with layer0 at
pollution:item/<name>; datagen aborts when that texture is missing. All items
registered through PollutionItems (plus helpers) get a placeholder copy until
the 1.12 texture pass lands.
"""
from __future__ import annotations
import re
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "src/main/java/meowmel/pollution"
TEX = ROOT / "src/main/resources/assets/pollution/textures/item"
SOURCE = TEX / "heart_fruit.png"

PATTERNS = [
    re.compile(r'\.item\("([a-z0-9_]+)"'),
    re.compile(r'register\("([a-z0-9_]+)"'),
    re.compile(r'ITEMS\.put\("([a-z0-9_]+)"'),
]

def main() -> int:
    names: set[str] = set()
    for path in JAVA.rglob("*.java"):
        text = path.read_text(encoding="utf-8", errors="ignore")
        for pat in PATTERNS:
            names.update(pat.findall(text))
    TEX.mkdir(parents=True, exist_ok=True)
    created = 0
    for name in sorted(names):
        target = TEX / f"{name}.png"
        if not target.exists():
            shutil.copyfile(SOURCE, target)
            created += 1
    print(f"item names: {len(names)}, placeholder textures created: {created}")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
