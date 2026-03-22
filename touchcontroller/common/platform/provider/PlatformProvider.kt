package top.fifthlight.touchcontroller.common.platform.provider

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import org.slf4j.LoggerFactory
import top.fifthlight.blazesdl.api.BlazeSDLAPI
import top.fifthlight.touchcontroller.common.gal.window.GlfwPlatform
import top.fifthlight.touchcontroller.common.gal.library.NativeLibraryPathGetter
import top.fifthlight.touchcontroller.common.gal.library.NativeLibraryPathGetterFactory
import top.fifthlight.touchcontroller.common.gal.window.PlatformWindowProvider
import top.fifthlight.touchcontroller.common.platform.Platform
import top.fifthlight.touchcontroller.common.platform.android.AndroidPlatform
import top.fifthlight.touchcontroller.common.platform.ios.IosPlatform
import top.fifthlight.touchcontroller.common.platform.proxy.ProxyPlatform
import top.fifthlight.touchcontroller.common.platform.sdl.BlazeSDLPlatform
import top.fifthlight.touchcontroller.common.platform.wayland.WaylandPlatform
import top.fifthlight.touchcontroller.common.platform.win32.Win32Platform
import top.fifthlight.touchcontroller.proxy.server.localhostLauncherSocketProxyServer
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.DosFileAttributeView
import java.nio.file.attribute.PosixFileAttributeView
import java.nio.file.attribute.PosixFilePermission
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.fileAttributesView
import kotlin.io.path.outputStream

object PlatformProvider {
    private val nativeLibraryPathGetter: NativeLibraryPathGetter = NativeLibraryPathGetterFactory.of()
    private val logger = LoggerFactory.getLogger(PlatformProvider::class.java)

    val systemName: String by lazy { System.getProperty("os.name") }
    val systemArch: String by lazy { System.getProperty("os.arch") }

    val isAndroid: Boolean by lazy {
        // Detect the existence of /system/build.prop
        val path = Paths.get("/", "system", "build.prop")
        try {
            path.exists()
        } catch (ex: SecurityException) {
            logger.info("Failed to access $path, may running on Android", ex)
            true
        } catch (ex: IOException) {
            logger.info("Failed to access $path, may running on Android", ex)
            true
        }
    }

    val isIos: Boolean by lazy {
        if (systemName.contains("iOS", ignoreCase = true)) {
            return@lazy true
        }
        // Check if running on iOS by detecting /var/mobile (iOS-specific path)
        val iosPath = Paths.get("/", "var", "mobile")
        try {
            iosPath.exists()
        } catch (ex: Exception) {
            logger.info("Failed to check iOS path, assuming iOS", ex)
            true
        }
    }

    val displayName by lazy {
        when {
            PlatformProvider.isAndroid -> "Android"
            PlatformProvider.isIos -> "iOS"
            else -> PlatformProvider.systemName
        }
    }

