# NeoForge 打包

NeoForge 的 JarJar 和 Fabric 的 JiJ 机制不同：JarJar 并不存在一个静默跳过机制，它总是会被加载进游戏，因此 Fabric 的方案行不通。
不过我们可以用一些黑魔法：通过 FML 的扩展点 `IDependencyLocator`，自己实现一个 Jar-in-Jar 机制，从而变相实现这个功能。

## 最终 JAR 结构

```
touchcontroller-neoforge.jar
├── META-INF/neoforge.mods.toml                                   ← 供启动器检测
├── META-INF/services/
│   ├── net.neoforged.neoforgespi.locating.IDependencyLocator     ← Early Service SPI
│   └── net.neoforged.neoforgespi.locating.IModFileReader         ← Early Service SPI
├── META-INF/jars/
│   ├── touchcontroller-common.jar                                ← 公共代码（GAMELIBRARY）
│   ├── touchcontroller-1.21.1.jar                                ← MC 1.21.1 特定代码（MOD）
│   ├── touchcontroller-1.21.10.jar                               ← MC 1.21.10 特定代码（MOD）
│   ├── multijar-neoforge-manifest.json                           ← 加载器清单
│   └── ...
└── NeoV3Locator.class                                            ← 自举加载器
```

外层 JAR 实际上不是一个 NeoForge 模组：它的作用只是加载嵌套的 JAR 文件。

### 为什么可以这么做

这个方案可行，依赖于 NeoForge 的三个基础设施：

#### 1. 早期服务发现（Early Service Discovery）

FML 在正式加载模组之前，会通过 `ServiceLoaderUtil.loadServices()` 发现所有提供 `IDependencyLocator`、`IModFileReader` 等 SPI 接口的 JAR，加载到早期服务 ClassLoader 中。

这些 JAR 在加载早期服务的过程中已被标记为"已定位"。后续 `ModsFolderLocator` 执行普通模组发现时，会跳过这些 JAR，因此外层 JAR 不会被当作普通模组加载。

#### 2. Dependency Locator 回调

普通模组发现完成后，`ModDiscoverer` 依次调用所有已发现的 `IDependencyLocator.scanMods(List.copyOf(loadedFiles), pipeline)`。我们在这个回调中扫描 mods 目录，找到含有 `multijar-neoforge-manifest.json` 的 JAR，读取版本清单，提取匹配的内部 JAR 并通过 `pipeline.readModFile()` + `pipeline.addModFile()` 将其注入模组列表。

#### 3. Minecraft 版本在此时已知

生产环境候选定位器（`ProductionClientProvider` / `ProductionServerProvider`）以 `HIGHEST_SYSTEM_PRIORITY` 优先运行，在 `IDependencyLocator` 执行之前已将 `minecraft` 模组加入 `loadedFiles`。因此在 `scanMods()` 中可以通过遍历 `loadedFiles` 获取当前 MC 版本，选择匹配的内部 JAR。
