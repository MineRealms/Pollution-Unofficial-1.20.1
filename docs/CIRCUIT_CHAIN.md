# 魔法电路链（Magic Circuit Chain）

本文档记录 `pollution:magic_circuit.*`（15 级蕴魔电路）与
`pollution:magic_circuit_board.*`（15 级魔法电路板）的完整合成链，以及随本次
工作补齐的多方块结构专属方块配方。

- 代码入口：`PollutionRecipes.init`
  - `MagicCircuitRecipes.init` — 电路/电路板链
  - `StructureBlockRecipes.init` — 结构方块
  - `InfusionRecipes.beamCores` — 阵法核心注魔（本次增强）
  - `MagicChemicalRecipes.beamCore` — 阵法核心化学法快捷路线（本次按等级放大）
- 配方类型：
  - `PORecipeMaps.MAGIC_ASSEMBLER_RECIPES`（魔导组装机，预算 9 物品 + 3 流体）
  - TC4R 注魔（`InfusionRecipes.infusion`，最多 8 个基座）
- 原则：
  1. 越晚期的电路/电路板，材料越晚期、步骤越多、要素与不稳定度越高；
  2. 不新增注册物品、不新增 lang key、不改动机器类；
  3. 所有物品经 `SafeItems` 解析，缺失时只跳过受影响配方并 `LOGGER.warn`；
  4. 不引用跳过表内容（Astral Sorcery / Blood Magic / 血肉系物品）。

## 一、魔法电路板链

ULV..IV 沿用移植版既有配方，本次不重复注册：

| 等级 | 配方类型 | 关键材料（既有配方） |
| --- | --- | --- |
| ULV | GT 组装机 | 纸 + 铜箔 + Salisundus 粉 + 胶水 |
| LV | 魔导组装机 | ULV 板 + BasicSubstrate + 魔力钢锭 + Salisundus 粉 + InfusedAura |
| MV | GT 组装机 | LV 板 + 无菌石板坯 + 银细线 + 电阻 + InfusedLife |
| HV | GT 组装机 | MV 板 + 赛特斯石英 + 镀银玻璃透镜 + 魔力谐振线圈 + 谐振器 + InfusedAura |
| EV | TC4R 注魔 | 中央 HV 板 + 精密符文坯/自然灌注线圈/SMD |
| IV | TC4R 注魔 | 中央 EV 板 + 白/黑/天璇符文 + 节点稳定框架 + 高级 SMD |

本次新增（`pollution:infusion/magic_circuit_board_*`），`rank = 等级 - ZPM`：

| 等级 | 中央 | 关键基座材料 | 要素 prae/mach/fab/aura/ordo（+思维/异界） | 不稳定度 |
| --- | --- | --- | --- | --- |
| LuV（替代线） | IV 板 | NaquadahAlloy 箔×8、Salisundus×8、高级 SMD 电容×4、高级 SMD 晶体管×4、魔力钻石×2、魔力珍珠×2、盖亚锭、春之符文×2 | 64 / 64 / 48 / 32 / 32 | 6 |
| ZPM | LuV 板 | 钛箔×8、钛板×4、Salisundus×8、高级 SMD 电容×8、魔力珍珠×2、夏之符文×2、神秘锭×2 | 64 / 48 / 64 / 32 / 32 | 5 |
| UV | ZPM 板 | 中子箔×8、中子板×4、Salisundus×16、高级 SMD 晶体管×8、盖亚锭、秋之符文×2、神秘锭×3 | 128 / 96 / 128 / 64 / 48 | 7 |
| UHV | UV 板 | 超能硅岩箔×8、超能硅岩板×4、Salisundus×24、高级 SMD 二极管×8、原始珍珠、素牡符文、冬之符文×2、神秘锭×4 | 192 / 144 / 192 / 96 / 64（+思维 64） | 9 |
| UEV | UHV 板 | 杜兰箔×8、杜兰板×4、Salisundus×32、高级 SMD 电感×8、异域物体×2、玄牝符文、春之符文×2、虚空锭×2 | 256 / 192 / 256 / 128 / 80（+思维 80 / 异界 64） | 11 |
| UIV | UEV 板 | 三钛箔×12、三钛板×6、Salisundus×40、高级 SMD 电阻×8、原始珍珠×2、玄牝符文、夏之符文×2、虚空锭×3 | 320 / 240 / 320 / 160 / 96（+思维 96 / 异界 80） | 13 |
| UXV | UIV 板 | 中子箔×12、中子板×6、Salisundus×48、高级 SMD 电容×8、异域物体×4、天璇符文×2、秋之符文×2、无尽粉尘×1 | 384 / 288 / 384 / 192 / 112（+思维 112 / 异界 96） | 15 |
| OpV | UXV 板 | 中子箔×12、中子板×6、Salisundus×56、高级 SMD 晶体管×8、原始珍珠×4、天璇符文×2、冬之符文×2、无尽粉尘×2 | 448 / 336 / 448 / 224 / 128（+思维 128 / 异界 112） | 17 |
| MAX | OpV 板 | 中子箔×12、中子板×6、Salisundus×64、高级 SMD 二极管×8、异域物体×8、天璇符文×2、春之符文×2、无尽粉尘×4 | 512 / 384 / 512 / 256 / 144（+思维 144 / 异界 128） | 19 |

