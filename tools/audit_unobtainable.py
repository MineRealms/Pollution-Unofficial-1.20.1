#!/usr/bin/env python3
"""Audit pollution items/blocks that no recipe produces.

Reads the runtime recipe dump written by the (temporary) server-side
``meowmel.pollution.debug.RecipeDump`` listener.  The dump is produced from the
live ``RecipeManager`` after the server has fully started, so it covers every
recipe type that ended up loaded at runtime: vanilla crafting/smelting,
GregTech dynamic-data-pack recipes (all GT recipe maps), Thaumcraft 4R infusion,
Botania native recipes and anything else registered by other mods.

Usage:
    python tools/audit_unobtainable.py                # print summary + write doc
    python tools/audit_unobtainable.py --no-doc       # print summary only
    python tools/audit_unobtainable.py --dump PATH --doc PATH

How the dump is produced (kept here for reproducibility):
    1. add ``src/main/java/meowmel/pollution/debug/RecipeDump.java`` and boot
       the dev server (``gradlew runServer``);
    2. the listener writes ``run/pollution-recipe-dump.txt`` on ServerStartedEvent;
    3. run this tool, then delete the debug class again.
"""

from __future__ import annotations

import argparse
import json
import zipfile
from collections import Counter, defaultdict
from datetime import date
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DEFAULT_DUMP = ROOT / "run" / "pollution-recipe-dump.txt"
DEFAULT_DOC = ROOT / "docs" / "UNOBTAINABLE_ITEMS.md"
EN_LANG = ROOT / "src" / "generated" / "resources" / "assets" / "pollution" / "lang" / "en_us.json"
ZH_LANG = ROOT / "src" / "main" / "resources" / "assets" / "pollution" / "lang" / "zh_cn.json"
GRADLE_GT = Path.home() / ".gradle" / "caches" / "modules-2" / "files-2.1" / "com.gregtechceu.gtceu"
GRADLE_PROPERTIES = ROOT / "gradle.properties"

DEBUG_ITEMS = {"pollution:test", "pollution:test_item"}


def load_json(path: Path) -> dict[str, str]:
    return json.loads(path.read_text(encoding="utf-8"))


def pinned_gt_version() -> str | None:
    if not GRADLE_PROPERTIES.exists():
        return None
    for line in GRADLE_PROPERTIES.read_text(encoding="utf-8", errors="ignore").splitlines():
        if line.strip().startswith("gtceu_version="):
            return line.split("=", 1)[1].strip()
    return None


def find_gt_lang() -> tuple[dict[str, str], dict[str, str], str | None]:
    """Locate a gtceu jar in the Gradle cache and read its lang files.

    Prefers the version pinned in ``gradle.properties`` so names match the
    runtime dependency even when newer jars are cached.
    """
    if not GRADLE_GT.exists():
        return {}, {}, None
    jars = sorted(GRADLE_GT.rglob("gtceu-1.20.1-*.jar"))
    pinned = pinned_gt_version()
    if pinned:
        exact = [jar for jar in jars if jar.name == f"gtceu-1.20.1-{pinned}.jar"]
        if exact:
            jars = exact
        else:
            jars = list(reversed(jars))
    else:
        jars = list(reversed(jars))
    for jar in jars:
        try:
            with zipfile.ZipFile(jar) as archive:
                en = json.loads(archive.read("assets/gtceu/lang/en_us.json"))
                zh = json.loads(archive.read("assets/gtceu/lang/zh_cn.json"))
            return en, zh, str(jar)
        except (KeyError, zipfile.BadZipFile, json.JSONDecodeError):
            continue
    return {}, {}, None


