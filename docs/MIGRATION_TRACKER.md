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
| Phase 0 | 目标工程骨架、Gradle 8.8、依赖锁定、Git 初始化 | 已完成（构建通过，已提交） |
| Phase 1 | 污染核心：区块污染数据、命令、配置、负效应框架 | 进行中（数据/命令/配置已落地） |
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
- [x] `git init` + 首次提交（`6a58999`）
- [x] `gradlew compileJava` 通过
- [x] `gradlew build` 通过（含 reobfJar），产物 `build/libs/pollution-1.20.1-1.0.0-1.20.1-port.0.1.0.jar`
- [x] 构建环境问题修复：
  - Gradle JVM 不读 `HTTP_PROXY` → 在 `gradle.properties` 写入 `127.0.0.1:7890` 代理到 `org.gradle.jvmargs`
  - `repo.maven.apache.org` TLS 不稳定 → 在 `build.gradle` 将 Maven Central 统一重定向到阿里云镜像
  - LDLib POM 声明了不在任何仓库的 `appeng:appliedenergistics2-forge:15.0.4-beta` → 对 `__obfuscated` 配置排除该模块
  - 所有 mod 依赖使用 `{ transitive = false }`，所需库显式声明
  - Forge 47.4 弃用 `FMLJavaModLoadingContext.get()` → 改为构造器注入 `FMLJavaModLoadingContext` 并 `context.registerConfig(...)`
- [x] 清理资产：1.12 旧格式资源（491 个文件）移至 `docs/reference/legacy-assets/`，避免被 1.20 资源加载器解析；`assets/pollution/lang/` 仅保留新 JSON
- [x] 修复 `gradle.properties` 中文作者名乱码（改用 `\uXXXX` 转义）

## 4. Phase 1 清单（污染核心）

- [x] `PollutionData`：按区块的脏数据表（`SavedData`，稀疏存储）
- [x] `PollutionEngine`：读/加/清洗 API 与低频衰减 tick
- [x] `PollutionConfig`：开关、倍率、阈值
- [x] `/pollution get|add|set|scrub` 调试命令
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

### 5.3 魔法材料与数据基础设施（已落地，2026-09-18）

**GTCEu 7.5.3 实际机制（已核实，非臆造）：**

| 事项 | 7.5.3 实际 | 旧 1.12.2 写法 | 处理 |
|---|---|---|---|
| 材料构建 | `Material.Builder(ResourceLocation)` + `.buildAndRegister()` | `Material.Builder(int id, ResourceLocation)` + `.build()` | 已改 |
| 材料注册钩子 | `MaterialRegistryEvent`（建 registry）+ `MaterialEvent`（建材料），mod bus | `IGTAddon#registerMaterials()` | 已改（该钩子已标记移除） |
| 元素注册 | `new Element(...)` + `IGTAddon#registerElements()` | `Elements.add(...)` | 已改 |
| 数字材料 ID | 不存在 | 自增 ID 段 | 移除 |
| `GENERATE_BOULE` | 不存在（GTQT 私有） | GTQT flag | 丢弃并在代码注释注明 |

**已移植文件：**

- `api/unification/PollutionElements.java`：六要素元素 Ae/Ig/Aq/Ter/Pe/Ord（质子/中子数、名称、符号与原版 `Elements.java` 一致）
- `api/unification/PollutionMaterials.java`：已移植材料的字段表（其余类别保持 TODO）
- `api/unification/materials/ElementMaterials.java`：六要素材料，颜色/形态/图标/元素与环境与原项目逐项一致
- `api/unification/materials/FirstDegreeMaterials.java`：六种魔法合金，`components` 配比与 `blast(2700, LOW)` 与原项目一致
- `api/unification/PollutionMaterialEvents.java`：mod bus 事件注册（registry + materials）
- `loaders/recipes/PollutionRecipes.java`：GT 配方 datagen 钩子（`IGTAddon#addRecipes` 已接线，配方待逐条移植）

**已移植材料对照（颜色/组分逐项来自上游）：**

