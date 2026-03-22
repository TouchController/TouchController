package top.fifthlight.touchcontroller.common.event.key

import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl
import top.fifthlight.touchcontroller.common.gal.key.KeyBindingEventsHandler
import top.fifthlight.touchcontroller.common.gal.key.KeyBindingState

@ActualImpl(KeyBindingEventsHandler::class)
object KeyEvents: KeyBindingEventsHandler {
    @JvmStatic
    @ActualConstructor
    fun of(): KeyBindingEventsHandler = this

    private val handlers = mutableListOf<(KeyBindingState) -> Unit>()

    fun addHandler(handler: (KeyBindingState) -> Unit) {
        handlers.add(handler)
    }

    override fun onKeyDown(state: KeyBindingState) {
        handlers.forEach {
            it.invoke(state)
        }
    }
}
