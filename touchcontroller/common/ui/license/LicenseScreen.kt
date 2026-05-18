/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.screen

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import top.fifthlight.combine.core.data.Text
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.drawing.background
import top.fifthlight.combine.core.modifier.placement.fillMaxWidth
import top.fifthlight.combine.core.modifier.placement.padding
import top.fifthlight.combine.core.modifier.scroll.verticalScroll
import top.fifthlight.combine.widget.ui.Text
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.common.about.License
import top.fifthlight.touchcontroller.common.ui.theme.LocalTouchControllerTheme
import top.fifthlight.touchcontroller.common.ui.widget.Scaffold
import top.fifthlight.touchcontroller.common.ui.widget.navigation.AppBar
import top.fifthlight.touchcontroller.common.ui.widget.navigation.BackButton

class LicenseScreen(
    val license: License,
) : Screen {
    @Composable
    override fun Content() {
        Scaffold(
            topBar = {
                AppBar(
                    modifier = Modifier.fillMaxWidth(),
                    leading = {
                        BackButton(
                            screenName = Text.translatable(Texts.SCREEN_LICENSE_TITLE)
                        )
                    },
                    title = {
                        Text(license.name)
                    },
                )
            },
        ) { modifier ->
            license.content?.let { content ->
                Text(
                    text = content,
                    modifier = Modifier
                        .padding(4)
                        .verticalScroll()
                        .background(LocalTouchControllerTheme.current.background)
                        .then(modifier)
                )
            }
        }
    }
}
