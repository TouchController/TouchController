/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.api.texture

import top.fifthlight.touchcontroller.api.v1.widget.BuiltInWidgetTextureProvider
import top.fifthlight.touchcontroller.assets.texture.set.BuiltInTextureItems

object ApiBuiltInWidgetTextureProvider: BuiltInWidgetTextureProvider {
    override fun getTexture(id: BuiltInWidgetTextureProvider.Texture) = when (id) {
        BuiltInWidgetTextureProvider.Texture.EMOTE -> BuiltInTextureItems.emote
    }.let(::ApiWidgetTexture)
}
