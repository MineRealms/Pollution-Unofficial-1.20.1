#!/usr/bin/env python3
"""Prints the broken-name report sections of the JEI debug dump."""

from __future__ import annotations

from pathlib import Path

DUMP = Path(r"G:\MinecraftGames\Sunlit Valley(BaopuEdition)\.minecraft\versions\Society Sunlit Valley\config\pollution-jei-dump.txt")


def main() -> int:
    lines = DUMP.read_text(encoding="utf-8", errors="ignore").splitlines()
    print(f"size={DUMP.stat().st_size} mtime={DUMP.stat().st_mtime}")
    for header in ("## Broken / untranslated item names", "## Broken / untranslated fluid names"):
        try:
            start = next(i for i, l in enumerate(lines) if l.startswith(header))
        except StopIteration:
            print(f"{header}: section missing")
            continue
        print(f"--- {header} (line {start + 1}) ---")
        shown = 0
        for line in lines[start + 1:]:
            if line.startswith("## "):
                break
            print("  " + line[:200])
            shown += 1
            if shown >= 40:
                print("  ...")
                break
        if shown == 0:
            print("  (none)")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
