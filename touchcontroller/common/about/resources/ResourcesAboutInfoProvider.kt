package top.fifthlight.touchcontroller.common.about.resources

import kotlinx.serialization.json.Json
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl
import top.fifthlight.touchcontroller.buildinfo.BuildInfo
import top.fifthlight.touchcontroller.common.about.AboutInfo
import top.fifthlight.touchcontroller.common.about.AboutInfoProvider
import top.fifthlight.touchcontroller.common.about.Libs
import java.io.InputStream

@ActualImpl(AboutInfoProvider::class)
object ResourcesAboutInfoProvider : AboutInfoProvider {
    @ActualConstructor
    @JvmStatic
    fun of() = ResourcesAboutInfoProvider

    private fun getResourceAsStream(name: String): InputStream? = this.javaClass.classLoader.getResourceAsStream(name)
    private fun readResource(name: String): String? = getResourceAsStream(name)?.reader()?.use { it.readText() }

    private val format = Json {
        ignoreUnknownKeys = true
    }

    override val aboutInfo: AboutInfo by lazy {
        val modLicense = readResource("LICENSE_${BuildInfo.MOD_NAME}")
        val librariesJson = readResource("aboutlibraries.json")
        val libraries = librariesJson?.let { librariesJson ->
            format.decodeFromString<Libs>(librariesJson)
        }
        AboutInfo(
            modLicense = modLicense,
            libraries = libraries,
        )
    }
}
