# Pollution Unofficial 1.20.1 移植跟踪文档

> 本文件是持续更新的移植主控文档。每完成一个步骤，都必须在此登记：状态、涉及文件、验证方式、遗留问题。

- 上游项目：`H:\MinecraftMods\Pollution`（Minecraft 1.12.2，GTCEu/GTQT 附属，Thaumcraft 6 时代设计）
- 目标项目：`H:\MinecraftMods\Pollution-Unofficial-1.20.1`
- 目标分支策略：单分支 `main`，阶段完成后提交
- 最近更新：2026-09-18（Phase 4 基本完成、Phase 5 起步；GTCEu 锁定 7.5.3 服务器版本）

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
| Phase 2 | GTCEu 集成：addon 注册、机器排污、消声仓等级、爆炸归因 | 进行中（addon 注册与机器排污基础已落地；消声仓/爆炸归因未开始） |
| Phase 3 | TC4R 集成：灵气抽取/再生、咒波清洗、扭曲联动 | 进行中（灵气抽取、咒波清洗、要素映射已落地；扭曲联动未开始） |
| Phase 4 | 基础机器：灵气发电机、灵气再生机、空气过滤机（单方块） | 基本完成（9 族中 6 族落地，3 族延期见 5.10） |
| Phase 5 | 多方块与材料：魔导系列、魔法合金、催化剂、源质系统 | 进行中（材料已落地；多方块部件起步：VIS_HATCH） |
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
- `api/unification/materials/InfusedMaterials.java`：34 个复合要素材料，颜色/形态/组分与原项目逐项一致（上游每个材料的 aspect tooltip 保留为注释）
- `api/unification/PollutionMaterialEvents.java`：mod bus 事件注册（registry + materials）
- `loaders/recipes/PollutionRecipes.java`：GT 配方 datagen 钩子（`IGTAddon#addRecipes` 已接线，配方待逐条移植）

**移植中丢弃的旧 API（无法在现代 GT 复现，已确认二进制中不存在）：**

| 旧写法 | 原因 |
|---|---|
| `Material#setTooltips(...)` | GTCEu 7.5.3 的 `Material`/`Material.Builder` 均无该方法，aspect 名称改为注释 |
| `GTQTMaterialFlags.GENERATE_BOULE` | GTQT 私有 flag |
| 数字材料 ID 段（startId/END_ID） | 7.5.3 使用 `ResourceLocation` 标识 |

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

- 依据：上游 `POAspectToGtFluidList` 的 Aspect→Infused 材料语义；TC4R 核心要素表
  （TC4R jar 内 `data/thaumcraft/thaumcraft/aspects/default.json`，48 个要素）
- 当前映射：**36 个材料**（6 原素 → vis channel；30 复合 → TC4R 要素 id）
- 4 个上游要素在 TC4R 核心表中不存在，刻意不映射：`ALCHEMY`（无 alkimia）、`SPATIO`/`TEMPUS`/`TINCTURA`（Planar Artifice）
- 两处 TC4R 命名差异已按 TC4R 表处理：上游 `DESIRE` → `lucrum`，上游 `PROTECT` → `tutamen`；上游 `AVERSION` → `telum`
- 运行期用 `AspectApi.contains(...)` 逐项校验，缺失会输出 WARN（当前无告警）
- 调试入口：`/pollution aspects`
- 运行期证据：`Mapped 36 aspect materials to Thaumcraft 4R aspects (6 vis channels)`（runServer 日志）

### 5.5 JEI 兼容性矩阵与决定（2026-09-18，全部实测）

**事实（逐个下载 JEI 版本用 javap 验证，非推测）：**

| JEI 版本 | `ISubtypeInterpreter`（TC4R 需要） | `FluidHelper.getTooltip` 签名（GT 需要 ITooltipBuilder） |
|---|---|---|
| 15.20.0.115 | 无 | ITooltipBuilder ✅ |
| 15.33.0.174 | 无 | ITooltipBuilder ✅ |
| 15.35.0.175 | 无 | ITooltipBuilder ✅ |
| 15.40.0.176 | 无 | List ❌ |
| 15.48.0.177 | 无 | List ❌ |
| 15.55.0.201 | 有 | List ❌ |
| 15.59.0.212（最新） | 有 | List ❌ |

结论：**1.20.1 上不存在同时满足 GTCEu 与 TC4R 的 JEI 版本**。
GTCEu（7.5.3 与 8.0.0 的 mixin 签名相同）与 JEI ≥15.40 会以
`InvalidInjectionException: Invalid descriptor ... Expected (List) but found (ITooltipBuilder)`
硬崩溃（`require = 0` 对描述符不匹配无效）。

**决定：**

1. GTCEu 固定 **7.5.3**（目标服务器实际运行版本；一度升级到 8.0.0 做验证，2026-09-18 已回退。
   JEI 冲突与 GT 版本无关：7.5.3 与 8.0.0 的 `jei.FluidHelperMixin` 同签名。两代 API 差异与适配见 5.11 节）
2. JEI 固定 **15.59.0.212**（满足 TC4R `ISubtypeInterpreter`，满足用户要求）
3. 本工程提供兼容 shim：`pollution.mixins.json`（`priority: 900`，早于 GT 的默认 1000）
   + `mixin/jei/FluidHelperCompatMixin`，向 JEI 的 `FluidHelper` 补回空的
   `getTooltip(ITooltipBuilder, FluidStack, TooltipFlag)` 方法，使 GT 的注入有目标、不再崩溃
   - 功能影响：JEI 15.59 内部走 `List` 重载，GT 额外的流体 tooltip 行不会显示；GT 的 JEI 分类/配方正常
4. mixin 基础设施：MixinGradle 0.7-SNAPSHOT + `annotationProcessor org.spongepowered:mixin:0.8.5:processor`；
   JEI forge 实现 jar 加入 `compileOnly`（AP 需要目标类在编译期可见）
