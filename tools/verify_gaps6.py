# -*- coding: utf-8 -*-
"""Nail down the remaining 6 audit MISSING entries."""
import io, os, re

UP = r'H:/MinecraftMods/Pollution/src/main/java'
PO = 'src/main/java'
UP_MTE = UP + '/meowmel/pollution/common/metatileentity/PollutionMetaTileEntities.java'

def read(p):
    return io.open(p, encoding='utf-8', errors='replace').read()

print('=' * 78)
print('A. UPSTREAM registration lines for the 4 flagged machines')
print('=' * 78)
L = read(UP_MTE).splitlines()
for i, l in enumerate(L):
    if re.search(r'FLUX_CLEAR|FLUX_PROMOTED_FUEL_CELL|FLUX_MUFFLER|SMALL_NODE_GENERATOR|TAROT', l):
        print('  %4d | %s' % (i + 1, l.strip()[:150]))

print()
print('=' * 78)
print('B. UPSTREAM tier id arrays (LOOP over VNF)')
print('=' * 78)
for i, l in enumerate(L):
    if re.search(r'new MetaTileEntity.*(FluxClear|FluxPromotedFuelCell|SmallNodeGenerator|FluxMuffler|TarotHatch)', l):
        print('  %4d | %s' % (i + 1, l.strip()[:200]))

print()
print('=' * 78)
print('C. PORT: is there any tarot hatch / tarot capability at all?')
print('=' * 78)
for d, _, fs in os.walk(PO):
    for f in fs:
        if not f.endswith('.java'):
            continue
        p = os.path.join(d, f)
        t = read(p)
        if re.search(r'[Tt]arot', t):
            hits = [l.strip()[:120] for l in t.splitlines() if re.search(r'[Tt]arot', l)]
            print('  %s (%d)' % (p, len(hits)))
            for h in hits[:5]:
                print('      ' + h)

print()
print('=' * 78)
print('D. PORT: FUEL_CELL / SCRUBBER / MUFFLER / NODE_GEN class + superclass')
print('=' * 78)
for name in ['FluxFuelCellMachine', 'FluxScrubberMachine', 'FluxMufflerMachine',
             'SmallNodeGeneratorMachine']:
    found = None
    for d, _, fs in os.walk(PO):
        if name + '.java' in fs:
            found = os.path.join(d, name + '.java')
    if not found:
        print('  %-26s NOT FOUND' % name)
        continue
    t = read(found)
    decl = re.search(r'public class ' + name + r'\b[^{]*', t)
    print('  %-26s %s' % (name, re.sub(r'\s+', ' ', decl.group(0))[:150] if decl else '?'))
    for l in t.splitlines():
        if re.search(r'Tiered|tier|super\(', l) and len(l.strip()) < 110:
            print('        ' + l.strip())