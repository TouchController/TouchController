package top.fifthlight.touchcontroller.common.platform.win32

object Interface {
    @JvmStatic
    external fun init(windowHandle: Long)

    @JvmStatic
    external fun pollEvent(buffer: ByteArray): Int

    @JvmStatic
    external fun pushEvent(buffer: ByteArray, length: Int)
}
