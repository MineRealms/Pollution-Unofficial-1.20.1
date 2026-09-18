#!/usr/bin/env python3
"""Merges the upstream 1.12 zh_cn.lang into the port's zh_cn.json.

Upstream key shapes: metaitem.<id>.name (items), tile.pollution.<id>.name
(blocks/machines), entity.pollution.<id>.name. Our keys are
item/block/entity.pollution.<id>. Matching is done on the id part with exact,
token-set (order independent) and suffix/substring fallbacks.
"""

from __future__ import annotations

import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
UP_LANG = Path(r"H:\MinecraftMods\Pollution\src\main\resources\assets\pollution\lang\zh_cn.lang")
EN = ROOT / "src/generated/resources/assets/pollution/lang/en_us.json"
ZH = ROOT / "src/main/resources/assets/pollution/lang/zh_cn.json"

MANUAL = {
    "itemGroup.pollution.main": "污染（非官方）",
    "mod.pollution.name": "污染（非官方）",
}

UP_PREFIXES = ("metaitem.", "tile.pollution.", "item.pollution.", "entity.pollution.", "tile.", "item.")
OUR_PREFIXES = ("item.pollution.", "block.pollution.", "entity.pollution.")


def norm(value: str) -> str:
    return re.sub(r"[^a-z0-9]+", "_", value.lower()).strip("_")


def tokens(value: str) -> str:
    return "_".join(sorted(norm(value).split("_")))


def strip_prefix(value: str, prefixes: tuple[str, ...]) -> str:
    for prefix in prefixes:
        if value.startswith(prefix):
            return value[len(prefix):]
    return value


def main() -> int:
    en: dict[str, str] = json.loads(EN.read_text(encoding="utf-8"))
    zh: dict[str, str] = json.loads(ZH.read_text(encoding="utf-8")) if ZH.exists() else {}

    exact: dict[str, str] = {}
    ordered: dict[str, str] = {}
    for line in UP_LANG.read_text(encoding="utf-8", errors="ignore").splitlines():
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, _, value = line.partition("=")
        key, value = key.strip(), value.strip()
        if key.endswith(".tooltip"):
            continue
        base = strip_prefix(key, UP_PREFIXES)
        base = base.removesuffix(".name")
        exact.setdefault(norm(base), value)
        ordered.setdefault(tokens(base), value)

    added = fuzzy = 0
    for key, value in en.items():
        if key in zh:
            continue
        if key in MANUAL:
            zh[key] = MANUAL[key]
            added += 1
            continue
        if key in exact:
            zh[key] = exact[key]
            added += 1
            continue
        ident = strip_prefix(key, OUR_PREFIXES).removesuffix(".name")
        if norm(ident) in exact:
            zh[key] = exact[norm(ident)]
            fuzzy += 1
            continue
        if tokens(ident) in ordered:
            zh[key] = ordered[tokens(ident)]
            fuzzy += 1
            continue
        n = norm(ident)
        for up_key, up_value in exact.items():
            if n and (up_key.endswith("_" + n) or n.endswith("_" + up_key) or n in up_key):
                zh[key] = up_value
                fuzzy += 1
                break

    ZH.parent.mkdir(parents=True, exist_ok=True)
    ZH.write_text(json.dumps(dict(sorted(zh.items())), indent=2, ensure_ascii=False) + "\n",
                  encoding="utf-8")
    missing = [key for key in en if key not in zh]
    print(f"en keys: {len(en)}, zh keys: {len(zh)}, filled: {added} exact + {fuzzy} fuzzy, missing: {len(missing)}")
    for key in missing[:20]:
        print(f"  {key}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
