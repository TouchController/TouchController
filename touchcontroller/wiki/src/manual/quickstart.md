# TouchController 从入门到精通

## 支持版本

> 参见 [#4](https://github.com/TouchController/TouchController/issues/4)

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

## 支持平台

- Windows：版本最低为 Windows 7，支持 x86、x86_64 和 ARM64 架构
- Linux：目前只支持 Wayland 显示协议，不支持 X11（包括 XWayland）；只支持 glibc，暂时不支持 musl；只支持 x86_64、x86、armv7、armv8 架构
- [Fold Craft Launcher](https://github.com/FCL-Team/FoldCraftLauncher)
- [Zalith Launcher](https://github.com/ZalithLauncher/ZalithLauncher)
- [Zalith Launcher 2](https://github.com/ZalithLauncher/ZalithLauncher2)
- [Angel Aura Amethyst](https://github.com/AngelAuraMC/Amethyst-Android)
- [Pojav Glow·Worm](https://github.com/Vera-Firefly/Pojav-Glow-Worm)

## 下载

> [!IMPORTANT]
> 请务必注意您的游戏版本和 mod 加载器，不要下载错误的版本。
>
> 请务必注意您使用的平台、游戏版本以及加载器是否支持 TouchController。

### 使用启动器下载

 1. 在模组安装页面中搜索 TouchController。
 2. 根据启动器的提示，根据安装到的 Minecraft 版本和 mod 加载器，寻找对应的最新版 TouchController。如果使用了 Fabric 版本，还需要安装 Fabric API，不过大部分启动器可以选择在安装游戏版本时一并安装 Fabric API，此时无需重复安装。
 3. Fabric 版本还可以选择安装 [模组菜单（Mod Menu）](https://modrinth.com/mod/modmenu) 这样可以更方便地进入设置页面。

### 从网站下载

#### 网址

- <https://www.mcmod.cn/download/17432.html>
- <https://modrinth.com/mod/touchcontroller/versions>
- <https://www.curseforge.com/minecraft/mc-mods/touchcontroller/files/all?showAlphaFiles=show>
- <https://github.com/TouchController/TouchController/releases>
- <https://gitee.com/fifth_light/TouchController/releases>

#### 安装

1. 找到 mod 文件夹，一般在 `.minecraft/mods`，如果开启了版本隔离，mod 文件夹就在 `.minecraft/versions/<游戏版本名称>/mods`。
2. 将下载的 TouchController 模组文件放进去，如果使用了 Fabric 版本，还需要安装 Fabric API。

## 游玩

目前 [GUI 控件](../widget/gui-widget.md)功能仍在开发中，关闭物品栏、呼出游戏菜单等 `Escape` 按键的功能可以由 **Android** 系统中的**返回**所替代。

### 隐藏启动器的控件

大部分启动器都有控件编辑功能，您可以删除启动器自带的控件。以 FCL 为例，您只需要新建一个空布局，删除原布局后进入游戏，便可以看到启动器自带的控件已被删除。

### 设置界面

您可以通过多种方法进入 TouchController 的设置界面。关于如何设置，请参考[设置界面](../gui/config-screen.md)及其子页面。

#### 通过模组菜单

- 打开模组菜单。如果使用 Fabric 加载器，需要安装[模组菜单](https://modrinth.com/mod/modmenu)，不过 Forge 自带模组菜单。
- 在模组菜单中找到 TouchController，便可以进入设置页面。

#### 通过游戏菜单

- 打开`游戏菜单 -> 选项 -> 按键控制 -> 触摸设置`。
- 一些版本进入方式可能有些许不同，但差别不会太大。

### 控件样式

#### 概念

{{#include ../widget/widget-style.md:p1}}

#### 图集

> 经典

{{#include ../widget/widget-style.md:p2}}

> 新样式

{{#include ../widget/widget-style.md:p3}}

> 经典扩展

{{#include ../widget/widget-style.md:p4}}

> 新样式回归

{{#include ../widget/widget-style.md:p5}}

### 选择喜欢的布局

{{#include ../mechanism/custom-layout/mode-switch.md:p1}}

{{#include ../mechanism/custom-layout/mode-switch.md:p2}}

> 预设模式选项：

{{#include ../gui/config-screen/sub-pages/manage-widget-preset.md:p1}}

{{#include ../gui/config-screen/sub-pages/manage-widget-preset.md:p2}}

### 自定义布局

#### 预设系统

{{#include ../mechanism/custom-layout/preset-system.md:p1}}

{{#include ../mechanism/custom-layout/preset-system.md:p2}}

#### 图层系统

{{#include ../mechanism/custom-layout/layer-system.md:p1}}

{{#include ../mechanism/custom-layout/layer-system.md:p2}}

> 图层编辑

{{#include ../mechanism/custom-layout/layer-system.md:p3}}

> 条件状态

{{#include ../mechanism/custom-layout/layer-system.md:p4}}

> 预置条件

{{#include ../mechanism/custom-layout/layer-system.md:p5}}

> 自定义图层条件

{{#include ../mechanism/custom-layout/custom-conditions.md:p1}}

{{#include ../mechanism/custom-layout/custom-conditions.md:p2}}

#### 内置控件

{{#include ../widget/in-game-widget.md:p1}}

> 方向键

{{#include ../widget/in-game-widget.md:p2}}

> 虚拟摇杆

{{#include ../widget/in-game-widget.md:p3}}

> 划船按钮

{{#include ../widget/in-game-widget.md:p4}}

#### 自定义控件

{{#include ../mechanism/custom-layout/custom-widget.md:p1}}

> 外观

{{#include ../mechanism/custom-layout/custom-widget.md:p2}}

> 动作

{{#include ../mechanism/custom-layout/custom-widget.md:p3}}

> 触发

{{#include ../mechanism/custom-layout/custom-widget.md:p4}}

#### 锚点机制

{{#include ../mechanism/anchor-mechanism.md:p1}}

{{#include ../mechanism/anchor-mechanism.md:p2}}

### 静默转头

{{#include ../mechanism/slient-turning-head.md:p1}}

> 机制

{{#include ../mechanism/slient-turning-head.md:p2}}

### 文本输入适配

{{#include ../mechanism/input-support.md:p1}}

#### Windows 系统

> [!TIP]
> Windows 系统中仅实现了自动弹出键盘。

{{#include ../mechanism/input-support.md:p2}}

#### Linux 系统

{{#include ../mechanism/input-support.md:p3}}

#### Android 系统

> [!IMPORTANT]
> Android 系统中的相关功能需要启动器的支持

{{#include ../mechanism/input-support.md:p4}}

### 触控圈

{{#include ../mechanism/touch-ring.md:p1}}

#### 外观

{{#include ../mechanism/touch-ring.md:p2}}

#### 特性

{{#include ../mechanism/touch-ring.md:p3}}

### 聊天界面

{{#include ../gui/chat-screen.md:p1}}

> 布局

{{#include ../gui/chat-screen.md:p2}}
