# Sunlit Valley DEV 依赖包（TC4R 20721）

交付压缩包：`Pollution-Unofficial-1.20.1-SunlitValley-DEV-20721.zip`。

## 内容

- `mods/` 共 22 个可运行模组 JAR，依据 `G:\MinecraftGames\Sunlit Valley(BaopuEdition)-DEV\.minecraft\versions\Society Sunlit Valley\mods` 目录制作。
- 该目录原有的 17 个其他模组 JAR 保持原样；Pollution 本体替换为本次 `reobfJar` 构建产物；Thaumcraft 4R、Forbidden Magic、Tainted Magic、Thaumic Tinkerer 替换为用户提供的 `1.20.1-forge-20721.zip` 中四个运行 JAR。
- 压缩包根目录的 `README-包内说明.txt` 列出版本、来源及全部文件名；`SHA256SUMS.txt` 为 22 个 JAR 的校验值。

原 `mods` 目录中的 `Pollution-1.20.1-格雷x神秘4Rx植魔联动[已全整合].zip` 是整合包归档，不是 Forge 可加载的模组依赖，因此不复制进运行包。DEV 客户端原目录不会被修改。

## 兼容性与验证

- TC4R 20721 API 新增 `PlayerKnowledgeApi` / `PlayerKnowledgeView`，调整 Aspect Pool 首次发现奖励及研究完成 Warp 行为。Pollution 未调用受影响的奖励/完成接口；本项目实际调用的 Flux、Research 查询和 essentia transport 方法签名未变。
- Thaumic Energistics 没有随 20721 提供，项目保留 20711 `compileOnly` 参考，不随运行 JAR 包分发。
- 20721 已移除通用物品 `thaumcraft:primal_arrow`。旧存档若保存了此物品，Forge 可能在载入时丢弃该物品；六种元素箭未受此变更影响。
- 血魔法和 Astral Sorcery 属于明确排除范围，DEV `mods` 依赖包也未包含它们。
- 本次 `compileJava reobfJar` 成功；`runGameTestServer` 的 35/35 项测试通过；独立 `runClient` 完成 Forge、TC4R、Pollution 初始化及客户端资源加载。

文件清单和 API 细节见 [`TC4R_20721_API_AUDIT.md`](TC4R_20721_API_AUDIT.md)。
