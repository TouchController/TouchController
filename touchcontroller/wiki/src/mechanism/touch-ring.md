# TouchController 中的触控圈

<!-- ANCHOR: p1 -->
> 曾用名：触摸准星（TouchController v0.1.x 及以前）

- 触控圈可以用于在屏幕上任意位置所对应的方向进行交互，而不仅仅是屏幕中心的准星所指方向。
- 触控圈在激活时将充当准星，可进行放置、破坏、攻击等等交互。
<!-- ANCHOR_END: p1 -->

## 外观

<!-- ANCHOR: p2 -->
- 触控圈实际上一个是同心二十四边形。
- 触控圈及其破坏进度都是以反色渲染的。
<!-- ANCHOR_END: p2 -->

## 特性

<!-- ANCHOR: p3 -->
- 触控圈只能同时存在一个（除非实现 [#77](https://github.com/TouchController/TouchController/issues/77)）。
- 打开分离控制后将禁用触控圈。
- 触控圈激活后将代替原有的十字准心瞄准方块或实体。
<!-- ANCHOR_END: p3 -->

![](assets/touch-ring/file-20251116210314479.png)
