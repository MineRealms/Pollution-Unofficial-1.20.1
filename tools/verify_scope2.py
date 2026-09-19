"""verify_scope2.py - mod affinity of the last two ambiguous upstream ids:
`tarot_hatch` (MetaTileEntityTarotHatch) and `bm_hpca*` (MetaTileEntityBMHPCA).

Mod affinity is decided from the upstream file's own imports, not its name.
"""
import glob
import io
import os
import re

UP = 'H:/MinecraftMods/Pollution/src/main/java/meowmel/pollution'
PORT = 'src/main/java/meowmel/pollution'


def read(p):
    try:
        return io.open(p, encoding='utf-8', errors='replace').read()
    except IOError:
        return ''


def imports_of(text):
    return re.findall(r'^\s*import\s+([\w.]+);', text, re.M)


def bucket(imports):
    joined = ' '.join(imports).lower()
    tags = []
    for tag, needle in (
        ('thaumcraft', 'thaumcraft'), ('botania', 'botania'), ('extrabotania', 'extrabotany'),
        ('astral', 'astralsorcery'), ('bloodmagic', 'bloodmagic'), ('ae2', 'appliedenergistics'),
        ('gtqtcore', 'gtqtcore'), ('gtnn', 'gtnn'), ('mouse', 'mouse'), ('jei', 'jei'),
    ):
        if needle in joined:
            tags.append(tag)
    return tags


for upstream_rel in (
    'common/metatileentity/multiblockpart/MetaTileEntityTarotHatch.java',
    'common/metatileentity/multiblockpart/BMHPCA/MetaTileEntityBMHPCA.java',
    'common/metatileentity/multiblockpart/wireless/MetaTileEntityWirelessManaHatch.java',
):
    p = os.path.join(UP, upstream_rel)
    t = read(p)
    print('=' * 78)
    print('UPSTREAM', upstream_rel, '->', 'FOUND' if t else 'MISSING',
          '(%d lines)' % len(t.splitlines()))
    if not t:
        continue
    imps = imports_of(t)
    print('  imports:', len(imps))
    print('  mod affinity (from imports):', bucket(imps) or 'core/GT only')
    for i in imps:
        if any(k in i.lower() for k in ('thaum', 'bloodmagic', 'botania', 'astral', 'aspect',
                                        'vis', 'tile', 'energy')):
            print('    !', i)

print()
print('=' * 78)
print('PORT side: tarot hatch + bm_hpca presence')
print('=' * 78)
for pat in ('TarotHatch', 'TarotCards', 'ITarotHatch', 'BMHPCA'):
    hits = sorted(set(glob.glob(PORT + '/**/*.java', recursive=True)))
    found = []
    for f in hits:
        if pat.lower() in os.path.basename(f).lower():
            found.append(f)
    print('  filename match %-14s -> %s' % (pat, found or 'none'))
    body = [f for f in hits if pat in read(f)]
    print('  body mentions %-15s -> %d file(s): %s' % (
        pat, len(body), [os.path.relpath(x, PORT) for x in body[:4]]))

print()
print('=' * 78)
print('TarotHatchView.java in full (17 lines)')
print('=' * 78)
print(read(PORT + '/api/amplification/TarotHatchView.java'))