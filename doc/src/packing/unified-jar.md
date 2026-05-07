# 统一 JAR 架构

前面分别介绍了 Fabric 和 NeoForge 下的打包方案。很巧的是：它们并不冲突。因此我们完全可以实现 Fabric + NeoForge 双加载器模组。

## 最终 JAR 结构

```
touchcontroller.jar
│
│   # 加载器元数据（各自只读自己认识的部分）
├── fabric.mod.json                                            ← Fabric Loader 读取
├── META-INF/neoforge.mods.toml                                ← 供启动器检测
├── META-INF/services/
│   ├── net.neoforged.neoforgespi.locating.IDependencyLocator  ← NeoForge 早期服务
│   └── net.neoforged.neoforgespi.locating.IModFileReader      ← NeoForge 早期服务
│
│   # JiJ 嵌套 JAR（Fabric + NeoForge 共享同一目录）
├── META-INF/jars/
│   ├── multijar-neoforge-manifest.json        ← NeoForge 自举定位器的版本清单
│   │
│   │  # 加载器无关的公共代码（始终加载）
│   ├── touchcontroller-common.jar             ← 公共代码
│   ├── touchcontroller-api.jar                ← 公共 API
│   ├── combine-common.jar                     ← Combine 公共代码
│   │
│   │  # 26.1+ 无混淆版本
│   ├── touchcontroller-common-26.1.jar        ← 版本相关、加载器无关
│   ├── touchcontroller-fabric-26.1.jar        ← Fabric 专属
│   ├── touchcontroller-neoforge-26.1.jar      ← NeoForge 专属
│   ├── combine-common-26.1.jar                ← Combine 版本相关、加载器无关
│   ├── combine-fabric-26.1.jar                ← Combine Fabric 专属
│   ├── combine-neoforge-26.1.jar              ← Combine NeoForge 专属
│   │
│   │  # 26.1- 有混淆版本
│   ├── touchcontroller-fabric-1.21.1.jar      ← Fabric，intermediary 映射
│   ├── touchcontroller-neoforge-1.21.1.jar    ← NeoForge，mojmap 映射
│   ├── combine-fabric-1.21.1.jar              ← Combine Fabric，intermediary 映射
│   ├── combine-neoforge-1.21.1.jar            ← Combine NeoForge，mojmap 映射
│   │
│   │  # ... 其他版本同理
│   ├── touchcontroller-fabric-1.21.10.jar
│   ├── touchcontroller-neoforge-1.21.10.jar
│   └── ...
│
└── NeoV3Locator.class                         ← NeoForge 自举加载器
```
