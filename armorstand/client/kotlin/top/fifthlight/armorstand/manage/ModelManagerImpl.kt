package top.fifthlight.armorstand.manage

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.slf4j.LoggerFactory
import top.fifthlight.armorstand.ArmorStandClient
import top.fifthlight.armorstand.manage.database.DatabaseManager
import top.fifthlight.armorstand.manage.database.DatabaseManagerImpl
import top.fifthlight.armorstand.manage.model.ModelItem
import top.fifthlight.armorstand.manage.model.ModelThumbnail
import top.fifthlight.armorstand.manage.scan.FileHandler
import top.fifthlight.armorstand.manage.scan.ModelLoaderFileHandler
import top.fifthlight.armorstand.manage.scan.ModelScanner
import top.fifthlight.armorstand.manage.scan.ModelScannerImpl
import top.fifthlight.armorstand.manage.schedule.ScanScheduler
import top.fifthlight.armorstand.manage.schema.H2SchemaManager
import top.fifthlight.armorstand.manage.schema.SchemaManager
import top.fifthlight.armorstand.manage.watch.ModelWatcher
import top.fifthlight.armorstand.manage.watch.ModelWatcherImpl
import top.fifthlight.armorstand.state.ModelInstanceManager
import top.fifthlight.armorstand.util.ModelHash
import java.io.IOException
import java.lang.AutoCloseable
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Instant
import kotlin.io.path.*

