"""Final targeted verification: warp depth, magic sweep stub, multiblock flux clear."""
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


print('=' * 78)
print('A. WARP: upstream event classes vs port')
print('=' * 78)
up_events = [f for f in walk(UP) if '/warpevent/events/' in f]
print('upstream Warp*Event implementation classes: %d' % len(up_events))
for f in sorted(up_events):
    print('   ', os.path.basename(f))

print('\nupstream WarpEvents.java registrar content:')
t = read(os.path.join(UP, 'meowmel/pollution/common/warpevent/WarpEvents.java'))
print('  lines:', t.count('\n') + 1)
for l in t.splitlines():
    s = l.strip()
    if 'register' in s or 'new Warp' in s:
        print('     ', s[:140])

print('\nport warp implementation:')
for f in sorted(walk(PORT)):
    if '/common/warp/' in f:
        t = read(f)
        print('\n--- %s (%d lines)' % (f.replace(PORT + '/', ''), t.count('\n') + 1))
        print(t)

print('=' * 78)
print('B. MAGIC SWEEP')
print('=' * 78)
t = read(os.path.join(PORT, 'meowmel/pollution/common/item/PollutionItems.java'))
for i, l in enumerate(t.splitlines(), 1):
    if 'SWEEP' in l:
        print('  PORT L%-5d %s' % (i, l.strip()[:160]))
print()
t = read(os.path.join(UP, 'meowmel/pollution/common/items/PollutionMetaItems.java'))
for i, l in enumerate(t.splitlines(), 1):
    if 'SWEEP' in l:
        print('  UP   L%-5d %s' % (i, l.strip()[:160]))

print('=' * 78)
print('C. FLUX CLEAR: single vs multiblock upstream')
print('=' * 78)
for rel in ('meowmel/pollution/common/metatileentity/single/MetaTileEntityFluxClear.java',
            'meowmel/pollution/common/metatileentity/multiblock/MetaTileEntityFluxClear.java'):
    t = read(os.path.join(UP, rel))
    print('\n--- %s (%d lines)' % (rel.split('/')[-1], t.count('\n') + 1))
    print(t[:1500])

print('=' * 78)
print('D. TC4R bridge (port)')
print('=' * 78)
for f in sorted(walk(PORT)):
    if 'compat' in f:
        t = read(f)
        print('  %-70s %d lines' % (f.replace(PORT + '/', ''), t.count('\n') + 1))