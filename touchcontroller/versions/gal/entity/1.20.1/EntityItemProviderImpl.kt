/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.gal.entity.v1_20_1

import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.Items
import net.minecraft.world.item.SpawnEggItem
import top.fifthlight.combine.backend.minecraft.item.v1_20_1.toCombine
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl
import top.fifthlight.touchcontroller.common.gal.entity.EntityItemProvider
import top.fifthlight.touchcontroller.common.gal.entity.EntityType as TouchControllerEntityType

@ActualImpl(EntityItemProvider::class)
object EntityItemProviderImpl : EntityItemProvider {
    @JvmStatic
    @ActualConstructor
    fun of(): EntityItemProvider = this

    override fun getEntityIconItem(entity: TouchControllerEntityType) =
        when (val entityType = (entity as EntityTypeImpl).inner) {
            EntityType.PAINTING -> Items.PAINTING
            EntityType.ARMOR_STAND -> Items.ARMOR_STAND
            EntityType.MINECART -> Items.MINECART
            EntityType.TNT_MINECART -> Items.TNT_MINECART
            EntityType.CHEST_MINECART -> Items.CHEST_MINECART
            EntityType.HOPPER_MINECART -> Items.HOPPER_MINECART
            EntityType.FURNACE_MINECART -> Items.FURNACE_MINECART
            EntityType.COMMAND_BLOCK_MINECART -> Items.COMMAND_BLOCK_MINECART
            EntityType.AREA_EFFECT_CLOUD -> Items.LINGERING_POTION
            EntityType.ARROW -> Items.ARROW
            EntityType.ITEM_FRAME -> Items.ITEM_FRAME
            EntityType.PLAYER -> Items.PLAYER_HEAD
            EntityType.SNOWBALL -> Items.SNOWBALL
            EntityType.EGG -> Items.EGG
            EntityType.FIREBALL -> Items.FIRE_CHARGE
            EntityType.FIREWORK_ROCKET -> Items.FIREWORK_ROCKET
            EntityType.TNT -> Items.TNT
            EntityType.SMALL_FIREBALL -> Items.FIREWORK_ROCKET
            EntityType.BOAT -> Items.OAK_BOAT
            EntityType.CHEST_BOAT -> Items.OAK_CHEST_BOAT
            EntityType.SPECTRAL_ARROW -> Items.SPECTRAL_ARROW
            EntityType.DRAGON_FIREBALL -> Items.DRAGON_BREATH
            EntityType.ENDER_PEARL -> Items.ENDER_PEARL
            EntityType.EYE_OF_ENDER -> Items.ENDER_EYE
            EntityType.END_CRYSTAL -> Items.END_CRYSTAL
            EntityType.EXPERIENCE_BOTTLE -> Items.EXPERIENCE_BOTTLE
            EntityType.EXPERIENCE_ORB -> Items.EXPERIENCE_BOTTLE
            EntityType.FALLING_BLOCK -> Items.SAND
            EntityType.GIANT -> Items.ZOMBIE_HEAD
            EntityType.GLOW_ITEM_FRAME -> Items.GLOW_ITEM_FRAME
            EntityType.LEASH_KNOT -> Items.LEAD
            EntityType.LIGHTNING_BOLT -> Items.LIGHTNING_ROD
            EntityType.POTION -> Items.SPLASH_POTION
            EntityType.SHULKER_BULLET -> Items.SHULKER_SHELL
            EntityType.SPAWNER_MINECART -> Items.SPAWNER
            EntityType.TRIDENT -> Items.TRIDENT
            EntityType.WITHER_SKULL -> Items.WITHER_SKELETON_SKULL
            EntityType.FISHING_BOBBER -> Items.FISHING_ROD
            else -> SpawnEggItem.byId(entityType)
        }?.toCombine()
}
