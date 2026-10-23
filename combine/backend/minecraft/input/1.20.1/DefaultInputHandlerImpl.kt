package top.fifthlight.combine.backend.minecraft.input.v1_20_1

import top.fifthlight.combine.core.input.text.InputHandler
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl

@ActualImpl(InputHandler::class)
object DefaultInputHandlerImpl : InputHandler by InputHandler.Empty {
    @JvmStatic
    @ActualConstructor("ofDefault")
    fun ofDefault() = DefaultInputHandlerImpl
}
