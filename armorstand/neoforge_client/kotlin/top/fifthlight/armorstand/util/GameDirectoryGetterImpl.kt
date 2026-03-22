package top.fifthlight.armorstand.util

import net.neoforged.fml.loading.FMLPaths
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl
import java.nio.file.Path

@ActualImpl(GameDirectoryGetter::class)
class GameDirectoryGetterImpl @ActualConstructor("create") constructor() : GameDirectoryGetter {
    override val gameDirectory: Path = FMLPaths.GAMEDIR.get()
    override val configDirectory: Path = FMLPaths.CONFIGDIR.get()
}
