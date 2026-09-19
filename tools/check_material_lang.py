#!/usr/bin/env python3
"""Checks that every registered material has a lang key in both languages."""

from __future__ import annotations

import json
import re
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
JAR = ROOT / "build/libs/pollution-1.20.1-1.0.0-1.20.1-port.0.1.0.jar"
UNIFICATION = ROOT / "src/main/java/meowmel/pollution/api/unification"


def main() -> int:
    jar = zipfile.ZipFile(JAR)
    en = json.loads(jar.read("assets/pollution/lang/en_us.json").decode("utf-8"))
    zh = json.loads(jar.read("assets/pollution/lang/zh_cn.json").decode("utf-8"))

    ids: set[str] = set()
    for path in UNIFICATION.rglob("*.java"):
        text = path.read_text(encoding="utf-8", errors="ignore")
        ids.update(re.findall(r'id\("([a-z0-9_]+)"\)', text))
        ids.update(re.findall(r'pollutionId\("([A-Za-z0-9_]+)"\)', text))
        ids.update(re.findall(r'fromNamespaceAndPath\(Pollution\.MOD_ID, "([a-z0-9_]+)"\)', text))

    missing_en = sorted(i for i in ids if f"material.pollution.{i.lower()}" not in en)
    missing_zh = sorted(i for i in ids if f"material.pollution.{i.lower()}" not in zh)
    print(f"material ids found: {len(ids)}")
    print(f"missing EN keys: {len(missing_en)} {missing_en[:25]}")
    print(f"missing ZH keys: {len(missing_zh)} {missing_zh[:25]}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
