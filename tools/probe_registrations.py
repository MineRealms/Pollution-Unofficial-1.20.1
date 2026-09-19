#!/usr/bin/env python3
"""Probe: dump every Registrate machine-registration call in the port tree."""
import os
import re

ROOT = os.path.normpath(os.path.join(os.path.dirname(os.path.abspath(__file__)), "..",
                                     "src", "main", "java"))

PATTERNS = {
    "machine": r'\.machine\(\s*"([a-z0-9_]+)"',
    "multi": r'\.multi\(\s*"([a-z0-9_]+)"',
    "multiblock": r'\.multiblock\(\s*"([a-z0-9_]+)"',
    "tiered": r'registerTieredMachines\(\s*[\w.]*\s*,\s*"([a-z0-9_]+)"',
    "dyn_machine": r'\.machine\(\s*([^",\n]+?)\s*,',
    "dyn_multi": r'\.multiblock\(\s*([^",\n]+?)\s*,',
    "create": r'REGISTRATE\.([a-z]+)\(',
}

hits = {k: [] for k in PATTERNS}
for dirpath, _dirs, files in os.walk(ROOT):
    for f in files:
        if not f.endswith(".java"):
            continue
        full = os.path.join(dirpath, f)
        rel = os.path.relpath(full, ROOT).replace("\\", "/")
        with open(full, "r", encoding="utf-8", errors="ignore") as fh:
            text = fh.read()
        for key, pat in PATTERNS.items():
            for m in re.finditer(pat, text):
                line = text.count("\n", 0, m.start()) + 1
                hits[key].append((rel, line, m.group(1)))

for key in PATTERNS:
    print("=== %s (%d) ===" % (key, len(hits[key])))
    seen = set()
    for rel, line, val in hits[key]:
        if key in ("dyn_machine", "dyn_multi"):
            print("   %-70s L%-5d %s" % (rel, line, val))
        else:
            if val in seen:
                continue
            seen.add(val)
            print("   %-70s L%-5d %s" % (rel, line, val))
