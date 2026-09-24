# TC4R 20719 API 适配审计

本记录核对 1.20.1 下 Thaumcraft 4R 20719 的 sources/API 与下游当前调用。
核对对象是 `local-repo/dev/tc4port/thaumcraft-forge/0.1.0-20719/`，并与现有
20711 sources 做了逐类比较。它只说明 API 适配状态，不代表 Pollution 的全部
1.12 功能已经等价移植。

## 依赖版本

- `thaumcraft-forge`、Forbidden Magic、Tainted Magic 和 Thaumic Tinkerer 使用
  `0.1.0-20719`。
- 20719 开发包没有 Thaumic Energistics 工件，因此
  `thaumic-energistics` 继续固定为 `0.1.0-20711`，只保留为编译参考。
  该旧 JAR 调用了 20719 已移除的对象源质 API，不能作为运行时依赖加载；
  版本在 `gradle.properties` 中单独声明，不能跟随其他 addon 的版本循环升级。

## 已核验的变化

- `dev.tc4port.thaumcraft.api.flux.FluxApi` 在 20719 改用
  `BlockBackedFluid`/`TCFluids` 的 quanta 表示，新增
  `place(ServerLevel, BlockPos, Fluid, int[, flags])` 和 `add(...)`，并让
  `place` 可走 zero-tick 放置及 `EntityPlaceEvent`。`consumeNearby` 的公开
  调用仍兼容，但实现改为按 fluid quanta 读写。
- `ThaumcraftApiHelper` 移除了旧的 object-aspect 推断辅助方法
  （`cullTags`、`getObjectAspects`、`getBonusObjectTags`、`generateTags`）。
  Pollution 当前只调用仍保留的 `getConnectableTransport`；对象源质应使用
  `ObjectAspectCatalog`/`AspectQueryApi` 等新入口。
- `ResearchApi` 新增购买相关方法；现有 `isComplete`/`discover`/`complete`
  调用保持兼容。`EssentiaContainerApi` 的公开签名未变。
- `AuraNodeBlockEntity` 的保存接口按现代方块实体生命周期改为
  `load(CompoundTag)`、`saveAdditional(CompoundTag)` 和无参数
  `getUpdateTag()`；Pollution 只使用节点状态、可见源质和 tick 入口，未直接
  覆盖这些序列化方法。
- `ObjectAspectCatalog` 增加 runtime phase、同步快照和 generation；现有
  `registerRuntime(Item, Map)` 仍可用。`TCBlocks` 的 obelisk cap 和 golem
  fetter 注册实现有变化，但 Pollution 没有依赖这两个具体方块类型。

## 当前调用与真实缺口

Pollution 现有 flux 机器通过 `TC4RBridge` 调用 `FluxApi.consumeNearby`，没有
直接写 Flux 方块，因此没有已知的 20719 编译级迁移缺口。今后新增 Flux 生成
应使用 `FluxApi.place`/`add`，以保留 20719 的放置事件语义。

仍未等价的内容包括：Astral 星座塔作为星流产能端（按移植范围排除）、地下桥
完整的 1.12 多 piece 布局与战利品房、Alfheim/Blood 的 WorldEngine 自定义地形、
部分自定义树/花/藤和 PureTar/缺少现代材料的矿脉。现有 datapack、程序化桥和
feature 近似实现可用，但不能宣称覆盖全部上游行为。
