/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.layout.widget.button

import top.fifthlight.data.Offset
import top.fifthlight.touchcontroller.common.layout.Context
import top.fifthlight.touchcontroller.common.state.PointerState
import top.fifthlight.touchcontroller.common.util.offset.fixAspectRadio
import kotlin.uuid.Uuid

fun Context.SwipeButton(
    id: Uuid,
    content: Context.(clicked: Boolean) -> Unit,
) = Button(
    id = id,
    swipe = true,
    content = content,
)

fun Context.Button(
    id: Uuid,
    grabTrigger: Boolean = false,
    swipe: Boolean = false,
    moveView: Boolean = false,
    content: Context.(clicked: Boolean) -> Unit,
): ButtonResult {
    var newPointer = false
    var clicked = false
    var release = false
    for (pointer in pointers.values) {
        when (val state = pointer.state) {
            PointerState.New -> {
                if (pointer.inRect(size)) {
                    pointer.state = PointerState.Button(
                        id = id,
                        swipe = swipe,
                        initialPosition = pointer.position,
                        lastPosition = pointer.position,
                        moving = false,
                    )
                    newPointer = true
                    clicked = true
                }
            }

            is PointerState.Button -> {
                if (moveView && state.id == id) {
                    var moving = state.moving
                    if (!moving) {
                        // Move detect
                        val delta = (pointer.position - state.initialPosition).fixAspectRadio(windowSize).squaredLength
                        val threshold = config.controlConfig.viewHoldDetectThreshold * 0.01f
                        if (delta > threshold * threshold) {
                            moving = true
                        }
                    } else {
                        val movement = (pointer.position - state.lastPosition).fixAspectRadio(windowSize)
                        result.lookDirection =
                            (result.lookDirection ?: Offset.ZERO) + movement * config.controlConfig.viewMovementSensitivity
                    }
                    pointer.state = state.copy(
                        moving = moving,
                        lastPosition = pointer.position,
                    )
                }
                if (swipe) {
                    if (pointer.inRect(size)) {
                        clicked = true
                    }
                } else if (state.id == id) {
                    if (grabTrigger || pointer.inRect(size)) {
                        clicked = true
                    }
                }
            }

            is PointerState.Released -> {
                val previousState = state.previousState
                if (previousState is PointerState.Button && previousState.id == id) {
                    release = true
                }
            }

            else -> {}
        }
    }

    content(clicked)
    return ButtonResult(
        newPointer = newPointer,
        clicked = clicked,
        release = release
    )
}