| 材料 | 上游字段 | 颜色 | 组分（上游原值） |
|---|---|---|---|
| infused_air | InfusedAir | 0xFEFE7D | 元素 Ae |
| infused_fire | InfusedFire | 0xFE3C01 | 元素 Ig |
| infused_water | InfusedWater | 0x0090FF | 元素 Aq |
| infused_earth | InfusedEarth | 0x00A000 | 元素 Ter |
| infused_entropy | InfusedEntropy | 0x43435E | 元素 Pe |
| infused_order | InfusedOrder | 0xEECCFF | 元素 Ord |
| aertitanium | Aertitanium | 0xEED2EE | Bauxite2 + Al1 + Mn1 + InfusedAir5 |
| ignissteel | IgnisSteel | 0x8B1A1A | Steel2 + Mg1 + Li1 + InfusedFire5 |
| aquasilver | Aquasilver | 0xCAE1FF | Ag2 + Sn1 + Hg1 + InfusedWater5 |
| terracopper | Terracopper | 0x8FBC8F | Cu2 + B1 + C1 + InfusedEarth5 |
| ordolead | Ordolead | 0x00008B | Pb2 + Si1 + Au1 + InfusedOrder5 |
| perditioaluminium | Perditioaluminium | 0x9C9C9C | Al2 + F1 + Th1 + InfusedEntropy5 |

**上游材料注册表未移植部分（保持 TODO，按阶段对应）：**

- 依赖 GTQT 专有材料的条目（Mana / Thaumium / 神秘超导线 / 电池线等）→ 需要先决定新基底材料
- 血魔法线（PurifiedBlood 等流体）→ Phase 6
- 星辉/植物魔法线（ElvenElementium、Terrasteel、Orichalcum 等）→ Phase 6
- 催化剂与化工线（Roughdraft、Substrate、AlchemicalResidue/Vapor 系列）→ 对应机器阶段

### 5.4 TC4R 要素映射（已落地，2026-09-18）

`api/magic/PollutionAspectMapping.java`：GT 材料 ↔ TC4R `VisChannel`/`AspectId` 双向映射。

| GT 材料 | VisChannel | AspectId（TC4R 命名空间） |
|---|---|---|
| infused_air | AER (0) | `thaumcraft:aer` |
| infused_earth | TERRA (1) | `thaumcraft:terra` |
| infused_fire | IGNIS (2) | `thaumcraft:ignis` |
| infused_water | AQUA (3) | `thaumcraft:aqua` |
| infused_order | ORDO (4) | `thaumcraft:ordo` |
| infused_entropy | PERDITIO (5) | `thaumcraft:perditio` |

- 依据：上游 `POAspectToGtFluidList` 的 Aspect→Infused 材料语义；TC4R `VisChannel` 枚举（`aer/terra/ignis/aqua/ordo/perditio`，`VisChannel#aspectId()` 自带 `thaumcraft:` 命名空间）
- 剩余 29 个复合要素（crystal/metal/life/...）等待对应 Infused 材料移植后加入映射
- 调试入口：`/pollution aspects`
- 运行期证据：`Mapped 6 aspect materials to Thaumcraft 4R vis channels`（runServer 日志）

**资产工具（Python，默认只读）：**

- `tools/asset_audit.py`：扫描 `docs/reference/legacy-assets`
  - 输出 `docs/reference/asset-inventory.csv`（491 文件，按类别/size/sha1）
  - 输出 `docs/reference/texture-copy-plan.csv`（315 张贴图，`textures/blocks→textures/block`、`textures/items→textures/item`）
  - `--apply` 才会实际复制；不删除任何文件

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

