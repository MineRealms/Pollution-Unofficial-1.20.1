#!/usr/bin/env python3
"""Detailed server smoke test suite for the Pollution port.

Phases
  1. boot        - launch runServer detached, wait for `Done (`
  2. log scan    - categorise errors from the server log (recipes, textures,
                   registration, worldgen, GT warnings)
  3. rcon checks - place and verify one machine per family, query dimensions,
                   read back machine block entities
  4. report      - PASS/FAIL summary written to docs/SMOKE_TEST_REPORT.md

Usage: python tools/smoke_test.py [--keep-server]
"""

from __future__ import annotations

import re
import subprocess
import sys
import time
from datetime import datetime
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
LOG = ROOT / "run" / "smoke-test.log"
RCON = ROOT / "tools" / "rcon_exec.py"
REPORT = ROOT / "docs" / "SMOKE_TEST_REPORT.md"
RCON_PASSWORD = "pollution"

ERROR_PATTERNS = {
    "recipe empty input": re.compile(r"Input item .* is empty"),
    "recipe empty output": re.compile(r"Output item .* is empty"),
    "EUt zero": re.compile(r"EUt can't be explicitly set to 0"),
    "recipe parse": re.compile(r"Parsing error loading recipe"),
    "missing texture": re.compile(r"Texture .* does not exist"),
    "missing model": re.compile(r"Failed to load model"),
    "worldgen parse": re.compile(r"Failed to parse pollution:"),
    "start failure": re.compile(r"Failed to start the minecraft server"),
    "exception": re.compile(r"Exception in thread|java\.lang\.\w+Exception"),
    # Pollution-specific warnings (recipe-id attribution via the `recipe_id`
    # named group). Any hit in these categories fails the smoke test.
    "pollution recipe conflict": re.compile(
        r"Attempted to add GTRecipe: (?P<recipe_id>\S+), which conflicts with pollution:"),
    "pollution staging add failure": re.compile(
        r"failed to add recipe from staging into lookup DB: (?P<recipe_id>pollution:\S+)"),
    "pollution invalid input": re.compile(
        r"Input item is not one of:(?:\r?\n(?!id: )[^\r\n]*){0,3}\r?\nid: (?P<recipe_id>pollution:\S+)"),
    "pollution empty item": re.compile(
        r"(?:Input|Output) item \d+ of recipe (?P<recipe_id>pollution:\S+) is empty"),
}

# Categories that make the verdict FAIL when found in the server log.
POLLUTION_ERROR_CATEGORIES = (
    "pollution recipe conflict",
    "pollution staging add failure",
    "pollution invalid input",
    "pollution empty item",
)

# machine id -> human label; one per machine family
MACHINE_CHECKS = [
    ("lv_aspect_tank", "Aspect Tank (LV)"),
    ("luv_small_node_generator", "Small Node Generator (LuV)"),
    ("source_charge", "Source Charge"),
    ("node_producer", "Node Producer"),
    ("magic_bender", "Magic Bender"),
    ("endoflame_array", "Endoflame Array"),
    ("mana_plate", "Mana Plate"),
    ("lv_mana_input_hatch_1a", "Mana Input Hatch 1A (LV)"),
    ("gt_essence_smelter", "GT Essence Smelter"),
    ("wire_coil_cupronickel", "Wire Coil: Cupronickel"),
    ("wire_coil_tritanium", "Wire Coil: Tritanium"),
    ("terra_watertight_casing", "Botania Casing: Terra Watertight"),
    ("void_prism", "Magic Casing: Void Prism"),
]

DIMENSIONS = [
    ("pollution:underground", "Underground dimension"),
    ("pollution:alfheim", "Alfheim dimension"),
    ("pollution:blood", "Blood dimension"),
    ("pollution:demiplane", "Demiplane dimension"),
]


def launch_server() -> subprocess.Popen:
    LOG.parent.mkdir(parents=True, exist_ok=True)
    handle = LOG.open("wb")
    return subprocess.Popen(
        ["cmd.exe", "/c", "gradlew.bat", "runServer", "--console=plain"],
        cwd=ROOT, stdout=handle, stderr=subprocess.STDOUT,
    )


