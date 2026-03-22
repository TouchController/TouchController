<h1 align="center">TouchController</h1>

<img src="static/logo.png" width="96" height="96" align="left" alt="TouchController icon" vspace="16" hspace="16" />

A mod bringing controlling style of bedrock version to Java version.

Currently in early development, welcome to report for bugs or other problems you found is welcome!
<br clear="left">
<div align="center">

[简体中文](#chinese) | [启动器适配文档 (How to add launcher support)](#launcher-support)

[![CI](https://github.com/TouchController/TouchController/actions/workflows/build.yml/badge.svg)](https://github.com/TouchController/TouchController/actions) [![Modrinth](https://img.shields.io/modrinth/dt/touchcontroller?style=&logo=modrinth&color=008833)](https://modrinth.com/mod/touchcontroller) [![Issues](https://img.shields.io/github/issues/TouchController/TouchController?style=logo=github-square&color=F7E672)](https://github.com/TouchController/TouchController/issues) [![TC WIKI](https://img.shields.io/static/v1?label=TC%20WIKI&message=Online&color=F08B95)](https://wiki.touchcontroller.fifthlight.top/) [![MC 百科](https://img.shields.io/badge/MC_%E7%99%BE%E7%A7%91-blue?style=&color=ae94ff)](https://www.mcmod.cn/class/17432.html)
</div>

## Download and install

You can download the latest release of TouchController mod
on [Modrinth](https://modrinth.com/mod/touchcontroller#download), [GitHub Releases](https://github.com/TouchController/TouchController/releases)
and [MC 百科](https://www.mcmod.cn/download/17432.html).

On different mod loaders, TouchController needs dependency mod as below:

- Fabric: [Fabric API](https://github.com/FabricMC/fabric)
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

Support for more game versions and mod loaders is in developing currently.

Below are platforms TouchController supports:

- Windows (Minimum supported version is Windows 7, supports x86, x86_64 and ARM64 architecture)
- [Fold Craft Launcher](https://github.com/FCL-Team/FoldCraftLauncher)
- [Zalith Launcher](https://github.com/ZalithLauncher/ZalithLauncher)
- [Zalith Launcher 2](https://github.com/ZalithLauncher/ZalithLauncher2)
- [Pojav Glow·Worm](https://github.com/Vera-Firefly/Pojav-Glow-Worm)
- [GlitchLauncher iOS](https://github.com/roggy666/GlitchLauncher-iOS)

Support for touch screen for X11 on Linux may be added in the future. iOS and macOS will not be officially supported because I don't have corresponding environment and devices, but pull requests for iOS and macOS support are welcome.

## Features supported by now

- Touch input of Minecraft Bedrock version style
- Customizable controller layout
- Ability to switch displaying buttons by conditions such as swimming and flying
- Provide haptic feedback when breaking blocks (Only supported on Android platform currently)

## Compile

At first, Rust compiler is required, you can install it with [rustup](https://rustup.rs/).

And then, you need to install Rust toolchains for targets below:

- armv7-linux-androideabi
- aarch64-linux-android
- i686-linux-android
- x86_64-linux-android

You can add these toolchain by `rustup target add <toolchain target>`.

You also need install the Android SDK which is able to install in Android Studio, and create a file named
`local.properties` in the project root directory with content below:

```
sdk.dir=<Android SDK directory>
```

By default Android Studio will do this for you. If you opened this project in Android Studio, there is no need for you
to configure this file.

You need to install the Android SDK, which is also able to install in Android Studio. After you installed the Android
SDK, install the `cargo-ndk` tool by using `cargo install cargo-ndk`.

You need to build two podman images: `touchcontroller-linux` and `llvm-mingw-jdk`. Make sure you installed podman, and
run commands below:

```shell
podman build -t touchcontroller-linux proxy-linux
podman build -t llvm-mingw-jdk proxy-windows
```

Finally, you can run `./gradlew build` to compile, and built file can bu found in `mod/build/libs`.

---

<h1 id="chinese" align="center">TouchController</h1>
<img src="static/logo.png" width="96" height="96" align="left" alt="TouchController 图标" vspace="16" hspace="16" />

一个为 Minecraft Java 版添加触控支持的 Mod。

目前处于早期开发中，如果遇到 Bug 或者其他问题，欢迎积极报告！
<br clear="left">
<div align="center">

[![CI](https://github.com/TouchController/TouchController/actions/workflows/build.yml/badge.svg)](https://github.com/TouchController/TouchController/actions) [![Modrinth](https://img.shields.io/modrinth/dt/touchcontroller?style=&logo=modrinth&color=008833)](https://modrinth.com/mod/touchcontroller) [![Issues](https://img.shields.io/github/issues/TouchController/TouchController?style=logo=github-square&color=F7E672)](https://github.com/TouchController/TouchController/issues) [![TC WIKI](https://img.shields.io/static/v1?label=TC%20WIKI&message=Online&color=F08B95)](https://wiki.touchcontroller.fifthlight.top/) [![MC 百科](https://img.shields.io/badge/MC_%E7%99%BE%E7%A7%91-blue?style=&color=ae94ff)](https://www.mcmod.cn/class/17432.html)
</div>

## 下载与安装
你可以在 [Modrinth](https://modrinth.com/mod/touchcontroller#download)、[GitHub Releases](https://github.com/TouchController/TouchController/releases)
和 [MC 百科](https://www.mcmod.cn/download/17432.html) 上下载 TouchController 的最新发布版。

根据不同的 mod 加载器，TouchController 需要如下的前置 mod：

- Fabric：[Fabric API](https://github.com/FabricMC/fabric)
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

更多游戏版本、mod 加载器的支持正在开发中。

目前支持的平台有：

- Windows（版本最低为 Windows 7，支持 x86、x86_64 和 ARM64 架构）
- [Fold Craft Launcher](https://github.com/FCL-Team/FoldCraftLauncher)
- [Zalith Launcher](https://github.com/ZalithLauncher/ZalithLauncher)
- [Zalith Launcher 2](https://github.com/ZalithLauncher/ZalithLauncher2)
- [Pojav Glow·Worm](https://github.com/Vera-Firefly/Pojav-Glow-Worm)
- [GlitchLauncher iOS](https://github.com/roggy666/GlitchLauncher-iOS)

在未来可能会添加 Linux 上 X11 触屏的支持。iOS 和 macOS 由于没有相应环境和设备，不会提供官方支持，但是接受 iOS 和 macOS 支持的 Pull Request。

## 目前支持的功能

- Minecraft 基岩版风格的触屏输入
- 可自定义的控制器布局
- 能够根据游泳、飞行等状态切换不同按键的显示
- 破坏方块时进行震动反馈（目前只支持 Android 平台）

## 编译

首先你需要 Rust 编译器，可以使用 [rustup](https://rustup.rs/) 安装。

接下来你需要安装以下几个目标的 Rust 工具链：

- armv7-linux-androideabi
- aarch64-linux-android
- i686-linux-android
- x86_64-linux-android

这些工具链可以用 `rustup target add <工具链目标>` 添加。

你还需要一份 Android SDK，可以在 Android Studio 内安装，然后在项目根目录创建 `local.properties`，其中内容如下：

```
sdk.dir=<Android SDK 目录>
```

默认 Android Studio 会帮你做这件事情，如果你用 Android Studio 打开过这个项目，则不需要配置这个选项。

接下来你还需要安装一份 Android NDK，同样也可以在 Android Studio 内安装，安装完后使用 `cargo install cargo-ndk` 安装
`cargo-ndk` 工具。

你需要构建两个 podman 镜像：`touchcontroller-linux` and `llvm-mingw-jdk`。确保你已经安装了 podman，然后运行以下命令：

```shell
podman build -t touchcontroller-linux proxy-linux
podman build -t llvm-mingw-jdk proxy-windows
```

最后运行 `./gradlew build` 就可以编译了，编译好的 mod 文件在 `mod/build/libs` 下。

---

<h2 id="launcher-support">添加新的启动器支持</h2>

If you can't read Chinese, feel free to use a translator.

欢迎添加其他启动器的支持！为其他启动器添加支持的步骤有：

1. 添加 TouchController 的 proxy-client 库到启动器内

    - Groovy
    ```groovy
    implementation 'top.fifthlight.touchcontroller:proxy-client-android:0.0.3'
    ```
    
    - Kotlin
    ```kotlin
    implementation("top.fifthlight.touchcontroller:proxy-client-android:0.0.3")
    ```
    
    - Gradle version catalogs
    ```toml
    touchcontroller-proxy-client-android = { group = "top.fifthlight.touchcontroller", name = "proxy-client-android", version = "0.0.3" }
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
    
    在 proxy-client-android 库中的 SimpleVibrationHandler 类实现了一个基本的 VibrationHandler，可以作为参考，但是不建议直接使用这个类，因为这个类缺失震动强度、震动效果的调节：
    
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
    
    如果不想手动做消息处理，库内也提供了一个基于 FrameLayout 的 TouchControllerLayout 类，只要将游戏相关的 View 包含在内，然后将 LauncherProxyClient 设置到 TouchControllerLayout 中即可发送处理触摸消息并发送。
    
    要注意的是消息中的 index 必须是单调递增的（与 Android 中可以复用 ID 的行为相反），并且所有坐标的范围是相对于游戏显示区域的 [0.0, 1.0]，而不是屏幕坐标。

## Star 历史

<a href="https://star-history.com/#TouchController/TouchController&Date">
 <picture>
   <source media="(prefers-color-scheme: dark)" srcset="https://api.star-history.com/svg?repos=TouchController/TouchController&type=Date&theme=dark" />
   <source media="(prefers-color-scheme: light)" srcset="https://api.star-history.com/svg?repos=TouchController/TouchController&type=Date" />
   <img alt="Star 历史图表" src="https://api.star-history.com/svg?repos=TouchController/TouchController&type=Date" />
 </picture>
</a>
