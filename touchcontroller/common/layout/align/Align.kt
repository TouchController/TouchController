/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.layout.align

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import top.fifthlight.combine.core.layout.Alignment
import top.fifthlight.data.IntOffset
import top.fifthlight.data.IntSize

@Serializable
enum class Align(val alignment: Alignment) {
    @SerialName("left_top")
    LEFT_TOP(Alignment.TopLeft),

    @SerialName("center_top")
    CENTER_TOP(Alignment.TopCenter),

    @SerialName("right_top")
    RIGHT_TOP(Alignment.TopRight),

    @SerialName("left_center")
    LEFT_CENTER(Alignment.CenterLeft),

    @SerialName("center_center")
    CENTER_CENTER(Alignment.Center),

    @SerialName("right_center")
    RIGHT_CENTER(Alignment.CenterRight),

    @SerialName("left_bottom")
    LEFT_BOTTOM(Alignment.BottomLeft),

    @SerialName("center_bottom")
    CENTER_BOTTOM(Alignment.BottomCenter),

    @SerialName("right_bottom")
    RIGHT_BOTTOM(Alignment.BottomRight);

    fun normalizeOffset(offset: IntOffset) = when (this) {
        LEFT_TOP, CENTER_TOP, LEFT_CENTER, CENTER_CENTER -> offset
        RIGHT_TOP, RIGHT_CENTER -> IntOffset(-offset.x, offset.y)
        LEFT_BOTTOM, CENTER_BOTTOM -> IntOffset(offset.x, -offset.y)
        RIGHT_BOTTOM -> -offset
    }

    fun alignOffset(windowSize: IntSize, size: IntSize, offset: IntOffset) = when (this) {
        LEFT_TOP -> offset

        CENTER_TOP -> IntOffset(
            x = (windowSize.width - size.width) / 2 + offset.x,
            y = offset.y
        )

        RIGHT_TOP -> IntOffset(
            x = windowSize.width - size.width - offset.x,
            y = offset.y,
        )

        LEFT_CENTER -> IntOffset(
            x = offset.x,
            y = (windowSize.height - size.height) / 2 + offset.y
        )

        CENTER_CENTER -> (windowSize - size) / 2 + offset

        RIGHT_CENTER -> IntOffset(
            x = windowSize.width - size.width - offset.x,
            y = (windowSize.height - size.height) / 2 + offset.y
        )

        LEFT_BOTTOM -> IntOffset(
            x = offset.x,
            y = windowSize.height - size.height - offset.y,
        )

        CENTER_BOTTOM -> IntOffset(
            x = (windowSize.width - size.width) / 2 + offset.x,
            y = windowSize.height - size.height - offset.y,
        )

        RIGHT_BOTTOM -> IntOffset(
            x = windowSize.width - size.width - offset.x,
            y = windowSize.height - size.height - offset.y,
        )
    }

    fun offsetAt(windowSize: IntSize, size: IntSize, absolutePos: IntOffset) = when (this) {
        LEFT_TOP -> absolutePos

        CENTER_TOP -> IntOffset(
            x = absolutePos.x - (windowSize.width - size.width) / 2,
            y = absolutePos.y,
        )

        RIGHT_TOP -> IntOffset(
            x = windowSize.width - size.width - absolutePos.x,
            y = absolutePos.y,
        )

        LEFT_CENTER -> IntOffset(
            x = absolutePos.x,
            y = absolutePos.y - (windowSize.height - size.height) / 2,
        )

        CENTER_CENTER -> absolutePos - (windowSize - size) / 2

        RIGHT_CENTER -> IntOffset(
            x = windowSize.width - size.width - absolutePos.x,
            y = absolutePos.y - (windowSize.height - size.height) / 2,
        )

        LEFT_BOTTOM -> IntOffset(
            x = absolutePos.x,
            y = windowSize.height - size.height - absolutePos.y,
        )

        CENTER_BOTTOM -> IntOffset(
            x = absolutePos.x - (windowSize.width - size.width) / 2,
            y = windowSize.height - size.height - absolutePos.y,
        )

        RIGHT_BOTTOM -> IntOffset(
            x = windowSize.width - size.width - absolutePos.x,
            y = windowSize.height - size.height - absolutePos.y,
        )
    }

    companion object {
        fun fromPosition(windowSize: IntSize, size: IntSize, absolutePos: IntOffset): Align {
            val center = absolutePos + size / 2
            val leftThird = windowSize.width / 3
            val rightThird = windowSize.width - leftThird
            val topThird = windowSize.height / 3
            val bottomThird = windowSize.height - topThird
            return when {
                center.y <= topThird && center.x <= leftThird -> LEFT_TOP
                center.y <= topThird && center.x >= rightThird -> RIGHT_TOP
                center.y <= topThird -> CENTER_TOP
                center.y >= bottomThird && center.x <= leftThird -> LEFT_BOTTOM
                center.y >= bottomThird && center.x >= rightThird -> RIGHT_BOTTOM
                center.y >= bottomThird -> CENTER_BOTTOM
                center.x <= leftThird -> LEFT_CENTER
                center.x >= rightThird -> RIGHT_CENTER
                else -> CENTER_CENTER
            }
        }
    }
}
