/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.license

import androidx.compose.runtime.Composable
import top.fifthlight.combine.core.data.Text
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.drawing.background
import top.fifthlight.combine.core.modifier.placement.fillMaxWidth
import top.fifthlight.combine.core.modifier.placement.padding
import top.fifthlight.combine.core.modifier.scroll.verticalScroll
import top.fifthlight.combine.widget.Text
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.common.about.License
import top.fifthlight.touchcontroller.common.ui.theme.LocalTouchControllerTheme
import top.fifthlight.touchcontroller.common.ui.widget.Scaffold
import top.fifthlight.touchcontroller.common.ui.widget.navigation.AppBar
import top.fifthlight.touchcontroller.common.ui.widget.navigation.BackButton

@Composable
fun LicenseScreen(
    license: License,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            AppBar(
                modifier = Modifier.fillMaxWidth(),
                leading = {
                    BackButton(
                        screenName = Text.translatable(Texts.SCREEN_LICENSE_TITLE),
                        onClick = onBack,
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
