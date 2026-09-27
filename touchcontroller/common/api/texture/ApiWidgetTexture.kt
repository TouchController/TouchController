/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.api.texture

import top.fifthlight.touchcontroller.api.v1.widget.WidgetTexture
import top.fifthlight.touchcontroller.common.assets.TextureItem

data class ApiWidgetTexture(
    val textureItem: TextureItem,
) : WidgetTexture

val WidgetTexture.textureItem
    get() = (this as ApiWidgetTexture).textureItem
