# 文档索引

这里按“当前状态 → 玩家使用 → 深度审计 → 历史记录”的顺序整理移植文档。

## 先看这些

| 文档 | 内容 | 状态 |
|---|---|---|
| [`PORTING_TARGET.md`](PORTING_TARGET.md) | 上游基线、目标版本、现代 API 适配和排除范围 | 当前规范 |
| [`PORT_COMPLETION_CHECKLIST.md`](PORT_COMPLETION_CHECKLIST.md) | 已完成范围、核心行为不变量和最终验证 | 当前规范 |
| [`SMOKE_TEST_REPORT.md`](SMOKE_TEST_REPORT.md) | 服务端启动、RCON 机器/维度检查和错误分类 | 当前证据 |
| [`GAMEPLAY_TUTORIAL.md`](GAMEPLAY_TUTORIAL.md) | 玩法流程、机器使用、配置和已知偏差 | 玩家手册 |

血魔法和 Astral Sorcery 是明确排除项。看到文档中的 Blood/Astral 条目时，应先确认它
是“范围外边界”还是历史审计中的未移植项；本项目保留独立 Starstream，但不实现 Astral
方尖碑及星辉原生供能闭环。

## 工程与兼容性

- [`MIGRATION_TRACKER.md`](MIGRATION_TRACKER.md)：版本矩阵、API 适配、阶段记录和历史变更日志。
  文件后半部分保留过程快照，当前状态以本索引列出的规范文档为准。
- [`TC4R_20719_API_AUDIT.md`](TC4R_20719_API_AUDIT.md)：Thaumcraft 4R 20719 API 适配和
  compile-only 依赖边界。
- [`HATCH_AUDIT.md`](HATCH_AUDIT.md)、[`HATCH_SEMANTICS.md`](HATCH_SEMANTICS.md)：仓室
  结构、IO 方向、容量、传输和 tooltip 语义。
- [`MACHINE_RECIPE_PLAN.md`](MACHINE_RECIPE_PLAN.md)、[`MACHINE_OVERLAYS.md`](MACHINE_OVERLAYS.md)：
  机器/配方和现代资源模型审计。
- [`MATERIALS_AUDIT.md`](MATERIALS_AUDIT.md)、[`GTQT_MATERIALS_ANALYSIS.md`](GTQT_MATERIALS_ANALYSIS.md)：
  材料、矿脉、GTQT 替代和不可移植链条。
- [`SUBSTITUTIONS.md`](SUBSTITUTIONS.md)：TC6、GTQT、Blood/Astral 等旧依赖在当前目标中的
  替代或排除关系。

## 资产、数据和专项审计

- [`CLIENT_ASSETS.md`](CLIENT_ASSETS.md)、[`CLIENT_RENDER_PARITY.md`](CLIENT_RENDER_PARITY.md)：
  资源转换、模型、纹理和客户端表现。
- [`LANG_AUDIT.md`](LANG_AUDIT.md)：语言键和生成输出。
- [`LOOT_AND_GUIDE_PORT.md`](LOOT_AND_GUIDE_PORT.md)：战利品表和 JEI/指南页。
- [`ASPECT_TANK_FEASIBILITY.md`](ASPECT_TANK_FEASIBILITY.md)、[`CIRCUIT_CHAIN.md`](CIRCUIT_CHAIN.md)：
  源质仓和电路链专项说明。
- [`UNOBTAINABLE_ITEMS.md`](UNOBTAINABLE_ITEMS.md)、[`TEXTURE_AUDIT.md`](TEXTURE_AUDIT.md)：
  可获得性、占位资源和缺失引用审计。

## 阅读约定

文档中的 1.12、GTQT、TC6 等名称通常表示上游对照对象，不代表目标运行时仍依赖它们。
历史审计保留原始判断，后续修复会在文首注明并由验收清单覆盖。资源和构建状态以当前
`gradle.properties`、`build.gradle`、`PORTING_TARGET.md` 和 `PORT_COMPLETION_CHECKLIST.md`
为准。
