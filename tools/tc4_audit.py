"""tc4_audit.py - objective completion audit of the 1.20.1 port against the 1.12.2
upstream, scoped to the Thaumcraft (TC4) portion only.

Method
------
1. Mod affinity is derived from *imports*, not file names. Any upstream/port source
   file referencing `thaumcraft` is TC4; files that only touch botania / astral /
   bloodmagic / ae2 / gtnn are classified into their own bucket and excluded from the
   TC4 score.
2. Machine ids are aligned by *normalised identifier* (tier tokens stripped), because
   the port renames `flux_clear.lv` -> `lv_flux_clear` and uses GTCEu registrate.
3. Remaining subsystems (recipes, aspects, assets, client) are compared by identifier
   fingerprints so that renames do not count as missing work.

Usage:  python tools/tc4_audit.py [--md out.md] [--verbose]
"""
import argparse
import glob
import io
import json
import os
import re
import sys
from collections import defaultdict

UP_ROOT = 'H:/MinecraftMods/Pollution'
PORT_ROOT = '.'
UP_JAVA = UP_ROOT + '/src/main/java/meowmel/pollution'
PORT_JAVA = 'src/main/java/meowmel/pollution'

# ---------------------------------------------------------------------------
# mod affinity from imports
# ---------------------------------------------------------------------------
AFFINITY_RULES = [
    ('thaumcraft', ('thaumcraft', 'dev.tc4port', 'tc4', 'aspect', 'essentia')),
    ('botania', ('botania', 'vazkii.botania', 'extrabotany')),
    ('astral', ('astralsorcery', 'astral')),
    ('bloodmagic', ('bloodmagic', 'wayoftime.bloodmagic')),
    ('ae2', ('appliedenergistics', 'ae2')),
    ('gcym', ('gcym', 'gregicality')),
    ('gtnn', ('gtnn',)),
]


# domain tokens used as a second-stage classifier when a file has no integration imports
DOMAIN_TOKENS = {
    'thaumcraft': ('vis', 'aspect', 'node', 'infus', 'essentia', 'essence', 'flux', 'warp',
                   'aura', 'arcane', 'alchem', 'eldritch', 'thaum', 'crucible', 'golem',
                   'taint', 'primal', 'sweep', 'focus', 'research', 'quantum', 'magic',
                   'crystal', 'eldritch', 'tarot', 'runic', 'knowledge', 'stabilizer'),
    'botania': ('mana', 'endoflame', 'daisy', 'rune_altar', 'petal', 'apothecary', 'terrasteel',
                'alfheim', 'gaia', 'elven', 'dandelifeon', 'botan', 'flower', 'livingwood',
                'dreamwood', 'lexicon', 'natura', 'pylon', 'spark'),
    'astral': ('astral', 'constellation', 'starlight', 'celestial', 'lightwell', 'starstream',
               'nexus', 'obelisk', 'calibration', 'crystal_growth', 'observatory', 'rocket'),
    'bloodmagic': ('blood', 'life_essence', 'sacrifice', 'tartaric', 'hpca', 'demon', 'slate'),
    'ae2': ('certus', 'fluix', 'meteor', 'spatial', 'ae_', 'aer_'),
    'gtnn': ('gtnn', 'gcym'),
}


def mod_affinity(text, name=''):
    """Dominant integration bucket for a source file.

    Stage 1: integration imports (objective, wins outright).
    Stage 2: domain tokens in the file/class name and body (fallback only).
    """
    votes = defaultdict(int)
    for bucket, needles in AFFINITY_RULES:
        for n in needles:
            votes[bucket] += len(re.findall(re.escape(n), text, 0 if bucket == 'thaumcraft' else re.I))
    if votes:
        best = max(votes.items(), key=lambda kv: kv[1])
        # a single incidental mention is not a classification
        if best[1] >= 2:
            return best[0]
    low = (name + '\n' + text).lower()
    for bucket, toks in DOMAIN_TOKENS.items():
        if any(t in low for t in toks):
            return bucket
    return 'core'


def read(path):
    return io.open(path, encoding='utf-8', errors='replace').read()


# ---------------------------------------------------------------------------
# machine id extraction
# ---------------------------------------------------------------------------
TIERS = ['ulv', 'lv', 'mv', 'hv', 'ev', 'iv', 'luv', 'zpm', 'uv', 'uhv',
         'uev', 'uiv', 'uxv', 'opv', 'max']


def normalise(mid):
    """flux_clear.lv / lv_flux_clear / vis_mv -> canonical token set key."""
    s = mid.lower().replace('.', '_')
    s = re.sub(r'_+', '_', s).strip('_')
    toks = [t for t in s.split('_') if t and t not in TIERS]
    return '_'.join(toks)


