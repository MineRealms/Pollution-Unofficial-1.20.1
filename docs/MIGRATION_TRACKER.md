# Pollution Unofficial 1.20.1 移植跟踪文档

> 本文件是持续更新的移植主控文档。每完成一个步骤，都必须在此登记：状态、涉及文件、验证方式、遗留问题。

- 上游项目：`H:\MinecraftMods\Pollution`（Minecraft 1.12.2，GTCEu/GTQT 附属，Thaumcraft 6 时代设计）
- 目标项目：`H:\MinecraftMods\Pollution-Unofficial-1.20.1`
- 目标分支策略：单分支 `main`，阶段完成后提交
- 最近更新：2026-09-18（Phase 1 进行中）

## 1. 版本矩阵（锁定，不允许浮动）

| 组件 | 版本 | 来源 |
|---|---|---|
| Minecraft | 1.20.1 | Forge MDK |
| Forge | 47.4.23 | `https://maven.minecraftforge.net/` |
| Gradle wrapper | 8.8 | `gradle/wrapper/gradle-wrapper.properties` |
| Java | 17（Temurin 17.0.18.8） | `gradle.properties` → `org.gradle.java.home` |
| Thaumcraft 4R | `dev.tc4port:thaumcraft-forge:0.1.0-20711` | 本地 Maven `local-repo/` |
| GregTech CEu Modern | `com.gregtechceu.gtceu:gtceu-1.20.1:7.5.3` | `https://maven.gtceu.com` |
| LDLib | `com.lowdragmc.ldlib:ldlib-forge-1.20.1:1.0.40.b` | `https://maven.firstdark.dev/snapshots` |
| Registrate | `com.tterrag.registrate:Registrate:MC1.20-1.3.11` | `https://maven.tterrag.com/` |
| JEI | `mezz.jei:jei-1.20.1-forge:15.59.0.212` | `https://maven.blamejared.com/` |
| KubeJS | `dev.latvian.mods:kubejs-forge:2001.6.5-build.26` | `https://maven.latvian.dev/releases` |
| Rhino | `2001.2.3-build.10` | 同上 |
| Architectury | `9.2.14` | `https://maven.architectury.dev/` |
| Curios | `5.14.1+1.20.1` | `https://maven.theillusivec4.top/` |
| TerraBlender | `1.20.1-3.0.1.10` | Forge Maven |

JEI 下限说明：TC4R 插件引用 `ISubtypeInterpreter`（JEI ≥ 15.59），因此 15.59.0.212 为硬性下限。

## 2. 阶段总览

| 阶段 | 内容 | 状态 |
|---|---|---|
| Phase 0 | 目标工程骨架、Gradle 8.8、依赖锁定、Git 初始化 | 进行中 |
| Phase 1 | 污染核心：区块污染数据、命令、配置、负效应框架 | 未开始 |
| Phase 2 | GTCEu 集成：addon 注册、机器排污、消声仓等级、爆炸归因 | 未开始 |
| Phase 3 | TC4R 集成：灵气抽取/再生、咒波清洗、扭曲联动 | 未开始 |
| Phase 4 | 基础机器：灵气发电机、灵气再生机、空气过滤机（单方块） | 未开始 |
| Phase 5 | 多方块与材料：魔导系列、魔法合金、催化剂、源质系统 | 未开始 |
| Phase 6 | 其他联动（全部 TODO，见第 6 节） | 未开始 |
| Phase 7 | 资产、模型、平衡、数据生成、发布 | 未开始 |

## 3. Phase 0 清单

- [x] 建立目标目录 `Pollution-Unofficial-1.20.1`
- [x] 复制 Gradle 8.8 wrapper（`gradlew`、`gradlew.bat`、`gradle/wrapper/*`）
- [x] 写入 `build.gradle`（ForgeGradle 6、Java 17、本地 Maven、GTCEu/JEI/KubeJS/TC4R 依赖）
- [x] 写入 `settings.gradle`、`gradle.properties`（版本矩阵锁定）
- [x] 写入 `.gitignore`、`local-repo/README.md`
- [x] 放入 TC4R 开发工件（`local-repo/dev/tc4port/...`，jar 不入库）
- [x] 建立 `README.md` 与本跟踪文档
- [ ] `git init` + 首次提交
- [ ] `gradlew compileJava` 通过（允许依赖下载）

