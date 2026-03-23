package top.fifthlight.combine.widget.ui

import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import top.fifthlight.combine.data.Text
import top.fifthlight.combine.data.TextFactory
import top.fifthlight.combine.input.MutableInteractionSource
import top.fifthlight.combine.input.key.Key
import top.fifthlight.combine.input.pointer.PointerIcon
import top.fifthlight.combine.input.text.LocalClipboard
import top.fifthlight.combine.input.text.TextInputState
import top.fifthlight.combine.input.text.TextRange
import top.fifthlight.combine.input.text.substring
import top.fifthlight.combine.layout.constraints.Constraints
import top.fifthlight.combine.layout.measure.Measurable
import top.fifthlight.combine.layout.measure.MeasurePolicy
import top.fifthlight.combine.layout.measure.MeasureResult
import top.fifthlight.combine.layout.measure.MeasureScope
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.modifier.drawing.border
import top.fifthlight.combine.modifier.drawing.clip
import top.fifthlight.combine.modifier.focus.FocusInteraction
import top.fifthlight.combine.modifier.focus.focusable
import top.fifthlight.combine.modifier.input.textInput
import top.fifthlight.combine.modifier.key.onKeyEvent
import top.fifthlight.combine.modifier.placement.minHeight
import top.fifthlight.combine.modifier.pointer.clickable
import top.fifthlight.combine.node.LocalInputHandler
import top.fifthlight.combine.paint.Colors
import top.fifthlight.combine.paint.TextMeasurer
import top.fifthlight.combine.theme.LocalTheme
import top.fifthlight.combine.ui.style.DrawableSet
import top.fifthlight.combine.widget.Canvas
import top.fifthlight.data.IntOffset
import top.fifthlight.data.IntRect
import top.fifthlight.data.IntSize

