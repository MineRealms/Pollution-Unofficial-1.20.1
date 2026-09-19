#!/usr/bin/env python3
"""Pollution 1.12.2 -> 1.20.1 port status, scoped to Thaumcraft-4 content only.

Unlike tools/port_audit.py this does **semantic** matching instead of filename
matching:

* machine inventory is diffed on the upstream ``PollutionID("...")`` unlocalised
  names against the port's Registrate ids;
* java files are matched by identifier sets (CamelCase/snake tokens extracted
  from class names, method names, field names and string literals) using the
  Jaccard coefficient, so renamed/relocated classes still match.

Non-TC4 mod integrations (Botania/Alfheim, Astral Sorcery/Starstream, Blood
Magic/BMHPCA, AE2/meteors, GTNN/rocketry) are classified as ``excluded`` and
kept out of the headline score.

Usage:
    python tools/tc4_status.py              # console report
    python tools/tc4_status.py --md FILE    # also write markdown
"""
from __future__ import annotations

import argparse
import os
import re
import sys
from collections import defaultdict

HERE = os.path.dirname(os.path.abspath(__file__))
UPSTREAM = r"H:\MinecraftMods\Pollution\src\main\java"
PORT = os.path.normpath(os.path.join(HERE, "..", "src", "main", "java"))

# --------------------------------------------------------------------------
# classification
# --------------------------------------------------------------------------
# First match wins, so the excluded-mod list must come first.
EXCLUDE = (
    "botania", "alfheim", "rainbow", "endoflame", "terrasteel", "petal",
    "pure_daisy", "puredaisy", "daisymachine", "manaturbine", "manainfusion",
    "manareceiver", "manaplate", "manahatch", "manapool", "manacontainer",
    "sourcecharge", "garden", "dreamleaves", "elvensand", "grape",
    "botcircuit", "botdistillery", "botgascollector", "botvacuumfreezer",
    "managenerator", "manatoeu", "notifiablemana", "multiblockmana",
    "abstractmanacontrol", "asteroid",
    "astral", "constellation", "celestial", "starlight", "starstream",
    "lightwell", "calibration", "crystalgrowth",
    "blood", "bmhpca", "sacrific",
    "ae2", "appeng", "meteor",
    "rocket", "gtnn", "gcyr",
)

TC4 = (
    "warp", "fluxwarp", "flux", "vis", "cleanvis", "aspect", "node",
    "infusion", "infused", "thaum", "essence", "taint", "flesh", "eldritch",
    "arcane", "alchemical", "golem", "wand", "staff", "crystalcluster",
    "spellprism", "spell_prism", "crucible", "salis", "mundus",
)

CORE = ("api", "material", "worldgen", "dimension", "client", "loader",
        "mixin", "integration", "command", "network", "recipe", "block",
        "item", "entity", "machine", "metatileentity", "event", "util",
        "proxy", "config", "pattern", "capability", "amplification")


def classify(rel: str, name: str) -> str:
    hay = (rel + "/" + name).lower()
    for kw in EXCLUDE:
        if kw in hay:
            return "excluded"
    for kw in TC4:
        if kw in hay:
            return "tc4"
    if "magic" in hay:
        return "tc4"          # magic* GT machines, magic blocks, magic materials
    for kw in CORE:
        if kw in hay:
            return "core"
    return "core"


