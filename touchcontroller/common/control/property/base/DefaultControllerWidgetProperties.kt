/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.control.property.base

import kotlinx.collections.immutable.persistentListOf
import top.fifthlight.combine.core.data.Text
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.common.control.ControllerWidget
import top.fifthlight.touchcontroller.common.control.ControllerWidgetDefaultProperties

@ActualImpl(ControllerWidgetDefaultProperties::class)
object DefaultControllerWidgetProperties : ControllerWidgetDefaultProperties {
    @JvmStatic
    @ActualConstructor
    fun of(): ControllerWidgetDefaultProperties = this

    override val properties = persistentListOf<ControllerWidget.Property<ControllerWidget, *>>(
        NameProperty(
            getValue = { it.name },
            setValue = { config, value -> config.cloneBase(name = value) },
            name = Text.translatable(Texts.WIDGET_GENERAL_PROPERTY_NAME),
        ),
        BooleanProperty(
            getValue = { it.lockMoving },
            setValue = { config, value -> config.cloneBase(lockMoving = value) },
            name = Text.translatable(Texts.WIDGET_GENERAL_PROPERTY_LOCK_MOVING),
        ),
        AnchorProperty(),
        BooleanProperty(
            getValue = { it.autoAlign },
            setValue = { config, value -> config.cloneBase(autoAlign = value) },
            name = Text.translatable(Texts.WIDGET_GENERAL_PROPERTY_ANCHOR_AUTO),
        ),
        FloatProperty(
            getValue = { it.opacity },
            setValue = { config, value -> config.cloneBase(opacity = value) },
            messageFormatter = { opacity ->
                Text.format(
                    Texts.WIDGET_GENERAL_PROPERTY_OPACITY,
                    kotlin.math.round(opacity * 100f).toInt().toString()
                )
            },
        ),
    )
}