@Composable
fun EditText(
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    drawableSet: DrawableSet = LocalTheme.current.drawables.editText,
    value: String,
    onValueChanged: (String) -> Unit,
    onEnter: (() -> Unit)? = {},
    placeholder: Text? = null,
) {
    val clipboard = LocalClipboard.current
    val inputManager = LocalInputHandler.current
    var textInputState by remember { mutableStateOf(TextInputState(value)) }

    fun updateInputState(block: TextInputState.() -> TextInputState) {
        textInputState = block(textInputState)
        if (value != textInputState.text) {
            onValueChanged(textInputState.text)
        }
    }

    var focused by remember { mutableStateOf(false) }
    var cursorShow by remember { mutableStateOf(false) }
    var cursorRect by remember { mutableStateOf<IntRect?>(null) }
    var areaRect by remember { mutableStateOf<IntRect?>(null) }
    var scrollOffset by remember { mutableStateOf(0) }
    LaunchedEffect(interactionSource) {
        try {
            interactionSource.interactions.collect {
                when (it) {
                    FocusInteraction.Blur -> {
                        inputManager?.tryHideKeyboard()
                        focused = false
                    }

                    FocusInteraction.Focus -> {
                        inputManager?.tryShowKeyboard()
                        focused = true
                    }
                }
            }
        } finally {
            inputManager?.updateInputState(null)
            inputManager?.tryHideKeyboard()
        }
    }
    inputManager?.let {
        LaunchedEffect(textInputState, focused, cursorRect, areaRect) {
            if (focused) {
                inputManager.updateInputState(textInputState, cursorRect, areaRect)
            } else {
                inputManager.updateInputState(null)
            }
        }
        LaunchedEffect(focused) {
            if (focused) {
                inputManager.events.collect { newState ->
                    updateInputState { newState }
                }
            }
        }
    }
    LaunchedEffect(value) {
        if (value == textInputState.text) {
            return@LaunchedEffect
        }
        textInputState = TextInputState(value)
    }
    LaunchedEffect(focused) {
        if (focused) {
            while (true) {
                cursorShow = !cursorShow
                delay(500)
            }
        } else {
            cursorShow = false
        }
    }

    val state by widgetState(interactionSource)
    val drawable = drawableSet.getByState(state)

    Canvas(
        modifier = Modifier
            .clip()
            .minHeight(9)
            .border(drawable)
            .clickable(
                interactionSource = interactionSource,
                pointerIcon = PointerIcon.Edit,
            ) {
                inputManager?.tryShowKeyboard()
            }
            .focusable(interactionSource)
            .textInput { updateInputState { commitText(it) } }
            .onKeyEvent { event ->
                if (!event.pressed) {
                    return@onKeyEvent
                }
                when (event.key) {
                    Key.DELETE -> updateInputState { doDelete() }
                    Key.BACKSPACE -> updateInputState { doBackspace() }

                    Key.HOME -> if (event.modifier.onlyShift) {
                        updateInputState { doShiftHome() }
                    } else if (event.modifier.empty) {
                        updateInputState { doHome() }
                    }

                    Key.END -> if (event.modifier.onlyShift) {
                        updateInputState { doShiftEnd() }
                    } else if (event.modifier.empty) {
                        updateInputState { doEnd() }
                    }

                    Key.ARROW_LEFT -> if (event.modifier.onlyShift) {
                        updateInputState { doShiftLeft() }
                    } else if (event.modifier.empty) {
                        updateInputState { doArrowLeft() }
                    }

                    Key.ARROW_RIGHT -> if (event.modifier.onlyShift) {
                        updateInputState { doShiftRight() }
                    } else if (event.modifier.empty) {
                        updateInputState { doArrowRight() }
                    }

                    Key.C -> if (event.modifier.onlyControl) {
                        val selectionText = textInputState.selectionText
                        clipboard.text = selectionText
                    }

                    Key.V -> if (event.modifier.onlyControl) {
                        updateInputState { commitText(clipboard.text) }
                    }

                    Key.X -> if (event.modifier.onlyControl) {
                        val selectionText = textInputState.selectionText
                        clipboard.text = selectionText
                        updateInputState { removeSelection() }
                    }

                    Key.ENTER -> {
                        if (onEnter == null) {
                            updateInputState { commitText("\n") }
                        } else {
                            onEnter()
                        }
                    }

                    else -> {}
                }
            }
            .then(modifier),
        measurePolicy = object : MeasurePolicy {
            override fun MeasureScope.measure(
                measurables: List<Measurable>,
                constraints: Constraints,
            ): MeasureResult {
                val textToMeasure = value.ifEmpty { placeholder?.string ?: "A" }
                val textSize = TextMeasurer.measure(textToMeasure)
                return layout(
                    width = textSize.width.coerceIn(constraints.minWidth, constraints.maxWidth),
                    height = textSize.height.coerceIn(constraints.minHeight, constraints.maxHeight),
                ) {}
            }

            override fun MeasureScope.minIntrinsicWidth(
                measurables: List<Measurable>,
                height: Int,
            ): Int {
                val textToMeasure = value.ifEmpty { placeholder?.string ?: "A" }
                return TextMeasurer.measure(textToMeasure).width
            }

            override fun MeasureScope.minIntrinsicHeight(
                measurables: List<Measurable>,
                width: Int,
            ): Int {
                val textToMeasure = value.ifEmpty { placeholder?.string ?: "A" }
                return TextMeasurer.measure(textToMeasure).height
            }

            override fun MeasureScope.maxIntrinsicWidth(
                measurables: List<Measurable>,
                height: Int,
            ): Int {
                val textToMeasure = value.ifEmpty { placeholder?.string ?: "" }
                return TextMeasurer.measure(textToMeasure).width
            }

            override fun MeasureScope.maxIntrinsicHeight(
                measurables: List<Measurable>,
                width: Int,
            ): Int {
                val textToMeasure = value.ifEmpty { placeholder?.string ?: "A" }
                return TextMeasurer.measure(textToMeasure).height
            }
        }
    ) { canvas, node ->
        areaRect = IntRect(offset = node.absolutePosition, size = node.size)
        if (value.isEmpty() && !focused) {
            if (placeholder != null) {
                val textSize = TextMeasurer.measure(placeholder)
                val offsetY = (node.height - textSize.height) / 2
                canvas.drawText(
                    offset = IntOffset(0, offsetY),
                    width = node.width,
                    text = placeholder,
                    color = Colors.LIGHT_GRAY
                )
                cursorRect = IntRect(node.absolutePosition, IntSize(1, 9))
            }
        } else {
            val fullText = textInputState.text
            val textSize = TextMeasurer.measure(fullText)
            val offsetY = (node.height - textSize.height) / 2

            val selectionStartX = TextMeasurer.measure(fullText.substring(0, textInputState.selection.start)).width
            val selectionWidth = TextMeasurer.measure(textInputState.selectionText).width

            if (selectionWidth > 0) {
                canvas.fillRect(
                    offset = IntOffset(selectionStartX, offsetY),
                    size = IntSize(selectionWidth, textSize.height),
                    color = Colors.GRAY,
                )
            }

            val cursorWidth = 1
            // 计算光标位置
            val cursorX = if (textInputState.selectionLeft) {
                selectionStartX
            } else {
                selectionStartX + selectionWidth
            }

            // 调整滚动偏移量以确保光标在可见区域中
            val visibleAreaStart = scrollOffset
            val visibleAreaEnd = scrollOffset + node.width
            if (cursorX < visibleAreaStart) {
                scrollOffset = cursorX
            } else if (cursorX + cursorWidth > visibleAreaEnd) {
                scrollOffset = cursorX + cursorWidth - node.width
            }

            // 确保滚动偏移量在范围内
            scrollOffset = scrollOffset.coerceIn(0, (textSize.width - node.width + cursorWidth).coerceAtLeast(0))

            // 记录光标矩形（用于输入法）
            cursorRect = IntRect(
                IntOffset(cursorX - scrollOffset, offsetY) + node.absolutePosition,
                IntSize(1, textSize.height)
            )

            // 绘制光标（如果需要）
            if (cursorShow) {
                canvas.fillRect(
                    offset = IntOffset(cursorX - scrollOffset, offsetY),
                    size = IntSize(1, textSize.height),
                    color = Colors.WHITE,
                )
            }

            // 构建富文本（处理预编辑文本的下划线）
            val styledText = TextFactory.build {
                if (textInputState.composition != TextRange.EMPTY) {
                    val beforeComposition = fullText.take(textInputState.composition.start)
                    val compositionText = fullText.substring(textInputState.composition)
                    val afterComposition = fullText.substring(textInputState.composition.end)
                    append(beforeComposition)
                    underline { append(compositionText) }
                    append(afterComposition)
                } else {
                    append(fullText)
                }
            }

            // 绘制整个富文本
            canvas.drawText(
                offset = IntOffset(-scrollOffset, offsetY),
                text = styledText,
                color = Colors.WHITE
            )
        }
    }
}
