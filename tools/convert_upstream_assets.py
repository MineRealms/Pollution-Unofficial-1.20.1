#!/usr/bin/env python3
"""Converts the upstream 1.12 asset tree to the 1.20.1 layout.

Textures:  textures/blocks -> textures/block, textures/items -> textures/item,
           dotted / roman-numeral names flattened (battery.hull.ev -> battery_hull_ev,
           filter.ii -> filter_2), *.png.mcmeta copied alongside.
Models:    1.12 model JSONs are rewritten for 1.20.1 (texture refs
           pollution:blocks/ -> pollution:block/, pollution:items/ -> pollution:item/)
           and written over the placeholder models where the block/item is ported.
Machines keep the GTCEu template models (GT's own model format), never the
1.12 OBJ files.
"""
from __future__ import annotations
import json
import re
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
UP = Path(r"H:\MinecraftMods\Pollution\src\main\resources\assets\pollution")
DEST = ROOT / "src/main/resources/assets/pollution"

ROMAN = {"i": "1", "ii": "2", "iii": "3", "iv": "4", "v": "5", "vi": "6", "vii": "7", "viii": "8", "ix": "9", "x": "10"}

def flat(name: str) -> str:
    parts = [ROMAN.get(p.lower(), p) for p in re.split(r"[.\-/]", name)]
    return re.sub(r"[^a-z0-9_]+", "_", "_".join(parts).lower()).strip("_")

def convert_texture_refs(text: str) -> str:
    text = text.replace("pollution:blocks/", "pollution:block/")
    text = text.replace("pollution:items/", "pollution:item/")
    text = text.replace('"blocks/', '"block/').replace('"items/', '"item/')
    return text

def main() -> int:
    copied = mcmeta = models = 0
    for src in UP.rglob("*"):
        if not src.is_file():
            continue
        rel = src.relative_to(UP)
        parts = rel.parts
        if parts[0] == "textures":
            group = "block" if parts[1] == "blocks" else "item" if parts[1] == "items" else parts[1]
            stem = src.name[:-len(".png.mcmeta")] if src.name.endswith(".png.mcmeta") else src.stem
            name = flat("_".join(parts[2:-1]) + "_" + stem) if len(parts) > 3 else flat(stem)
            suffix = ".png.mcmeta" if src.name.endswith(".png.mcmeta") else src.suffix
            target = DEST / "textures" / group / f"{name}{suffix}"
            target.parent.mkdir(parents=True, exist_ok=True)
            shutil.copyfile(src, target)
            if suffix == ".png.mcmeta":
                mcmeta += 1
            else:
                copied += 1
        elif parts[0] == "models" and src.suffix == ".json":
            group = parts[1] if parts[1] in ("block", "item") else None
            if group is None:
                continue
            target = DEST / "models" / group / f"{flat(src.stem)}.json"
            try:
                data = json.loads(src.read_text(encoding="utf-8"))
            except json.JSONDecodeError:
                continue
            raw = json.dumps(data, indent=2)
            raw = convert_texture_refs(raw)
            raw = re.sub(r'"parent"\s*:\s*"item/', '"parent": "minecraft:item/', raw)
            raw = re.sub(r'"parent"\s*:\s*"block/', '"parent": "minecraft:block/', raw)
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_text(raw + "\n", encoding="utf-8")
            models += 1
    print(f"textures copied: {copied}, mcmeta copied: {mcmeta}, models converted: {models}")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
