"""verify_flux_scope.py - resolve the last open questions in the TC4 audit.

Questions:
1. Upstream id `flux_clear.*` (MetaTileEntityFluxClear) - is it really missing in
   the port, or renamed?
2. Upstream id `pollution_muffler_hatch.*` (MetaTileEntityFluxMuffler) - same.
3. Upstream id `tarot_hatch` (MetaTileEntityTarotHatch) - same.
4. Upstream id `bm_hpca*` (BMHPCA) - blood-magic subsystem, out of TC4 scope?
"""
import io
import re

PORT = 'src/main/java/meowmel/pollution'


def read(p):
    try:
        return io.open(p, encoding='utf-8', errors='replace').read()
    except IOError:
        return ''


machines = read(PORT + '/common/machine/PollutionMachines.java')

print('=' * 78)
print('1/2/3. Flux scrubber / muffler / tarot hatch - real registrations in port')
print('=' * 78)

# every registrate machine(...) call in the port machine registry
for m in re.finditer(r'\.machine\(\s*([A-Za-z0-9_ +"]+)', machines):
    ids = re.findall(r'"([A-Za-z0-9_]+)"', m.group(1))
    for i in ids:
        if any(k in i for k in ('flux', 'muffler', 'tarot', 'vis', 'scrubber')):
            print('  .machine(...) id fragment:', i)

# the tiered helper registers "<name>_<tier>"
print()
print('  tiered registerTieredMachines names:')
for m in re.finditer(r'registerTieredMachines\(\s*PollutionGTAddon\.REGISTRATE,\s*"([a-z0-9_]+)"', machines):
    print('    -', m.group(1))

print()
print('  direct registrate .multiblock(...) / .machine(...) string ids containing flux/tarot/muffler:')
for m in re.finditer(r'\.(?:machine|multiblock)\(\s*"([a-z0-9_]+)"', machines):
    if any(k in m.group(1) for k in ('flux', 'tarot', 'muffler', 'scrubber', 'vis')):
        print('    -', m.group(1))

print()
print('  port machine classes that are the renamed equivalents:')
for cls, path in (
    ('FluxScrubberMachine', PORT + '/common/machine/single/FluxScrubberMachine.java'),
    ('FluxMufflerMachine', PORT + '/common/machine/part/FluxMufflerMachine.java'),
    ('FluxFuelCellMachine', PORT + '/common/machine/single/FluxFuelCellMachine.java'),
    ('TarotHatchView', PORT + '/api/amplification/TarotHatchView.java'),
):
    t = read(path)
    print('    %-22s lines=%-5s %s' % (cls, len(t.splitlines()), 'OK' if t else 'MISSING'))

print()
print('=' * 78)
print('4. BMHPCA - which mod does it connect to?')
print('=' * 78)
bm = read(PORT + '/common/machine/multiblock/bloodmagic/BMHPCAMachine.java')
if not bm:
    import glob
    hits = [p for p in glob.glob(PORT + '/**/*.java', recursive=True) if 'BMHPCA' in p]
    print('  files with BMHPCA in the name:', hits)
    t = read(hits[0]) if hits else ''
else:
    t = bm
print('  mentions bloodmagic/wayoftime/life_essence:',
      [k for k in ('bloodmagic', 'wayoftime', 'LifeEssence', 'life_essence', 'BloodMagic')
       if k in t])
print('  mentions thaumcraft/vis/aspect:', [k for k in ('thaumcraft', 'Thaumcraft', 'vis', 'aspect')
                                            if k in t])

print()
print('=' * 78)
print('5. magic_sweep - the one confirmed TC4 gap')
print('=' * 78)
items = read(PORT + '/common/item/PollutionItems.java')
print('  MAGIC_SWEEP item entry:', 'MAGIC_SWEEP' in items)
for p in ('src/main/java/meowmel/pollution/common/SweepEventLoader.java',
          'src/main/java/meowmel/pollution/common/event/SweepEventLoader.java'):
    t = read(p)
    if t:
        print('  SweepEventLoader lines:', len(t.splitlines()))
        print('  flight/immunity markers:',
              [k for k in ('allowFlying', 'setCanceled', 'grantFlyAbility', 'BaublesApi')
               if k in t])
        break
else:
    import glob
    hits = glob.glob('src/main/java/**/*Sweep*.java', recursive=True) + \
        glob.glob('src/main/java/**/*sweep*.java', recursive=True)
    print('  port files matching *Sweep*:', hits or 'none')