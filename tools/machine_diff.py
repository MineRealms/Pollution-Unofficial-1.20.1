#!/usr/bin/env python3
"""Machine-level diff between upstream 1.12.2 and the 1.20.1 port.

Upstream registers machines in PollutionMetaTileEntities.java with
`new MetaTileEntityXxx(...)`; the port registers them in PollutionMachines.java
with Registrate `.machine("id", info -> new XxxMachine(info, ...))`.
Class names are normalised so that e.g. MetaTileEntityMagicMacerator and
MagicMaceratorMachine both become `magicmacerator`.
"""
from __future__ import annotations

import os
import re
from collections import defaultdict

UP = r"H:\MinecraftMods\Pollution\src\main\java\meowmel\pollution\common\metatileentity\PollutionMetaTileEntities.java"
PORT_JAVA = os.path.normpath(os.path.join(os.path.dirname(os.path.abspath(__file__)),
                                          "..", "src", "main", "java"))
PORT = os.path.join(PORT_JAVA, "meowmel", "pollution", "common", "machine")

NEW_CLASS = re.compile(r"new\s+((?:MetaTileEntity|Multi)?[A-Z]\w*?)\s*\(")
REG_MACHINE = re.compile(r'\.machine\(\s*(?:"([a-z0-9_]+)"|[^,]+),\s*info\s*->\s*new\s+(\w+)')
MULTI_DEF = re.compile(r"\.multi\(\s*\"([a-z0-9_]+)\"\s*,\s*(\w+)")

PREFIXES = ("metatileentity", "tileentity", "po")
SUFFIXES = ("machine", "tileentity", "blockentity", "multiblock")


def norm(name: str) -> str:
    s = name.lower()
    for p in PREFIXES:
        if s.startswith(p) and len(s) > len(p) + 2:
            s = s[len(p):]
    for suf in SUFFIXES:
        if s.endswith(suf) and len(s) > len(suf) + 2:
            s = s[: -len(suf)]
    return s


def read(path: str) -> str:
    with open(path, "r", encoding="utf-8", errors="ignore") as fh:
        return fh.read()


def upstream_machines() -> dict:
    """normalised class name -> raw class name"""
    out = {}
    text = read(UP)
    for m in NEW_CLASS.finditer(text):
        cls = m.group(1)
        if cls in ("ResourceLocation", "RecipeMap", "String", "Random"):
            continue
        out[norm(cls)] = cls
    return out


def port_machines() -> dict:
    out = {}
    for dirpath, _d, files in os.walk(PORT):
        for f in files:
            if f.endswith(".java"):
                out[norm(f[:-5])] = f[:-5]
    # registrate ids declared in PollutionMachines
    ids = defaultdict(list)
    text = read(os.path.join(PORT, "PollutionMachines.java"))
    for m in REG_MACHINE.finditer(text):
        mid = m.group(1) or "(dynamic)"
        ids[m.group(2)].append(mid)
    for m in MULTI_DEF.finditer(text):
        ids[m.group(2)].append(m.group(1))
    return out, ids


def main() -> int:
    up = upstream_machines()
    port, ids = port_machines()
    print("upstream machine classes referenced : %d" % len(up))
    print("port machine classes on disk        : %d" % len(port))
    print("port registrate definitions         : %d" % sum(len(v) for v in ids.values()))

    port_only = sorted(k for k in port if k not in up)
    missing = sorted(k for k in up if k not in port)

    print("\n== upstream machine classes with no port counterpart (%d) ==" % len(missing))
    for k in missing:
        print("   %-40s (%s)" % (up[k], k))

    print("\n== port-only machine classes (%d) ==" % len(port_only))
    for k in port_only:
        print("   %-40s  ids: %s" % (port[k], ", ".join(ids.get(port[k], []))[:60]))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())