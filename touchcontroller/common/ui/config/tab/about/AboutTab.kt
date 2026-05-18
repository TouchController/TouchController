/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.config.tab.about

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
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
import top.fifthlight.combine.widget.ui.Icon
import top.fifthlight.combine.widget.ui.Link
import top.fifthlight.combine.widget.ui.Text
import top.fifthlight.data.IntPadding
import top.fifthlight.data.IntSize
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.buildinfo.BuildInfo
import top.fifthlight.touchcontroller.common.about.License
import top.fifthlight.touchcontroller.common.ui.config.tab.Tab
import top.fifthlight.touchcontroller.common.ui.config.tab.TabGroup
import top.fifthlight.touchcontroller.common.ui.config.tab.TabOptions
import top.fifthlight.touchcontroller.common.ui.config.tab.about.model.AboutScreenModel
import top.fifthlight.touchcontroller.common.ui.screen.LicenseScreen
import top.fifthlight.touchcontroller.common.ui.theme.LocalTouchControllerTheme

object AboutTab : Tab() {
    override val options = TabOptions(
        titleId = Texts.SCREEN_CONFIG_ABOUT_TITLE,
        group = TabGroup.SystemGroup,
        index = 0,
    )

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val screenModel = rememberScreenModel { AboutScreenModel() }
        val aboutInfo by screenModel.aboutInfo.collectAsState()
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
                            "touchcontroller",
                            "textures/icon.png",
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
                                navigator?.push(LicenseScreen(license))
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
                val libraries = aboutInfo.libraries
                if (libraries == null) {
                    return@let
                }
                Column(verticalArrangement = Arrangement.spacedBy(8)) {
                    for (library in libraries.libraries) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(LocalTouchControllerTheme.current.tabButtonDrawablesUnchecked.normal),
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
                                    license?.content?.let { content ->
                                        Link(
                                            text = license.name,
                                            onClick = { navigator?.push(LicenseScreen(license)) },
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
}
