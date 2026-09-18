#!/usr/bin/env python3
"""Injects a no-op blockstate provider into Registrate block chains.

The manual assets under assets/pollution/blockstates|models are authoritative
(plants use cross models, casings use GT textures), so Registrate's automatic
cubeAll provider must not run - it would demand pollution:block/<name> textures
that intentionally do not exist and abort datagen.
"""
from __future__ import annotations
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
TARGETS = [
    ROOT / "src/main/java/meowmel/pollution/common/block/PollutionPlantBlocks.java",
    ROOT / "src/main/java/meowmel/pollution/common/block/PollutionMiscBlocks.java",
]
NOOP = "            .blockstate((context, provider) -> {\n            })\n"
BLOCK_LINE = re.compile(r'^(\s*)\.block\("[a-z0-9_]+".*\)\s*$')

def main() -> int:
    for path in TARGETS:
        lines = path.read_text(encoding="utf-8").splitlines(keepends=True)
        out = []
        injected = 0
        for i, line in enumerate(lines):
            out.append(line)
            if BLOCK_LINE.match(line):
                # skip if the next non-empty line already has a blockstate call
                nxt = lines[i + 1] if i + 1 < len(lines) else ""
                if ".blockstate(" not in nxt:
                    out.append(NOOP)
                    injected += 1
        path.write_text("".join(out), encoding="utf-8")
        print(f"{path.name}: injected {injected}")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
