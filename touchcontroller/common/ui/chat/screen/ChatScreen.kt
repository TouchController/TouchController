/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.chat.screen

import androidx.compose.runtime.*
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
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
import top.fifthlight.combine.core.screen.LocalCloseHandler
import top.fifthlight.combine.core.screen.ScreenFactory
import top.fifthlight.combine.core.text.TextHitTestResult
import top.fifthlight.combine.core.widget.layout.Column
import top.fifthlight.combine.core.widget.layout.Row
import top.fifthlight.combine.theme.blackstone.widget.AlertDialog
import top.fifthlight.combine.widget.*
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.assets.texture.Textures
import top.fifthlight.touchcontroller.common.control.action.provider.ChatScreenProvider
import top.fifthlight.touchcontroller.common.gal.chat.ChatMessageProvider
import top.fifthlight.touchcontroller.common.gal.chat.ChatMessageProviderFactory
import top.fifthlight.touchcontroller.common.ui.chat.component.ChatComponent
import top.fifthlight.touchcontroller.common.ui.chat.component.ChatSettingsComponent
import top.fifthlight.touchcontroller.common.ui.navigation.BackController
import top.fifthlight.touchcontroller.common.ui.navigation.LifecycleController
import top.fifthlight.touchcontroller.common.ui.state.subscribeAsState
import top.fifthlight.touchcontroller.common.ui.theme.TouchControllerTheme
import top.fifthlight.touchcontroller.common.ui.widget.ColorPreferenceItem
import top.fifthlight.touchcontroller.common.ui.widget.IntSliderPreferenceItem
import top.fifthlight.touchcontroller.common.ui.widget.Scaffold
import top.fifthlight.touchcontroller.common.ui.widget.navigation.AppBar
import top.fifthlight.touchcontroller.common.ui.widget.navigation.BackButton

@Composable
fun ChatSettingsDialog(
    component: ChatSettingsComponent?,
    onClose: () -> Unit,
) = AlertDialog(
    value = component,
    valueTransformer = { it },
    modifier = Modifier.fillMaxWidth(.6f),
    onDismissRequest = onClose,
    actions = { component ->
        WarningButton(onClick = {
            component.resetSettings()
        }) {
            Text(Text.translatable(Texts.SCREEN_CHAT_SETTINGS_RESET))
        }
        GuideButton(onClick = onClose) {
            Text(Text.translatable(Texts.SCREEN_CHAT_SETTINGS_OK))
        }
    },
) { component ->
    val uiState by component.uiState.subscribeAsState()
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
                component.updateLineSpacing(it)
            }
        )

        ColorPreferenceItem(
            modifier = Modifier.fillMaxWidth(),
            title = Text.translatable(Texts.SCREEN_CHAT_SETTINGS_TEXT_COLOR),
            value = uiState.textColor,
            onValueChanged = {
                component.updateTextColor(it)
            }
        )
    }
}

@Composable
fun ChatScreen(component: ChatComponent) {
    TouchControllerTheme {
        val uiState by component.uiState.subscribeAsState()

        val dialog by component.dialog.subscribeAsState()
        val instance = dialog.child?.instance
        ChatSettingsDialog(
            component = (instance as? ChatComponent.ChildModel.Settings)?.component,
            onClose = component::closeSettingsDialog,
        )

        Scaffold(
            topBar = {
                AppBar(
                    modifier = Modifier.fillMaxWidth(),
                    leading = {
                        BackButton(
                            screenName = Text.translatable(Texts.SCREEN_CHAT_EXIT),
                            onClick = {
                                component.onBack()
                            },
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
                                component.updateText(it.text)
                            } else {
                                // TODO: append after cursor. there is no good way to do it currently
                                component.updateText(uiState.text + it.text)
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
                            component.openSettingsDialog()
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
                        onValueChanged = component::updateText,
                        onEnter = component::sendText,
                    )
                    IconButton(
                        modifier = Modifier
                            .width(64)
                            .fillMaxHeight(),
                        onClick = component::sendText,
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
        val lifecycleRegistry = LifecycleRegistry()
        val backDispatcher = BackDispatcher()

        ScreenFactory.openScreen(
            renderBackground = false,
            title = Text.translatable(Texts.SCREEN_CHAT_TITLE),
        ) {
            val closeHandler = LocalCloseHandler.current
            val viewModel = remember(closeHandler) {
                ChatComponent(
                    componentContext = DefaultComponentContext(
                        lifecycle = lifecycleRegistry,
                        backHandler = backDispatcher,
                    ),
                    onBack = {
                        closeHandler.close()
                    },
                )
            }

            BackController(backDispatcher)
            LifecycleController(lifecycleRegistry)

            ChatScreen(viewModel)
        }
    }
}

