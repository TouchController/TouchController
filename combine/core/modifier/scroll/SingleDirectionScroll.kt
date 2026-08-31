package top.fifthlight.combine.core.modifier.scroll

import androidx.compose.runtime.Composable
import top.fifthlight.combine.core.input.pointer.PointerEvent
import top.fifthlight.combine.core.input.pointer.PointerEventType
import top.fifthlight.combine.core.layout.constraints.Constraints
import top.fifthlight.combine.core.layout.measure.Measurable
import top.fifthlight.combine.core.layout.measure.MeasureResult
import top.fifthlight.combine.core.layout.measure.MeasureScope
import top.fifthlight.combine.core.layout.measure.Placeable
import top.fifthlight.combine.core.layout.measure.Placer
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.drawing.DrawModifierNode
import top.fifthlight.combine.core.modifier.drawing.LayoutModifierNode
import top.fifthlight.combine.core.modifier.pointer.PointerInputModifierNode
import top.fifthlight.combine.core.node.LayoutNode
import top.fifthlight.combine.core.node.plus
import top.fifthlight.combine.core.paint.BackgroundTexture
import top.fifthlight.combine.core.paint.Canvas
import top.fifthlight.combine.core.paint.Color
import top.fifthlight.data.*
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@Composable
fun Modifier.verticalScroll(
    reverse: Boolean,
) = verticalScroll(
    scrollState = rememberScrollState(),
    reverse = reverse,
)

@Composable
fun Modifier.verticalScroll(
    scrollState: SingleDirectionScrollState = rememberScrollState(),
    reverse: Boolean = false,
    background: BackgroundTexture? = null,
    backgroundScale: Float = 1f,
) = then(
    VerticalScrollNode(
        scrollState = scrollState,
        reverse = reverse,
        background = background,
        backgroundScale = backgroundScale
    )
)

@Composable
fun Modifier.horizontalScroll(
    reverse: Boolean,
) = horizontalScroll(
    scrollState = rememberScrollState(),
    reverse = reverse,
)

@Composable
fun Modifier.horizontalScroll(
    scrollState: SingleDirectionScrollState = rememberScrollState(),
    reverse: Boolean = false,
    background: BackgroundTexture? = null,
    backgroundScale: Float = 1f,
) = then(
    HorizontalScrollNode(
        scrollState = scrollState,
        reverse = reverse,
        background = background,
        backgroundScale = backgroundScale
    )
)

