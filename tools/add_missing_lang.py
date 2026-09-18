#!/usr/bin/env python3
"""Adds the missing en_us keys (34) and the new coil block names to the lang
provider, and fills the zh_cn entries from the upstream wire_coil keys."""

from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
POLLUTION = ROOT / "src/main/java/meowmel/pollution/Pollution.java"
UP_LANG = Path(r"H:\MinecraftMods\Pollution\src\main\resources\assets\pollution\lang\zh_cn.lang")
ZH = ROOT / "src/main/resources/assets/pollution/lang/zh_cn.json"

EN = {
    "pollution.machine.bot_gas_collector_beamLevel": "Beam level: %s",
    "pollution.machine.bot_gas_collector_essenceConsumptionSpeed": "Essence consumption: %s mB/t",
    "pollution.machine.bot_gas_collector_finalCollectionSpeed": "Collection rate: %s mB",
    "pollution.machine.bot_gas_collector_manaConsumptionSpeed": "Mana consumption: %s",
    "pollution.machine.endoflame_array.display.flowers": "Flowers: %s",
    "pollution.machine.endoflame_array.display.fuel_cache": "Fuel cache: %s / %s tick",
    "pollution.machine.endoflame_array.display.fuel_items": "Remaining fuel items: %s",
    "pollution.machine.endoflame_array.display.mana_pool": "Mana pool: %s / %s Mana",
    "pollution.machine.endoflame_array.display.output": "Actual mana output: %s Mana/t",
    "pollution.machine.mana_generator.tooltip": "Generates EU from a nearby Botania mana pool",
    "pollution.machine.mana_hatch.tooltip": "Mana hatch: buffers mana for magic multiblocks",
    "pollution.machine.mana_plate.speed": "Speed: %s | Mana: %s",
    "pollution.machine.mana_plate.tier": "Tier: %s | Mana: %s / %s",
    "pollution.machine.mana_pool_hatch.capacity": "Mana capacity: %s",
    "pollution.machine.mana_pool_hatch.tooltip": "Mana pool hatch: buffers Botania mana pools",
    "pollution.machine.mana_pool_hatch.transfer": "Transfer: %s Mana/t",
    "pollution.machine.mana_pool_hatch.type": "Mana pool type: %s",
    "pollution.machine.mana_pool_hatch.type.diluted": "Diluted",
    "pollution.machine.mana_pool_hatch.type.mythic": "Mythic",
    "pollution.machine.mana_pool_hatch.type.normal": "Normal",
    "pollution.machine.mana_pool_input_hatch.tooltip": "Mana pool input hatch",
    "pollution.machine.mana_pool_output_hatch.tooltip": "Mana pool output hatch",
    "pollution.machine.mega_mana_turbine.catalyst": "Catalyst tier: %s",
    "pollution.machine.mega_mana_turbine.max_output": "Max output: %s",
    "pollution.machine.mega_mana_turbine.parallel": "Parallels: %s | Coil tier: %s",
    "pollution.machine.pollution_multi_dan_de_life_on.buffer": "Garden EU buffer: %s",
    "pollution.machine.pollution_multi_dan_de_life_on.energy": "Garden energy: %s / %s",
    "pollution.machine.pollution_multi_dan_de_life_on.mode": "Garden mode: %s",
    "pollution.machine.pollution_multi_dan_de_life_on.mode0": "Accelerated growth",
    "pollution.machine.pollution_multi_dan_de_life_on.mode1": "Slowed growth",
    "pollution.machine.wireless_mana_hatch.tooltip": "Wireless mana hatch",
    "pollution.machine.wireless_mana_pool_hatch.tooltip": "Wireless mana pool hatch",
    "pollution.magic.failure.catalyst": "Missing required magic catalyst",
    "pollution.modeChanged.message": "Machine mode switched",
}

COILS = ["cupronickel", "kanthal", "nichrome", "rtm_alloy", "hssg", "naquadah",
         "trinium", "tritanium"]

for coil in COILS:
    EN[f"block.pollution.wire_coil_{coil}"] = f"Wire Coil: {coil.replace('_', ' ').title()}"


def main() -> int:
    source = POLLUTION.read_text(encoding="utf-8")
    anchor = '            provider.add("pollution.crystal_quality.grade", "Lens quality: grade %s (%s%%)");\n'
    if anchor not in source:
        print("anchor not found")
        return 1
    lines = "".join(f'            provider.add("{key}", "{value}");\n' for key, value in EN.items())
    source = source.replace(anchor, anchor + lines)
    POLLUTION.write_text(source, encoding="utf-8")

    upstream = {}
    for line in UP_LANG.read_text(encoding="utf-8", errors="ignore").splitlines():
        if line.startswith("tile.wire_coil."):
            key, _, value = line.strip().partition("=")
            upstream[key[len("tile.wire_coil."):-len(".name")]] = value

    zh = json.loads(ZH.read_text(encoding="utf-8")) if ZH.exists() else {}
    added = 0
    for coil in COILS:
        key = f"block.pollution.wire_coil_{coil}"
        value = upstream.get(f"machine_coil_{coil}")
        if value and key not in zh:
            zh[key] = value
            added += 1
    ZH.write_text(json.dumps(dict(sorted(zh.items())), indent=2, ensure_ascii=False) + "\n",
                  encoding="utf-8")
    print(f"en keys added: {len(EN)}, zh coil keys added: {added}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
