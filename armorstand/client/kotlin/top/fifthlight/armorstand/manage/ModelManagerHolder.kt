package top.fifthlight.armorstand.manage

import top.fifthlight.armorstand.util.GameDirectoryGetter
import java.lang.AutoCloseable
import java.nio.file.Path

object ModelManagerHolder : AutoCloseable {
    private var closed = false
    private var _instance: ModelManagerImpl? = null
    val modelDir: Path = System.getProperty("armorstand.modelDir")?.let {
        Path.of(it).toAbsolutePath()
    } ?: GameDirectoryGetter.gameDirectory.resolve("models")
    val instance: ModelManager
        get() = _instance ?: throw IllegalStateException("ModelManager not initialized")

    @Synchronized
    fun initialize() {
        check(!closed) { "Already closed" }
        if (_instance != null) {
            return
        }
        val instance = ModelManagerImpl()
        _instance = instance
    }

    @Synchronized
    override fun close() {
        if (closed) {
            return
        }
        closed = true
        _instance?.let {
            it.stopWatching()
            it.close()
        }
    }
}
