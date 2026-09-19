# -*- coding: utf-8 -*-
"""Verify tier coverage of the machines the TC4 audit flagged as MISSING/renamed."""
import io, os, re

UP = r'H:/MinecraftMods/Pollution/src/main/java'
PO = 'src/main/java'

def read(p):
    return io.open(p, encoding='utf-8', errors='replace').read()

def walk(root):
    for d, _, fs in os.walk(root):
        for f in fs:
            if f.endswith('.java'):
                yield os.path.join(d, f)

print('=' * 78)
print('A. UPSTREAM tier ids per flagged machine')
print('=' * 78)
up_mte = read(UP + '/meowmel/pollution/common/metatileentity/PollutionMetaTileEntities.java')
for key in ['flux_promoted_fuel_cell', 'flux_clear', 'pollution_muffler_hatch',
            'pollution_small_node_generator', 'tarot_hatch', 'bm_hpca', 'source_charge']:
    ids = re.findall(r'"(?:pollution:)?[\w./]*' + key + r'[\w./]*"', up_mte)
    print('  %-32s %s' % (key, sorted(set(x.strip('"').replace('pollution:', '') for x in ids))))

print()
print('=' * 78)
print('B. PORT ids with the same stems')
print('=' * 78)
allpo = {}
for p in walk(PO):
    allpo[p] = read(p)
for key in ['flux_fuel_cell', 'flux_scrubber', 'flux_muffler', 'small_node_generator',
            'tarot', 'hpca', 'source_charge']:
    hits = {}
    for p, t in allpo.items():
        for m in re.findall(r'"(?:\w+_){0,3}' + key + r'(?:_\w+)*"', t):
            hits.setdefault(m.strip('"'), set()).add(os.path.basename(p))
    print('  %-24s %s' % (key, sorted(hits.keys())))
    for k in sorted(hits.keys()):
        print('        %-38s <- %s' % (k, ', '.join(sorted(hits[k]))[:90]))

print()
print('=' * 78)
print('C. UPSTREAM tier list constants (ManaGenerator/SolarPlate/etc.)')
print('=' * 78)
for cls, path in [
    ('FluxPromotedFuelCell', UP + '/meowmel/pollution/common/metatileentity/single/MetaTileEntityFluxPromotedFuelCell.java'),
    ('FluxClear(single)', UP + '/meowmel/pollution/common/metatileentity/single/MetaTileEntityFluxClear.java'),
    ('SmallNodeGenerator', UP + '/meowmel/pollution/common/metatileentity/single/MetaTileEntitySmallNodeGenerator.java'),
    ('FluxMuffler(part)', UP + '/meowmel/pollution/common/metatileentity/multiblockpart/MetaTileEntityFluxMuffler.java'),
    ('TarotHatch(part)', UP + '/meowmel/pollution/common/metatileentity/multiblockpart/MetaTileEntityTarotHatch.java'),
]:
    if not os.path.exists(path):
        print('  %-22s MISSING FILE' % cls)
        continue
    t = read(path)
    print('  %-22s %s' % (cls, ' | '.join(
        l.strip() for l in t.splitlines()
        if re.search(r'GTValues\.(LV|MV|HV|EV|IV|LuV|ZPM|UV|UHV)|int\[\]|TIER', l) and 'import' not in l)[:300]))