> LuV 替代线的意义：既有 LuV 板配方（`MagicIntegrationRecipes.magicCircuitBoardLuv`）
> 依赖星辉透镜、血肉电路与活体生物膜，均属跳过内容且无获取途径。替代注魔只用
> 已注册且可获取的材料，使 ZPM 以上的整条链自洽；既有配方保留不删。

## 二、蕴魔电路链

### ULV..LuV（魔导组装机，`pollution:circuit/magic_circuit_*`）

每条配方 = 上一级电路 + 对应电路板 + 等级金属 + 魔法材料 + 1..3 种灌注流体。

| 等级 | 上一级 | 电路板 | 等级金属 | 魔法材料 | 流体 | 耗时/EUt |
| --- | --- | --- | --- | --- | --- | --- |
| ULV | —（真空管×2 起步） | ULV 板 | 锡箔×4 | Salisundus×2、魔力粉×4 | 胶水 100 | 100 / 8 |
| LV | ULV 电路 | LV 板 | 铜箔×4 | 魔力钢锭、Salisundus×2、魔力粉×4、魔力符文 | InfusedAura 100、胶水 100 | 120 / 30 |
| MV | LV 电路 | MV 板 | 银箔×4 | 魔力钢锭×2、Salisundus×2、魔力钻石、风之符文 | InfusedAura 200、InfusedLife 100 | 160 / 120 |
| HV | MV 电路 | HV 板 | 金箔×4 | 精灵锭×2、赛特斯石英×2、魔力珍珠、地之符文 | InfusedAura 300、InfusedMagic 100、InfusedLight 100 | 200 / 480 |
| EV | HV 电路 | EV 板 | 铝箔×8 | 泰拉钢锭×2、高级 SMD 电容×2、高级 SMD 晶体管×2、精灵尘×4、水之符文 | InfusedAura 500、InfusedMagic 200、InfusedLight 200 | 240 / 1920 |
| IV | EV 电路 | IV 板 | 钨钢箔×8 | Salisundus×4、高级 SMD 二极管×2、高级 SMD 电感×2、盖亚锭、泰拉钢锭×2、火之符文 | InfusedAura 1000、InfusedOrder 500、InfusedMagic 500 | 300 / 7680 |
| LuV | IV 电路 | LuV 板 | 硅岩合金箔×8 | 虚空锭×2、高级 SMD 电阻×4、高级 SMD 电容×4、盖亚锭×2、龙石×2、春之符文 | InfusedAura 2000、InfusedLife 1000、InfusedLight 1000 | 400 / 30720 |

### ZPM..MAX（TC4R 注魔，`pollution:infusion/magic_circuit_*`）

