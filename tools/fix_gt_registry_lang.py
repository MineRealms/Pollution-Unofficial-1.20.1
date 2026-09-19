#!/usr/bin/env python3
"""Central lang fix for GT registries we fill ourselves.

GT's datagen only emits lang for GT's own recipe types / abilities, so every
entry we register through GTRecipeTypes.register(...) or new PartAbility(...)
needs a manual key. Also patches three dependency keys that are missing from
the pack (GTNN stone type + two GTNN material names).
"""

from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ZH = ROOT / "src/main/resources/assets/pollution/lang/zh_cn.json"
POLLUTION = ROOT / "src/main/java/meowmel/pollution/Pollution.java"

RECIPE_TYPES = {
    "magic_blast_smelter": ("Magic Alloy Blast Smelter", "魔导合金高炉"),
    "stove": ("Stove", "炉灶"),
    "magic_fusion_reactor": ("Magic Fusion Reactor", "魔导聚变反应堆"),
    "magic_assembler": ("Magic Assembler", "魔导组装机"),
    "magic_greenhouse": ("Magic Greenhouse", "魔导温室"),
    "magic_turbine": ("Magic Turbine", "魔导涡轮"),
    "forge_alchemy": ("Forge Alchemy", "神秘炼金"),
    "node_magic_fusion": ("Node Fusion", "节点聚变"),
    "mana_petal_recipes": ("Mana Petal Apothecary", "魔力花瓣炼制"),
    "mana_rune_altar_recipes": ("Mana Rune Altar", "魔力符文祭坛"),
    "pure_daisy_recipes": ("Pure Daisy", "白雏菊转化"),
    "mana_infusion_recipes": ("Mana Infusion", "魔力灌注"),
    "mana_gen_recipes": ("Mana Generation", "魔力发电"),
    "mana_to_eu": ("Mana to EU", "魔力转 EU"),
    "dan_de_life_on": ("Dan De Life On", "生命花园"),
}

ABILITIES = {
    "pollution_vis_hatch": ("Vis Hatch", "灵气仓"),
    "pollution_infused_fluid_hatch": ("Infused Fluid Hatch", "灌注流体仓"),
    "pollution_mana_input_hatch": ("Mana Input Hatch", "魔力输入仓"),
    "pollution_mana_output_hatch": ("Mana Output Hatch", "魔力输出仓"),
    "pollution_mana_input_pool": ("Mana Pool Input Hatch", "魔力池输入仓"),
    "pollution_mana_output_pool": ("Mana Pool Output Hatch", "魔力池输出仓"),
}

DEPENDENCY_PATCHES = {
    "tagprefix.proxima_centauri_b": ("Proxima Centauri B Stone", "比邻星B岩石"),
    "material.gtceu.manasteel": ("Manasteel", "魔力钢"),
    "material.gtceu.terrasteel": ("Terrasteel", "泰拉钢"),
}


def main() -> int:
    entries: dict[str, tuple[str, str]] = {}
    for name, (en, zh) in RECIPE_TYPES.items():
        entries[f"gtceu.{name}"] = (en, zh)
    for name, (en, zh) in ABILITIES.items():
        entries[f"gtceu.ability.{name}"] = (en, zh)
    for key, (en, zh) in DEPENDENCY_PATCHES.items():
        entries[key] = (en, zh)

    zh = json.loads(ZH.read_text(encoding="utf-8"))
    added_zh = 0
    for key, (_, value) in entries.items():
        if key not in zh:
            zh[key] = value
            added_zh += 1
    ZH.write_text(json.dumps(dict(sorted(zh.items())), indent=2, ensure_ascii=False) + "\n",
                  encoding="utf-8")

    source = POLLUTION.read_text(encoding="utf-8")
    anchor = '            provider.add("pollution.crystal_quality.grade", "Lens quality: grade %s (%s%%)");\n'
    if anchor not in source:
        print("anchor not found")
        return 1
    lines = "".join(f'            provider.add("{k}", "{v[0]}");\n' for k, v in entries.items())
    source = source.replace(anchor, anchor + lines)
    POLLUTION.write_text(source, encoding="utf-8")

    print(f"keys queued: {len(entries)}, zh added: {added_zh}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
