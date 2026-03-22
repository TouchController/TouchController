package top.fifthlight.touchcontroller.gal.library.v1_21_11

import org.slf4j.LoggerFactory
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl
import top.fifthlight.touchcontroller.common.gal.library.NativeLibraryPathGetter
import java.io.IOException
import java.io.InputStream

@ActualImpl(NativeLibraryPathGetter::class)
object NativeLibraryPathGetterImpl : NativeLibraryPathGetter {
    @JvmStatic
    @ActualConstructor
    fun of(): NativeLibraryPathGetter = this

    private val logger = LoggerFactory.getLogger(NativeLibraryPathGetterImpl::class.java)

    override fun getNativeLibraryPath(path: String): InputStream? {
        return try {
            javaClass.classLoader.getResourceAsStream(path)
        } catch (ex: IOException) {
            logger.warn("Open native library failed", ex)
            null
        }
    }
}
