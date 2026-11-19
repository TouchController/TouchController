/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.config.component

import com.arkivanov.decompose.Cancellation
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.GenericComponentContext
import com.arkivanov.decompose.router.children.*
import com.arkivanov.decompose.value.Value

/**
 * Tab navigation model: fixed tabs, with a full-screen tab on top of them.
 */
data class Tabs<out C : Any>(
    val items: List<C>,
    val selectedIndex: Int,
    val screen: C? = null,
) {
    init {
        if (items.isNotEmpty()) {
            require(selectedIndex in items.indices) {
                "The selectedIndex argument must be within the range: ${items.indices}. Actual: $selectedIndex."
            }
        }
    }
}

data class ChildTabs<out C : Any, out T : Any>(
    val items: List<Child.Created<C, T>>,
    val selectedIndex: Int,
    val screen: Child.Created<C, T>? = null,
) {
    val active: Child.Created<C, T>?
        get() = screen ?: items.getOrNull(selectedIndex)
}

/**
 * Navigation of [Tabs]: select replaces the active tab or the screen, pop closes the screen.
 */
interface TabsNavigation<C : Any> : NavigationSource<TabsNavigation.Event<C>> {
    sealed interface Event<out C : Any> {
        data class Select<out C : Any>(val tab: C) : Event<C>
        data object Pop : Event<Nothing>
    }

    fun select(tab: C)

    fun pop()
}

fun <C : Any> TabsNavigation(): TabsNavigation<C> =
    object : TabsNavigation<C> {
        private val navigation = SimpleNavigation<TabsNavigation.Event<C>>()

        override fun subscribe(observer: (TabsNavigation.Event<C>) -> Unit): Cancellation =
            navigation.subscribe(observer)

        override fun select(tab: C) {
            navigation.navigate(TabsNavigation.Event.Select(tab))
        }

        override fun pop() {
            navigation.navigate(TabsNavigation.Event.Pop)
        }
    }

fun <Ctx : GenericComponentContext<Ctx>, C : Any, T : Any> Ctx.childTabs(
    source: NavigationSource<TabsNavigation.Event<C>>,
    initialTabs: () -> Tabs<C>,
    key: String = "DefaultChildTabs",
    childFactory: (configuration: C, Ctx) -> T,
): Value<ChildTabs<C, T>> =
    children(
        source = source,
        key = key,
        initialState = { TabsNavState(initialTabs()) },
        saveState = { null },
        restoreState = { null },
        navTransformer = { state, event ->
            TabsNavState(
                when (event) {
                    is TabsNavigation.Event.Select -> {
                        val index = state.tabs.items.indexOf(event.tab)
                        if (index != -1) {
                            state.tabs.copy(selectedIndex = index, screen = null)
                        } else {
                            state.tabs.copy(screen = event.tab)
                        }
                    }

                    is TabsNavigation.Event.Pop -> state.tabs.copy(screen = null)
                },
            )
        },
        stateMapper = { state, children ->
            @Suppress("UNCHECKED_CAST")
            val createdChildren = children as List<Child.Created<C, T>>
            ChildTabs(
                items = createdChildren.take(state.tabs.items.size),
                selectedIndex = state.tabs.selectedIndex,
                screen = createdChildren.getOrNull(state.tabs.items.size),
            )
        },
        backTransformer = { state ->
            state.tabs.screen?.let {
                { state.copy(tabs = state.tabs.copy(screen = null)) }
            }
        },
        childFactory = childFactory,
    )

private data class TabsNavState<out C : Any>(
    val tabs: Tabs<C>,
) : NavState<C> {
    override val children: List<ChildNavState<C>> =
        tabs.items.mapIndexed { index, configuration ->
            SimpleChildNavState(
                configuration = configuration,
                status = if (tabs.screen == null && index == tabs.selectedIndex) {
                    ChildNavState.Status.RESUMED
                } else {
                    ChildNavState.Status.CREATED
                },
            )
        } + listOfNotNull(
            tabs.screen?.let { configuration ->
                SimpleChildNavState(
                    configuration = configuration,
                    status = ChildNavState.Status.RESUMED,
                )
            },
        )
}
