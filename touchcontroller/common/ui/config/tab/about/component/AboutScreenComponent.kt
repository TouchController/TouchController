/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.config.tab.about.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import top.fifthlight.combine.core.util.dispatcher.Game
import top.fifthlight.touchcontroller.common.about.AboutInfo
import top.fifthlight.touchcontroller.common.about.AboutInfoProvider
import top.fifthlight.touchcontroller.common.about.License
import java.util.*

class AboutScreenComponent(
    componentContext: ComponentContext,
    val onOpenLicenseScreen: (License) -> Unit,
) : ComponentContext by componentContext {
    private val logger = LoggerFactory.getLogger(AboutTabComponent::class.java)

    private val _aboutInfo = MutableValue(Optional.empty<AboutInfo>())
    val aboutInfo: Value<Optional<AboutInfo>> = _aboutInfo

    init {
        coroutineScope(Dispatchers.Game).launch {
            withContext(Dispatchers.IO) {
                try {
                    AboutInfoProvider.aboutInfo
                } catch (ex: Exception) {
                    logger.warn("Failed to read about information", ex)
                    null
                }
            }?.let { aboutInfo ->
                _aboutInfo.value = Optional.of(aboutInfo)
            }
        }
    }

    fun openLicenseScreen(license: License) {
        onOpenLicenseScreen(license)
    }
}
