#!/usr/bin/env python3
"""Ensures textures/item/<raw id>.png exists for every registered item id.

Registrate uses the raw id (dots preserved) as the model texture path, so the
file name must match exactly. Real upstream textures are preferred (exact,
suffix or substring match against the imported pool), placeholders only as a
last resort so datagen never aborts.
"""

from __future__ import annotations

import re
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
TEX = ROOT / "src/main/resources/assets/pollution/textures/item"
JAVA = ROOT / "src/main/java/meowmel/pollution"
PLACEHOLDER = TEX / "heart_fruit.png"

PATTERNS = [
    re.compile(r'\.item\(\s*"([a-z0-9_.]+)"'),
    re.compile(r'\.block\(\s*"([a-z0-9_.]+)"'),
    re.compile(r'register\(\s*"([a-z0-9_.]+)"'),
    re.compile(r'ITEMS\.put\(\s*"([a-z0-9_.]+)"'),
    re.compile(r'magicBattery\(\s*"([a-z0-9_.]+)"'),
    re.compile(r'"([a-z0-9_.]{3,})"\s*,'),
]


def main() -> int:
    ids: set[str] = set()
    for path in JAVA.rglob("*.java"):
        text = path.read_text(encoding="utf-8", errors="ignore")
        for pattern in PATTERNS:
            ids.update(pattern.findall(text))

    pool = {p.stem: p for p in TEX.glob("*.png") if p != PLACEHOLDER}
    created = matched = 0
    for raw in sorted(ids):
        target = TEX / f"{raw}.png"
        if target.exists():
            continue
        norm = raw.replace(".", "_")
        source = None
        if norm in pool:
            source = pool[norm]
        else:
            for up, png in pool.items():
                if up.endswith("_" + norm) or norm.endswith("_" + up) or norm in up:
                    source = png
                    break
        if source is not None:
            shutil.copyfile(source, target)
            mcmeta = source.with_suffix(".png.mcmeta")
            if mcmeta.exists():
                shutil.copyfile(mcmeta, TEX / f"{raw}.png.mcmeta")
            matched += 1
        else:
            shutil.copyfile(PLACEHOLDER, target)
            created += 1
    print(f"ids: {len(ids)}, upstream-matched: {matched}, placeholder: {created}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
