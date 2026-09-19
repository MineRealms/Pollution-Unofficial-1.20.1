#!/usr/bin/env python3
"""Checks our FactoryBlockPattern definitions for missing predicates.

Scans every *.java under common/machine for `aisle("...")` strings and
`.where('X'` definitions, then reports characters used in aisles but never
defined (GT throws "Predicates for character(s) X are missing" at build time).
"""

from __future__ import annotations

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MACHINES = ROOT / "src/main/java/meowmel/pollution/common/machine"


def main() -> int:
    problems = 0
    for path in MACHINES.rglob("*.java"):
        text = path.read_text(encoding="utf-8", errors="ignore")
        if "aisle(" not in text:
            continue
        used: set[str] = set()
        for aisle in re.findall(r"aisle\(([^)]*)\)", text):
            for row in re.findall(r'"([^"]*)"', aisle):
                used.update(row)
        defined = set(re.findall(r"\.where\('(.)'", text))
        missing = sorted(used - defined)
        if missing:
            problems += 1
            print(f"{path.relative_to(ROOT)}: missing predicates for {missing}")
    print(f"files with missing predicates: {problems}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
