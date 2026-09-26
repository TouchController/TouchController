/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.assets

import top.fifthlight.combine.core.data.Text

data class TextureSet(
    val name: Text,
    val title: Text,
    val grayWhenActive: Boolean,
    val classic: Boolean,
)
