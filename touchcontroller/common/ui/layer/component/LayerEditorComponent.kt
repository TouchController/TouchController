/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.layer.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.children.ChildNavState.Status
import com.arkivanov.decompose.router.pages.*
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import top.fifthlight.combine.core.data.Identifier
import top.fifthlight.touchcontroller.common.config.condition.HoldingItemLayerConditionKey
import top.fifthlight.touchcontroller.common.config.condition.LayerConditions
import top.fifthlight.touchcontroller.common.config.layout.LayoutLayer
import top.fifthlight.touchcontroller.common.config.preset.LayoutPreset
import top.fifthlight.touchcontroller.common.config.preset.info.LayerCustomConditions
import top.fifthlight.touchcontroller.common.ui.item.component.ItemChooserComponent
import top.fifthlight.touchcontroller.common.ui.layer.state.LayerEditorScreenState
import top.fifthlight.touchcontroller.common.ui.layer.tab.LayerConditionTabComponent
import top.fifthlight.touchcontroller.common.ui.layer.tab.LayerConditionTabConfig
import top.fifthlight.touchcontroller.common.ui.layer.tab.custom.component.CustomTabComponent
import top.fifthlight.touchcontroller.common.util.name.TranslatableName
import java.util.*

class LayerEditorComponent(
    componentContext: ComponentContext,
    val screenName: Identifier,
    val preset: Value<Optional<LayoutPreset>>,
    private val initialValue: LayoutLayer,
    private val onCustomConditionsChanged: (LayerCustomConditions) -> Unit,
    private val onValueChanged: (LayoutLayer) -> Unit,
    private val onBack: () -> Unit,
) : ComponentContext by componentContext {
    private val _uiState = MutableValue(LayerEditorScreenState(initialValue))
    val uiState: Value<LayerEditorScreenState> = _uiState

    private val navigation = PagesNavigation<LayerConditionTabConfig>()

    val childPages: Value<ChildPages<LayerConditionTabConfig, LayerConditionTabComponent>> = childPages(
        source = navigation,
        serializer = null,
        initialPages = {
            Pages(
                items = LayerConditionTabConfig.all,
                selectedIndex = 0,
            )
        },
        pageStatus = { index, pages ->
            when (index) {
                pages.selectedIndex -> Status.RESUMED
                else -> Status.CREATED
            }
        },
        handleBackButton = false,
        childFactory = { config, context ->
            val onConditionAdded: (LayerConditions.Key) -> Unit = { key ->
                addCondition(
                    LayerConditions.Item(
                        key = key,
                        value = LayerConditions.Value.WANT,
                    )
                )
            }
            when (config) {
                LayerConditionTabConfig.BuiltIn -> LayerConditionTabComponent.BuiltIn(
                    onConditionAdded = onConditionAdded,
                )

                LayerConditionTabConfig.HoldingItem -> LayerConditionTabComponent.HoldingItem(
                    itemChooser = ItemChooserComponent(
                        componentContext = context,
                    ) { item ->
                        onConditionAdded(HoldingItemLayerConditionKey(item))
                    },
                )

                LayerConditionTabConfig.RidingEntity -> LayerConditionTabComponent.RidingEntity(
                    onConditionAdded = onConditionAdded,
                )

                LayerConditionTabConfig.SelectingEntity -> LayerConditionTabComponent.SelectingEntity(
                    onConditionAdded = onConditionAdded,
                )

                LayerConditionTabConfig.Custom -> LayerConditionTabComponent.Custom(
                    CustomTabComponent(
                        componentContext = context,
                        preset = preset,
                        onCustomConditionsChanged = onCustomConditionsChanged,
                        onConditionAdded = onConditionAdded,
                    )
                )
            }
        },
    )

    fun selectTab(tab: LayerConditionTabConfig) {
        val index = childPages.value.items.indexOfFirst { it.configuration == tab }
        if (index != -1) {
            navigation.select(index)
        }
    }

    fun editName(newName: String) {
        _uiState.update {
            it.copy(name = TranslatableName.Literal(newName))
        }
    }

    fun applyChanges() {
        onValueChanged(uiState.value.edit(initialValue))
        onBack.invoke()
    }

    fun editCondition(index: Int, editor: LayerConditions.Item.() -> LayerConditions.Item) {
        _uiState.update {
            it.copy(
                conditions = LayerConditions(
                    conditions = it.conditions.conditions.set(index, editor(it.conditions.conditions[index])),
                ),
            )
        }
    }

    fun removeCondition(index: Int) {
        _uiState.update {
            it.copy(
                conditions = LayerConditions(
                    conditions = it.conditions.conditions.removeAt(index),
                ),
            )
        }
    }

    fun addCondition(condition: LayerConditions.Item) {
        _uiState.update {
            it.copy(
                conditions = LayerConditions(
                    conditions = it.conditions.conditions.add(condition),
                ),
            )
        }
    }

    fun onBack() {
        onBack.invoke()
    }
}
