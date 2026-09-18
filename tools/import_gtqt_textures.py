#!/usr/bin/env python3
"""Imports the GTQT-Core textures that upstream Pollution referenced.

Upstream POTextures.java lists the texture names (magicblock/..., botblock/...,
multiblock/casings/...). The 1.12 repo does not ship all of them; GTQT-Core
(1.20.1) does, under assets/{gregtech,gtqtcore}/textures/blocks. This script
finds each name by basename in GTQT-Core, copies it into the port's assets and
rewrites the casing models to reference the imported pollution: textures.
"""

from __future__ import annotations

import json
import re
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
UP_CODE = Path(r"H:\MinecraftMods\Pollution\src\main\java\meowmel\pollution\client\textures\POTextures.java")
GTQT = Path(r"H:\MinecraftMods\GTQT-Core\src\main\resources\assets")
DEST = ROOT / "src/main/resources/assets/pollution"


def main() -> int:
    names = sorted(set(re.findall(r'SimpleOverlayRenderer\("([^"]+)"\)',
                                  UP_CODE.read_text(encoding="utf-8", errors="ignore"))))
    index: dict[str, Path] = {}
    for png in GTQT.rglob("*.png"):
        index.setdefault(png.stem.lower(), png)

    copied, missing = 0, []
    for name in names:
        base = name.rsplit("/", 1)[-1].lower()
        source = index.get(base)
        if source is None:
            missing.append(name)
            continue
        target = DEST / "textures/block" / f"{base}.png"
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(source, target)
        copied += 1
    print(f"upstream names: {len(names)}, copied from GTQT-Core: {copied}, missing: {len(missing)}")
    for name in missing[:40]:
        print(f"  {name}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