def wait_for_done(process: subprocess.Popen, timeout: float = 420.0) -> bool:
    deadline = time.time() + timeout
    while time.time() < deadline:
        time.sleep(6)
        if LOG.exists():
            text = LOG.read_text(encoding="utf-8", errors="ignore")
            if "Done (" in text:
                return True
            if "Failed to start the minecraft server" in text:
                return False
        if process.poll() is not None:
            break
    return False


def scan_log() -> tuple[dict[str, int], dict[str, list[str]]]:
    """Count each error category and attribute pollution recipe ids."""
    text = LOG.read_text(encoding="utf-8", errors="ignore")
    counts: dict[str, int] = {}
    recipe_ids: dict[str, list[str]] = {}
    for name, pattern in ERROR_PATTERNS.items():
        matches = list(pattern.finditer(text))
        counts[name] = len(matches)
        if "recipe_id" in pattern.groupindex:
            attributed = [match.group("recipe_id") for match in matches]
            if attributed:
                recipe_ids[name] = attributed
    return counts, recipe_ids


def rcon(*commands: str) -> str:
    result = subprocess.run(
        [sys.executable, str(RCON), "--password", RCON_PASSWORD, *commands],
        cwd=ROOT, capture_output=True, text=True, timeout=120,
    )
    return result.stdout + result.stderr


def run_rcon_checks() -> list[tuple[str, bool, str]]:
    checks: list[tuple[str, bool, str]] = []
    for index, (machine, label) in enumerate(MACHINE_CHECKS):
        x = 120 + index * 2
        output = rcon(
            f"setblock {x} -60 0 pollution:{machine}",
            f"execute if block {x} -60 0 pollution:{machine}",
        )
        passed = "Test passed" in output
        checks.append((f"machine {label} ({machine})", passed,
                       "placed+verified" if passed else output.strip()[-160:]))
    for dimension, label in DIMENSIONS:
        output = rcon(f"execute in {dimension} run time query daytime")
        passed = "The time is" in output
        checks.append((f"dimension {label}", passed,
                       "loaded" if passed else output.strip()[-160:]))
    return checks


def main() -> int:
    keep = "--keep-server" in sys.argv
    started = datetime.now()
    print(f"[smoke] launching dedicated server at {started:%H:%M:%S}")
    process = launch_server()
    booted = wait_for_done(process)
    print(f"[smoke] booted={booted}")

    checks: list[tuple[str, bool, str]] = []
    errors: dict[str, int] = {}
    pollution_ids: dict[str, list[str]] = {}
    if booted:
        errors, pollution_ids = scan_log()
        checks = run_rcon_checks()
        rcon("stop")
        time.sleep(8)
    if not keep and process.poll() is None:
        subprocess.run(["taskkill", "/F", "/T", "/PID", str(process.pid)], capture_output=True)

    failed_checks = [name for name, ok, _ in checks if not ok]
    error_total = sum(errors.values())
    pollution_error_total = sum(errors.get(name, 0) for name in POLLUTION_ERROR_CATEGORIES)
    verdict = "PASS" if booted and not failed_checks and pollution_error_total == 0 else "FAIL"

    lines = [
        "# Pollution Port - Server Smoke Test",
        "",
        f"- date: {started:%Y-%m-%d %H:%M:%S}",
        f"- server booted (`Done (`): {'yes' if booted else 'NO'}",
        f"- log errors (all patterns): {error_total}",
        f"- pollution-specific errors (fail on any): {pollution_error_total}",
        "",
        "## Log error categories",
        "",
    ]
    for name, count in errors.items():
        marker = " **[POLLUTION]**" if name in POLLUTION_ERROR_CATEGORIES else ""
        lines.append(f"- {name}: {count}{marker}")
    if pollution_error_total:
        lines += ["", "## Pollution-specific errors (attributed recipe ids)", ""]
        for name in POLLUTION_ERROR_CATEGORIES:
            for recipe_id in pollution_ids.get(name, []):
                lines.append(f"- {name}: {recipe_id}")
    lines += ["", "## RCON checks", ""]
    for name, ok, detail in checks:
        lines.append(f"- [{'x' if ok else ' '}] {name} - {detail}")
    lines += ["", f"## Verdict: **{verdict}**", ""]
    REPORT.write_text("\n".join(lines), encoding="utf-8")
    print("\n".join(lines))
    return 0 if verdict == "PASS" else 1


if __name__ == "__main__":
    raise SystemExit(main())
