package top.fifthlight.armorstand.ui.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.minecraft.Util
import net.minecraft.client.Minecraft
import top.fifthlight.armorstand.config.ConfigHolder
import top.fifthlight.armorstand.manage.ModelManager
import top.fifthlight.armorstand.manage.ModelManagerHolder
import top.fifthlight.armorstand.state.ModelInstanceManager
import top.fifthlight.armorstand.ui.state.ConfigScreenState
import java.nio.file.Path

class ConfigViewModel(scope: CoroutineScope) : ViewModel(scope) {
    private val _uiState = MutableStateFlow(ConfigScreenState())
    val uiState = _uiState.asStateFlow()

    private fun findLargestMultiple(base: Int, maximum: Int) = maximum / base * base

    private data class SearchParam(
        val offset: Int,
        val pageSize: Int?,
        val searchString: String,
        val order: ModelManager.Order,
        val ascend: Boolean,
    ) {
        constructor(state: ConfigScreenState): this(
            offset = state.currentOffset,
            pageSize = state.pageSize,
            searchString = state.searchString,
            order = state.order,
            ascend = state.sortAscend,
        )
    }

    init {
        with(scope) {
            launch {
                val prevScanTime = ModelManagerHolder.instance.lastUpdateTime.value
                ModelManagerHolder.instance.scheduleScan(true)
                ModelManagerHolder.instance.lastUpdateTime.first { it?.equals(prevScanTime) ?: false }

                uiState.map(::SearchParam).distinctUntilChanged().collectLatest { param ->
                    val searchStr = param.searchString.takeIf { it.isNotBlank() }
                    ModelManagerHolder.instance.lastUpdateTime.collectLatest {
                        val totalItems = ModelManagerHolder.instance.getTotalModels(searchStr)
                        _uiState.getAndUpdate { state ->
                            state.copy(
                                totalItems = totalItems,
                                currentOffset = if (param.pageSize != null) {
                                    if (state.currentOffset + param.pageSize > totalItems) {
                                        // to last page
                                        findLargestMultiple(param.pageSize, totalItems - 1)
                                    } else {
                                        // to current page
                                        findLargestMultiple(param.pageSize, state.currentOffset)
                                    }
                                } else {
                                    0
                                },
                            )
                        }
                        param.pageSize?.let { pageSize ->
                            uiState.map { it.currentOffset }.distinctUntilChanged().collectLatest { offset ->
                                val items = ModelManagerHolder.instance.getModels(
                                    offset = offset,
                                    length = pageSize,
                                    search = searchStr,
                                    order = param.order,
                                    ascend = param.ascend,
                                )
                                _uiState.getAndUpdate { state ->
                                    state.copy(currentPageItems = items)
                                }
                            }
                        }
                    }
                }
            }
            launch {
                ConfigHolder.config.collect { config ->
                    _uiState.getAndUpdate {
                        it.copy(
                            currentModel = config.modelPath,
                            showOtherPlayerModel = config.showOtherPlayerModel,
                            sendModelData = config.sendModelData,
                            hidePlayerShadow = config.hidePlayerShadow,
                            hidePlayerArmor = config.hidePlayerArmor,
                            modelScale = config.modelScale,
                            thirdPersonDistanceScale = config.thirdPersonDistanceScale,
                        )
                    }
                }
            }
        }
    }

    fun tick() {
        val minecraft = Minecraft.getInstance()
        val player = minecraft.player ?: return
        val item = ModelInstanceManager.get(player.uuid, null)
        val modelItem = item as? ModelInstanceManager.ModelInstanceItem.Model ?: return
        val currentMetadata = uiState.value.currentMetadata
        if (currentMetadata != modelItem.metadata) {
            _uiState.getAndUpdate { state ->
                state.copy(currentMetadata = modelItem.metadata)
            }
        }
    }

    fun selectModel(path: Path?) {
        ConfigHolder.update {
            copy(model = path?.toString())
        }
    }

    fun setFavoriteModel(path: Path, favorite: Boolean) {
        scope.launch {
            ModelManagerHolder.instance.setFavorite(path, favorite)
        }
    }

    fun updatePageSize(pageSize: Int?) {
        pageSize?.let {
            check(pageSize > 0) { "Invalid page size: $pageSize" }
        }
        _uiState.getAndUpdate { state ->
            state.copy(pageSize = pageSize)
        }
    }

    fun updatePageIndex(delta: Int) {
        _uiState.getAndUpdate { state ->
            if (state.pageSize == null) {
                return@getAndUpdate state
            }
            val target = (state.currentOffset + delta * state.pageSize).coerceAtLeast(0)
            state.copy(
                currentOffset = if (target >= state.totalItems) {
                    findLargestMultiple(state.pageSize, state.totalItems - 1).coerceAtLeast(0)
                } else {
                    findLargestMultiple(
                        base = state.pageSize,
                        maximum = target,
                    ).coerceAtLeast(0)
                }
            )
        }
    }

    fun updateShowOtherPlayerModel(showOtherPlayerModel: Boolean) {
        ConfigHolder.update {
            copy(showOtherPlayerModel = showOtherPlayerModel)
        }
    }

    fun updateSendModelData(sendModelData: Boolean) {
        ConfigHolder.update {
            copy(sendModelData = sendModelData)
        }
    }

    fun updateHidePlayerShadow(hidePlayerShadow: Boolean) {
        ConfigHolder.update {
            copy(hidePlayerShadow = hidePlayerShadow)
        }
    }

    fun updateHidePlayerArmor(hidePlayerArmor: Boolean) {
        ConfigHolder.update {
            copy(hidePlayerArmor = hidePlayerArmor)
        }
    }

    fun updateModelScale(modelScale: Float) {
        ConfigHolder.update {
            copy(modelScale = modelScale)
        }
    }

    fun updateThirdPersonDistanceScale(thirdPersonDistanceScale: Float) {
        ConfigHolder.update {
            copy(thirdPersonDistanceScale = thirdPersonDistanceScale)
        }
    }

    fun updateSearchString(searchString: String) {
        _uiState.getAndUpdate { state ->
            state.copy(searchString = searchString)
        }
    }

    fun updateSearchParam(order: ModelManager.Order, ascend: Boolean) {
        _uiState.getAndUpdate { state ->
            state.copy(
                order = order,
                sortAscend = ascend,
            )
        }
    }

    fun refreshModels() {
        _uiState.getAndUpdate { state ->
            state.copy(
                currentPageItems = null
            )
        }
        scope.launch {
            ModelManagerHolder.instance.scheduleScan()
        }
    }

    fun openModelDir() {
        Util.getPlatform().openPath(ModelManagerHolder.modelDir)
    }
}
