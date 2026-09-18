# local-repo

本目录是一个小型 Maven 仓库，用于解析 Thaumcraft 4R 的 1.20.1 开发工件（未公开分发）。

已提交的内容：

- `dev/tc4port/thaumcraft-forge/0.1.0-20711/thaumcraft-forge-0.1.0-20711.pom`

未提交的内容（被 `.gitignore` 排除，属第三方二进制）：

- `dev/tc4port/thaumcraft-forge/0.1.0-20711/thaumcraft-forge-0.1.0-20711.jar`

新环境首次构建前，把 TC4R 工件从开发包复制到本目录：

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