5. 运行验证：`runData` 全流程通过（此前 100% 崩溃点消失）

**datagen 验证结果：**

- `runData` 完成：`All providers took: 795 ms`
- 产物 `src/generated/resources/assets/pollution/lang/en_us.json`：
  46 个 GT 材料键（`material.pollution.*`）+ 5 个手工键（`mod.pollution.name`、`/pollution` 命令反馈）
- `en_ud.json`（滑稽英语）为 JEI/Registrate 标准产物
- 已知环境怪癖：datagen 完成后游戏 JVM 不自行退出（KubeJS/文件监听等非守护线程），
  验证日志出现 `All providers took` 后手动结束进程即可；不影响产物

### 5.6 TC4R 对象要素注册（已落地，2026-09-18）

`api/magic/PollutionObjectAspects.java`：通过 TC4R 官方运行期 API
`ObjectAspectCatalog.registerRuntime(Item, Map<AspectId,Integer>)` 给 Pollution 材料物品注册要素。

**重要事实：上游没有可移植的数值。**
上游 `ThaumcraftModule.registerAspectsToItem(...)` 仅有两处定义、**从未被调用**
（全仓 grep 调用点为 0），因此不存在原项目要素数值可复刻。

**移植设计规则（显式声明为设计决定，参照 TC4R 自带数据量级）：**

| 物品形态 | 要素数值 | 参照 |
|---|---|---|
| 要素材料 gem / dust | 自身要素 4 | TC4R：铁锭 metallum 4 |
| 魔法合金 ingot | 自身原素 3 + metallum 2 | TC4R：金锭 metallum 3 + lucrum 2 |

- 合金亲和：aertitanium→aer、ignissteel→ignis、aquasilver→aqua、terracopper→terra、ordolead→ordo、perditioaluminium→perditio
- 合金不进 `PollutionAspectMapping`（该映射语义为“要素燃料材料”，保持上游语义）
- 矿石方块逐石材类型生成（`pollution:red_granite_*_ore` 等），本轮未注册，按 tag 注册留待后续核实（TODO）
- 运行期证据：`Registered 78 TC4R object aspect entries for Pollution materials`（72 gem/dust + 6 ingot）→ `Done (4.050s)`

### 5.7 调试命令与服务器联调工具（已落地，2026-09-18）

`/pollution` 命令树（需权限等级 2）：

| 命令 | 作用 | API 路径 |
|---|---|---|
| `/pollution get` | 当前区块工业污染 | `PollutionEngine` |
| `/pollution set <amount>` | 设置区块污染 | `PollutionEngine` |
| `/pollution add <amount>` | 增加区块污染 | `PollutionEngine` |
| `/pollution scrub <amount>` | 清洗区块污染 | `PollutionEngine` |
| `/pollution aspects` | 列出材料↔要素映射 | `PollutionAspectMapping` |
| `/pollution vis <channel> [amount]` | 查询（SIMULATE）/抽取（EXECUTE）灵气 | `TC4RBridge.drainVis` |
| `/pollution flux [scrub <quanta>]` | 查询/清洗咒波 | `TC4RBridge.scrubFlux` |

**联调工具**：`tools/rcon_exec.py`（Python，标准库实现 RCON 协议），配套 `run/server.properties`
启用 RCON（`enable-rcon=true`, `rcon.port=25575`, `rcon.password=pollution`）。

**实测记录（RCON 回执，runServer 环境）：**

```
> pollution aspects      -> Aspect mapping (36): infused_air -> aer [AER] ...（36 行）
> pollution get          -> Chunk pollution: 0.0000
> pollution vis aer      -> Drainable 0 AER vis          (SIMULATE)
> pollution vis aer 5    -> Drained 0 AER vis            (EXECUTE)
> pollution flux         -> Scrubbable 0 flux (range 16) (SIMULATE)
> pollution flux scrub 8 -> Scrubbed 0 flux (range 16)   (EXECUTE)
```

数值为 0 属预期：平坦测试世界出生点附近没有 TC4R 灵气节点/咒波；
重点验证了 SIMULATE/EXECUTE 两条代码路径均真实调用 TC4R API 且无异常。

### 5.8 第一台机器：灵气发电机（已落地，2026-09-18）

**语义（对照上游 `MetaTileEntityVisGenerator`，含一处有意修正）：**

| 项 | 上游 1.12.2 | 本移植 |
|---|---|---|
| 能量 | 把能量缓存直接充满（未真正抽灵气）、仅排污 | 通过 `TC4RBridge.drainVis` **真实抽取灵气**后按比例发电 |
| 转换率 | `visGeneratorEuPerVis`（默认 250） | 同配置项，沿用 |
| 污染 | `drainedVis * visGeneratorPollutionMultiplier`（默认 0.1） | 同配置项，写入工业污染系统 `PollutionEngine` |
| 发电速率 | 满容量（异常） | 按等级电压 `V[tier]`，不足 1 灵气量子的部分按 tick 累积 |
| 通道 | TC6 全局灵气 | TC4R 六通道轮询（aer/terra/ignis/aqua/ordo/perditio） |
| 等级 | `AURA_GENERATORS[6]`，tier LV..LuV | 相同 6 级（`lv_vis_generator` .. `luv_vis_generator`） |

**关键实现事实（两个都是真实验证出来的坑）：**

1. **注册时机**：`GTRegistries.MACHINES` 在 GT 的 `CommonProxy.init()`（CommonSetup）里冻结；
   `IGTAddon#initializeAddon()` 在被冻结**之后**调用，addon 机器不能在那里注册。
   模组构造器注册又会让 `GTMachineModels` 提前类初始化（casing 静态表 null key）。
   正确做法：监听 `GTCEuAPI.RegisterEvent`（`MachineDefinition.class` 泛型监听，mod bus），
   该事件在 `GTMachines` 初始化末尾、冻结之前触发。
