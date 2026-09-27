/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.control

import kotlinx.collections.immutable.PersistentList
import top.fifthlight.mergetools.api.ExpectFactory

interface ControllerWidgetDefaultProperties {
    val properties: PersistentList<ControllerWidget.Property<ControllerWidget, *>>

    @ExpectFactory
    interface Factory {
        fun of(): ControllerWidgetDefaultProperties
    }
}