中央物品为上一级电路；基座含对应电路板、等级板/箔、高级 SMD、Botania 符文、
Thaumcraft 珍珠/异域物体，UHV 起加入 Pollution 白/黑/天璇符文，UXV 起加入无尽粉尘。

| 等级 | 中央 | 关键基座材料 | 要素 prae/mach/fab/aura/ordo（+思维/异界） | 不稳定度 |
| --- | --- | --- | --- | --- |
| ZPM | LuV 电路 | ZPM 板、钛板×4、钛箔×8、高级 SMD 电容×4、夏之符文×2、魔力珍珠×2、神秘锭×2 | 128 / 64 / 64 / 64 / 32 | 6 |
| UV | ZPM 电路 | UV 板、中子板×4、中子箔×8、高级 SMD 晶体管×4、秋之符文×2、盖亚锭、神秘锭×3 | 224 / 128 / 128 / 112 / 64 | 8 |
| UHV | UV 电路 | UHV 板、超能硅岩板×4、超能硅岩箔×8、高级 SMD 二极管×4、冬之符文×2、原始珍珠、素牡符文、神秘锭×4 | 320 / 192 / 192 / 160 / 96（+思维 96） | 10 |
| UEV | UHV 电路 | UEV 板、杜兰板×4、杜兰箔×8、高级 SMD 电感×4、春之符文×2、异域物体×2、玄牝符文 | 416 / 256 / 256 / 208 / 128（+思维 128 / 异界 128） | 12 |
| UIV | UEV 电路 | UIV 板、三钛板×6、三钛箔×12、高级 SMD 电阻×8、夏之符文×2、原始珍珠×2、玄牝符文 | 512 / 320 / 320 / 256 / 160（+思维 160 / 异界 160） | 14 |
| UXV | UIV 电路 | UXV 板、中子板×6、中子箔×12、高级 SMD 电容×8、秋之符文×2、异域物体×4、天璇符文×2、无尽粉尘×1 | 608 / 384 / 384 / 304 / 192（+思维 192 / 异界 192） | 16 |
| OpV | UXV 电路 | OpV 板、中子板×6、中子箔×12、高级 SMD 晶体管×8、冬之符文×2、原始珍珠×4、天璇符文×2、无尽粉尘×2 | 704 / 448 / 448 / 352 / 224（+思维 224 / 异界 224） | 18 |
| MAX | OpV 电路 | MAX 板、中子板×6、中子箔×12、高级 SMD 二极管×8、春之符文×2、异域物体×8、天璇符文×2、无尽粉尘×4 | 800 / 512 / 512 / 400 / 256（+思维 256 / 异界 256） | 20 |

> 说明：基座数量固定在上限 8 件；等级越高，投入的 SMD 数量（4→8）、
> Salisundus 数量（8→64）、无尽粉尘（1→4）和要素总量同步增长，
> 高等级材料（神秘锭/虚空锭）在更高 rank 被珍珠/异域物体与无尽粉尘取代。

## 三、材料阶梯

| 阶段 | GT 金属（板/箔） | 魔法材料 |
| --- | --- | --- |
| ULV..LV | 锡 → 铜 | Salisundus、魔力粉、魔力符文 |
| MV..HV | 银 → 金 | 魔力钢/精灵锭、魔力钻石/珍珠、元素符文 |
| EV..IV | 铝 → 钨钢 | 泰拉钢、盖亚锭、精灵尘、高级 SMD |
| LuV | 硅岩合金 | 盖亚锭、龙石、虚空锭 |
| ZPM..UIV | 钛 → 中子 → 超能硅岩 → 杜兰 → 三钛 | 原始珍珠、异域物体、白/黑符文 |
| UXV..MAX | 中子为主 | 天璇符文、无尽粉尘、原始珍珠/异域物体成组投入 |

## 四、结构方块（`StructureBlockRecipes`）

