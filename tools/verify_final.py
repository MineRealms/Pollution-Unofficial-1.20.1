"""Final targeted verification for the TC4 audit's open questions.

Checks, for every upstream id the audit calls MISSING or tier-degraded:
  * does the port register an equivalent machine id (any tier)?
  * how many tiers are registered on each side?
Also pins down `tarot` and `magic_sweep` scope.
"""
import io
import os
import re
import sys

PORT = 'src/main/java'
UP = r'H:/MinecraftMods/Pollution/src/main/java'


def read(path):
    try:
        return io.open(path, encoding='utf-8', errors='replace').read()
    except OSError:
        return ''


def walk(root):
    for base, _dirs, files in os.walk(root):
        for f in files:
            if f.endswith('.java'):
                yield os.path.join(base, f)


def grep(root, pattern, limit=40):
    rx = re.compile(pattern)
    out = []
    for path in walk(root):
        for i, line in enumerate(read(path).splitlines(), 1):
            if rx.search(line):
                out.append((os.path.relpath(path, root).replace('\\', '/'), i, line.strip()))
                if len(out) >= limit:
                    return out
    return out


def show(title, root, pattern, limit=40):
    print('=' * 78)
    print(title)
    print('=' * 78)
    rows = grep(root, pattern, limit)
    if not rows:
        print('  (no match)')
    for rel, line, text in rows:
        print('  %s:%d  %s' % (rel, line, text[:150]))
    print()


# --- 1. flux scrubber / clear ------------------------------------------------
show('1a. PORT flux_scrubber / flux_clear registrations', PORT,
     r'"(flux_scrubber|flux_clear)|FLUX_SCRUBBER|FluxScrubber')
show('1b. UPSTREAM flux_clear ids', UP,
     r'PollutionID\("flux_clear')

# --- 2. muffler hatch --------------------------------------------------------
show('2a. PORT muffler registrations', PORT, r'flux_muffler|FLUX_MUFFLER')
show('2b. UPSTREAM muffler ids', UP, r'PollutionID\("pollution_muffler_hatch')

# --- 3. tarot hatch ----------------------------------------------------------
show('3a. PORT tarot usage', PORT, r'[Tt]arot')
show('3b. UPSTREAM tarot class header', UP,
     r'class MetaTileEntityTarotHatch|package |^import ')

# --- 4. small node generator tiers ------------------------------------------
print('=' * 78)
print('4. small node generator: tier counts')
print('=' * 78)
up = read(r'H:/MinecraftMods/Pollution/src/main/java/meowmel/pollution/common/'
          r'metatileentity/PollutionMetaTileEntities.java')
port = read(PORT + '/meowmel/pollution/common/machine/PollutionMachines.java')
print('  upstream ids:', re.findall(r'pollution_small_node_generator\.\w+', up))
print('  port SMALL_NODE_GENERATOR_TIERS:',
      re.findall(r'SMALL_NODE_GENERATOR_TIERS\s*=\s*\{([^}]*)\}', port))
print()

# --- 5. flux fuel cell tiers -------------------------------------------------
print('=' * 78)
print('5. flux fuel cell: tier counts')
print('=' * 78)
print('  upstream ids:', re.findall(r'flux_promoted_fuel_cell\.\w+', up))
print('  port refs:', re.findall(r'FLUX_FUEL_CELL\w*\s*=\s*[^;]{0,120}', port))
print()

# --- 6. magic sweep ----------------------------------------------------------
print('=' * 78)
print('6. magic sweep: is it a stub item?')
print('=' * 78)
for rel, line, text in grep(PORT, r'MAGIC_SWEEP'):
    print('  %s:%d  %s' % (rel, line, text[:150]))
print('  --- upstream behaviour markers ---')
for marker in ('allowFlying', 'capabilities.disableDamage', 'setCanceled',
               'getHeldItemMainhand', 'BaublesApi'):
    hits = grep(UP, re.escape(marker), limit=3)
    print('    %-24s -> %d file(s)' % (marker, len(hits)))
print()

# --- 7. bm_hpca scope --------------------------------------------------------
print('=' * 78)
print('7. bm_hpca: which mod does upstream depend on?')
print('=' * 78)
bmpath = (r'H:/MinecraftMods/Pollution/src/main/java/meowmel/pollution/common/'
          r'metatileentity/multiblockpart/BMHPCA/MetaTileEntityBMHPCA.java')
src = read(bmpath)
print('  imports:')
for line in src.splitlines():
    if line.startswith('import'):
        print('    ' + line.strip())