"""Probe: where can we get a definitive list of registered machine ids on each side?"""
import io, json, os, re

PORT = 'src/main/java/meowmel/pollution'
UP = 'H:/MinecraftMods/Pollution/src/main/java/meowmel/pollution'

for label, path in (('PORT', 'src/main/resources/assets/pollution/lang/en_us.json'),
                    ('UPSTREAM', 'H:/MinecraftMods/Pollution/src/main/resources/assets/pollution/lang/en_us.json')):
    if not os.path.exists(path):
        print(label, 'MISSING', path)
        continue
    data = json.load(io.open(path, encoding='utf-8'))
    blocks = [k for k in data if k.startswith('block.pollution.')]
    print(f'--- {label}: total keys={len(data)} block.*={len(blocks)}')
    print('   sample:', blocks[:8])
    machines = [k for k in blocks if 'tooltip' not in k]
    print(f'   block.* without tooltip = {len(machines)}')