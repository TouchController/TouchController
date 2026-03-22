package top.fifthlight.touchcontroller.common.config.platform

import kotlinx.coroutines.flow.StateFlow
import top.fifthlight.mergetools.api.ExpectFactory

interface PlatformConfigProvider {
    val platformConfig: StateFlow<PlatformConfig>

    @ExpectFactory
    interface Factory {
        fun of(): PlatformConfigProvider
    }

    companion object : PlatformConfigProvider by PlatformConfigProviderFactory.of()
}