def upstream_machine_ids():
    """id -> (class, affinity) from upstream PollutionMetaTileEntities.java."""
    path = UP_JAVA + '/common/metatileentity/PollutionMetaTileEntities.java'
    text = read(path)
    # `new MetaTileEntityFoo(PollutionID("bar")`  and  `new X(PollutionID("a." + tier))`
    out = {}
    for m in re.finditer(r'new\s+([A-Za-z0-9_]+)\s*\(\s*PollutionID\(\s*"([^"]+)"', text):
        cls, mid = m.group(1), m.group(2)
        out[mid] = cls
    return out


def upstream_class_affinity():
    """class name -> affinity bucket, from the declaring file's imports."""
    aff = {}
    for f in glob.glob(UP_JAVA + '/**/*.java', recursive=True):
        cls = os.path.basename(f)[:-5]
        aff[cls] = mod_affinity(read(f), cls)
    return aff


def port_machine_ids():
    """ids the port actually registers (registrate model files + tiered helpers)."""
    ids = set()
    for p in glob.glob('src/generated/resources/assets/pollution/models/block/machine/*.json'):
        ids.add(os.path.basename(p)[:-5])
    for f in glob.glob(PORT_JAVA + '/**/*.java', recursive=True):
        text = read(f)
        for m in re.finditer(r'registerTieredMachines\(\s*[^,]+,\s*"([a-z0-9_]+)"', text):
            ids.add(m.group(1))
    return ids


def port_class_affinity():
    aff = {}
    for f in glob.glob(PORT_JAVA + '/**/*.java', recursive=True):
        cls = os.path.basename(f)[:-5]
        aff[cls] = mod_affinity(read(f), cls)
    return aff


def tokens(name):
    """camelCase / snake_case -> lowercase token set."""
    s = re.sub(r'([a-z0-9])([A-Z])', r'\1_\2', name)
    return set(t for t in re.split(r'[^A-Za-z0-9]+', s.lower()) if t)


def jaccard(a, b):
    if not a or not b:
        return 0.0
    return len(a & b) / len(a | b)


def best_match(src_tokens, candidates, threshold=0.5):
    """Return (key, score) of the candidate with the highest token Jaccard."""
    best, score = None, 0.0
    for key, ctoks in candidates:
        s = jaccard(src_tokens, ctoks)
        if s > score:
            best, score = key, s
    return (best, score) if score >= threshold else (None, score)
# ---------------------------------------------------------------------------
# subsystems: presence probes keyed on observable behaviour, not file names
# ---------------------------------------------------------------------------
SUBSYSTEM_PROBES = [
    ('Aspects / aspect registry', r'class\s+TCAspectAddons|AspectRegistryEvent|registerAspect'),
    ('Aspect-storage machines (tank/index)', r'AspectTankMachine|AspectStorage'),
    ('Aspect -> GT fluid bridge', r'POAspectToGtFluidList|AspectToGTFluid'),
    ('Compound aspect recipes', r'CompoundAspectRecipes'),
    ('Vis (aura) generation', r'VisGeneratorMachine|VisProviderMachine'),
    ('Vis hatch (multiblock capability)', r'VisHatchMachine'),
    ('Vis storage container', r'VisContainer|ICleanVis'),
    ('Node machines', r'NodeProducerMachine|NodeWasherMachine|NodeFusionReactorMachine|'
                      r'LargeNodeGeneratorMachine|SmallNodeGeneratorMachine'),
    ('Infusion (TC altar in GT)', r'IndustrialInfusionMachine|InfusionRecipes'),
    ('Infused fluid hatch', r'InfusedFluidHatchMachine'),
    ('Essentia smeltery', r'EssenceSmelterMachine|GtEssenceSmelterMachine'),
    ('Essence collector', r'EssenceCollectorMachine'),
    ('Flux scrubber / muffler', r'FluxScrubberMachine|FluxMufflerMachine'),
    ('Flux fuel cell', r'FluxFuelCellMachine'),
    ('Warp events engine', r'WarpEventHandler|WarpQueue|IWorldTickWarpEvent'),
    # 1.12 registered one class per event; the port registers lambdas/records in a
    # single table, so probe the event ids instead of upstream class names.
    ('Warp event implementations',
     r'"blind"|"nausea"|"countdown_bomb"|"inventory_scramble"|"zombie_siege"'),
    ('Flux warp accumulation', r'FluxWarpManager'),
    # The port only registers the MAGIC_SWEEP item; upstream SweepEventLoader
    # (flight grant + LivingAttackEvent immunity) has no counterpart yet.
    ('Magic sweep (flight + immunity)', r'grantFlyAbility|allowFlying|LivingAttackEvent'),
    ('Forge alchemy (metal transmute)', r'ForgeAlchemyRecipes'),
    ('Thaumcraft recipe bridging', r'ThaumcraftRecipes'),
    ('TC4R infusion JSON emitter', r'INFUSION_SERIALIZER|addInfusionCraftingRecipe'),
    ('Magic energy amplification', r'MagicAmplificationEngine'),
    ('Cultivated crystal cluster', r'CultivatedCrystal|crystal_cluster'),
    ('Alchemical construct / furnace', r'alchemical_construct|alchemical_furnace'),
    ('Arcane worktable', r'arcane_worktable'),
    ('Aspect-gated solar plate', r'SolarPlateMachine'),
    ('Pollution (chunk) engine', r'class\s+PollutionEngine|class\s+PollutionData'),
    ('Machine pollution emission', r'MachinePollution'),
]


