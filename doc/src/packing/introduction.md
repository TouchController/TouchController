# 介绍

曾经的 TouchController 每个版本打个包，不论是构建上用 R8 打包、还是发布时逐个发布，都非常麻烦。
TouchController 0.3 采用多合一 JAR 策略，把所有的版本支持全部打包在一个 JAR 内，解决所有的问题。

## 两种加载器，两种思路

对于 Fabric 和 NeoForge，实现方式不同：

- **Fabric** 的 Jar-in-Jar 机制采用尽力加载的机制，因此可以利用版本约束实现根据 Minecraft 版本的动态加载。
- **NeoForge** 的 JarJar 系统是为库依赖版本选择的，不支持按 Minecraft 版本条件加载，因此我们需要一些黑魔法。

下文分别介绍两种加载器下的打包方案，以及最终的统一 JAR 架构。
