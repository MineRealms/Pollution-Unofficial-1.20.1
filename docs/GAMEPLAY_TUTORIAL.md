# Pollution 非官方 1.20.1 移植版 —— 玩法教程

> 本文严格依据当前源码编写（Forge 1.20.1 / GTCEu Modern 7.5.3 / Thaumcraft 4R 依赖）。
> 每条机制后标注来源，格式为 `文件名.java:行号`。**未实现或代码与说明不一致的地方会明确写出“未实现 / 偏差 / 疑似缺口”**，不做脑补。
>
> 文件索引（相对 `src/main/java/meowmel/pollution/`）：
>
> | 简称 | 路径 |
> |---|---|
> | `PollutionConfig.java` | `PollutionConfig.java` |
> | `VisGeneratorMachine.java` | `common/machine/VisGeneratorMachine.java` |
> | `VisProviderMachine.java` | `common/machine/single/VisProviderMachine.java` |
> | `SmallNodeGeneratorMachine.java` | `common/machine/single/SmallNodeGeneratorMachine.java` |
> | `NodeProducerMachine.java` | `common/machine/multiblock/node/NodeProducerMachine.java` |
> | `LargeNodeGeneratorMachine.java` | `common/machine/multiblock/node/LargeNodeGeneratorMachine.java` |
> | `NodeWasherMachine.java` | `common/machine/multiblock/node/NodeWasherMachine.java` |
> | `NodeBlastFurnaceMachine.java` | `common/machine/multiblock/node/NodeBlastFurnaceMachine.java` |
> | `NodeBlastFurnacePatterns.java` | `common/machine/multiblock/node/NodeBlastFurnacePatterns.java` |
> | `NodeFusionReactorMachine.java` | `common/machine/multiblock/node/NodeFusionReactorMachine.java` |
> | `NodeFusionReactorPatterns.java` | `common/machine/multiblock/node/NodeFusionReactorPatterns.java` |
> | `CentralVisTowerMachine.java` | `common/machine/multiblock/node/CentralVisTowerMachine.java` |
> | `CentralVisTowerPatterns.java` | `common/machine/multiblock/node/CentralVisTowerPatterns.java` |
> | `PackagedAuraNode.java` / `PackagedAuraNodeItem.java` | `common/item/` |
> | `EssenceSmelterMachine.java` / `GtEssenceSmelterMachine.java` | `common/machine/multiblock/magic/` |
> | `EssenceCollectorMachine.java` / `EssenceCollectorPatterns.java` | `common/machine/multiblock/magic/` |
> | `InfusedExchangeMachine.java` | `common/machine/multiblock/magic/` |
> | `AspectTankMachine.java` / `AspectTankBlockEntity.java` | `common/machine/single/` |
> | `ManaGeneratorMachine.java` | `common/machine/single/` |
> | `ManaPlateMachine.java` | `common/machine/multiblock/botania/` |
> | `EndoflameArrayMachine.java` | `common/machine/multiblock/botania/` |
> | `MegaManaTurbineMachine.java` | `common/machine/multiblock/botania/` |
> | `MultiDanDeLifeOnMachine.java` | `common/machine/multiblock/botania/` |
> | `ManaHatchMachine.java` / `ManaPoolHatchMachine.java` / `NotifiableManaContainer.java` | `common/machine/part/mana/` |
> | `MagicLargeTurbineMachine.java` / `MagicMegaTurbineMachine.java` | `common/machine/multiblock/magic/` |
> | `MagicRecipeLogic.java` / `MagicMultiblockController.java` | `common/machine/multiblock/` |
> | `MagicFuelRecipes.java` / `CompoundAspectRecipes.java` / `ManaToEuRecipes.java` | `loaders/recipes/` |
> | `PollutionEngine.java` / `MachinePollution.java` / `PollutionData.java` | `api/pollution/` |
> | `PollutionAspectMapping.java` | `api/magic/` |
> | `TC4RBridge.java` | `compat/tc4r/` |
> | `GTEssentiaHandler.java` | `common/lib/` |
> | `PollutionMachines.java` | `common/machine/PollutionMachines.java` |

---

## 0. 前置规则

### 0.1 电压与 EU 缓存

- 所有机器沿用 GTCEu 电压表 `GTValues.V[tier]`（LV=32，MV=128，HV=512，EV=2048，IV=8192，LuV=32768，ZPM=131072，UV=524288，UHV=2097152）。
- 单方块机器的 EU 缓存是 `V[tier] * 64`（`PollutionMachines.java:256-257`、`324-325`）。
- 多方块机器不直接给 EU，而是靠“能源仓 / 动力仓”部件输入或输出。

### 0.2 负 EUt 规则（发电机专用）

- 配方构建器里 **负 EUt = 该配方输出 EU**（发电机/涡轮燃料），**正 EUt = 消耗 EU**。
- 例：`ManaToEuRecipes.java:56` 的 `.EUt(-8192)`；`MagicFuelRecipes.java:100`、`:127`、`:152` 均为负值；`DandelifeonRecipe.java:33` 是正值 `EUt(999)`（消耗）。
- 因此燃料类多方块（魔力涡轮、魔法涡轮、大型/超级魔力涡轮）必须配**动力仓（OUTPUT_ENERGY）**，而不是能源仓。

### 0.3 工业污染（Industrial Pollution）

- 污染按 **区块** 存储为稀疏 `SavedData`（`PollutionData.java:16-24`），只有被污染过的区块会保留数据。
- 每 200 tick 全局结算一次：先给玩家上负面效果，再对所有污染区块执行 `decay = pollutionDecayPerTick * 200`（`PollutionEngine.java:38-54`）。
- 超过阈值 `effectThreshold`（默认 10.0）时，每 200 tick 给玩家 **反胃（Confusion）+ 饥饿（Hunger）各 100 tick（5 秒）**，并在首次进入时发一条警告（`PollutionEngine.java:60-83`）。
- 机器爆炸会加污染：由 Forge 爆炸事件归因到 `pollution` 命名空间的机器，再调用 `addExplosionPollution`，污染量 = 爆炸威力 × `mufflerPollutionMultiplier`（`MachinePollutionEvents.java:29-43`、`MachinePollution.java:38-44`）。

---

## 1. 灵气发电（Vis → EU）

TC4R 没有 TC6 的“环境灵气”，灵气只存在于 **灵气节点（Aura Node）** 和节点/中继网络里。本移植版所有 Vis 操作都通过 `TC4RBridge` 调用 TC4R API：

- 抽取：`TC4RBridge.drainVis` → `VisNetworkApi.drain(level, pos, channel, amount, action)`（`TC4RBridge.java:39-44`）。
- 洗消通量（Flux）：`TC4RBridge.scrubFlux` → `FluxApi.consumeNearby(level, pos, 16, quanta, machineContext, action)`（`TC4RBridge.java:23, 47-62`）。
- 节点状态读写：`AuraNodeBlockEntity.nodeState()` 与 `NodeApi.replaceLoadedState(...)`（比较并交换，CAS）（`VisProviderMachine.java:59, 85-86`）。

### 1.1 单方块：Vis Generator（灵气发电机）

**做什么**
把周围 TC4R 灵气网络中的 vis 抽出来转成 EU，并按抽取量产生工业污染。上游 1.12 版本从不真正抽 vis，只填能量并污染灵气；本移植版会真实抽取（类注释 `VisGeneratorMachine.java:14-29`）。

**结构要求**
单方块，无结构。注册电压 LV..LuV（tier 1..6）（`PollutionMachines.java:106`）。

**输入 → 输出（速率 / 容量 / 常量）**
- 每 tick：`visBuffer += V[tier] / euPerVis`，`euPerVis = PollutionConfig.VIS_GENERATOR_EU_PER_VIS`（默认 250）（`VisGeneratorMachine.java:70, 76`）。
- 可抽数量 = `min(visBuffer 的整数部分, 能量缓存剩余 / euPerVis)`（`:78-79`）。
- 抽到多少 vis，就产出 `vis × euPerVis` EU，并加 `vis × VIS_GENERATOR_POLLUTION_MULTIPLIER`（默认 0.1）污染（`:93-95`）。
- 六个 vis 通道（`VisChannel.values()`）轮流尝试，每 tick 每个通道最多尝试一次（`:98-109`）。
- 能量缓存 `V[tier] * 64`，只能输出（`isEnergyEmitter = true`，`:44-46`；tooltip `PollutionMachines.java:253-258`）。
- 实际速率：LV 约 32 EU/t（32/250 ≈ 0.128 vis/t，攒够 1 vis 才抽一次），LuV 约 32768 EU/t（131 vis/t）。

**玩法步骤**
1. 在灵气节点/中继网络覆盖范围内放置机器，并把能量输出面接上导线。
2. 保证网络里有可抽的 vis（节点已充能、通道有存量）。
3. 观察机器只有在“抽到 vis”的那一 tick 才产出 EU；灵气耗尽后自动停摆。

**注意事项（失败原因）**
- 网络无可抽 vis → `drained = 0`，不产出（`:86-90`）。
- 能量缓存满 → 不抽 vis（防止堆积），`visBuffer` 被压回 1.0（`:80-84`）。
- `VIS_GENERATOR_EU_PER_VIS ≤ 0` → 整机停摆（`:71-73`）。
- 抽取是“整 quanta”结算，低电压档位表现为间歇性脉冲输出。

来源：`VisGeneratorMachine.java:14-109`、`PollutionConfig.java:44-50`、`PollutionMachines.java:245-260`。

### 1.2 单方块：Vis Provider（灵气充能机）

**做什么**
反向机器：消耗 EU 给 **8 格半径内最近的已加载灵气节点** 充 vis（上游是给 TC6 环境灵气充能；TC4R 只能改节点，类注释 `VisProviderMachine.java:20-29`）。

**结构要求**
单方块，无结构。注册电压 LV..UHV（tier 1..9）（`PollutionMachines.java:108`）。

**输入 → 输出（速率 / 容量 / 常量）**
- 每次操作消耗 `V[tier]` EU（`VisProviderMachine.java:48`），每 tick 尝试一次（继承 `PollutionEnergyMachine` 的 `pollutionTick`）。
- 充能量：`step = max(1, V[tier] × VIS_PROVIDER_MULTIPLIER)`（默认 0.05），即 LV 1.6→1，MV 6.4→6，UHV 104857（`:81`）。
- 充到“空闲基础容量最大”的那个方面（`baseVis - currentVis` 最大者），上限为 `min(step, 剩余空间)`（`:65-84`）。
- 节点扫描半径 8，缓存 60 tick（`:33-34, 52-55`）。
- 通过 `NodeApi.replaceLoadedState` CAS 写入，只有写入成功才扣 EU（`:85-89`）。

**玩法步骤**
1. 把机器放在节点 8 格内（区块必须已加载，`:97-99`）。
2. 接入 EU，机器会自动把节点最缺的方面补起来。
3. 用灵气发电机/节点多方块把充好的 vis 再抽走，形成“充电—放电”循环。