2. **模型 datagen**：Forge 的 `ExistingFileHelper` **看不到 GTCEu jar 内的模型**，
   因此 GT 的 `tieredHullModel`/`simpleGeneratorModel` 在 addon datagen 中必然报
   `Model at gtceu:block/casings/voltage/lv does not exist` 或卡住。
   解决：机器模型放本模组资源内（`assets/pollution/models/block/machine/vis_generator_<tier>.json`），
   parent 指向 GT 模板（运行期从 GT jar 解析），由
   `tools/generate_machine_models.py` 生成；贴图暂用 GT 电压外壳 + 锅炉正面（占位，TODO 换本模组贴图）。

**验证证据：**

```
runData: All providers took: 1004 ms；(生成 20 个文件，含 6 个等级 blockstate)
对话：Registered Pollution machine definitions
runServer: Done (4.189s)
RCON: setblock 0 -60 0 pollution:lv_vis_generator -> Changed the block at 0, -60, 0
RCON: execute if block ... data get block ... id -> "pollution:lv_vis_generator"
```

**待办**：自定义贴图、UI/状态显示、灵气仓（多方块部件）。

### 5.9 灵气发电机合成配方（已落地，2026-09-18）

**上游配方（`MachineRecipes.muffler()`）：**

```
registerMachineRecipe(AURA_GENERATORS, "ABA", "CHC", "ABA",
        'H', HULL, 'A', MOTOR, 'B', PISTON, 'C', ROTOR)
```

即每级：外壳 ×1 + 马达 ×4 + 活塞 ×2 + 转子 ×2，按等级自动替换部件材料。

**现代实现（与最初“assembler”判断不同，已按上游事实修正）：**
使用 GTCEu 自带的同源辅助方法
`MetaTileEntityLoader.registerMachineRecipe(provider, MachineDefinition[], Object...)`
（形状配方 + `CraftingComponent` 按机器等级解析），模式与上游逐字符一致。

**重要机制修正（已实测）：**
GTCEu Modern（实测于 8.0.0；7.5.3 下机制相同待复验）**不再通过 `runData` 生成配方 JSON**。`GTRecipes.recipeAddition` 在
common setup 被调用并写入内置动态数据包（`GTDynamicDataPack::addRecipe`），
因此 `IGTAddon#addRecipes` 是**服务器运行时**回调，配方不会出现在
`src/generated/resources/data/`。验证方式改为启动服务器看日志。

**验证证据：**

```
Registered Pollution machine definitions
Registered 6 vis generator crafting recipes
Done (4.274s)! For help, type "help"
```

无我方配方错误；日志中仅存在已知的 authlib 网络错误与 TC4R 自身
`thaumcraft:compat/native_*_cluster_smelting` 空输出告警（均与本次改动无关）。

**资产工具（Python，默认只读）：**

- `tools/asset_audit.py`：扫描 `docs/reference/legacy-assets`
  - 输出 `docs/reference/asset-inventory.csv`（491 文件，按类别/size/sha1）
  - 输出 `docs/reference/texture-copy-plan.csv`（315 张贴图，`textures/blocks→textures/block`、`textures/items→textures/item`）
  - `--apply` 才会实际复制；不删除任何文件

### 5.10 机器移植总览与规划（全部机器，2026-09-18）

**单方块机器（upstream `common/metatileentity/single`，9 类）：**

| 上游类 | 数量 | 状态 | 移植说明 |
|---|---|---|---|
| MetaTileEntityVisGenerator | 6 (LV..LuV) | ✅ 已完成 | 真实抽 vis 发电 + 工业污染 |
| MetaTileEntityVisProvider | 9 (LV..UHV) | ✅ 已完成 | TC4R 无环境灵气，改为 `NodeApi` CAS 给最近普通节点充能 |
| MetaTileEntityMagicEnergyAbsorber | 5 (LV..IV) | ✅ 已完成 | 龙蛋基座发电；Botania 水晶/盖亚头基座 TODO（Phase 6） |
| MetaTileEntityFluxClear (VIS_CLEAR) | 4+2（含重复 ID） | ✅ 已完成（去重为 5 档） | EU 清洗咒波；上游过滤物品未移植（物品阶段），暂 EU-only |
| MetaTileEntityFluxPromotedFuelCell | 5 (LV..IV) | ✅ 已完成 | 咒波发电 + 效率区间 + 超上限爆炸；GTQT 燃料配方 UI 不移植 |
| MetaTileEntitySolarPlate | 18（3 档 × 6 种） | ✅ 已完成 | 光照/维度/高度条件与增产一致；天空 7×7 扫描简化为机顶判定；上游注魔配方待 TC4R 注魔阶段 |
| MetaTileEntitySmallNodeGenerator | 4 (LuV..UHV) | ⏸ 延期 | 依赖 `PACKAGED_AURA_NODE` 物品（物品阶段） |
| MetaTileEntitySourceCharge | 1 | ⏸ 延期 | 依赖魔力饰品物品与灌注流体映射（物品阶段） |
| ManaGeneratorTileEntity | 5 | ⏸ 延期 | Botania 魔力（Phase 6） |

本轮新增 48 个机器定义（generator 6 + provider 9 + absorber 5 + scrubber 5 + fuel cell 5 + solar 18），
一次性 `compileJava` 通过，`runServer` 全部注册，RCON 抽查 6 台可放置且 block entity 正常。
（注：上述 runServer/RCON 验证是在 GTCEu 8.0.0 下完成的；回退 7.5.3 后需复跑运行验证，用户已要求暂缓。）

后续新增：VIS_HATCH 9 + INFUSED_FLUID_HATCH 9 + FLUX_MUFFLER 9（见下方部件进度），机器定义累计 75。

**多方块部件（upstream `multiblockpart`，20 个源文件）— 规划与进度：**

