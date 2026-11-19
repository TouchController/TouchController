/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.importpreset.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import top.fifthlight.touchcontroller.common.config.preset.builtin.key.BuiltinPresetKey

class ImportPresetComponent(
    componentContext: ComponentContext,
    private val onPresetKeySelected: (BuiltinPresetKey) -> Unit,
    private val onConfigTopBar: (BuiltinPresetKey) -> Unit,
    private val onBack: () -> Unit,
) : ComponentContext by componentContext {
    private val _key = MutableValue(BuiltinPresetKey())
    val key: Value<BuiltinPresetKey> = _key

    fun updateKey(key: BuiltinPresetKey) {
        _key.value = key
    }

    fun configTopBar() {
        onConfigTopBar(key.value)
    }

    fun finish() {
        onPresetKeySelected(key.value)
        onBack.invoke()
    }

    fun onBack() {
        onBack.invoke()
    }
}
