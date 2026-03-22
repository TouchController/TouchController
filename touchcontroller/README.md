<h1 align="center">TouchController</h1>

<img src="resources/texture/icon.png" width="96" height="96" align="left" alt="TouchController icon" vspace="16" hspace="16" />

A mod bringing controlling style of bedrock version to Java version.

Currently in early development, welcome to report for bugs or other problems you found is welcome!
<br clear="left">
<div align="center">

[简体中文](#chinese) | [启动器适配文档 (How to add launcher support)](#launcher-support)

[![Modrinth](https://img.shields.io/modrinth/dt/touchcontroller?style=&logo=modrinth&color=008833)](https://modrinth.com/mod/touchcontroller) [![Issues](https://img.shields.io/github/issues/TouchController/TouchController?style=logo=github-square&color=F7E672)](https://github.com/TouchController/TouchController/issues) [![TC WIKI](https://img.shields.io/static/v1?label=TC%20WIKI&message=Online&color=F08B95)](https://tcwiki.fifthlight.top/) [![MC 百科](https://img.shields.io/badge/MC_%E7%99%BE%E7%A7%91-blue?style=&color=ae94ff)](https://www.mcmod.cn/class/17432.html)
</div>

## Download and install

You can download the latest release of TouchController mod
on [Modrinth](https://modrinth.com/mod/touchcontroller#download),
[GitHub Releases](https://github.com/TouchController/TouchController/releases)
and [MC 百科](https://www.mcmod.cn/download/17432.html).

On different mod loaders, TouchController needs dependency mod as below:

- Fabric: [Fabric API](https://github.com/FabricMC/fabric),
  [Fabric Language Kotlin](https://github.com/FabricMC/fabric-language-kotlin)
- Forge: None
- NeoForge: None

## Supported game version and platforms

Below are game versions and mod loaders TouchController supports:

- 1.12.2 (Forge)
- 1.16.5 (Forge, Fabric)
- 1.20.1 (Forge, Fabric)
- 1.20.4 (Forge, NeoForge, Fabric)
- 1.20.6 (Forge, NeoForge, Fabric)
- 1.21   (Forge, NeoForge, Fabric)
- 1.21.1 (Forge, NeoForge, Fabric)
- 1.21.3 (Forge, NeoForge, Fabric)
- 1.21.4 (Forge, NeoForge, Fabric)
- 1.21.5 (Forge, NeoForge, Fabric)
- 1.21.6 (Forge, NeoForge, Fabric)
- 1.21.7 (Forge, NeoForge, Fabric)
- 1.21.8 (Forge, NeoForge, Fabric)
- 1.21.11 (Fabric)

Support for more game versions and mod loaders is under development.

Below are platforms TouchController supports:

- Windows (minimum supported version is Windows 7, supports x86, x86_64 and ARM64 architecture)
- BlazeSDL
- Linux Wayland (without touch to mouse emulation support, prefer BlazeSDL)
- [Fold Craft Launcher](https://github.com/FCL-Team/FoldCraftLauncher)
- [Zalith Launcher](https://github.com/ZalithLauncher/ZalithLauncher)
- [Zalith Launcher 2](https://github.com/ZalithLauncher/ZalithLauncher2)
- [Pojav Glow·Worm](https://github.com/Vera-Firefly/Pojav-Glow-Worm)
- [Amethyst Android](https://github.com/AngelAuraMC/Amethyst-Android)

Support for touch screen for X11 on Linux may be added in the future. iOS support will be added in the future.

## Features supported by now

- Touch input of Minecraft Bedrock version style
- Customizable controller layout
- Ability to switch displaying buttons by conditions such as swimming and flying
- Provide haptic feedback when breaking blocks (Only supported on Android platform currently)

## Compile

At first, you need to install Android SDK & NDK, and set ANDROID_HOME / ANDROID_SDK_HOME environment variable.

And then, follow instruction on <https://bazel.build/install/bazelisk> to
install [Bazelisk](https://github.com/bazelbuild/bazelisk).

Finally, run `bazel build //touchcontroller/versions/fabric/1.21.11` (Fabric 1.21.11 for example) to build
TouchController.

---

<h1 id="chinese" align="center">TouchController</h1>
<img src="resources/texture/icon.png" width="96" height="96" align="left" alt="TouchController 图标" vspace="16" hspace="16" />

一个为 Minecraft Java 版添加触控支持的 Mod。

目前处于早期开发中，如果遇到 Bug 或者其他问题，欢迎积极报告！
<br clear="left">
<div align="center">

[![Modrinth](https://img.shields.io/modrinth/dt/touchcontroller?style=&logo=modrinth&color=008833)](https://modrinth.com/mod/touchcontroller) [![Issues](https://img.shields.io/github/issues/TouchController/TouchController?style=logo=github-square&color=F7E672)](https://github.com/TouchController/TouchController/issues) [![TC WIKI](https://img.shields.io/static/v1?label=TC%20WIKI&message=Online&color=F08B95)](https://wiki.touchcontroller.fifthlight.top/) [![MC 百科](https://img.shields.io/badge/MC_%E7%99%BE%E7%A7%91-blue?style=&color=ae94ff)](https://www.mcmod.cn/class/17432.html)
</div>

## 下载与安装

你可以在 [Modrinth](https://modrinth.com/mod/touchcontroller#download)、
[GitHub Releases](https://github.com/TouchController/TouchController/releases)
和 [MC 百科](https://www.mcmod.cn/download/17432.html) 上下载 TouchController 的最新发布版。

根据不同的 mod 加载器，TouchController 需要如下的前置 mod：

- Fabric：[Fabric API](https://github.com/FabricMC/fabric)、
  [Fabric Language Kotlin](https://github.com/FabricMC/fabric-language-kotlin)
- Forge：无前置
- NeoForge: 无前置

## 支持的游戏版本和平台

目前 TouchController 支持的 Minecraft 版本和 mod 加载器有：

- 1.12.2 (Forge)
- 1.16.5 (Forge, Fabric)
- 1.20.1 (Forge, Fabric)
- 1.20.4 (Forge, NeoForge, Fabric)
- 1.20.6 (Forge, NeoForge, Fabric)
- 1.21   (Forge, NeoForge, Fabric)
- 1.21.1 (Forge, NeoForge, Fabric)
- 1.21.3 (Forge, NeoForge, Fabric)
- 1.21.4 (Forge, NeoForge, Fabric)
- 1.21.5 (Forge, NeoForge, Fabric)
- 1.21.6 (Forge, NeoForge, Fabric)
- 1.21.7 (Forge, NeoForge, Fabric)
- 1.21.8 (Forge, NeoForge, Fabric)
- 1.21.11 (Fabric)

更多游戏版本、mod 加载器的支持正在开发中。

目前支持的平台有：

- Windows（版本最低为 Windows 7，支持 x86、x86_64 和 ARM64 架构）
- BlazeSDL
- Linux Wayland（但是没有触控模拟鼠标支持，建议使用 BlazeSDL）
- [Fold Craft Launcher](https://github.com/FCL-Team/FoldCraftLauncher)
- [Zalith Launcher](https://github.com/ZalithLauncher/ZalithLauncher)
- [Zalith Launcher 2](https://github.com/ZalithLauncher/ZalithLauncher2)
- [Pojav Glow·Worm](https://github.com/Vera-Firefly/Pojav-Glow-Worm)
- [Amethyst Android](https://github.com/AngelAuraMC/Amethyst-Android)

在未来可能会添加 Linux 上 X11 触屏的支持。未来会加入 iOS 支持。

## 目前支持的功能

- Minecraft 基岩版风格的触屏输入
- 可自定义的控制器布局
- 能够根据游泳、飞行等状态切换不同按键的显示
- 破坏方块时进行震动反馈（目前只支持 Android 平台）

## 编译

首先你需要安装 Android SDK 和 NDK，然后设置好 ANDROID_HOME 和 ANDROID_SDK_HOME 这两个环境变量。

然后，跟随 <https://bazel.build/install/bazelisk> 上的指导，安装 [Bazelisk](https://github.com/bazelbuild/bazelisk)。

最后运行 `bazel build //touchcontroller/versions/fabric/1.21.11` (以 Fabric 1.21.11 为例子)，就可以编译 TouchController
了。

---

<h2 id="launcher-support">添加新的启动器支持</h2>

If you can't read Chinese, feel free to use a translator.

欢迎添加其他启动器的支持！为其他启动器添加支持的步骤有：

1. 添加 TouchController 的 proxy-client 库到启动器内

    - Groovy
    ```groovy
    implementation 'top.fifthlight.touchcontroller:proxy-client-android:0.0.5'
    ```

    - Kotlin
    ```kotlin
    implementation("top.fifthlight.touchcontroller:proxy-client-android:0.0.5")
    ```

    - Gradle version catalogs
    ```toml
    touchcontroller-proxy-client-android = { group = "top.fifthlight.touchcontroller", name = "proxy-client-android", version = "0.0.5" }
    ```

2. 创建 MessageTransport

   目前版本的 TouchController 使用 Unix 套接字进行游戏和启动器之间的 IPC，因此需要先创建一个 UnixSocketTransport：

    ```java
    private static final String socketName = "YourLauncher";
    
    /* ... */
    
    MessageTransport transport = UnixSocketTransportKt.UnixSocketTransport(socketName);
    ```

   你还需要在游戏启动时将 Unix Socket 的名称通过 `TOUCH_CONTROLLER_PROXY_SOCKET` 环境变量传递给 mod。

    ```java
    Os.setenv("TOUCH_CONTROLLER_PROXY_SOCKET", socketName, true);
    ```

3. 创建一个 LauncherProxyClient

   有了 MessageTransport 后你就可以创建一个 LauncherProxyClient 了，这是实现启动器和游戏之间交互协议的类：

    ```java
    LauncherProxyClient client = new LauncherProxyClient(transport);
    ```

4. 创建一个 VibrationHandler（可选）

   TouchController 从 v0.0.12 版本开始支持震动反馈。首先你需要实现 VibrationHandler：

    ```kotlin
    interface VibrationHandler {
        fun vibrate(kind: VibrateMessage.Kind)
    }
    ```

   在 proxy-client-android 库中的 SimpleVibrationHandler 类实现了一个基本的
   VibrationHandler，可以作为参考，但是不建议直接使用这个类，因为这个类缺失震动强度、震动效果的调节：

    ```kotlin
    private val TAG = "SimpleVibrationHandler"
    
    class SimpleVibrationHandler(private val service: Vibrator) : LauncherProxyClient.VibrationHandler {
        override fun viberate(kind: VibrateMessage.Kind) {
            try {
                @Suppress("DEPRECATION")
                service.vibrate(100)
            } catch (ex: Exception) {
                Log.w(TAG, "Failed to trigger vibration", ex)
            }
        }
    }
    ```

   然后设置 VibrationHandler 到 LauncherProxyClient 中：

    ```java
    SimpleVibrationHandler handler = new SimpleVibrationHandler(vibrator);
    client.setVibrationHandler(handler);
    ```

5. 启动 LauncherProxyClient，并发送消息：

   调用 LauncherProxyClient 的 run() 方法，否则 LauncherProxyClient 不会发送任何消息到游戏：

    ```java
    client.run();
    ```

   然后调用 LauncherProxyClient 的以下方法更新触点：

    - addPointer：添加或者更新一个触点
    - removePointer：删除一个触点
    - clearPointer：清除所有的触点

   如果不想手动做消息处理，库内也提供了一个基于 FrameLayout 的 TouchControllerLayout 类，只要将游戏相关的 View 包含在内，然后将
   LauncherProxyClient 设置到 TouchControllerLayout 中即可发送处理触摸消息并发送。

   要注意的是消息中的 index 必须是单调递增的（与 Android 中可以复用 ID
   的行为相反），并且所有坐标的范围是相对于游戏显示区域的 [0.0, 1.0]，而不是屏幕坐标。
