# TC4R 20719 → 20721 API 适配审计

本审计比较本地 20719 与 `D:\Downloads\1.20.1-forge-20721-dev.zip` 内 20721
的 Thaumcraft 4R API/source，并用 `D:\Downloads\1.20.1-forge-20721.zip` 的运行 JAR
核对公开方法签名。当前构建及 DEV 模组包使用 core、Forbidden Magic、Tainted Magic
和 Thaumic Tinkerer 0.1.0-20721。

## 版本与依赖边界

- `gradle.properties` 锁定 Thaumcraft 4R 与三个已提供更新包的附属为 `0.1.0-20721`。
- 20721 开发包没有 Thaumic Energistics；现有 20711 工件仅作 `compileOnly` 参考，
  不进入运行时。它调用了 20719 起已移除的对象源质 API。
- 20719 本地 Maven 工件保留作升级前对照，不参与当前 Gradle 解析。

## API 变化

- 新增 `PlayerKnowledgeApi` 和 `PlayerKnowledgeView`。
- `AspectPoolApi` 改变首次发现行为：发现新要素时会授予一次 2 点要素池奖励；
  导入结果也会把奖励计入实际增加值。
- `ResearchApi.complete` 的 Warp 语义调整为不同完成原因都发放条目 Warp，管理命令
  只静默提示。Pollution 仅调用 `ResearchApi.isComplete`，未调用完成/发放接口。
- `FluxApi.consumeNearby`、`ResearchApi.isComplete` 和
  `ThaumcraftApiHelper.getConnectableTransport` 在 20721 保持与 20719 相同的公开
  参数及返回类型。20721 API JAR 与实际运行 JAR 的这些方法签名也一致。
- Pollution 没有直接调用 `AspectPoolApi`；也没有使用 20721 移除的原版泛用
  `primal_arrow` 注册项。因此这些行为/注册变化不要求 Pollution 侧代码改写。
- 20721 不再注册通用物品 `thaumcraft:primal_arrow`，但保留六种元素箭物品。旧存档若有该通用箭的物品栈，Forge 会报告 missing mapping，继续载入时该栈可能丢失；目前没有语义等价的新物品，不应任意 remap。GameTest 的既有测试目录也记录了这个旧注册 ID。

## Pollution 调用点

| Pollution 调用 | 20721 结果 |
|---|---|
| `TC4RBridge.scrubFlux` → `FluxApi.consumeNearby` | 签名兼容，模拟/执行及 quanta 消耗保持既有实现 |
| `IndustrialInfusionMachine` → `ResearchApi.isComplete` | 签名兼容，只读研究门槛逻辑无需调整 |
| Essentia helpers / `AspectTankMachine` → `getConnectableTransport` | 签名兼容，管道传输调用无需调整 |
| Forbidden Magic aspect 映射 | 改用 20721 附属运行工件；由 `compileJava`/GameTest 验证二进制兼容 |

本次升级没有发现需要改写的 Pollution API 调用。最终兼容判定以 20721 依赖下的
编译、GameTest 和独立客户端启动结果为准；存档中已移除的通用箭物品按上文作为上游注册迁移边界单独处理。

## 上游行为边界

Astral 星座塔作为星流产能端与 Blood Magic 专属内容按范围排除；地下桥完整的 1.12
多 piece 布局与战利品房仍非等价实现。Alfheim 的程序化树/花/藤、PureTar、矿脉和
现代 feature 已纳入当前验收，但不声称完全复刻 1.12 WorldEngine 的区块种子结果。
