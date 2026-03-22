package top.fifthlight.touchcontroller.common.gal.window

import top.fifthlight.data.IntSize
import top.fifthlight.data.Offset
import top.fifthlight.mergetools.api.ExpectFactory

interface WindowHandle {
    val size: IntSize
    val scaledSize: IntSize
    val mouseLeftPressed: Boolean
    val mousePosition: Offset?

    @ExpectFactory
    interface Factory {
        fun of(): WindowHandle
    }
}
