package top.fifthlight.touchcontroller.common.ui.config.tab.about.model

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import top.fifthlight.touchcontroller.common.about.AboutInfo
import top.fifthlight.touchcontroller.common.about.AboutInfoProvider
import top.fifthlight.touchcontroller.common.ui.model.TouchControllerScreenModel

class AboutScreenModel : TouchControllerScreenModel() {
    private val logger = LoggerFactory.getLogger(AboutScreenModel::class.java)
    private val _aboutInfo = MutableStateFlow<AboutInfo?>(null)
    val aboutInfo = _aboutInfo.asStateFlow()

    init {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    AboutInfoProvider.aboutInfo
                } catch (ex: Exception) {
                    logger.warn("Failed to read about information", ex)
                    null
                }
            }?.let { aboutInfo ->
                _aboutInfo.value = aboutInfo
            }
        }
    }
}
