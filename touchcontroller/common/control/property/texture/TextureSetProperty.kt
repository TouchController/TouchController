/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.control.property.texture

import kotlinx.collections.immutable.toPersistentList
import top.fifthlight.combine.core.data.Text
import top.fifthlight.touchcontroller.common.assets.TextureSet
import top.fifthlight.touchcontroller.common.assets.TextureSets
import top.fifthlight.touchcontroller.common.control.ControllerWidget
import top.fifthlight.touchcontroller.common.control.property.base.EnumProperty

fun <Config : ControllerWidget> TextureSetProperty(
    getValue: (Config) -> TextureSet,
    setValue: (Config, TextureSet) -> Config,
    name: Text,
) = EnumProperty(
    getValue = getValue,
    setValue = setValue,
    items = TextureSets.registry.map {
        Pair(it, it.name)
    }.toPersistentList(),
    name = name,
)

