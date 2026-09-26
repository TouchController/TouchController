/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.config

import top.fifthlight.touchcontroller.common.config.data.*
import top.fifthlight.touchcontroller.common.config.platform.PlatformConfig
import top.fifthlight.touchcontroller.common.gal.itemlist.DefaultItemListProvider

data class GlobalConfig(
    val status: StatusConfig = StatusConfig(),
    val regular: RegularConfig = RegularConfig(),
    val control: ControlConfig = ControlConfig(),
    val platform: PlatformConfig = PlatformConfig(),
    val touchRing: TouchRingConfig = TouchRingConfig(),
    val debug: DebugConfig = DebugConfig(),
    val item: ItemConfig = ItemConfig.default(DefaultItemListProvider),
    val preset: PresetConfig = PresetConfig.BuiltIn(),
    val chat: ChatConfig = ChatConfig(),
) {
    companion object {
        val default by lazy {
            GlobalConfig()
        }
    }
}
