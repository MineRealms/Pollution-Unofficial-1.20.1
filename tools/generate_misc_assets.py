#!/usr/bin/env python3
"""Generates block assets for the Pollution misc blocks (portal / flesh heart /
mineral extractor).

All models are placeholders built from vanilla textures until the 1.12
textures are converted in the asset pass (TODO). The portal blockstate covers
both values of its ``one_way`` property; the portal has no block item, so no
item model is written for it.
"""

from __future__ import annotations

import itertools
import json
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parents[1]
ASSETS = PROJECT_ROOT / "src" / "main" / "resources" / "assets" / "pollution"
BLOCKSTATES = ASSETS / "blockstates"
BLOCK_MODELS = ASSETS / "models" / "block"
ITEM_MODELS = ASSETS / "models" / "item"

# TODO: replace every texture below with converted 1.12 Pollution textures.
TEXTURE_PORTAL = "minecraft:block/nether_portal"
TEXTURE_FLESH_HEART = "minecraft:block/nether_wart_block"
TEXTURE_MINERAL_EXTRACTOR = "minecraft:block/iron_block"


class BlockSpec:
    def __init__(self, name: str, texture: str, properties=None, has_item: bool = True):
        self.name = name
        self.texture = texture
        self.properties = properties or []
        self.has_item = has_item


BLOCKS = [
    BlockSpec("portal", TEXTURE_PORTAL, [("one_way", ["false", "true"])], has_item=False),
    BlockSpec("flesh_heart", TEXTURE_FLESH_HEART),
    BlockSpec("mineral_extractor", TEXTURE_MINERAL_EXTRACTOR),
]


def write_json(path: Path, payload: dict) -> None:
    path.write_text(json.dumps(payload, indent=2) + "\n", encoding="utf-8")


def blockstate_variants(spec: BlockSpec) -> dict:
    if not spec.properties:
        return {"": {"model": f"pollution:block/{spec.name}"}}
    names = [name for name, _ in spec.properties]
    values = [values for _, values in spec.properties]
    variants = {}
    for combo in itertools.product(*values):
        key = ",".join(f"{name}={value}" for name, value in zip(names, combo))
        variants[key] = {"model": f"pollution:block/{spec.name}"}
    return variants


def block_model(spec: BlockSpec) -> dict:
    return {"parent": "minecraft:block/cube_all", "textures": {"all": spec.texture}}


def main() -> int:
    for directory in (BLOCKSTATES, BLOCK_MODELS, ITEM_MODELS):
        directory.mkdir(parents=True, exist_ok=True)
    for spec in BLOCKS:
        write_json(BLOCKSTATES / f"{spec.name}.json", {"variants": blockstate_variants(spec)})
        write_json(BLOCK_MODELS / f"{spec.name}.json", block_model(spec))
        if spec.has_item:
            write_json(ITEM_MODELS / f"{spec.name}.json", {"parent": f"pollution:block/{spec.name}"})
        print(f"wrote misc assets for {spec.name}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
