/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.presets.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.*
import com.arkivanov.decompose.router.stack.*
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import org.slf4j.LoggerFactory
import top.fifthlight.touchcontroller.common.config.preset.LayoutPreset
import top.fifthlight.touchcontroller.common.config.preset.PresetsContainer
import top.fifthlight.touchcontroller.common.config.preset.builtin.key.BuiltinPresetKey
import top.fifthlight.touchcontroller.common.config.preset.info.PresetControlInfo
import top.fifthlight.touchcontroller.common.config.preset.manager.PresetManager
import top.fifthlight.touchcontroller.common.ui.importpreset.component.ImportPresetComponent
import top.fifthlight.touchcontroller.common.util.name.TranslatableName
import java.util.*
import kotlin.uuid.Uuid

class PresetsTabComponent(
    componentContext: ComponentContext,
    val presets: Value<PresetsContainer>,
    val selectedPreset: Value<Pair<Uuid?, LayoutPreset?>>,
    private val onPresetSelected: (Uuid) -> Unit,
    private val onPresetCreated: (preset: LayoutPreset) -> Unit,
    private val onPresetEdited: (uuid: Uuid, name: TranslatableName, controlInfo: PresetControlInfo) -> Unit,
    private val onPresetDeleted: (uuid: Uuid) -> Unit,
    private val onPresetOrderChanged: (ImmutableList<Uuid>) -> Unit,
) : ComponentContext by componentContext {
    private companion object {
        private val logger = LoggerFactory.getLogger(PresetsTabComponent::class.java)
    }

    sealed interface StackConfig {
        data object Presets : StackConfig

        data object Import : StackConfig

        data class ImportTopBar(val key: BuiltinPresetKey) : StackConfig
    }

    sealed interface StackComponent {
        data object Presets : StackComponent

        data class Import(val component: ImportPresetComponent) : StackComponent

        data class ImportTopBar(
            val key: Value<BuiltinPresetKey>,
            val onKeyChanged: (BuiltinPresetKey.TopBarConfig) -> Unit,
            val onBack: () -> Unit,
        ) : StackComponent
    }

    sealed interface ChildConfig {
        data object CreateChoose : ChildConfig

        data object CreateEmpty : ChildConfig

        data class Edit(val uuid: Uuid) : ChildConfig

        data class Delete(val uuid: Uuid) : ChildConfig

        data class Path(val uuid: Uuid) : ChildConfig
    }

    data class PresetData(
        val name: TranslatableName = LayoutPreset.DEFAULT_PRESET_NAME,
        val controlInfo: PresetControlInfo = PresetControlInfo(),
    )

    sealed interface ChildComponent {
        data object CreateChoose : ChildComponent

        class CreateEmpty(
            name: TranslatableName = LayoutPreset.DEFAULT_PRESET_NAME,
            controlInfo: PresetControlInfo = PresetControlInfo(),
            val onCreated: (name: TranslatableName, controlInfo: PresetControlInfo) -> Unit,
        ) : ChildComponent {
            val uiState = MutableValue(PresetData(name, controlInfo))

            fun update(transform: PresetData.() -> PresetData) {
                uiState.value = uiState.value.transform()
            }
        }

        class Edit(
            name: TranslatableName,
            controlInfo: PresetControlInfo,
            val onEdited: (name: TranslatableName, controlInfo: PresetControlInfo) -> Unit,
        ) : ChildComponent {
            val uiState = MutableValue(PresetData(name, controlInfo))

            fun update(transform: PresetData.() -> PresetData) {
                uiState.value = uiState.value.transform()
            }
        }

        class Delete(
            val name: String,
            val onDeleted: () -> Unit,
        ) : ChildComponent

        data class Path(
            val path: String? = null,
        ) : ChildComponent
    }

    private val stackNavigation = StackNavigation<StackConfig>()

    val childStack: Value<ChildStack<StackConfig, StackComponent>> = childStack(
        source = stackNavigation,
        serializer = null,
        initialStack = { listOf(StackConfig.Presets) },
        handleBackButton = true,
        childFactory = { config, context ->
            when (config) {
                StackConfig.Presets -> StackComponent.Presets

                StackConfig.Import -> StackComponent.Import(
                    ImportPresetComponent(
                        componentContext = context,
                        onPresetKeySelected = { key ->
                            onPresetCreated(key.preset)
                        },
                        onConfigTopBar = { key ->
                            stackNavigation.pushNew(StackConfig.ImportTopBar(key))
                        },
                        onBack = { stackNavigation.pop() },
                    )
                )

                is StackConfig.ImportTopBar -> {
                    val importScreen = childStack.value.items
                        .mapNotNull { it.instance as? StackComponent.Import }
                        .last()
                        .component
                    StackComponent.ImportTopBar(
                        key = importScreen.key,
                        onKeyChanged = { importScreen.updateKey(importScreen.key.value.copy(topBar = it)) },
                        onBack = { stackNavigation.pop() },
                    )
                }
            }
        },
    )

    private val navigation = SlotNavigation<ChildConfig>()

    val dialog: Value<ChildSlot<ChildConfig, ChildComponent>> = childSlot(
        source = navigation,
        serializer = null,
        handleBackButton = true,
        initialConfiguration = { null },
        childFactory = { config, _ ->
            when (config) {
                ChildConfig.CreateChoose -> ChildComponent.CreateChoose

                ChildConfig.CreateEmpty -> ChildComponent.CreateEmpty(onCreated = { name, controlInfo ->
                    onPresetCreated(LayoutPreset(name, controlInfo))
                })

                is ChildConfig.Edit -> presets.value[config.uuid]?.let { preset ->
                    ChildComponent.Edit(
                        name = preset.name,
                        controlInfo = preset.controlInfo,
                        onEdited = { name, controlInfo ->
                            onPresetEdited(config.uuid, name, controlInfo)
                        },
                    )
                } ?: ChildComponent.Edit(
                    name = TranslatableName.Literal(""),
                    controlInfo = PresetControlInfo(),
                    onEdited = { name, controlInfo ->
                        onPresetCreated(LayoutPreset(name, controlInfo))
                    },
                )

                is ChildConfig.Delete -> presets.value[config.uuid]?.let { preset ->
                    ChildComponent.Delete(
                        name = preset.name.asString(),
                        onDeleted = { onPresetDeleted(config.uuid) },
                    )
                } ?: ChildComponent.Delete(name = "", onDeleted = {})

                is ChildConfig.Path -> try {
                    val path = PresetManager.presetDir.resolve("${config.uuid}.json").toAbsolutePath()
                    ChildComponent.Path(path.toString())
                } catch (ex: Exception) {
                    logger.error("Failed to get preset path", ex)
                    ChildComponent.Path(null)
                }
            }
        },
    )

    fun openCreatePresetChooseDialog() {
        navigation.activate(ChildConfig.CreateChoose)
    }

    fun openCreateEmptyPresetDialog() {
        navigation.activate(ChildConfig.CreateEmpty)
    }

    fun openEditPresetDialog(uuid: Uuid) {
        navigation.activate(ChildConfig.Edit(uuid))
    }

    fun openDeletePresetBox(uuid: Uuid) {
        navigation.activate(ChildConfig.Delete(uuid))
    }

    fun openPresetPathDialog(uuid: Uuid) {
        navigation.activate(ChildConfig.Path(uuid))
    }

    fun closeDialog() {
        navigation.dismiss()
    }

    fun openImportPreset() {
        navigation.dismiss()
        stackNavigation.pushNew(StackConfig.Import)
    }

    fun selectPreset(uuid: Uuid) {
        onPresetSelected(uuid)
    }

    fun copyPreset(preset: LayoutPreset) {
        onPresetCreated(preset)
    }

    fun movePreset(uuid: Uuid, offset: Int) {
        val entries = presets.value.orderedEntries
        val index = entries.indexOfFirst { (id, _) -> id == uuid }
        if (index == -1) {
            return
        }
        val newIndex = (index + offset).coerceIn(entries.indices)
        val newOrder = entries.removeAt(index).add(newIndex, entries[index]).map { it.first }
        onPresetOrderChanged(newOrder.toPersistentList())
    }
}