1. [x] **VIS_HATCH（首个部件）**：`VisHatchMachine` + `IVisHatch`（`common/machine/part`、`api/capability`）
   - 等级 LV..UHV（9 档；上游注册 14 档到 MAX，超出档位待后续）
   - 每 20 tick 从 TC4R 灵气网络抽 5 centivis（六通道轮询），成功则存 `tier` 单位，容量 `tier * 2000`
     （对齐上游语义：抽 0.05 vis → 存 tier 单位）
   - 持久化用 7.5.3 的 `saveCustomPersistedData/loadCustomPersistedData`
   - `PartAbility` 自定义为 `pollution_vis_hatch`，供控制器收集
   - 配方：上游 `"ABA"/"CHC"/"ABA"`（H 外壳、A 传送带、B 电路、C 发射器）
   - 状态：代码落地 + 占位模型 + 语言键，GTCEu 7.5.3 下 `compileJava` 通过；运行验证待复验（用户要求暂缓烟测）
2. [x] **INFUSED_FLUID_HATCH（灌注流体仓）**：`InfusedFluidHatchMachine` extends `TieredPartMachine` +
   `NotifiableFluidTank`（1 罐，`8000 << min(9, tier)` mB，IO.BOTH）。上游的 1 输入/1 输出流体容器槽待物品阶段。
   等级 LV..UHV（9 档，上游 14）；能力 `POMultiblockAbility.INFUSED_FLUID_HATCH`
3. [x] **FLUX_MUFFLER（魔法消声仓）**：`FluxMufflerMachine implements IMufflerMachine`
   - 回收概率 `min((tier-1)*10, 100)`（上游公式，区别于 GT 的 `tier*10`）
   - 回收仓 `(1 + min(UHV, tier))^2` 槽；`recoverItemsTable` 按概率逐格 `insertItemStacked`
   - 上游 `getPollutionAmount()=0` 语义：消声仓回收本身不增加工业污染（现代 GT 已移除该钩子，注释记录）
   - 能力使用 GT 标准 `PartAbility.MUFFLER`，可被现有 GT 多方块识别
4. [x] **MagicItemHatch 基类**：`MagicItemHatchMachine`（抽象，focus 槽 + 过滤 + 锁定；`NotifiableItemStackHandler`
   暴露物品能力）。具体子类（Tarot 等）随对应联动阶段；本轮不注册机器定义
5. [ ] 容器类：`ManaContainer`、`VisContainer`（当前 VIS_HATCH 用整型字段替代 `VisContainer`，待魔法配方系统落地后再评估是否需要独立容器抽象）
6. [ ] 外部模组部件延期：`ManaHatch`/`ManaPoolHatch`/无线款式（Botania，Phase 6）、`BloodMagicHatch`（Phase 6）、`AstralLensHatch`/`TarotHatch`（Phase 6）、BM-HPCA 系列 5 个（Phase 6）

**魔法多方块（upstream `multiblock` 19 类 + `multiblock/magic` 18 类 + `multiblock/generator` 3 类）— 规划：**

1. **前置：魔法配方系统**。上游用 `MagicRecipeProperties`（配方属性：催化剂、灵气并行、注魔流体、星辉条件等）与 `MagicMultiblockRecipeLogic`。需在本工程复刻一套等价的 `api/recipes` 属性与逻辑层（可用 GT `RecipeModifier` + 自定义 `RecipeLogic` 实现），这是所有魔导多块的共同前置。
2. **第一批（加工型，机制最接近 GT 原版）**：MagicBender / MagicCentrifuge / MagicElectrolyzer / MagicMixer / MagicMacerator / MagicWireMill / MagicExtruder / MagicSolidifier / MagicCutter / MagicSifter / MagicChemicalBath —— 复用 GT 对应配方类型 + 魔法外壳/等级。
3. **第二批（高级加工）**：MagicElectricBlastFurnace / MagicChemicalReactor / MagicDistillery / MagicBrewery / MagicAutoclave / MagicAlloyBlastSmelter / MagicAssembler / MagicGreenHouse。
4. **第三批（独立特殊机器）**：IndustrialInfusion（GT 化注魔，需 TC4R 注魔对接）、MagicBattery（大储电）、InfusedExchange（要素转换）、CentralVisTower / LargeNodeGenerator / NodeWasher / NodeProducer / NodeBlastFurnace / NodeFusionReactor / StarstreamNexus*（节点体系，基于 `AuraNodeView`/`NodeApi`）、EssenceCollector / EssenceSmelter / GtEssenceSmelter（源质体系，需 TC4R essentia API 适配层）、SmallChemicalPlant / MultiDanDeLifeOn（特殊逻辑）。
5. **多方块发电机**：MagicTurbine / MagicLargeTurbine / MagicMegaTurbine——用 GT `SimpleGeneratorMachine` 同款机制 + 自定义燃料类型（基于要素流体），需先确定燃料表（上游 `MagicTurbineType`）。
6. **储罐**：AspectTank（要素储罐，需要素存储 + UI）。

**依赖与风险速记：**

- 魔导多块的第一步是魔法配方系统，否则只能做空壳
- 源质/节点体系务必走 `compat/tc4r` 适配层
- 外部联动部件（魔力/血魔法/星辉/塔罗）统一放到 Phase 6，避免污染核心构建

### 5.11 GTCEu 锁回 7.5.3 与两代 API 差异（2026-09-18）

**背景：** 目标服务器实际运行 **GTCEu Modern 7.5.3**，依赖已固定回 7.5.3（`gradle.properties`），
一度升级的 8.0.0 已回退。JEI 冲突与 GT 版本无关（7.5.3 的 `jei.FluidHelperMixin` 与 8.0.0 同签名），
`FluidHelperCompatMixin` shim 继续适用（见 5.5 节）。

**两代 API 差异与适配（7.5.3 各点均已用发布 jar `javap` 核实）：**

