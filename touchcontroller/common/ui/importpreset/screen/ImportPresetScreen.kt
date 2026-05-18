/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.importpreset.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import top.fifthlight.combine.core.data.Text
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.placement.fillMaxWidth
import top.fifthlight.combine.widget.ui.Button
import top.fifthlight.combine.widget.ui.Text
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.common.config.preset.builtin.key.BuiltinPresetKey
import top.fifthlight.touchcontroller.common.ui.component.BuiltInPresetKeySelector
import top.fifthlight.touchcontroller.common.ui.importpreset.model.ImportPresetScreenModel
import top.fifthlight.touchcontroller.common.ui.widget.Scaffold
import top.fifthlight.touchcontroller.common.ui.widget.navigation.AppBar
import top.fifthlight.touchcontroller.common.ui.widget.navigation.BackButton

class ImportPresetScreen(private val onPresetKeySelected: (BuiltinPresetKey) -> Unit) : Screen {
    @Composable
    override fun Content() {
        val screenModel = rememberScreenModel { ImportPresetScreenModel(onPresetKeySelected) }
        val navigator = LocalNavigator.current
        Scaffold(
            topBar = {
                AppBar(
                    modifier = Modifier.fillMaxWidth(),
                    leading = {
                        BackButton(
                            screenName = Text.translatable(Texts.SCREEN_IMPORT_BUILTIN_PRESET),
                        )
                    },
                    trailing = {
                        Button(onClick = {
                            navigator?.pop()
                            screenModel.finish()
                        }) {
                            Text(Text.translatable(Texts.SCREEN_IMPORT_BUILTIN_PRESET_FINISH))
                        }
                    }
                )
            },
        ) { modifier ->
            val key by screenModel.key.collectAsState()
            BuiltInPresetKeySelector(
                modifier = modifier,
                value = key,
                onValueChanged = screenModel::updateKey,
            )
        }
    }
}
