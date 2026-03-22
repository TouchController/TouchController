package top.fifthlight.touchcontroller.common.ui.chat.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import top.fifthlight.combine.paint.Color
import top.fifthlight.combine.paint.Colors
import top.fifthlight.touchcontroller.common.config.holder.GlobalConfigHolder
import top.fifthlight.touchcontroller.common.gal.chat.ChatMessageProvider
import top.fifthlight.touchcontroller.common.gal.chat.ChatMessageProviderFactory
import top.fifthlight.touchcontroller.common.ui.chat.state.ChatScreenState
import top.fifthlight.touchcontroller.common.ui.model.TouchControllerScreenModel

class ChatScreenModel : TouchControllerScreenModel() {
    private val chatMessageProvider: ChatMessageProvider = ChatMessageProviderFactory.of()
    private val _uiState: MutableStateFlow<ChatScreenState> = MutableStateFlow(ChatScreenState())
    val uiState = _uiState.asStateFlow()

    init {
        coroutineScope.launch {
            GlobalConfigHolder.config.collect { config ->
                _uiState.getAndUpdate {
                    it.copy(
                        lineSpacing = config.chat.lineSpacing,
                        textColor = config.chat.textColor,
                    )
                }
            }
        }
    }

    fun updateText(newText: String) {
        _uiState.getAndUpdate { it.copy(text = newText) }
    }

    fun sendText() {
        chatMessageProvider.sendMessage(uiState.value.text)
        updateText("")
    }

    fun openSettingsDialog() {
        _uiState.getAndUpdate { it.copy(settingsDialogOpened = true) }
    }

    fun closeSettingsDialog() {
        _uiState.getAndUpdate { it.copy(settingsDialogOpened = false) }
    }

    fun resetSettings() {
        _uiState.getAndUpdate {
            it.copy(
                lineSpacing = 0,
                textColor = Colors.WHITE,
            )
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
