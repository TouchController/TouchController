package top.fifthlight.combine.widget.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import top.fifthlight.combine.input.MutableInteractionSource
import top.fifthlight.combine.input.pointer.PointerIcon
import top.fifthlight.combine.layout.measure.MeasurePolicy
import top.fifthlight.combine.layout.measure.MeasureScope.layout
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.modifier.focus.focusable
import top.fifthlight.combine.modifier.placement.height
import top.fifthlight.combine.modifier.pointer.draggable
import top.fifthlight.combine.theme.LocalTheme
import top.fifthlight.combine.ui.style.DrawableSet
import top.fifthlight.combine.widget.Canvas
import top.fifthlight.data.IntOffset
import top.fifthlight.data.IntRect
import top.fifthlight.data.IntSize

data class SliderDrawableSet(
    val activeTrack: DrawableSet,
    val inactiveTrack: DrawableSet?,
    val handle: DrawableSet,
) {
    companion object {
        val current
            @Composable
            get() = LocalTheme.current.let { theme ->
                SliderDrawableSet(
                    activeTrack = theme.drawables.sliderActiveTrack,
                    inactiveTrack = theme.drawables.sliderInactiveTrack,
                    handle = theme.drawables.sliderHandle,
                )
            }
    }
}

@Composable
fun Slider(
    modifier: Modifier = Modifier,
    drawableSet: SliderDrawableSet = SliderDrawableSet.current,
    range: ClosedFloatingPointRange<Float>,
    value: Float,
    onValueChanged: (Float) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val state by widgetState(interactionSource)

    fun Float.toValue() = this * (range.endInclusive - range.start) + range.start
    fun Float.toProgress() = (this - range.start) / (range.endInclusive - range.start)

    val progress = value.toProgress()

    val handleDrawable = drawableSet.handle.getByState(state)
    val handleLeftHalfWidth = handleDrawable.size.width / 2

    val activeTrackDrawable = drawableSet.activeTrack.getByState(state)
    val inactiveTrackDrawable = drawableSet.inactiveTrack?.getByState(state)

    Canvas(
        modifier = Modifier
            .draggable(
                interactionSource = interactionSource,
                pointerIcon = PointerIcon.ResizeHorizonal,
            ) { _, absolute ->
                val rawProgress = (absolute.x - handleLeftHalfWidth) / (size.width - handleDrawable.size.width)
                val newProgress = rawProgress.coerceIn(0f, 1f)
                onValueChanged(newProgress.toValue())
            }
            .focusable(interactionSource)
            .then(modifier),
        measurePolicy = { _, constraints ->
            layout(
                width = constraints.minWidth,
                height = maxOf(
                    activeTrackDrawable.size.height,
                    inactiveTrackDrawable?.size?.height ?: 0
                ).coerceIn(
                    constraints.minHeight,
                    constraints.maxHeight
                ),
            ) { }
        }
    ) { canvas, node ->
        val trackRect = IntRect(
            offset = IntOffset(
                x = handleLeftHalfWidth,
                y = 0
            ),
            size = IntSize(
                width = node.width - handleDrawable.size.width,
                height = node.height,
            ),
        )
        val progressWidth = (trackRect.size.width * progress).toInt()

        activeTrackDrawable.draw(canvas, trackRect)

        inactiveTrackDrawable?.draw(
            canvas = canvas,
            dstRect = IntRect(
                offset = trackRect.offset + IntOffset(
                    x = progressWidth,
                    y = 0,
                ),
                size = IntSize(
                    width = trackRect.size.width - progressWidth,
                    height = trackRect.size.height,
                ),
            ),
        )

        handleDrawable.draw(
            canvas = canvas,
            dstRect = IntRect(
                offset = IntOffset(
                    x = progressWidth,
                    y = 0,
                ),
                size = handleDrawable.size,
            ),
        )
    }
}

@Composable
fun IntSlider(
    modifier: Modifier = Modifier,
    drawableSet: SliderDrawableSet = SliderDrawableSet.current,
    range: IntRange,
    value: Int,
    onValueChanged: (Int) -> Unit,
) {
    fun Int.toProgress() = (this - range.first).toFloat() / (range.last - range.first)
    fun Float.toValue() = (this * (range.last - range.first)).toInt() + range.first

    Slider(
        modifier = modifier,
        drawableSet = drawableSet,
        range = 0f..1f,
        value = value.toProgress(),
        onValueChanged = {
            onValueChanged(it.toValue())
        },
    )
}
