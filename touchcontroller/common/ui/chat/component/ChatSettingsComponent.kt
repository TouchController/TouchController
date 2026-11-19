/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.chat.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import top.fifthlight.combine.core.paint.Color
import top.fifthlight.combine.core.util.dispatcher.Game
import top.fifthlight.touchcontroller.common.config.data.ChatConfig
import top.fifthlight.touchcontroller.common.config.holder.GlobalConfigHolder
import top.fifthlight.touchcontroller.common.ui.chat.state.ChatSettingsDialogState

class ChatSettingsComponent(
    componentContext: ComponentContext,
) : ComponentContext by componentContext {
    private val _uiState = MutableValue(GlobalConfigHolder.config.value.let { config ->
        ChatSettingsDialogState(
            lineSpacing = config.chat.lineSpacing,
            textColor = config.chat.textColor,
        )
    })
    val uiState: Value<ChatSettingsDialogState> = _uiState

    init {
        coroutineScope(Dispatchers.Game).launch {
            GlobalConfigHolder.config.collect { config ->
                _uiState.update {
                    it.copy(
                        lineSpacing = config.chat.lineSpacing,
                        textColor = config.chat.textColor,
                    )
                }
            }
        }
    }

    fun resetSettings() {
        GlobalConfigHolder.updateConfig {
            copy(chat = ChatConfig())
        }
    }

    fun updateLineSpacing(lineSpacing: Int) {
        GlobalConfigHolder.updateConfig {
            copy(chat = chat.copy(lineSpacing = lineSpacing))
        }
    }

    fun updateTextColor(textColor: Color) {
        GlobalConfigHolder.updateConfig {
            copy(chat = chat.copy(textColor = textColor))
        }
    }
}
