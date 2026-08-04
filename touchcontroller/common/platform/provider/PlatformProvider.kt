/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.platform.provider

import org.lwjgl.sdl.SDL_Event
import org.slf4j.LoggerFactory
import top.fifthlight.blazesdl.api.BlazeSDLAPI
import top.fifthlight.blazesdl.api.BlazeSDLEventHandler
import top.fifthlight.touchcontroller.common.gal.window.PlatformWindowProvider
import top.fifthlight.touchcontroller.common.gal.window.PlatformWindow
import top.fifthlight.touchcontroller.common.platform.Platform
import top.fifthlight.touchcontroller.common.platform.ios.IosPlatform
import top.fifthlight.touchcontroller.common.platform.proxy.ProxyPlatform
import top.fifthlight.touchcontroller.common.platform.sdl.SdlPlatform
import top.fifthlight.touchcontroller.proxy.server.localhostLauncherSocketProxyServer
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.outputStream

object PlatformProvider {
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
            isAndroid -> "Android"
            isIos -> "iOS"
            else -> systemName
        }
    }

    private val hasBlazeSDL: Boolean by lazy {
        try {
            Class.forName("top.fifthlight.blazesdl.api.BlazeSDLAPI")
            BlazeSDLAPI.getInstance() != null
        } catch (ex: ClassNotFoundException) {
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

    internal fun loadPlatform(): (() -> Platform)? {
        if (hasBlazeSDL) {
            BlazeSDLAPI.getInstance()?.let { api ->
                return@loadPlatform {
                    SdlPlatform { handler ->
                        api.registerEventHandler(object : BlazeSDLEventHandler {
                            override fun getPriority(): Int = 1000

                            override fun handleEvent(event: SDL_Event): Boolean = handler(event)
                        })
                    }
                }
            }
        }

        val socketPort = System.getenv("TOUCH_CONTROLLER_PROXY")?.toIntOrNull()
        if (socketPort != null) {
            logger.warn("TOUCH_CONTROLLER_PROXY set, use legacy UDP transport")
            val proxy = localhostLauncherSocketProxyServer(socketPort) ?: return null
            return { ProxyPlatform(proxy) }
        }

        logger.info("System name: $systemName, system arch: $systemArch")

        val platformWindow = PlatformWindowProvider.platform

        if (isIos) {
            IosPlatform().also { platform ->
                platform.resize(PlatformWindowProvider.windowWidth, PlatformWindowProvider.windowHeight)
            }
        }

        NativeLibraryLoader.probeNativeLibraryInfo(platformWindow)?.let { info ->
            logger.info("Native library info:")
            logger.info("path: ${info.modContainerPath}")
            val nativeLibrary = try {
                javaClass.classLoader.getResourceAsStream(info.modContainerPath)
            } catch (ex: Exception) {
                logger.warn("Failed to get native library path: {}", info.modContainerPath, ex)
                return null
            } ?: run {
                logger.warn("Failed to get native library path: {}", info.modContainerPath)
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

            return {
                info.platformFactory().also { platform ->
                    platform.resize(PlatformWindowProvider.windowWidth, PlatformWindowProvider.windowHeight)
                }
            }
        }

        if (platformWindow is PlatformWindow.Sdl) {
            return { SdlPlatform(platformWindow::registerEventHandler) }
        }

        logger.warn("No platform loaded")
        return null
    }

    private var platformNativeLoaded = false
    private var platformProvider: (() -> Platform)? = null

    private var platformLoaded = false
    var platform: Platform? = null
        private set

    fun loadNative() = synchronized(this) {
        if (platformNativeLoaded) {
            return
        }
        this@PlatformProvider.platformProvider = loadPlatform()
        platformNativeLoaded = true
    }

    fun initialize() = synchronized(this) {
        require(platformNativeLoaded) { "Platform native libraries hasn't been initialized" }
        if (platformLoaded) {
            return
        }
        platformLoaded = true
        platform = platformProvider?.invoke().also { it?.init() }
    }
}
