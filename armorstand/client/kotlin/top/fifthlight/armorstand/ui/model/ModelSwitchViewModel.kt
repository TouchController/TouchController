package top.fifthlight.armorstand.ui.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import top.fifthlight.armorstand.config.ConfigHolder
import top.fifthlight.armorstand.manage.ModelManagerHolder
import top.fifthlight.armorstand.manage.model.ModelItem
import top.fifthlight.armorstand.ui.state.ModelSwitchScreenState

class ModelSwitchViewModel(scope: CoroutineScope) : ViewModel(scope) {
    companion object {
        const val TOTAL_ITEMS = 5
    }

    private val _uiState = MutableStateFlow(ModelSwitchScreenState())
    val uiState = _uiState.asStateFlow()

    init {
        scope.launch {
            val totalModelCount = ModelManagerHolder.instance.getTotalFavoriteModels()
            if (totalModelCount == 0) {
                _uiState.getAndUpdate {
                    it.copy(content = ModelSwitchScreenState.Content.Empty)
                }
                return@launch
            }
            val currentModelPath = ConfigHolder.config.value.modelPath
            val currentSelectedIndex: Int
            val extraItem: ModelItem?
            if (currentModelPath == null) {
                extraItem = null
                currentSelectedIndex = 0
            } else {
                val favoriteIndex = ModelManagerHolder.instance.getFavoriteModelIndex(currentModelPath)
                if (favoriteIndex == null) {
                    extraItem = ModelManagerHolder.instance.getModelByPath(currentModelPath)
                    currentSelectedIndex = if (extraItem == null) {
                        0
                    } else {
                        totalModelCount
                    }
                } else {
                    extraItem = null
                    currentSelectedIndex = favoriteIndex
                }
            }

            val models = buildList {
                addAll(ModelManagerHolder.instance.getFavoriteModels())
                extraItem?.let { add(it) }
            }
            _uiState.getAndUpdate {
                it.copy(
                    content = ModelSwitchScreenState.Content.Loaded(
                        currentIndex = currentSelectedIndex,
                        totalCount = totalModelCount,
                        extraItem = extraItem,
                        totalModels = models,
                    ),
                )
            }
        }
    }

    fun clientTick() {
        _uiState.getAndUpdate { state ->
            val currentTick = state.currentTick?.let { it + 1 } ?: 0
            val inactiveTick = currentTick - state.lastActiveTick
            state.copy(
                currentTick = currentTick,
                needToBeClosed = state.needToBeClosed || inactiveTick > 20,
            )
        }
    }

    fun switchModel(next: Boolean) {
        _uiState.updateAndGet { state ->
            state.copy(
                lastActiveTick = state.currentTick ?: 0,
                needToBeClosed = false,
                content = when (val content = state.content) {
                    is ModelSwitchScreenState.Content.Loaded -> {
                        val diff = if (next) 1 else -1
                        val newIndex =
                            (content.currentIndex + diff + content.totalModels.size) % content.totalModels.size
                        content.copy(currentIndex = newIndex)
                    }

                    else -> content
                }
            )
        }
    }
}
