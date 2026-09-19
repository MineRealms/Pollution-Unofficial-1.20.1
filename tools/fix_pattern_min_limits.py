#!/usr/bin/env python3
"""Removes impossible setMinGlobalLimited values from four multiblock patterns.

Upstream `Elements.abilities(layer, slot, ...)` per-layer slots were mistranslated
into global minimum counts (200/105/25/20) that exceed the pattern block counts,
so those machines can never form.
"""

from __future__ import annotations

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
FILES = [
    "src/main/java/meowmel/pollution/common/machine/multiblock/magic/EssenceCollectorPatterns.java",
    "src/main/java/meowmel/pollution/common/machine/multiblock/magic/MagicFusionReactorPatterns.java",
    "src/main/java/meowmel/pollution/common/machine/multiblock/node/LargeNodeGeneratorMachine.java",
    "src/main/java/meowmel/pollution/common/machine/multiblock/node/NodeBlastFurnacePatterns.java",
]
PATTERN = re.compile(r"\s*\.setMinGlobalLimited\((?:200|105|25|20)\)")


def main() -> int:
    for rel in FILES:
        path = ROOT / rel
        text = path.read_text(encoding="utf-8")
        fixed, count = PATTERN.subn("", text)
        if count:
            path.write_text(fixed, encoding="utf-8")
            print(f"{rel}: removed {count}")
        else:
            print(f"{rel}: nothing matched")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