def parse_dump(path: Path) -> tuple[dict[str, tuple[str, str]], dict[str, tuple[str, str]],
                                    set[str], set[str], list[str]]:
    items: dict[str, tuple[str, str]] = {}
    blocks: dict[str, tuple[str, str]] = {}
    outputs: set[str] = set()
    inputs: set[str] = set()
    errors: list[str] = []
    section = None
    for line in path.read_text(encoding="utf-8", errors="ignore").splitlines():
        if line.startswith("## "):
            section = line[3:].strip()
            continue
        if not line:
            continue
        if line.startswith("## ERROR"):
            errors.append(line[3:].strip())
            continue
        if section == "ITEMS":
            item_id, _, rest = line.partition("|")
            desc, _, cls = rest.partition("|")
            items[item_id] = (desc, cls or "?")
        elif section == "BLOCKS":
            block_id, _, rest = line.partition("|")
            desc, _, cls = rest.partition("|")
            blocks[block_id] = (desc, cls or "?")
        elif section == "RECIPE_OUTPUTS":
            outputs.add(line.strip())
        elif section == "RECIPE_INPUTS":
            inputs.add(line.strip())
    return items, blocks, outputs, inputs, errors


def category_of(item_id: str, cls: str) -> str:
    if cls == "MetaMachineItem":
        return "machines"
    if cls == "GTBucketItem":
        return "buckets"
    if cls.endswith("BlockItem"):
        return "blocks"
    return "items"


class NameResolver:
    """Resolve EN/ZH display names for pollution registry ids."""

    def __init__(self, en: dict[str, str], zh: dict[str, str],
                 gt_en: dict[str, str], gt_zh: dict[str, str]) -> None:
        self.en = en
        self.zh = zh
        self.gt_en = gt_en
        self.gt_zh = gt_zh
        self.materials = sorted(
            (key[len("material.pollution."):] for key in en if key.startswith("material.pollution.")),
            key=len, reverse=True,
        )

    def material_of(self, path: str) -> str | None:
        for material in self.materials:
            if path == material or path.startswith(material + "_"):
                return material
        return None

    def _lang_pair(self, path: str) -> tuple[str | None, str | None]:
        en = self.en.get(f"item.pollution.{path}") or self.en.get(f"block.pollution.{path}")
        zh = self.zh.get(f"item.pollution.{path}") or self.zh.get(f"block.pollution.{path}")
        return en, zh

    def resolve(self, item_id: str, desc: str) -> tuple[str, str, str | None]:
        """Return (english, chinese, note)."""
        path = item_id.split(":", 1)[1]
        en, zh = self._lang_pair(path)
        note = None

        if desc.startswith("tagprefix."):
            material = self.material_of(path)
            if material:
                mat_en = self.en.get(f"material.pollution.{material}", material)
                mat_zh = self.zh.get(f"material.pollution.{material}", material)
                fmt_en = self.gt_en.get(desc)
                fmt_zh = self.gt_zh.get(desc)
                if fmt_en and not en:
                    en = fmt_en.replace("%s", mat_en)
                if fmt_zh and not zh:
                    zh = fmt_zh.replace("%s", mat_zh)
            if path.endswith("_ore"):
                note = "worldgen_ore"
        elif desc == "item.gtceu.bucket":
            material = self.material_of(path)
            if material:
                fluid_en = self.en.get(f"fluid.pollution.{material}") or \
                    self.en.get(f"material.pollution.{material}", material)
                fluid_zh = self.zh.get(f"fluid.pollution.{material}") or \
                    self.zh.get(f"material.pollution.{material}", material)
                fmt_en = self.gt_en.get("item.gtceu.bucket")
                fmt_zh = self.gt_zh.get("item.gtceu.bucket")
                if fmt_en and not en:
                    en = fmt_en.replace("%s", fluid_en)
                if fmt_zh and not zh:
                    zh = fmt_zh.replace("%s", fluid_zh)

        return en or path, zh or path, note


def build_rows(items: dict[str, tuple[str, str]], outputs: set[str], inputs: set[str],
               resolver: NameResolver) -> list[dict]:
    rows = []
    for item_id, (desc, cls) in items.items():
        if item_id in outputs:
            continue
        en, zh, tag_note = resolver.resolve(item_id, desc)
        notes = []
        is_ore = item_id.endswith("_ore")
        if tag_note == "worldgen_ore":
            notes.append("世界生成矿石（可挖掘获得）")
        if item_id in DEBUG_ITEMS:
            notes.append("调试/占位物品")
        if cls == "GTBucketItem":
            notes.append("GT 流体桶（无配方产出）")
        if item_id in inputs and not is_ore:
            notes.append("仅作为配方原料")
        elif not is_ore and cls != "GTBucketItem":
            notes.append("无任何配方引用")
        rows.append({
            "id": item_id,
            "en": en,
            "zh": zh,
            "cls": cls,
            "desc": desc,
            "category": category_of(item_id, cls),
            "notes": "；".join(notes),
        })
    rows.sort(key=lambda row: row["id"])
    return rows


