package top.fifthlight.combine.widget.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import top.fifthlight.combine.animation.animateFloatAsState
import top.fifthlight.combine.input.MutableInteractionSource
import top.fifthlight.combine.layout.constraints.coerceIn
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.modifier.focus.focusable
import top.fifthlight.combine.modifier.pointer.toggleable
import top.fifthlight.combine.sound.LocalSoundManager
import top.fifthlight.combine.sound.SoundKind
import top.fifthlight.combine.sound.SoundManager
import top.fifthlight.combine.theme.LocalTheme
import top.fifthlight.combine.ui.style.DrawableSet
import top.fifthlight.combine.ui.style.TextureSet
import top.fifthlight.combine.widget.Canvas
import top.fifthlight.data.IntOffset
import top.fifthlight.data.IntRect
import top.fifthlight.data.IntSize
import kotlin.math.max
import kotlin.math.roundToInt

data class SwitchDrawableSet(
    val frame: DrawableSet,
    val background: TextureSet? = null,
    val handle: DrawableSet,
) {
    companion object {
        val current
            @Composable get() = LocalTheme.current.let { theme ->
                SwitchDrawableSet(
                    frame = theme.drawables.switchFrame,
                    background = theme.drawables.switchBackground,
                    handle = theme.drawables.switchHandle,
                )
            }
    }
}

@Composable
fun Switch(
    modifier: Modifier = Modifier,
    drawableSet: SwitchDrawableSet = SwitchDrawableSet.current,
    enabled: Boolean = true,
    value: Boolean,
    clickSound: Boolean = true,
    onValueChanged: ((Boolean) -> Unit)?,
) {
    val soundManager: SoundManager = LocalSoundManager.current
    val interactionSource = remember { MutableInteractionSource() }
    val state by widgetState(interactionSource)
    val frameDrawable = drawableSet.frame.getByState(state, enabled = enabled)
    val backgroundTexture = drawableSet.background?.getByState(state, enabled = enabled)
    val handleDrawable = drawableSet.handle.getByState(state, enabled = enabled)

    val modifier = if (onValueChanged == null || !enabled) {
        modifier
    } else {
        Modifier
            .toggleable(
                interactionSource,
                value,
            ) {
                if (clickSound) {
                    soundManager.play(SoundKind.BUTTON_PRESS, 1f)
                }
                onValueChanged(it)
            }
            .focusable(interactionSource)
            .then(modifier)
    }

    val size = IntSize(
        width = frameDrawable.size.width,
        height = max(frameDrawable.size.height, handleDrawable.size.height),
    )

    val handleValue by animateFloatAsState(if (value) 1f else 0f)

    Canvas(
        modifier = modifier,
        measurePolicy = { _, constraints -> layout(size.coerceIn(constraints)) {} },
    ) { canvas, node ->
        val frameRect = IntRect(
            offset = IntOffset(
                x = 0,
                y = (node.size.height - handleDrawable.size.height) / 2,
            ),
            size = frameDrawable.size
        )
        val handleMoveWidth = frameDrawable.size.width - handleDrawable.size.width
        val handleMoveOffsetX = (handleMoveWidth * handleValue).roundToInt()
        backgroundTexture?.let {
            val backgroundInitialOffsetX = (backgroundTexture.size.width - handleDrawable.size.width) / 2
            backgroundTexture.draw(
                canvas = canvas,
                srcRect = IntRect(
                    offset = IntOffset(
                        x = backgroundInitialOffsetX - handleMoveOffsetX,
                        y = (node.size.height - backgroundTexture.size.height) / 2
                    ),
                    size = IntSize(
                        width = frameRect.size.width,
                        height = backgroundTexture.size.height,
                    )
                ).toRect(),
                dstRect = frameRect.toRect(),
            )
        }
        frameDrawable.draw(canvas = canvas, dstRect = frameRect)
        handleDrawable.let {
            handleDrawable.draw(
                canvas = canvas,
                dstRect = IntRect(
                    offset = IntOffset(
                        x = handleMoveOffsetX,
                        y = (node.size.height - handleDrawable.size.height) / 2,
                    ),
                    size = handleDrawable.size,
                ),
            )
        }
    }
}