# --------------------------------------------------------------------------
# java sources
# --------------------------------------------------------------------------
IDENT = re.compile(r"[A-Za-z_][A-Za-z0-9_]*")
CAMEL = re.compile(r"[A-Z]+(?=[A-Z][a-z])|[A-Z]?[a-z]+|[A-Z]+|[0-9]+")
STRING = re.compile(r'"([^"\\]{2,60})"')
COMMENT = re.compile(r"//[^\n]*|/\*.*?\*/", re.S)
NOISE = {
    "the", "and", "for", "with", "from", "this", "that", "true", "false",
    "null", "new", "return", "public", "private", "static", "final", "void",
    "class", "extends", "implements", "import", "package", "override",
    "get", "set", "is", "has", "can", "list", "array", "map", "setter",
    "com", "net", "java", "util", "meowmel", "pollution", "minecraft",
    "forge", "gregtech", "gtceu", "item", "items", "block", "blocks",
    "tile", "entity", "entities", "common", "api", "impl", "internal",
    "type", "types", "builder", "build", "init", "register", "registered",
    "string", "int", "boolean", "double", "float", "long", "object",
    "component", "translatable", "resource", "location", "instance", "value",
    "values", "size", "tier", "tiers", "id", "ids", "name", "names",
    "add", "remove", "create", "make", "of", "to", "in", "on", "by",
}


def tokens(text: str) -> set:
    text = COMMENT.sub(" ", text)
    toks = set()
    for m in IDENT.finditer(text):
        ident = m.group(0)
        if len(ident) < 3 or (ident.isupper() and len(ident) > 6):
            continue
        for part in CAMEL.findall(ident):
            part = part.lower()
            if len(part) >= 3 and part not in NOISE and not part.isdigit():
                toks.add(part)
    for m in STRING.finditer(text):
        for part in re.split(r"[^A-Za-z0-9]+", m.group(1)):
            part = part.lower()
            if len(part) >= 3 and part not in NOISE and not part.isdigit():
                toks.add(part)
    return toks


def read(path: str) -> str:
    try:
        with open(path, "r", encoding="utf-8", errors="ignore") as fh:
            return fh.read()
    except OSError:
        return ""


def java_files(root: str):
    for dirpath, _d, files in os.walk(root):
        for f in files:
            if f.endswith(".java"):
                full = os.path.join(dirpath, f)
                yield full, os.path.relpath(full, root).replace("\\", "/"), f[:-5]


def jaccard(a: set, b: set) -> float:
    if not a or not b:
        return 0.0
    inter = len(a & b)
    if not inter:
        return 0.0
    return inter / float(len(a | b))


# --------------------------------------------------------------------------
# machine inventory
# --------------------------------------------------------------------------
UP_MTE = re.compile(
    r"registerMetaTileEntity\(\s*\d+\s*,\s*new\s+(\w+)\s*\(\s*PollutionID\(\s*\"([^\"]+)\"")
UP_MTE_LOOP = re.compile(r"PollutionID\(\s*\"([^\"]+)\"\s*\+")
UP_MTE_FMT = re.compile(r"PollutionID\(\s*String\.format\(\s*\"([^\"]+)\"")

PORT_MACHINE = re.compile(r'\.machine\(\s*"([a-z0-9_]+)"')
PORT_MULTI = re.compile(r'\.multi\(\s*"([a-z0-9_]+)"')
PORT_TIERED = re.compile(r'registerTieredMachines\(\s*[\w.]*?,\s*"([a-z0-9_]+)"')


def norm_id(mid: str) -> str:
    s = mid.lower()
    for pre in ("tiered:", "pollution:tiered:"):
        if s.startswith(pre):
            s = s[len(pre):]
    if s.startswith("pollution_"):
        s = s[len("pollution_"):]
    s = re.sub(r"\.(ulv|lv|mv|hv|ev|iv|luv|zpm|uv|uhv|uev|uiv|max)$", "", s)
    s = s.replace("%s", "").replace("*", "")
    return s.strip("._")


def id_tokens(mid: str) -> set:
    return {p for p in re.split(r"[^a-z0-9]+", norm_id(mid)) if p}


