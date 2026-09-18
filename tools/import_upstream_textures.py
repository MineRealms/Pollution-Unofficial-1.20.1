#!/usr/bin/env python3
"""Imports the upstream 1.12 textures into the 1.20.1 flat asset layout.

Upstream lives in textures/{blocks,items,entity} with dotted subfolders
(e.g. items/metaitems/battery.ev.magic/frame_0.png). The port uses flat
textures/{block,item,entity} names, so every upstream png is copied under a
normalised flat name and then matched against the registered ids (placeholders
generated earlier get overwritten by the real texture when a match exists).
"""
from __future__ import annotations
import re
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
UPSTREAM = Path(r"H:\MinecraftMods\Pollution\src\main\resources\assets\pollution\textures")
DEST = ROOT / "src/main/resources/assets/pollution/textures"
JAVA = ROOT / "src/main/java/meowmel/pollution"

def norm(value: str) -> str:
    return re.sub(r"[^a-z0-9]+", "_", value.lower()).strip("_")

def main() -> int:
    upstream: dict[str, Path] = {}
    for png in UPSTREAM.rglob("*.png"):
        rel = png.relative_to(UPSTREAM)
        parts = [norm(p) for p in rel.parts]
        flat = "_".join(parts[:-1] + [norm(png.stem)])
        upstream.setdefault(flat, png)

    # 1) wholesale copy, flattened
    copied = 0
    for flat, png in upstream.items():
        rel = png.relative_to(UPSTREAM)
        group = "block" if rel.parts[0] == "blocks" else "item" if rel.parts[0] == "items" else rel.parts[0]
        target = DEST / group / f"{flat}.png"
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(png, target)
        copied += 1

    # 2) map registered ids to upstream textures by token subset
    candidates: set[str] = set()
    for path in JAVA.rglob("*.java"):
        text = path.read_text(encoding="utf-8", errors="ignore")
        candidates.update(re.findall(r'\.item\("([a-z0-9_]+)"', text))
        candidates.update(re.findall(r'register\("([a-z0-9_]+)"', text))
        candidates.update(re.findall(r'ITEMS\.put\("([a-z0-9_]+)"', text))
        candidates.update(re.findall(r'\.block\("([a-z0-9_]+)"', text))
    # list-style ids in PollutionItems
    items_file = JAVA / "common/item/PollutionItems.java"
    if items_file.exists():
        for token in re.findall(r'"([a-z0-9_]{3,})"', items_file.read_text(encoding="utf-8", errors="ignore")):
            candidates.add(token)

    def score(cand: str, flat: str) -> int:
        c, f = set(cand.split("_")), set(flat.split("_"))
        if cand == flat:
            return 100
        if c <= f:
            return 60 + len(c)
        if f <= c:
            return 40 + len(f)
        return 0

    matched = 0
    unmatched: list[str] = []
    for cand in sorted(candidates):
        best, best_score = None, 0
        for flat, png in upstream.items():
            s = score(cand, flat)
            if s > best_score:
                best, best_score = png, s
        if best is None or best_score < 42:
            unmatched.append(cand)
            continue
        for group in ("item", "block"):
            target = DEST / group / f"{cand}.png"
            if target.exists():
                shutil.copyfile(best, target)
                matched += 1
                break
    print(f"upstream textures copied: {copied}")
    print(f"registered ids matched: {matched}")
    print(f"unmatched ids: {len(unmatched)}")
    for name in unmatched[:25]:
        print(f"  {name}")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
