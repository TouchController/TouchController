package top.fifthlight.armorstand.util

import net.fabricmc.loader.api.FabricLoader
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl
import java.nio.file.Path

@ActualImpl(GameDirectoryGetter::class)
class GameDirectoryGetterImpl @ActualConstructor("create") constructor() : GameDirectoryGetter {
    override val gameDirectory: Path
    override val configDirectory: Path

    init {
        val loader = FabricLoader.getInstance()
        gameDirectory = loader.gameDir
        configDirectory = loader.configDir
    }
}