**注意事项（失败原因）**
- 8 格内没有节点 / 节点未加载 → 待机（`:56-58, 92-105`）。
- 节点该方面已满（`room <= 0`）→ 不操作、不耗电（`:77-79`）。
- EU 不足 `V[tier]` → 待机（`:49-51`）。
- CAS 失败（节点状态被其他逻辑改动）→ 不扣 EU（`:87-89`）。

来源：`VisProviderMachine.java:31-113`、`PollutionConfig.java:51-53`。

### 1.3 多方块：Node Producer（节点生产机）

**做什么**
消耗 EU + 源质流体（Infused Energy），按上游概率表“打包”出新的封装灵气节点物品。

**结构要求**（`NodeProducerMachine.java:179-260`）
- 尺寸 20×16×16（16 层剖面，每层 16 行 × 20 字符）。
- 控制器 `S`（位于结构内部，`:209`）。
- 主要材料：`X` = 聚变机外壳（可放输入能源仓 ≤1、输入流体仓 ≤1、输出物品仓 ≤1、维护仓 ≤1）；`A` = 聚变机外壳；`B` = 层压玻璃；`C` = 虚空棱镜；`D/E/H/K/L` = 光束核心 0/1/4/2/3；`F` = 咒法棱镜；`G` = 钨钢齿轮箱；`I` = GT 加热线圈；`O` = 输出物品仓（≤1）。

**输入 → 输出（速率 / 容量 / 常量）**
- 能源仓电压决定 `euTier = ceil(log4(V/32) + 1)`；**euTier ≤ 3（LV/MV/HV）直接不工作**，即至少需要 EV 能源仓（`:77-81`）。
- 生产周期 `duration = ceil(30 / (euTier - 3))` 秒：EV 30 s、IV 15 s、LuV 10 s、ZPM 8 s、UV 6 s（`:82`）。
- 每次消耗 `infusedCost = 144 × 2^(euTier-4)` mB Infused Energy：EV 144、IV 288、LuV 576……（`:83-85`）。
- **代码实际行为**：只要输出物品仓有空间、EU 够 `voltage`、流体够 `infusedCost`，就 **每 tick** 扣 `voltage` EU 和 `infusedCost` 流体；而计时器只在每秒 +1，攒满 `duration` 秒才真正插入一个节点（`:90-109`）。
- 节点属性（`:112-151`）：
  - 等级 `NodeTire`：Normal 60%、Withering 20%、Bright 5%、Pale 15%。
  - 类型 `NodeType`：Standard 60%、Ominous 10%、Pure 10%、Concussive 15%、Voracious 5%。
  - 六种源质 `Essence*`：高斯分布 `abs(ceil(N(0,1) × 100 × bound))`，上限 1000；Air/Water/Earth/Entropy 的 bound=1.0，Fire=0.9+0.1×线圈等级，Order=0.6+0.1×euTier（`:144-149, 153-155`）。
- 线圈等级取自结构里匹配到的 `ICoilType`，没有线圈按 1 算（`:143, 158-168`）。

**玩法步骤**
1. 搭好 20×16×16 结构，放 EV 以上能源仓、输入流体仓、输出物品仓。
2. 输入 Infused Energy（由 GT 源质熔炉或化合源质链获得，见第 2 章）。
3. 每 `duration` 秒获得一个封装节点，用物品管道抽走。
4. 封装节点可直接喂给节点发电机 / 节点清洗机 / 节点高炉 / 节点聚变堆。

**注意事项（失败原因）**
- 能源仓电压不足 EV → 完全不工作（`:79-81`）。
- 输出仓放不下（`insertItemStacked` 模拟失败）→ 不扣料不产出（`:86-89`）。
- EU 或 Infused Energy 不足 → 待机（`:90-96`）。
- ⚠️ **疑似缺口**：扣料是“每 tick”，产出是“每 `duration` 秒”，两者节奏不一致；按代码计算，EV 档一个节点要烧 30 秒 × 2048 EU/t ≈ 61,440 EU + 144 mB/t × 600 tick = 86,400 mB，远高于上游一次性消耗的设计意图。使用时请以实际版本行为为准。

来源：`NodeProducerMachine.java:66-168, 179-260`。

### 1.4 多方块：Large Node Generator（大型节点发电机）

**做什么**
把输入总线里的封装节点当作“持续燃料”发电：节点在总线上就持续提供 EU，不消耗节点（除非特殊行为烧毁）。

**结构要求**（`LargeNodeGeneratorMachine.java:203-245`）
- 尺寸 3×11×25（25 层剖面，每层 11 行 × 3 字符）。
- 控制器 `S`；`A` = HSSG 框架；`B` = 虚空棱镜；`C` = 聚变机外壳，可放输出能源仓 ≤1、输出激光仓 ≤1、维护仓 ≤1、输入流体仓 ≤1；`D` = 加热线圈；`E` = 光束核心 4；`F` = 特殊层压玻璃；`G` = 虚空棱镜；`H` = 输入物品仓（≤1）。

**输入 → 输出（速率 / 容量 / 常量）**
- 基础容量 `BASIC_CAPACITY = 2048`（`:41`）。
- 每个节点贡献 `2048 × 节点倍率 × 线圈等级`；总和 `expectedCapacity`，再乘随机浮动 `roll = 1 + rand × variance / 节点数`（`variance` 来自节点的 Air 源质）（`:80-101, 128-129`）。
- 每 tick 向能量仓 `addEnergy(finalCapacity)`，但要求仓内能一次性装下，否则该 tick 不发电（`:130-133`）。
- 节点倍率（`:136-155`）：等级 Withering ×0.25、Pale ×0.5、Bright ×4；类型 Ominous/Pure ×2、Concussive ×4、Voracious ×8；再乘熵惩罚 `max(1.2 - 0.005×EssenceEntropy, 0)` 和火/秩序加成 `1 + 0.02×sqrt(Fire×Order)`。
- 维持费（每秒，`:103-123`）：尝试各扣 `max(1, ceil(200 × speedMultiplier))` mB 的 **Infused Aura** 和 **Infused Order**；`speedMultiplier = min(节点们 max(0.15, 1 - Water/400))`（水越多越省）。
- 特殊行为（每秒，`:157-174`）：
  - Ominous：加 0.1 污染/秒。
  - Pure：洗掉周围 1 点 flux/秒。
  - Concussive：每秒 1% 概率被消耗（`BURNOUT_CHANCE×10`）。
  - Voracious：当 Aura 和 Order 都缺时，每秒 1% 概率被消耗。

**玩法步骤**
1. 搭结构，放输出能源仓、输入物品仓、输入流体仓。
2. 把封装节点（越大倍率越好）放进输入总线，并持续供应 Infused Aura + Infused Order。
3. 节点留在总线上就能持续发电；Concussive/Voracious 会被烧毁，需要补充。

**注意事项（失败原因）**
- 缺能量仓 / 物品仓 / 流体仓 → 整机待机（`:73-77`）。
- 能量仓空间不足 `finalCapacity` → 该 tick 不输出（`:131`）。
- **维持流体并不是发电的必要条件**：代码只在“有流体时扣流体”，发电本身不看流体；流体只影响特殊行为与烧毁判定（`:107-116`）。
- ⚠️ 结构里 `C` 只允许 **输出** 能源仓/激光仓，不能放输入能源仓。

来源：`LargeNodeGeneratorMachine.java:39-174, 203-245`。

### 1.5 多方块：Node Washer（节点清洗机）

**做什么**
消耗 EU + Infused Water，原位清洗封装节点里的 **EssenceEntropy（熵源质）**，同时洗掉周围一点 flux。上游该机器是半成品；移植版按上游字段补全（类注释 `NodeWasherMachine.java:24-35`）。

**结构要求**（`NodeWasherMachine.java:130-147`）
- 尺寸 7×4×4。
- `S` 控制器；`X` = 热咒法棱镜，可放输入能源仓 ≤1、输入流体仓 ≤1、输入物品仓 ≤1、维护仓 ≤1；`A` = 聚四氟乙烯管道；`C` = 光束核心 4；`D` = 特殊层压玻璃；`E` = 加热线圈。

**输入 → 输出（速率 / 容量 / 常量）**
- 每秒执行一次（`:67-69`）。
- 每次消耗 `V[tier]` EU + `WATER_PER_WASH = 144` mB Infused Water（`:38, 82-86, 98-102`）。
- 清洗量 `maxWash = max(25, 线圈等级 × euTier × 25)`（`:80`）。
- 从第一个含熵的节点开始，熵减 `maxWash`（不低于 0），并洗掉 1 点 flux（`:88-106`）。
- `euTier` 由输入能源仓电压计算（同节点生产机公式，`:77-78`）。

**玩法步骤**
1. 搭结构，放输入能源仓、输入流体仓、输入物品仓。
2. 输入封装节点和 Infused Water。
3. 每秒自动清洗一个节点的熵；洗到 0 后跳过该节点（不会给其他节点继续洗，而是跳过继续找下一个含熵节点）。

**注意事项（失败原因）**
- 缺任一仓 → 待机（`:73-75`）。
- Infused Water 不足 144 mB → 待机（`:82-86`）。
- EU 不足 `V[tier]` → 待机（`:98-100`）。
- 只能降熵、不能加熵；熵为 0 的节点不会被处理（`:94-96`）。

来源：`NodeWasherMachine.java:36-147`。

### 1.6 多方块：Node Blast Furnace（节点高炉）

**做什么**
跑 **BLAST_RECIPES** 与 **FORGE_ALCHEMY_RECIPES**（受线圈温度限制），并用封装节点当“源质催化剂”：节点燃烧 30 秒，期间每秒把节点的 Order/Entropy 源质按 `×10` 转成 **Infused Light / Infused Dark** 流体。上游的 White/Black Mansus 映射到这两种流体（类注释 `NodeBlastFurnaceMachine.java:18-26`）。

**结构要求**（`NodeBlastFurnacePatterns.java:16-47`）
- 尺寸 13×19×13。
- `S` 控制器；`A` = 钨钢齿轮箱；`B` = 魔力基础外壳，可放输入能源仓 ≤1、输入激光仓 ≤3、维护仓 ≤1；`C` = HSSG 框架；`D` = 虚空棱镜；`E` = 光束核心 4；`F` = 钨钢管道；`G` = 特殊层压玻璃；`H` = 光束核心 0；`I` = 加热线圈；`X` = 魔力基础外壳；`Y` = 消音仓（恰好 1 个）。

**输入 → 输出（速率 / 容量 / 常量）**
- 节点寿命 `NODE_LIFETIME_TICKS = 600`（30 秒）（`NodeBlastFurnaceMachine.java:30, 71-83`）。
- 有节点时每秒：`Infused Light += Order × 10` mB、`Infused Dark += Entropy × 10` mB，直接填进流体仓（`:85-90, 109-113`）。
- 节点用完后计时归零，需要再消耗一个（`:71-83, 93-107`）。

