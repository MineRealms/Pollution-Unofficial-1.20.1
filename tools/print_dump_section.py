#!/usr/bin/env python3
"""Prints the recipe section for a given category from the JEI debug dump."""

from __future__ import annotations

import sys
from pathlib import Path

DUMP = Path(r"G:\MinecraftGames\Sunlit Valley(BaopuEdition)\.minecraft\versions\Society Sunlit Valley\config\pollution-jei-dump.txt")
TARGET = sys.argv[1] if len(sys.argv) > 1 else "magic_turbine"


def main() -> int:
    lines = DUMP.read_text(encoding="utf-8", errors="ignore").splitlines()
    start = None
    for i, line in enumerate(lines):
        if line.startswith("### ") and TARGET in line:
            start = i
            break
    if start is None:
        print(f"section '{TARGET}' not found; section headers containing it:")
        for line in lines:
            if line.startswith("### ") and TARGET.split(":")[-1] in line:
                print("  " + line[:180])
        return 0
    for line in lines[start:start + 40]:
        if line.startswith("### ") and line != lines[start]:
            break
        print(line[:220])
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
