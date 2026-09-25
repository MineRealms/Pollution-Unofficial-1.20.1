# local-repo

本目录是一个小型 Maven 仓库，用于解析 Thaumcraft 4R 的 1.20.1 开发工件（未公开分发）。

当前构建使用 20721 工件：

- `dev/tc4port/thaumcraft-forge/0.1.0-20721/`
- `dev/tc4port/forbidden-magic/0.1.0-20721/`
- `dev/tc4port/tainted-magic/0.1.0-20721/`
- `dev/tc4port/thaumic-tinkerer/0.1.0-20721/`

这些目录中的运行 JAR、sources JAR 和可用 API JAR 来自用户提供的 20721 包，均被 `.gitignore` 排除。20719 工件保留在旧版本目录用于审计，不参与当前构建。

Thaumic Energistics 没有随 20721 包提供。其 20711 JAR 仅保留为编译参考，
不会作为运行时依赖加载，因为它调用了 20719+ 已移除的对象源质 API。

来源：`D:\Downloads\1.20.1-forge-20721-dev.zip`（源码/API）和
`D:\Downloads\1.20.1-forge-20721.zip`（Thaumcraft 与三个附属的运行 JAR）。Gradle 依赖通过
`0.1.0-20721` Maven 坐标解析，不依赖 ZIP 文件名。

源码包仅作 API 对照，不参与构建；Gradle 使用对应 20721 artifact 目录中的运行 JAR。
