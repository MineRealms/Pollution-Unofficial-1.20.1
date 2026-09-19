#!/usr/bin/env python3
"""Deletes unreferenced placeholder item textures (over-collection clutter).

A file is deleted only when BOTH hold:
  - it is byte-identical to the placeholder source (textures/item/heart_fruit.png)
  - no model/blockstate JSON references pollution:item/<name>
The 4 referenced gaps are reported instead of deleted.
"""

from __future__ import annotations

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/pollution"
TEX = ASSETS / "textures/item"


def referenced_names() -> set[str]:
    refs: set[str] = set()
    targets = list((ASSETS / "models").rglob("*.json"))
    targets += list((ROOT / "src/generated/resources/assets/pollution/models").rglob("*.json"))
    targets += list((ASSETS / "blockstates").rglob("*.json"))
    targets += list((ROOT / "src/generated/resources/assets/pollution/blockstates").rglob("*.json"))
    for path in targets:
        text = path.read_text(encoding="utf-8", errors="ignore")
        refs.update(re.findall(r"pollution:item/([A-Za-z0-9_.\-]+)", text))
        refs.update(re.findall(r'"item/([A-Za-z0-9_.\-]+)"', text))
    return refs


def main() -> int:
    placeholder = (TEX / "heart_fruit.png").read_bytes()
    refs = referenced_names()
    deleted = kept = 0
    for png in sorted(TEX.glob("*.png")):
        if png.stem in ("heart_fruit", "heart_fruit_i"):
            kept += 1
            continue
        if png.read_bytes() != placeholder:
            continue
        if png.stem in refs:
            print(f"kept (referenced, no upstream art): {png.stem}")
            kept += 1
            continue
        mcmeta = png.with_suffix(".png.mcmeta")
        png.unlink()
        if mcmeta.exists():
            mcmeta.unlink()
        deleted += 1
    print(f"deleted clutter: {deleted}, kept referenced gaps: {kept}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