| 事项 | 7.5.3（现行） | 8.0.0（已弃用） | 本工程处理 |
|---|---|---|---|
| 机器构造器 | `IMachineBlockEntity holder` | `BlockEntityCreationInfo info` | 全部机器类改为 `IMachineBlockEntity` |
| 坐标读取 | `MetaMachine#getPos()` | `getBlockPos()` | 全部改回 `getPos()` |
| 持久化 | `saveCustomPersistedData(CompoundTag, boolean)` / `loadCustomPersistedData(CompoundTag)` | `@SaveField` 注解 | `VisHatchMachine` 用前者；其余机器暂无持久字段 |
| 注册器 | `GTRegistrate#machine(String, Function<IMachineBlockEntity, MetaMachine>)` | `Function<BlockEntityCreationInfo, ...>` | 方法引用/λ 自动匹配 |
| 分级注册 | `GTMachineUtils.registerTieredMachines(...)` factory 为 `BiFunction<IMachineBlockEntity, Integer, MetaMachine>` | 同位置为 `BlockEntityCreationInfo` | 方法引用自动匹配 |
| `PartAbility` | `new PartAbility(String)` 可用 | 同 | 同 |
| 能量机器 | `TieredEnergyMachine(IMachineBlockEntity, int, Object...)`、`isEnergyEmitter()` protected | `TieredEnergyMachine(BlockEntityCreationInfo, int)` | 构造器已适配 |

**验证：** `gradlew compileJava` 在 7.5.3 下通过（2026-09-18）。
此前在 8.0.0 下完成的 runServer/runData/RCON 证据需在 7.5.3 下复跑一轮（用户要求暂缓烟测）。

### 5.12 神秘侧缺口盘点（2026-09-18，按类名匹配重新扫描）

**总量：** 上游 `src/main/java` 共 **423** 个源文件；按类名（归一化：去 `MetaTileEntity` 前缀、
去 `Machine`/`TileEntity` 后缀）匹配，已覆盖 **11**，**未移植 412**。
其中按内容关键字（`tc4port|thaum|aspect|aura|vis|flux|warp|infus|essentia|node|magic` 等）
扫描命中的未移植类 **164** 个；扣除 2 个改名已覆盖
（`MetaTileEntityFluxClear`→`FluxScrubberMachine`、`MetaTileEntityFluxPromotedFuelCell`→`FluxFuelCellMachine`）后，
**魔法/TC 侧实际缺口约 162 个类**。

**机器体系缺口（metatileentity 包系，共 91 个源文件，已覆盖 5，缺口 86）：**

| 区域 | 上游位置 | 缺口 | 说明 |
|---|---|---|---|
| 单方块剩余 | `common/metatileentity/single` | 3 | SmallNodeGenerator（需 `PACKAGED_AURA_NODE` 物品）、SourceCharge（需魔力饰品/灌注流体）、ManaGenerator（Botania） |
| 魔导多块 19 台 | `.../multiblock/magic` | 19 | 前置：魔法配方系统（`MagicRecipeProperties`/`MagicMultiblockRecipeLogic`/`MagicRecipeMapMultiblockController`/`POMultiblockAbility`/`PORecipeMaps`） |
| 多块发电机 | `.../multiblock/generator` | 6 | `MagicTurbineType`、Large/MegaTurbine、`MultiDanDeLifeOn`、2 个 WorkableHandler；前置燃料表（`MagicFuelRecipes`） |
| 多块特殊 | `.../multiblock` 顶层 | 17 | CentralVisTower、LargeNodeGenerator、NodeWasher/Producer/BlastFurnace/FusionReactor、IndustrialInfusion、InfusedExchange、MagicBattery、EssenceCollector/EssenceSmelter/GtEssenceSmelter、SmallChemicalPlant、EndoflameArray、MagicFusionReactor、MegaManaTurbine |
| 多方块部件 | `.../multiblockpart` | 19 | VIS_HATCH ✅（本轮）；INFUSED_FLUID_HATCH / FluxMuffler / MagicItemHatch 排队；容器与外部联动（Mana/Blood/Astral/Tarot/BM-HPCA/无线）延期 |
| 其余基类 | `.../metatileentity` 顶层等 | ~2 | 抽象类/管理器，随机器阶段处理 |

（注：`multiblock` 目录含子目录共 60 个源文件缺口，上表拆分出 magic 19 + generator 6 + 顶层 17，
其余为子目录中的辅助类——合计口径以 60 为准。）

**其余大类缺口（含大量非魔法内容）：** `common/block` 55、`dimension` 47、`loaders` 32
（其中 `loaders/recipes` 29：含 `AERecipes`/`BloodAltar`/`BotaniaRecipes`/`CompoundAspectRecipes`/
`MagicChemicalRecipes`/`MagicFuelRecipes`/`MagicGCYMRecipes`/`MagicHatchRecipes`/`NodeFusionRecipes`/
`StarstreamNexusRecipes`/`ThaumcraftRecipes` 等）、`common/items` 19（含 `PollutionMetaItems`、
`GogglesNano/Quantum`、`Tarots`、`PollutionBaubles`）、`common/entity` 15（含 Basalz/Blitz/Blizz）、
`common/warpevent` 36、`api` 63、客户端（`gui`/`tesr`/`aspect` 渲染）约 20。

**TC4R 特性替换策略（只列已核实 API；未核实项显式标注）：**

| 上游（TC6 时代）依赖 | TC4R（1.20.1）替换 | 状态 |
|---|---|---|
| `AuraHelper` 环境灵气读写 | `VisNetworkApi.drain`（SIMULATE/EXECUTE）+ `NodeApi` CAS 充能 | 已用于 VisGenerator/VisProvider/VisHatch |
| AspectList / TC6 aspects | `AspectId` / `AspectApi` / `NodeVis` / `AuraNodeState` | 已用于要素映射与对象要素 |
| 咒波/污染清洗 | `FluxApi.consumeNearby` + `FluxConsumeContext.machine` | 已用于 FluxScrubber/FluxFuelCell |
| 扭曲读写 | `PlayerWarpApi.view(Player)`（只读视图，已核实） | 待扭曲事件阶段接入；TC4R 是否提供可取消 warp 事件 API **待核实** |
| `ThaumcraftApi` 研究/注魔注册 | TC4R 数据驱动（jar 内 research/infusion JSON）；运行期 API **待核实** | IndustrialInfusion 前置 |
| 源质 Essentia 传输 | `GTEssentiaHandler`/`EssenceCollector` 系列需 TC4R essentia API —— **待核实** | 源质体系阶段 |
| 上游自定义网络（`MeowmelNetwork` 及包） | 现代 Forge `SimpleChannel`（1.20.1）重写 | 网络阶段统一处理 |
| 上游 Mixin（`CommonInternalsMixin`/`ConfigAspectsMixin`/`ThaumcraftApiHelperMixin`） | TC4R 数据驱动后多数不再需要 | 逐个评估，能删则删 |

