package top.fifthlight.touchcontroller.common.platform

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.LoggerFactory
import top.fifthlight.touchcontroller.common.gal.GlfwPlatform
import top.fifthlight.touchcontroller.common.gal.NativeLibraryPathGetter
import top.fifthlight.touchcontroller.common.gal.PlatformWindowProvider
import top.fifthlight.touchcontroller.common.platform.android.AndroidPlatform
import top.fifthlight.touchcontroller.common.platform.ios.IOSPlatform
import top.fifthlight.touchcontroller.common.platform.proxy.ProxyPlatform
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

class PlatformProvider : KoinComponent {
    private val nativeLibraryPathGetter: NativeLibraryPathGetter by inject()

    private val logger = LoggerFactory.getLogger(PlatformProvider::class.java)

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

    val isIOS: Boolean by lazy {
        val systemName = System.getProperty("os.name") ?: ""
        // iOS detection: check for Darwin/Mac OS X + /var/mobile path exists (iOS sandbox)
        // or TOUCH_CONTROLLER_IOS_SOCKET env variable is set
        if (systemName.contains("Darwin", ignoreCase = true) ||
            systemName.contains("Mac OS X", ignoreCase = true) ||
            systemName.contains("iOS", ignoreCase = true)) {
            // Check if running on iOS by detecting /var/mobile (iOS-specific path)
            val iosPath = Paths.get("/var/mobile")
            try {
                iosPath.exists() || System.getenv("TOUCH_CONTROLLER_IOS_SOCKET")?.isNotEmpty() == true
            } catch (ex: Exception) {
                logger.info("Failed to check iOS path, assuming iOS", ex)
                true
            }
        } else {
            false
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
        val debugPath: Path?,
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

    private fun probeNativeLibraryInfo(windowProvider: PlatformWindowProvider): NativeLibraryInfo? {
        val systemName = System.getProperty("os.name")
        val systemArch = System.getProperty("os.arch")
        logger.info("System name: $systemName, system arch: $systemArch")

        if ((systemName.startsWith("Linux", ignoreCase = true) && isAndroid) || systemName.contains(
                "Android",
                ignoreCase = true
            )
        ) {
            logger.info("Android detected")

            val socketName = System.getenv("TOUCH_CONTROLLER_PROXY_SOCKET")?.takeIf { it.isNotEmpty() }
            if (socketName == null) {
                logger.info("No TOUCH_CONTROLLER_PROXY_SOCKET environment set, TouchController will not be loaded")
                return null
            }

            val targetArch = when (systemArch) {
                "x86_32", "x86", "i386", "i486", "i586", "i686" -> "i686-linux-android"
                "amd64", "x86_64" -> "x86_64-linux-android"
                "armeabi", "armeabi-v7a", "armhf", "arm", "armel" -> "armv7-linux-androideabi"
                "arm64", "aarch64" -> "aarch64-linux-android"
                else -> null
            } ?: run {
                logger.warn("Unsupported Android arch")
                return null
            }

            return NativeLibraryInfo(
                modContainerPath = "$targetArch/libproxy_server_android.so",
                debugPath = null,
                extractPrefix = "libproxy_server_android",
                extractSuffix = ".so",
                readOnlySetter = ::posixReadOnlySetter,
                removeAfterLoaded = true,
                platformFactory = { AndroidPlatform(socketName) },
            )
        }

        // iOS is handled separately in loadIOSPlatform() - native lib is statically linked

        val platform = windowProvider.platform
        when (platform) {
            is GlfwPlatform.Win32 -> {
                val (targetTriple, target) = when (systemArch) {
                    "x86_32", "x86", "i386", "i486", "i586", "i686" -> Pair("i686-w64-mingw32", "i686")
                    "amd64", "x86_64" -> Pair("x86_64-w64-mingw32", "x86_64")
                    "arm64", "aarch64" -> Pair("aarch64-w64-mingw32", "aarch64")
                    else -> null
                } ?: run {
                    logger.warn("Unsupported Windows arch: $systemArch")
                    return null
                }
                val systemVersion = System.getProperty("os.version")
                val majorVersion = systemVersion.substringBefore(".").toIntOrNull()
                val isLegacy = majorVersion == null || majorVersion < 10
                logger.info("Target arch: $targetTriple, legacy: $isLegacy")
                val libraryName = if (isLegacy) {
                    "libproxy_windows_legacy"
                } else {
                    "libproxy_windows"
                }

                return NativeLibraryInfo(
                    modContainerPath = "$targetTriple/$libraryName.dll",
                    debugPath = Paths.get(
                        "..",
                        "..",
                        "..",
                        "..",
                        "proxy-windows",
                        "build",
                        "cmake",
                        target,
                        "$libraryName.dll"
                    ),
                    extractPrefix = libraryName,
                    extractSuffix = ".dll",
                    readOnlySetter = ::windowsReadOnlySetter,
                    removeAfterLoaded = false,
                    platformFactory = { Win32Platform(platform.nativeWindow) },
                )
            }

            is GlfwPlatform.Wayland, GlfwPlatform.X11 -> {
                val (archPrefix, archSuffix) = when (systemArch) {
                    "x86_32", "x86", "i386", "i486", "i586", "i686" -> Pair("i386", "")
                    "amd64", "x86_64" -> Pair("x86_64", "")
                    "armv8", "arm64", "aarch64" -> Pair("aarch64", "")
                    "arm", "armhf", "armel", "armv7" -> Pair("arm", "eabihf")
                    else -> null
                } ?: run {
                    logger.warn("Unsupported Linux arch: $systemArch")
                    return null
                }
                val platformName = when (platform) {
                    is GlfwPlatform.Wayland -> "wayland"
                    is GlfwPlatform.X11 -> {
                        logger.warn("X11 is not supported for now")
                        return null
                    }
                    else -> throw AssertionError()
                }
                // TODO: detect musl, and use musl libraries
                val targetTriple = "$archPrefix-linux-gnu$archSuffix"
                logger.info("Target triple: $targetTriple")

                return NativeLibraryInfo(
                    modContainerPath = "$targetTriple/libproxy_linux_$platformName.so",
                    debugPath = Paths.get(
                        "..",
                        "..",
                        "..",
                        "..",
                        "proxy-linux",
                        "build",
                        "cmake",
                        archPrefix,
                        "libproxy_linux_$platformName.so"
                    ),
                    extractPrefix = "libproxy_linux_$platformName",
                    extractSuffix = ".so",
                    readOnlySetter = ::posixReadOnlySetter,
                    removeAfterLoaded = false,
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

    private fun loadIOSPlatform(windowProvider: PlatformWindowProvider): Platform? {
        // iOS: native library is statically linked into the launcher app
        // No need to load it dynamically - JNI symbols are already available
        val socketPath = System.getenv("TOUCH_CONTROLLER_IOS_SOCKET")
        if (socketPath.isNullOrEmpty()) {
            logger.info("TOUCH_CONTROLLER_IOS_SOCKET not set")
            logger.info("Please enable TouchController in launcher settings and restart the game")
            return null
        }

        logger.info("iOS: Using Unix socket at $socketPath (native library is statically linked)")
        val platform = IOSPlatform(socketPath)
        platform.resize(windowProvider.windowWidth, windowProvider.windowHeight)
        return platform
    }

    private fun loadPlatform(windowProvider: PlatformWindowProvider): Platform? {
        val socketPort = System.getenv("TOUCH_CONTROLLER_PROXY")?.toIntOrNull()
        if (socketPort != null) {
            logger.warn("TOUCH_CONTROLLER_PROXY set, use legacy UDP transport")
            val proxy = localhostLauncherSocketProxyServer(socketPort) ?: return null
            @OptIn(DelicateCoroutinesApi::class)
            return ProxyPlatform(GlobalScope, proxy)
        }

        // iOS special case: native library is statically linked into launcher
        if (isIOS) {
            return loadIOSPlatform(windowProvider)
        }

        val info = probeNativeLibraryInfo(windowProvider) ?: return null

        logger.info("Native library info:")
        logger.info("path: ${info.modContainerPath}")
        logger.info("debugPath: ${info.debugPath}")
        val nativeLibrary = nativeLibraryPathGetter.getNativeLibraryPath(
            path = info.modContainerPath,
            debugPath = info.debugPath
        ) ?: run {
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
        platform.resize(windowProvider.windowWidth, windowProvider.windowHeight)
        return platform
    }

    private var platformLoaded = false
    var platform: Platform? = null
        private set

    fun load(windowProvider: PlatformWindowProvider) {
        if (platformLoaded) {
            return
        }
        this@PlatformProvider.platform = loadPlatform(windowProvider)
        platformLoaded = true
    }
}