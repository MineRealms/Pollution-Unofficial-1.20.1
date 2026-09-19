"""probe_tc4_gaps.py - manually verify the suspicious 'MISSING' verdicts of
tools/tc4_audit.py. Prints the raw registration ids the port actually emits for
the contested upstream ids, plus the presence of the warpevent/sweep subsystems.
"""
import glob
import io
import os
import re

PORT_JAVA = 'src/main/java/meowmel/pollution'
UP_JAVA = 'H:/MinecraftMods/Pollution/src/main/java/meowmel/pollution'


def read(p):
    return io.open(p, encoding='utf-8', errors='replace').read()


def port_ids():
    ids = set()
    for p in glob.glob('src/generated/resources/assets/pollution/models/block/machine/*.json'):
        ids.add(os.path.basename(p)[:-5])
    for f in glob.glob(PORT_JAVA + '/**/*.java', recursive=True):
        for m in re.finditer(r'registerTieredMachines\(\s*[^,]+,\s*"([a-z0-9_]+)"', read(f)):
            ids.add(m.group(1))
    return sorted(ids)


def find(ids, *needles):
    print('--- ids containing %s' % (needles,))
    hits = [i for i in ids if all(n in i for n in needles)]
    for h in hits:
        print('   ', h)
    if not hits:
        print('    (none)')
    return hits


ids = port_ids()
print('total port ids:', len(ids))
print()

find(ids, 'flux')
print()
find(ids, 'muffler')
print()
find(ids, 'tarot')
print()
find(ids, 'hpca')
print()
find(ids, 'vis')
print()

print('=== upstream contested classes: which id strings does the port use? ===')
for cls, needle in [('MetaTileEntityFluxClear', 'FluxClear'),
                    ('MetaTileEntityFluxScrubber', 'FluxScrubber'),
                    ('MetaTileEntityFluxMuffler', 'FluxMuffler'),
                    ('MetaTileEntityTarotHatch', 'Tarot')]:
    hits = []
    for f in glob.glob(PORT_JAVA + '/**/*.java', recursive=True):
        if needle.lower() in os.path.basename(f).lower():
            hits.append(f.replace('\\', '/'))
    print('%-32s -> %s' % (cls, hits or '(no port file)'))
print()

print('=== warpevent / sweep subsystem presence in port ===')
for sub in ['common/warpevent', 'common/SweepEventLoader', 'SweepEventLoader']:
    p = PORT_JAVA + '/' + sub
    if os.path.isdir(p):
        files = [os.path.basename(x) for x in glob.glob(p + '/*')]
        print('%-32s DIR  %d entries: %s' % (sub, len(files), sorted(files)[:12]))
    elif os.path.isfile(p + '.java'):
        print('%-32s FILE' % sub)
    else:
        print('%-32s ABSENT' % sub)
print()

print('=== upstream warpevent/warp classes vs port ===')
up_warp = sorted(os.path.basename(x)[:-5]
                 for x in glob.glob(UP_JAVA + '/**/*.java', recursive=True)
                 if 'warp' in os.path.basename(x).lower())
po_warp = sorted(os.path.basename(x)[:-5]
                 for x in glob.glob(PORT_JAVA + '/**/*.java', recursive=True)
                 if 'warp' in os.path.basename(x).lower())
print('upstream (%d): %s' % (len(up_warp), up_warp))
print('port     (%d): %s' % (len(po_warp), po_warp))