def upstream_machines() -> dict:
    """normalised id -> (raw id, class name)"""
    out = {}
    text = read(os.path.join(UPSTREAM, "meowmel", "pollution", "common",
                             "metatileentity", "PollutionMetaTileEntities.java"))
    for cls, mid in UP_MTE.findall(text):
        out[norm_id(mid)] = (mid, cls)
    for mid in UP_MTE_LOOP.findall(text):
        out.setdefault(norm_id(mid) + ".tierloop", (mid + "<tier>", "(tiered loop)"))
    for fmt in UP_MTE_FMT.findall(text):
        out.setdefault(norm_id(fmt) + ".tierloop", (fmt + "<args>", "(tiered loop)"))
    return out


def port_machines() -> dict:
    out = {}
    for full, rel, _name in java_files(PORT):
        text = read(full)
        found = []
        found += PORT_MACHINE.findall(text)
        found += PORT_MULTI.findall(text)
        found += PORT_TIERED.findall(text)
        for mid in found:
            out.setdefault(norm_id(mid), (mid, rel))
    return out


def match_machine(up_id: str, port_ids: dict):
    """Return the best-matching port id for an upstream id, or None."""
    ut = id_tokens(up_id)
    if not ut:
        return None
    best, score = None, 0.0
    for pid in port_ids:
        pt = id_tokens(pid)
        if not pt:
            continue
        if ut <= pt or pt <= ut:
            s = 1.0
        else:
            s = len(ut & pt) / float(len(ut | pt))
        if s > score:
            best, score = pid, s
    return best if score >= 0.5 else None


# --------------------------------------------------------------------------
# file matching
# --------------------------------------------------------------------------
def file_index(root: str):
    idx = []
    for full, rel, name in java_files(root):
        text = read(full)
        idx.append({
            "rel": rel,
            "name": name,
            "tokens": tokens(text),
            "loc": text.count("\n") + 1,
            "cls": classify(rel, name),
        })
    return idx


def best_match(entry, pool):
    best, score = None, 0.0
    for other in pool:
        s = jaccard(entry["tokens"], other["tokens"])
        if s > score:
            best, score = other, s
    return best, score


def recipe_builders(text: str) -> int:
    pat = re.compile(r"\.recipeBuilder\(|GTRecipeBuilder\.|addRecipe\(|\.buildAndRegister\(|"
                     r"ThaumcraftApi\.add|addInfusionCraftingRecipe|InfusionRecipeBuilder")
    return len(pat.findall(text))


def todo_markers(text: str) -> int:
    pat = re.compile(r"\bTODO\b|\bFIXME\b|\bXXX\b|not (?:yet )?(?:ported|implemented)|"
                     r"placeholder|stub\b", re.I)
    return len(pat.findall(text))
# --------------------------------------------------------------------------
# report
# --------------------------------------------------------------------------
MATCH_OK = 0.50
MATCH_PARTIAL = 0.22


def machine_report():
    """[(norm_id, raw_id, upstream_class, scope, matching_port_id_or_None)]"""
    ups = upstream_machines()
    ports = port_machines()
    rows = []
    for uid, (raw, cls) in sorted(ups.items()):
        rows.append((uid, raw, cls, classify("", uid), match_machine(uid, ports)))
    return rows, ports


def file_report():
    up = file_index(UPSTREAM)
    port = file_index(PORT)
    port_by_name = defaultdict(list)
    for e in port:
        port_by_name[e["name"]].append(e)
    rows = []
    for e in up:
        same_name = port_by_name.get(e["name"])
        if same_name:
            best, score = same_name[0], 1.0
        else:
            best, score = best_match(e, port)
        rows.append({"up": e, "port": best, "score": score})
    return rows, up, port


def recipe_report():
    out = {}
    for label, root in (("upstream", UPSTREAM), ("port", PORT)):
        total = 0
        per = []
        for full, rel, name in java_files(os.path.join(root, "meowmel", "pollution", "loaders",
                                                       "recipes")):
            text = read(full)
            if classify(rel, name) == "excluded":
                continue
            n = recipe_builders(text)
            total += n
            per.append((name, text.count("\n") + 1, n))
        out[label] = (total, sorted(per))
    return out


