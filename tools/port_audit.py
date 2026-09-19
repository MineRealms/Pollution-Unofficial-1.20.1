#!/usr/bin/env python3
"""Pollution 1.12.2 -> 1.20.1 port audit.

Compares the upstream 1.12.2 sources with the unofficial 1.20.1 port and
reports, per domain, how much of the Thaumcraft-4-related content has been
carried over.  Non-TC4 mod integrations (Botania, Astral Sorcery, Blood Magic,
AE2, GTNN, ...) are reported separately and excluded from the headline score.

Usage:
    python tools/port_audit.py            # console summary
    python tools/port_audit.py --md FILE  # also write a markdown report
"""
from __future__ import annotations

import argparse
import os
import re
import sys
from collections import defaultdict

UPSTREAM = r"H:\MinecraftMods\Pollution\src\main\java"
PORT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "main", "java")
PORT = os.path.normpath(PORT)

# --------------------------------------------------------------------------
# domain classification
# --------------------------------------------------------------------------
# Order matters: first match wins.
DOMAIN_RULES = [
    # --- excluded: other mod integrations -----------------------------------
    ("other:botania", ("botania", "mana", "alfheim", "endoflame", "terrasteel",
                       "petal", "runealtar", "pure daisy", "puredaisy", "garden of glass")),
    ("other:astral", ("astral", "constellation", "celestial", "starlight", "starstream",
                      "lightwell", "calibration", "crystalgrowth")),
    ("other:bloodmagic", ("blood", "bmhpca", "bmaltar", "sacrific")),
    ("other:ae2", ("ae2", "aeitem", "appeng", "meteor")),
    ("other:gtnn", ("rocket", "gtnn", "gcyr")),
    # --- TC4 core ----------------------------------------------------------
    ("tc4:aspect", ("aspect", "compoundaspect", "objectaspect", "aspecttank")),
    ("tc4:vis", ("vis", "cleanvis", "aural", "aura")),
    ("tc4:infusion", ("infusion", "infused", "infusedfluid")),
    ("tc4:warp", ("warp", "fluxwarp")),
    ("tc4:flux", ("flux")),
    ("tc4:node", ("node",)),
    ("tc4:essence", ("essence",)),
    ("tc4:thaum-other", ("thaum", "eldritch", "taint", "flesh", "arcane", "alchemical",
                         "golem", "pech", "wand", "staff", "salis", "mundus", "demiplane",
                         "crystalcluster", "crystal")),
    ("tc4:magic-machine", ("magic",)),
    # --- shared infrastructure ---------------------------------------------
    ("core:machine", ("machine",)),
    ("core:material", ("material", "element", "oreprefix", "stonetype")),
    ("core:api", ("api", "capability", "pattern", "recipe", "amplification", "utils")),
    ("core:block-item", ("block", "item", "potion", "entity")),
    ("core:worldgen", ("dimension", "biome", "worldgen", "chunkgenerator", "structure", "feature")),
    ("core:client", ("client", "gui", "render", "screen", "widget", "tesr")),
    ("core:misc", ()),
]


def classify(path: str) -> str:
    low = path.replace("\\", "/").lower()
    for domain, tokens in DOMAIN_RULES:
        for token in tokens:
            if token in low:
                return domain
    return "core:misc"


# class-name normalisation: MetaTileEntityMagicMacerator -> magicmacerator
STRIP_PREFIX = ("metatileentity", "tileentity", "po", "meta")
STRIP_SUFFIX = ("tileentity", "machine", "metatileentity", "blockentity")


def norm(stem: str) -> str:
    s = stem.lower()
    for p in STRIP_PREFIX:
        if s.startswith(p) and len(s) > len(p) + 2:
            s = s[len(p):]
    for suf in STRIP_SUFFIX:
        if s.endswith(suf) and len(s) > len(suf) + 2:
            s = s[: -len(suf)]
    return s
# --------------------------------------------------------------------------
# file collection
# --------------------------------------------------------------------------
def java_files(root: str) -> list:
    out = []
    for dirpath, _dirs, files in os.walk(root):
        for f in files:
            if f.endswith(".java"):
                out.append(os.path.join(dirpath, f))
    return sorted(out)


def rel(root: str, path: str) -> str:
    return os.path.relpath(path, root).replace("\\", "/")


def loc(path: str) -> int:
    try:
        with open(path, "r", encoding="utf-8", errors="ignore") as fh:
            return sum(1 for _ in fh)
    except OSError:
        return 0


def read(path: str) -> str:
    try:
        with open(path, "r", encoding="utf-8", errors="ignore") as fh:
            return fh.read()
    except OSError:
        return ""


def upstream_machine_ids(up_root: str) -> set:
    ids = set()
    for path in java_files(up_root):
        if os.path.basename(path) != "PollutionMetaTileEntities.java":
            continue
        text = read(path)
        for m in re.finditer(r'"([a-z0-9_]+\.[a-z0-9_.]+)"', text):
            ids.add(m.group(1))
    return ids


