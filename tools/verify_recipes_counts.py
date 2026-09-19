"""verify_recipes_counts.py - settle the two suspicious recipe numbers:
1. "Machine crafting recipes: 5 upstream -> 0 port (0%)" - the port generates
   machine recipes through MetaTileEntityLoader.registerMachineRecipe, not
   buildAndRegister, so the counter may miss them.
2. TC4 recipe families: recount with a matcher that also accepts
   registerMachineRecipe / GTRecipeBuilder.of / .EUt(
"""
import glob
import io
import os
import re

UP = 'H:/MinecraftMods/Pollution/src/main/java/meowmel/pollution/loaders/recipes'
PORT = 'src/main/java/meowmel/pollution/loaders/recipes'


def read(p):
    return io.open(p, encoding='utf-8', errors='replace').read()


PATTERNS = {
    'buildAndRegister': r'\.buildAndRegister\s*\(',
    'registerMachineRecipe': r'registerMachineRecipe\s*\(',
    'GTRecipeBuilder': r'GTRecipeBuilder\.of\s*\(',
    'recipeBuilder/other': r'\.recipeBuilder\s*\(',
}


def counts(root, fname):
    p = os.path.join(root, fname)
    if not os.path.exists(p):
        return None
    t = read(p)
    return {k: len(re.findall(v, t, re.M)) for k, v in PATTERNS.items()}


print('=' * 78)
print('1. Machine crafting recipes')
print('=' * 78)
for name in ('MachineRecipes.java', 'PollutionRecipes.java'):
    up = counts(UP, name)
    po = counts(PORT, name)
    print('  upstream %-22s %s' % (name, up))
    print('  port     %-22s %s' % (name, po))

print()
print('=' * 78)
print('2. Port PollutionRecipes: which machine recipe registrars are invoked')
print('=' * 78)
pr = read(PORT + '/PollutionRecipes.java')
print('  lines:', len(pr.splitlines()))
for m in re.finditer(r'private static void (register\w+)\(', pr):
    print('    -', m.group(1))
print('  registerMachineRecipe calls:', len(re.findall(r'registerMachineRecipe\s*\(', pr)))

print()
print('=' * 78)
print('3. TC4 recipe family recount (all four matchers)')
print('=' * 78)
FAMILIES = ['MagicChemicalRecipes', 'MagicGCYMRecipes', 'MagicIntegrationRecipes',
            'ThaumcraftRecipes', 'ForgeAlchemyRecipes', 'NodeFusionRecipes',
            'CompoundAspectRecipes', 'MagicFuelRecipes', 'ManaToEuRecipes',
            'InfusionRecipes']
print('  %-26s %s' % ('family', 'UP' + ' ' * 34 + 'PORT'))
for fam in FAMILIES:
    up = counts(UP, fam + '.java')
    po = counts(PORT, fam + '.java')
    def fmt(c):
        if c is None:
            return 'missing'
        return ' '.join('%s=%d' % (k[:14], v) for k, v in c.items())
    print('  %-26s up: %s' % (fam, fmt(up)))
    print('  %-26s po: %s' % ('', fmt(po)))

print()
print('=' * 78)
print('4. Test / validation assets in the port')
print('=' * 78)
for pat, label in (
    ('src/test/java/**/*.java', 'unit tests'),
    ('src/main/java/**/gametest/**/*.java', 'gametest java'),
    ('src/main/resources/data/**/gametest/**/*.json', 'gametest structures (data)'),
    ('src/generated/**/gametest/**/*.json', 'gametest structures (generated)'),
):
    hits = glob.glob(pat, recursive=True)
    print('  %-30s %d' % (label, len(hits)))
    for h in hits[:5]:
        print('      ', h)

print()
print('=' * 78)
print('5. Port source totals')
print('=' * 78)
total = 0
files = 0
for f in glob.glob('src/main/java/**/*.java', recursive=True):
    files += 1
    total += len(read(f).splitlines())
print('  java files: %d, lines: %d' % (files, total))