/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.config.widget

import com.ubertob.kondor.json.JList
import com.ubertob.kondor.json.toJsonStream
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.slf4j.LoggerFactory
import top.fifthlight.touchcontroller.common.control.ControllerWidget
import top.fifthlight.touchcontroller.common.control.serialization.JControllerWidget
import top.fifthlight.touchcontroller.common.gal.config.ConfigDirectoryProvider
import top.fifthlight.touchcontroller.common.gal.config.ConfigDirectoryProviderFactory
import top.fifthlight.touchcontroller.common.serialization.jsonStyle
import kotlin.io.path.createDirectories
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

object WidgetPresetManager {
    private val logger = LoggerFactory.getLogger(WidgetPresetManager::class.java)
    private val configDirectoryProvider: ConfigDirectoryProvider = ConfigDirectoryProviderFactory.of()
    private val presetFile = configDirectoryProvider.configDirectory.resolve("widget.json")

    private val _presets = MutableStateFlow(persistentListOf<ControllerWidget>())
    val presets = _presets.asStateFlow()

    fun load() {
        try {
            logger.info("Reading TouchController widgets file")
            _presets.value = runCatching {
                presetFile.inputStream().use { JList(JControllerWidget).fromJson(it).orThrow() }
            }.getOrNull()?.toPersistentList() ?: persistentListOf()
        } catch (ex: Exception) {
            logger.warn("Failed to read presets", ex)
        }
    }

    fun save(presets: PersistentList<ControllerWidget>) {
        logger.info("Saving TouchController widgets")
        presetFile.parent.createDirectories()
        presetFile.outputStream().use { JList(JControllerWidget).toJsonStream(presets, it, jsonStyle) }
        _presets.value = presets
    }
}
