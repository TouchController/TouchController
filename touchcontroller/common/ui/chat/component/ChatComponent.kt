/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.chat.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.*
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import top.fifthlight.combine.core.util.dispatcher.Game
import top.fifthlight.touchcontroller.common.config.holder.GlobalConfigHolder
import top.fifthlight.touchcontroller.common.gal.chat.ChatMessageProvider
import top.fifthlight.touchcontroller.common.gal.chat.ChatMessageProviderFactory
import top.fifthlight.touchcontroller.common.ui.chat.state.ChatScreenState

class ChatComponent(
    componentContext: ComponentContext,
    private val onBack: () -> Unit,
) : ComponentContext by componentContext {
    sealed interface ChildConfig {
        data object Settings : ChildConfig
    }

    sealed interface ChildModel {
        data class Settings(val component: ChatSettingsComponent) : ChildModel
    }

    private val chatMessageProvider: ChatMessageProvider = ChatMessageProviderFactory.of()

    private val dialogNavigation = SlotNavigation<ChildConfig>()

    val dialog: Value<ChildSlot<ChildConfig, ChildModel>> = childSlot(
        source = dialogNavigation,
        serializer = null,
        handleBackButton = true,
        childFactory = { config, context ->
            when (config) {
                ChildConfig.Settings -> ChildModel.Settings(
                    ChatSettingsComponent(
                        context,
                    )
                )
            }
        },
    )

    private val _uiState = MutableValue(ChatScreenState())
    val uiState: Value<ChatScreenState> = _uiState

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

    fun updateText(newText: String) {
        _uiState.update { it.copy(text = newText) }
    }

    fun sendText() {
        chatMessageProvider.sendMessage(uiState.value.text)
        updateText("")
    }

    fun openSettingsDialog() {
        dialogNavigation.activate(ChildConfig.Settings)
    }

    fun closeSettingsDialog() {
        dialogNavigation.dismiss()
    }

    fun onBack() {
        onBack.invoke()
    }
}
