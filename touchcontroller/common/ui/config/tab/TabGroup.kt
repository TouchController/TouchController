/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.config.tab

import androidx.compose.runtime.Composable
import top.fifthlight.combine.core.data.Identifier
import top.fifthlight.combine.core.data.Text
import top.fifthlight.touchcontroller.assets.lang.Texts

sealed class TabGroup(
    val titleId: Identifier
) {
    val title: Text
        @Composable
        get() = Text.translatable(titleId)

    data object SystemGroup : TabGroup(Texts.SCREEN_CONFIG_SYSTEM_TITLE)
    data object LayoutGroup : TabGroup(Texts.SCREEN_CONFIG_LAYOUT_TITLE)
    data object GeneralGroup : TabGroup(Texts.SCREEN_CONFIG_GENERAL_TITLE)
    data object ItemGroup : TabGroup(Texts.SCREEN_CONFIG_ITEM_TITLE)
}
