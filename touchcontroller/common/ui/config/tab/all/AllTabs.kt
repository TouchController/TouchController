/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.config.tab.all

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import top.fifthlight.touchcontroller.common.ui.config.model.ConfigScreenModel
import top.fifthlight.touchcontroller.common.ui.config.tab.Tab
import top.fifthlight.touchcontroller.common.ui.config.tab.TabGroup
import top.fifthlight.touchcontroller.common.ui.config.tab.about.AboutTab
import top.fifthlight.touchcontroller.common.ui.config.tab.general.ControlTab
import top.fifthlight.touchcontroller.common.ui.config.tab.general.DebugTab
import top.fifthlight.touchcontroller.common.ui.config.tab.general.RegularTab
import top.fifthlight.touchcontroller.common.ui.config.tab.general.TouchRingTab
import top.fifthlight.touchcontroller.common.ui.config.tab.item.ItemTabs
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.CustomControlLayoutTab
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.preset.ManageControlPresetsTab
import top.fifthlight.touchcontroller.common.ui.config.tab.platform.platformTab
import top.fifthlight.touchcontroller.common.ui.config.tab.status.StatusTab

fun getAllTabs(configScreenModel: ConfigScreenModel): PersistentList<Tab> {
    val itemTabs = ItemTabs(configScreenModel)
    return listOfNotNull(
        StatusTab,
        AboutTab,
        platformTab,
        ManageControlPresetsTab,
        CustomControlLayoutTab,
        RegularTab,
        ControlTab,
        TouchRingTab,
        DebugTab,
        itemTabs.usableItemsTab,
        itemTabs.showCrosshairItemsTab,
        itemTabs.usingAimingItemsTab,
    ).toPersistentList()
}

val allTabGroups = persistentListOf(
    TabGroup.SystemGroup,
    TabGroup.LayoutGroup,
    TabGroup.GeneralGroup,
    TabGroup.ItemGroup,
)