**玩法步骤**
1. 搭结构，至少放输入能源仓、维护仓和消音仓。
2. 接入配方所需的物品/流体（本机也是普通多方块配方机）。
3. 往物品总线放封装节点，启动后节点每 30 秒消耗一个并持续产出 Light/Dark 流体。

**注意事项（失败原因）**
- ⚠️ **疑似未完成**：该结构的字符 `B` 只允许 `INPUT_ENERGY / INPUT_LASER / MAINTENANCE`，`Y` 是消音仓；**整个结构没有输入物品仓、输入流体仓或输出流体仓的位置**。而代码需要 `findPart(ItemBusPartMachine.class)` 和 `findPart(FluidHatchPartMachine.class)`，二者为 null 时直接 return（`:65-69`）。也就是说：当前版本里“节点→Light/Dark 流体”的功能不可达，连普通配方也缺少物品/流体仓。以代码为准，这是一处结构性缺口。
- 缺消音仓时结构不成立（`Y` 是 exact limit 1）。

来源：`NodeBlastFurnaceMachine.java:28-127`、`NodeBlastFurnacePatterns.java:13-51`。

### 1.7 多方块：Node Fusion Reactor（节点聚变反应堆，LuV / ZPM / UV）

**做什么**
跑 **FUSION_RECIPES** 与 **NODE_MAGIC_FUSION_RECIPES**，以封装节点作为催化剂：节点决定并行数与每秒消耗的 Dark/Light/Aura（Mansus）流体量。污染 ≤ 4.2 才允许工作（TC6 环境灵气洁净度检查的移植，`NodeFusionReactorMachine.java:56-59`）。

**结构要求**（`NodeFusionReactorPatterns.java:16-63`）
- 尺寸 31×31×31 的球壳。
- `S` 控制器；`A` = 聚变玻璃；`B` = 光束核心 1；`D` = 虚空棱镜，可放输入流体仓（不限）、输入能源仓 ≤16、维护仓 ≤1；`E` = 超导线圈；`F` = 虚空咒法棱镜，可放输入流体仓 ≤5；`G` = 虚空咒法棱镜，可放输出流体仓 ≤1；`H` = 输入物品仓（恰好 1 个）。
- 三个注册档：LuV（tier 6）、ZPM（tier 7）、UV（tier 8）（`PollutionMachines.java:633-635, 845-855`）。

**输入 → 输出（速率 / 容量 / 常量）**
- 每秒读取输入总线里的所有节点（`:82-90, 105-148`），按 **switch 贯穿（fall-through）** 语义累计：
  - Ominous：并行 +1、黑 +1、白 +1，然后继续执行 Pure/Voracious/Concussive 的分支。
  - Pure：黑 −1、白 −1，然后继续执行 Voracious/Concussive。
  - Voracious：并行 +2、星辉 +1，然后继续执行 Concussive。
  - Concussive：黑 +1、白 +1。
  - 等级：Bright 黑 −1、白 −1，并继续执行 Withering 的并行 −1；Withering 并行 −1。
  - 并行下限 1（`:141-143`）。
- 三种 Mansus 计数全为 0 → 不消耗（`:92-94`）。
- 配方工作中且流体足够时，每秒扣 **Infused Dark / Light / Aura** 各等于对应计数（mB）（`:95-102, 150-163`）。
- 洁净度要求：`PollutionEngine.get(...) ≤ 4.2`（`:57-59`）。

**玩法步骤**
1. 搭 31³ 球壳，放输入物品仓（恰好 1）、若干输入流体仓、输出流体仓和输入能源仓。
2. 输入配方所需物品/流体与 Mansus 流体（Infused Dark/Light/Aura）。
3. 往输入总线放封装节点，按类型/等级决定并行与消耗；启动配方。
4. 用 Pure 节点可以降低黑白消耗，Ominous/Voracious 提高并行但提高消耗。

**注意事项（失败原因）**
- ⚠️ **未实现**：`overallParallelAmount` 被赋值但全工程无人读取，节点算出的并行数实际没有作用（`NodeFusionReactorMachine.java:38, 144`，grep 全仓仅这两处）。
- ⚠️ **未接线**：`ICleanVis.isCleanVis()` 只在类里实现，没有任何调用方，因此“污染 ≤ 4.2”实际上没有被检查（`ICleanVis.java:4-8`，grep 仅实现处）。
- ⚠️ **文档化限制**：现代 GT 的聚变启动成本没有重新实现，配方只靠能源仓供电（类注释 `:28-31`）。
- 缺物品仓或流体仓 → 每秒逻辑直接 return（`:85-89`）。
- 只有配方 `isWorking()` 时才扣 Mansus，否则流体只被检查不被消耗（`:98-102`）。

来源：`NodeFusionReactorMachine.java:21-177`、`NodeFusionReactorPatterns.java:13-67`。

### 1.8 多方块：Central Vis Tower（中央灵气塔）

**做什么**
以自身为中心 8 格找节点：把节点中 **超过基础值的多余 vis** 一次性抽干并转成 **Infused Light**，同时洗掉周围 flux 转成 **Infused Dark**。上游的 Botania 魔力维护费改成 EU + 少量 Infused Aura（类注释 `CentralVisTowerMachine.java:24-40`）。

**结构要求**（`CentralVisTowerPatterns.java:16-49`）
- 高塔结构；`S` 控制器；`A` = 魔力基础外壳；`B` = 框架组（HSSG / 硅岩合金 / 钨钢 / 不锈钢四选一，`:53-58`）；`C` = 硅岩合金框架；`D` = 聚变玻璃；`E` = MANA_2 外壳；`F` = 光束核心 1；`G` = MANA_1 外壳；`H` = 层压玻璃；`J` = 光束核心 4；`K` = 魔力基础外壳，可放 **输出流体仓 3~8 个**、输入能源仓 ≤1、维护仓 ≤1。

**输入 → 输出（速率 / 容量 / 常量）**
- 每秒结算一次（`:73-75`）。
- 启动费：`V[tier]` EU + **4 mB Infused Aura**（`:81-89, 101-104`）。
- 抽节点多余 vis（current − base，六通道求和）→ `Infused Light = 抽出的 vis × 10` mB（`:124-137, 105-107`）。
- 洗 1 点 flux → `Infused Dark = 1 × 10` mB（`:96, 108-110`）。
- 两者都没有 → 不扣启动费直接返回（`:97-99`）。
- Starry Mansus（全洁净奖励）**没有对应实现，不会产出**（类注释 `:39-40`）。

**玩法步骤**
1. 搭塔，放输入能源仓和 3~8 个输出流体仓。
2. 在塔 8 格内放一个灵气节点，并让它先充能到基础值以上（例如用 Vis Provider）。
3. 供应 Infused Aura 作为维护费；塔会周期性把节点“充过头”的 vis 变成 Light、把周围 flux 变成 Dark。
4. 节点多余 vis 会被**一次抽干到基础值**，之后需要重新充能。

**注意事项（失败原因）**
- ⚠️ **疑似缺口**：结构 `K` 只允许 **输出** 流体仓，代码却要求从流体仓里 **输入 Infused Aura** 作为维护费（`:85-89`）。GT 的输出仓正常无法被外部灌入，因此该机器很可能永远卡在维护费检查上、完全不产出。以代码为准，这是一处未完成/漏配的结构（缺少 IMPORT_FLUIDS 位置）。
- 8 格内没有节点 → `excess = 0`，只能靠洗 flux 产 Dark（`:91-99`）。
- 输出仓满 → 流体填不进去，但 EU/维持费仍会扣（`:105-110` 无空间检查）。

来源：`CentralVisTowerMachine.java:42-167`、`CentralVisTowerPatterns.java:13-61`。

### 1.9 单方块：Small Node Generator（微型星光节点反应堆，LuV..UHV）

**做什么**
把封装节点放进单方块机器的输入槽，节点在槽里就持续发电；不消耗节点。

**结构要求**
单方块，注册电压 LuV..UHV（tier 6..9）（`PollutionMachines.java:120-121, 347-362`）。输入槽只进不出，可用漏斗/管道装填（类注释 `SmallNodeGeneratorMachine.java:19-31`）。

**输入 → 输出（速率 / 容量 / 常量）**
- 每 tick 产出 `8192 × 节点倍率 × 机器 tier` EU（`SmallNodeGeneratorMachine.java:35, 50-59`）。
- 倍率表（`:61-87`）：等级 Withering ×0.25、Pale ×0.5、Bright ×4；类型 Ominous/Pure ×4、Concussive ×8、Voracious ×16；熵惩罚 `max(1.2 - 0.005×Entropy, 0)`；火/秩序加成 `1 + 0.02×sqrt(Fire×Order)`。
- 能量缓存 `V[tier]*64`，只输出（`PollutionMachines.java:356-359`）。

**玩法步骤**
1. 用漏斗/管道把封装节点塞进唯一的输入槽。
2. 输出面接导线即可持续发电；换节点前需先把旧节点取出。
3. 想要高输出：Bright + Voracious + 高 Fire/Order、低 Entropy 的节点。

**注意事项（失败原因）**
- 槽里没有节点 → 不发电（`:52-54`）。
- 与上游偏差：上游节点移除后仍按最后倍率发电；本移植版**只在节点在槽内时**发电（类注释 `:21-24`）。
- 无污染产出。

来源：`SmallNodeGeneratorMachine.java:33-88`。

### 1.10 封装灵气节点物品与 NBT 契约

**物品**：`pollution:packaged_aura_node`，tooltip 按固定顺序显示 NBT（`PackagedAuraNodeItem.java:29-48`）。

**NBT 契约**（`PackagedAuraNode.java:16-23`，键名沿用上游，脚本兼容）：
| 键 | 含义 | 取值 |
|---|---|---|
| `NodeTire` | 节点等级 | `Normal` / `Withering` / `Bright` / `Pale` |
| `NodeType` | 节点类型 | `Standard` / `Ominous` / `Pure` / `Concussive` / `Voracious` |
| `EssenceAir` / `EssenceFire` / `EssenceWater` / `EssenceEarth` / `EssenceOrder` / `EssenceEntropy` | 六种源质数值 | 非负整数（写入时 `max(0, v)`，`PackagedAuraNode.java:41-43`） |

**节点在机器间的流转**：
- 生产：Node Producer 生成（`NodeProducerMachine.java:112-151`）。
- 清洗：Node Washer 原位修改 `EssenceEntropy`（`NodeWasherMachine.java:94-103`）。
- 燃烧/发电：Large Node Generator（保留物品）、Small Node Generator（保留物品）、Node Blast Furnace（每 30 秒消耗 1 个）、Node Fusion Reactor（只读，不消耗）。
- 判断入口统一是 `PackagedAuraNode.isNode(stack)`（`PackagedAuraNode.java:25-27`）。

