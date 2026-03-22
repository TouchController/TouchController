package top.fifthlight.touchcontroller.common.model

import top.fifthlight.data.Offset
import top.fifthlight.touchcontroller.common.state.Pointer
import top.fifthlight.touchcontroller.common.state.PointerState

class TouchStateModel {
    val pointers = HashMap<Int, Pointer>()

    fun addPointer(index: Int, position: Offset) {
        pointers[index]?.let { pointer ->
            pointer.position = position
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
