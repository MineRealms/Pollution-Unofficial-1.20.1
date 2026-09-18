#!/usr/bin/env python3
"""Generates the GameTest structure template (a flat 3x3 stone platform)."""

from __future__ import annotations

import gzip
import struct
from pathlib import Path

OUTPUT = (Path(__file__).resolve().parents[1] / "src" / "main" / "resources" /
          "data" / "pollution" / "structures" / "platform.nbt")

TAG_INT = 3
TAG_STRING = 8
TAG_LIST = 9
TAG_COMPOUND = 10


def write_string(value: str) -> bytes:
    raw = value.encode("utf-8")
    return struct.pack(">H", len(raw)) + raw


def write_payload(tag_type: int, value) -> bytes:
    if tag_type == TAG_INT:
        return struct.pack(">i", value)
    if tag_type == TAG_STRING:
        return write_string(value)
    if tag_type == TAG_COMPOUND:
        payload = b""
        for name, (child_type, child_value) in value.items():
            payload += bytes([child_type]) + write_string(name) + write_payload(child_type, child_value)
        return payload + b"\x00"
    if tag_type == TAG_LIST:
        element_type, items = value
        payload = bytes([element_type]) + struct.pack(">i", len(items))
        for item in items:
            payload += write_payload(element_type, item)
        return payload
    raise ValueError(f"unsupported tag {tag_type}")


def main() -> int:
    size = (3, 3, 3)
    blocks = [{"pos": (x, 0, z), "state": 0} for x in range(3) for z in range(3)]
    root = {
        "DataVersion": (TAG_INT, 3465),
        "size": (TAG_LIST, (TAG_INT, list(size))),
        "palette": (TAG_LIST, (TAG_COMPOUND, [{"Name": (TAG_STRING, "minecraft:stone")}])),
        "blocks": (TAG_LIST, (TAG_COMPOUND, [
            {"pos": (TAG_LIST, (TAG_INT, list(block["pos"]))), "state": (TAG_INT, block["state"])}
            for block in blocks
        ])),
        "entities": (TAG_LIST, (TAG_COMPOUND, [])),
    }
    payload = bytes([TAG_COMPOUND]) + write_string("") + write_payload(TAG_COMPOUND, root)
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    with gzip.open(OUTPUT, "wb") as handle:
        handle.write(payload)
    print(f"wrote {OUTPUT}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