---

## 2. 源质（Essentia）引擎链

### 2.0 TC4R API 速览

| 用途 | API | 出现位置 |
|---|---|---|
| 读取物品源质 | `AspectApi.getItemAspects(stack)` | `EssenceSmelterMachine.java:130`、`GtEssenceSmelterMachine.java:115` |
| 六种原始（primal）方面 | `AspectApi.primals()` | `InfusedExchangeMachine.java:77` |
| 输出到容器/管道 | `EssentiaApi.add(level, transport, aspect, amount, side, mode)` | `EssenceSmelterMachine.java:173-174` |
| 寻找源 | `EssentiaApi.findSource(level, pos, aspect, amount, EssentiaSearch.nearby(r)[, filter])` | `InfusedExchangeMachine.java:78-79`、`GTEssentiaHandler.java:143-144` |
| 抽取 | `EssentiaApi.extract(level, ref/transport, aspect, amount, mode)` | `InfusedExchangeMachine.java:92`、`GTEssentiaHandler.java:149-156` |
| 容器（罐/瓶） | `EssentiaContainerApi.contents/capacity/extract/insert/labelAspect` | `AspectTankMachine.java:268, 283-288, 312-325, 674-686` |
| 方块实体发现 | `ThaumcraftApiHelper.getConnectableTransport(...)` | `GTEssentiaHandler.java:99` |
| 方面→流体映射 | `PollutionAspectMapping.materialOf(aspect)`（32 种映射，原始六方面直接对应六个 Vis 通道） | `PollutionAspectMapping.java:41-89, 132-134` |

映射表要点（`PollutionAspectMapping.java:54-86`）：`aer→InfusedAir`、`ignis→InfusedFire`、`aqua→InfusedWater`、`terra→InfusedEarth`、`ordo→InfusedOrder`、`perditio→InfusedEntropy`；化合物如 `victus→InfusedLife`、`mortuus→InfusedDeath`、`praecantatio→InfusedMagic`、`auram→InfusedAura`、`lux→InfusedLight`、`tenebrae→InfusedDark` 等。TC4R 核心没有的方面（如 `alkimia`）不映射。

### 2.1 Essence Smelter（源质熔炉）

**做什么**
把物品“溶解”成 TC4R 物品源质表里的方面，然后把整批源质 **分发到周围 5 格内的所有 `EssentiaTransport` 方块实体**（罐子、管道、储罐等）。

**结构要求**（`EssenceSmelterMachine.java:195-218`）
- 尺寸 7×6×7。
- `S` 控制器；`B` = 虚空咒法棱镜，可放输入物品仓 ≤1、输入流体仓 ≤1、输入能源仓 ≤2、维护仓 ≤1；`E` = 魔法电池外壳；`A` = 不锈钢框架；`F` = HSSG 框架；`D` = 光束核心 2；`G` = 光束核心 4；`C` = 特殊层压玻璃；`H` = 咒法棱镜。

**输入 → 输出（速率 / 容量 / 常量）**
- 启动：取输入总线里第一个“有方面”的物品堆，整堆消耗；总源质 `total = Σ(amount × count)`（`:123-146`）。
- 时长 `duration = max(20, total × 10 / 4^(euTier-1))` tick（`:138`）；`euTier` 由输入电压换算（`:93-94`）。
- 每 tick 消耗 `V[tier]` EU + `infusedCost` mB Infused Fire；`infusedCost = euTier ≤ 1 ? 1 : max(1, (int)(ln1024 / ln euTier))`，即 MV 10、HV 6、EV 5、IV 4、LuV 起 3（`:95, 103-115`）。
- 完成时对 5 格立方内每个 `EssentiaTransport` 调用 `EssentiaApi.add`，一次性推入整批方面（`:162-177`）。

**玩法步骤**
1. 搭结构，放输入物品仓、输入流体仓、能源仓。
2. 输入含源质的物品（原版/TC4R 物品都有源质表）和 Infused Fire。
3. 机器下方/周围放 TC4R 罐子或源质管道，产物会直接灌进去。
4. 想要“物品→流体”请改用 GT Essence Smelter。

**注意事项（失败原因）**
- 物品没有源质 → 跳过（`:130-133`）。
- EU / Infused Fire 不足 → 暂停（`:103-110`）。
- 周围 5 格内没有 `EssentiaTransport` → 完成时源质直接丢失（`:162-177` 只做 add，不缓存）。
- Infused Fire 不足 → 暂停（`:106-110`）。注意判空条件写成 `!infusedFire.isEmpty() && ...`：若该材料将来没有流体，检查会被跳过；当前 Infused Fire 已注册，因此按预期暂停。

来源：`EssenceSmelterMachine.java:46-220`。

### 2.2 GT Essence Smelter（GT 源质熔炉）

**做什么**
与 Essence Smelter 相同的启动/计时，但产物不是给罐子，而是按 `PollutionAspectMapping` 转成对应 **Infused 流体**，比例 **1 源质 = 144 mB**（类注释 `GtEssenceSmelterMachine.java:31-35`）。

**结构要求**（`GtEssenceSmelterMachine.java:175-200`）
- 尺寸/材料与 Essence Smelter 相同（7×6×7）。
- 区别：`B`（虚空咒法棱镜）额外允许 **输出流体仓 ≤6**。

**输入 → 输出（速率 / 容量 / 常量）**
- 计时、EU、Infused Fire 消耗公式完全同 Essence Smelter（`:81-106, 108-131`）。
- 完成时对每个方面：`material.getFluid(144 × amount)` 填入第一个能装的流体仓（`:142-157`）。

**玩法步骤**
1. 搭结构，放输入物品仓、输入流体仓、输出流体仓、能源仓。
2. 输入物品 + Infused Fire。
3. 输出流体仓里会得到对应 Infused 流体（如含 `praecantatio` 的物品 → Infused Magic），供涡轮、节点机器、化合源质链使用。

**注意事项**
- 未映射的方面直接跳过（`:144-147`）。
- 输出仓满 → 该方面被静默丢弃（`fill` 无回滚，`:152-155`）。
- 上游的“周围 5 格罐子”模式在这个变体里没有，必须用输出仓。

来源：`GtEssenceSmelterMachine.java:36-201`。

### 2.3 Essence Collector（源质收集器）

**做什么**
把“局部灵气场”凝成六种原始 Infused 流体。上游读 TC6 环境灵气；移植版把 **8 格内节点的当前 vis** 当作 `vis`、把 **区块工业污染** 当作 `flux`（类注释 `EssenceCollectorMachine.java:24-34`）。

**结构要求**（`EssenceCollectorPatterns.java:16-48`）
- 尺寸 15×11×11。
- `S` 控制器；`A` = 咒法棱镜，可放输入能源仓 ≤2、维护仓 ≤1；`B` = 加热线圈；`C` = 层压玻璃；`D` = **输出流体仓（恰好 6 个）**；`E` = 光束核心 4；`F` = 不锈钢齿轮箱；`G` = 虚空咒法棱镜；`H/I/J/K/L` = 地/水/火/风/秩序咒法棱镜；`O` = 输入物品仓 ≤2。

**输入 → 输出（速率 / 容量 / 常量）**
- 每 tick 消耗 `V[tier]` EU（`:82-84, 99`）。
- 需要 8 格内有节点，且 `vis > 0 && flux < vis`（`:88-92, 103-106`）。
- 速率 `speed = ceil(0.025 × (1 + 线圈等级) × 2^euTier × (ln(vis) − ln(1 + flux/vis)))`（`:38, 103-114`）。
- 每 tick 向 6 个输出仓依次灌入六种原始流体各 `speed` mB（`:116-135`）。

**玩法步骤**
1. 搭结构，放能源仓和恰好 6 个输出流体仓。
2. 在 8 格内放一个高 vis 的节点。
3. 保持区块污染（flux）低于节点 vis；污染越高产量越低，污染 ≥ vis 时停产。
4. 六个输出仓接管道，把原始 Infused 流体送去化合源质混合器或涡轮。

**注意事项（失败原因）**
- 没有节点 / vis=0 / flux ≥ vis / 对数项 ≤0 → 停产（`:88-97, 103-111`）。
- EU 不足 → 停产（`:83-85`）。
- 输出仓满了对应流体 → 那个流体这一 tick 浪费（`:125-134` 逐个 hatch 尝试后 break）。
- ⚠️ **文档化偏差**：上游的“聚焦水晶”模式没有 TC4R 对应物，未实现（类注释 `:32-34`）。

来源：`EssenceCollectorMachine.java:36-182`、`EssenceCollectorPatterns.java:13-52`。

### 2.4 Infused Exchange（源质交换器）

**做什么**
从机器 **上方方块附近 3 格** 找到 TC4R 源质源，抽 1 点源质转成 **144 mB** 对应 Infused 流体，填进机器上方的输出流体仓（类注释 `InfusedExchangeMachine.java:27-37`）。

**结构要求**（`InfusedExchangeMachine.java:109-116`）
- 只有两层：`S` 控制器 + 上方 1 格 `A` = 输出流体仓（`EXPORT_FLUIDS`）。

**输入 → 输出（速率 / 容量 / 常量）**
- 每 10 tick 执行一次（`:70-72`）。
- 遍历 `AspectApi.primals()`，找到第一个可抽的原始方面 → 1 源质 → `144 mB`（`MB_PER_ESSENTIA`）（`:40, 77-96`）。
- 搜索中心是控制器上方一格，半径 3（`SEARCH_RANGE`）（`:41, 78-79`）。

**玩法步骤**
1. 放机器，上方放输出流体仓。
2. 在机器上方 3 格范围内放装有源质的 TC4R 罐子/储罐。
3. 输出仓接管道，把 Infused 流体送走。

**注意事项（失败原因）**
- 没有输出流体仓 → 待机（`:73-76`）。
- 上方附近没有可抽取的源质 → 待机。
- 输出仓装不下 144 mB → 不抽取（`:87-91`）。
- ⚠️ 每 10 tick 只处理 **一个** 方面；遇到未映射的原始方面会 `return` 而不是 `continue`（`:83-86`），可能拖慢其他方面。
- 只处理 `AspectApi.primals()`，化合物不会被转换。

来源：`InfusedExchangeMachine.java:38-117`。

### 2.5 Aspect Tank（源质储罐，单方块）

**做什么**
分等级的源质存储：一次只存 **一种方面 + 数量**，支持锁定（filter）、溢出销毁（voiding）、自动输出与罐/瓶容器交互。为了让 TC4R 管道/魔像/镜子能发现它，注册了自定义方块实体 `AspectTankBlockEntity` 做委托（类注释 `AspectTankMachine.java:43-104`、`AspectTankBlockEntity.java:18-34`）。

**结构要求**
单方块，LV..UHV（tier 1..9）（`PollutionMachines.java:137, 380-400`）。

