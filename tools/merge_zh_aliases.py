import json, re
from pathlib import Path
ROOT = Path(r"H:\MinecraftMods\Pollution-Unofficial-1.20.1")
UP = Path(r"H:\MinecraftMods\Pollution\src\main\resources\assets\pollution\lang\zh_cn.lang")
EN = ROOT / "src/generated/resources/assets/pollution/lang/en_us.json"
ZH = ROOT / "src/main/resources/assets/pollution/lang/zh_cn.json"
TIERS = ("ulv","lv","mv","hv","ev","iv","luv","zpm","uv","uhv","uev","uiv","uxv","opv","max")
en = json.loads(EN.read_text(encoding="utf-8"))
zh = json.loads(ZH.read_text(encoding="utf-8"))
up = {}
for line in UP.read_text(encoding="utf-8", errors="ignore").splitlines():
    if not line.startswith("pollution.machine."): continue
    key,_,value = line.strip().partition("=")
    if key.endswith(".name"): up[key[len("pollution.machine."):-len(".name")]] = value

added = 0
for key in list(en):
    if key in zh or not key.startswith("block.pollution."): continue
    ident = key[len("block.pollution."):]
    tier = next((t for t in TIERS if ident.startswith(t + "_")), None)
    body = ident[len(tier)+1:] if tier else ident
    if body.startswith("flux_muffler") and tier:
        value = up.get(f"pollution_muffler_hatch.{tier}")
        if value: zh[key] = value; added += 1; continue
    if body.startswith("solar_plate_"):
        kind = body[len("solar_plate_"):]
        for candidate in (f"solar_plate_{kind}.{kind}", f"solar_plate_{kind}.1", f"solar_plate_{kind}"):
            if candidate in up: zh[key] = up[candidate]; added += 1; break
ZH.write_text(json.dumps(dict(sorted(zh.items())), indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
missing = [k for k in en if k not in zh]
print(f"alias added: {added}, zh total: {len(zh)}, missing: {len(missing)}")
for k in missing[:20]: print("  " + k)
