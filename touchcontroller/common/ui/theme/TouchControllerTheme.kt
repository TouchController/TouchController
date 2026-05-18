/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import top.fifthlight.combine.core.node.LocalInputHandler
import top.fifthlight.combine.core.paint.BackgroundTexture
import top.fifthlight.combine.core.paint.Drawable
import top.fifthlight.combine.theme.LocalTheme
import top.fifthlight.combine.theme.Theme
import top.fifthlight.combine.theme.blackstone.BlackstoneTextures
import top.fifthlight.combine.theme.blackstone.BlackstoneTheme
import top.fifthlight.combine.ui.style.ColorTheme
import top.fifthlight.combine.ui.style.DrawableSet
import top.fifthlight.touchcontroller.assets.texture.Textures
import top.fifthlight.touchcontroller.common.input.InputManager

val LocalTouchControllerTheme = staticCompositionLocalOf { TouchControllerTheme() }

data class TouchControllerTheme(
    val background: BackgroundTexture = Textures.background_brick_background,
    val borderBackgroundDark: Drawable = BlackstoneTextures.widget_background_background_dark,

    val appBarBackground: Drawable = BlackstoneTextures.widget_background_background_gray_title,

    val titleBoxBackground: Drawable = BlackstoneTextures.widget_background_background_lightgray_title,

    val tabButtonDrawablesUnchecked: DrawableSet = DrawableSet(
        normal = BlackstoneTextures.widget_tab_tab,
        focus = BlackstoneTextures.widget_tab_tab_hover,
        hover = BlackstoneTextures.widget_tab_tab_hover,
        active = BlackstoneTextures.widget_tab_tab_active,
        disabled = BlackstoneTextures.widget_tab_tab_disabled,
    ),
    val tabButtonColorsUnchecked: ColorTheme = ColorTheme.dark,
    val tabButtonDrawablesChecked: DrawableSet =DrawableSet(
        normal = BlackstoneTextures.widget_tab_tab_presslock,
        focus = BlackstoneTextures.widget_tab_tab_presslock_hover,
        hover = BlackstoneTextures.widget_tab_tab_presslock_hover,
        active = BlackstoneTextures.widget_tab_tab_active,
        disabled = BlackstoneTextures.widget_tab_tab_disabled,
    ),
    val tabButtonColorsChecked: ColorTheme = ColorTheme.light,

    val listButtonDrawablesUnchecked: DrawableSet = DrawableSet(
        normal = BlackstoneTextures.widget_list_list,
        focus = BlackstoneTextures.widget_list_list_hover,
        hover = BlackstoneTextures.widget_list_list_hover,
        active = BlackstoneTextures.widget_list_list_active,
        disabled = BlackstoneTextures.widget_list_list_disabled,
    ),
    val listButtonColorsUnchecked: ColorTheme = ColorTheme.dark,
    val listButtonDrawablesChecked: DrawableSet = DrawableSet(
        normal = BlackstoneTextures.widget_list_list_presslock,
        focus = BlackstoneTextures.widget_list_list_presslock_hover,
        hover = BlackstoneTextures.widget_list_list_presslock_hover,
        active = BlackstoneTextures.widget_list_list_active,
        disabled = BlackstoneTextures.widget_list_list_disabled,
    ),
    val listButtonColorsChecked: ColorTheme = ColorTheme.light,

    val checkButtonDrawablesUnchecked: DrawableSet = DrawableSet(
        normal = BlackstoneTextures.widget_button_button,
        hover = BlackstoneTextures.widget_button_button_hover,
        focus = BlackstoneTextures.widget_button_button_hover,
        active = BlackstoneTextures.widget_button_button_active,
    ),
    val checkButtonColorsUnchecked: ColorTheme = ColorTheme.light,
    val checkButtonDrawablesChecked: DrawableSet = DrawableSet(
        normal = BlackstoneTextures.widget_button_button_presslock,
        hover = BlackstoneTextures.widget_button_button_presslock_hover,
        focus = BlackstoneTextures.widget_button_button_presslock_hover,
        active = BlackstoneTextures.widget_button_button_presslock_active,
    ),
    val checkButtonColorsChecked: ColorTheme = ColorTheme.light,

    val base: Theme = BlackstoneTheme,
) {

    companion object {
        val default = TouchControllerTheme()

        @Composable
        inline operator fun invoke(crossinline block: @Composable TouchControllerTheme.() -> Unit) {
            default(block)
        }
    }
}

@Composable
inline operator fun TouchControllerTheme.invoke(crossinline block: @Composable TouchControllerTheme.() -> Unit) {
    CompositionLocalProvider(
        LocalTouchControllerTheme provides TouchControllerTheme(),
        LocalInputHandler provides InputManager,
        LocalTheme provides base,
    ) {
        block()
    }
}
