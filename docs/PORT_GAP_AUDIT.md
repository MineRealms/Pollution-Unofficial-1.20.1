# 上游对比缺口审计（排除：血魔法 / 星辉魔法）

> 2026-09-25 更新：下文为 9 月 24 日的历史抽查，机器“已注册”不代表行为完整。后续发现并修复的支付、超频、转子、储能、蒸馏、浸洗、传送门入口及世界生成问题，以 [最新验收清单](PORT_COMPLETION_CHECKLIST.md) 和 [移植边界](PORTING_TARGET.md) 为准。

> 生成时间：2026-09-24。对照上游 1.12 `H:\MinecraftMods\Pollution`（423 个 Java 文件）
> 与本移植（293 个）。本表只列**用户可见/功能级**缺口；改名重构（MetaTileEntityX → XMachine
> 等）不计入。状态：❌ 缺失、⚠️ 部分、✅ 已补齐、🚫 按指令排除。

## ✅ 机器（5 台）

| 上游 | 说明 | 现状 |
|---|---|---|
| `magic_turbine.lv` / `.mv` / `.hv` | 源质要素轮机 LV/MV/HV（`SimpleGeneratorMetaTileEntity`，烧 `MAGIC_TURBINE_FUELS`） | ✅ `SimpleGeneratorMachine` 已注册并有配方/模型 |
| `pollution_large_mana_turbine` | 大型魔力导能塔（LuV，`MANA_TO_EU`，结构用 `MANA_3` 板 + 钨钢管道） | ✅ 独立 `LargeManaTurbineMachine`，无消声仓 |

备注：两台的燃料表 `MAGIC_TURBINE_FUELS` / `MANA_TO_EU` 均已存在，只是机器本体没注册。

## ⚠️ 星流（Starstream）网络（~15 类 + 4 TESR）

已就绪：`starstream_casing` / `starstream_runed_casing` / `constellation_anchor` 方块、
星流链接器物品（模式切换已实现）、相关合成配方、JEI 管线。

已补：

- **方块 + BE**：核心、中继器、操作核心、跨维度中继、区块锚，包含保存、容量、路由、区块锚定和原子扣款。
- **网络层**：`StarstreamWirelessBinding`、`IStarstreamWirelessProvider` / `IStarstreamWirelessTerminal`、
  `StarstreamNetwork` 与 `IStarstreamOperationCore`。
- **物品**：星流链接器的核心→操作核心绑定流程；输入模式支持中继/网关→目标端点。

上游 29×33 的方尖碑多方块和动态 TESR 缩为可用的核心方块与普通模型；Astral 星座塔产能端按范围排除，必须由外部整合调用 `receiveConstellationEnergy`。

> 用户已明确排除 Astral。独立星流网络保留，并补充了非玩家拆除、卸载目标的断链回收和区块票据恢复校验；依赖 Astral 的方尖碑结构属于排除范围。

## ✅ 客户端渲染/表现

| 项目 | 上游 | 现状 |
|---|---|---|
| 扭曲屏幕效果 | `ClientWarpEffects` + `PacketBlinkParticles` / `PacketFakeExplosionSound` / `PacketFakeRain` | ✅ 服务端包 + 客户端本地粒子，不改天气且无伤害 |
| 史莱姆凝胶层 | `LayerTcSlimeGel`（TC 史莱姆外层半透明胶质渲染） | ✅ 继承原版 SlimeOuterLayer 并使用动态纹理 |
| 矿物提取器物品形态 | `ItemMineralExtractorRenderer`（`BlockEntityWithoutLevelRenderer` + `builtin/entity`） | ✅ 自定义 BlockItem + BEWLR |
| 护目镜/翅膀穿戴渲染 | `GogglesNano/Quantum`、`NanosuitWings/QuantumWings`（头盔/胸甲护甲模型） | ✅ 四套护甲纹理与姿态同步 |

## ⚠️ 部分完成

| 系统 | 上游规模 | 现状 |
|---|---|---|
| Warp 事件 | 30+ 类：19 个独立效果 + 队列/标签/工具 + 每效果配置开关 + 包体 | ✅ 紧凑注册表覆盖 22 效果，配置、倒计时、假表现和诊断触发均已补 |
| JEI 指南页 | `MagicGuideRecipes`（晶圆/塔罗/水晶培育 3 页，`MagicRecipeProperties.guidePage`） | ✅ 四份上游信息页通过 JEI 物品说明展示，Astral 页明确标注范围外依赖 |
| 结构战利品 | `TBLLootTableList` + `GregTechLootTable` | ✅ Forge 独立 LootPool，现代村庄映射和 TBL 缺失命名空间安全跳过 |
| 指令 | `CommandMagicAmplification`（查看/设置魔法增幅） | ✅ `/pollution amplification inspect|profiles|explain` 与 `/pollution warp trigger` |
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