| 方块 | 配方类型 | 关键材料 | 说明 |
| --- | --- | --- | --- |
| `pollution:polytetrafluoroethylene_pipe` | 魔导组装机（EV） | 聚四氟乙烯板×6、HSSG 板×2、GT 聚四氟乙烯管道外壳、Salisundus×2、InfusedAura 500 | 输出×3，仿 `MagicGCYMRecipes.pipe` 约定；此前仅作为配方原料 |
| `pollution:mineral_extractor` | 魔导组装机（EV） | EV 机壳、HV 电路、MV 电路板、EV 传感器×2、EV 发射器×2、HSSG 框架×4、Valonite×2、Salisundus×4、魔力谐振线圈×2、InfusedEarth 2000、InfusedAura 1000、润滑油 1000 | 9 物品 + 3 流体，矿物提取器本体 |
| `pollution:eldritch_eye` | TC4R 注魔 | 中央异域物体、虚空之种、碎片×4、末影之眼×2、末影珍珠×4、水银×2、虚空锭、通量粘液×2、炼金煤×2；alienis 64 / praecantatio 32 / tenebrae 32 / vacuos 16 | 深渊之眼，Thaumcraft 风味 |

### 阵法核心（beam core 0..4）

`InfusionRecipes.beamCores` 由“五个核心共用一套 4 件基座”改为分级异化：

| 核心 | 中央 | 板/框架 | 额外基座 | 要素 | 不稳定度 |
| --- | --- | --- | --- | --- | --- |
| 0 强磁 | 磁化钢块 | HSSG×4 / HSSG | 磁化铁杆×4、魔力粉×4、风之符文 | machina 16 / praecantatio 8 / metallum 16 | 4 |
| 1 内爆 | TNT | HSSG×4 / HSSG | 炼金煤×2、火之符文、烈焰粉×4 | machina 28 / praecantatio 16 / perditio 28 | 5 |
| 2 导压 | 灵魂沙 | HSSG×6 / HSSG | 水银×2、水之符文、水水晶簇 | machina 40 / praecantatio 24 / sensus 40 / auram 24 | 6 |
| 3 束流 | 水水晶簇 | 钨钢×4 / 钨钢 | 琥珀×2、地之符文、要素瓶 | machina 52 / praecantatio 32 / aqua 52 / auram 32 | 8 |
| 4 聚能 | 充能中继 | 钨钢×6 / 钨钢 | 魔力谐振线圈×2、魔力符文×2、原始珍珠、充能中继 | machina 64 / praecantatio 40 / potentia 64 / auram 40 | 10 |

`MagicChemicalRecipes.beamCore`（化学法快捷线）同步放大：核心 0 用 1 份
NaquadahAlloy 框架 + 576 mB InfusedEnergy，核心 4 用 5 份框架 + 2880 mB，
耗时 1000→5000 tick、EUt 480→2400，避免五核仅靠电路编号区分。

## 五、跳过与未覆盖

- **跳过**：`flesh_*`、`heart_fruit`、`tentacle`（血肉系，任务跳过表）、
  其余植物方块（`alfheim_*`、`rainbow_*` 为世界生成/生长获取，无配方）、
  `pollution:portal`（无物品形态）、全部 Astral Sorcery / Blood Magic 物品。
- **保留但未删除**：既有 LuV 板配方（依赖跳过内容）仍存在于
  `MagicIntegrationRecipes`，新替代线并行提供可获取路径。
- **无新增注册物品**：全部配方只使用已注册的 GT / GTNN / Botania / TC4R /
  Pollution 物品。

## 六、ID 约定

| 内容 | 命名空间/路径 |
| --- | --- |
| 组装机电路 | `pollution:circuit/magic_circuit_<tier>` |
| 注魔电路 | `pollution:infusion/magic_circuit_<tier>` |
| 注魔电路板 | `pollution:infusion/magic_circuit_board_<tier>`（LuV 替代线 `..._luv_alternative`） |
| 结构方块 | `pollution:structure/<name>` / `pollution:infusion/eldritch_eye` |

`<tier>` 为 GT 电压小写名：`ulv, lv, mv, hv, ev, iv, luv, zpm, uv, uhv, uev, uiv, uxv, opv, max`。
