"""Count warp-event entries in both trees and magic-sweep wiring in the port."""
import io, os, re

PORT = 'src/main/java'
UP = 'H:/MinecraftMods/Pollution/src/main/java'

p = os.path.join(PORT, 'meowmel/pollution/common/warp/PollutionWarpEvents.java')
t = io.open(p, encoding='utf-8', errors='ignore').read()
ids = re.findall(r'add\("([a-z_]+)"', t)
print('PORT warp event entries:', len(ids))
print(' ', ids)

ups = os.path.join(UP, 'meowmel/pollution/common/warpevent/events')
up_files = sorted(f[:-5] for f in os.listdir(ups))
print('UPSTREAM warp event classes:', len(up_files))

up_ev = io.open(os.path.join(UP, 'meowmel/pollution/common/warpevent/WarpEvents.java'),
                encoding='utf-8', errors='ignore').read()
enum_ids = re.findall(r'^\s{4}([A-Z_]+)\s*[,(]', up_ev, re.M)
print('UPSTREAM WarpEvents enum:', len(enum_ids))
print(' ', enum_ids)

print('\n--- magic sweep wiring in port ---')
for d, _, fs in os.walk(PORT):
    for f in fs:
        if not f.endswith('.java'):
            continue
        fp = os.path.join(d, f)
        src = io.open(fp, encoding='utf-8', errors='ignore').read()
        for i, l in enumerate(src.split('\n'), 1):
            if re.search(r'magic_sweep|MAGIC_SWEEP|allowFlying|mayfly|SweepEvent', l):
                print('  %s:%d %s' % (fp.replace('\\', '/'), i, l.strip()[:120]))

print('\n--- upstream sweep logic ---')
up_sweep = os.path.join(UP, 'meowmel/pollution/common/SweepEventLoader.java')
if os.path.exists(up_sweep):
    us = io.open(up_sweep, encoding='utf-8', errors='ignore').read()
    print('  SweepEventLoader lines:', us.count('\n') + 1)
    print('  key markers:', [k for k in ('allowFlying', 'setCanceled', 'BaublesApi', 'grantFlyAbility')
                              if k in us])