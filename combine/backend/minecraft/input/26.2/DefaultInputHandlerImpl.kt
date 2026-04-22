package top.fifthlight.combine.backend.minecraft.input.v26_2

import net.minecraft.client.Minecraft
import top.fifthlight.combine.input.text.InputHandler
import top.fifthlight.combine.input.text.TextInputState
import top.fifthlight.data.IntRect
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl

@ActualImpl(InputHandler::class)
object DefaultInputHandlerImpl : InputHandler {
    @JvmStatic
    @ActualConstructor("ofDefault")
    fun ofDefault() = DefaultInputHandlerImpl

    override val events
        get() = InputHandler.Empty.events

    private var haveState = false
    override fun updateInputState(textInputState: TextInputState?, cursorRect: IntRect?, areaRect: IntRect?) {
        val client = Minecraft.getInstance()
        val textInputManager = client.textInputManager()
        if (!haveState && textInputState != null) {
            textInputManager.startTextInput()
        } else if (haveState && textInputState == null) {
            textInputManager.stopTextInput()
        }
        cursorRect?.let { cursorRect ->
            textInputManager.setTextInputArea(cursorRect.left, cursorRect.top, cursorRect.right, cursorRect.bottom)
        }
        haveState = textInputState != null
    }

    override fun tryShowKeyboard() {}
    override fun tryHideKeyboard() {}
}
