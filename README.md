# Pollution Unofficial (1.20.1)

Pollution 的非官方 Forge 1.20.1 移植版。

上游项目：`H:\MinecraftMods\Pollution`（Minecraft 1.12.2，GTCEu / GTQT 附属，围绕 Thaumcraft 6 设计）。
本移植将污染系统与魔法内容重建到：

- Minecraft 1.20.1 / Forge 47.4.23
- GregTech CEu Modern 7.5.3（`com.gregtechceu.gtceu:gtceu-1.20.1`）
- Thaumcraft 4R 0.1.0-20719（`dev.tc4port:thaumcraft-forge`，本地 Maven）
- JEI 15.56.0.205（保留配方查看功能）
- KubeJS 2001.6.5-build.16（保留配方自定义能力）

## 目标

- 工业污染：按区块存储、机器排污、消声仓等级影响、爆炸归因
- 污染效果：负面效果、环境转化、机器效率惩罚
- TC4R 贯通：灵气抽取（`VisNetworkApi`）、咒波清洗（`FluxApi`）、扭曲联动
- GTCEu 机器与多方块：灵气发电机、空气过滤机、魔导系列
- 植物魔法与 AE2 联动已保留；血魔法和 Astral Sorcery 按范围排除，星流网络保留独立 API，见 `docs/PORTING_TARGET.md`

## 构建

```
gradlew build
```

首次构建要求：

1. JDK 17（路径见 `gradle.properties` 的 `org.gradle.java.home`）
2. 按 `local-repo/README.md` 放入 TC4R 开发 jar
3. 允许访问 `maven.gtceu.com`、`maven.blamejared.com`、`maven.latvian.dev` 等仓库

## 协议

本移植沿用上游协议：AGPL-3.0-or-later。
上游版权：Copyright (c) KeQingSoCute520 越人不歌。
