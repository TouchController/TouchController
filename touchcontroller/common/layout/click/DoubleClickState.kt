package top.fifthlight.touchcontroller.common.layout.click

data class DoubleClickState(private val clickTime: Int = 15) {
    private var lastClick: Int = -1

    fun click(time: Int): Boolean {
        if (lastClick == -1) {
            lastClick = time
            return false
        }

        val interval = time - lastClick
        lastClick = time
        val doubleClicked = interval <= clickTime
        if (doubleClicked) {
            lastClick = -1
        }

        return doubleClicked
    }

    fun clear() {
        this.lastClick = -1
    }
}
