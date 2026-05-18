/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.chat.screen

import androidx.compose.runtime.*
import top.fifthlight.combine.core.data.Text
import top.fifthlight.combine.core.input.interaction.MutableInteractionSource
import top.fifthlight.combine.core.layout.Alignment
import top.fifthlight.combine.core.layout.Arrangement
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.drawing.background
import top.fifthlight.combine.core.modifier.focus.FocusInteraction
import top.fifthlight.combine.core.modifier.focus.FocusRequester
import top.fifthlight.combine.core.modifier.focus.focusRequester
import top.fifthlight.combine.core.modifier.placement.fillMaxHeight
import top.fifthlight.combine.core.modifier.placement.fillMaxWidth
import top.fifthlight.combine.core.modifier.placement.height
import top.fifthlight.combine.core.modifier.placement.width
import top.fifthlight.combine.core.modifier.scroll.verticalScroll
import top.fifthlight.combine.core.paint.Colors
import top.fifthlight.combine.core.screen.ScreenFactory
import top.fifthlight.combine.core.screen.ScreenFactoryFactory
import top.fifthlight.combine.core.text.TextHitTestResult
import top.fifthlight.combine.core.widget.layout.Column
import top.fifthlight.combine.core.widget.layout.Row
import top.fifthlight.combine.widget.ui.*
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.assets.texture.Textures
import top.fifthlight.touchcontroller.common.control.action.provider.ChatScreenProvider
import top.fifthlight.touchcontroller.common.gal.chat.ChatMessageProvider
import top.fifthlight.touchcontroller.common.gal.chat.ChatMessageProviderFactory
import top.fifthlight.touchcontroller.common.ui.chat.model.ChatScreenModel
import top.fifthlight.touchcontroller.common.ui.theme.TouchControllerTheme
import top.fifthlight.touchcontroller.common.ui.widget.ColorPreferenceItem
import top.fifthlight.touchcontroller.common.ui.widget.IntSliderPreferenceItem
import top.fifthlight.touchcontroller.common.ui.widget.Scaffold
import top.fifthlight.touchcontroller.common.ui.widget.navigation.AppBar
import top.fifthlight.touchcontroller.common.ui.widget.navigation.BackButton

@Composable
private fun ChatScreen() {
    val screenModel = remember { ChatScreenModel() }
    DisposableEffect(screenModel) {
        onDispose {
            screenModel.onDispose()
        }
    }

    TouchControllerTheme {
        val uiState by screenModel.uiState.collectAsState()
        AlertDialog(
            visible = uiState.settingsDialogOpened,
            modifier = Modifier.fillMaxWidth(.6f),
            onDismissRequest = {
                screenModel.closeSettingsDialog()
            },
            action = {
                WarningButton(onClick = {
                    screenModel.resetSettings()
                }) {
                    Text(Text.translatable(Texts.SCREEN_CHAT_SETTINGS_RESET))
                }
                GuideButton(onClick = {
                    screenModel.closeSettingsDialog()
                }) {
                    Text(Text.translatable(Texts.SCREEN_CHAT_SETTINGS_OK))
                }
            },
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4)
            ) {
                IntSliderPreferenceItem(
                    modifier = Modifier.fillMaxWidth(),
                    title = Text.translatable(Texts.SCREEN_CHAT_SETTINGS_LINE_SPACING),
                    range = 0..16,
                    value = uiState.lineSpacing,
                    onValueChanged = {
                        screenModel.updateLineSpacing(it)
                    }
                )

                ColorPreferenceItem(
                    modifier = Modifier.fillMaxWidth(),
                    title = Text.translatable(Texts.SCREEN_CHAT_SETTINGS_TEXT_COLOR),
                    value = uiState.textColor,
                    onValueChanged = {
                        screenModel.updateTextColor(it)
                    }
                )
            }
        }

        Scaffold(
            topBar = {
                AppBar(
                    modifier = Modifier.fillMaxWidth(),
                    leading = {
                        BackButton(
                            screenName = Text.translatable(Texts.SCREEN_CHAT_EXIT),
                        )
                    },
                    title = {
                        Text(Text.translatable(Texts.SCREEN_CHAT_TITLE))
                    },
                )
            },
        ) { modifier ->
            Column(
                modifier = modifier,
            ) {
                val messageProvider: ChatMessageProvider = ChatMessageProviderFactory.of()
                var messages by remember { mutableStateOf(messageProvider.getMessages()) }
                LaunchedEffect(Unit) {
                    while (true) {
                        withFrameMillis { _ ->
                            messages = messageProvider.getMessages()
                        }
                    }
                }
                CompositionLocalProvider(
                    LocalTextHitHandler provides {
                        when (it) {
                            is TextHitTestResult.InsertText -> if (it.overwrite) {
                                screenModel.updateText(it.text)
                            } else {
                                // TODO: append after cursor. there is no good way to do it currently
                                screenModel.updateText(uiState.text + it.text)
                            }

                            is TextHitTestResult.Native -> it.action()
                        }
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .verticalScroll(true)
                            .background(Colors.TRANSPARENT_BLACK)
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(uiState.lineSpacing, Alignment.Bottom),
                    ) {
                        for (message in messages) {
                            ClickableText(
                                text = message.message,
                                color = uiState.textColor,
                                acceptInsertion = false, // No shift key in touch interface
                            )
                        }
                    }
                }
                val bottomBarHeight = 32
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(bottomBarHeight),
                ) {
                    val focusRequester = remember { FocusRequester() }
                    val interactionSource = remember { MutableInteractionSource() }
                    var focused by remember { mutableStateOf(false) }
                    LaunchedEffect(interactionSource) {
                        interactionSource.interactions.collect {
                            when (it) {
                                FocusInteraction.Blur -> {
                                    focused = false
                                }

                                FocusInteraction.Focus -> {
                                    focused = true
                                }
                            }
                        }
                    }
                    IconButton(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(bottomBarHeight),
                        focusable = false,
                        onClick = {
                            if (focused) {
                                focusRequester.requestBlur()
                            } else {
                                focusRequester.requestFocus()
                            }
                        },
                    ) {
                        Icon(Textures.icon_chat_keyboard)
                    }
                    IconButton(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(bottomBarHeight),
                        onClick = {
                            screenModel.openSettingsDialog()
                        },
                    ) {
                        Icon(Textures.icon_chat_setting)
                    }
                    EditText(
                        interactionSource = interactionSource,
                        modifier = Modifier
                            .focusRequester(focusRequester)
                            .weight(1f)
                            .fillMaxHeight(),
                        value = uiState.text,
                        onValueChanged = screenModel::updateText,
                        onEnter = screenModel::sendText,
                    )
                    IconButton(
                        modifier = Modifier
                            .width(64)
                            .fillMaxHeight(),
                        onClick = screenModel::sendText,
                    ) {
                        Icon(Textures.icon_chat_send)
                    }
                }
            }
        }
    }
}

@ActualImpl(ChatScreenProvider::class)
object ChatScreenProviderFactoryImpl : ChatScreenProvider {
    @JvmStatic
    @ActualConstructor
    fun of(): ChatScreenProvider = this

    override fun openChatScreen() {
        val screenFactory: ScreenFactory = ScreenFactoryFactory.of()
        screenFactory.openScreen(
            renderBackground = false,
            title = Text.translatable(Texts.SCREEN_CHAT_TITLE),
        ) {
            ChatScreen()
        }
    }
}

