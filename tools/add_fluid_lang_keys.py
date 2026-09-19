#!/usr/bin/env python3
"""Adds fluid.pollution.<id> lang keys mirroring every material name.

GT fluid tooltips may resolve fluid.<ns>.<path> keys; the port only had
material.<ns>.<path>. Mirroring them is harmless when unused and fixes JEI
fluid tooltips showing only the mod name.
"""

from __future__ import annotations

import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
EN = ROOT / "src/generated/resources/assets/pollution/lang/en_us.json"
ZH = ROOT / "src/main/resources/assets/pollution/lang/zh_cn.json"
POLLUTION = ROOT / "src/main/java/meowmel/pollution/Pollution.java"


def main() -> int:
    en = json.loads(EN.read_text(encoding="utf-8"))
    zh = json.loads(ZH.read_text(encoding="utf-8"))

    material_keys = {k: v for k, v in en.items() if k.startswith("material.pollution.")}
    fluid_en = {k.replace("material.", "fluid."): v for k, v in material_keys.items()}
    fluid_zh = {
        k.replace("material.", "fluid."): zh.get(k, v)
        for k, v in material_keys.items()
    }
    added_en = [k for k in fluid_en if k not in en]
    added_zh = [k for k in fluid_zh if k not in zh]

    # Chinese file: add directly.
    zh.update({k: v for k, v in fluid_zh.items() if k not in zh})
    ZH.write_text(json.dumps(dict(sorted(zh.items())), indent=2, ensure_ascii=False) + "\n",
                  encoding="utf-8")

    # English: add provider.add lines to Pollution.java (datagen source of truth).
    source = POLLUTION.read_text(encoding="utf-8")
    anchor = '            provider.add("pollution.crystal_quality.grade", "Lens quality: grade %s (%s%%)");\n'
    if anchor not in source:
        print("anchor not found")
        return 1
    lines = "".join(f'            provider.add("{k}", "{v}");\n' for k, v in sorted(fluid_en.items()))
    source = source.replace(anchor, anchor + lines)
    POLLUTION.write_text(source, encoding="utf-8")

    print(f"fluid EN keys queued: {len(added_en)}, fluid ZH keys added: {len(added_zh)}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
