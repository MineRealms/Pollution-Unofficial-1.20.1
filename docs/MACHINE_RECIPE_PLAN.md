# 机器/主方块配方补齐计划（MachineRecipes + HatchRecipes）

- 日期：2026-09-20（材料丰富化修订：材料更丰富/更有多样性）
- 对应实现：`src/main/java/meowmel/pollution/loaders/recipes/MachineRecipes.java`、`src/main/java/meowmel/pollution/loaders/recipes/HatchRecipes.java`（由 `PollutionRecipes.init` 调用）
- 依据：`docs/UNOBTAINABLE_ITEMS.md`（这些机器/主方块无任何配方产出）+ 上游 1.12.2 配方（`MachineRecipes`、`MagicGCYMRecipes`、`NodeFusionRecipes`）
- 不改动机器类、不新增 lang key；全部配方 null-safe（`SafeItems` + `isEmpty()` 守卫，缺件仅跳过并 `LOGGER.warn`）。
- 所有物品 id 均已对照本包实际注册项核实（Botania / Thaumcraft 4R / AE2 / GTCEu 7.5.3 / 本移植版注册表），不发明 id。

## 设计原则

| 阶段 | 判定 | 配方类型 | 风格 |
| --- | --- | --- | --- |
| 早期 | 等级 ≤ HV（LV/MV/HV） | TC4R 注魔（`InfusionRecipes.infusion`） | 中央物品为该等级 GT 机壳；**8 个基座全部填满**：Botania 符文/钻石/珍珠 + Thaumcraft 谐振器/神秘锭/虚空锭/火红莲/炼金煤/要素瓶 + GT 力场发生器/齿轮 + 元素符文；要素按等级放大（每系 4/7/10 + perditio 2/4/6 + praecantatio 4/8/15 + potentia 4/8/12 + auram 2/6/10），不稳定度 2/3/5 |
| 进阶 | 等级 ≥ EV | 魔导组装机（`PORecipeMaps.MAGIC_ASSEMBLER_RECIPES`） | GT 工业风：等级机壳/上一级机器 + 魔法电路 + **2-4 个等级组件**（力场发生器/发射器/传感器/泵/机械臂/转子）+ 该等级材料的框架/齿轮/板/螺栓 + Botania/Thaumcraft/AE2 物品 + 本移植版电路/符文/棱镜/外壳 + 注魔流体与 GT 流体；越大越复杂（组件种类、数量、GT 部件等级同步提高） |

> **输入预算**：TC4R 注魔每条配方固定 8 个基座组件（本批全部填满；上游 1.12 允许更多，但按任务约定封顶 8）。魔导组装机配方类型注册为 `setMaxIOSize(9, 1, 3, 0)`，即 **9 种物品 + 3 种流体 = 最多 12 种不同输入**；GT 超限只告警，但配方 UI/JEI 仅渲染声明槽位，故本批全部控制在 9+3 以内。

## 配方清单（共 18 条，材料丰富化后）

