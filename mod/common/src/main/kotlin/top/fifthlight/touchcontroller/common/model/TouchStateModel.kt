package top.fifthlight.touchcontroller.common.model

import org.koin.core.component.KoinComponent
import top.fifthlight.data.Offset
import top.fifthlight.touchcontroller.common.state.Pointer
import top.fifthlight.touchcontroller.common.state.PointerState

class TouchStateModel : KoinComponent {
    val pointers = HashMap<Int, Pointer>()

    fun addPointer(index: Int, position: Offset) {
        pointers[index]?.let { pointer ->
            pointer.position = position
            // Reset state if it was Released (e.g., double-tap scenario)
            if (pointer.state is PointerState.Released) {
                pointer.state = PointerState.New
            }
        } ?: run {
            pointers[index] = Pointer(position = position)
        }
    }

    fun removePointer(index: Int) {
        val pointer = pointers[index] ?: return
        if (pointer.state !is PointerState.Released) {
            pointer.state = PointerState.Released(previousPosition = pointer.position, previousState = pointer.state)
        }
    }

    fun clearPointer() {
        pointers.forEach { (_, pointer) ->
            if (pointer.state !is PointerState.Released) {
                pointer.state =
                    PointerState.Released(previousPosition = pointer.position, previousState = pointer.state)
            }
        }
    }
}