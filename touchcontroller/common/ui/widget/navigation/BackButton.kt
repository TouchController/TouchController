/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.widget.navigation

import androidx.compose.runtime.Composable
import top.fifthlight.combine.core.data.Text
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.widget.Text
import top.fifthlight.combine.widget.TextButton
import top.fifthlight.touchcontroller.assets.lang.Texts

@Composable
fun BackButton(
    modifier: Modifier = Modifier,
    screenName: Text,
    onClick: () -> Unit,
) {
    TextButton(
        modifier = modifier,
        onClick = onClick,
    ) {
        Text(Text.format(Texts.BACK, screenName.string))
    }
}
