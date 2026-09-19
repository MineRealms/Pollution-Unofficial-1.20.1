#!/usr/bin/env python3
"""Lists the materials used as fluids in MAGIC_TURBINE_FUELS recipes and checks
their lang keys in both languages."""

from __future__ import annotations

import json
import re
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
JAR = ROOT / "build/libs/pollution-1.20.1-1.0.0-1.20.1-port.0.1.0.jar"
LOADERS = ROOT / "src/main/java/meowmel/pollution/loaders/recipes"


def main() -> int:
    jar = zipfile.ZipFile(JAR)
    en = json.loads(jar.read("assets/pollution/lang/en_us.json").decode("utf-8"))
    zh = json.loads(jar.read("assets/pollution/lang/zh_cn.json").decode("utf-8"))

    materials: set[str] = set()
    for path in LOADERS.glob("*.java"):
        text = path.read_text(encoding="utf-8", errors="ignore")
        if "MAGIC_TURBINE_FUELS" not in text:
            continue
        # collect material references used with getFluid in this file
        materials.update(re.findall(r"PollutionMaterials\.([A-Za-z0-9_]+)\.getFluid", text))
        materials.update(re.findall(r"GTMaterials\.([A-Za-z0-9_]+)\.getFluid", text))
        materials.update(re.findall(r"GTNNMaterials\.([A-Za-z0-9_]+)\.getFluid", text))
    print(f"fluids in MAGIC_TURBINE_FUELS recipes: {len(materials)}")
    missing = []
    for name in sorted(materials):
        for prefix in ("pollution", "gtceu", "gtnn"):
            key = f"material.{prefix}.{name.lower()}"
            if key in en:
                if key not in zh:
                    missing.append((key, "zh"))
                break
        else:
            missing.append((f"material.*.{name}", "both"))
    print("missing lang keys:", missing if missing else "none")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
