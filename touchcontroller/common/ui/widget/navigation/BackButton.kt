/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.widget.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.LocalNavigator
import top.fifthlight.combine.core.data.Text
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.screen.LocalCloseHandler
import top.fifthlight.combine.widget.ui.Text
import top.fifthlight.combine.widget.ui.TextButton
import top.fifthlight.touchcontroller.assets.lang.Texts

@Composable
fun BackButton(
    modifier: Modifier = Modifier,
    screenName: Text,
    onClick: (() -> Unit)? = null,
) {
    val closeHandler = LocalCloseHandler.current
    val navigator = LocalNavigator.current
    TextButton(
        modifier = modifier,
        onClick = {
            if (onClick != null) {
                onClick()
            } else {
                if (navigator?.pop() != true) {
                    closeHandler.close()
                }
            }
        }
    ) {
        Text(Text.format(Texts.BACK, screenName.string))
    }
}
