package top.fifthlight.touchcontroller.common.layout.data

class ContextTimer {
    var clientTick: Int = 0
        private set
    var renderTick: Int = 0
        private set

    fun clientTick() {
        clientTick++
    }

    fun renderTick() {
        renderTick++
    }
}
