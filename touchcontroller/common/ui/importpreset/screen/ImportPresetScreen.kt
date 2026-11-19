/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.importpreset.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import top.fifthlight.combine.core.data.Text
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.placement.fillMaxWidth
import top.fifthlight.combine.widget.Button
import top.fifthlight.combine.widget.Text
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.common.ui.importpreset.component.ImportPresetComponent
import top.fifthlight.touchcontroller.common.ui.preset.BuiltInPresetKeySelector
import top.fifthlight.touchcontroller.common.ui.state.subscribeAsState
import top.fifthlight.touchcontroller.common.ui.widget.Scaffold
import top.fifthlight.touchcontroller.common.ui.widget.navigation.AppBar
import top.fifthlight.touchcontroller.common.ui.widget.navigation.BackButton

@Composable
fun ImportPresetScreen(component: ImportPresetComponent) {
    Scaffold(
        topBar = {
            AppBar(
                modifier = Modifier.fillMaxWidth(),
                leading = {
                    BackButton(
                        screenName = Text.translatable(Texts.SCREEN_IMPORT_BUILTIN_PRESET),
                        onClick = component::onBack,
                    )
                },
                trailing = {
                    Button(onClick = component::finish) {
                        Text(Text.translatable(Texts.SCREEN_IMPORT_BUILTIN_PRESET_FINISH))
                    }
                }
            )
        },
    ) { modifier ->
        val key by component.key.subscribeAsState()
        BuiltInPresetKeySelector(
            modifier = modifier,
            value = key,
            onValueChanged = component::updateKey,
            onConfigureTopBar = component::configTopBar,
        )
    }
}
