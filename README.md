# TouchController

这是 TouchController 组织的代码集中存储仓库。这里有：

- TouchController：一款 Minecraft：Java 版的模组，添加了类基岩版的触摸控制系统。[README](touchcontroller/README.md)
- Combine：一个为 Minecraft：Java 版制作的 GUI 库。支持以 Compose 的风格构建跨 Minecraft 版本的 UI。
- BlazeRod：一个为 Minecraft：Java 版制作的模型渲染库。支持渲染 glTF、PMX 等复杂模型，也支持渲染基岩版实体模型，并支持骨骼动画、形态键（变形目标）等高级渲染特性。
- ArmorStand：一款 Minecraft：Java 版的模型渲染模组，使用 BlazeRod 进行渲染，支持替换玩家模型。[README](armorstand/README.md)
- AuthProxy: 一款 Minecraft：Java 版的模组，允许为服务器验证使用代理。
- BlazeSDL: 一款 Minecraft：Java 版的模组，将窗口管理库替换为 SDL3。
- PathFlow: 一个 Kotlin 库，用于定义数据的特征和转换规则，从而自动求解转换路径，完成数据转换。计划用于 BlazeRod 库，以自动完成不同格式的动画转换。
- FastMerger：一款为处理速度设计的 JAR shadowing + relocating 软件，为打包 Minecraft Mod 依赖而生。 

另外的，还有以下的 Bazel 规则：

- [rules_mc](https://github.com/TouchController/rules_mc)：一款用于在 Bazel 内下载获取 Minecraft 官方客户端 + 服务端，以及依赖库的规则。[README](rules_mc/README.md)

---

# TouchController Monorepo

This is the central repository of TouchController organization. Now there are:

- TouchController: A mod for Minecraft: Java Edition, adding touch control system like the Bedrock Edition. [README](touchcontroller/README.md)
- Combine: A GUI library for Minecraft: Java Edition. It supports building cross-version Minecraft UIs in a Compose-like style.
- BlazeRod: A model rendering library for Minecraft: Java Edition. It supports rendering complex models such as glTF and PMX, as well as Bedrock Edition entity models, and includes advanced rendering features like skeletal animation and morph targets (shape keys).
- ArmorStand: A model rendering mod for Minecraft: Java Edition, using BlazeRod for rendering, and supports replacing player models. [README](armorstand/README.md)
- AuthProxy: A mod for Minecraft: Java Edition, allowing using proxies for server authentication.
- BlazeSDL: A mod for Minecraft：Java Edition, replacing window management library to SDL3.
- PathFlow: A Kotlin library for defining data features and transformation rules, thereby automatically solving transformation paths and completing data transformations. It is planned to be used in the BlazeRod library to automate animation conversions of different formats.
- FastMerger: A software to do JAR shadowing + relocating designed for processing speed, born for packing Minecraft Mod dependencies.

Also, following Bazel rules are in this repo:

- [rules_mc](https://github.com/TouchController/rules_mc): A Bazel rule to download Minecraft official client + server, and their library dependencies. [README](rules_mc/README.md)
