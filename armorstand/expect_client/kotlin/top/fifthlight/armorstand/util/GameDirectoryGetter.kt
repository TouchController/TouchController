package top.fifthlight.armorstand.util

import top.fifthlight.mergetools.api.ExpectFactory
import java.nio.file.Path

interface GameDirectoryGetter {
    companion object : GameDirectoryGetter by GameDirectoryGetterFactory.create()

    val gameDirectory: Path
    val configDirectory: Path

    @ExpectFactory
    interface Factory {
        fun create(): GameDirectoryGetter
    }
}
