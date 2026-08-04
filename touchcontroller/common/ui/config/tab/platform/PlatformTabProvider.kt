/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.config.tab.platform

import top.fifthlight.touchcontroller.common.platform.provider.PlatformProvider
import top.fifthlight.touchcontroller.common.platform.sdl.SdlPlatform
import top.fifthlight.touchcontroller.common.ui.config.tab.Tab

val platformTab: Tab?
    get() = when {
        PlatformProvider.platform is SdlPlatform -> SdlConfigTab
        else -> null
    }
