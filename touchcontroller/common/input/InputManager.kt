/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.input

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import top.fifthlight.combine.core.input.text.InputHandler
import top.fifthlight.combine.core.input.text.InputHandlerFactory
import top.fifthlight.combine.core.input.text.TextInputState
import top.fifthlight.combine.core.util.dispatcher.Game
import top.fifthlight.data.IntRect
import top.fifthlight.touchcontroller.common.gal.window.WindowHandle
import top.fifthlight.touchcontroller.common.gal.window.WindowHandleFactory
import top.fifthlight.touchcontroller.common.platform.Platform
import top.fifthlight.touchcontroller.common.platform.capabilities.PlatformCapabilitiesHolder
import top.fifthlight.touchcontroller.common.platform.provider.PlatformProvider
import top.fifthlight.touchcontroller.proxy.client.PlatformCapability
import top.fifthlight.touchcontroller.proxy.message.*
import top.fifthlight.touchcontroller.proxy.message.input.TextInputState as ProxyTextInputState
import top.fifthlight.touchcontroller.proxy.message.input.TextRange as ProxyTextRange

object InputManager : InputHandler {
    private val default = InputHandlerFactory.ofDefault()

    private val platform: Platform?
        get() = PlatformProvider.platform

    private var inputState: TextInputState? = null
    private var cursorRect: IntRect? = null
    private var areaRect: IntRect? = null
    private val windowHandle: WindowHandle = WindowHandleFactory.of()
    private val _events = MutableSharedFlow<TextInputState>()
    override val events = _events.asSharedFlow()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Game)

    fun updateNativeState(textInputState: TextInputState) {
        inputState = textInputState
        scope.launch {
            _events.emit(textInputState)
        }
    }

    override fun updateInputState(textInputState: TextInputState?, cursorRect: IntRect?, areaRect: IntRect?) {
        if (platform?.useDefaultInputHandler != false) {
            default?.updateInputState(textInputState, cursorRect, areaRect)
        }

        val inputStateUpdated = inputState != textInputState
        val cursorRectUpdated = cursorRect != this.cursorRect
        val areaRectUpdated = areaRect != this.areaRect
        this.inputState = textInputState
        this.cursorRect = cursorRect
        this.areaRect = areaRect
        if (PlatformCapability.TEXT_STATUS in PlatformCapabilitiesHolder.platformCapabilities.value) {
            platform?.let { platform ->
                if (inputStateUpdated) {
                    platform.sendEvent(InputStatusMessage(textInputState?.let {
                        ProxyTextInputState(
                            text = textInputState.text,
                            composition = ProxyTextRange(
                                start = textInputState.composition.start,
                                length = textInputState.composition.length,
                            ),
                            selection = ProxyTextRange(
                                start = textInputState.selection.start,
                                length = textInputState.selection.length,
                            ),
                            selectionLeft = textInputState.selectionLeft,
                        )
                    }))
                }
                if (cursorRectUpdated) {
                    platform.sendEvent(
                        InputCursorMessage(
                            cursorRect?.let { rect ->
                                FloatRect(
                                    left = rect.offset.left.toFloat() / windowHandle.scaledSize.width.toFloat(),
                                    top = rect.offset.top.toFloat() / windowHandle.scaledSize.height.toFloat(),
                                    width = rect.size.width.toFloat() / windowHandle.scaledSize.width.toFloat(),
                                    height = rect.size.height.toFloat() / windowHandle.scaledSize.height.toFloat(),
                                )
                            }
                        )
                    )
                }
                if (areaRectUpdated) {
                    platform.sendEvent(
                        InputAreaMessage(
                            areaRect?.let { rect ->
                                FloatRect(
                                    left = rect.offset.left.toFloat() / windowHandle.scaledSize.width.toFloat(),
                                    top = rect.offset.top.toFloat() / windowHandle.scaledSize.height.toFloat(),
                                    width = rect.size.width.toFloat() / windowHandle.scaledSize.width.toFloat(),
                                    height = rect.size.height.toFloat() / windowHandle.scaledSize.height.toFloat(),
                                )
                            }
                        )
                    )
                }
            }
        }
    }

    override fun tryShowKeyboard() {
        platform?.let { platform ->
            if (PlatformCapability.KEYBOARD_SHOW in PlatformCapabilitiesHolder.platformCapabilities.value) {
                platform.sendEvent(KeyboardShowMessage(true))
            }
        }
    }

    override fun tryHideKeyboard() {
        platform?.let { platform ->
            if (PlatformCapability.KEYBOARD_SHOW in PlatformCapabilitiesHolder.platformCapabilities.value) {
                platform.sendEvent(KeyboardShowMessage(false))
            }
        }
    }
}
