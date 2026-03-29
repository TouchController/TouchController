/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.gal.player.v1_21_10

import kotlinx.collections.immutable.toPersistentList
import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.player.Inventory
import top.fifthlight.combine.backend.minecraft.item.v1_21_10.ItemStackImpl
import top.fifthlight.combine.backend.minecraft.item.v1_21_10.toCombine
import top.fifthlight.combine.item.data.Item
import top.fifthlight.combine.item.data.ItemStack
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl
import top.fifthlight.touchcontroller.common.config.item.ItemList
import top.fifthlight.touchcontroller.common.gal.entity.EntityType
import top.fifthlight.touchcontroller.common.gal.player.PlayerHandle
import top.fifthlight.touchcontroller.common.gal.player.PlayerInventory
import top.fifthlight.touchcontroller.extension.v1_21_10.SyncableGameMode
import top.fifthlight.touchcontroller.gal.entity.v1_21_10.EntityTypeImpl

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
        get() = inner.inventory.selectedSlot
        set(value) {
            inner.inventory.selectedSlot = value
        }

    override fun hasItemsOnHand(list: ItemList): Boolean = InteractionHand.entries.any { hand ->
        inner.getItemInHand(hand).toCombine().item in list
    }

    override fun matchesItemOnHand(item: Item): Boolean = InteractionHand.entries.any { hand ->
        item.matches(inner.getItemInHand(hand).toCombine().item)
    }

    override fun getInventory() = PlayerInventory(
        main = inner.inventory.nonEquipmentItems.map { it.toCombine() }.toPersistentList(),
        armor = Inventory.EQUIPMENT_SLOT_MAPPING
            .filterValues { it != EquipmentSlot.OFFHAND }
            .keys
            .map { inner.inventory.getItem(it).toCombine() }
            .toPersistentList(),
        offHand = inner.inventory.getItem(Inventory.SLOT_OFFHAND).toCombine(),
    )

    override fun getInventorySlot(index: Int): ItemStack = ItemStackImpl(inner.inventory.getItem(index))

    override val ridingEntityType: EntityType?
        get() = inner.vehicle?.type?.let { EntityTypeImpl(it) }
}
