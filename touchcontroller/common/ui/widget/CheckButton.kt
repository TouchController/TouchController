/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.widget

import androidx.compose.runtime.Composable
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.widget.layout.BoxScope
import top.fifthlight.combine.ui.style.ColorThemeSet
import top.fifthlight.combine.ui.style.DrawableSet
import top.fifthlight.combine.widget.ui.Button
import top.fifthlight.data.IntPadding
import top.fifthlight.data.IntSize
import top.fifthlight.touchcontroller.common.ui.theme.LocalTouchControllerTheme

data class CheckButtonDrawables(
    val unchecked: DrawableSet,
    val checked: DrawableSet,
)

data class CheckButtonColors(
    val unchecked: ColorThemeSet,
    val checked: ColorThemeSet,
)

@Composable
fun TabButton(
    modifier: Modifier = Modifier,
    drawableSet: CheckButtonDrawables = LocalTouchControllerTheme.current.let {
        CheckButtonDrawables(
            unchecked = it.tabButtonDrawablesUnchecked,
            checked = it.tabButtonDrawablesChecked,
        )
    },
    colors: CheckButtonColors = LocalTouchControllerTheme.current.let {
        CheckButtonColors(
            unchecked = it.tabButtonColorsUnchecked,
            checked = it.tabButtonColorsChecked,
        )
    },
    checked: Boolean = false,
    minSize: IntSize = IntSize(48, 20),
    padding: IntPadding = IntPadding(left = 4, right = 4, top = 1),
    enabled: Boolean = true,
    onClick: () -> Unit,
    clickSound: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) = CheckButton(
    modifier = modifier,
    drawableSet = drawableSet,
    colors = colors,
    checked = checked,
    padding = padding,
    enabled = enabled,
    minSize = minSize,
    onClick = onClick,
    clickSound = clickSound,
    content = content,
)

@Composable
fun ListButton(
    modifier: Modifier = Modifier,
    drawableSet: CheckButtonDrawables = LocalTouchControllerTheme.current.let {
        CheckButtonDrawables(
            unchecked = it.listButtonDrawablesUnchecked,
            checked = it.listButtonDrawablesChecked,
        )
    },
    colors: CheckButtonColors = LocalTouchControllerTheme.current.let {
        CheckButtonColors(
            unchecked = it.listButtonColorsUnchecked,
            checked = it.listButtonColorsChecked,
        )
    },
    checked: Boolean = false,
    minSize: IntSize = IntSize(48, 20),
    padding: IntPadding = IntPadding(left = 4, right = 4, top = 1),
    enabled: Boolean = true,
    onClick: () -> Unit,
    clickSound: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) = CheckButton(
    modifier = modifier,
    drawableSet = drawableSet,
    colors = colors,
    checked = checked,
    minSize = minSize,
    padding = padding,
    enabled = enabled,
    onClick = onClick,
    clickSound = clickSound,
    content = content,
)

@Composable
fun CheckButton(
    modifier: Modifier = Modifier,
    drawableSet: CheckButtonDrawables = LocalTouchControllerTheme.current.let {
        CheckButtonDrawables(
            unchecked = it.checkButtonDrawablesUnchecked,
            checked = it.checkButtonDrawablesChecked,
        )
    },
    colors: CheckButtonColors = LocalTouchControllerTheme.current.let {
        CheckButtonColors(
            unchecked = it.checkButtonColorsUnchecked,
            checked = it.checkButtonColorsChecked,
        )
    },
    checked: Boolean = false,
    minSize: IntSize = IntSize(48, 20),
    padding: IntPadding = IntPadding(left = 4, right = 4, top = 1),
    enabled: Boolean = true,
    onClick: () -> Unit,
    clickSound: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) = Button(
    modifier = modifier,
    drawableSet = if (checked) {
        drawableSet.checked
    } else {
        drawableSet.unchecked
    },
    colorThemeSet = if (checked) {
        colors.checked
    } else {
        colors.unchecked
    },
    minSize = minSize,
    padding = padding,
    enabled = enabled,
    onClick = onClick,
    clickSound = clickSound,
    content = content,
)