**输入 → 输出（速率 / 容量 / 常量）**
- 容量 `10_000 << (tier - 1)`：LV 10,000 → UHV 2,560,000（`AspectTankMachine.java:113-114, 170-173`）。
- 每 tick 顺序：先从输入槽的容器抽进罐子，再把罐子灌回输入槽的空容器，最后自动输出（`:235-251`）。
- 自动输出每次 1 点源质到输出面；输出面默认机器背面（`:352-369, 587-597`）。
- 输出面无人接收时，会改为从 8 格内最近的源质源“吸”1 点（前提是罐子已锁定或已有该方面）（`:371-390`）。
- 吸入（suction）：存量 < 250 时 32，带锁定时 64；≥250 时为 0（`:117-119, 453-459`）。
- 连接面 = 机器正面；`canInputFrom/canOutputTo` 都只认正面（`:431-444`）。
- 容器交互（`:263-334`）：整罐内容必须装得下、空容器必须放得进输出槽，否则整次操作不发生；瓶子/罐子容量取自 `EssentiaContainerApi.capacity`。
- 交互（无 GUI，`:608-689`）：
  - 软锤：切换自动输出；潜行+软锤：切换溢出销毁。
  - 扳手（非潜行）：设置/取消输出面。
  - 螺丝刀：在输出面上切换“允许从输出面输入”。
  - 手持带标签/装有源质的容器右键：设置锁定方面；潜行右键：清除锁定。

**玩法步骤**
1. 放下罐子，用装源质的罐子右键锁定方面（可选）。
2. 用源质管道从正面输入；打开自动输出或接管道抽走。
3. 想让机器主动抽源质时：先锁定方面，再开自动输出，它会从 8 格内拉取。
4. 装/卸 TC4R 罐子和瓶子：把容器放进输入槽即可自动完成并移到输出槽。

**注意事项（失败原因）**
- 罐内已有其他方面 → `addEssentia` 返回 0，装不进（`:477-489`）。
- 罐满且未开溢出销毁 → 拒绝（`:483-488`）。
- 罐内容大于容器容量 / 输出槽满 → 容器交互整次跳过（`:279-288`）。
- 锁定后只接受该方面（`doesContainerAccept`，`:754-758`）。
- ⚠️ **未实现**：上游的“罐中罐”物品堆叠（把源质罐装进另一个源质罐物品）没有移植，因为 GTCEu 机器物品没有通用 BE 数据 API（类注释 `:92-97`）。
- ⚠️ 无 ModularUI 界面，所有开关改为工具交互（类注释 `:74-82`）。

来源：`AspectTankMachine.java:106-759`、`AspectTankBlockEntity.java:33-133`、`GTEssentiaHandler.java:79-162`。

### 2.6 实用闭环：源质怎么进、怎么存、怎么用

**进（获得源质）**
1. `Essence Smelter`：物品 → 周围 5 格的 TC4R 罐/管道（`EssenceSmelterMachine.java:162-177`）。
2. `GT Essence Smelter`：物品 → Infused 流体（144 mB/点）（`GtEssenceSmelterMachine.java:142-157`）。
3. `Essence Collector`：节点 vis + 低污染 → 六种原始 Infused 流体（`EssenceCollectorMachine.java:103-135`）。
4. `Infused Exchange`：TC4R 源质 → Infused 流体（144 mB/点）（`InfusedExchangeMachine.java:77-96`）。
5. `Node Blast Furnace` / `Central Vis Tower`：节点 → Light/Dark 流体（见 1.6 / 1.8）。

**存**
- 源质本体：`Aspect Tank`（单方块，10k~2.56M，可被管道发现）。
- 流体形态：`Infused Fluid Hatch`（多方块部件，容量 `8000 × 2^tier`，`InfusedFluidHatchMachine.java:23, 33-36`）或普通 GT 流体仓/储罐。

**用（哪些配方/机器消耗 Infused 流体）**
- 魔法多方块通用消耗：`MagicRecipeLogic.handleTickRecipe` 按 `MagicRecipeProperties.getInfusedFluidPerTick` 从 `Infused Fluid Hatch` 扣（`MagicRecipeLogic.java:341-348`、`MagicMultiblockController.java:122-137`）。
- `Essence Smelter` / `GT Essence Smelter`：每 tick 消耗 Infused Fire（`EssenceSmelterMachine.java:106-115`）。
- `Node Producer`：消耗 Infused Energy（`NodeProducerMachine.java:85, 93-101`）。
- `Large Node Generator`：每秒尝试消耗 Infused Aura + Infused Order（`LargeNodeGeneratorMachine.java:103-116`）。
- `Central Vis Tower`：每秒 4 mB Infused Aura 维护费（`CentralVisTowerMachine.java:85-89`）。
- `Node Fusion Reactor`：工作中每秒消耗 Infused Dark/Light/Aura（`NodeFusionReactorMachine.java:95-102`）。
- `CompoundAspectRecipes`：混合器 1000+1000 → 2000 mB 化合物，并在魔法涡轮里燃烧（`CompoundAspectRecipes.java:112-145`）。
- `MagicFuelRecipes`：化学配方消耗 1152 mB Infused Energy 制造高级燃料（`MagicFuelRecipes.java:81-92, 109-122, 136-144`）。
- `Source Charge`：用水环等饰品 + Infused Water 充能（`SourceChargeMachine.java:17-35`，容量 4000 mB，`:51`）。

**注意**：Mana / Life Essence / Astral 资源在 `MagicMultiblockController` 里仍是保留系统——要求它们的配方会像上游缺少仓室时一样失败（`MagicMultiblockController.java:30-34, 160-191`）。塔罗仓已移植：`TarotHatchMachine` 提供一个过滤卡槽，控制器通过 `ITarotHatch` 发现它；`TAROT` 配方属性与 `EXPERIMENTAL` / `MAGIC_CONVERSION` / `HIDDEN_RITUAL` / `RECYCLING` / `THREE_MAGIC_SYSTEMS` 工序标签的授权检查现已生效。受上游设计限制，塔罗的增幅数值仍需配合星辉晶圆（星辉透镜仓尚未移植）。

---

## 3. 魔力发电（Botania Mana → EU）

### 3.1 单方块：Mana Generator（魔力发电机）

**做什么**
把接收到的 Botania 魔力 **1:1 存成 EU** 并输出。它不是“烧燃料”的机器，而是“魔力电池 + 发电机”（类注释 `ManaGeneratorMachine.java:9-30`）。

**结构要求**
单方块，注册电压 LV..IV（tier 1..5）（`PollutionMachines.java:118-119, 330-345`）。能量缓存 `V[tier]*64`。

**输入 → 输出（速率 / 容量 / 常量）**
- `receiveMana(mana)` 直接把 mana 加进能量容器（1 mana = 1 EU），容量即能量缓存 `V[tier]*64`（`ManaGeneratorMachine.java:53-73`）。
- 实现 Botania `ManaReceiver`，由 `ManaHatchCapabilityEvents` 挂 `MANA_RECEIVER` 能力，火花/魔力脉冲可以直接充它（`ManaGeneratorMachine.java:82-108`、`ManaHatchCapabilityEvents.java:26-32`）。
- 相邻的 Pollution 魔力仓/魔力池仓也会通过 `ManaReceiverLookup` 把魔力推给它（`ManaReceiverLookup.java:28-34`）。
- `consumeMana` 恒返回 false（发电机不给回魔力，`:75-79`）。
- 无污染产出（`pollutionTick` 为空，`:43-47`）。

**玩法步骤**
1. 放下发电机，输出面接导线。
2. 用魔力池仓/魔力仓的自动输出贴脸供魔，或让 Botania 火花/脉冲直接打它。
3. 发电机把魔力当 EU 库存放并对外输出；魔力用完即止。

**注意事项**
- 上游的 `mana_gen_recipes` 配方表在移植版里存在（`BotaniaRecipeMaps.java:53-56`）但**没有注册任何配方**，机器也不引用它；实际转换就是 1:1 入库。
- 只有输入侧能收魔力；输出仓式魔力仓（export）不会给它充能（`ManaHatchMachine.java:126-128, 145-150`）。

来源：`ManaGeneratorMachine.java:32-109`、`PollutionMachines.java:330-345`、`ManaHatchCapabilityEvents.java:12-35`。

### 3.2 多方块：Mana Plate（魔力板）

**做什么**
消耗 Botania 魔力，加速站在它上方 11×11 区域里的 GT 机器（推进配方进度）。上游是 11×11 地板 + 节流阀；移植版没有节流 UI，成型后直接以最大档运行（类注释 `ManaPlateMachine.java:19-38`）。

**结构要求**（`ManaPlatePatterns.java:23-40`）
- 11×1×11 的水平地板。
- `S` 控制器；`C` = 魔力基础外壳，可放 **魔力输入池仓（恰好 1）**。

**输入 → 输出（速率 / 容量 / 常量）**
- 成型时 `speedMax = 魔力池仓的 tier`，`speed = speedMax`（`:69-73`）。
- 每 tick 扫描以“控制器背面方向 5 格处”为中心、上方一层（y+1）的 11×11 区域（`:85-90`）。
- 对每个正在工作的 `IRecipeLogicMachine`：消耗 `2^(speed-1)` 魔力，进度 `+speed`（`:87, 99-105`）。
- 档位示例：稀释池（LV，tier 1）speed=1，1 魔力/台/ tick，+1 进度；普通池（LuV，tier 6）speed=6，32 魔力/台/tick，+6 进度；神话池（UEV，tier 10）speed=10，512 魔力/台/tick，+10 进度。

**玩法步骤**
1. 铺 11×11 地板，在 `C` 位置放一个魔力输入池仓（稀释/普通/神话决定速度档）。
2. 把要加速的 GT 机器放在地板上方一层、中心 11×11 范围内。
3. 给池仓供魔，机器工作时就会被加速；魔力耗尽即停止加速。

**注意事项（失败原因）**
- 没有魔力输入池仓 → 结构不成立（`C` 是 exact limit 1）。
- 魔力不足 `2^(speed-1)` → 立即 `return`，本 tick 后续机器全部不加速（`:102-104`）。
- 只加速 `IRecipeLogicMachine`（GT 配方机器）；非 GT 方块实体不加速（类注释 `:36-37`）。
- 上游节流按钮未移植，速度恒为池仓 tier。

来源：`ManaPlateMachine.java:39-136`、`ManaPlatePatterns.java:21-43`、`ManaPoolHatchMachine.java:153-184`。

### 3.3 多方块：Endoflame Array（末影之焰魔力阵列）

**做什么**
把 **末影之焰花（Endoflame）物品** 和 **熔炉燃料物品** 放进输入总线，按“花数 × 燃烧时间”把燃料转成魔力，输出到魔力输出池仓。转换率 **1.5 魔力 / 燃烧 tick / 朵花**（类注释 `EndoflameArrayMachine.java:21-47`）。