def group_rows(rows: list[dict]) -> dict[str, list[dict]]:
    grouped: dict[str, list[dict]] = defaultdict(list)
    for row in rows:
        grouped[row["category"]].append(row)
    return grouped


def render_table(rows: list[dict]) -> list[str]:
    lines = ["| 注册名 (ID) | 英文名 | 中文名 | 备注 |", "| --- | --- | --- | --- |"]
    for row in rows:
        lines.append(f"| `{row['id']}` | {row['en']} | {row['zh']} | {row['notes']} |")
    return lines


def write_doc(path: Path, items: dict, blocks: dict, rows: list[dict], errors: list[str],
              gt_jar: str | None, dump_path: Path) -> None:
    grouped = group_rows(rows)
    machines = grouped.get("machines", [])
    block_rows = grouped.get("blocks", [])
    item_rows = grouped.get("items", [])
    bucket_rows = grouped.get("buckets", [])
    other_rows = grouped.get("other", [])

    ores = [row for row in block_rows if row["id"].endswith("_ore")]
    plain_blocks = [row for row in block_rows if not row["id"].endswith("_ore")]
    itemless_blocks = [bid for bid in blocks if bid not in items]

    total_items = len(items)
    total_missing = len(rows)

    lines: list[str] = [
        "# Pollution 无配方产出物品/方块审计",
        "",
        f"- 审计日期：{date.today():%Y-%m-%d}",
        f"- 数据来源：`{dump_path.relative_to(ROOT).as_posix()}`"
        "（服务器启动后从运行时 `RecipeManager` 全量转储）",
        "- 审计脚本：`tools/audit_unobtainable.py`",
        f"- 物品注册总数（`pollution` 命名空间）：**{total_items}**",
        f"- 无任何配方产出的物品数：**{total_missing}**",
        f"- 其中世界生成矿石：**{len(ores)}**（可挖掘获得，仅无配方）",
        f"- 其余（无配方且非世界生成矿石）：**{total_missing - len(ores)}**",
        "",
        "## 分类统计",
        "",
        "| 分类 | 数量 | 说明 |",
        "| --- | ---: | --- |",
        f"| 机器/多方块 | {len(machines)} | `MetaMachineItem`，含单方块机器、多方块控制器、仓室 |",
        f"| 方块 | {len(block_rows)} | 其中世界生成矿石 {len(ores)}、结构/装饰/植物方块 {len(plain_blocks)} |",
        f"| 物品 | {len(item_rows)} | 普通物品、材料部件、电路、饰品等 |",
        f"| 桶/流体容器 | {len(bucket_rows)} | GT 流体桶（`GTBucketItem`） |",
        f"| 其他 | {len(other_rows)} | 未归入以上分类 |",
        f"| 无对应物品的方块 | {len(itemless_blocks)} | 注册了方块但无物品形态 |",
        "",
        "## 说明与注意事项（Caveats）",
        "",
        "1. **本表只统计“有配方产出”**：世界生成（矿石、植物）、生物掉落、结构箱子、"
        "任务奖励、JEI 隐藏物品等获取途径不计入配方。",
        f"2. **世界生成矿石 {len(ores)} 项**：这些是 GT 材料矿块，靠挖矿获得，"
        "没有（也不需要）配方，属于正常现象。",
        "3. **桶/流体容器**：`GTBucketItem` 没有配方产出；GT 的桶可右键流体源拾取，"
        "但 Pollution 的 GT 流体没有可放置的源方块，因此实际上只能创造模式获得。",
        "4. **标签输出已展开**：GT 配方输出以 `Ingredient` 存储，转储时通过 "
        "`Ingredient.getItems()` 展开为具体物品，所以“标签产出的物品”已计入可制造。",
        "5. **KubeJS 运行时改动已包含**：转储读取的是 `RecipeManager` 的最终状态。",
        f"6. 转储期间 `getResultItem`/输入展开异常：**{len(errors)}** 个"
        + ("。" if not errors else "：" + "; ".join(errors)),
        "7. **机器/仓室缺失多为移植未完成**：例如 Aspect Tank、Flux Muffler、"
        "Infused Fluid Hatch 全等级无配方；Mana 输入仓只有 1A 等级有配方，"
        "4A/16A/64A 与输出仓、无线仓全部缺失；部分多方块控制器（Magic Greenhouse、"
        "Magic Mega Turbine、Bot Distillery、Mana Plate、Node Fusion Reactor 等）无配方。",
        "8. `pollution:test`、`pollution:test_item` 为调试/占位物品。",
        "9. 大量材料部件（dust/plate/ingot 等）的缺失需要人工复核上游配方链，"
        "本表只保证“运行时确实没有配方产出”。",
    ]
    if gt_jar:
        lines.append(f"10. 名称解析使用的 GT 语言文件：`{gt_jar}`。")
    lines.append("")

    lines += ["## 机器/多方块（" + str(len(machines)) + "）", ""]
    lines += render_table(machines)
    lines += ["", "## 方块（" + str(len(block_rows)) + "）", ""]
    lines += [f"### 世界生成矿石（{len(ores)}）", ""]
    lines += render_table(ores)
    lines += ["", f"### 结构/装饰/植物方块（{len(plain_blocks)}）", ""]
    lines += render_table(plain_blocks)
    if itemless_blocks:
        lines += ["", f"### 无物品形态的方块（{len(itemless_blocks)}）", ""]
        lines += ["| 注册名 (ID) | 备注 |", "| --- | --- |"]
        for block_id in sorted(itemless_blocks):
            lines.append(f"| `{block_id}` | 技术方块（无对应物品，无法以物品形式获得） |")
    lines += ["", "## 物品（" + str(len(item_rows)) + "）", ""]
    lines += render_table(item_rows)
    lines += ["", "## 桶/流体容器（" + str(len(bucket_rows)) + "）", ""]
    lines += render_table(bucket_rows)
    if other_rows:
        lines += ["", "## 其他（" + str(len(other_rows)) + "）", ""]
        lines += render_table(other_rows)
    lines.append("")

    path.write_text("\n".join(lines), encoding="utf-8")


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--dump", type=Path, default=DEFAULT_DUMP)
    parser.add_argument("--doc", type=Path, default=DEFAULT_DOC)
    parser.add_argument("--no-doc", action="store_true", help="do not write the markdown report")
    args = parser.parse_args()

    if not args.dump.exists():
        print(f"dump not found: {args.dump}")
        print("run the server once with the temporary RecipeDump listener enabled")
        return 1

    items, blocks, outputs, inputs, errors = parse_dump(args.dump)
    en = load_json(EN_LANG)
    zh = load_json(ZH_LANG)
    gt_en, gt_zh, gt_jar = find_gt_lang()
    if not gt_en:
        print("warning: gtceu lang jar not found, material names will fall back to ids")

    resolver = NameResolver(en, zh, gt_en, gt_zh)
    rows = build_rows(items, outputs, inputs, resolver)

    grouped = group_rows(rows)
    print(f"items registered: {len(items)}")
    print(f"items without recipe output: {len(rows)}")
    for category in ("machines", "blocks", "items", "buckets", "other"):
        print(f"  {category}: {len(grouped.get(category, []))}")
    print(f"  worldgen ores: {sum(1 for r in rows if r['id'].endswith('_ore'))}")
    print(f"  used only as ingredient: {sum(1 for r in rows if '仅作为配方原料' in r['notes'])}")
    print(f"  dump errors: {len(errors)}")

    if not args.no_doc:
        write_doc(args.doc, items, blocks, rows, errors, gt_jar, args.dump)
        print(f"doc written: {args.doc}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
