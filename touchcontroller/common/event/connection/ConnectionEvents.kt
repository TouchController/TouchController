/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.event.connection

import top.fifthlight.combine.core.data.Text
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.common.config.data.StatusConfig
import top.fifthlight.touchcontroller.common.config.holder.GlobalConfigHolder
import top.fifthlight.touchcontroller.common.gal.action.GameAction
import top.fifthlight.touchcontroller.common.platform.provider.PlatformProvider
import top.fifthlight.touchcontroller.common.platform.proxy.ProxyPlatform
import top.fifthlight.touchcontroller.common.platform.warning.WarningProvider

object ConnectionEvents {
    fun onJoinedWorld() {
        val config = GlobalConfigHolder.config.value
        if (config.status.status == StatusConfig.Status.DISABLED) {
            return
        }
        val platform = PlatformProvider.platform
        if (platform == null) {
            GameAction.sendMessage(Text.translatable(Texts.WARNING_PROXY_NOT_CONNECTED))
            GameAction.sendMessage(WarningProvider.warning)
        } else if (platform is ProxyPlatform) {
            GameAction.sendMessage(Text.translatable(Texts.WARNING_LEGACY_UDP_PROXY_USED))
        }
    }
}