    private fun extractNativeLibrary(prefix: String, suffix: String, stream: InputStream): Path =
        stream.use { input ->
            Files.createTempFile(prefix, suffix).also { outputFile ->
                logger.info("Extracting native library to $outputFile")
                outputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }

    private data class NativeLibraryInfo(
        val modContainerPath: String,
        val extractPrefix: String,
        val extractSuffix: String,
        val readOnlySetter: (Path) -> Unit = {},
        val removeAfterLoaded: Boolean,
        val platformFactory: () -> Platform,
    )

    private fun windowsReadOnlySetter(path: Path) {
        val attributeView = path.fileAttributesView<DosFileAttributeView>()
        attributeView.setReadOnly(true)
    }

    private fun posixReadOnlySetter(path: Path) {
        val attributeView = path.fileAttributesView<PosixFileAttributeView>()
        // 500
        attributeView.setPermissions(
            setOf(
                PosixFilePermission.OWNER_READ,
                PosixFilePermission.OWNER_EXECUTE
            )
        )
    }

    private fun probeNativeLibraryInfo(): NativeLibraryInfo? {
        if ((systemName.startsWith("Linux", ignoreCase = true) && isAndroid) ||
            systemName.contains("Android", ignoreCase = true)
        ) {
            logger.info("Android detected")

            val socketName = System.getenv("TOUCH_CONTROLLER_PROXY_SOCKET")?.takeIf { it.isNotEmpty() }
            if (socketName == null) {
                logger.info("No TOUCH_CONTROLLER_PROXY_SOCKET environment set, TouchController will not be loaded")
                return null
            }

            val target = when (systemArch) {
                "x86_32", "x86", "i386", "i486", "i586", "i686" -> "android_x86_32"
                "amd64", "x86_64" -> "android_x86_64"
                "armeabi", "armeabi-v7a", "armhf", "arm", "armel" -> "android_armv7"
                "arm64", "aarch64" -> "android_aarch64"
                else -> null
            } ?: run {
                logger.warn("Unsupported Android arch")
                return null
            }
            logger.info("Target: $target")

            val libraryName = "proxy_server_android"

            return NativeLibraryInfo(
                modContainerPath = "${libraryName}_${target}/lib${libraryName}.so",
                extractPrefix = "lib$libraryName",
                extractSuffix = ".so",
                readOnlySetter = ::posixReadOnlySetter,
                removeAfterLoaded = true,
                platformFactory = { AndroidPlatform(socketName) },
            )
        }

        when (val platform = PlatformWindowProvider.platform) {
            is GlfwPlatform.Win32 -> {
                val target = when (systemArch) {
                    "x86_32", "x86", "i386", "i486", "i586", "i686" -> "windows_x86_32"
                    "amd64", "x86_64" -> "windows_x86_64"
                    "arm64", "aarch64" -> "windows_aarch64"
                    else -> null
                } ?: run {
                    logger.warn("Unsupported Windows arch: $systemArch")
                    return null
                }
                val systemVersion = System.getProperty("os.version")
                val majorVersion = systemVersion.substringBefore(".").toIntOrNull()
                val isLegacy = majorVersion == null || majorVersion < 10
                logger.info("Target: $target, legacy: $isLegacy")
                val libraryName = if (isLegacy) {
                    "proxy_server_windows_legacy"
                } else {
                    "proxy_server_windows"
                }

                return NativeLibraryInfo(
                    modContainerPath = "${libraryName}_${target}/lib${libraryName}.dll",
                    extractPrefix = "lib$libraryName",
                    extractSuffix = ".dll",
                    readOnlySetter = ::windowsReadOnlySetter,
                    removeAfterLoaded = false,
                    platformFactory = { Win32Platform(platform.nativeWindow) },
                )
            }

            is GlfwPlatform.Wayland, GlfwPlatform.X11 -> {
                val target = when (systemArch) {
                    "amd64", "x86_64" -> "linux_x86_64"
                    "armv8", "arm64", "aarch64" -> "linux_aarch64"
                    else -> null
                } ?: run {
                    logger.warn("Unsupported Linux arch: $systemArch")
                    return null
                }
                val libraryName = when (platform) {
                    is GlfwPlatform.Wayland -> "proxy_server_wayland"
                    is GlfwPlatform.X11 -> {
                        logger.warn("X11 is not supported for now")
                        return null
                    }

                    else -> throw AssertionError()
                }
                // TODO: detect musl, and use musl libraries
                logger.info("Target: $target")

                return NativeLibraryInfo(
                    modContainerPath = "${libraryName}_${target}/lib${libraryName}.so",
                    extractPrefix = "lib$libraryName",
                    extractSuffix = ".so",
                    readOnlySetter = ::posixReadOnlySetter,
                    removeAfterLoaded = true,
                    platformFactory = {
                        when (platform) {
                            is GlfwPlatform.Wayland -> WaylandPlatform(platform.nativeWindow)
                            else -> throw AssertionError()
                        }
                    },
                )
            }

            GlfwPlatform.Cocoa -> {
                logger.warn("macOS is not supported for now")
                return null
            }

            GlfwPlatform.Unknown -> {
                logger.warn("Unsupported system: $systemName")
                return null
            }
        }
    }

    private fun loadPlatform(): Platform? {
        BlazeSDLAPI.getInstance()?.let { api -> return BlazeSDLPlatform(api) }

        val socketPort = System.getenv("TOUCH_CONTROLLER_PROXY")?.toIntOrNull()
        if (socketPort != null) {
            logger.warn("TOUCH_CONTROLLER_PROXY set, use legacy UDP transport")
            val proxy = localhostLauncherSocketProxyServer(socketPort) ?: return null
            @OptIn(DelicateCoroutinesApi::class)
            return ProxyPlatform(GlobalScope, proxy)
        }

        logger.info("System name: $systemName, system arch: $systemArch")
        if (isIos) {
            // iOS: native library is statically linked into the launcher app
            // No need to load it dynamically - JNI symbols are already available
            val socketPath = System.getenv("TOUCH_CONTROLLER_PROXY_SOCKET")
            if (socketPath.isNullOrEmpty()) {
                logger.info("TOUCH_CONTROLLER_PROXY_SOCKET not set")
                logger.info("Please enable TouchController in launcher settings and restart the game")
                return null
            }

            val platform = IosPlatform(socketPath)
            platform.resize(PlatformWindowProvider.windowWidth, PlatformWindowProvider.windowHeight)
            return platform
        }

        val info = probeNativeLibraryInfo() ?: return null

        logger.info("Native library info:")
        logger.info("path: ${info.modContainerPath}")
        val nativeLibrary = nativeLibraryPathGetter.getNativeLibraryPath(info.modContainerPath) ?: run {
            logger.warn("Failed to get native library path")
            return null
        }

        val destinationFile = try {
            extractNativeLibrary(info.extractPrefix, info.extractSuffix, nativeLibrary)
        } catch (ex: Exception) {
            logger.warn("Failed to extract native library", ex)
            return null
        }

        try {
            info.readOnlySetter.invoke(destinationFile)
        } catch (ex: Exception) {
            logger.info("Failed to set file $destinationFile read-only", ex)
        }

        logger.info("Loading native library")
        try {
            @Suppress("UnsafeDynamicallyLoadedCode")
            System.load(destinationFile.toAbsolutePath().toString())
        } catch (_: Exception) {
            return null
        }
        logger.info("Loaded native library")

        if (info.removeAfterLoaded) {
            destinationFile.deleteIfExists()
        }

        val platform = info.platformFactory.invoke()
        platform.resize(PlatformWindowProvider.windowWidth, PlatformWindowProvider.windowHeight)
        return platform
    }

    private var platformLoaded = false
    var platform: Platform? = null
        private set

    fun load() {
        if (platformLoaded) {
            return
        }
        this@PlatformProvider.platform = loadPlatform()
        platformLoaded = true
    }
}
