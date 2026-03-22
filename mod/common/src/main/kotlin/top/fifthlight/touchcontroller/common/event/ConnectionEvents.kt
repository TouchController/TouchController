package top.fifthlight.touchcontroller.common.event

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import top.fifthlight.combine.data.TextFactory
import top.fifthlight.touchcontroller.assets.Texts
import top.fifthlight.touchcontroller.common.gal.GameAction
import top.fifthlight.touchcontroller.common.platform.PlatformProvider
import top.fifthlight.touchcontroller.common.platform.proxy.ProxyPlatform

object ConnectionEvents : KoinComponent {
    private val platformProvider: PlatformProvider by inject()
    private val gameAction: GameAction by inject()
    private val textFactory: TextFactory by inject()

    fun onJoinedWorld() {
        val platform = platformProvider.platform
        if (platform == null) {
            gameAction.sendMessage(textFactory.of(Texts.WARNING_PROXY_NOT_CONNECTED))
            val systemName = System.getProperty("os.name")
            val isLinux = systemName.startsWith("Linux", ignoreCase = true)
            val isWindows = systemName.startsWith("Windows", ignoreCase = true)
            if ((isLinux && platformProvider.isAndroid) || systemName.contains("Android", ignoreCase = true)) {
                gameAction.sendMessage(textFactory.of(Texts.WARNING_PROXY_NOT_CONNECTED_ANDROID))
            } else if (platformProvider.isIOS) {
                gameAction.sendMessage(textFactory.of(Texts.WARNING_PROXY_NOT_CONNECTED_IOS))
            } else if (isWindows) {
                gameAction.sendMessage(textFactory.of(Texts.WARNING_PROXY_NOT_CONNECTED_WINDOWS))
            } else if (isLinux) {
                gameAction.sendMessage(textFactory.of(Texts.WARNING_PROXY_NOT_CONNECTED_LINUX))
            } else {
                gameAction.sendMessage(
                    textFactory.format(
                        Texts.WARNING_PROXY_NOT_CONNECTED_OS_NOT_SUPPORTED,
                        systemName
                    )
                )
            }
        } else if (platform is ProxyPlatform) {
            gameAction.sendMessage(textFactory.of(Texts.WARNING_LEGACY_UDP_PROXY_USED))
        }
    }
}