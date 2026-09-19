#!/usr/bin/env python3
"""Checks ability predicate limits against character counts in patterns (v2).

Parses each `.where('X', ...)` block separately, then compares any
setMinGlobalLimited/setExactLimit in that block with the max number of that
character in any single aisle layer.
"""

from __future__ import annotations

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MACHINES = ROOT / "src/main/java/meowmel/pollution/common/machine"


def main() -> int:
    found = 0
    for path in sorted(MACHINES.rglob("*.java")):
        text = path.read_text(encoding="utf-8", errors="ignore")
        if "aisle(" not in text:
            continue
        counts: dict[str, int] = {}
        for aisle in re.findall(r"aisle\(([^)]*)\)", text):
            per: dict[str, int] = {}
            for row in re.findall(r'"([^"]*)"', aisle):
                for ch in row:
                    if ch != " ":
                        per[ch] = per.get(ch, 0) + 1
            for ch, n in per.items():
                counts[ch] = max(counts.get(ch, 0), n)
        problems = []
        for block in re.split(r"\.where\('", text)[1:]:
            if len(block) < 2:
                continue
            ch = block[0]
            match = re.search(r"set(?:Min|Exact)GlobalLimited\((\d+)\)", block)
            if match and int(match.group(1)) > counts.get(ch, 0):
                problems.append((ch, int(match.group(1)), counts.get(ch, 0)))
        if problems:
            found += 1
            print(path.relative_to(ROOT))
            for ch, limit, count in problems:
                print(f"   char {ch!r}: limit {limit} > count {count}")
    print(f"files with impossible limits: {found}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
