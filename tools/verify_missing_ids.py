"""Verify the 6 'MISSING' upstream ids against the actual port tree."""
import io, os, re

PORT = 'src/main/java'
UP = 'H:/MinecraftMods/Pollution/src/main/java'

checks = {
    'bm_hpca': [r'bm_hpca', r'BMHPCA'],
    'flux_clear': [r'flux_clear', r'FluxClear'],
    'pollution_muffler_hatch': [r'muffler_hatch', r'FluxMuffler', r'MUFFLER'],
    'tarot_hatch': [r'tarot_hatch', r'TarotHatch'],
    'flux_promoted_fuel_cell': [r'flux_fuel_cell', r'FluxFuelCell'],
    'pollution_small_node_generator': [r'small_node_generator', r'SmallNodeGenerator'],
}

files = {}
for d, _, fs in os.walk(PORT):
    for f in fs:
        if f.endswith('.java'):
            fp = os.path.join(d, f)
            files[fp.replace('\\', '/')] = io.open(fp, encoding='utf-8', errors='ignore').read()

for key, pats in checks.items():
    print('=' * 70)
    print('UPSTREAM ID:', key)
    for pat in pats:
        rx = re.compile(pat)
        ev = [p for p, t in files.items() if rx.search(t)]
        if ev:
            print('  pattern %-28s -> %d file(s)' % (pat, len(ev)))
            for p in sorted(ev)[:6]:
                print('      ', p)
        else:
            print('  pattern %-28s -> none' % pat)

print('=' * 70)
print('UPSTREAM registrations for these ids:')
up = io.open(os.path.join(UP, 'meowmel/pollution/common/metatileentity/PollutionMetaTileEntities.java'),
             encoding='utf-8', errors='ignore').read()
for i, l in enumerate(up.split('\n'), 1):
    if re.search(r'bm_hpca|flux_clear|muffler_hatch|tarot_hatch|fuel_cell|small_node', l):
        print('  L%d %s' % (i, l.strip()[:150]))