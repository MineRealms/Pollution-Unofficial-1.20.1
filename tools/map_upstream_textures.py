#!/usr/bin/env python3
"""Maps every registered id to the closest upstream texture (token subset) and
copies the texture plus its .png.mcmeta animation metadata. Ids without a
plausible upstream source keep the placeholder and are reported.
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

def main() -> int:
    ids: set[str] = set()
    for path in JAVA.rglob("*.java"):
        text = path.read_text(encoding="utf-8", errors="ignore")
        ids.update(re.findall(r'\.item\(\s*"([a-z0-9_.]+)"', text))
        ids.update(re.findall(r'\.block\(\s*"([a-z0-9_.]+)"', text))
        ids.update(re.findall(r'register\(\s*"([a-z0-9_.]+)"', text))
        ids.update(re.findall(r'ITEMS\.put\(\s*"([a-z0-9_.]+)"', text))
    text = (JAVA / "common/item/PollutionItems.java").read_text(encoding="utf-8", errors="ignore")
    ids.update(re.findall(r'"([a-z0-9_]{3,})"\s*,', text))

    pool = {}
    for png in ITEM_TEX.glob("*.png"):
        if png.name != "heart_fruit.png":
            pool[png.stem] = png

    def score(cand: str, up: str) -> int:
        c, u = set(cand.split("_")), set(up.split("_"))
        if cand == up:
            return 100
        if c <= u:
            return 60 + len(c)
        if u <= c:
            return 40 + len(u)
        return 0

    matched, unmatched = 0, []
    for raw in sorted(ids):
        name = raw.replace(".", "_")
        target = ITEM_TEX / f"{name}.png"
        if not target.exists():
            continue
        best, best_score = None, 0
        for up, png in pool.items():
            s = score(name, up)
            if s > best_score:
                best, best_score = png, s
        if best is None or best_score < 42 or best == target:
            unmatched.append(name)
            continue
        shutil.copyfile(best, target)
        mcmeta = best.with_suffix(".png.mcmeta")
        if mcmeta.exists():
            shutil.copyfile(mcmeta, ITEM_TEX / f"{name}.png.mcmeta")
        matched += 1
    print(f"ids with texture: {sum(1 for r in ids if (ITEM_TEX / (r.replace('.', '_') + '.png')).exists())}")
    print(f"upstream matched: {matched}")
    print(f"still placeholder: {len(unmatched)}")
    for name in unmatched:
        print(f"  {name}")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())

