# 上游对比缺口审计（排除：血魔法 / 星辉魔法）

> 生成时间：2026-09-24。对照上游 1.12 `H:\MinecraftMods\Pollution`（423 个 Java 文件）
> 与本移植（293 个）。本表只列**用户可见/功能级**缺口；改名重构（MetaTileEntityX → XMachine
> 等）不计入。状态：❌ 缺失、⚠️ 部分、🚫 按指令排除。

## ❌ 机器（5 台）

| 上游 | 说明 | 现状 |
|---|---|---|
| `magic_turbine.lv` / `.mv` / `.hv` | 源质要素轮机 LV/MV/HV（`SimpleGeneratorMetaTileEntity`，烧 `MAGIC_TURBINE_FUELS`） | ❌ 只有大型（EV `magic_large_turbine`）与特大（IV `magic_mega_turbine`） |
| `pollution_large_mana_turbine` | 大型魔力导能塔（LuV，`MANA_TO_EU`，结构用 `MANA_3` 板 + 钨钢管道） | ❌ 只有巨型（`mega_mana_turbine`） |

备注：两台的燃料表 `MAGIC_TURBINE_FUELS` / `MANA_TO_EU` 均已存在，只是机器本体没注册。

## ❌ 星流（Starstream）网络（~15 类 + 4 TESR）

已就绪：`starstream_casing` / `starstream_runed_casing` / `constellation_anchor` 方块、
星流链接器物品（模式切换已实现）、相关合成配方、JEI 管线。

缺失：

- **多方块**：星流中枢方尖碑 `starstream_nexus_obelisk`（上游 `MetaTileEntityStarstreamNexusObelisk`）
- **方块 + BE + TESR**：方尖碑核心、中继器 `POStarstreamRelay`、操作核心 `POStarstreamOperationCore`、
  跨维度中继 `POStarstreamInterdimensionalRelay`、区块锚 `POStarstreamChunkAnchor`
  （对应 4 个 `TesrStarstream*`、5 个 `TileEntity*`）
- **网络层**：`StarstreamChunkLoadingManager`（区块加载）、`StarstreamWirelessBinding`、
  `IStarstreamWirelessProvider` / `IStarstreamWirelessTerminal`、`StarstreamWirelessTerminalStatus`、
  `StarstreamNetworkConstants`、`StarstreamNetworkPreviewHandler`（网络预览）
- **物品**：无线终端（带 GUI，见上游 `IStarstreamWirelessTerminal`）
- **接口**：`IStarstreamOperationCore`

> ⚠️ 若"星辉"本意包含星流网络，此节可整体排除；按星辉=星辉魔法(Astral) 理解，本节点仍是最大剩余项。

## ❌ 客户端渲染/表现

| 项目 | 上游 | 现状 |
|---|---|---|
| 扭曲屏幕效果 | `ClientWarpEffects` + `PacketBlinkParticles` / `PacketFakeExplosionSound` / `PacketFakeRain` | ❌ 我们的 warp 效果纯服务端，无客户端粒子/音效/假雨表现 |
| 史莱姆凝胶层 | `LayerTcSlimeGel`（TC 史莱姆外层半透明胶质渲染） | ❌ 我们的 6 只史莱姆只有本体渲染 |
| 矿物提取器物品形态 | `ItemMineralExtractorRenderer`（`BlockEntityWithoutLevelRenderer` + `builtin/entity`） | ❌ 方块形态已移植（`MineralExtractorRenderer`），物品形态没有 |
| 护目镜/翅膀穿戴渲染 | `GogglesNano/Quantum`、`NanosuitWings/QuantumWings`（头盔/胸甲护甲模型） | ⚠️ 已改 Curios 形态（功能齐全），无穿戴模型；`GogglesItem` javadoc 留有 TODO |

## ⚠️ 部分完成