private abstract class SingleDirectionScrollNode<T : SingleDirectionScrollNode<T>> : LayoutModifierNode,
    DrawModifierNode,
    PointerInputModifierNode, Modifier.Node<SingleDirectionScrollNode<T>> {
    abstract val scrollState: SingleDirectionScrollState
    abstract val reverse: Boolean
    abstract val background: BackgroundTexture?
    abstract val backgroundScale: Float

    protected abstract val Offset.amount: Float
    protected abstract val Constraints.maxAmount: Int
    protected abstract fun Constraints.copyInDirection(): Constraints
    protected abstract val Placeable.sizeAmount: Int
    protected abstract fun Placeable.placeWithAmount(amount: Int)
    protected abstract fun MeasureScope.layout(placeable: Placeable, viewportAmount: Int, placer: Placer): MeasureResult
    protected abstract fun drawScrollBar(wrapperNode: Placeable, canvas: Canvas)
    protected abstract fun drawBackground(wrapperNode: Placeable, background: BackgroundTexture, canvas: Canvas)

    override fun onPointerEvent(
        event: PointerEvent,
        node: Placeable,
        layoutNode: LayoutNode,
        children: (PointerEvent) -> Boolean,
    ): Boolean {
        return when (event.type) {
            PointerEventType.Scroll -> {
                val scrollDelta = if (reverse) {
                    -event.effectiveDelta.amount
                } else {
                    event.effectiveDelta.amount
                }
                scrollState.updateProgress(
                    (scrollState.progress.value - scrollDelta * 12).toInt(),
                    animateOverscroll = true
                )
                true
            }

            PointerEventType.Press -> {
                scrollState.initialPointerPosition = event.position
                scrollState.startPointerPosition = null
                scrollState.scrolling = false
                scrollState.stopAnimation()
                children(event)
                true
            }

            PointerEventType.Cancel, PointerEventType.Release -> {
                scrollState.initialPointerPosition = null
                scrollState.startPointerPosition = null
                scrollState.updateProgress(scrollState.progress.value, animateOverscroll = true)
                if (scrollState.scrolling) {
                    scrollState.scrolling = false
                    true
                } else {
                    false
                }
            }

            PointerEventType.Move -> {
                val initialPosition = scrollState.initialPointerPosition
                if (scrollState.scrolling) {
                    val distance = if (reverse) {
                        (event.position.amount - scrollState.startPointerPosition!!.amount).roundToInt()
                    } else {
                        (scrollState.startPointerPosition!!.amount - event.position.amount).roundToInt()
                    }
                    scrollState.updateProgress(distance + scrollState.startProgress)
                    true
                } else if (initialPosition != null) {
                    val distance = if (reverse) {
                        (event.position.amount - initialPosition.amount)
                    } else {
                        (initialPosition.amount - event.position.amount)
                    }
                    if (distance.absoluteValue > 8) {
                        scrollState.scrolling = true
                        scrollState.startProgress = scrollState.progress.value
                        scrollState.startPointerPosition = event.position
                        children(event.copy(type = PointerEventType.Cancel))
                        true
                    } else {
                        false
                    }
                } else {
                    false
                }
            }

            else -> false
        }
    }

    override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
        val viewportMaxAmount = constraints.maxAmount
        if (viewportMaxAmount == Int.MAX_VALUE) {
            error("Bad maxWidth/maxHeight of *Scroll(): check nested scroll modifiers")
        }

        val placeable = measurable.measure(constraints.copyInDirection())

        val viewportAmount = placeable.sizeAmount.coerceAtMost(viewportMaxAmount)
        scrollState.contentAmount = placeable.sizeAmount
        scrollState.viewportAmount = viewportAmount

        val maxScrollOffset = (placeable.sizeAmount - viewportAmount).coerceAtLeast(0)
        val actualProgress = scrollState.actualProgress.value
        if (actualProgress > maxScrollOffset) {
            scrollState.updateProgress(maxScrollOffset)
        } else if (actualProgress < 0) {
            scrollState.updateProgress(0)
        }

        return layout(placeable, viewportAmount) {
            val amount = if (reverse) {
                -(maxScrollOffset - scrollState.progress.value)
            } else {
                -scrollState.progress.value
            }
            placeable.placeWithAmount(amount)
        }
    }

    override fun renderBefore(
        canvas: Canvas,
        wrapperNode: Placeable,
        node: LayoutNode,
        cursorPos: Offset,
    ) {
        canvas.pushClip(
            IntRect(
                offset = IntOffset(wrapperNode.absoluteX, wrapperNode.absoluteY),
                size = IntSize(wrapperNode.width, wrapperNode.height)
            ),
            IntRect(
                offset = IntOffset(wrapperNode.x, wrapperNode.y),
                size = IntSize(wrapperNode.width, wrapperNode.height)
            ),
        )
        background?.let { background ->
            drawBackground(wrapperNode, background, canvas)
        }
    }

    override fun renderAfter(
        canvas: Canvas,
        wrapperNode: Placeable,
        node: LayoutNode,
        cursorPos: Offset,
    ) {
        if (scrollState.viewportAmount < scrollState.contentAmount) {
            drawScrollBar(wrapperNode, canvas)
        }
        canvas.popClip()
    }

    companion object {
        private val wrapperFactory =
            LayoutModifierNode.wrapperFactory + DrawModifierNode.wrapperFactory + PointerInputModifierNode.wrapperFactory
    }

    override val wrapperFactory
        get() = Companion.wrapperFactory
}

