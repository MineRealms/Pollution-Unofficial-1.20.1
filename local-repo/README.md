# local-repo

本目录是一个小型 Maven 仓库，用于解析 Thaumcraft 4R 的 1.20.1 开发工件（未公开分发）。

当前切换到 20719 工件：

- `dev/tc4port/thaumcraft-forge/0.1.0-20719/thaumcraft-forge-0.1.0-20719.pom`
- `dev/tc4port/forbidden-magic/0.1.0-20719/`
- `dev/tc4port/tainted-magic/0.1.0-20719/`
- `dev/tc4port/thaumic-tinkerer/0.1.0-20719/`

这些目录中的 JAR、sources JAR 和 API JAR 属于用户提供的第三方二进制，均被 `.gitignore` 排除。

Thaumic Energistics 没有随 20719 包提供，因此仍使用
`dev/tc4port/thaumic-energistics/0.1.0-20711/`，版本在 `gradle.properties` 中单独声明。

来源：`D:\Downloads\1.20.1-forge-20719-dev.zip`（源码/API）和
`D:\Downloads\1.20.1-forge-20719.zip`（运行 JAR）。Gradle 依赖通过
`0.1.0-20719` Maven 坐标解析，不依赖 ZIP 文件名。

```
来源（任选其一）：
  D:\Downloads\1.20.1-forge-20711.zip 内解压得到的
    thaumcraft-forge-4.2.3.5-1.20.1-port.0.1.0-20711.jar
  H:\MinecraftMods\FM-port-deps\full\thaumcraft-forge-4.2.3.5-1.20.1-port.0.1.0-20711.jar

目标：
  local-repo/dev/tc4port/thaumcraft-forge/0.1.0-20711/thaumcraft-forge-0.1.0-20711.jar
```

源码包（只读参考，不参与构建）：

- `H:\MinecraftMods\FM-port-deps\tc\thaumcraft-forge-4.2.3.5-1.20.1-port.0.1.0-20711-sources.jar`
- 已解压副本：`H:\MinecraftMods\FM-port-deps\tc-src`
源码包（只读参考，不参与构建）放在对应 20719 artifact 目录；Gradle 直接使用同目录的运行 JAR。
