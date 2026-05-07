# Fabric 打包

Fabric Loader 里对于不同来源的模组有不同优先级：mods 下的模组是顶层模组，它们**必须**加载；而 JiJ 的嵌套模组只会**尽力**加载，如果依赖关系
不满足则会静默跳过，这就给我们留下了很大的操作空间。

## 最终 JAR 结构

```
touchcontroller-fabric.jar
├── fabric.mod.json                          ← 根模组元数据，声明所有嵌套 JAR
├── META-INF/jars/
│   ├── touchcontroller-1-21-1.jar           ← 仅在 MC 1.21.1 时加载
│   ├── touchcontroller-1-21-10.jar          ← 仅在 MC 1.21.10 时加载
│   ├── touchcontroller-1-21-11.jar          ← 仅在 MC 1.21.11 时加载
│   ├── touchcontroller-26-1.jar             ← 仅在 MC 26.1 时加载
│   ├── combine-1-21-1.jar                   ← Combine 库，仅在 MC 1.21.1 时加载
│   ├── combine-1-21-10.jar                  ← Combine 库，仅在 MC 1.21.10 时加载
│   ├── touchcontroller-api.jar              ← 公共 API（无版本限制，始终加载）
│   └── ...其他 JiJ 依赖
└── ...
```

通过每个版本特定的 JAR 都携带一个独立的 `fabric.mod.json`，声明了 `"depends": { "minecraft": "1.21.1" }` 等版本约束，我们就可以让
给定的 JiJ 只在条件满足时才加载。
