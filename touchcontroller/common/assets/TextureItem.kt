/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.assets

import top.fifthlight.combine.core.paint.Texture

data class TextureItem(
    val name: String,
    val hidden: Boolean = false,
    val get: (TextureSet) -> Texture,
)