def gather_java(root):
    """{path -> text} for every .java under root."""
    out = {}
    for f in glob.glob(root + '/**/*.java', recursive=True):
        out[f.replace('\\', '/')] = read(f)
    return out
def scope_affinity(files):
    """group relative paths by affinity bucket."""
    aff = defaultdict(list)
    for rel, text in files.items():
        aff[mod_affinity(text, os.path.basename(rel)[:-5])].append(rel)
    return aff


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--md')
    ap.add_argument('--verbose', action='store_true')
    args = ap.parse_args()

    lines = []

    def out(s=''):
        lines.append(s)
        print(s)

    up_files = gather_java(UP_JAVA)
    po_files = gather_java(PORT_JAVA)
    up_aff = scope_affinity(up_files)
    po_aff = scope_affinity(po_files)

    out('# Pollution 1.20.1 port - TC4 (Thaumcraft) completion audit')
    out()
    out('## 1. Scope: which source files belong to Thaumcraft')
    out()
    out('| bucket | upstream files | port files |')
    out('|---|---|---|')
    for b in sorted(set(up_aff) | set(po_aff)):
        out(f'| {b} | {len(up_aff.get(b, []))} | {len(po_aff.get(b, []))} |')
    out()
    out(f'java files: upstream {len(up_files)}, port {len(po_files)}')
    out()

    up_ids = upstream_machine_ids()
    up_cls_aff = upstream_class_affinity()
    po_ids = port_machine_ids()

    po_norm = defaultdict(list)
    for mid in po_ids:
        po_norm[normalise(mid)].append(mid)

    tc4_up = {mid: cls for mid, cls in up_ids.items()
              if up_cls_aff.get(cls, 'core') == 'thaumcraft'}
    other_up = {mid: cls for mid, cls in up_ids.items() if mid not in tc4_up}

    out('## 2. Machine coverage (TC4 scope only)')
    out()
    out(f'- upstream registrations: {len(up_ids)} total, **{len(tc4_up)} TC4-scoped** '
        f'({len(other_up)} other-mod entries excluded)')
    out(f'- port machine ids discovered: {len(po_ids)}')
    out()

    matched, renamed, missing = [], [], []
    pcand = [(k, tokens(k)) for k in po_norm]
    for mid, cls in sorted(tc4_up.items()):
        key = normalise(mid)
        if key in po_norm:
            matched.append((mid, cls, po_norm[key][0]))
            continue
        best, score = best_match(tokens(key), pcand, 0.5)
        if best:
            renamed.append((mid, cls, po_norm[best][0], round(score, 2)))
        else:
            missing.append((mid, cls))

    out(f'### 2.1 matched by identical normalised id: {len(matched)}')
    out(f'### 2.2 matched by token similarity (renamed in port): {len(renamed)}')
    out(f'### 2.3 MISSING: {len(missing)}')
    out()
    if renamed:
        out('| upstream id | upstream class | port id | similarity |')
        out('|---|---|---|---|')
        for mid, cls, pid, s in renamed:
            out(f'| `{mid}` | {cls} | `{pid}` | {s} |')
        out()
    if missing:
        out('| upstream id | upstream class |')
        out('|---|---|')
        for mid, cls in missing:
            out(f'| `{mid}` | {cls} |')
        out()
    if args.verbose and matched:
        out('<details><summary>matched by id (click to expand)</summary>')
        out()
        for mid, cls, pid in matched:
            out(f'- `{mid}` ({cls}) -> `{pid}`')
        out()
        out('</details>')
        out()

    # ----- 3. subsystem probes -------------------------------------------
    po_all = '\n'.join(po_files.values())
    out('## 3. Subsystem probes (port tree)')
    out()
    out('| subsystem | present | evidence files |')
    out('|---|---|---|')
    present = 0
    for label, pat in SUBSYSTEM_PROBES:
        rx = re.compile(pat)
        ev = [os.path.basename(p) for p, t in po_files.items() if rx.search(t)]
        ok = bool(ev)
        present += ok
        shown = ', '.join(sorted(ev)[:3]) + ('...' if len(ev) > 3 else '')
        out(f'| {label} | {"yes" if ok else "NO"} | {shown or "-"} |')
    out()
    out(f'subsystem probes passed: {present}/{len(SUBSYSTEM_PROBES)}')
    out()

    if args.md:
        io.open(args.md, 'w', encoding='utf-8').write('\n'.join(lines))
        print('\nwritten ->', args.md)
    return lines


if __name__ == '__main__':
    main()
