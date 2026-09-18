#!/usr/bin/env python3
"""Generates block assets for the Pollution plant blocks (flesh / rainbow / alfheim).

All models are placeholders built from vanilla textures until the 1.12 textures
are converted in the asset pass (TODO). Blocks with dynamic blockstate
properties get a full cartesian-product blockstate file so every state maps to
the placeholder model.
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
TEXTURE_FLESH = "minecraft:block/nether_wart_block"
TEXTURE_FLESH_LEAVES = "minecraft:block/red_mushroom_block"
TEXTURE_MUSHROOM = "minecraft:block/red_mushroom"
TEXTURE_FUNGUS = "minecraft:block/crimson_fungus"
TEXTURE_EYE = "minecraft:block/sculk"
TEXTURE_TENTACLE = "minecraft:block/slime_block"
TEXTURE_RAINBOW_LEAVES = "minecraft:block/oak_leaves"
TEXTURE_RAINBOW_SAPLING = "minecraft:block/oak_sapling"
TEXTURE_LILY_PAD = "minecraft:block/lily_pad"

LEAVES_PROPERTIES = [
    ("distance", [str(value) for value in range(1, 8)]),
    ("persistent", ["false", "true"]),
    ("waterlogged", ["false", "true"]),
]

SIX_CONNECTIONS = [
    ("north", ["false", "true"]),
    ("east", ["false", "true"]),
    ("south", ["false", "true"]),
    ("west", ["false", "true"]),
    ("up", ["false", "true"]),
    ("down", ["false", "true"]),
]

FACING = ["down", "up", "north", "south", "west", "east"]


class BlockSpec:
    def __init__(self, name: str, model: str, texture: str, properties=None):
        self.name = name
        self.model = model
        self.texture = texture
        self.properties = properties or []


BLOCKS = [
    BlockSpec("flesh_plant", "cube_all", TEXTURE_FLESH, SIX_CONNECTIONS),
    BlockSpec("flesh_flower", "cross", TEXTURE_MUSHROOM,
              [("age", [str(value) for value in range(0, 8)])]),
    BlockSpec("flesh_leaves", "cube_all", TEXTURE_FLESH_LEAVES, LEAVES_PROPERTIES),
    BlockSpec("flesh_sapling", "cross", TEXTURE_FUNGUS, [("stage", ["0", "1"])]),
    BlockSpec("heart_fruit", "cross", TEXTURE_MUSHROOM,
              [("age", [str(value) for value in range(0, 4)])]),
    BlockSpec("eldritch_eye", "cube_all", TEXTURE_EYE,
              [("facing", FACING), ("open", ["false", "true"])]),
    BlockSpec("tentacle", "cube_all", TEXTURE_TENTACLE,
              [("thickness", ["0", "1", "2"])] + SIX_CONNECTIONS),
    BlockSpec("rainbow_leaves", "leaves", TEXTURE_RAINBOW_LEAVES, LEAVES_PROPERTIES),
    BlockSpec("rainbow_sapling", "tinted_cross", TEXTURE_RAINBOW_SAPLING,
              [("stage", ["0", "1"])]),
    BlockSpec("alfheim_white_grape", "lily_pad", TEXTURE_LILY_PAD,
              [("age", ["0", "1", "2"])]),
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
    texture = spec.texture
    if spec.model == "cube_all":
        return {"parent": "minecraft:block/cube_all", "textures": {"all": texture}}
    if spec.model == "cross":
        return {"parent": "minecraft:block/cross", "textures": {"cross": texture}}
    if spec.model == "leaves":
        return {"parent": "minecraft:block/leaves", "textures": {"all": texture}}
    if spec.model == "lily_pad":
        return {"parent": "minecraft:block/lily_pad",
                "textures": {"particle": texture, "texture": texture}}
    if spec.model == "tinted_cross":
        # The vanilla block/cross model has no tintindex, so rainbow saplings
        # get their own copy with tintindex 0 for the client colour handler.
        return {
            "ambientocclusion": False,
            "textures": {"particle": texture, "cross": texture},
            "elements": [
                {
                    "from": [0.8, 0, 8],
                    "to": [15.2, 16, 8],
                    "rotation": {"origin": [8, 8, 8], "axis": "y", "angle": 45, "rescale": True},
                    "shade": False,
                    "faces": {
                        "north": {"uv": [0, 0, 16, 16], "texture": "#cross", "tintindex": 0},
                        "south": {"uv": [0, 0, 16, 16], "texture": "#cross", "tintindex": 0},
                    },
                },
                {
                    "from": [8, 0, 0.8],
                    "to": [8, 16, 15.2],
                    "rotation": {"origin": [8, 8, 8], "axis": "y", "angle": 45, "rescale": True},
                    "shade": False,
                    "faces": {
                        "west": {"uv": [0, 0, 16, 16], "texture": "#cross", "tintindex": 0},
                        "east": {"uv": [0, 0, 16, 16], "texture": "#cross", "tintindex": 0},
                    },
                },
            ],
        }
    raise ValueError(f"unknown model style: {spec.model}")


def main() -> int:
    for directory in (BLOCKSTATES, BLOCK_MODELS, ITEM_MODELS):
        directory.mkdir(parents=True, exist_ok=True)
    for spec in BLOCKS:
        write_json(BLOCKSTATES / f"{spec.name}.json", {"variants": blockstate_variants(spec)})
        write_json(BLOCK_MODELS / f"{spec.name}.json", block_model(spec))
        write_json(ITEM_MODELS / f"{spec.name}.json", {"parent": f"pollution:block/{spec.name}"})
        print(f"wrote plant assets for {spec.name}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
