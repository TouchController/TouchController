package top.fifthlight.touchcontroller.common.platform.native

object Interface {
    @JvmStatic
    external fun init(artJvmPtr: Long, artApplication: Long)

    @JvmStatic
    external fun send(buf: ByteArray, length: Int)

    @JvmStatic
    external fun poll(buf: ByteArray): Int

    @JvmStatic
    external fun stop()
}
