package top.fifthlight.touchcontroller.fabric.v26_1.gal.config

import net.fabricmc.loader.api.FabricLoader
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl
import top.fifthlight.touchcontroller.buildinfo.BuildInfo
import top.fifthlight.touchcontroller.common.gal.config.ConfigDirectoryProvider
import java.nio.file.Path

@ActualImpl(ConfigDirectoryProvider::class)
object ConfigDirectoryProviderImpl : ConfigDirectoryProvider {
    @JvmStatic
    @ActualConstructor
    fun of(): ConfigDirectoryProvider = this

    override val configDirectory: Path by lazy {
        val fabricLoader = FabricLoader.getInstance()
        fabricLoader.configDir.resolve(BuildInfo.MOD_ID)
    }
}