## 4. Phase 1 清单（污染核心）

- [ ] `PollutionData`：按区块的脏数据表（`SavedData`，稀疏存储）
- [ ] `PollutionEngine`：读/加/清洗 API 与低频衰减 tick
- [ ] `PollutionConfig`：开关、倍率、阈值
- [ ] `/pollution get|add|set|scrub` 调试命令
- [ ] 玩家负效应（虚弱/挖掘疲劳/反胃/失明，按梯度）
- [ ] 环境污染转化（草→沙、水→岩浆，带预算限流）
- [ ] 客户端同步（仅邻近区块，阈值触发）
- [ ] 服务器 TPS 预算与性能基准（见第 8 节）

## 5. 依赖 API 基线（已核实）

### 5.1 TC4R（`dev.tc4port.thaumcraft.api.*`）

| 需求 | API | 说明 |
|---|---|---|
| 抽取灵气 | `VisNetworkApi.drain(ServerLevel, BlockPos, VisChannel, int, VisAction)` | `VisChannel.AER/TERRA/IGNIS/AQUA/ORDO/PERDITIO` |
| 清洗咒波 | `FluxApi.consumeNearby(ServerLevel, BlockPos, range, quanta, FluxConsumeContext, VisAction)` | `range` 0..32；返回 `FluxConsumeResult(consumedQuanta, affectedBlocks)` |
| 机器上下文 | `FluxConsumeContext.machine(ResourceLocation, BlockPos)` | 有事件 `FluxConsumeEvent` 可取消 |
| 扭曲读取 | `PlayerWarpApi.view(Player)` → `PlayerWarpView(permanent, sticky, temporary)` | 只读视图 |
| 节点视图 | `AuraNodeView`、`AuraNodeState` | 供后续节点机器使用 |
| 守卫灵气 | `WardingAuraApi` | 后续阻挡/保护逻辑 |

参考实现：`H:\MinecraftMods\FM-port-deps\tc-src\dev\tc4port\thaumcraft\block\entity\FluxScrubberBlockEntity.java`

### 5.2 GTCEu Modern 7.5.x（已核实源码 + 字节码）

| 需求 | API | 说明 |
|---|---|---|
| addon 声明 | `@GTAddon` + `IGTAddon` | 注解扫描发现，无需手工注册 |
| 注册器 | `GTRegistrate.create(modId)` / `registerRegistrate()` | 由 addon 持有 |
| 材料 | `IGTAddon#registerMaterials` + `Material.Builder` | Phase 5 |
| 机器 | `GTRegistrate#machine` / `multiblock` | Phase 4/5 |
| 配方 | `IGTAddon#addRecipes(Consumer<FinishedRecipe>)` | Phase 5 |
| KJS 配方键 | `IGTAddon#registerRecipeKeys(KJSRecipeKeyEvent)` | Phase 5/6 |

