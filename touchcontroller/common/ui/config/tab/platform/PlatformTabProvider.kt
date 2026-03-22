package top.fifthlight.touchcontroller.common.ui.config.tab.platform

import top.fifthlight.touchcontroller.common.platform.provider.PlatformProvider
import top.fifthlight.touchcontroller.common.platform.sdl.BlazeSDLPlatform
import top.fifthlight.touchcontroller.common.ui.config.tab.Tab

val platformTab: Tab?
    get() = when (PlatformProvider.platform) {
        is BlazeSDLPlatform -> BlazeSDLConfigTab
        else -> null
    }