def port_machine_ids(port_root: str) -> set:
    ids = set()
# --------------------------------------------------------------------------
# matching
# --------------------------------------------------------------------------
def build_index(root: str) -> dict:
    """normalised stem -> list of (relpath, domain, loc)"""
    idx = defaultdict(list)
    for path in java_files(root):
        stem = os.path.splitext(os.path.basename(path))[0]
        idx[norm(stem)].append((rel(root, path), classify(rel(root, path)), loc(path)))
    return idx


def match(up_norm: str, port_idx: dict) -> list:
    if up_norm in port_idx:
        return port_idx[up_norm]
    found = []
    for key, val in port_idx.items():
        if len(up_norm) >= 7 and (up_norm in key or key in up_norm):
            found.extend(val)
    return found


RECIPE_START = re.compile(r"\.EUt\(|\.duration\(|\.inputItems\(|\.outputItems\(|"
                          r"\.inputFluids\(|\.outputFluids\(|\.circuitMeta\(|"
                          r"addInfusionCraftingRecipe|\.chancedOutput\(")
TODO = re.compile(r"\bTODO\b|\bFIXME\b|\bXXX\b|\bnot ported\b|\bnot implemented\b|"
                  r"\bstub\b|\bplaceholder\b", re.I)


def recipe_stats(root: str) -> dict:
    base = os.path.join(root, "meowmel", "pollution", "loaders", "recipes")
    stats = {}
    if not os.path.isdir(base):
        return stats
    for dirpath, _d, files in os.walk(base):
        for f in files:
            if not f.endswith(".java"):
                continue
            p = os.path.join(dirpath, f)
            text = read(p)
            hits = RECIPE_START.findall(text)
            # group 6 consecutive builder calls into roughly one recipe
            stats[f[:-5]] = {"loc": loc(p), "builders": len(hits)}
    return stats


def tc4_recipe_stats(root: str) -> dict:
    """Recipe files that belong to the TC4 subset."""
    stats = recipe_stats(root)
    keep = {}
    for name, data in stats.items():
        domain = classify("loaders/recipes/" + name)
        if domain.startswith("tc4") or name in (
                "MagicChemicalRecipes", "MagicGCYMRecipes", "MagicFuelRecipes",
                "MagicIntegrationRecipes", "MagicHatchRecipes", "ManaToEuRecipes",
                "PollutionRecipes", "CoilRecipes", "InfusedProcessingRecipes"):
            keep[name] = dict(data, domain=domain)
    return keep


