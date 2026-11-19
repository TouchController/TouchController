/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.item.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.value.Value
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import top.fifthlight.combine.item.data.Item
import top.fifthlight.touchcontroller.common.gal.creativetab.CreativeTabsProvider
import top.fifthlight.touchcontroller.common.gal.creativetab.CreativeTabsProviderFactory
import top.fifthlight.touchcontroller.common.gal.player.PlayerHandle
import top.fifthlight.touchcontroller.common.gal.player.PlayerHandleFactory
import top.fifthlight.touchcontroller.common.gal.player.PlayerInventory

class ItemChooserComponent(
    componentContext: ComponentContext,
    private val player: PlayerHandle? = PlayerHandleFactory.current(),
    private val creativeTabsProviderFactory: CreativeTabsProvider = CreativeTabsProviderFactory.of(),
    private val onItemChosen: (Item) -> Unit,
) : ComponentContext by componentContext {
    sealed interface ChildConfig {
        data object DefaultList : ChildConfig
        data object CreativeTab : ChildConfig
    }

    sealed interface ChildComponent {
        data object DefaultList : ChildComponent
        data class CreativeTab(
            val tabs: PersistentList<CreativeTabsProvider.CreativeTab>,
            val playerInventory: PlayerInventory?,
        ) : ChildComponent
    }

    private val navigation = SlotNavigation<ChildConfig>()

    val slot: Value<ChildSlot<ChildConfig, ChildComponent>> = childSlot(
        source = navigation,
        serializer = null,
        handleBackButton = true,
        childFactory = { config, _ ->
            when (config) {
                ChildConfig.DefaultList -> ChildComponent.DefaultList
                ChildConfig.CreativeTab -> {
                    if (player != null) {
                        ChildComponent.CreativeTab(
                            tabs = creativeTabsProviderFactory.getCreativeTabs(player),
                            playerInventory = player.getInventory(),
                        )
                    } else {
                        ChildComponent.CreativeTab(
                            tabs = persistentListOf(),
                            playerInventory = null,
                        )
                    }
                }
            }
        },
    )

    fun openDefaultList() {
        navigation.activate(ChildConfig.DefaultList)
    }

    fun openCreativeTab() {
        navigation.activate(ChildConfig.CreativeTab)
    }

    fun chooseItem(item: Item) {
        onItemChosen.invoke(item)
    }
}