**结构要求**（`EndoflameArrayPatterns.java:26-49`）
- 尺寸 7×6×7。
- `S` 控制器；`A` = TERRA_4 外壳，可放输入物品仓（1~27 个）、**魔力输出池仓（≤1）**、维护仓 ≤1；`B` = 钨钢框架；`C` = 特殊层压玻璃；`D` = 魔力塔；`E` = 泥土；`F` = 白色浮空花；`G` = 光束核心 0；`X` = 空气（必须空）。

**输入 → 输出（速率 / 容量 / 常量）**
- 燃料缓存上限 `MAX_TICKS = 1_600_000_000` 燃烧 tick，NBT 键 `FuelBurnTime`（`:51-52, 165-174`）。
- 每秒检查输入总线：统计末影之焰花数量 `flowers` 与可燃烧物品 `fuelCount`；当 `flowers > 0`、缓存未满、且缓存 + 该物品燃烧时间不超上限时，逐个抽取燃料物品并累加燃烧时间（`:88-114`）。
- 每 tick：`speed = min(flowers, fireticks)`；请求魔力 `speed × 3 / 2`，能塞进池仓多少就结算多少，并从缓存扣掉对应速度（`:115-130`）。
- 池仓满时 `speed = 0`、输出显示归零（`:115-118`）。

**玩法步骤**
1. 搭结构，放输入物品仓和 1 个魔力输出池仓。
2. 往输入总线放末影之焰花（花不会被消耗）和熔炉燃料（煤、木炭、烈焰粉等，`ForgeHooks.getBurnTime` 判定）。
3. 输出池仓接魔力池/魔力仓；花越多、燃料燃烧时间越长，魔力产出越高。
4. 燃料缓存跨存档保存，燃料不会被浪费。

**注意事项（失败原因）**
- 没有花 → `speed = 0`，不烧燃料（`:105, 119`）。
- 输出池仓满 → 不抽取燃料、不产魔力（`:105, 115-117`）。
- 没有输入总线 → 跳过统计（`:88-89`）。
- 燃料判定使用 `RecipeType.SMELTING` 燃烧时间（`:146-148`），与熔炉一致。

来源：`EndoflameArrayMachine.java:49-179`、`EndoflameArrayPatterns.java:24-53`。

### 3.4 多方块：Mega Mana Turbine（超级魔力涡轮）

**做什么**
ZPM 级燃料涡轮：烧 `MANA_TO_EU` 燃料表里的魔力流体发电，可选 **催化剂对** 提高输出上限，并随连续运行时间把并行从 1 拉到 32768（类注释 `MegaManaTurbineMachine.java:26-66`）。

**结构要求**（`MegaManaTurbinePatterns.java:23-56`）
- 尺寸 7×7×14。
- `I` 控制器；`A` = MANA_3 外壳；`B` = 聚变玻璃；`C` = 聚变机外壳 MK2；`D` = 聚变线圈；`E` = 彩虹桥（bifrost）；`F` = 微光梦木；`G` = 加热线圈；`H` = MANA_5 外壳，可放维护仓 ≤1、输入流体仓 ≤4、输出流体仓 ≤4、消音仓 ≤1；`J` = 聚变线圈，可放输出能源仓 ≤1、输出激光仓 ≤1。

**输入 → 输出（速率 / 容量 / 常量）**
- 燃料：`BotaniaRecipeMaps.MANA_TO_EU`（`PollutionMachines.java:742-750`），7 种燃料见 4.4。
- 基础输出上限 `V[8]`（UV，无催化剂）（`:69-70, 97, 141-146`）。
- 催化剂对（`:81-85, 130-147`）：
  - `black_mansus + white_mansus` → 等级 1，上限 `V[9]`；
  - `kq_gold + hyperdimensional_silver` → 等级 2，上限 `2 × V[10]`；
  - `sentient_metal + binding_metal` → 等级 3，上限 `V[12]`。
  - 流体通过 `ForgeRegistries.FLUIDS` 按 `pollution:black_mansus` 等 id 解析；缺失时该对不生效、等级保持 0（`:209-216`、类注释 `:44-54`）。
- 每 100 个工作中 tick 消耗活跃催化剂对各 32 mB（`:71, 150-159, 268-276`）。
- 并行上限 `parallelLimit`：连续运行 `60000 / 线圈等级` tick 内从 1 线性升到 32768，之后保持（`:72-74, 161-171`）。并行在每个新配方开始时应用（`:259-265`）。
- 配方输出 EUt 必须 ≤ 催化剂上限，否则拒绝（`:245-257`）。

**玩法步骤**
1. 搭结构，放输出能源仓/激光仓、输入输出流体仓。
2. 输入魔力燃料（Infused Aura 等）与可选催化剂对；配加热线圈。
3. 持续运行让并行爬升；更换催化剂对会立刻改变输出上限。

**注意事项（失败原因）**
- 没有输出能源仓 → 结构不成立（`J` 提供）。
- 催化剂对不齐/流体缺失 → 等级 0，只能用 UV 上限。
- 燃料不足 / 输出仓满 → 配方逻辑停止。
- ⚠️ 类注释说催化剂材料“尚未移植”，但材料表里这些魔力流体已被 `ManaToEuRecipes` 注册为真实材料；实际是否生效取决于运行时流体注册名是否匹配，代码本身做了运行时解析兜底（`:44-54, 209-216`）。

来源：`MegaManaTurbineMachine.java:67-278`、`MegaManaTurbinePatterns.java:21-59`。

### 3.5 多方块：Life Activation Garden（生命激活花园 / MultiDanDeLifeOn）

**做什么**
在控制器上方 31×31 的“细胞方块棋盘”上跑 **康威生命游戏**，死去的细胞按年龄折算成能量存入内部缓存；缓存可按模式输出为 **EU（模式 0）** 或 **魔力流体（模式 1）**（类注释 `MultiDanDeLifeOnMachine.java:32-68`）。

**结构要求**（`MultiDanDeLifeOnPatterns.java:30-124`）
- 尺寸 63×10×63。
- `E` 控制器；`A` = 活石砖；`B` = 聚变玻璃；`C` = 加热线圈；`D` = 苔石活石砖；`F` = 魔力塔；`G` = MANA_4 外壳，可放输出能源仓 ≤1、输出激光仓 ≤1、维护仓 ≤1、**输入能源仓 ≥1**、**输入流体仓 ≥1**、输入物品仓 ≤1、输出流体仓 ≤1；`H` = MANA_3；`I` = 雕纹活石砖；`M/N` = 活石台阶/自然塔；`O/R` = 聚变机外壳 MK3；`P` = 注魔泥土；`Q` = 活石墙；`T` = MANA_5；`V` = TERRA_2；`W` = 龙石；`X` = 微光梦木；`Y/Z` = MANA_3。

**输入 → 输出（速率 / 容量 / 常量）**
- 配方：`DandelifeonRecipe` —— 1 个精灵尘 + 不可消耗的蒲公英花（dandelifeon），20 tick，`EUt(999)`（正数 = 耗电 999 EU/t）（`DandelifeonRecipe.java:29-34`）。
- 每个配方完成时跑一轮生命游戏（`:366-380`）。
- 棋盘位置由朝向决定（`:175-201`）：北 = (x−15, z+16)，南 = (x−15, z−46)，西 = (x+16, z−15)，东 = (x−46, z−15)；y = 控制器上方一层。
- 规则（`:244-258`）：活细胞邻居 2~3 存活；死细胞邻居恰为 3 复活。
- 棋盘外 4 格内的“越界繁殖”细胞每轮清除（`:203-214`）。
- 死亡细胞年龄 >0 时入账：`coil^1.5 × 2,560,000 × age^(1/3) + 22,937,600 × 2/3`（`:260-265`）。年龄每轮 +1（`:229`）。
- 缓存每 tick 冲刷（`:123-159`）：
  - 模式 0：填进输出能源仓（有空间就搬）。
  - 模式 1：填 `pollution:mana` 流体到输出流体仓。
- 模式切换：潜行 + 螺丝刀（配方逻辑非活跃时才能切，`:275-288`）。

**玩法步骤**
1. 搭 63×10×63 大结构（JEI 预览被刻意关闭，`:` 注册处 `renderMultiblockXEIPreview(false)`，`PollutionMachines.java:758`）。
2. 在控制器前方/侧方按朝向铺 31×31 的 Botania 细胞方块棋盘，作为“种子”。
3. 输入精灵尘（可再补），启动配方；每 20 tick 一轮生命游戏。
4. 选模式 0 出 EU，或模式 1 出魔力流体；缓存会一直保留直到输出。

**注意事项（失败原因）**
- 缺少输入能源仓/输入流体仓 → 结构不成立（`G` 的 `setMinGlobalLimited(1)`）。
- 模式 1 需要 `pollution:mana` 流体；若未注册，缓存不会销毁也不会输出（`:143-147`，类注释 `:61-64`）。
- 生命游戏只对 `BotaniaBlocks.cellBlock` 生效。
- 年龄指数使用 `1.0/3.0`，修正了上游“整数除法导致恒为 1”的问题（类注释 `:53-55`）。

来源：`MultiDanDeLifeOnMachine.java:69-381`、`MultiDanDeLifeOnPatterns.java:28-128`、`DandelifeonRecipe.java:13-35`。

### 3.6 魔力仓 / 魔力池仓（Mana Hatch / Mana Pool Hatch）

**Mana Hatch（魔力能量仓，1A/4A/16A/64A 输入输出）**
- 输入容量 `V[tier] × 16 × 电流`，输出容量 `V[tier] × 64 × 电流`（`ManaHatchMachine.java:49-56`；tooltip `PollutionMachines.java:789-809`）。
- 输出仓每 tick 向相邻第一个能接收的 `ManaReceiver` 推送 `V[tier] × 电流` 魔力（`ManaHatchMachine.java:81-95`）。
- 输入仓可被 Botania 火花/脉冲充能（`canReceiveManaFromBursts`，`:126-128`）。
- `consumeMana` 供多方块配方使用（`:119-123`）。

**Mana Pool Hatch（魔力池仓，三种池型）**
- 池型（`ManaPoolHatchMachine.java:153-184`）：`diluted` = LV、10,000 容量；`normal` = LuV、1,000,000 容量；`mythic` = UEV、1,000,000 容量。
- 每 tick 转移速率 = `V[machineTier]`（`:181-183`）。
- 输入池的外部充能（火花/脉冲）按转移速率节流；输出池向邻居推送也按转移速率节流（`NotifiableManaContainer.java:120-142`、`ManaPoolHatchMachine.java:74-87`）。
- 输出池不会接受火花充能（`canReceiveManaFromBursts` 要求 `!isExport`，`:114-117`）。

