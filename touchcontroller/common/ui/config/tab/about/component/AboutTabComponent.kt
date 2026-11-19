/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.config.tab.about.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.*
import com.arkivanov.decompose.value.Value
import top.fifthlight.touchcontroller.common.about.License

class AboutTabComponent(
    componentContext: ComponentContext,
) : ComponentContext by componentContext {
    sealed interface ChildConfig {
        data object About : ChildConfig
        data class LicensePage(val license: License) : ChildConfig
    }

    sealed interface ChildComponent {
        data class About(val component: AboutScreenComponent) : ChildComponent
        data class LicensePage(val license: License) : ChildComponent
    }

    private val navigation = StackNavigation<ChildConfig>()

    val childStack: Value<ChildStack<ChildConfig, ChildComponent>> = childStack(
        source = navigation,
        serializer = null,
        initialStack = { listOf(ChildConfig.About) },
        handleBackButton = true,
        childFactory = { config, context ->
            when (config) {
                ChildConfig.About -> ChildComponent.About(AboutScreenComponent(
                    componentContext = context,
                    onOpenLicenseScreen = {
                        navigation.pushNew(ChildConfig.LicensePage(it))
                    }
                ))
                is ChildConfig.LicensePage -> ChildComponent.LicensePage(config.license)
            }
        },
    )

    fun onBack() {
        navigation.pop()
    }
}
