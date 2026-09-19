"""Print port warp event ids + magic sweep item status (de-truncated)."""
import io
import os
import re

PORT = 'src/main/java'
UP = r'H:/MinecraftMods/Pollution/src/main/java'


def read(p):
    try:
        return io.open(p, encoding='utf-8', errors='replace').read()
    except OSError:
        return ''


t = read(os.path.join(PORT, 'meowmel/pollution/common/warp/PollutionWarpEvents.java'))
ids = re.findall(r'add\("([a-z_]+)"', t)
print('PORT warp events registered: %d -> %s' % (len(ids), sorted(ids)))
print('PORT warp file lines:', t.count('\n') + 1)

print('\n-- port warp scheduler tick() body (tail) --')
print(t[-2200:])

print('\n' + '=' * 78)
print('MAGIC SWEEP: port item registrations')
print('=' * 78)
t = read(os.path.join(PORT, 'meowmel/pollution/common/item/PollutionItems.java'))
for i, l in enumerate(t.splitlines(), 1):
    if re.search(r'SWEEP|sweep', l):
        print('  L%-5d %s' % (i, l.strip()[:170]))
print('  (total lines: %d)' % (t.count('\n') + 1))

print('\n-- any sweep behaviour class in port? --')
hits = []
for base, _d, files in os.walk(PORT):
    for f in files:
        if f.endswith('.java'):
            p = os.path.join(base, f)
            c = read(p)
            if re.search(r'[Ss]weep', c):
                hits.append((p.replace('\\', '/').replace(PORT + '/', ''),
                             len(re.findall(r'[Ss]weep', c))))
print('  files:', len(hits))
for f, c in sorted(hits, key=lambda x: -x[1]):
    print('    %-65s %d' % (f, c))

print('\n' + '=' * 78)
print('UPSTREAM magic sweep behaviour (SweepEventLoader) lines: %d'
      % (read(os.path.join(UP, 'meowmel/pollution/common/SweepEventLoader.java')).count('\n') + 1))

print('\n' + '=' * 78)
print('BLOCK/ITEM/ENTITY counts (TC4 scope)')
print('=' * 78)
for root, label in ((UP, 'UP  '), (PORT, 'PORT')):
    n_block = [f for f in os.listdir(root) if False]
    print(label, 'java files:', sum(1 for b, _d, fs in os.walk(root) for f in fs if f.endswith('.java')))


def count_reg(root, pattern):
    total = 0
    for base, _d, files in os.walk(root):
        for f in files:
            if f.endswith('.java'):
                total += len(re.findall(pattern, read(os.path.join(base, f))))
    return total


patterns = {
    'block registrations (regMetaTileEntity/registerBlock)': r'registerMetaTileEntity\(|registerBlock\(',
    'item registrations': r'registerMetaItem\(|registerItem\(',
    'GTRecipeBuilder chains': r'GTRecipeBuilder\.of\(|\.buildAndRegister\(',
    'recipe map decls': r'GTRecipeType|RecipeMap',
}
for label, pat in patterns.items():
    print('%-52s UP=%-6d PORT=%-6d' % (label, count_reg(UP, pat), count_reg(PORT, pat)))