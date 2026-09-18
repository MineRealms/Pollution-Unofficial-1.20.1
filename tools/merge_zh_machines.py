#!/usr/bin/env python3
"""Adds Chinese machine names from the upstream pollution.machine.* keys.

Upstream key shape: pollution.machine.<name>[.<tier>].name. Our machine blocks
are block.pollution.<tier>_<id> or block.pollution.<id>; matching is token-set
based, tier aware.
"""

from __future__ import annotations

import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
UP = Path(r"H:\MinecraftMods\Pollution\src\main\resources\assets\pollution\lang\zh_cn.lang")
EN = ROOT / "src/generated/resources/assets/pollution/lang/en_us.json"
ZH = ROOT / "src/main/resources/assets/pollution/lang/zh_cn.json"

TIERS = ("ulv", "lv", "mv", "hv", "ev", "iv", "luv", "zpm", "uv", "uhv", "uev", "uiv", "uxv", "opv", "max")


def norm(value: str) -> str:
    return re.sub(r"[^a-z0-9]+", "_", value.lower()).strip("_")


def toks(value: str) -> str:
    return "_".join(sorted(norm(value).split("_")))


def main() -> int:
    en = json.loads(EN.read_text(encoding="utf-8"))
    zh = json.loads(ZH.read_text(encoding="utf-8")) if ZH.exists() else {}

    machine_exact: dict[str, str] = {}
    machine_tier: dict[tuple[str, str], str] = {}
    for line in UP.read_text(encoding="utf-8", errors="ignore").splitlines():
        if not line.startswith("pollution.machine."):
            continue
        key, _, value = line.strip().partition("=")
        if not key.endswith(".name"):
            continue
        body = key[len("pollution.machine."):-len(".name")]
        parts = body.split(".")
        if parts and parts[-1] in TIERS:
            machine_tier[(toks(".".join(parts[:-1])), parts[-1])] = value
        else:
            machine_exact.setdefault(toks(body), value)

    added = 0
    for key in list(en):
        if key in zh or not key.startswith("block.pollution."):
            continue
        ident = key[len("block.pollution."):]
        tier = None
        for candidate in TIERS:
            if ident.startswith(candidate + "_"):
                tier, ident = candidate, ident[len(candidate) + 1:]
                break
        if tier and (toks(ident), tier) in machine_tier:
            zh[key] = machine_tier[(toks(ident), tier)]
            added += 1
            continue
        if toks(ident) in machine_exact:
            zh[key] = machine_exact[toks(ident)]
            added += 1
            continue
        ident_tokens = toks(ident)
        for (mtoks, mtier), value in machine_tier.items():
            if tier == mtier and (
                mtoks.endswith("_" + ident_tokens)
                or ident_tokens.endswith("_" + mtoks)
                or ident_tokens in mtoks
                or mtoks in ident_tokens
            ):
                zh[key] = value
                added += 1
                break

    ZH.write_text(json.dumps(dict(sorted(zh.items())), indent=2, ensure_ascii=False) + "\n",
                  encoding="utf-8")
    missing = [k for k in en if k not in zh]
    print(f"machine keys added: {added}, zh total: {len(zh)}, missing: {len(missing)}")
    for key in missing[:15]:
        print("  " + key)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