class ModelManagerImpl(
    private val databaseName: String = ".cache",
    private val defaultModelName: String = "armorstand.vrm",
    private val defaultAnimationName: String = "default-animation.zip",
    override val databaseManager: DatabaseManager = DatabaseManagerImpl(),
    private val schemaManager: SchemaManager = H2SchemaManager(),
    private val modelDir: Path = ModelManagerHolder.modelDir,
    private val fileHandler: FileHandler = ModelLoaderFileHandler,
    private val watcher: ModelWatcher = ModelWatcherImpl(modelDir) {
        fileHandler.isModelFile(it) || fileHandler.isAnimationFile(
            it
        )
    },
    private val scanner: ModelScanner = ModelScannerImpl(modelDir, databaseManager),
) : ModelManager, AutoCloseable {
    companion object {
        private val logger = LoggerFactory.getLogger(ModelManagerImpl::class.java)
    }

    private val databaseFile = modelDir.resolve("$databaseName.mv.db").toAbsolutePath()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val scheduler = ScanScheduler(
        onScan = {
            scanner.scan(fileHandler)
        }
    )
    override val lastUpdateTime = MutableStateFlow(scheduler.lastScanTime.value)

    init {
        scope.launch {
            scheduler.lastScanTime.collect {
                lastUpdateTime.value = it
            }
        }

        val extractDefaultModel = modelDir.notExists()
        modelDir.createDirectories()

        val databaseRelativePath = databaseFile.relativeTo(Paths.get(".").toAbsolutePath())
        runBlocking {
            val driverClass = "org.h2.Driver"
            val jdbcUrl = "jdbc:h2:./${databaseRelativePath.toString().removeSuffix(".mv.db")}"
            try {
                databaseManager.start(
                    driverClass = driverClass,
                    jdbcUrl = jdbcUrl,
                    schemaManager = schemaManager,
                )
            } catch (ex: Exception) {
                logger.warn("Failed to open database, backup and recreate", ex)
                try {
                    val backupFile = modelDir.resolve("$databaseName-backup.mv.db").toAbsolutePath()
                    databaseFile.moveTo(backupFile, overwrite = true)
                } catch (ex: IOException) {
                    logger.warn("Failed to backup database file", ex)
                    databaseFile.deleteIfExists()
                }
                try {
                    databaseManager.start(
                        driverClass = driverClass,
                        jdbcUrl = jdbcUrl,
                        schemaManager = schemaManager,
                    )
                } catch (ex: Exception) {
                    throw IllegalStateException("Failed to recreate database, give up and crash!", ex)
                }
            }
            runCatching {
                // On windows, set the database file as hidden.
                // On other platforms, because the file name is start with dot, the file is already hidden.
                Files.setAttribute(databaseFile, "dos:hidden", true)
            }
            try {
                startWatching()
            } catch (ex: Exception) {
                logger.warn("Failed to start watching model directory", ex)
            }
            scheduler.scheduleScan()
        }
        scope.launch(Dispatchers.IO) {
            if (extractDefaultModel) {
                try {
                    logger.info("Extracting default model: {}", defaultModelName)
                    javaClass.classLoader.getResourceAsStream(defaultModelName).use { input ->
                        modelDir.resolve(defaultModelName).outputStream().use { output ->
                            input.transferTo(output)
                        }
                    }
                    logger.info("Extracted default model")
                } catch (ex: Exception) {
                    logger.warn("Failed to extract default model", ex)
                }
            }
            if (extractDefaultModel || ArmorStandClient.instance.debug) {
                val defaultAnimationDir = ModelInstanceManager.defaultAnimationDir
                val extractDefaultAnimations = defaultAnimationDir.notExists()
                defaultAnimationDir.createDirectories()
                if (extractDefaultAnimations) {
                    logger.info("Extracting default animations")
                    val zipFileSystem = try {
                        val uri = this.javaClass.classLoader.getResource(defaultAnimationName)!!.toURI()
                        val zipPath = uri.toPath()
                        FileSystems.newFileSystem(zipPath)
                    } catch (ex: Exception) {
                        logger.warn("Failed to extract default animations", ex)
                        return@launch
                    }
                    zipFileSystem.getPath("/").forEachDirectoryEntry { entry ->
                        if (!entry.isRegularFile()) {
                            return@forEachDirectoryEntry
                        }
                        try {
                            entry.copyTo(defaultAnimationDir.resolve(entry.fileName.toString()), overwrite = false)
                        } catch (ex: Exception) {
                            logger.warn("Failed to extract animation file $entry", ex)
                        }
                    }
                    logger.info("Extracted default animations")
                }
            }
        }
    }

    override fun startWatching() {
        watcher.start {
            scheduleScan()
        }
    }

    override fun stopWatching() {
        watcher.stop()
    }

    override fun scheduleScan(immediately: Boolean) = scheduler.scheduleScan(immediately)

    override suspend fun getTotalModels(search: String?) =
        databaseManager.transaction { modelRepository.count(search) }

    override suspend fun getModelByPath(path: Path): ModelItem? =
        databaseManager.transaction { modelRepository.findByPath(path.normalize().toString()) }

    override suspend fun getModelByHash(hash: ModelHash): ModelItem? =
        databaseManager.transaction { modelRepository.findByHash(hash) }

    override suspend fun getAnimations(): List<AnimationItem> =
        databaseManager.transaction { animationRepository.findAll() }

    override suspend fun getModelThumbnail(modelItem: ModelItem): ModelThumbnail =
        databaseManager.transaction { thumbRepository.findEmbed(modelItem.hash) }

    override suspend fun getModels(
        offset: Int,
        length: Int,
        search: String?,
        order: ModelManager.Order,
        ascend: Boolean,
    ): List<ModelItem> = databaseManager.transaction {
        modelRepository.findRange(
            search = search,
            order = order,
            ascend = ascend,
            limit = length,
            offset = offset,
        )
    }

    override suspend fun setFavorite(path: Path, favorite: Boolean) = databaseManager.transaction {
        favoriteRepository.setFavorite(
            path = path.normalize().toString(),
            favorite = favorite,
            timeMillis = System.currentTimeMillis()
        )
        lastUpdateTime.value = Instant.now()
    }

    override suspend fun getFavoriteModels(): List<ModelItem> =
        databaseManager.transaction { favoriteRepository.findAll() }

    override suspend fun getTotalFavoriteModels(): Int =
        databaseManager.transaction { favoriteRepository.count() }

    override suspend fun getFavoriteModelIndex(path: Path): Int? =
        databaseManager.transaction { favoriteRepository.rankIndex(path.normalize().toString()) }

    override fun close() {
        scope.cancel()
        watcher.stop()
        scheduler.stop()
        runBlocking {
            databaseManager.stop()
        }
    }
}
