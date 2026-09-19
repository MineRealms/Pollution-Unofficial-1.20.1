"""Locate the port's warp / tarot / flux-clear implementations precisely."""
import io
import os
import re

PORT = 'src/main/java'
UP = r'H:/MinecraftMods/Pollution/src/main/java'


def walk(root):
    out = []
    for base, _d, files in os.walk(root):
        for f in files:
            if f.endswith('.java'):
                out.append(os.path.join(base, f).replace('\\', '/'))
    return out


def read(p):
    try:
        return io.open(p, encoding='utf-8', errors='replace').read()
    except OSError:
        return ''


def find_in_port(names):
    print('--- port files named %s' % names)
    for f in walk(PORT):
        b = os.path.basename(f)
        if any(n.lower() in b.lower() for n in names):
            t = read(f)
            print('  %s  (%d lines)' % (f.replace(PORT + '/', ''), t.count('\n') + 1))


for names in (['Warp'], ['Tarot'], ['Flux'], ['Sweep'], ['Aspect']):
    find_in_port(names)
    print()

# tarot hatch registration probe
print('=' * 78)
print('TAROT / AMPLIFICATION wiring')
print('=' * 78)
for f in walk(PORT):
    t = read(f)
    if 'TAROT' in t or 'Tarot' in t:
        rel = f.replace(PORT + '/', '')
        lines = [l.strip() for l in t.splitlines() if 'TAROT' in l or 'Tarot' in l]
        print('\n  %s' % rel)
        for l in lines[:12]:
            print('      ', l[:150])

print()
print('=' * 78)
print('FLUX CLEAR / SCRUBBER')
print('=' * 78)
for f in walk(PORT):
    t = read(f)
    if 'FluxScrubber' in t or 'FLUX_SCRUBBER' in t or 'flux_scrubber' in t:
        rel = f.replace(PORT + '/', '')
        lines = [l.strip() for l in t.splitlines()
                 if re.search(r'FluxScrubber|FLUX_SCRUBBER|flux_scrubber', l)]
        print('\n  %s (%d lines total)' % (rel, t.count('\n') + 1))
        for l in lines[:10]:
            print('      ', l[:150])

print()
print('=' * 78)
print('UPSTREAM flux_clear registrations (for comparison)')
print('=' * 78)
t = read(os.path.join(UP, 'meowmel/pollution/common/metatileentity/PollutionMetaTileEntities.java'))
for i, l in enumerate(t.splitlines(), 1):
    if re.search(r'flux_clear|FLUX_CLEAR|FLUX_SCRUBBER', l):
        print('  L%-5d %s' % (i, l.strip()[:160]))

print()
print('=' * 78)
print('PORT machine count sanity (registrate calls)')
print('=' * 78)
reg = {}
for f in walk(PORT):
    t = read(f)
    n_single = len(re.findall(r'REGISTRATE\s*\n?\s*\.machine\(', t)) + len(re.findall(r'\.machine\(', t))
    n_multi = len(re.findall(r'\.multiblock\(', t))
    n_tiered = len(re.findall(r'registerTieredMachines\(', t))
    if n_single or n_multi or n_tiered:
        reg[f.replace(PORT + '/', '')] = (n_single, n_multi, n_tiered)
for k, v in sorted(reg.items()):
    print('  %-60s .machine()=%d .multiblock()=%d tiered=%d' % (k, v[0], v[1], v[2]))