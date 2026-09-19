#!/usr/bin/env python3
"""Splits placeholder item textures into referenced (real gaps) vs clutter."""

from __future__ import annotations

import collections
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/pollution"
TEX = ASSETS / "textures/item"


def main() -> int:
    refs: set[str] = set()
    targets = list((ASSETS / "models").rglob("*.json"))
    targets += list((ROOT / "src/generated/resources/assets/pollution/models").rglob("*.json"))
    targets += list((ASSETS / "blockstates").rglob("*.json"))
    targets += list((ROOT / "src/generated/resources/assets/pollution/blockstates").rglob("*.json"))
    for path in targets:
        text = path.read_text(encoding="utf-8", errors="ignore")
        refs.update(re.findall(r"pollution:item/([A-Za-z0-9_.\-]+)", text))
        refs.update(re.findall(r'"item/([A-Za-z0-9_.\-]+)"', text))

    placeholder = (TEX / "heart_fruit.png").read_bytes()
    placeholders = sorted(p.stem for p in TEX.glob("*.png") if p.read_bytes() == placeholder)
    referenced = [n for n in placeholders if n in refs]
    unreferenced = [n for n in placeholders if n not in refs]

    print(f"placeholder files: {len(placeholders)}")
    print(f"referenced (real gaps): {len(referenced)}")
    for name in referenced[:30]:
        print(f"  {name}")
    print(f"unreferenced (clutter): {len(unreferenced)}")
    counter = collections.Counter(n.split("_")[0].split(".")[0] for n in unreferenced)
    print("clutter prefixes:", counter.most_common(12))
    for name in unreferenced[:15]:
        print(f"  {name}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
