package top.fifthlight.touchcontroller.common.layout.queue

import top.fifthlight.combine.paint.Canvas

typealias DrawCall = (Canvas) -> Unit

class DrawQueue {
    private val items = mutableListOf<DrawCall>()

    fun enqueue(block: DrawCall) {
        items.add(block)
    }

    fun execute(canvas: Canvas) {
        items.forEach { it.invoke(canvas) }
    }
}
