"""probe_tc4_gaps2.py - remaining verifications for the TC4 completion audit.

1. count the consolidated warp events in the port vs the 31 upstream classes
2. locate the tarot hatch implementation (upstream `tarot_hatch` machine)
3. list upstream classes with no port counterpart (identifier similarity)
"""
import glob
import io
import os
import re

PORT_JAVA = 'src/main/java/meowmel/pollution'
UP_JAVA = 'H:/MinecraftMods/Pollution/src/main/java/meowmel/pollution'


def read(p):
    return io.open(p, encoding='utf-8', errors='replace').read()


def cls(path):
    return os.path.basename(path)[:-5]


def tokens(name):
    s = re.sub(r'([a-z0-9])([A-Z])', r'\1_\2', name)
    return set(t for t in re.split(r'[^A-Za-z0-9]+', s.lower()) if t)


def jaccard(a, b):
    return len(a & b) / len(a | b) if a and b else 0.0


# --- 1. warp events ---------------------------------------------------------
text = read(PORT_JAVA + '/common/warp/PollutionWarpEvents.java')
ids = re.findall(r'add\(\s*"([a-z_]+)"', text)
print('port consolidated warp events (%d): %s' % (len(ids), sorted(ids)))
up_warp = sorted(cls(x) for x in glob.glob(UP_JAVA + '/**/warpevent/events/*.java', recursive=True))
print('upstream WarpXxx event classes (%d): %s' % (len(up_warp), [w[4:].lower() for w in up_warp]))
print()

# --- 2. tarot hatch ---------------------------------------------------------
print('=== tarot hatch ===')
for f in glob.glob(PORT_JAVA + '/**/*.java', recursive=True):
    t = read(f)
    for m in re.finditer(r'.{0,90}(?i:tarot).{0,90}', t):
        print('%-58s %s' % (cls(f), m.group(0).replace('\n', ' ').strip()[:150]))
print()

# --- 3. upstream classes with no port counterpart ---------------------------
up_classes = {cls(f): f for f in glob.glob(UP_JAVA + '/**/*.java', recursive=True)}
po_classes = {cls(f): f for f in glob.glob(PORT_JAVA + '/**/*.java', recursive=True)}
print('upstream classes: %d, port classes: %d' % (len(up_classes), len(po_classes)))
print()

# TC4 affinity: file mentions thaumcraft
def is_tc4(path):
    return bool(re.search(r'thaumcraft|tc4port|aspect|essentia|vis\b|Warp', read(path), re.I))


up_tc4 = {c: p for c, p in up_classes.items() if is_tc4(p)}
po_names = list(po_classes)
missing = []
for c, p in sorted(up_tc4.items()):
    if c in po_classes:
        continue
    t = tokens(c)
    best = max(((pc, jaccard(t, tokens(pc))) for pc in po_names), key=lambda kv: kv[1])
    if best[1] < 0.5:
        missing.append((c, round(best[1], 2), best[0]))
print('TC4-scoped upstream classes absent from the port tree: %d / %d'
      % (len(missing), len(up_tc4)))
for c, s, b in missing:
    print('   %-52s best=%-22s sim=%s' % (c, b, s))