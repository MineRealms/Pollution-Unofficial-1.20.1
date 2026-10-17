# Pollution Unofficial 1.20.1

Pollution 的非官方 Forge 1.20.1 移植版。原版是 Minecraft 1.12.2 上围绕 GregTech（GTCEu/GTQT）
与神秘时代 6 设计的污染管理模组；本项目把污染、源质、魔力机器、多方块、世界生成和各条联动
完整适配到现代 Forge API，并接入了本地移植的星辉魔法（Astral Sorcery）1.20.1。

## 这是什么模组

在 GregTech 的工业体系中，多方块机器的消声仓、爆炸和大规模加工都会向区块排放**污染**。污染不仅
损害玩家健康、拖慢机器效率，还会催生难缠的裂隙。本模组提供一整套污染的产生、监测、清理与利用
玩法，并把神秘时代 4R 的灵气/咒波/源质体系接入工业链路：

- **污染系统**：按区块的污染数据、分级负面效果、环境转化、机器排污与爆炸归因，
  `/pollution` 查询与清理命令。
- **工业魔法机器**：单体机器、多方块、分层蒸馏塔、化学浸洗池、魔法电池、大型/巨型轮机。
- **神秘时代 4R 联动**：Vis、要素、注魔流体与魔法支付逻辑；咒波与污染互相转化。
- **星辉魔法联动**：Starstream 核心/中继/操作核心/区块锚、十六星座塔、透镜仓、
  五类星辉处理机，以及星辉供能接口。
- **植物魔法联动**：魔力仓、魔力转换、生命花阵列、Alfheim 世界生成与天空效果。
- **AE2 / JEI / KubeJS**：AE2 配方联动、JEI 信息页、KubeJS 配方与污染事件桥接。
- **装备与世界**：矿物提取器、护目镜、翅膀（Curios 兼容）、Underground/Alfheim 世界生成、
  矿脉、焦油池与结构战利品。

血魔法仍按范围排除。Astral Sorcery 开发 JAR 放在
`local-repo/local/astral/astralsorcery/1.20.1.0/`，不提交到 Git，需要本地构建时准备。

## 版本矩阵

- Minecraft 1.20.1 / Forge 47.4.23
- Java 17 / Gradle Wrapper 8.8
- GregTech CEu Modern 7.5.3
- Thaumcraft 4R、Forbidden Magic、Tainted Magic、Thaumic Tinkerer：编译基准 0.1.0-20721，
  已验证兼容最新构建 0.1.0-20740
- Thaumic Energistics：0.1.0-20711，仅 compile-only
- JEI 15.62.0.216（与本地 Astral 端一致）
- KubeJS 2001.6.5-build.16
- Botania、AE2、Curios、TerraBlender、Patchouli、GuideME 等依赖版本以
  `gradle.properties` 为准

本地 TC4R 开发工件不进入仓库，放置方式见 [`local-repo/README.md`](local-repo/README.md)。

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

## 文档入口

| 文档 | 用途 |
|---|---|
| [`docs/README.md`](docs/README.md) | 文档索引、当前状态和阅读顺序 |
| [`docs/PORTING_TARGET.md`](docs/PORTING_TARGET.md) | 移植目标、范围边界和现代 API 取舍 |
| [`docs/PORT_COMPLETION_CHECKLIST.md`](docs/PORT_COMPLETION_CHECKLIST.md) | 当前完成范围、核心不变量、最终验证 |
| [`docs/GAMEPLAY_TUTORIAL.md`](docs/GAMEPLAY_TUTORIAL.md) | 玩家操作、机器、配方和配置说明 |
| [`docs/ASTRAL_SURVIVAL_AND_UI.md`](docs/ASTRAL_SURVIVAL_AND_UI.md) | 星辉生存与界面说明 |
| [`docs/SUBSTITUTIONS.md`](docs/SUBSTITUTIONS.md) | 1.12 依赖在现代版本中的替代关系 |

开发过程的历史验收记录见 git log 与 `docs/` 各专题文档。

## 致谢与协议

本项目的全部核心内容都来自原版 Pollution，在此向原作者致谢：

- **Pollution**（1.12.2，含主线策划、代码与**全部材质美术**）
  — Copyright (c) **KeQingSoCute520（越人不歌）**，
  [GTQT/Pollution](https://github.com/GTQT/Pollution)。
  本移植中的方块、物品、机器与 GUI 材质均取自原版，版权归原许可方所有。
- **GregTech CEu Modern**、**Thaumcraft 4R** 及其它联动模组各自的原作者与贡献者。
- 1.20.1 移植适配由 MineRealms 的移植贡献者完成。

本移植沿用上游协议：**AGPL-3.0-or-later**（见 [LICENSE](LICENSE)）。
任何二次分发请一并遵守该协议并保留对原作者的署名。
