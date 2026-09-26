/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.config.layout

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

@JvmInline
value class ControllerLayout(
    val layers: PersistentList<LayoutLayer> = persistentListOf(),
) : PersistentList<LayoutLayer> by layers

fun controllerLayoutOf(vararg layers: LayoutLayer?) = ControllerLayout(layers.mapNotNull { it }.toPersistentList())
