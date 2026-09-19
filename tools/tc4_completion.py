"""TC4 completion audit v2 - precise, per-family recipe and machine counts.

Scope: Thaumcraft (TC4) content only. Other mod integrations (Astral Sorcery,
Blood Magic, Botania, AE2, GTNN, GCYM) are reported separately as OUT OF SCOPE.
"""
import io
import os
import re

PORT = 'src/main/java'
UP = r'H:/MinecraftMods/Pollution/src/main/java'

RECIPE_FAMILIES = [
    # (label, upstream file, port file, in_tc4_scope)
    ('Magic chemical (TC4 essentia/infusion chemistry)', 'MagicChemicalRecipes', 'MagicChemicalRecipes', True),
    ('Magic GCYM (multiblock controller recipes)', 'MagicGCYMRecipes', 'MagicGCYMRecipes', True),
    ('Magic integration (cross-mod magic bridge)', 'MagicIntegrationRecipes', 'MagicIntegrationRecipes', True),
    ('Thaumcraft bridging', 'ThaumcraftRecipes', 'ThaumcraftRecipes', True),
    ('Forge alchemy (metal transmute)', 'ForgeAlchemyRecipes', 'ForgeAlchemyRecipes', True),
    ('Node fusion', 'NodeFusionRecipes', 'NodeFusionRecipes', True),
    ('Compound aspects', 'CompoundAspectRecipes', 'CompoundAspectRecipes', True),
    ('Magic fuel (magic fuel / flux)', 'MagicFuelRecipes', 'MagicFuelRecipes', True),
    ('Mana -> EU conversion', 'ManaToEuRecipes', 'ManaToEuRecipes', True),
    ('Starstream nexus', 'StarstreamNexusRecipes', 'StarstreamNexusRecipes', True),
    ('Machine crafting recipes', 'MachineRecipes', 'PollutionRecipes', True),
    ('-- out of scope below --', '', '', False),
    ('Astral Sorcery', 'AstralSorcery', None, False),
    ('Blood Altar', 'BloodAltar', None, False),
    ('Blood Circuit', 'BloodCircuit', None, False),
    ('Botania', 'Botania', 'BotaniaRecipes', False),
    ('Constellation tower', 'ConstellationTowerRecipes', None, False),
    ('Crystal line', 'CrystalLine', None, False),
    ('Flesh tree', 'FleshTreeRecipes', None, False),
    ('Magic guide', 'MagicGuideRecipes', None, False),
    ('Magic hatch', 'MagicHatchRecipes', None, False),
    ('Meteors', 'MeteorsHelper', None, False),
]


def read(p):
    try:
        return io.open(p, encoding='utf-8', errors='replace').read()
    except OSError:
        return ''


def find(root, stem):
    hits = []
    for base, _d, files in os.walk(root):
        for f in files:
            if f.endswith('.java') and f[:-5] == stem:
                hits.append(os.path.join(base, f))
    return hits


def count_recipes(path):
    """Count GT recipe entries: buildAndRegister() calls plus recipe-map adds."""
    if not path:
        return None
    t = read(path)
    n = len(re.findall(r'\.buildAndRegister\(\)', t))
    n += len(re.findall(r'\bGTRecipeBuilder\.of\(', t))
    if n == 0:
        n = len(re.findall(r'\.recipeBuilder\(|addRecipe\(', t))
    return n, t.count('\n') + 1


print('=' * 92)
print('TC4 RECIPE FAMILY COVERAGE  (entries = .buildAndRegister() / GTRecipeBuilder.of())')
print('=' * 92)
print('%-48s %8s %8s %8s %8s' % ('family', 'UP ent', 'PORT ent', 'UP ln', 'PORT ln'))
print('-' * 92)
tot_up = tot_port = 0
for label, up_stem, port_stem, in_scope in RECIPE_FAMILIES:
    if not up_stem:
        print()
        continue
    upf = find(os.path.join(UP, 'meowmel/pollution/loaders/recipes'), up_stem)
    up_res = count_recipes(upf[0] if upf else None)
    port_res = None
    if port_stem:
        pf = find(os.path.join(PORT, 'meowmel/pollution/loaders/recipes'), port_stem)
        port_res = count_recipes(pf[0] if pf else None)
    up_e = up_res[0] if up_res else 0
    up_l = up_res[1] if up_res else 0
    p_e = port_res[0] if port_res else 0
    p_l = port_res[1] if port_res else 0
    pct = ('%d%%' % round(100 * p_e / up_e)) if up_e else 'n/a'
    mark = ' ' if in_scope else '*'
    print('%-48s %8d %8d %8d %8d  %s%s' % (label[:48], up_e, p_e, up_l, p_l, pct, mark))
    if in_scope:
        tot_up += up_e
        tot_port += p_e
print('-' * 92)
print('%-48s %8d %8d              %d%%' % ('TC4-scope TOTAL', tot_up, tot_port,
                                           round(100 * tot_port / tot_up) if tot_up else 0))
print('(* = out of scope: non-TC4 mod integration)')

print()
print('=' * 92)
print('SOURCE SIZE BY SUBSYSTEM (java lines, TC4 relevant)')
print('=' * 92)
groups = {
    'machines (common/machine, port) / metatileentity (up)':
        ('meowmel/pollution/common/machine', 'meowmel/pollution/common/metatileentity'),
    'api (pollution core + magic api)':
        ('meowmel/pollution/api', 'meowmel/pollution/api'),
    'recipes (loaders/recipes)':
        ('meowmel/pollution/loaders/recipes', 'meowmel/pollution/loaders/recipes'),
    'blocks (common/block)':
        ('meowmel/pollution/common/block', 'meowmel/pollution/common/block'),
    'items (common/item)':
        ('meowmel/pollution/common/item', 'meowmel/pollution/common/items'),
    'worldgen / dimension':
        ('meowmel/pollution/dimension', 'meowmel/pollution/dimension'),
    'warp events':
        ('meowmel/pollution/common/warp', 'meowmel/pollution/common/warpevent'),
    'entities':
        ('meowmel/pollution/common/entity', 'meowmel/pollution/common/entity'),
    'materials (api/unification)':
        ('meowmel/pollution/api/unification', 'meowmel/pollution/api/unification'),
    'client':
        ('meowmel/pollution/client', 'meowmel/pollution/client'),
}
print('%-52s %10s %10s' % ('subsystem', 'UP lines', 'PORT lines'))
print('-' * 92)
for label, (port_sub, up_sub) in groups.items():
    def lines(root, sub):
        d = os.path.join(root, sub)
        if not os.path.isdir(d):
            return 0
        return sum(read(os.path.join(b, f)).count('\n') + 1
                   for b, _x, fs in os.walk(d) for f in fs if f.endswith('.java'))
    up_l = lines(UP, up_sub)
    p_l = lines(PORT, port_sub)
    pct = ('%d%%' % round(100 * p_l / up_l)) if up_l else 'n/a'
    print('%-52s %10d %10d  %s' % (label, up_l, p_l, pct))