### 2026-09-18 — Phase 0 完成 + Phase 1 起步
- 首次提交 `6a58999`（工程骨架、构建配置、文档、核心类骨架、TC4R 工件占位）
- `gradlew build` 全流程通过（compileJava → processResources → jar → reobfJar）
- 产物：`build/libs/pollution-1.20.1-1.0.0-1.20.1-port.0.1.0.jar`（含 mods.toml 展开、语言文件、KubeJS 插件清单）
- 已落地类：
  - `meowmel.pollution.Pollution`（Forge 47.4 构造器注入、配置注册、命令/衰减监听）
  - `meowmel.pollution.PollutionConfig`（污染开关/倍率/阈值/灵气发电机参数）
  - `meowmel.pollution.api.pollution.PollutionData`（按区块稀疏 `SavedData`，long→double，NBT 存取）
  - `meowmel.pollution.api.pollution.PollutionEngine`（get/add/set/scrub + 200 tick 衰减）
  - `meowmel.pollution.common.command.PollutionCommand`（`/pollution get|set|add|scrub`）
  - `meowmel.pollution.compat.tc4r.TC4RBridge`（`drainVis`、`scrubFlux`、`warpOf` 单点适配）
  - `meowmel.pollution.compat.gtceu.PollutionGTAddon`（`@GTAddon`，GTRegistrate 初始化）
  - `meowmel.pollution.compat.jei.PollutionJeiPlugin`（`@JeiPlugin` 骨架）
  - `meowmel.pollution.compat.kubejs.PollutionKubeJSPlugin`（`kubejs.plugins.txt` 注册）
- 环境说明：`gradle.properties` 内写死本机代理 `127.0.0.1:7890`，换机时需删除或修改

### 2026-09-18 — 魔法材料基础设施
- 核实并采用 GTCEu 7.5.3 的真实注册机制：`MaterialRegistryEvent` + `MaterialEvent`、`buildAndRegister()`、`new Element(...)`
- 移植六要素材料与六种魔法合金（颜色/组分/温度与上游逐项一致），`compileJava` 通过
- 修正：拆弃 `IGTAddon#registerMaterials()`（7.5.3 标记将删除）
- 建立 GT 配方 datagen 钩子 `PollutionRecipes`（已接线，配方待逐条移植）
- 新增 `tools/asset_audit.py`，生成资产清单与贴图复制映射（491 / 315 文件）

### 2026-09-18 — 独立服务端烟测（通过）

方式：后台独立启动 `gradlew runServer`（WMI 创建进程），轮询 `run/logs/latest.log`，验证后关闭。

关键日志证据：

```
[modloading-worker-0/INFO] [meowmel.pollution.Pollution/]: Pollution Unofficial booting: GregTech CEu Modern x Thaumcraft 4R integration
[modloading-worker-0/INFO] [KubeJS/]: Found plugin source pollution
[main/INFO] [meowmel.pollution.Pollution/]: Registered Pollution materials: 6 aspect materials, 6 aspect alloys
[Server thread/INFO]: Done (5.479s)! For help, type "help"
```

结论：GTCEu 7.5.3 + TC4R + JEI + KubeJS + Curios + TerraBlender 与本模组共同加载成功；材料注册与配置落盘正常。

烟测中发现并修复的问题（两个都是真实运行期问题，非编译期）：

1. **Registrate 模块重复**：显式 `runtimeOnly` 的 Registrate/LDLib 与 GTCEu jarJar 内置副本冲突
   → `java.lang.module.ResolutionException: Modules Registrate.MC1._20 and Registrate export package ...`
   → 移除这两个 `runtimeOnly`，仅保留 `compileOnly`
2. **addon registrate 挂错总线**：`initializeAddon()` 运行在 GT 的构造线程上，
   `FMLJavaModLoadingContext.get()` 返回的是 GT 的 bus，导致 addon 矿石方块先于 GT 石材方块注册
   → `NullPointerException: Registry entry not present: gtceu:red_granite`（`gtceu:red_granite_infused_fire_ore`）
   → registrate 改在 `Pollution` 构造器（自己的 mod bus）注册；`initializeAddon()` 不再注册

遗留非致命项：离线模式下 authlib 尝试连接 Mojang 超时（日志有 `Connection reset` 堆栈），
不影响 dedicated server 启动，属于网络环境问题。

### 2026-09-18 — TC4R 要素映射
- 新增 `api/magic/PollutionAspectMapping.java`（材料 ↔ VisChannel ↔ AspectId 双向查询）
- 映射依据上游 `POAspectToGtFluidList` 语义与 TC4R `VisChannel` 枚举，六要素全部对应
- 新增调试命令 `/pollution aspects`
- runServer 复测通过：`Mapped 6 aspect materials ...` → `Done (4.194s)`
