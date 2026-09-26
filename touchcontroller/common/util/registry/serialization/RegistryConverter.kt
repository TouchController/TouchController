/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.util.registry.serialization

import com.ubertob.kondor.json.JStringRepresentable
import top.fifthlight.touchcontroller.common.util.registry.Registry

fun <T : Any> registryConverter(
    registry: Registry<T>,
    unknown: (String) -> T,
): JStringRepresentable<T> = object : JStringRepresentable<T>() {
    override val cons: (String) -> T = { id -> registry[id] ?: unknown(id) }

    override val render: (T) -> String = { value ->
        registry.getId(value) ?: error("Value $value not registered")
    }
}
