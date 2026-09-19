"""Probe: derive machine id lists from both sides."""
import io, os, re, glob, json

UP = 'H:/MinecraftMods/Pollution/src/main/java/meowmel/pollution'
PORT = 'src/main/java/meowmel/pollution'

print('=== UPSTREAM files containing registerMetaTileEntity / PollutionID ===')
for f in glob.glob(UP + '/**/*.java', recursive=True):
    t = io.open(f, encoding='utf-8', errors='replace').read()
    if 'registerMetaTileEntity' in t or 'PollutionID(' in t:
        print('  ', os.path.relpath(f, UP).replace('\\', '/'),
              'MTEreg=', t.count('registerMetaTileEntity'), 'POID=', t.count('PollutionID('))

print()
print('=== PORT machine model files (ids) ===')
models = sorted(os.path.basename(p)[:-5] for p in
                glob.glob('src/generated/resources/assets/pollution/models/block/machine/*.json'))
print('  count =', len(models))
print('  sample =', models[:15])

print()
print('=== PORT registrate calls ===')
hits = {}
for f in glob.glob(PORT + '/**/*.java', recursive=True):
    t = io.open(f, encoding='utf-8', errors='replace').read()
    n = len(re.findall(r'\.(?:machine|multiblock)\(', t))
    if n:
        hits[os.path.relpath(f, PORT).replace('\\', '/')] = n
for k, v in sorted(hits.items(), key=lambda kv: -kv[1]):
    print(f'  {v:4d}  {k}')

print()
print('=== PORT generated lang keys (block.pollution.*, no tooltip) ===')
d = json.load(io.open('src/generated/resources/assets/pollution/lang/en_us.json', encoding='utf-8'))
keys = [k[len('block.pollution.'):] for k in d if k.startswith('block.pollution.') and '.tooltip' not in k]
print('  count =', len(keys))