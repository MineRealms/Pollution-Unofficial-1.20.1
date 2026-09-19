#!/usr/bin/env python3
"""Parses the JEI debug dump for broken ingredient names inside recipes."""

from __future__ import annotations

import re
from collections import Counter
from pathlib import Path

DUMP = Path(r"G:\MinecraftGames\Sunlit Valley(BaopuEdition)\.minecraft\versions\Society Sunlit Valley\config\pollution-jei-dump.txt")

SLOT = re.compile(r"\[(INPUT|OUTPUT|CATALYST|RENDER_ONLY)\]\s+(.*)")
ENTRY = re.compile(r"(item|fluid)\{([^|]+)\s*\|\s*desc=([^|]+)\s*\|\s*name=([^|}]*)\s*\|\s*(?:count|amount)=(\d+)\}")
BROKEN_PREFIXES = ("item.", "block.", "material.", "fluid.", "tagprefix.", "gtceu.")


def main() -> int:
    category = "?"
    broken: Counter[str] = Counter()
    examples: dict[str, str] = {}
    total_entries = 0
    for line in DUMP.read_text(encoding="utf-8", errors="ignore").splitlines():
        if line.startswith("### "):
            category = line[4:].strip()
            continue
        match = SLOT.search(line)
        if not match:
            continue
        for entry in ENTRY.finditer(match.group(2)):
            total_entries += 1
            kind, registry, desc, name, amount = entry.groups()
            broken_name = (not name.strip()) or name.strip() == desc.strip() or name.strip().startswith(BROKEN_PREFIXES)
            if broken_name:
                key = f"{kind} {desc.strip()}"
                broken[key] += 1
                examples.setdefault(key, f"{category} | {registry.strip()} | name='{name.strip()}'")
    print(f"ingredient entries scanned: {total_entries}")
    print(f"broken ingredient kinds: {len(broken)}")
    for key, count in broken.most_common(30):
        print(f"  {count}x {key}   e.g. {examples[key][:140]}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
