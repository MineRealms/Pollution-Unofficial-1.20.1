"""verify_tarot.py - is the tarot hatch really unported, and which mod owns tarot cards?"""
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


print('=' * 78)
print('A. upstream: every file mentioning tarot (case-insensitive)')
print('=' * 78)
for f in sorted(glob.glob(UP + '/**/*.java', recursive=True)):
    t = read(f)
    if re.search(r'tarot', t, re.I):
        print('  ', os.path.relpath(f, UP), '(%d lines)' % len(t.splitlines()))

print()
print('=' * 78)
print('B. upstream TarotCards.java (what a card is)')
print('=' * 78)
tc = read(UP + '/api/recipes/properties/TarotCards.java')
print('\n'.join(tc.splitlines()[:45]))

print()
print('=' * 78)
print('C. upstream MetaTileEntityTarotHatch.java (full)')
print('=' * 78)
print(read(UP + '/common/metatileentity/multiblockpart/MetaTileEntityTarotHatch.java'))

print()
print('=' * 78)
print('D. port: POMultiblockAbility contents (is TAROT_HATCH declared?)')
print('=' * 78)
pma = read(PORT + '/api/metatileentity/POMultiblockAbility.java')
print('\n'.join(l for l in pma.splitlines() if l.strip() and not l.strip().startswith(('*', '/*', '//'))))

print()
print('=' * 78)
print('E. port: who supplies getActiveTarot()?')
print('=' * 78)
for f in sorted(glob.glob(PORT + '/**/*.java', recursive=True)):
    t = read(f)
    if 'getActiveTarot' in t:
        print('  ', os.path.relpath(f, PORT),
              '-> impl:', bool(re.search(r'String\s+getActiveTarot\s*\(\s*\)\s*\{', t)))