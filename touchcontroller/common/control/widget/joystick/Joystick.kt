/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.control.widget.joystick

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.plus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import top.fifthlight.combine.core.data.Text
import top.fifthlight.combine.core.paint.Color
import top.fifthlight.data.*
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.assets.texture.set.BuiltInTextureItems
import top.fifthlight.touchcontroller.assets.texture.set.BuiltInTextureSets
import top.fifthlight.touchcontroller.common.assets.TextureSet
import top.fifthlight.touchcontroller.common.control.BooleanProperty
import top.fifthlight.touchcontroller.common.control.ControllerWidget
import top.fifthlight.touchcontroller.common.control.FloatProperty
import top.fifthlight.touchcontroller.common.control.TextureSetProperty
import top.fifthlight.touchcontroller.common.gal.key.DefaultKeyBindingType
import top.fifthlight.touchcontroller.common.layout.Context
import top.fifthlight.touchcontroller.common.layout.align.Align
import top.fifthlight.touchcontroller.common.state.PointerState
import top.fifthlight.touchcontroller.common.util.uuid.fastRandomUuid
import kotlin.math.round
import kotlin.math.sqrt
import kotlin.uuid.Uuid

@Serializable
@SerialName("joystick")
data class Joystick(
    val textureSet: TextureSet = BuiltInTextureSets.new,
    val size: Float = 1.5f,
    val stickSize: Float = 1.15f,
    val triggerSprint: Boolean = false,
    val increaseOpacityWhenActive: Boolean = true,
    override val id: Uuid = fastRandomUuid(),
    override val name: Name = Name.Translatable(Texts.WIDGET_JOYSTICK_NAME),
    override val align: Align = Align.LEFT_BOTTOM,
    override val offset: IntOffset = IntOffset.ZERO,
    override val opacity: Float = 1f,
    override val lockMoving: Boolean = false,
) : ControllerWidget() {
    companion object {
        @Suppress("UNCHECKED_CAST")
        private val _properties = properties + persistentListOf<Property<Joystick, *>>(
            FloatProperty(
                getValue = { it.size },
                setValue = { config, value -> config.copy(size = value) },
                range = .5f..4f,
                messageFormatter = {
                    Text.format(
                        Texts.WIDGET_JOYSTICK_PROPERTY_SIZE,
                        round(it * 100f).toString()
                    )
                },
            ),
            TextureSetProperty(
                getValue = { it.textureSet },
                setValue = { config, value -> config.copy(textureSet = value) },
                name = Text.translatable(Texts.WIDGET_JOYSTICK_PROPERTY_TEXTURE_SET),
            ),
            FloatProperty(
                getValue = { it.stickSize },
                setValue = { config, value -> config.copy(stickSize = value) },
                range = .5f..4f,
                messageFormatter = {
                    Text.format(
                        Texts.WIDGET_JOYSTICK_PROPERTY_STICK_SIZE,
                        round(it * 100f).toString()
                    )
                },
            ),
            BooleanProperty(
                getValue = { it.triggerSprint },
                setValue = { config, value -> config.copy(triggerSprint = value) },
                name = Text.translatable(Texts.WIDGET_JOYSTICK_PROPERTY_TRIGGER_SPRINT),
            ),
            BooleanProperty(
                getValue = { it.increaseOpacityWhenActive },
                setValue = { config, value -> config.copy(increaseOpacityWhenActive = value) },
                name = Text.translatable(Texts.WIDGET_JOYSTICK_PROPERTY_INCREASE_OPACITY_WHEN_ACTIVE),
            )
        ) as PersistentList<Property<ControllerWidget, *>>
    }

    override val properties
        get() = _properties

    override fun size(): IntSize = IntSize((size * 72).toInt())

    private val padTexture = BuiltInTextureItems.pad.get(textureSet)
    private val stickTexture = BuiltInTextureItems.stick.get(textureSet)

    fun stickSize() = IntSize((stickSize * 48).toInt())

    override fun cloneBase(
        id: Uuid,
        name: Name,
        align: Align,
        offset: IntOffset,
        opacity: Float,
        lockMoving: Boolean,
    ) = copy(
        id = id,
        name = name,
        align = align,
        offset = offset,
        opacity = opacity,
        lockMoving = lockMoving,
    )

    override fun layout(context: Context): Unit = with(context) {
        val layout = this@Joystick

        var currentPointer = pointers.values.firstOrNull {
            it.state is PointerState.Joystick
        }
        currentPointer?.let {
            for (pointer in pointers.values) {
                if (!pointer.inRect(size)) {
                    continue
                }
                when (pointer.state) {
                    PointerState.New -> pointer.state = PointerState.Invalid
                    else -> {}
                }
            }
        } ?: run {
            for (pointer in pointers.values) {
                when (pointer.state) {
                    PointerState.New -> {
                        if (!pointer.inRect(size)) {
                            continue
                        }
                        if (currentPointer != null) {
                            pointer.state = PointerState.Invalid
                        } else {
                            pointer.state = PointerState.Joystick
                            currentPointer = pointer
                        }
                    }

                    else -> {}
                }
            }
        }

        val rawOffset = currentPointer?.let { pointer ->
            pointer.scaledOffset / size.width.toFloat() * 2f - 1f
        }

        val normalizedOffset = rawOffset?.let { offset ->
            val squaredLength = offset.squaredLength
            if (squaredLength > 1) {
                val length = sqrt(squaredLength)
                offset / length
            } else {
                offset
            }
        }

        val opacityMultiplier = if (!layout.increaseOpacityWhenActive || currentPointer == null) {
            1f
        } else {
            1.5f
        }

        withOpacity(opacityMultiplier) {
            drawQueue.enqueue { canvas ->
                val color = Color(((0xFF * opacity).toInt() shl 24) or 0xFFFFFF)
                val size = size
                layout.padTexture.draw(
                    canvas = canvas,
                    dstRect = IntRect(size = size),
                    tint = color,
                )
                val drawOffset = normalizedOffset ?: Offset.ZERO
                val stickSize = layout.stickSize()
                val actualOffset = ((drawOffset + 1f) / 2f * size) - stickSize.toSize() / 2f
                layout.stickTexture.draw(
                    canvas = canvas,
                    dstRect = Rect(
                        offset = actualOffset,
                        size = stickSize.toSize()
                    ),
                    tint = color,
                )
            }
        }

        normalizedOffset?.let { (right, backward) ->
            val sprintButtonState = keyBindingHandler.getState(DefaultKeyBindingType.SPRINT)
            if (layout.triggerSprint && rawOffset.y < -1.1f) {
                sprintButtonState.clicked = true
            }
            result.left = -right
            result.forward = -backward
        }
    }
}