def todo_stats(root: str) -> tuple:
    count = 0
    files = []
    for path in java_files(root):
        text = read(path)
        hits = TODO.findall(text)
        if hits:
            count += len(hits)
            files.append((rel(root, path), len(hits)))
    files.sort(key=lambda x: -x[1])
    return count, files
    path = os.path.join(port_root, "meowmel", "pollution", "common", "machine",
def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--md", default=None, help="write a markdown report to this path")
    args = ap.parse_args()

    up_files = java_files(UPSTREAM)
    port_files = java_files(PORT)
    print("upstream java files : %d (%d LOC)" % (len(up_files), sum(loc(p) for p in up_files)))
    print("port     java files : %d (%d LOC)" % (len(port_files), sum(loc(p) for p in port_files)))

    up_idx = build_index(UPSTREAM)
    port_idx = build_index(PORT)

    lines = []
    if args.md:
        lines.append("# Pollution 1.12.2 -> 1.20.1 port audit\n")
        lines.append("Generated by `tools/port_audit.py`.\n")
        lines.append("| tree | .java files | LOC |")
        lines.append("| --- | ---: | ---: |")
        lines.append("| upstream 1.12.2 | %d | %d |" % (len(up_files), sum(loc(p) for p in up_files)))
        lines.append("| port 1.20.1 | %d | %d |\n" % (len(port_files), sum(loc(p) for p in port_files)))

    # ---- per-domain coverage ---------------------------------------------
    dom_up = defaultdict(lambda: {"files": 0, "loc": 0})
    dom_port = defaultdict(lambda: {"files": 0, "loc": 0})
    for path in up_files:
        d = classify(rel(UPSTREAM, path))
        dom_up[d]["files"] += 1
        dom_up[d]["loc"] += loc(path)
    for path in port_files:
        d = classify(rel(PORT, path))
        dom_port[d]["files"] += 1
        dom_port[d]["loc"] += loc(path)

    print("\n== files / LOC per domain ==")
    print("%-22s %12s %12s" % ("domain", "upstream", "port"))
    for d in sorted(set(dom_up) | set(dom_port)):
        u, p = dom_up[d], dom_port[d]
        print("%-22s %5d/%-6d %5d/%-6d" % (d, u["files"], u["loc"], p["files"], p["loc"]))

    if args.md:
        lines.append("## files / LOC per domain\n")
        lines.append("| domain | upstream files | upstream LOC | port files | port LOC |")
        lines.append("| --- | ---: | ---: | ---: | ---: |")
        for d in sorted(set(dom_up) | set(dom_port)):
            u, p = dom_up[d], dom_port[d]
            lines.append("| %s | %d | %d | %d | %d |" % (d, u["files"], u["loc"], p["files"], p["loc"]))
        lines.append("")

    # ---- TC4 file-level coverage -----------------------------------------
    tc4_up, tc4_missing, tc4_matched = [], [], 0
    for path in up_files:
        r = rel(UPSTREAM, path)
        if not classify(r).startswith("tc4"):
            continue
        tc4_up.append(r)
        m = match(norm(os.path.splitext(os.path.basename(path))[0]), port_idx)
        if m:
            tc4_matched += 1
        else:
            tc4_missing.append(r)

    print("\n== TC4 subset ==")
    print("upstream TC4 files      : %d" % len(tc4_up))
    print("matched in port         : %d (%.1f%%)" % (tc4_matched, 100.0 * tc4_matched / max(1, len(tc4_up))))
    print("not matched             : %d" % len(tc4_missing))
    for r in tc4_missing:
        print("   - " + r)

    if args.md:
        lines.append("## TC4 subset\n")
        lines.append("- upstream TC4 files: **%d**" % len(tc4_up))
        lines.append("- matched in port: **%d (%.1f%%)**" % (tc4_matched, 100.0 * tc4_matched / max(1, len(tc4_up))))
        lines.append("- not matched: **%d**\n" % len(tc4_missing))
        lines.append("### upstream TC4 files with no obvious port counterpart\n")
        for r in tc4_missing:
            lines.append("- `%s`" % r)
        lines.append("")

    # ---- recipes ----------------------------------------------------------
    up_rec = tc4_recipe_stats(UPSTREAM)
    port_rec = recipe_stats(PORT)
    print("\n== TC4-relevant recipe files (builder-call count) ==")
    for name in sorted(up_rec):
        u = up_rec[name]
        p = port_rec.get(name)
        if p:
            print("%-26s up %5d builders /%6d loc | port %5d builders /%6d loc  (%.0f%%)"
                  % (name, u["builders"], u["loc"], p["builders"], p["loc"],
                     100.0 * p["builders"] / max(1, u["builders"])))
        else:
            print("%-26s up %5d builders /%6d loc | port MISSING" % (name, u["builders"], u["loc"]))
    port_only = [n for n in sorted(port_rec) if n not in up_rec]
    print("\nport-only recipe files (%d): %s" % (len(port_only), ", ".join(port_only)))

    if args.md:
        lines.append("## recipe files\n")
        lines.append("| file | upstream builders | port builders | upstream LOC | port LOC |")
        lines.append("| --- | ---: | ---: | ---: | ---: |")
        for name in sorted(up_rec):
            u = up_rec[name]
            p = port_rec.get(name, {"builders": 0, "loc": 0})
            lines.append("| %s | %d | %d | %d | %d |" % (name, u["builders"], p["builders"], u["loc"], p["loc"]))
        lines.append("")
        lines.append("Port-only recipe files: %s\n" % (", ".join("`%s`" % x for x in port_only) or "none"))

    # ---- machines ---------------------------------------------------------
    up_ids = upstream_machine_ids(UPSTREAM)
    p_ids = port_machine_ids(PORT)
    print("\n== machine ids ==")
    print("upstream ids: %d, port registrate ids: %d" % (len(up_ids), len(p_ids)))
    print("port ids sample: %s" % ", ".join(sorted(p_ids)[:12]))

    # ---- todo markers -----------------------------------------------------
    c, files = todo_stats(PORT)
    print("\n== TODO/stub markers in port: %d in %d files ==" % (c, len(files)))
    for r, n in files[:15]:
        print("   %-70s %d" % (r, n))

    if args.md:
        lines.append("## TODO / stub markers in the port\n")
        lines.append("total **%d** markers in **%d** files\n" % (c, len(files)))
        lines.append("| file | markers |")
        lines.append("| --- | ---: |")
        for r, n in files[:40]:
            lines.append("| `%s` | %d |" % (r, n))
        lines.append("")

    if args.md:
        with open(args.md, "w", encoding="utf-8") as fh:
            fh.write("\n".join(lines))
        print("\nwrote " + args.md)
    return 0


if __name__ == "__main__":
    sys.exit(main())
                        "PollutionMachines.java")
    text = read(path)
    for m in re.finditer(r'\.machine\(\s*"([a-z0-9_]+)"', text):
        ids.add(m.group(1))
    for m in re.finditer(r'\.machine\(\s*tierName\((\w+)\)\s*\+\s*"([a-z0-9_]+)"', text):
        ids.add("tiered:*" + m.group(2))
    for m in re.finditer(r'\.machine\(\s*"([a-z0-9_]+)"\s*\+\s*"_"\s*\+\s*(?:kind|plateKind)', text):
        ids.add("kind:" + m.group(1))
    return ids