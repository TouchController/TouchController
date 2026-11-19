/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.config.tab

import androidx.compose.runtime.Composable
import top.fifthlight.combine.core.data.Identifier
import top.fifthlight.combine.core.data.Text
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.common.platform.provider.PlatformProvider
import top.fifthlight.touchcontroller.common.platform.sdl.SdlPlatform

sealed class TabConfig(
    private val titleId: Identifier,
    val group: TabGroup? = null,
    val index: Int,
    val openAsScreen: Boolean = false,
) {
    val title: Text
        @Composable
        get() = Text.translatable(titleId)

    data object About : TabConfig(
        titleId = Texts.SCREEN_CONFIG_ABOUT_TITLE,
        group = TabGroup.SystemGroup,
        index = 0,
    )

    data object Status : TabConfig(
        titleId = Texts.SCREEN_CONFIG_STATUS_TITLE,
        group = TabGroup.SystemGroup,
        index = 1,
    )

    data object Sdl : TabConfig(
        titleId = Texts.SCREEN_CONFIG_PLATFORM_TITLE,
        group = TabGroup.SystemGroup,
        index = 2,
    )

    data object Regular : TabConfig(
        titleId = Texts.SCREEN_CONFIG_GENERAL_REGULAR_TITLE,
        group = TabGroup.GeneralGroup,
        index = 0,
    )

    data object Control : TabConfig(
        titleId = Texts.SCREEN_CONFIG_GENERAL_CONTROL_TITLE,
        group = TabGroup.GeneralGroup,
        index = 1,
    )

    data object TouchRing : TabConfig(
        titleId = Texts.SCREEN_CONFIG_GENERAL_TOUCH_RING_TITLE,
        group = TabGroup.GeneralGroup,
        index = 2,
    )

    data object Debug : TabConfig(
        titleId = Texts.SCREEN_CONFIG_GENERAL_DEBUG_TITLE,
        group = TabGroup.GeneralGroup,
        index = 3,
    )

    data object ManageControlPresets : TabConfig(
        titleId = Texts.SCREEN_CONFIG_LAYOUT_MANAGE_CONTROL_PRESET,
        group = TabGroup.LayoutGroup,
        index = 0,
        openAsScreen = true,
    )

    data object CustomControlLayout : TabConfig(
        titleId = Texts.SCREEN_CONFIG_LAYOUT_CUSTOM_CONTROL_LAYOUT,
        group = TabGroup.LayoutGroup,
        index = 1,
        openAsScreen = true,
    )

    data object UsableItemsTab : TabConfig(
        titleId = Texts.SCREEN_CONFIG_ITEM_USABLE_ITEMS_TITLE,
        group = TabGroup.ItemGroup,
        index = 0,
    )

    data object ShowCrosshairItemsTab : TabConfig(
        titleId = Texts.SCREEN_CONFIG_ITEM_SHOW_CROSSHAIR_ITEMS_TITLE,
        group = TabGroup.ItemGroup,
        index = 1,
    )

    data object UsingAimingItemsTab : TabConfig(
        titleId = Texts.SCREEN_CONFIG_ITEM_USING_AIMING_ITEMS_TITLE,
        group = TabGroup.ItemGroup,
        index = 2,
    )

    companion object {
        val all: List<TabConfig>
            get() = listOfNotNull(
                About,
                Status,
                if (PlatformProvider.platform is SdlPlatform) Sdl else null,
                Regular,
                Control,
                TouchRing,
                Debug,
                ManageControlPresets,
                CustomControlLayout,
                UsableItemsTab,
                ShowCrosshairItemsTab,
                UsingAimingItemsTab,
            )
    }
}
