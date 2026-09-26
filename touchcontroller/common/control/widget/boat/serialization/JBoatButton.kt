/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.control.widget.boat.serialization

import com.ubertob.kondor.json.*
import com.ubertob.kondor.json.jsonnode.FieldsValues
import com.ubertob.kondor.json.jsonnode.NodePath
import top.fifthlight.data.IntOffset
import top.fifthlight.touchcontroller.assets.texture.set.BuiltInTextureSets
import top.fifthlight.touchcontroller.common.assets.serialization.JTextureSet
import top.fifthlight.touchcontroller.common.control.name.serialization.JControllerWidgetName
import top.fifthlight.touchcontroller.common.control.widget.boat.BoatButton
import top.fifthlight.touchcontroller.common.control.widget.boat.BoatButtonSide
import top.fifthlight.touchcontroller.common.layout.align.Align
import top.fifthlight.touchcontroller.common.layout.align.serialization.JAlign
import top.fifthlight.touchcontroller.common.serialization.JIntOffset
import top.fifthlight.touchcontroller.common.serialization.JUuid
import top.fifthlight.touchcontroller.common.serialization.enumByName

val JBoatButtonSide = enumByName<BoatButtonSide> {
    "left" encodes BoatButtonSide.LEFT
    "right" encodes BoatButtonSide.RIGHT
}

object JBoatButton : JObj<BoatButton>() {
    val textureSet by JFieldMaybe(BoatButton::textureSet, JTextureSet)
    val size by JFieldMaybe(BoatButton::size, JFloat)
    val side by JFieldMaybe(BoatButton::side, JBoatButtonSide)
    val id by str(JUuid, BoatButton::id)
    val name by JFieldMaybe(BoatButton::name, JControllerWidgetName)
    val align by JFieldMaybe(BoatButton::align, JAlign)
    val autoAlign by JFieldMaybe(BoatButton::autoAlign, JBoolean)
    val offset by JFieldMaybe(BoatButton::offset, JIntOffset)
    val opacity by JFieldMaybe(BoatButton::opacity, JFloat)
    val lockMoving by JFieldMaybe(BoatButton::lockMoving, JBoolean)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = BoatButton(
        textureSet = (+textureSet) ?: BuiltInTextureSets.classic,
        size = (+size) ?: 3f,
        side = (+side) ?: BoatButtonSide.LEFT,
        id = +id,
        name = (+name) ?: BoatButton().name,
        align = (+align) ?: Align.CENTER_CENTER,
        autoAlign = (+autoAlign) ?: true,
        offset = (+offset) ?: IntOffset.ZERO,
        opacity = (+opacity) ?: 1f,
        lockMoving = (+lockMoving) ?: false,
    )
}
