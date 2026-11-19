/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom

import androidx.compose.runtime.*
import top.fifthlight.combine.core.layout.Layout
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.ParentDataModifierNode
import top.fifthlight.combine.core.modifier.drawing.innerLine
import top.fifthlight.combine.core.modifier.pointer.consumePress
import top.fifthlight.combine.core.modifier.pointer.draggable
import top.fifthlight.combine.core.paint.Colors
import top.fifthlight.data.IntOffset
import top.fifthlight.data.IntSize
import top.fifthlight.data.Offset
import top.fifthlight.touchcontroller.common.config.layout.LayoutLayer
import top.fifthlight.touchcontroller.common.control.ControllerWidget
import top.fifthlight.touchcontroller.common.layout.align.Align
import top.fifthlight.touchcontroller.common.ui.control.ControllerWidget

private data class ControllerWidgetParentData(
    val align: Align,
    val offset: IntOffset,
    val size: IntSize,
)

private data class WidgetDataModifierNode(
    val align: Align,
    val offset: IntOffset,
    val size: IntSize,
) : ParentDataModifierNode, Modifier.Node<WidgetDataModifierNode> {
    constructor(widget: ControllerWidget, offset: IntOffset? = null) : this(
        widget.align,
        offset ?: widget.offset,
        widget.size()
    )

    override fun modifierParentData(parentData: Any?): ControllerWidgetParentData {
        return ControllerWidgetParentData(
            align = align,
            offset = offset,
            size = size
        )
    }
}

@Composable
fun LayoutEditorPanel(
    modifier: Modifier = Modifier,
    selectedWidgetIndex: Int = -1,
    onSelectedWidgetChanged: (Int) -> Unit = { _ -> },
    layer: LayoutLayer,
    layerIndex: Int,
    lockMoving: Boolean = false,
    highlight: Boolean = false,
    onWidgetChanged: (Int, ControllerWidget) -> Unit = { _, _ -> },
    onEmptyAreaClicked: () -> Unit = {},
) {
    val selectedWidget = layer.widgets.getOrNull(selectedWidgetIndex)
    var panelSize by remember { mutableStateOf(IntSize.ZERO) }
    Layout(
        modifier = Modifier
            .consumePress {
                onSelectedWidgetChanged(-1)
                onEmptyAreaClicked()
            }
            .then(modifier),
        measurePolicy = { measurables, constraints ->
            val childConstraint = constraints.copy(minWidth = 0, minHeight = 0)
            val placeables = measurables.map { it.measure(childConstraint) }

            val width = constraints.maxWidth
            val height = constraints.maxHeight
            panelSize = IntSize(width, height)
            layout(width, height) {
                placeables.forEachIndexed { index, placeable ->
                    val parentData = measurables[index].parentData as ControllerWidgetParentData
                    placeable.placeAt(
                        parentData.align.alignOffset(
                            windowSize = IntSize(width, height),
                            size = parentData.size,
                            offset = parentData.offset
                        )
                    )
                }
            }
        }
    ) {
        fun Int.safeCoerceIn(min: Int, max: Int): Int = if (min > max) {
            min
        } else {
            coerceIn(min, max)
        }

        fun clampOffset(align: Align, widgetSize: IntSize, widgetOffset: IntOffset) = IntOffset(
            x = when (align) {
                Align.LEFT_TOP, Align.LEFT_CENTER, Align.LEFT_BOTTOM ->
                    widgetOffset.x.safeCoerceIn(0, panelSize.width - widgetSize.width)

                Align.CENTER_CENTER, Align.CENTER_BOTTOM, Align.CENTER_TOP ->
                    widgetOffset.x.safeCoerceIn(
                        -panelSize.width / 2 + widgetSize.width / 2,
                        panelSize.width / 2 - widgetSize.width / 2
                    )

                Align.RIGHT_TOP, Align.RIGHT_CENTER, Align.RIGHT_BOTTOM ->
                    widgetOffset.x.safeCoerceIn(0, panelSize.width - widgetSize.width)
            },
            y = when (align) {
                Align.LEFT_TOP, Align.CENTER_TOP, Align.RIGHT_TOP ->
                    widgetOffset.y.safeCoerceIn(0, panelSize.height - widgetSize.height)

                Align.LEFT_CENTER, Align.CENTER_CENTER, Align.RIGHT_CENTER ->
                    widgetOffset.y.safeCoerceIn(
                        -panelSize.height / 2 + widgetSize.height / 2,
                        panelSize.height / 2 - widgetSize.height / 2
                    )

                Align.LEFT_BOTTOM, Align.CENTER_BOTTOM, Align.RIGHT_BOTTOM ->
                    widgetOffset.y.safeCoerceIn(0, panelSize.height - widgetSize.height)
            }
        )

        var dragTotalOffset by remember(selectedWidgetIndex, layerIndex) { mutableStateOf(Offset.ZERO) }
        LaunchedEffect(selectedWidget) {
            dragTotalOffset = Offset.ZERO
        }

        for ((index, widget) in layer.widgets.withIndex()) {
            val lockWidgetMoving = lockMoving || widget.lockMoving
            var modifier = if (index == selectedWidgetIndex) {
                if (lockWidgetMoving) {
                    Modifier.innerLine(Colors.RED)
                } else {
                    Modifier.innerLine(Colors.WHITE)
                }
            } else if (highlight) {
                Modifier.innerLine(Colors.YELLOW)
            } else {
                Modifier
            }

            val widgetOffset = if (index == selectedWidgetIndex) {
                val dragIntOffset = dragTotalOffset.toIntOffset()
                val normalizedOffset = widget.align.normalizeOffset(dragIntOffset)
                clampOffset(widget.align, widget.size(), normalizedOffset + widget.offset)
            } else {
                null
            }

            if (!lockWidgetMoving) {
                modifier = modifier.draggable(
                    onPress = { _, _ ->
                        if (index != selectedWidgetIndex) {
                            onSelectedWidgetChanged(index)
                        }
                        dragTotalOffset = Offset.ZERO
                    },
                    onDrag = { relative, _ ->
                        dragTotalOffset += relative
                    },
                    onRelease = { _, _ ->
                        val widgetOffset = widgetOffset ?: return@draggable
                        val widgetSize = widget.size()
                        val clampedOffset = clampOffset(widget.align, widgetSize, widgetOffset)
                        val newWidget = if (widget.autoAlign) {
                            val absolutePos = widget.align.alignOffset(panelSize, widgetSize, clampedOffset)
                            val newAlign = Align.fromPosition(
                                windowSize = panelSize,
                                size = widgetSize,
                                absolutePos = absolutePos,
                            )
                            val newOffset = newAlign.offsetAt(
                                windowSize = panelSize,
                                size = widgetSize,
                                absolutePos = absolutePos,
                            )
                            widget.cloneBase(align = newAlign, offset = newOffset)
                        } else widget.cloneBase(offset = clampedOffset)
                        dragTotalOffset = Offset.ZERO
                        onWidgetChanged(index, newWidget)
                    }
                )
            } else {
                modifier = modifier.draggable {
                    if (index != selectedWidgetIndex) {
                        onSelectedWidgetChanged(index)
                    }
                }
            }
            ControllerWidget(
                modifier = Modifier
                    .then(WidgetDataModifierNode(widget, widgetOffset))
                    .then(modifier),
                widget = widget
            )
        }
    }
}
