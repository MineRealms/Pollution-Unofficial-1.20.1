#!/usr/bin/env python3
"""Asset audit / migration planner for the Pollution 1.20.1 port.

Reads the archived 1.12.2 assets (docs/reference/legacy-assets) and:
  1. writes a categorized inventory with sizes and sha1 hashes
     -> docs/reference/asset-inventory.csv
  2. writes a texture copy plan mapping legacy paths to 1.20.1 layout
     -> docs/reference/texture-copy-plan.csv
  3. optionally applies the texture copy plan (--apply), copying files into
     src/main/resources/assets/pollution/ using modern paths:
        textures/blocks -> textures/block
        textures/items  -> textures/item

Defaults are read-only. Nothing is ever deleted.
"""

from __future__ import annotations

import argparse
import csv
import hashlib
import shutil
import sys
from collections import Counter
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parents[1]
LEGACY_ROOT = PROJECT_ROOT / "docs" / "reference" / "legacy-assets"
REFERENCE_DIR = PROJECT_ROOT / "docs" / "reference"
RESOURCE_ROOT = PROJECT_ROOT / "src" / "main" / "resources" / "assets" / "pollution"

CATEGORY_RULES = [
    ("texture_block", ("textures/blocks/", "textures/block/")),
    ("texture_item", ("textures/items/", "textures/item/")),
    ("texture_entity", ("textures/entity/",)),
    ("texture_gui", ("textures/gui/",)),
    ("texture_other", ("textures/",)),
    ("sound", ("sounds/",)),
    ("model_obj", ("models/obj/",)),
    ("model_block", ("models/block/",)),
    ("model_item", ("models/item/",)),
    ("model_other", ("models/",)),
    ("blockstate", ("blockstates/",)),
    ("loot_table", ("loot_tables/",)),
    ("lang", ("lang/",)),
    ("gui_definition", ("gui/",)),
]

TEXTURE_REWRITES = {
    "textures/blocks/": "textures/block/",
    "textures/items/": "textures/item/",
}


def categorize(relative: str) -> str:
    normalized = relative.replace("\\", "/").lower()
    for name, prefixes in CATEGORY_RULES:
        if normalized.startswith(prefixes):
            return name
    return "other"


def sha1_of(path: Path) -> str:
    digest = hashlib.sha1()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1 << 16), b""):
            digest.update(chunk)
    return digest.hexdigest()


def build_inventory() -> list[dict[str, str]]:
    rows: list[dict[str, str]] = []
    for path in sorted(LEGACY_ROOT.rglob("*")):
        if not path.is_file():
            continue
        relative = path.relative_to(LEGACY_ROOT).as_posix()
        rows.append(
            {
                "path": relative,
                "category": categorize(relative),
                "size_bytes": str(path.stat().st_size),
                "sha1": sha1_of(path),
            }
        )
    return rows


def target_texture_path(relative: str) -> str | None:
    normalized = relative.replace("\\", "/")
    lowered = normalized.lower()
    if not lowered.startswith("textures/"):
        return None
    rewritten = normalized
    for legacy, modern in TEXTURE_REWRITES.items():
        if lowered.startswith(legacy):
            rewritten = modern + normalized[len(legacy):]
            break
    return rewritten


def main() -> int:
    if hasattr(sys.stdout, "reconfigure"):
        sys.stdout.reconfigure(encoding="utf-8")

    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--apply", action="store_true", help="copy planned textures into src/main/resources")
    args = parser.parse_args()

    if not LEGACY_ROOT.is_dir():
        print(f"legacy asset root not found: {LEGACY_ROOT}", file=sys.stderr)
        return 1

    rows = build_inventory()
    REFERENCE_DIR.mkdir(parents=True, exist_ok=True)

    inventory_path = REFERENCE_DIR / "asset-inventory.csv"
    with inventory_path.open("w", newline="", encoding="utf-8") as handle:
        writer = csv.DictWriter(handle, fieldnames=["path", "category", "size_bytes", "sha1"])
        writer.writeheader()
        writer.writerows(rows)

    counts = Counter(row["category"] for row in rows)
    total_bytes = sum(int(row["size_bytes"]) for row in rows)
    print(f"inventory: {len(rows)} files, {total_bytes / 1024 / 1024:.2f} MiB -> {inventory_path}")
    for name, count in sorted(counts.items(), key=lambda item: (-item[1], item[0])):
        print(f"  {name:15s} {count}")

    plan_rows = []
    for row in rows:
        target = target_texture_path(row["path"])
        if target is not None:
            plan_rows.append(
                {
                    "legacy": row["path"],
                    "target": target,
                    "sha1": row["sha1"],
                }
            )

    plan_path = REFERENCE_DIR / "texture-copy-plan.csv"
    with plan_path.open("w", newline="", encoding="utf-8") as handle:
        writer = csv.DictWriter(handle, fieldnames=["legacy", "target", "sha1"])
        writer.writeheader()
        writer.writerows(plan_rows)
    print(f"texture plan: {len(plan_rows)} files -> {plan_path}")

    if args.apply:
        copied = 0
        for row in plan_rows:
            source = LEGACY_ROOT / row["legacy"]
            destination = RESOURCE_ROOT / row["target"]
            destination.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(source, destination)
            copied += 1
        print(f"applied: {copied} textures copied into {RESOURCE_ROOT}")
    else:
        print("dry run (pass --apply to copy textures)")

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
