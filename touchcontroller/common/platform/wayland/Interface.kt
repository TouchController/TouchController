package top.fifthlight.touchcontroller.common.platform.wayland

object Interface {
    @JvmStatic
    external fun init(displayHandle: Long, windowHandle: Long)

    @JvmStatic
    external fun resize(width: Int, height: Int)

    @JvmStatic
    external fun pollEvent(buffer: ByteArray): Int

    @JvmStatic
    external fun pushEvent(buffer: ByteArray, length: Int)
}