**注意**
- 无线魔力仓（Wireless Mana Hatch / Pool Hatch）目前 **与普通仓完全相同**：上游的 `WirelessManager` 没有移植，属于占位实现（类注释 `WirelessManaHatchMachine.java:12-16`）。

来源：`ManaHatchMachine.java:17-151`、`ManaPoolHatchMachine.java:16-185`、`NotifiableManaContainer.java:9-152`、`ManaReceiverLookup.java:12-47`。

---

## 4. 燃料涡轮与发电机

### 4.1 Magic Large Turbine / Magic Mega Turbine（魔法大型/超级涡轮）

**做什么**
烧 `MAGIC_TURBINE_FUELS` 燃料表发电（`PollutionMachines.java:682-688`）。

**结构要求**
- Magic Large Turbine（4×3×3，`MagicLargeTurbineMachine.java:26-40`）：`S` 控制器；`C` 咒法棱镜；`H` 咒法棱镜，可放输入流体仓 ≤2、输出流体仓 ≤1、输出能源仓 ≤2、维护仓 ≤1；`R` 转子支架 ≤2；`G` 钨钢齿轮箱。
- Magic Mega Turbine（7×7×9，`MagicMegaTurbineMachine.java:23-44`）：`C` 虚空咒法棱镜；`G` 钨钢齿轮箱；`R` 转子支架 ≤3；`M` 消音仓 ≤1；`A` 虚空咒法棱镜，可放输入流体仓 ≤3、输出流体仓 ≤1、输出能源仓 ≤3、维护仓 ≤1。

**输入 → 输出**
- 燃料与数值见 4.2 / 4.3；输出 EU 由负 EUt 配方决定。
- 需要转子支架（结构要求），但 ⚠️ **GT 转子的耐久机制没有重新实现**（类注释 `MagicLargeTurbineMachine.java:16-18`）。

**玩法步骤**
1. 搭结构，放输出能源仓（至少 1）、输入流体仓、转子支架。
2. 用泵输入魔力/魔法燃料（Infused Aura、Compound Aspect 流体、Magic Nitrobenzene 等）。
3. 接导线取电；无燃料时自动停机。

**注意事项**
- 没有输出能源仓 → 结构不成立。
- 消音仓可选（Mega 有 M 位），缺省不影响发电。
- 无转子耐久消耗，属于简化。

来源：`MagicLargeTurbineMachine.java:20-42`、`MagicMegaTurbineMachine.java:17-46`。

### 4.2 MagicFuelRecipes（魔法燃料链）

**做什么**
把 GTCEu 原生中间体做成三种高级燃料，并注册它们的燃烧配方（上游 GTQT 材料用 GTCEu 材料替代，类注释 `MagicFuelRecipes.java:19-43`）。

| 燃料 | 制造（`MAGIC_CHEMICAL_REACTOR_RECIPES`） | 燃烧（`MAGIC_TURBINE_FUELS`） |
|---|---|---|
| Magic Nitrobenzene（魔力抗爆焦化硝基苯） | 1000 mB AmmoniumFormate + 1000 mB Ethanol + 10000 mB Nitrobenzene + 1152 mB Infused Energy + 焦化催化剂核心（不消耗），HV，200 tick → 16000 mB（`:76-95`） | 1 mB → 90 tick，`-VA[HV]/4`（`:97-101`） |
| Infernal Blaze Propellant（焚天烈焰推进剂） | 1000 mB RocketFuel + 1000 mB Dimethylhydrazine + 10000 mB NitricAcid + 1152 mB Infused Energy + 8 铝粉 + 催化剂，HV，800 tick → 16000 mB（`:104-131`） | 1 mB → 80 tick，`-VA[EV]`（`:124-128`） |
| Dragon Pulse Fuel（龙脉星轨燃剂） | 1000 mB HydrofluoricAcid + 1000 mB LeadZincSolution + 10000 mB Dimethylhydrazine + 4 龙息 + 1152 mB Infused Energy + 催化剂，IV，800 tick → 16000 mB（`:133-156`） | 1 mB → 160 tick，`-VA[IV]`（`:149-153`） |

**注意**
- 所有制造配方都用 `COKING_CATALYST_CORE` 作为不可消耗催化剂（`:88, 118, 143`）。
- GTNN/GCYR 的火箭燃料图（如果装了对应模组）由 `GTNNRocketFuels` / `GCYRRocketFuels` 注册（`:67-74`）。
- 材料没有流体时会跳过并打日志，不会崩（`:93-95, 129-131, 154-156`）。

来源：`MagicFuelRecipes.java:44-170`。

### 4.3 CompoundAspectRecipes（化合物源质）

**做什么**
两种 Infused 流体在混合器里合成化合物，并把所有 Infused 流体注册成魔法涡轮燃料（上游表格原样移植，类注释 `CompoundAspectRecipes.java:16-24`）。

**输入 → 输出（速率 / 常量）**
- 混合器：`input1 1000 mB + input2 1000 mB → output 2000 mB`；时长 `100 × 组件数` tick；`EUt = VA[min(组件数/4, 上限)]`（`:112-129`）。
- “组件数”= 递归统计材料的嵌套组件数（`:117, 147-164`）。
- 涡轮：每种 Infused 流体 `80 mB`，时长 `80` tick（原始六种）或 `40 × 组件数` tick（化合物），`EUt = -V[max(1, min(组件数/3, 上限))]`（`:128, 131-145`）。
- 原始六种：Air/Fire/Water/Earth/Entropy/Order，80 mB → 80 tick @ `-V[1]`（`:35-40, 131-133`）。
- 合成表共 32 条（`:42-109`），例如 Air+Earth→Crystal、Order+Fire→Energy、Entropy+Light→Dark、Magic+Air→Aura、Void+Entropy→Spatio、Spatio+Exchange→Tempus、Sense+Exchange→Tinctura 等。

**玩法步骤**
1. 用源质收集器/GT 源质熔炉攒原始 Infused 流体。
2. 在混合器里按表合成化合物（化合物本身也是涡轮燃料）。
3. 把流体送进 Magic Large/Mega Turbine 或 Mega Mana Turbine 发电。

**注意事项**
- 材料没有流体时该条配方跳过（`:114-116, 137-139`）。
- 部分化合物（如 Spatio/Tempus/Tinctura/Alchemy）在 TC4R 方面映射中不存在，只能作为涡轮燃料，不能反向变回源质（`PollutionAspectMapping.java:86`）。

来源：`CompoundAspectRecipes.java:26-173`。

### 4.4 ManaToEuRecipes（魔力→EU 燃料）

**做什么**
在 `MANA_TO_EU` 表注册 7 种魔力流体燃料，全部 **100 mB 输入、`-8192 EU/t`**，只有时长不同（上游 `EuPerMb` 换算，类注释 `ManaToEuRecipes.java:12-31`）。

| 燃料 | 时长（tick） | 100 mB 总 EU |
|---|---|---|
| `InfusedAura`（自然魔力替代） | 100 | 819,200 |
| `Impuremana` | 3 | 24,576 |
| `WhiteMansus` | 3 | 24,576 |
| `BlackMansus` | 6 | 49,152 |
| `Starrymansus` | 12 | 98,304 |
| `RichAura` | 25 | 204,800 |
| `ErichAura` | 400 | 3,276,800 |

**玩法步骤**
1. 用 CompoundAspectRecipes / 节点机器 / GT 源质熔炉获得这些流体。
2. 输入 Mega Mana Turbine（`BotaniaRecipeMaps.MANA_TO_EU`），输出能源仓取电。
3. 高价值燃料（ErichAura）单 mB 发电量最高。

**注意事项**
- 所有配方 `EUt` 都是负数 = 输出 EU（`:56, 62, 68, 74, 80, 86, 92`）。
- 上游 7/7 燃料全部注册，无缺失（`:95-96`）。

来源：`ManaToEuRecipes.java:33-102`、`BotaniaRecipeMaps.java:58-61`。

### 4.5 EUt 符号规则小结

| 配方来源 | EUt | 含义 |
|---|---|---|
| `ManaToEuRecipes` | `-8192` | 输出 8192 EU/t |
| `CompoundAspectRecipes` 涡轮 | `-V[tier]` | 输出 |
| `MagicFuelRecipes` 燃烧 | `-VA[tier]` 或 `-VA[HV]/4` | 输出 |
| `DandelifeonRecipe` | `+999` | 消耗 999 EU/t |
| `MagicFuelRecipes` 化学合成 | `+VA[HV]` / `+VA[IV]` | 消耗 |
| `CompoundAspectRecipes` 混合器 | `+VA[tier]` | 消耗 |

机器侧：燃料类机器必须配 **输出能源仓（OUTPUT_ENERGY）**；配方机（合成/加工）配输入能源仓。

---

## 5. 配置项（`PollutionConfig.java`）

配置文件为 Forge 配置，注释区块为 `pollution` 与 `aura` 两组（`PollutionConfig.java:23, 44`）。

| 配置键 | 默认值 | 范围 | 作用 / 影响位置 |
|---|---|---|---|
| `enablePollution` | `true` | 布尔 | 污染系统总开关；关闭后 `PollutionEngine.add` 不生效、玩家不受效果（`PollutionEngine.java:24, 61`） |
| `enableExplosionPollution` | `true` | 布尔 | 机器爆炸是否加污染（`MachinePollution.java:39`） |
| `mufflerPollutionMultiplier` | `1.0` | 0 ~ 1000 | 消音仓每次操作污染与爆炸污染的倍率（`MachinePollution.java:42, 55`；`MagicRecipeLogic.java:257-268`） |
| `fluxScrubberMultiplier` | `0.002` | 0 ~ 1 | Flux 洗消机每 EU 操作洗掉的 flux 系数：`2^(tier-1) × 系数`（`FluxScrubberMachine.java:39`） |
| `pollutionDecayPerTick` | `0.001` | 0 ~ 10 | 每 tick 自然衰减；实际每 200 tick 结算 `×200`（`PollutionEngine.java:47`） |
| `effectThreshold` | `10.0` | 0 ~ 1,000,000 | 超过该值的区块给玩家反胃+饥饿（`PollutionEngine.java:64-72`） |
| `visGeneratorEuPerVis` | `250` | 1 ~ 1,000,000 | 每 vis 产出的 EU（`VisGeneratorMachine.java:70`） |
| `visGeneratorPollutionMultiplier` | `0.1` | 0 ~ 1000 | 每抽 1 vis 产生的污染（`VisGeneratorMachine.java:94-95`） |
| `visProviderMultiplier` | `0.05` | 0 ~ 100 | 灵气充能机每 EU 充入的 vis：`V[tier] × 系数`（`VisProviderMachine.java:81`） |
| `fluxFuelCellFluxPerTick` | `0.005` | 0 ~ 100 | 通量强化燃料电池的基础耗 flux 系数：`desired = 系数×4 + 0.05×4×(tier-1)`；超过 `60 + 5×4^tier` 会爆炸（`FluxFuelCellMachine.java:43-51`） |

