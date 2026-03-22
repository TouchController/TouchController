package top.fifthlight.touchcontroller.common.platform.ios

import org.slf4j.LoggerFactory

object Transport {
    private val logger = LoggerFactory.getLogger(Transport::class.java)

    private external fun init()
    external fun new(path: String): Long
    external fun receive(handle: Long, buffer: ByteArray): Int
    external fun send(handle: Long, buffer: ByteArray, off: Int, len: Int)
    external fun destroy(handle: Long)

    init {
        // For NeoForge compatibility: register native methods explicitly
        // NeoForge uses JPMS ModuleLayers which break standard JNI native lookup
        // for statically linked libraries.
        try {
            logger.info("iOS: Calling registerNatives()...")
            registerNatives()
            logger.info("iOS: registerNatives() succeeded")
        } catch (e: UnsatisfiedLinkError) {
            logger.info("iOS: registerNatives() failed: ${e.message}")
            logger.info("iOS: Trying launcher Tools.registerTouchControllerNatives()...")
            try {
                // Call launcher's Tools class which is loaded via system class loader
                // and can access JNI functions in the main app binary
                val toolsClass = Class.forName("lol.gzmc.glaunch.Utils")
                val method = toolsClass.getMethod("registerTouchControllerNatives", Class::class.java)
                method.invoke(null, Transport::class.java)
                logger.info("iOS: Tools.registerTouchControllerNatives() succeeded")
            } catch (e2: Exception) {
                logger.error("iOS: Tools.registerTouchControllerNatives() failed: ${e2.message}", e2)
                throw e // rethrow original error
            }
        }
        logger.info("iOS: Calling init()...")
        init()
        logger.info("iOS: init() succeeded")
    }

    /**
     * Explicitly registers native methods for this class.
     * Implemented in libproxy_server_ios.a (Rust).
     */
    @JvmStatic
    private external fun registerNatives()
}
