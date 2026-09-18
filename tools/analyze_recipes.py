#!/usr/bin/env python3
"""Summarizes upstream recipe chains into compact one-line-per-call listings.

Usage: python tools/analyze_recipes.py <file.java> [<file2.java> ...]
"""

from __future__ import annotations

import re
import sys
from pathlib import Path


def extract_calls(body: str) -> list[tuple[str, str]]:
    """Balanced-parenthesis extraction of .name(args) chains."""
    calls: list[tuple[str, str]] = []
    index = 0
    length = len(body)
    while index < length:
        if body[index] == ".":
            match = re.match(r"\.(\w+)\(", body[index:])
            if match:
                name = match.group(1)
                start = index + match.end()
                depth = 1
                cursor = start
                while cursor < length and depth > 0:
                    char = body[cursor]
                    if char == "(":
                        depth += 1
                    elif char == ")":
                        depth -= 1
                    cursor += 1
                calls.append((name, body[start:cursor - 1]))
                index = cursor
                continue
        index += 1
    return calls


def summarize_compact(path: Path) -> None:
    """One line per recipe with only the decisive calls."""
    text = path.read_text(encoding="utf-8", errors="replace")
    pattern = re.compile(r"([\w.]+)\.recipeBuilder\(\)(.*?)\.buildAndRegister\(\)", re.S)
    interesting = {"input", "inputs", "output", "outputs", "fluidInputs", "fluidOutputs",
                   "duration", "EUt", "notConsumable", "circuit", "blastFurnaceTemp",
                   "cleanroom", "chancedOutput"}
    for match in pattern.finditer(text):
        calls = [f"{name}({ ' '.join(args.split()) })" for name, args in extract_calls(match.group(2))
                 if name in interesting]
        print(f"[{match.group(1)}] " + " ".join(calls))


def summarize(path: Path) -> None:
    text = path.read_text(encoding="utf-8", errors="replace")
    print(f"===== {path.name} =====")
    # Find recipe chains: from '<MAP>.recipeBuilder()' to '.buildAndRegister()'
    pattern = re.compile(r"([\w.]+)\.recipeBuilder\(\)(.*?)\.buildAndRegister\(\)", re.S)
    count = 0
    for match in pattern.finditer(text):
        count += 1
        recipe_map = match.group(1)
        body = match.group(2)
        print(f"--- recipe {count} [{recipe_map}] ---")
        for name, args in extract_calls(body):
            compact = " ".join(args.split())
            print(f"  .{name}({compact})")
    if count == 0:
        print("  (no recipeBuilder chains found)")
    print()


def main() -> int:
    args = sys.argv[1:]
    compact = "--compact" in args
    files = [arg for arg in args if arg != "--compact"]
    if not files:
        print(__doc__)
        return 1
    for raw in files:
        if compact:
            summarize_compact(Path(raw))
        else:
            summarize(Path(raw))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
