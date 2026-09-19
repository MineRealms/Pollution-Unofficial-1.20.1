"""Confirm multiblock FluxClear id, dimension data files, and lang coverage."""
import io
import os
import re

PORT = 'src/main/java'
UP = r'H:/MinecraftMods/Pollution/src/main/java'
PORT_RES = 'src/main/resources'
PORT_GEN = 'src/generated/resources'


def walk(root, ext='.java'):
    out = []
    if not os.path.isdir(root):
        return out
    for base, _d, files in os.walk(root):
        for f in files:
            if f.endswith(ext):
                out.append(os.path.join(base, f).replace('\\', '/'))
    return out


def read(p):
    try:
        return io.open(p, encoding='utf-8', errors='replace').read()
    except OSError:
        return ''


print('=' * 78)
print('A. upstream MetaTileEntityFluxClear (multiblock) registration site')
print('=' * 78)
for f in walk(UP):
    t = read(f)
    if 'MetaTileEntityFluxClear' in t:
        rel = os.path.relpath(f, UP).replace('\\', '/')
        for i, l in enumerate(t.splitlines(), 1):
            if 'MetaTileEntityFluxClear' in l:
                print('  %-70s L%-5d %s' % (rel.split('/')[-1], i, l.strip()[:120]))

print('\n' + '=' * 78)
print('B. port: multiblock flux/muffler machines registered?')
print('=' * 78)
t = read(os.path.join(PORT, 'meowmel/pollution/common/machine/PollutionMachines.java'))
for i, l in enumerate(t.splitlines(), 1):
    if re.search(r'Flux|flux', l):
        print('  L%-5d %s' % (i, l.strip()[:150]))

print('\n' + '=' * 78)
print('C. dimension / worldgen data files in port')
print('=' * 78)
for root in (PORT_RES, PORT_GEN):
    ds = [f for f in walk(root, '.json') if '/dimension' in f or '/worldgen' in f]
    print('%s -> %d dimension/worldgen json' % (root, len(ds)))
    for f in sorted(ds)[:40]:
        print('   ', f.replace(root + '/', ''))

print('\n' + '=' * 78)
print('D. lang coverage for machines (TC4 scope)')
print('=' * 78)
lang_files = [f for f in walk(PORT_GEN, '.json') if 'lang' in f]
lang_files += [f for f in walk(PORT_RES, '.json') if 'lang' in f]
keys = {}
for f in lang_files:
    t = read(f)
    for k in re.findall(r'"([a-z0-9_.]+)":\s*"', t):
        keys[k] = f.replace('\\', '/').split('/')[-1]
print('lang files: %d, total keys: %d' % (len(lang_files), len(keys)))
for pat, label in ((r'^block\.pollution\.', 'block.'), (r'^item\.pollution\.', 'item.'),
                   (r'^pollution\.machine\.', 'pollution.machine.')):
    n = [k for k in keys if re.match(pat, k)]
    print('  %-20s %d keys' % (label, len(n)))

print('\n' + '=' * 78)
print('E. recipe files comparison (TC4 scope)')
print('=' * 78)
up_r = {os.path.basename(f) for f in walk(os.path.join(UP, 'meowmel/pollution/loaders/recipes'))}
port_r = {os.path.basename(f) for f in walk(os.path.join(PORT, 'meowmel/pollution/loaders/recipes'))}
print('upstream recipe files: %d / port: %d' % (len(up_r), len(port_r)))
print('\nonly upstream:')
for n in sorted(up_r - port_r):
    print('    -', n)
print('\nonly port:')
for n in sorted(port_r - up_r):
    print('    +', n)