private data class VerticalScrollNode(
    override val scrollState: SingleDirectionScrollState,
    override val reverse: Boolean,
    override val background: BackgroundTexture?,
    override val backgroundScale: Float,
) : SingleDirectionScrollNode<VerticalScrollNode>() {
    override val Offset.amount get() = y
    override val Constraints.maxAmount get() = maxHeight
    override fun Constraints.copyInDirection() = copy(minHeight = minHeight, maxHeight = Int.MAX_VALUE)
    override val Placeable.sizeAmount: Int get() = height
    override fun Placeable.placeWithAmount(amount: Int) = placeAt(0, amount)
    override fun MeasureScope.layout(
        placeable: Placeable,
        viewportAmount: Int,
        placer: Placer
    ) = layout(
        width = placeable.width,
        height = viewportAmount,
        placer = placer,
    )

    override fun drawScrollBar(wrapperNode: Placeable, canvas: Canvas) {
        val progress = scrollState.progress.value.toFloat() / (scrollState.contentAmount - scrollState.viewportAmount).toFloat()
        val barAmount = (wrapperNode.height * scrollState.viewportAmount / scrollState.contentAmount).coerceAtLeast(12)
        val barY = ((wrapperNode.height - barAmount) * if (reverse) {
            1f - progress
        } else {
            progress
        }).roundToInt()
        canvas.fillRect(
            offset = IntOffset(wrapperNode.width - 3, barY),
            size = IntSize(3, barAmount),
            color = Color(0x66FFFFFFu),
        )
    }

    override fun drawBackground(wrapperNode: Placeable, background: BackgroundTexture, canvas: Canvas) {
        val height = background.size.height
        if (height == 0) {
            return
        }
        val tileHeight = height * backgroundScale
        val tileOffset = scrollState.progress.value.toFloat() % tileHeight
        background.draw(
            canvas = canvas,
            scale = backgroundScale,
            dstRect = Rect(
                offset = Offset(
                    x = 0f,
                    y = -tileHeight - tileOffset,
                ),
                size = Size(
                    width = wrapperNode.width.toFloat(),
                    height = wrapperNode.height.toFloat() + tileHeight * 2,
                ),
            )
        )
    }
}

private data class HorizontalScrollNode(
    override val scrollState: SingleDirectionScrollState,
    override val reverse: Boolean,
    override val background: BackgroundTexture?,
    override val backgroundScale: Float,
) : SingleDirectionScrollNode<HorizontalScrollNode>() {
    override val Offset.amount get() = x
    override val Constraints.maxAmount get() = maxWidth
    override fun Constraints.copyInDirection() = copy(minWidth = minWidth, maxWidth = Int.MAX_VALUE)
    override val Placeable.sizeAmount: Int get() = width
    override fun Placeable.placeWithAmount(amount: Int) = placeAt(amount, 0)
    override fun MeasureScope.layout(
        placeable: Placeable,
        viewportAmount: Int,
        placer: Placer
    ) = layout(
        width = viewportAmount,
        height = placeable.height,
        placer = placer,
    )

    override fun drawScrollBar(wrapperNode: Placeable, canvas: Canvas) {
        val progress = scrollState.progress.value.toFloat() / (scrollState.contentAmount - scrollState.viewportAmount).toFloat()
        val barAmount = (wrapperNode.width * scrollState.viewportAmount / scrollState.contentAmount).coerceAtLeast(12)
        val barX = ((wrapperNode.width - barAmount) * if (reverse) {
            1f - progress
        } else {
            progress
        }).roundToInt()
        canvas.fillRect(
            offset = IntOffset(barX, wrapperNode.height - 3),
            size = IntSize(barAmount, 3),
            color = Color(0x66FFFFFFu),
        )
    }

    override fun drawBackground(wrapperNode: Placeable, background: BackgroundTexture, canvas: Canvas) {
        val width = background.size.width
        if (width == 0) {
            return
        }
        val tileWidth = width * backgroundScale
        val tileOffset = scrollState.progress.value.toFloat() % tileWidth
        background.draw(
            canvas = canvas,
            scale = backgroundScale,
            dstRect = Rect(
                offset = Offset(
                    x = -tileWidth - tileOffset,
                    y = 0f,
                ),
                size = Size(
                    width = wrapperNode.width.toFloat() + tileWidth * 2,
                    height = wrapperNode.height.toFloat(),
                ),
            )
        )
    }
}
