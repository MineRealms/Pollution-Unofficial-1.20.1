# Pollution Unofficial 1.20.1

Pollution 的非官方 Forge 1.20.1 移植版。上游是 Minecraft 1.12.2 的
`H:\MinecraftMods\Pollution`，原本围绕 GTCEu/GTQT 和 Thaumcraft 6 设计；本项目把
污染、源质、魔力机器、多方块、世界生成和相关联动适配到现代 Forge API。

当前可移植范围已经完成。血魔法和 Astral Sorcery（星辉魔法）的提供端、机器、配方、
Astral 方尖碑和原生星辉产能端按用户范围明确排除；独立 Starstream 网络保留，并提供
`receiveConstellationEnergy` 等兼容接口供外部整合调用。

## 当前版本矩阵

- Minecraft 1.20.1 / Forge 47.4.23
- Java 17 / Gradle Wrapper 8.8
- GregTech CEu Modern 7.5.3
- Thaumcraft 4R、Forbidden Magic、Tainted Magic、Thaumic Tinkerer：0.1.0-20721
- Thaumic Energistics：0.1.0-20711，仅 compile-only
- JEI 15.56.0.205
- KubeJS 2001.6.5-build.16
- Botania、AE2、Curios、TerraBlender、Patchouli、Guideme 等依赖版本以
  `gradle.properties` 为准

本地 TC4R 开发工件不进入仓库，放置方式见 [`local-repo/README.md`](local-repo/README.md)。

## 本轮 TC4R 20721 更新

- Thaumcraft 4R、Forbidden Magic、Tainted Magic 和 Thaumic Tinkerer 已从 20719 升到 20721。
- 20721 新增玩家研究知识视图 API，并调整首次发现要素的奖励和研究完成 Warp 行为；本模组没有调用受影响的发放 API，现有直接调用签名保持兼容，无需改写适配代码。
- 20721 移除了通用物品注册 `thaumcraft:primal_arrow`（六种元素箭仍存在）；旧存档若实际保存了该通用箭物品，Forge 会报告缺失注册并可能丢弃该物品。没有语义等价的替代项，因此不做自动映射。
- `compileJava`、`reobfJar`、35 项 Forge GameTest 均通过；独立 `runClient` 加载新版 TC4R、完成污染联动注册及客户端资源初始化。
- 20721 未附 Thaumic Energistics，因此 20711 仍仅作 `compileOnly` 参考，不进入运行包。

DEV 客户端依赖包的来源、替换规则与版本说明见 [`docs/DEV_MODS_BUNDLE_20721.md`](docs/DEV_MODS_BUNDLE_20721.md)。

## 已完成内容

- 按区块污染数据、分级负面效果、环境转化、机器排污、消声仓和爆炸归因。
- TC4R 灵气/咒波/源质适配，Vis、Aspect、Infused 流体和魔法支付逻辑。
- GTCEu 单体机器、多方块、分层蒸馏塔、化学浸洗池、魔法电池和大型/巨型轮机。
- Botania 魔力仓、魔力转换、生命花阵列、Alfheim 世界生成与客户端天空效果。
- AE2 配方联动、JEI 信息页和配方增幅说明、KubeJS 配方与污染事件桥接。
- Starstream 核心/中继/操作核心/区块锚、持久化、区块票据和原子多通道扣款。
- 矿物提取器、护目镜、翅膀、Curios 兼容、传送门形成和边界检查。
- Underground/Alfheim 世界生成、矿脉、石团、焦油池、PureTar 和结构战利品。

## 构建和运行

在 PowerShell 中执行：

```powershell
.\gradlew.bat compileJava reobfJar --console=plain
.\gradlew.bat runData --console=plain
.\gradlew.bat runGameTestServer --console=plain
.\gradlew.bat runServer --console=plain
.\gradlew.bat runClient --console=plain
```

`runClient` 使用独立的 `run-client` 工作目录。开发客户端烟测使用：

```powershell
.\gradlew.bat runClient -PclientSmokeTest --console=plain
```

首次构建要求 JDK 17、TC4R 本地开发 jar，以及访问 GTCEu、JEI、KubeJS 等 Maven 仓库。

## 最近验收

2026-09-25 的最终回归结果：

- `runData`、`compileJava`、`reobfJar`：通过。
- Forge GameTest：35/35 通过。
- TC4R 20721 升级回归：重新编译及 35/35 GameTest 通过；独立客户端完成 Forge/TC4R/Pollution 初始化。完整 UI/联机客户端烟测见此前 20719 基线记录。
- 服务端烟测：通过，污染专属错误为 0。
- 20719 基线客户端烟测：通过，包含 HUD 网络、石材模型、矿物提取器 GUI/按钮、装备、JEI 和 Alfheim 天空。
- 2206 个资源 JSON 可解析且无重复键；污染纹理和模型目标审计通过。

截图位于 `run-client/screenshots`。完整验收边界和行为不变量见
[`docs/PORT_COMPLETION_CHECKLIST.md`](docs/PORT_COMPLETION_CHECKLIST.md)。

## 文档入口

| 文档 | 用途 |
|---|---|
| [`docs/README.md`](docs/README.md) | 文档索引、当前状态和阅读顺序 |
| [`docs/PORTING_TARGET.md`](docs/PORTING_TARGET.md) | 移植目标、范围边界和现代 API 取舍 |
| [`docs/PORT_COMPLETION_CHECKLIST.md`](docs/PORT_COMPLETION_CHECKLIST.md) | 当前完成范围、核心不变量、最终验证 |
| [`docs/SMOKE_TEST_REPORT.md`](docs/SMOKE_TEST_REPORT.md) | 服务端烟测和 RCON 检查 |
| [`docs/GAMEPLAY_TUTORIAL.md`](docs/GAMEPLAY_TUTORIAL.md) | 玩家操作、机器、配方和配置说明 |
| [`docs/MIGRATION_TRACKER.md`](docs/MIGRATION_TRACKER.md) | 版本矩阵、API 记录和历史执行日志 |
| [`docs/PORT_GAP_AUDIT.md`](docs/PORT_GAP_AUDIT.md) | 上游对比审计；以最新验收清单覆盖历史快照 |
| [`docs/SUBSTITUTIONS.md`](docs/SUBSTITUTIONS.md) | 1.12 依赖在现代版本中的替代关系 |

迁移跟踪文档保留了开发过程中的历史记录；判断当前状态时，以 README、移植边界、
最新验收清单和烟测报告为准。

## 协议

本移植沿用上游协议：AGPL-3.0-or-later。上游版权：Copyright (c) KeQingSoCute520 越人不歌。