| 机器 | 等级 | 配方类型 | 主要材料（丰富化后） | 输入数（物品+流体，前→后） | 说明/理由 |
| --- | --- | --- | --- | --- | --- |
| 魔力发电机 `mana_generator` | LV | 注魔 | 中央 LV 机壳；基座：魔力符文、魔力珍珠、LV 力场发生器×2、钢齿轮×2、魔力钻石、谐振器、神秘锭、火红莲；要素 4 系 + 2 perditio + 4 praecantatio + 4 potentia + 2 auram | 7→**8** 基座 | 早期走注魔，Botania/TC 双线材料，廉价可及 |
| 魔力发电机 | MV | 注魔 | 中央 MV 机壳；基座：魔力符文、魔力珍珠、MV 力场发生器×2、铝齿轮×2、风之符文、谐振器、虚空锭、炼金煤；要素 7 + 4 + 8 + 8 potentia + 6 auram | 9→**8** 基座（重新配平） | 按 8 基座约定重排，要素更丰富 |
| 魔力发电机 | HV | 注魔 | 中央 HV 机壳；基座：魔力符文、魔力珍珠、HV 力场发生器×2、不锈钢齿轮×2、土之符文、泰拉钢、虚空锭、要素瓶；要素 10 + 6 + 15 + 12 potentia + 10 auram | 11→**8** 基座（重新配平） | 早期上限，要素最丰富 |
| 魔力发电机 | EV | 装配 | EV 机壳 + EV 魔法电路 + 魔力共振线圈 + 魔力符文×2 + EV 力场发生器×2 + **EV 发射器×2** + 泰拉钢×2 + **HSSG 齿轮×4** + **火红莲×4**；注魔灵气 2000/注魔魔力 1000/**焊锡 288** | 6+2→**9+3** | 进入 EV 后转 GT 工业风，追加第二个等级组件与 GT 流体 |
| 魔力发电机 | IV | 装配 | IV 机壳 + EV 发电机升级 + IV 魔法电路 + 共振线圈×2 + 魔力符文×4 + IV 力场发生器×4 + **IV 发射器×4** + 盖亚锭×2 + 精灵尘×4；灵气 4000/魔力 2000/光 1000 | 9+3→**9+3**（材料升级：发射器替换泰拉钢） | 升级链 + 更高等级部件 |
| 源质充能器 `source_charge` | LV | 注魔 | LV 机壳；基座：水之指环（充能对象）、魔力符文、魔力钻石、魔力珍珠、谐振器、神秘锭、LV 泵、LV 传感器；要素 4 系 + 2 perditio + 4 praecantatio + 水 8 + 交换 4 + 运动 4 + 能量 4 | 7→**8** 基座 | 单方块早期机器，注魔获取 |
| 微型星光节点反应堆 `small_node_generator` | LuV | 装配 | LuV 机壳 + 大型节点发电机 + LuV Vis 仓 + LuV 魔法电路 + LuV 发射器×2 + LuV 力场发生器×2 + **硅岩合金框架×4** + 封装灵气节点×2 + 魔力符文×2；注魔灵气 4000/光 2000/魔力 1000 | 8+2→**9+3** | 上游有序合成（机壳+大节点发电机+发射器+电路+Vis 仓+力场）的装配机化，追加框架与第三流体 |
| 微型星光节点反应堆 | ZPM | 装配 | ZPM 机壳 + 大型节点发电机 + ZPM Vis 仓 + ZPM 魔法电路 + ZPM 发射器×4 + ZPM 力场发生器×4 + **三钛框架×4** + 封装节点×4 + **春之符文×4**；灵气 6000/光 3000/暗 1000 | 8+3→**9+3** | 等级越高节点/框架/符文越多 |
| 微型星光节点反应堆 | UV | 装配 | UV 机壳 + 大型节点发电机 + UV Vis 仓 + UV 魔法电路 + UV 发射器×6 + UV 力场发生器×6 + **中子素框架×4** + 封装节点×8 + **夏之符文×8**；灵气 8000/光 4000/暗 2000 | 8+3→**9+3** | UV 最复杂（UHV 按任务跳过） |
| 节点聚变堆 `luv_node_fusion_reactor` | LuV | 装配 | 虚空棱镜×4 + 光束核心 I×8 + LuV 电路 + 星辉符文 + 封装节点×2 + LuV 力场×4 + 硅岩合金框架×4 + 超导线圈×4 + 念想核心×2；KQ金 8000/注魔光 8000/注魔暗 8000 | 9+3→**9+3** | 上游 NodeFusionRecipes 装配链（POHyper 外壳→虚空棱镜，Starrymansus→注魔灵气） |
| 节点聚变堆 `zpm_node_fusion_reactor` | ZPM | 装配 | LuV 反应堆升级 + 虚空棱镜×8 + 光束核心 I×16 + ZPM 电路 + 星辉符文×2 + 封装节点×4 + ZPM 力场×8 + **三钛框架×4** + 超导线圈×8；缚束金属/感知金属/注魔灵气各 8000 | 9+3→**9+3**（框架升级三钛，力场/线圈翻倍） | 升级链体现“越大越复杂” |
| 节点聚变堆 `uv_node_fusion_reactor` | UV | 装配 | ZPM 反应堆升级 + 虚空棱镜×12 + 光束核心 I×24 + UV 电路 + 星辉符文×3 + 封装节点×8 + UV 力场×12 + **中子素框架×4** + 超导线圈×12；既存之枢/消逝之枢 8000 + 灵气 16000 | 9+3→**9+3**（框架升级中子素，数量提升） | 终局控制器，材料与流体最多 |
| 特大源质要素轮机 `magic_mega_turbine` | IV | 装配 | 大型魔力轮机 + 炽热咒法棱镜×4 + IV 电路 + 星辉符文 + 分离催化核心 + 硅岩合金框架×8 + 钨钢齿轮×8 + **钨钢转子×4** + IV 力场×4；灵气 8000/注魔火 4000/润滑剂 4000 | 8+3→**9+3** | 上游 `large_magic_turbine` 注魔（EV）升一级为装配机版，追加涡轮转子 |
| 魔导温室 `magic_green_house` | EV | 装配 | **EV 机壳** + 魔导控制组件 + EV 电路 + 法罗钠晶体×4 + 水咒法棱镜×4 + 光束核心 3 + 火红莲×4 + **魔力珍珠×4** + EV 力场×4；注魔植物 4000/灵气 4000/土 2000 | 7+3→**9+3** | 上游注魔用 GTFO 温室（本包没有），改为机壳+控制组件+棱镜装配，追加 Botania 珍珠 |
| 泰拉蒸馏塔 `bot_distillery` | EV | 装配 | GT 蒸馏塔 + 泰拉水密外壳×8 + 夹层玻璃×8 + EV 电路 + 控制组件 + 泰拉钢×4 + 魔力符文×4 + **EV 泵×4** + EV 力场×2；注魔水 8000/灵气 4000/**润滑剂 1000** | 8+2→**9+3** | 对应其结构外壳（TERRA_WATERTIGHT_CASING + CAMINATED_GLASS），追加泵与 GT 流体 |
| 节点清洗机 `node_washer` | LuV | 装配 | 光束核心 2×2 + 虚空棱镜×4 + LuV 电路 + 星辉符文 + 封装节点×2 + 节点稳定框架 + **LuV 泵×4** + LuV 力场×4 + 魔力符文×4；注魔水 16000/灵气 8000/魔力 4000 | 9+3→**9+3**（泵替换天然注魔线圈） | 节点家族 LuV 定位；用水/灵气/魔力“洗词条”，泵增强循环 |
| 魔力基板加速器 `mana_plate` | IV | 装配 | 基础魔力基板×16 + IV 电路 + 魔力共振线圈×4 + 星辉符文 + 天然注魔线圈×2 + IV 力场×4 + 盖亚锭×4 + 精灵尘×8 + 魔力符文×4；灵气 16000/魔力 8000/**注魔秩序 4000** | 9+2→**9+3** | 11×11 结构，按结构材料配平，追加秩序流体 |
| 启命花园 `pollution_multi_dan_de_life_on` | LuV | 装配 | 光束核心 4 + LuV 电路 + 星辉符文×2 + Botania 细胞方块×16 + 演化催化核心 + 盖亚锭×4 + **蒲公英生命花（dandelifeon）×4** + LuV 力场×4 + 魔力基板 V×4；注魔生命 16000/灵气 8000/植物 4000 | 9+3→**9+3**（生命花替换精灵尘，精灵尘转为运行燃料） | 生命游戏控制器；蒲公英生命花是生命游戏主题花，结构用细胞方块/魔力基板 |

## HatchRecipes 材料丰富化（LV..UV）

### 组装机类（魔导组装机，9 物品 + 3 流体上限）

| 家族 | 配方数 | 基础件 | 丰富化追加（按等级，缺件自动省略） | 输入数（物品+流体，前→后） |
| --- | --- | --- | --- | --- |
| 魔力输入仓 4A/16A/64A | 24 | GT 能量仓 + 魔力符文 + 等级齿轮 + 传感器（EV 以下再加增幅线材） | LV 魔力粉；MV 风之符文；HV 土之符文+神秘锭；EV 水之符文+神秘锭+钛板+焊锡 144；IV 火之符文+钨钢板+虚空锭+焊锡 288；LuV+ 春/夏/冬之符文+硅岩合金板+虚空锭+盖亚锭 | 4-6+1→**5-9+1-2** |
| 魔力输出仓 1A/4A/16A/64A | 32 | GT 能量仓 + 魔力符文 + 等级齿轮 + 发射器（1A 额外消耗魔力钻石以避开魔力池配方冲突） | 同上 | 4-6+1→**5-9+1-2** |
| 无线魔力输入/输出仓（各安培） | 64 | 对应有线魔力仓 + 魔力共振线圈 + Botania 火花 + AE2 无线接收器（末影之眼兜底） | 同上，且 EV+ 追加等级发射器×2 | 4+1→**5-9+1-2** |
| 要素储罐 | 8 | GT 流体输入仓 + 要素罐/虚空罐 + 魔力珍珠/钻石 + 钢化玻璃 | 同上；HV+ 追加注魔魔力 500 | 4+1→**5-8+1-2** |
| 通量消声仓 | 8 | GT 消声仓 + 通量粘液×2 + 炼金触媒 | 同上；HV+ 追加注魔灵气 500 | 2-3+1→**4-7+1-2** |
| 注魔流体仓 | 8 | GT 流体输入仓 + 魔力符文 + 等级齿轮 | 同上；HV+ 追加注魔魔力 500 | 3+1→**4-7+1-2** |

> 丰富化规则 `addHatchEnrichment`：LV 魔力粉 → MV 元素符文 → HV 元素符文+神秘锭 → EV 再追加该等级板材与焊锡 144 → IV 追加虚空锭与焊锡 288 → LuV+ 追加盖亚锭。所有追加件均为可选（`isEmpty()` 时省略），低阶保持廉价。

### 高阶注魔类（IV..UV，每条固定 8 基座）

| 家族 | 核心基座（6-7） | 补足至 8 | 要素（每系 = 4×(tier-HV)，IV 8 .. UV 20） |
| --- | --- | --- | --- |
| 魔力输入仓 1A | 魔力符文、等级传感器×2、神秘锭×2、虚空锭、泰拉钢、原始珍珠 | +魔力钻石、要素瓶 | 5 系 + praecantatio×2 + potentia |
| 魔力输出仓 1A | 魔力符文、等级发射器×2、神秘锭×2、虚空锭、泰拉钢、原始珍珠 | +魔力钻石、火红莲 | 5 系 + praecantatio×2 + potentia |
| 无线魔力输入仓 1A | 共振线圈×2、火花×2、无线接收器、神秘锭×2、虚空锭、盖亚锭、原始珍珠 | +魔力钻石 | 5 系 + praecantatio×2 + potentia + auram |
| 要素储罐 | 要素罐、魔力珍珠、钢化玻璃、神秘锭×2、虚空锭、异域物体 | +魔力钻石、要素瓶 | 交换/虚空/水/秩序 + praecantatio×2 + auram + 思维 |
| 通量消声仓 | 通量粘液×2、炼金触媒、神秘锭×2、虚空锭、异域物体、原始珍珠 | +炼金煤、火红莲 | 腐化/混沌/火/水 + praecantatio×2 + 虚空 |
| 注魔流体仓 | 魔力符文、要素瓶、神秘锭×2、虚空锭、泰拉钢、原始珍珠 | +魔力钻石、钢化玻璃 | 水/水晶/交换 + praecantatio×2 + 秩序 + 运动 |

> 不稳定度 IV 4 → UV 12；全部走 `InfusionRecipes.infusion`，任一必需件缺失仅跳过该条并告警。

## 等级判定说明

- `MANA_GENERATOR`：`MANA_GENERATOR_TIERS = {1..5}`（LV..IV），LV/MV/HV 注魔、EV/IV 装配。
- `SMALL_NODE_GENERATOR`：`SMALL_NODE_GENERATOR_TIERS = {6..9}`（LuV..UHV），按任务覆盖 LuV/ZPM/UV，UHV 跳过。
- `NODE_FUSION_REACTOR_LUV/ZPM/UV`：注册等级 6/7/8。
- 其余多方块在 `PollutionMachines` 中未显式 `.tier()`，按上游定位与结构材料判定：
  - `MAGIC_MEGA_TURBINE` IV（上游 `META_TILE_ENTITY` 注册时传入 `GTValues.IV`，语言文件亦标注 IV）；
  - `MAGIC_GREEN_HOUSE` EV（上游注魔使用 HV 电路 + GTFO 温室，GTFO 温室为 EV）；
  - `BOT_DISTILLERY` EV（对应 GT 蒸馏塔等级）；
  - `NODE_WASHER` / `MULTI_DAN_DE_LIFE_ON` LuV（节点/生命游戏家族，线圈+电压定位）；
  - `MANA_PLATE` IV（漫宿机械方块 tier 4/5 段位）；
  - `SOURCE_CHARGE` LV（无能量接口的单方块，早期）。

## 已核实的物品 id（丰富化新增）

- Botania：`rune_air/fire/water/earth/spring/summer/winter`、`mana_powder`、`mana_pearl`、`mana_diamond`、`terrasteel_ingot`、`gaia_ingot`、`elementium_ingot`、`pixie_dust`、`dragonstone`、`endoflame`、`dandelifeon`、`cell_block`、`alchemy_catalyst`、`spark`、`natura_pylon`、`livingwood_log`/`dreamwood_log` 等（lang/blockstate 对照 Botania 1.20.1-456 源与 jar 核实）。
- Thaumcraft 4R：`thaumium_ingot`、`void_ingot`、`primordial_pearl`、`eldritch_object`、`alumentum`、`flux_goo`、`resonator`、`warded_jar`、`void_jar`、`essence_phial`、`vis_charge_relay`、`node_transducer`、`runic_matrix` 等（`thaumcraft-forge-0.1.0-20711` 的 `en_us.json` 核实）。
- AE2 15.4.10：`wireless_receiver`、`calculation_processor`、`engineering_processor`、`logic_processor`、`fluix_crystal`、`certus_quartz_crystal` 等（`ae2` lang 核实）。
- GTCEu 7.5.3：`TagPrefix.plate/bolt/screw/rod/ring/gear/frameGt/foil/rotor/wireGt*`、`GTMaterials`（Steel/Aluminium/StainlessSteel/Titanium/TungstenSteel/HSSG/NaquadahAlloy/Tritanium/Neutronium/SolderingAlloy/Lubricant 等）、等级组件 `lv_sensor/emitter/field_generator/electric_pump/robot_arm`、`superconducting_coil` 等（GTCEu 7.5.2/7.5.3 源码与 jar 核实）。

## 未触碰 / 跳过

- 已由 `HatchRecipes`、`PollutionRecipes`（Vis 发电机/供应器/吸收器/净化器/燃料电池/Vis 仓）覆盖的仓室与单方块不重复注册。
- `MAGIC_LARGE_TURBINE`、`MEGA_MANA_TURBINE` 等已由 `MagicGCYMRecipes` 覆盖，不重复。
- 按任务要求跳过 `uhv_small_node_generator`。
- 未使用任何新物品 id：全部取自现有 Botania/Thaumcraft/AE2/GTCEu/Pollution 注册项，缺件时仅该条配方跳过。
- 魔导组装机配方严格控制在 `setMaxIOSize(9, 1, 3, 0)` 内；如未来需要 10+ 种物品输入，需同步调整 `PORecipeMaps.MAGIC_ASSEMBLER_RECIPES` 的槽位上限，否则 GT 只告警且 JEI/UI 显示不全。