参考源码：`H:\MinecraftMods\GregTech-Modern-7.5.2\src\main\java\com\gregtechceu\gtceu\api\addon\`

## 6. 其他附属扩展联动（全部 MARK TODO）

| 联动 | 上游 1.12.2 依赖 | 1.20.1 目标 | 状态 |
|---|---|---|---|
| 植物魔法 | Botania | Botania 1.20.1-456-FORGE | TODO（Phase 6） |
| 血魔法 | Blood Magic | Blood Magic 1.20.1 | TODO（Phase 6） |
| 星辉魔法 | Astral Sorcery | Astral Sorcery 1.20.1 | TODO（Phase 6，上游已停更风险） |
| 额外植物学 | ExtraBotany | 无已知 1.20.1 版 | TODO（可能降级为可选自实现） |
| AE2 联动 | AE2 | AE2 15.0.18 | TODO（Phase 6） |
| 神秘能源 | Thaumic Energistics | TC4R 附属 `thaumic-energistics` 本地包 | TODO（Phase 6，参考 FM-port-deps） |
| 禁忌魔法 | Forbidden Magic | TC4R 附属 `forbidden-magic` 本地包 | TODO（Phase 6，参考 FM-port-deps） |
| 污秽魔法 | Tainted Magic | TC4R 附属 `tainted-magic` 本地包 | TODO（Phase 6，参考 FM-port-deps） |
| 神秘工匠 | Thaumic Tinkerer | TC4R 附属 `thaumic-tinkerer` 本地包 | TODO（Phase 6，参考 FM-port-deps） |
| 商店系统 | FTB Library/Quests/Money | FTB 1.20.1 对应版本 | TODO（大概率废弃，转 KubeJS） |
| 信息显示 | TOP | Jade / TOP 1.20.1 | TODO（Phase 7 可选） |
| 连接纹理 | CTM | 现代 CTM / 普通模型 | TODO（Phase 7） |
| 旧 UI 库 | ModularUI 3.1.5 | GTCEu Modern UI / LDLib | 已替换方向确认 |
| 旧模型加载 | ModelLoader (OBJ) | 原生 JSON/烘培模型 | TODO（Phase 7） |

## 7. 强制保留的能力

- [x] JEI 依赖声明（`15.59.0.212`）
- [ ] JEI 插件骨架（`@JeiPlugin`）
- [ ] JEI 配方分类：机器排污信息、注魔对照
- [x] KubeJS 依赖声明（`2001.6.5-build.26`）
- [ ] KubeJS 插件骨架（`kubejs.plugins.txt` 注册）
- [ ] KubeJS 脚本事件：配方增删、污染事件暴露

## 8. 性能红线（迁移时必须遵守）

- 区块污染只存脏数据，禁止全表 tick 扫描
- 玩家效果 20–100 tick 检查一次，不每 tick 遍历
- 环境转化随机采样 + 每维度每 tick 预算
- 大型过滤机缓存区块列表，不重复搜索
- 网络同步仅发邻近玩家且超阈值
- 污染归零即从存档删除记录

## 9. 风险登记

| 风险 | 等级 | 说明 | 缓解 |
|---|---|---|---|
| TC4R 处于 0.1.0 开发期，API 可能变动 | 高 | 已确认 API 可用但非冻结 | 所有调用集中在 `compat/tc4r`，单点适配 |
| GTCEu 无“配方完成/排气”公开事件时 | 高 | 需要机器扩展点或受限 Mixin | 优先公共 API → 扩展接口 → AT → Mixin（隔离包） |
| GTCEu 版本跨 7.5.3 / 8.0.0 | 中 | 7.5.2 源码仅参考；8.0.0 不承诺 | 版本锁定 7.5.3，禁止浮动 |
| GTCEu 依赖（LDLib/Registrate）版本不匹配 | 中 | 必须与 7.5.3 对应 | 锁定 1.0.40.b / MC1.20-1.3.11，构建验证 |
| TC4R 工件不可公开发布 | 低 | 本地 Maven 引用 | jar 不入库，README 记录来源 |
| 上游 440 个源文件的重写工作量 | 高 | 逐层移植 | 按 Phase 拆解，先闭环最小系统 |
| 旧资产格式（1.12 模型/OBJ） | 中 | 不能直接复用 | Phase 7 统一转换 |

## 10. 变更日志

### 2026-09-18 — Phase 0 启动
- 建立目标工程目录与 Gradle 8.8 wrapper
- 锁定版本矩阵，写入 `build.gradle` / `gradle.properties`
- 核实 TC4R 公共 API（`VisNetworkApi`、`FluxApi`、`PlayerWarpApi`）与 GTCEu addon 机制（`@GTAddon` 注解扫描）
- 放入 TC4R 本地 Maven 工件
- 建立本跟踪文档
