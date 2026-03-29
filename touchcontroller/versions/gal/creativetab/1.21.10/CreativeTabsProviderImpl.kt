/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.gal.creativetab.v1_21_10

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.CreativeModeTabs
import top.fifthlight.combine.backend.minecraft.text.v1_21_10.TextImpl
import top.fifthlight.combine.backend.minecraft.item.v1_21_10.toCombine
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl
import top.fifthlight.touchcontroller.common.gal.player.PlayerHandle
import top.fifthlight.touchcontroller.common.gal.creativetab.CreativeTabsProvider
import top.fifthlight.touchcontroller.gal.player.v1_21_10.PlayerHandleImpl
import kotlin.jvm.optionals.getOrNull

@ActualImpl(CreativeTabsProvider::class)
object CreativeTabsProviderImpl : CreativeTabsProvider {
    @JvmStatic
    @ActualConstructor
    fun of(): CreativeTabsProvider = this

    private val client = Minecraft.getInstance()

    class CreativeTabImpl(group: CreativeModeTab) : CreativeTabsProvider.CreativeTab {
        override val type = when (group.type) {
            CreativeModeTab.Type.CATEGORY -> CreativeTabsProvider.CreativeTab.Type.CATEGORY
            CreativeModeTab.Type.INVENTORY -> CreativeTabsProvider.CreativeTab.Type.SURVIVAL_INVENTORY
            CreativeModeTab.Type.HOTBAR -> error("Bad item group")
            CreativeModeTab.Type.SEARCH -> CreativeTabsProvider.CreativeTab.Type.SEARCH
        }

        override val name = TextImpl(group.displayName)

        override val icon = group.iconItem.toCombine()

        override val items = group.displayItems.map { it.toCombine() }.toPersistentList()
    }

    val LocalPlayer.shouldShowOperatorTab
        get() = canUseGameMasterBlocks() && client.options.operatorItemsTab().get()

    fun LocalPlayer.refreshCreativeTabs(shouldShowOperatorTab: Boolean) {
        val enabledFeatures = connection.enabledFeatures()
        CreativeModeTabs.tryRebuildTabContents(enabledFeatures, shouldShowOperatorTab, level().registryAccess())
    }

    private val opBlocks = ResourceLocation.withDefaultNamespace("op_blocks")

    override fun getCreativeTabs(player: PlayerHandle): PersistentList<CreativeTabsProvider.CreativeTab> {
        val player = (player as PlayerHandleImpl).inner
        val shouldShowOperatorTab = player.shouldShowOperatorTab
        player.refreshCreativeTabs(shouldShowOperatorTab)
        return CreativeModeTabs
            .tabs()
            .filter {
                if (it.type == CreativeModeTab.Type.HOTBAR) {
                    return@filter false
                }
                val key = BuiltInRegistries.CREATIVE_MODE_TAB.getResourceKey(it).getOrNull()
                if (key?.location() == opBlocks) {
                    return@filter shouldShowOperatorTab
                }
                true
            }
            .map(::CreativeTabImpl)
            .toPersistentList()
    }
}
