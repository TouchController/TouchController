/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.config.tab.about

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import top.fifthlight.combine.core.data.Identifier
import top.fifthlight.combine.core.data.Text
import top.fifthlight.combine.core.layout.Alignment
import top.fifthlight.combine.core.layout.Arrangement
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.drawing.border
import top.fifthlight.combine.core.modifier.placement.fillMaxSize
import top.fifthlight.combine.core.modifier.placement.fillMaxWidth
import top.fifthlight.combine.core.modifier.placement.height
import top.fifthlight.combine.core.modifier.placement.padding
import top.fifthlight.combine.core.modifier.scroll.verticalScroll
import top.fifthlight.combine.core.paint.Colors
import top.fifthlight.combine.core.paint.TextureFactory
import top.fifthlight.combine.core.widget.layout.Column
import top.fifthlight.combine.core.widget.layout.Row
import top.fifthlight.combine.theme.blackstone.widget.LocalNavigationButtonTheme
import top.fifthlight.combine.widget.Icon
import top.fifthlight.combine.widget.Link
import top.fifthlight.combine.widget.Text
import top.fifthlight.data.IntPadding
import top.fifthlight.data.IntSize
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.buildinfo.BuildInfo
import top.fifthlight.touchcontroller.common.about.License
import top.fifthlight.touchcontroller.common.ui.config.tab.about.component.AboutScreenComponent
import top.fifthlight.touchcontroller.common.ui.config.tab.about.component.AboutTabComponent
import top.fifthlight.touchcontroller.common.ui.license.LicenseScreen
import top.fifthlight.touchcontroller.common.ui.state.subscribeAsState
import top.fifthlight.touchcontroller.common.ui.theme.LocalTouchControllerTheme
import kotlin.jvm.optionals.getOrNull

@Composable
fun AboutScreen(component: AboutScreenComponent) {
    val aboutInfoOptional by component.aboutInfo.subscribeAsState()
    val aboutInfo = aboutInfoOptional.getOrNull()
    Column(
        modifier = Modifier
            .padding(8)
            .verticalScroll(background = LocalTouchControllerTheme.current.background)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8),
    ) {
        val iconSize = 32
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(iconSize),
            horizontalArrangement = Arrangement.spacedBy(8),
        ) {
            Icon(
                drawable = remember {
                    TextureFactory.create(
                        Identifier.of("touchcontroller", "textures/icon.png"),
                        iconSize,
                        iconSize,
                        IntPadding.ZERO
                    )
                },
                size = IntSize(iconSize),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(iconSize),
                verticalArrangement = Arrangement.SpaceAround,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4)
                ) {
                    Text(text = Text.literal(BuildInfo.MOD_NAME).bold())
                    Text(
                        modifier = Modifier.weight(1f),
                        text = BuildInfo.MOD_VERSION,
                    )
                }
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = BuildInfo.MOD_DESCRIPTION,
                )
            }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Row {
                Text(text = Text.translatable(Texts.SCREEN_CONFIG_ABOUT_AUTHORS_TITLE))
                Text(
                    modifier = Modifier.weight(1f),
                    text = BuildInfo.MOD_AUTHORS.joinToString(", "),
                )
            }
            Row {
                Text(text = Text.translatable(Texts.SCREEN_CONFIG_ABOUT_CONTRIBUTORS_TITLE))
                Text(
                    modifier = Modifier.weight(1f),
                    text = BuildInfo.MOD_CONTRIBUTORS.joinToString(", "),
                )
            }
            Row {
                Text(text = Text.translatable(Texts.SCREEN_CONFIG_ABOUT_LICENSE_TITLE))
                aboutInfo?.modLicense?.let { modLicense ->
                    val license = License(
                        name = BuildInfo.MOD_LICENSE,
                        content = modLicense,
                    )
                    Link(
                        modifier = Modifier.weight(1f),
                        text = BuildInfo.MOD_LICENSE,
                        onClick = {
                            component.openLicenseScreen(license)
                        },
                    )
                } ?: run {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = BuildInfo.MOD_LICENSE,
                    )
                }
            }
        }

        aboutInfo?.let { aboutInfo ->
            val libraries = aboutInfo.libraries ?: return@let
            Column(verticalArrangement = Arrangement.spacedBy(8)) {
                for (library in libraries.libraries) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(LocalNavigationButtonTheme.current.drawableSet.off.normal),
                        verticalArrangement = Arrangement.spacedBy(4),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(library.name)
                            library.artifactVersion?.let { version ->
                                Text(version, color = Colors.ALTERNATE_WHITE)
                            } ?: run {
                                Text(
                                    text = Text.translatable(Texts.SCREEN_CONFIG_ABOUT_UNKNOWN_VERSION),
                                    color = Colors.ALTERNATE_WHITE
                                )
                            }
                        }
                        Text(library.uniqueId, color = Colors.ALTERNATE_WHITE)
                        Row(horizontalArrangement = Arrangement.spacedBy(4)) {
                            for (developer in library.developers) {
                                developer.name?.let { name ->
                                    Text(
                                        text = name,
                                        color = Colors.ALTERNATE_WHITE
                                    )
                                }
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4, Alignment.Right)) {
                            for (license in library.licenses) {
                                val license = aboutInfo.libraries?.licenses[license]
                                license?.content?.let {
                                    Link(
                                        text = license.name,
                                        onClick = {
                                            component.openLicenseScreen(license)
                                        },
                                    )
                                } ?: license?.name?.let { name ->
                                    Text(name)
                                }
                            }
                        }
                    }
                }
            }
        } ?: run {
            Text(Text.translatable(Texts.SCREEN_CONFIG_ABOUT_LOADING))
        }
    }
}

@Composable
fun AboutTab(component: AboutTabComponent) {
    val stack by component.childStack.subscribeAsState()
    when (val instance = stack.active.instance) {
        is AboutTabComponent.ChildComponent.About -> AboutScreen(instance.component)
        is AboutTabComponent.ChildComponent.LicensePage -> LicenseScreen(
            instance.license,
            onBack = {
                component.onBack()
            },
        )
    }
}