def emit(lines, text=""):
    lines.append(text)
    print(text)


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--md", default=None)
    args = ap.parse_args()

    lines = []

    # ---- machines -------------------------------------------------------
    mrows, ports = machine_report()
    scoped = [r for r in mrows if r[3] != "excluded"]
    excluded = [r for r in mrows if r[3] == "excluded"]
    have = [r for r in scoped if r[4]]
    miss = [r for r in scoped if not r[4]]

    emit(lines, "=" * 78)
    emit(lines, "POLLUTION 1.12.2 -> 1.20.1  |  Thaumcraft-4 scope only")
    emit(lines, "=" * 78)
    emit(lines, "")
    emit(lines, "[machines] upstream ids: %d  (tc4/core %d, excluded %d)"
         % (len(mrows), len(scoped), len(excluded)))
    emit(lines, "[machines] port ids parsed: %d" % len(ports))
    emit(lines, "[machines] tc4/core matched: %d/%d  (%.0f%%)"
         % (len(have), len(scoped), 100.0 * len(have) / max(1, len(scoped))))
    emit(lines, "")
    emit(lines, "-- MISSING tc4/core machines --")
    for uid, raw, cls, _scope, _m in miss:
        emit(lines, "   %-38s %s" % (uid, cls))
    if not miss:
        emit(lines, "   (none)")

    # ---- files ----------------------------------------------------------
    frows, up, port = file_report()
    fs = [r for r in frows if r["up"]["cls"] != "excluded"]
    ok = [r for r in fs if r["score"] >= MATCH_OK]
    part = [r for r in fs if MATCH_PARTIAL <= r["score"] < MATCH_OK]
    gone = [r for r in fs if r["score"] < MATCH_PARTIAL]
    up_loc = sum(r["up"]["loc"] for r in fs)
    port_loc = sum(r["port"]["loc"] for r in ok + part if r["port"])

    emit(lines, "")
    emit(lines, "[files] upstream java: %d (in scope %d, excluded %d)"
         % (len(up), len(fs), len(up) - len(fs)))
    emit(lines, "[files] port java: %d" % len(port))
    emit(lines, "[files] strong match %d | partial %d | missing %d"
         % (len(ok), len(part), len(gone)))
    emit(lines, "[files] in-scope upstream LOC %d | matched port LOC %d"
         % (up_loc, port_loc))
    emit(lines, "")
    emit(lines, "-- MISSING files (no port counterpart) --")
    for r in sorted(gone, key=lambda r: -r["up"]["loc"]):
        emit(lines, "   %-60s loc=%-5d %s" % (r["up"]["rel"], r["up"]["loc"],
                                              r["up"]["cls"]))
    if not gone:
        emit(lines, "   (none)")
    emit(lines, "")
    emit(lines, "-- PARTIAL files (weak counterpart) --")
    for r in sorted(part, key=lambda r: -r["up"]["loc"])[:40]:
        emit(lines, "   %-58s -> %-40s %.2f"
             % (r["up"]["rel"], r["port"]["rel"] if r["port"] else "-", r["score"]))

    # ---- recipes --------------------------------------------------------
    rr = recipe_report()
    emit(lines, "")
    emit(lines, "[recipes] in-scope builder calls: upstream %d | port %d  (%.0f%%)"
         % (rr["upstream"][0], rr["port"][0],
            100.0 * rr["port"][0] / max(1, rr["upstream"][0])))
    for label in ("upstream", "port"):
        emit(lines, "   %s:" % label)
        for name, loc, n in rr[label]:
            emit(lines, "     %-32s loc=%-6d builders=%d" % (name, loc, n))

    if args.md:
        with open(args.md, "w", encoding="utf-8") as fh:
            fh.write("```\n" + "\n".join(lines) + "\n```\n")
        print("\nwrote %s" % args.md)
    return 0


if __name__ == "__main__":
    sys.exit(main())