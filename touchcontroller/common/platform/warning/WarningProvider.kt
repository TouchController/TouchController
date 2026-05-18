/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.platform.warning

import top.fifthlight.combine.core.data.Text
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.common.platform.provider.PlatformProvider

object WarningProvider {
    val warning by lazy {
        val systemName = PlatformProvider.systemName
        val isLinux = systemName.startsWith("Linux", ignoreCase = true)
        val isWindows = systemName.startsWith("Windows", ignoreCase = true)
        if ((isLinux && PlatformProvider.isAndroid) || systemName.contains("Android", ignoreCase = true)) {
            Text.translatable(Texts.WARNING_PROXY_NOT_CONNECTED_ANDROID)
        } else if (isWindows) {
            Text.translatable(Texts.WARNING_PROXY_NOT_CONNECTED_WINDOWS)
        } else if (isLinux) {
            Text.translatable(Texts.WARNING_PROXY_NOT_CONNECTED_LINUX)
        } else {
            Text.format(Texts.WARNING_PROXY_NOT_CONNECTED_OS_NOT_SUPPORTED, PlatformProvider.displayName)
        }
    }
}