**结论：** 神秘侧剩余约 **162 类**，主体是机器体系（单方块 3 + 多方块 60 + 部件 19），
其次扭曲事件 36 类与配方/材料辅助类。推进顺序保持 5.10 节既定：
部件框架 → 魔法配方系统 → 魔导多块三批 → 节点/源质体系 → 扭曲事件。

### 5.13 魔法配方系统现代化方案与进度（2026-09-18）

**上游结构（1.12 GTCEu）：** `MagicRecipeProperties`（RecipeProperty 注册表）→ `MagicMultiblockRecipeLogic`
（`MultiblockRecipeLogic` 子类，vis/魔力/源质/星辉/塔罗资源与增幅系统）→ `MagicRecipeMapMultiblockController`
（资源仓收集与消耗）→ `PORecipeMaps`（魔导 RecipeMap 注册）→ 19 台魔导多块。

**现代 GTCEu 7.5.3 的关键差异（已用发布 jar 核实）：**

| 事项 | 上游（1.12 GTCEu） | 7.5.3 实际 | 现代化方案 |
|---|---|---|---|
| 配方自定义属性 | `GregTechAPI.RECIPE_PROPERTIES` + `RecipeProperty` 子类 | 无属性注册表；`GTRecipe.data`（CompoundTag）是官方扩展点 | 键名保留上游字符串（`pollution.magic.*`），`MagicRecipeProperties` 改为 data 读写 + 构建器 helper |
| 多方块逻辑基类 | `MultiblockRecipeLogic`（独立类） | **不存在**；`WorkableMultiblockMachine` + `RecipeLogic`（`api/machine/trait/RecipeLogic`） | `MagicRecipeLogic` 需基于 `RecipeLogic` 重建（下轮开始） |
| 配方构建 | `RecipeBuilder#applyProperty` | `GTRecipeBuilder#addData(String, int/long/String/...)` | 已封装进 `MagicRecipeProperties` builder helpers |
| 配方类型 | `RecipeMap` | `GTRecipeType` | `PORecipeMaps` 待基于 `GTRecipeType` builder 重建 |

**已落地（本轮）：**

- `api/recipes/properties/MagicRecipeProperties.java`（现代化重写）：
  - TC 面键已接线：`VIS_PER_CRAFT`、`INFUSED_FLUID_PER_TICK`、`THAUMCRAFT_RESEARCH`（+ builder/ getter）
  - 数字-only 键一并提供构建器：`MANA_PER_TICK`、`LIFE_ESSENCE_PER_TICK`、`TAROT`、`CONSUMABLE_CATALYST_INPUTS`
  - 延期系统键保留上游名字（ASTRAL/PROCESS_TAG...），避免未来配方不兼容；JEI 展示待现代 GT 配方信息 API 调研
- `api/capability/ICleanVis.java`（上游原样：干净灵气标记接口）
- 部件批次（见 5.10）：INFUSED_FLUID_HATCH、FLUX_MUFFLER、MagicItemHatch 基类

**逻辑层与配方类型（本轮落地，全部 7.5.3 编译通过）：**

- `common/machine/multiblock/MagicMultiblockController.java`（上游 `MagicRecipeMapMultiblockController` 的 TC 子集）：
  - `extends WorkableMultiblockMachine`；`createRecipeLogic` 注入 `MagicRecipeLogic`
  - `onStructureFormed` 从 `getParts()` 收集 `IVisHatch` 与 `InfusedFluidHatchMachine`
  - `consumeVis(int,boolean)` / `drainInfusedFluid(int,boolean)`（后者校验流体属于 `PollutionAspectMapping` 映射材料）
  - `checkMagicRequirements(GTRecipe)`：mana/life/astral/tarot 配方在对应仓未移植前直接判失败（与上游“缺仓即失败”语义一致）
  - `consumeMana`/`consumeLifeEssence` 预留（恒 `amount<=0`）
- `common/machine/multiblock/MagicRecipeLogic.java`（上游 `MagicMultiblockRecipeLogic` 的 TC 子集）：
  - `checkRecipe`：额外校验 `checkMagicRequirements` + vis 可支付（SIMULATE）
  - `handleTickRecipe`：每 tick 扣灌注流体（先 SIMULATE 后 EXECUTE）；vis 每 craft 只扣一次（`visPaidThisCraft`）
  - `setupRecipe`/`onRecipeFinish`/`resetRecipeLogic` 清理付费状态
  - 增幅/塔罗/星辉/晶体变换等上游扩展明确不在 v1 范围
- `api/recipes/PORecipeMaps.java`（现代化重写）：
  - 键名与上游逐字一致（`magic_blast_smelter`/`stove`/`magic_fusion_reactor`/`magic_chemical_reactor`/
    `magic_assembler`/`magic_greenhouse`/`magic_turbine`/`forge_alchemy`/`node_magic_fusion`/
    `industrial_infusion_recipes`），使用 `GTRecipeTypes.register(name, "pollution")` + `setMaxIOSize`/`setEUIO`
  - Botania/Astral/星辉/指南类地图按其系统延期；`MagicPropertyRecipeUI` 等 UI 待现代配方 UI pass

**7.5.3 逻辑层 API 事实（已 javap 核实，下一轮直接据此实现）：**

