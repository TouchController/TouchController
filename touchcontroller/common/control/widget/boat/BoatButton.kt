/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.control.widget.boat

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.plus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import top.fifthlight.combine.core.data.Text
import top.fifthlight.combine.core.paint.Color
import top.fifthlight.data.IntOffset
import top.fifthlight.data.IntSize
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.assets.texture.set.BuiltInTextureItems
import top.fifthlight.touchcontroller.assets.texture.set.BuiltInTextureSets
import top.fifthlight.touchcontroller.common.assets.TextureSet
import top.fifthlight.touchcontroller.common.control.ControllerWidget
import top.fifthlight.touchcontroller.common.control.EnumProperty
import top.fifthlight.touchcontroller.common.control.FloatProperty
import top.fifthlight.touchcontroller.common.control.TextureSetProperty
import top.fifthlight.touchcontroller.common.layout.Context
import top.fifthlight.touchcontroller.common.layout.align.Align
import top.fifthlight.touchcontroller.common.layout.widget.Texture
import top.fifthlight.touchcontroller.common.layout.widget.button.Button
import top.fifthlight.touchcontroller.common.util.uuid.fastRandomUuid
import kotlin.math.round
import kotlin.uuid.Uuid

fun Context.BoatButton(
    id: Uuid,
    grayOnActive: Boolean,
    textureSet: TextureSet,
    side: BoatButtonSide,
) {
    val (_, clicked) = Button(id) { clicked ->
        if (grayOnActive) {
            if (clicked) {
                Texture(BuiltInTextureItems.up.get(textureSet), tint = Color(0xFFAAAAAAu))
            } else {
                Texture(BuiltInTextureItems.up.get(textureSet))
            }
        } else {
            if (clicked) {
                Texture(BuiltInTextureItems.upActive.get(textureSet))
            } else {
                Texture(BuiltInTextureItems.up.get(textureSet))
            }
        }
    }
    if (clicked) {
        when (side) {
            BoatButtonSide.LEFT -> result.boatLeft = true
            BoatButtonSide.RIGHT -> result.boatRight = true
        }
    }
}

@Serializable
@SerialName("boat_button")
data class BoatButton(
    val textureSet: TextureSet = BuiltInTextureSets.classic,
    val size: Float = 3f,
    val side: BoatButtonSide = BoatButtonSide.LEFT,
    override val id: Uuid = fastRandomUuid(),
    override val name: Name = Name.Translatable(Texts.WIDGET_BOAT_BUTTON_NAME),
    override val align: Align = Align.LEFT_BOTTOM,
    override val offset: IntOffset = IntOffset.ZERO,
    override val opacity: Float = 1f,
    override val lockMoving: Boolean = false,
) : ControllerWidget() {
    companion object {
        @Suppress("UNCHECKED_CAST")
        private val _properties = properties + persistentListOf<Property<BoatButton, *>>(
            FloatProperty(
                getValue = { it.size },
                setValue = { config, value -> config.copy(size = value) },
                range = .5f..4f,
                messageFormatter = {
                    Text.format(
                        Texts.WIDGET_BOAT_BUTTON_PROPERTY_SIZE,
                        round(it * 100f).toString()
                    )
                },
            ),
            TextureSetProperty(
                getValue = { it.textureSet },
                setValue = { config, value -> config.copy(textureSet = value) },
                name = Text.translatable(Texts.WIDGET_BOAT_BUTTON_PROPERTY_TEXTURE_SET),
            ),
            EnumProperty(
                getValue = { it.side },
                setValue = { config, value -> config.copy(side = value) },
                name = Text.translatable(Texts.WIDGET_BOAT_BUTTON_PROPERTY_SIDE),
                items = persistentListOf(
                    BoatButtonSide.LEFT to Text.translatable(Texts.WIDGET_BOAT_BUTTON_PROPERTY_SIDE_LEFT),
                    BoatButtonSide.RIGHT to Text.translatable(Texts.WIDGET_BOAT_BUTTON_PROPERTY_SIDE_RIGHT),
                )
            ),
        ) as PersistentList<Property<ControllerWidget, *>>
    }

    override val properties
        get() = _properties

    private val textureSize = BuiltInTextureItems.up.get(textureSet).size

    override fun size(): IntSize = (textureSize.toSize() * size).toIntSize()

    override fun layout(context: Context) {
        context.BoatButton(
            id = id,
            grayOnActive = textureSet.grayWhenActive,
            textureSet = textureSet,
            side = side,
        )
    }

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
}
