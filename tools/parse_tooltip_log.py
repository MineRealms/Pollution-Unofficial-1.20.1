#!/usr/bin/env python3
"""Parses config/pollution-jei-tooltip.log and reports tooltips that lack a name line."""

from __future__ import annotations

from pathlib import Path

LOG = Path(r"G:\MinecraftGames\Sunlit Valley(BaopuEdition)\.minecraft\versions\Society Sunlit Valley\config\pollution-jei-tooltip.log")

SUSPECT_PREFIXES = ("Pollution", "gtceu:", "gcyr:", "pollution:", "gtnn:")


def main() -> int:
    text = LOG.read_text(encoding="utf-8", errors="ignore")
    blocks = [b.strip() for b in text.split("=== Pollution JEI recipe slot tooltip ===") if b.strip()]
    print(f"entries: {len(blocks)}")
    suspicious = []
    for block in blocks:
        lines = block.splitlines()
        data: dict[str, str] = {}
        tooltip: list[str] = []
        in_tooltip = False
        for line in lines:
            if line.startswith("tooltipLines:"):
                in_tooltip = True
                continue
            if in_tooltip:
                tooltip.append(line.strip())
            elif ":" in line:
                key, _, value = line.partition(":")
                data[key.strip()] = value.strip()
        name = data.get("helperDisplayName", "?")
        first = tooltip[0] if tooltip else "(empty)"
        # suspicious: no tooltip, or the first line is not the display name and looks like an id/mod name
        bad = (not tooltip) or (
            name and first != name and first.startswith(SUSPECT_PREFIXES)
        )
        if bad:
            suspicious.append((data.get("role"), data.get("ingredientType"), name, tooltip))
    print(f"suspicious entries: {len(suspicious)}")
    for role, itype, name, tooltip in suspicious[:25]:
        print(f"- {role} {itype} name={name}")
        for line in tooltip:
            print(f"    {line[:160]}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