| 事项 | 7.5.3 实际 |
|---|---|
| 控制器基类 | `WorkableMultiblockMachine(IMachineBlockEntity, Object...)`，持有 `public final RecipeLogic recipeLogic`；`protected RecipeLogic createRecipeLogic(Object...)` 为注入点；`onStructureFormed/onStructureInvalid` 可覆写 |
| 逻辑基类 | `RecipeLogic(IRecipeLogicMachine)`；`machine` 字段公开 |
| 逻辑钩子 | `protected ActionResult checkRecipe(GTRecipe)`；`public ActionResult handleTickRecipe(GTRecipe)`（每 tick，资源扣费应在此）；`public void onRecipeFinish()`；`protected void regressRecipe()`；`resetRecipeLogic()`；`markLastRecipeDirty()` |
| 失败返回 | `ActionResult.SUCCESS` / `ActionResult.fail(Component, RecipeCapability<?>, IO)` / `FAIL_NO_REASON` / `FAIL_NO_CAPABILITIES` |
| 控制器接口 | `IRecipeLogicMachine`：`getRecipeTypes()/getRecipeType()/getActiveRecipeType()/setActiveRecipeType(int)` 等 |
| 配方查询 | `RecipeLogic#searchRecipe()`/`findAndHandleRecipe()`/`handleSearchingRecipes(...)`；`GTRecipe` 经 `GTRecipeType` 数据管理器 |

**下一步（TC 关键路径）：**

1. `MagicRecipeLogic extends RecipeLogic`：在 `checkRecipe` 做研究门槛/vis 可支付校验，在 `handleTickRecipe` 扣
   vis（每 craft 一次）+ infused fluid（每 tick），`onRecipeFinish` 清理状态；先做 TC 子集，
   增幅/塔罗/星辉部分延后
2. ~~`MagicMultiblockController extends WorkableMultiblockMachine`~~ ✅（本轮完成，见上）
3. ~~`PORecipeMaps` 基于 `GTRecipeType` 重建~~ ✅（本轮完成 TC 子集）
4. **外壳块（本轮完成）**：上游 3 个变体块拆分为 26 个独立方块（`PollutionMagicBlocks`，
   现代 GT 无 addon 变体块助手）：`spell_prism_*` 9 + `void_prism`/`alloy_blast_casing`/`magic_battery_casing`、
   `beam_core_0..4` + `filter_1..5`、`laminated_glass` 系 5；占位材质（金属用 GT 电压外壳纹理、
   玻璃用原版玻璃），`tools/generate_casing_assets.py` 生成 blockstate/模型/物品模型；
   中英文语言键已加（en 走 datagen provider、zh_cn 手工文件）
   - 与上游差异：方块 id 由 `pollution:magic_block[variant=...]` 变为每变体独立 id（已记录）
5. **下一步：魔导多块机器**（19 台）。7.5.3 结构 API 已核实：
   `MultiblockMachineBuilder.pattern(Function<MultiblockMachineDefinition, BlockPattern>)` +
   `FactoryBlockPattern`/`Predicates`/`TraceabilityPredicate`（`api/pattern`）；
   机器定义需 `recipeType(s)`（由 `getDefinition().getRecipeTypes()` 自动注入 `WorkableMultiblockMachine`）+
   `workableCasingModel`/贴图 + `recoveryItems`（消声仓）
   - 每台机器上游还实现 `getMaterial()`（该机器对应的 Infused 材料）与 `canBeDistinct()`
   - 上游结构里还用到多方块部件能力匹配（`Elements.hatch(...)` → 现代 `Predicates.abilities(...)`）
5. **PORecipeMaps 的注册时机风险**：`GTRecipeTypes.register` 在 addon 静态初始化时执行，
   需在 GT 注册冻结前（`RegisterEvent` 流程内被机器类首次引用），运行期复验时重点检查
6. TC 配方数据（`AERecipes`/`ThaumcraftRecipes`/`NodeFusionRecipes` 等，按机器阶段逐批）
2. `MagicRecipeMapMultiblockController`：仓口收集（`POMultiblockAbility.VIS_HATCH`/`INFUSED_FLUID_HATCH`）与消耗 API
3. `PORecipeMaps`：基于 `GTRecipeType` 重建魔导配方类型
4. 19 台魔导多块 + 节点/源质/注魔系列
5. TC 配方数据（`AERecipes`/`ThaumcraftRecipes`/`NodeFusionRecipes` 等，按机器阶段逐批）

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

### 2026-09-18 — 复合要素材料（34 个）
- 移植 `InfusedMaterials`：vitreus/victus/mortuus/spiritus/telum/metallum/potentia/instrumentum/
  permutatio/praecantatio/alchemia/gelum/auram/lux/fabrico/vacuos/motus/vitium/tenebrae/alienis/
  volatus/herba/machina/vinculum/exanimis/cognitio/sensus/bestia/humanus/lucrum/tutamen/spatium/tempus/tinctura
- 颜色/形态/1:1 组分与原项目逐项一致；上游 `setTooltips` 无现代等价 API，改为注释记录
- 映射扩展至 36 项并用 `AspectApi.contains` 运行期校验（36/36 命中，无 WARN）
- runServer 证据：`Mapped 36 aspect materials to Thaumcraft 4R aspects (6 vis channels)` → `Done (4.451s)`

### 2026-09-18 — JEI 冲突定位与兼容层 + datagen 通过
- 发现并实测 JEI 15.40+ 与 GTCEu（7.5.3/8.0.0）硬冲突（FluidHelper 描述符不匹配）
- GTCEu 一度升级 8.0.0（后于本日回退至 7.5.3，见 5.11 节）；JEI 保持 15.59.0.212；新增 mixin 兼容 shim（priority 900）
- 新增 mixin 构建基础设施（MixinGradle 0.7-SNAPSHOT + mixin AP 0.8.5）
- `gradlew build` 通过；jar 内含 `pollution.mixins.json`、`pollution.refmap.json`、`MixinConfigs` 清单
- `runData` 通过并生成 46 个材料语言键 + 手工键（见 5.5 节）