| 系统 | 上游规模 | 现状 |
|---|---|---|
| Warp 事件 | 30+ 类：19 个独立效果 + 队列/标签/工具 + 每效果配置开关 + 包体 | ⚠️ 紧凑版 3 类、20 效果（含 blind/nausea/poison/wither/jump/wind/blood/lightning/obsidian/mushrooms/fake_explosion/rain/junk/blink/swamp/countdown_bomb/wither_rose/fall/inventory_scramble/zombie_siege）。缺：每效果配置开关、`countdown_bomb` 无倒计时过程（直接爆）、客户端表现 |
| JEI 指南页 | `MagicGuideRecipes`（晶圆/塔罗/水晶培育 3 页，`MagicRecipeProperties.guidePage`） | ⚠️ 已有魔法增幅信息页 + 塔罗 tooltip/文档；`guidePage` 属性与 3 页未按上游移植 |
| 结构战利品 | `TBLLootTableList`（common/dungeon/cragrock tower/wight fortress 箱子+陶罐）+ `GregTechLootTable` | ❌ 无 `loot_tables` 数据；遗迹箱不会开出本模组物品 |
| 指令 | `CommandMagicAmplification`（查看/设置魔法增幅） | ⚠️ `/pollution` 已有 get/set/add/vis/flux/scrub/aspects；无 amplification 子命令（增幅引擎 `api/amplification` 已存在） |
| 血肉丘 | `WorldGenFleshMound`（血维度地形） | ⚠️ 血维度已用 datapack（`flesh_block` 地面）；血肉丘地形未做（属血魔范畴，可忽略） |

## 🚫 排除确认（血魔法 + 星辉魔法）

- 血魔法：`bm_hpca` 全家（8 类）、`blood_magic_hatch`、`BloodAltar` / `BloodCircuit` / `BloodWorld`、
  `POBloodBlock`、`WarpBlood` / `PacketBlood`、血肉丘
- 星辉魔法：`astral_lens_hatch(+_advanced)`、`celestial_*`（3 台）、`MetaTileEntityConstellationTower`、
  `POConstellationCrystal` / `TileEntityConstellationCrystal` / `TesrConstellationCrystal`、
  `industrial_lightwell`、`industrial_starlight_infuser`、`AstralConstellation*` GUI 组件、
  `ConstellationTowerDefinition/Recipes`、`MeteorsHelper`
  （物品侧 `ConstellationDataItem` / `CrystalQualityItem` 保留为简化实现）

## ✅ 已核对齐全（本轮抽查）

- **维度**：地下（8 生物群系 + noise settings 地表规则 + 地下桥结构 + 12 组洞穴 feature 及按群系摆放）、
  Alfheim（12 生物群系 + 湖/树/花/瓜/芦苇/睡莲等 14 组摆放）、血（datapack 基础版）
- **结构方块**：spell_prism ×8、void_prism、星流外壳 ×3、叠层玻璃 ×6、光束核心 ×4、
  魔法电池外壳、POTurbine 齿轮箱 ×5 + 管道 ×5、MANA 板 ×5、线圈
- **机器**：21 台魔导单体、9 级源质缸、塔罗仓、注魔祭坛、节点链（4）、魔力链（导能/花阵/命花园/
  祭坛/花瓣锅/纯雏菊/池仓…）、无线魔力仓、汇流交换、Essence 三台、Botania 四台、太阳能板、
  魔法电池、聚变、小化工厂、Vis 发电机/仓/供应器、源质充能
- **实体**：史莱姆 ×6、元素 ×3、弹射物 ×3 + 渲染器
- **物品**：全部（水戒/护目镜/翅膀/魔法电池/心之果实/星流链接器/Vis 检测器/塔罗牌/塔罗·愚者等）
- **API/杂项**：amplification 引擎、AE2 配方、TC 桥、POAspectMapping（要素→GT 流体）、
  aspect 缸渲染（本轮）、扫帚飞行渲染（本轮）
