/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.gal.player.v1_20_1

import kotlinx.collections.immutable.toPersistentList
import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.world.InteractionHand
import top.fifthlight.combine.backend.minecraft.item.v1_20_1.ItemStackImpl
import top.fifthlight.combine.backend.minecraft.item.v1_20_1.toCombine
import top.fifthlight.combine.item.data.Item
import top.fifthlight.combine.item.data.ItemStack
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl
import top.fifthlight.touchcontroller.common.config.item.ItemList
import top.fifthlight.touchcontroller.common.gal.entity.EntityType
import top.fifthlight.touchcontroller.common.gal.player.PlayerHandle
import top.fifthlight.touchcontroller.common.gal.player.PlayerInventory
import top.fifthlight.touchcontroller.extension.v1_20_1.SyncableGameMode
import top.fifthlight.touchcontroller.gal.entity.v1_20_1.EntityTypeImpl

@ActualImpl(PlayerHandle::class)
class PlayerHandleImpl(override val inner: LocalPlayer) : PlayerHandle {
    companion object {
        @JvmStatic
        @ActualConstructor
        fun current(): PlayerHandle? = Minecraft.getInstance().player?.let(::PlayerHandleImpl)
    }

    private val client: Minecraft
        get() = Minecraft.getInstance()

    override fun changeLookDirection(deltaYaw: Double, deltaPitch: Double) {
        // Magic value 0.15 from net.minecraft.entity.Entity.turn
        inner.turn(deltaYaw / 0.15, deltaPitch / 0.15)
    }

    override fun dropSlot(index: Int) {
        if (index == currentSelectedSlot) {
            inner.drop(true)
            return
        }

        val originalSlot = currentSelectedSlot
        val interactionManagerAccessor = client.gameMode as SyncableGameMode

        // Can it trigger anti-cheat?
        currentSelectedSlot = index
        interactionManagerAccessor.`touchcontroller$callSyncSelectedSlot`()

        inner.drop(true)

        currentSelectedSlot = originalSlot
        interactionManagerAccessor.`touchcontroller$callSyncSelectedSlot`()
    }

    override val isUsingItem: Boolean
        get() = inner.isUsingItem

    override val onGround: Boolean
        get() = inner.onGround()

    override var isFlying: Boolean
        get() = inner.abilities.flying
        set(value) {
            inner.abilities.flying = value
        }

    override val isSubmergedInWater: Boolean
        get() = inner.isUnderWater

    override val isTouchingWater: Boolean
        get() = inner.isInWater

    override var isSprinting: Boolean
        get() = inner.isSprinting
        set(value) {
            inner.isSprinting = value
        }

    override val isSneaking: Boolean
        get() = inner.isSteppingCarefully

    override val canFly: Boolean
        get() = inner.abilities.mayfly

    override val instantBuild: Boolean
        get() = inner.abilities.instabuild

    override var currentSelectedSlot: Int
        get() = inner.inventory.selected
        set(value) {
            inner.inventory.selected = value
        }

    override fun hasItemsOnHand(list: ItemList): Boolean = InteractionHand.entries.any { hand ->
        inner.getItemInHand(hand).toCombine().item in list
    }

    override fun matchesItemOnHand(item: Item): Boolean = InteractionHand.entries.any { hand ->
        item.matches(inner.getItemInHand(hand).toCombine().item)
    }

    override fun getInventory() = PlayerInventory(
        main = inner.inventory.items.map { it.toCombine() }.toPersistentList(),
        armor = inner.inventory.armor.map { it.toCombine() }.toPersistentList(),
        offHand = inner.inventory.offhand.first().toCombine(),
    )

    override fun getInventorySlot(index: Int): ItemStack = ItemStackImpl(inner.inventory.getItem(index))

    override val ridingEntityType: EntityType?
        get() = inner.vehicle?.type?.let { EntityTypeImpl(it) }
}