### 2026-09-18 — TC4R 对象要素注册
- 核实上游 `registerAspectsToItem` 为死代码（0 调用点），无上游数值；采用显式设计规则（见 5.6 节）
- 新增 `api/magic/PollutionObjectAspects` + `PollutionMagicEvents`（FMLCommonSetup 触发）
- 注册 78 条（36 要素材料 × gem/dust + 6 合金 ingot），runServer 验证通过
- 观察到 TC4R 与 GT 的兼容配方告警（`thaumcraft:compat/native_*_cluster_smelting` 输出为空），
  属 TC4R 端口自身与 GT 的集成瑕疵（当时在 8.0.0 下观察），不影响本模组；回退 7.5.3 后待复验

### 2026-09-18 — TC4R 桥接可执行化 + RCON 联调
- `TC4RBridge` 增加 SIMULATE/EXECUTE 重载（vis 查询/抽取、flux 查询/清洗）
- `/pollution vis`、`/pollution flux` 命令落地（见 5.7 节）
- 新增 `tools/rcon_exec.py`，RCON 实测全部命令回执正常（6/6）

### 2026-09-18 — 第一台魔法机器（灵气发电机）
- `VisGeneratorMachine`（真实抽灵气发电 + 排污）、`PollutionMachines`、`PollutionMachineEvents`
- 踩坑与修正：addon 机器注册必须走 `GTCEuAPI.RegisterEvent`（见 5.8 节）；datagen 无法引用 GT jar 内模型，
  改为自持模型 + Python 生成器（`tools/generate_machine_models.py`）
- 6 个等级（LV..LuV）注册、datagen、runServer、RCON 放置验证全部通过

### 2026-09-18 — 灵气发电机配方
- `PollutionRecipes` 落地六级形状配方，模式与上游 `MachineRecipes` 完全一致
  （`MetaTileEntityLoader.registerMachineRecipe` + `GTCraftingComponents`）
- 机制修正：GTCEu 8.0.0 配方为运行时动态数据包，非 datagen JSON（见 5.9 节）
- runServer 验证：`Registered 6 vis generator crafting recipes` → `Done (4.274s)`

### 2026-09-18 — 全部单方块机器推进 + 一次性编译验证
- 新增 5 台机器类：`VisProviderMachine`、`MagicEnergyAbsorberMachine`、`FluxScrubberMachine`、
  `FluxFuelCellMachine`、`SolarPlateMachine`（+ 共用基类 `PollutionEnergyMachine`）
- 注册 48 个新机器定义：provider 9 + absorber 5 + scrubber 5 + fuel cell 5 + solar 18
  （含 48 个占位模型，由 `tools/generate_machine_models.py` 生成）
- 配方全部按上游模式落地（`CIRCUIT` 替代未移植的 `BLANKCORE`，已注释说明）
- 一次性 `compileJava` 通过；`runServer` 全部注册 + `Done (4.330s)`；
  RCON 抽查 6 台机器 setblock 与 block entity ID 正常
- 延期机器与全部多方块的详细规划见 5.10 节（SmallNodeGenerator/SourceCharge/ManaGenerator、
  部件框架、魔导系列三批、发电机、储罐）

### 2026-09-18 — GTCEu 回退 7.5.3（服务器实际版本）+ 首个多方块部件 VIS_HATCH
- 依赖回退：`gtceu_version=8.0.0` → `7.5.3`（用户确认服务器版本；差异与适配见 5.11 节）
  - 全部机器类构造器 `BlockEntityCreationInfo` → `IMachineBlockEntity`、`getBlockPos()` → `getPos()`
  - `VisHatchMachine` 持久化改用 7.5.3 的 `saveCustomPersistedData/loadCustomPersistedData`
  - `gradlew compileJava` 在 7.5.3 下通过；运行验证需复跑（用户要求暂缓烟测）
- 新增首个多方块部件 `VIS_HATCH`（LV..UHV 9 档）：`VisHatchMachine` + `IVisHatch` + 自定义
  `PartAbility`（`pollution_vis_hatch`）+ 占位模型（Python 生成器扩展）+ 上游形状配方 + 语言键
- 完成神秘侧缺口盘点（见 5.12 节）：未移植 412 类，其中魔法/TC 相关约 162 类

### 2026-09-18 — TC 部件批次 + 魔法配方系统起步
- 新增 3 个部件类 + 18 个机器定义（累计 75）：`InfusedFluidHatchMachine`（9 档）、
  `FluxMufflerMachine`（9 档，上游回收概率公式）、`MagicItemHatchMachine`（抽象基类）
- 新增 `POMultiblockAbility`（自定义 `PartAbility`，经 `MachineBuilder#abilities` 自动注册方块）
- `MagicRecipeProperties` 现代化重写（GT 7.5.3 无 RecipeProperty 注册表 → `GTRecipe.data`），
  TC 键全部接线；新增 `ICleanVis`
- `compileJava` 通过（7.5.3）；魔法配方系统整体方案与下一步见 5.13 节

### 2026-09-18 — 魔法配方逻辑层 + 配方类型
- 新增 `MagicMultiblockController`（资源仓收集/扣费/需求校验）与 `MagicRecipeLogic`
  （vis 每 craft 一次、灌注流体每 tick、失败原因 lang）——现代 `RecipeLogic` 钩子（见 5.13）
- 新增 `PORecipeMaps`（10 个 TC 面配方类型，键名与上游一致；`GTRecipeTypes.register` + `setMaxIOSize`/`setEUIO`）
- 核实 7.5.3 结构 API：`MultiblockMachineBuilder.pattern` + `FactoryBlockPattern`/`Predicates`；
  魔导多块下一步需要自定义壳体块（SPELL_PRISM/BEAM_CORE/BAMINATED_GLASS）
- `compileJava` 在 7.5.3 下通过
