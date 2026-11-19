/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.config.tab.item.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.*
import com.arkivanov.decompose.value.Value
import top.fifthlight.touchcontroller.common.config.item.ItemList
import top.fifthlight.touchcontroller.common.ui.component.component.ComponentListComponent
import top.fifthlight.touchcontroller.common.ui.config.tab.item.component.ItemTabComponent.ChildComponent.*
import top.fifthlight.touchcontroller.common.ui.item.component.ItemListComponent

class ItemTabComponent(
    componentContext: ComponentContext,
    val itemList: Value<ItemList>,
    val onItemListChanged: (ItemList) -> Unit,
    val defaultItemListGetter: () -> ItemList,
) : ComponentContext by componentContext {
    sealed interface ChildConfig {
        data object Tab : ChildConfig
        data object Whitelist : ChildConfig
        data object Blacklist : ChildConfig
        data object Components : ChildConfig
    }

    sealed interface ChildComponent {
        data class Tab(val component: ItemScreenComponent) : ChildComponent
        data class Whitelist(val component: ItemListComponent) : ChildComponent
        data class Blacklist(val component: ItemListComponent) : ChildComponent
        data class Components(val component: ComponentListComponent) : ChildComponent
    }

    private val navigation = StackNavigation<ChildConfig>()

    val childStack: Value<ChildStack<ChildConfig, ChildComponent>> = childStack(
        source = navigation,
        serializer = null,
        initialStack = { listOf(ChildConfig.Tab) },
        handleBackButton = true,
        childFactory = { config, context ->
            when (config) {
                ChildConfig.Tab -> Tab(
                    ItemScreenComponent(
                        componentContext = context,
                        itemList = itemList,
                        onItemListChanged = onItemListChanged,
                        onWhitelistOpened = ::openWhitelistEditor,
                        onBlacklistOpened = ::openBlacklistEditor,
                        onComponentListOpened = ::openComponentListEditor,
                    )
                )

                ChildConfig.Whitelist -> Whitelist(
                    ItemListComponent(
                        componentContext = context,
                        initialValue = itemList.value.whitelist,
                        onValueChanged = {
                            onItemListChanged.invoke(itemList.value.copy(whitelist = it))
                        },
                        onBack = ::onBack,
                    )
                )

                ChildConfig.Blacklist -> Blacklist(
                    ItemListComponent(
                        componentContext = context,
                        initialValue = itemList.value.blacklist,
                        onValueChanged = {
                            onItemListChanged.invoke(itemList.value.copy(blacklist = it))
                        },
                        onBack = ::onBack,
                    )
                )

                ChildConfig.Components -> Components(
                    ComponentListComponent(
                        componentContext = context,
                        initialValue = itemList.value.components,
                        onValueChanged = {
                            onItemListChanged.invoke(itemList.value.copy(components = it))
                        },
                        onBack = ::onBack,
                    )
                )
            }
        },
    )

    fun openWhitelistEditor() {
        navigation.pushNew(ChildConfig.Whitelist)
    }

    fun openBlacklistEditor() {
        navigation.pushNew(ChildConfig.Blacklist)
    }

    fun openComponentListEditor() {
        navigation.pushNew(ChildConfig.Components)
    }

    fun resetConfig() {
        onItemListChanged(defaultItemListGetter())
    }

    fun onBack() {
        navigation.pop()
    }
}
