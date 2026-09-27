/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.control

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.PersistentList
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.data.IntOffset
import top.fifthlight.data.IntSize
import top.fifthlight.touchcontroller.common.config.preset.info.PresetControlInfo
import top.fifthlight.touchcontroller.common.control.name.ControllerWidgetName
import top.fifthlight.touchcontroller.common.layout.Context
import top.fifthlight.touchcontroller.common.layout.align.Align
import top.fifthlight.touchcontroller.common.util.uuid.fastRandomUuid
import kotlin.uuid.Uuid

@Immutable
abstract class ControllerWidget {
    abstract val id: Uuid
    abstract val name: ControllerWidgetName
    abstract val align: Align
    abstract val autoAlign: Boolean
    abstract val offset: IntOffset
    abstract val opacity: Float
    abstract val lockMoving: Boolean

    abstract class Property<Config : ControllerWidget, Value>(
        val getValue: (Config) -> Value,
        val setValue: (Config, Value) -> Config,
    ) {
        data class ConfigContext(
            val presetControlInfo: PresetControlInfo?,
            val editAreaSize: IntSize?,
        )

        @Composable
        abstract fun controller(
            modifier: Modifier,
            config: ControllerWidget,
            context: ConfigContext,
            onConfigChanged: (ControllerWidget) -> Unit,
        )
    }

    companion object {
        val properties = ControllerWidgetDefaultPropertiesFactory.of().properties
    }

    open val properties: PersistentList<Property<ControllerWidget, *>> = Companion.properties

    abstract fun size(): IntSize

    abstract fun layout(context: Context)

    abstract fun cloneBase(
        id: Uuid = this.id,
        name: ControllerWidgetName = this.name,
        align: Align = this.align,
        autoAlign: Boolean = this.autoAlign,
        offset: IntOffset = this.offset,
        opacity: Float = this.opacity,
        lockMoving: Boolean = this.lockMoving,
    ): ControllerWidget

    open fun newId() = cloneBase(
        id = fastRandomUuid(),
    )
}