**注意**
- 污染衰减是“每 200 tick 对所有已污染区块统一减 `pollutionDecayPerTick × 200`”，不是每 tick 单独减（`PollutionEngine.java:13, 38-54`）。
- `effectThreshold` 的效果每 200 tick 重新施加一次，每次 100 tick（`PollutionEngine.java:69-72`）。

---

## 6. 未实现 / 偏差 / 疑似缺口清单

以下内容按源码明确标注为未实现、偏差，或经阅读发现“代码路径不可达”，使用时请特别注意。

**结构与可达性**
1. **Node Blast Furnace 结构缺少物品/流体仓**：`B` 只允许输入能源仓/激光仓/维护仓，而机器代码需要 `ItemBusPartMachine` 与 `FluidHatchPartMachine`；二者为 null 时直接返回。节点→Light/Dark 功能不可达，普通配方也缺物品/流体接口（`NodeBlastFurnaceMachine.java:65-69`、`NodeBlastFurnacePatterns.java:33-46`）。
2. **Central Vis Tower 无法输入 Infused Aura**：结构 `K` 只允许输出流体仓，代码却要求输入 4 mB Infused Aura 维护费；正常玩法下很可能永远无法启动（`CentralVisTowerMachine.java:85-89`、`CentralVisTowerPatterns.java:44-48`）。
3. **Node Producer 扣料/产出节奏不一致**：每 tick 扣 EU+流体，但每 `duration` 秒才产出一个节点（`NodeProducerMachine.java:90-109`）。
4. **Node Fusion Reactor 的节点并行未生效**：`overallParallelAmount` 赋值后无人读取（`NodeFusionReactorMachine.java:38, 144`）。
5. **Node Fusion Reactor 的洁净度检查未接线**：实现了 `ICleanVis.isCleanVis()`（污染 ≤ 4.2），但没有任何调用方（`ICleanVis.java:4-8`）。
6. **Node Fusion Reactor 聚变启动成本未实现**：类注释明确说明（`NodeFusionReactorMachine.java:28-31`）。

**功能缺失 / 简化**
7. **Central Vis Tower 不产 Starry Mansus**（类注释 `CentralVisTowerMachine.java:39-40`）。
8. **Essence Collector 的聚焦水晶模式未实现**（类注释 `EssenceCollectorMachine.java:32-34`）。
9. **Infused Exchange 每 10 tick 只处理一个原始方面**，且遇到未映射方面直接 return（`InfusedExchangeMachine.java:77-97`）。
10. **Aspect Tank 无 GUI**（工具交互替代）、“罐中罐”物品填充为 TODO（类注释 `AspectTankMachine.java:74-104`）。
11. **无线魔力仓 = 普通魔力仓**：`WirelessManager` 未移植（类注释 `WirelessManaHatchMachine.java:12-16`）。
12. **Mana Plate 节流阀未移植**：成型后固定最大档（类注释 `ManaPlateMachine.java:32-35`）。
13. **魔法涡轮无转子耐久消耗**（类注释 `MagicLargeTurbineMachine.java:16-18`）。
14. **Life Activation Garden 模式 1（魔力流体）依赖未移植的 `pollution:mana` 流体**：流体缺失时能量缓存只存不发（`MultiDanDeLifeOnMachine.java:142-147`）。
15. **Mana Generator 不引用 `mana_gen_recipes`**：该配方表存在但没有配方，转换逻辑是 1 mana = 1 EU（`ManaGeneratorMachine.java:20-24`、`BotaniaRecipeMaps.java:53-56`）。
16. **Magic Multiblock 的 Mana / Life Essence / Astral 资源**：`MagicMultiblockController` 保留为未实现系统；要求这些资源的配方会失败（`MagicMultiblockController.java:30-34, 160-191`、`MagicRecipeLogic.java:79-93`）。**Tarot 已移植**（`TarotHatchMachine` + `ITarotHatch` 发现 + 配方授权检查）；增幅数值仍受星辉晶圆门槛限制（见 `docs/HATCH_SEMANTICS.md` §4）。
17. **Mega Mana Turbine 催化剂材料**：代码运行时按 `pollution:black_mansus` 等 id 解析；若注册表缺失则催化剂等级为 0（`MegaManaTurbineMachine.java:44-54, 209-216`）。

**已修复/有意偏差（相对上游）**
18. **Vis Generator 真实抽取 vis**（上游只污染灵气不抽 vis，`VisGeneratorMachine.java:14-29`）。
19. **Vis Provider 改为给节点充能**（上游给 TC6 环境灵气充能，`VisProviderMachine.java:20-29`）。
20. **Small Node Generator 只在节点在槽内时发电**（上游移除节点后仍按旧倍率发电，`SmallNodeGeneratorMachine.java:19-31`）。
21. **Life Activation Garden 年龄指数修正为 `1.0/3.0`**（上游整数除法恒为 1，`MultiDanDeLifeOnMachine.java:53-55`）。
22. **结构框架材料替换**：GTQT 的 HyperdimensionalSilver/KQGold/Mansussteel/Thaumium → NaquadahAlloy/TungstenSteel/HSSG/StainlessSteel（`MagicStructureElements.java:36-48`）。

---

## 附录 A：常用机器注册名

| 机器 | 注册 id（tier 前缀省略） |
|---|---|
| Vis Generator | `vis_generator`（`lv_`..`luv_`） |
| Vis Provider | `vis_provider`（`lv_`..`uhv_`） |
| Flux Scrubber | `flux_scrubber` |
| Flux Promoted Fuel Cell | `flux_fuel_cell` |
| Mana Generator | `mana_generator` |
| Small Node Generator | `small_node_generator`（`luv_`..`uhv_`） |
| Aspect Tank | `aspect_tank`（`lv_`..`uhv_`） |
| Infused Fluid Hatch | `infused_fluid_hatch` |
| Vis Hatch | `vis_hatch` |
| Flux Muffler | `flux_muffler` |
| Mana Hatches | `mana_input_hatch_1a/4a/16a/64a`、`mana_output_hatch_*`、`wireless_*` |
| Mana Pool Hatches | `mana_pool_input_hatch_{diluted,normal,mythic}` 等 |
| Node Producer | `node_producer` |
| Large Node Generator | `large_node_generator` |
| Node Washer | `node_washer` |
| Node Blast Furnace | `node_blast_furnace` |
| Node Fusion Reactor | `luv_node_fusion_reactor` / `zpm_...` / `uv_...` |
| Central Vis Tower | `central_vis_tower` |
| Essence Smelter | `essence_smelter` |
| GT Essence Smelter | `gt_essence_smelter` |
| Essence Collector | `essence_collector` |
| Infused Exchange | `infused_exchange` |
| Magic Large/Mega Turbine | `magic_large_turbine` / `magic_mega_turbine` |
| Mega Mana Turbine | `mega_mana_turbine` |
| Life Activation Garden | `pollution_multi_dan_de_life_on` |

来源：`PollutionMachines.java:244-761`。

## 附录 B：多方块结构一览（按字符）

| 机器 | 尺寸（x×y×z，近似） | 主壳体/框架 | 关键可替换仓 |
|---|---|---|---|
| Node Producer | 20×16×16 | 聚变外壳 + 层压玻璃 + 虚空棱镜 + 光束核心 | 输入能源仓 ≤1、输入流体仓 ≤1、输出物品仓 ≤1、维护仓 ≤1 |
| Large Node Generator | 3×11×25 | HSSG 框架 + 虚空棱镜 + 聚变外壳 | 输出能源仓 ≤1、输出激光仓 ≤1、输入流体仓 ≤1、输入物品仓 ≤1、维护仓 ≤1 |
| Node Washer | 7×4×4 | 热咒法棱镜 + PTFE 管道 | 输入能源仓 ≤1、输入流体仓 ≤1、输入物品仓 ≤1、维护仓 ≤1 |
| Node Blast Furnace | 13×19×13 | 魔力基础外壳 + HSSG 框架 + 虚空棱镜 | 输入能源仓 ≤1、输入激光仓 ≤3、维护仓 ≤1、消音仓 ×1 |
| Node Fusion Reactor | 31×31×31 | 聚变玻璃 + 光束核心 + 虚空棱镜 + 超导线圈 | 输入能源仓 ≤16、输入流体仓、输出流体仓 ≤1、输入物品仓 ×1、维护仓 ≤1 |
| Central Vis Tower | 高塔 | 魔力外壳 + 四选一框架 + 聚变玻璃 + 层压玻璃 | 输出流体仓 3~8、输入能源仓 ≤1、维护仓 ≤1 |
| Essence Smelter | 7×6×7 | 不锈钢/HSSG 框架 + 魔法电池外壳 + 特殊层压玻璃 | 输入物品仓 ≤1、输入流体仓 ≤1、输入能源仓 ≤2、维护仓 ≤1 |
| GT Essence Smelter | 7×6×7 | 同上 | 额外输出流体仓 ≤6 |
| Essence Collector | 15×11×11 | 咒法棱镜系列 + 层压玻璃 + 不锈钢齿轮箱 | 输入能源仓 ≤2、输出流体仓 ×6、输入物品仓 ≤2、维护仓 ≤1 |
| Infused Exchange | 1×2×1 | 控制器 + 输出流体仓 | 输出流体仓 ×1 |
| Mana Plate | 11×1×11 | 魔力基础外壳 | 魔力输入池仓 ×1 |
| Endoflame Array | 7×6×7 | TERRA_4 + 钨钢框架 | 输入物品仓 1~27、魔力输出池仓 ≤1、维护仓 ≤1 |
| Mega Mana Turbine | 7×7×14 | 聚变玻璃/外壳 + 彩虹桥 + 微光梦木 + 加热线圈 | 输入流体仓 ≤4、输出流体仓 ≤4、输出能源仓 ≤1、输出激光仓 ≤1、消音仓 ≤1、维护仓 ≤1 |
| Life Activation Garden | 63×10×63 | 活石砖 + 聚变玻璃/外壳 MK3 + 龙石 + 微光梦木 | 输入能源仓 ≥1、输入流体仓 ≥1、输入物品仓 ≤1、输出流体仓 ≤1、输出能源仓 ≤1、输出激光仓 ≤1、维护仓 ≤1 |
| Magic Large Turbine | 4×3×3 | 咒法棱镜 + 钨钢齿轮箱 | 输入流体仓 ≤2、输出流体仓 ≤1、输出能源仓 ≤2、转子支架 ≤2、维护仓 ≤1 |
| Magic Mega Turbine | 7×7×9 | 虚空咒法棱镜 + 钨钢齿轮箱 | 输入流体仓 ≤3、输出流体仓 ≤1、输出能源仓 ≤3、转子支架 ≤3、消音仓 ≤1、维护仓 ≤1 |

来源：各 `*Patterns.java` 与机器类中的 `createPattern`。
