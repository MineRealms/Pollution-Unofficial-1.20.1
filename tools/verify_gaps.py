"""Verify the suspicious findings from tc4_audit.py against the port tree.

Checks:
  1. warp event subsystem (upstream common/warpevent/**) -> port?
  2. magic sweep (flight + damage immunity) -> port?
  3. the 6 machine ids reported MISSING -> real absence or renaming?
"""
import io
import os
import re

PORT = 'src/main/java'
UP = r'H:/MinecraftMods/Pollution/src/main/java'


def walk(root, ext='.java'):
    out = []
    for base, _dirs, files in os.walk(root):
        for f in files:
            if f.endswith(ext):
                out.append(os.path.join(base, f).replace('\\', '/'))
    return out


def read(path):
    try:
        return io.open(path, encoding='utf-8', errors='replace').read()
    except OSError:
        return ''


def section(title):
    print('\n' + '=' * 78)
    print(title)
    print('=' * 78)


print('port java files:', len(walk(PORT)))
print('upstream java files:', len(walk(UP)))

# ---------------------------------------------------------------- 1. warp
section('1. WARP EVENT SUBSYSTEM')
up_warp = [f for f in walk(UP) if '/warpevent/' in f]
port_warp = [f for f in walk(PORT) if '/warpevent/' in f]
print('upstream warpevent files: %d' % len(up_warp))
print('port     warpevent files: %d' % len(port_warp))
for f in sorted(port_warp):
    print('   ', f.replace(PORT + '/', ''))
print('\nfile names present upstream but not port:')
up_names = {os.path.basename(f) for f in up_warp}
port_names = {os.path.basename(f) for f in port_warp}
for n in sorted(up_names - port_names):
    print('    -', n)

# any warp references anywhere in the port?
hits = []
for f in walk(PORT):
    t = read(f)
    if re.search(r'Warp(Warp|Event|Flux)', t):
        hits.append((os.path.basename(f), len(re.findall(r'\bWarp', t))))
print('\nport files mentioning Warp*: %d' % len(hits))
for n, c in sorted(hits, key=lambda x: -x[1])[:20]:
    print('    %-40s %d refs' % (n, c))

# ---------------------------------------------------------------- 2. sweep
section('2. MAGIC SWEEP (flight + damage immunity)')
for root, label in ((UP, 'upstream'), (PORT, 'port')):
    found = []
    for f in walk(root):
        t = read(f)
        if 'MAGIC_SWEEP' in t or 'MagicSweep' in t or 'magic_sweep' in t:
            found.append(os.path.relpath(f, root).replace('\\', '/'))
    print('%s files referencing magic sweep: %d' % (label, len(found)))
    for f in sorted(found):
        print('    ', f)

sweep = [f for f in walk(PORT) if 'Sweep' in os.path.basename(f)]
for f in sweep:
    t = read(f)
    print('\n--- %s (%d lines) ---' % (f, t.count('\n') + 1))
    print(t[:4000])

# ---------------------------------------------------------------- 3. ids
section('3. THE 6 "MISSING" MACHINE IDS')
probes = {
    'bm_hpca': r'bm_hpca|BMHPCA',
    'flux_clear': r'flux_clear|FluxClear',
    'pollution_muffler_hatch': r'pollution_muffler|FluxMuffler',
    'tarot_hatch': r'tarot_hatch|TarotHatch',
    'flux_promoted_fuel_cell': r'flux_promoted_fuel_cell|FluxFuelCell|FluxPromotedFuelCell',
    'pollution_small_node_generator': r'small_node_generator|SmallNodeGenerator',
}
for name, pat in probes.items():
    print('\n[%s]' % name)
    for root, label in ((UP, 'UP '), (PORT, 'PORT')):
        files = []
        for f in walk(root):
            t = read(f)
            if re.search(pat, t):
                c = len(re.findall(pat, t))
                files.append((os.path.relpath(f, root).replace('\\', '/'), c))
        files.sort(key=lambda x: -x[1])
        print('  %s %d files' % (label, len(files)))
        for f, c in files[:8]:
            print('       %-70s %d' % (f, c))
