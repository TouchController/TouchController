/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.gal.entity.v1_21_10

import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.Items
import top.fifthlight.combine.backend.minecraft.item.v1_21_10.toCombine
import net.minecraft.world.item.SpawnEggItem
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
            EntityType.WIND_CHARGE -> Items.WIND_CHARGE
            EntityType.SNOWBALL -> Items.SNOWBALL
            EntityType.EGG -> Items.EGG
            EntityType.FIREBALL -> Items.FIRE_CHARGE
            EntityType.FIREWORK_ROCKET -> Items.FIREWORK_ROCKET
            EntityType.TNT -> Items.TNT
            EntityType.SMALL_FIREBALL -> Items.FIREWORK_ROCKET
            EntityType.ACACIA_BOAT -> Items.ACACIA_BOAT
            EntityType.ACACIA_CHEST_BOAT -> Items.ACACIA_CHEST_BOAT
            EntityType.BAMBOO_RAFT -> Items.BAMBOO_RAFT
            EntityType.BAMBOO_CHEST_RAFT -> Items.BAMBOO_CHEST_RAFT
            EntityType.BIRCH_BOAT -> Items.BIRCH_BOAT
            EntityType.BIRCH_CHEST_BOAT -> Items.BIRCH_CHEST_BOAT
            EntityType.SPECTRAL_ARROW -> Items.SPECTRAL_ARROW
            EntityType.CHERRY_BOAT -> Items.CHERRY_BOAT
            EntityType.CHERRY_CHEST_BOAT -> Items.CHERRY_CHEST_BOAT
            EntityType.DARK_OAK_BOAT -> Items.DARK_OAK_BOAT
            EntityType.DARK_OAK_CHEST_BOAT -> Items.DARK_OAK_CHEST_BOAT
            EntityType.DRAGON_FIREBALL -> Items.DRAGON_BREATH
            EntityType.ENDER_PEARL -> Items.ENDER_PEARL
            EntityType.EYE_OF_ENDER -> Items.ENDER_EYE
            EntityType.END_CRYSTAL -> Items.END_CRYSTAL
            EntityType.EXPERIENCE_BOTTLE -> Items.EXPERIENCE_BOTTLE
            EntityType.EXPERIENCE_ORB -> Items.EXPERIENCE_BOTTLE
            EntityType.FALLING_BLOCK -> Items.SAND
            EntityType.GIANT -> Items.ZOMBIE_HEAD
            EntityType.GLOW_ITEM_FRAME -> Items.GLOW_ITEM_FRAME
            EntityType.JUNGLE_BOAT -> Items.JUNGLE_BOAT
            EntityType.JUNGLE_CHEST_BOAT -> Items.JUNGLE_CHEST_BOAT
            EntityType.LEASH_KNOT -> Items.LEAD
            EntityType.LIGHTNING_BOLT -> Items.LIGHTNING_ROD
            EntityType.MANGROVE_BOAT -> Items.MANGROVE_BOAT
            EntityType.MANGROVE_CHEST_BOAT -> Items.MANGROVE_CHEST_BOAT
            EntityType.OAK_BOAT -> Items.OAK_BOAT
            EntityType.OAK_CHEST_BOAT -> Items.OAK_CHEST_BOAT
            EntityType.OMINOUS_ITEM_SPAWNER -> Items.TRIAL_SPAWNER
            EntityType.PALE_OAK_BOAT -> Items.PALE_OAK_BOAT
            EntityType.PALE_OAK_CHEST_BOAT -> Items.PALE_OAK_CHEST_BOAT
            EntityType.SPLASH_POTION -> Items.SPLASH_POTION
            EntityType.LINGERING_POTION -> Items.LINGERING_POTION
            EntityType.SHULKER_BULLET -> Items.SHULKER_SHELL
            EntityType.SPAWNER_MINECART -> Items.SPAWNER
            EntityType.SPRUCE_BOAT -> Items.SPRUCE_BOAT
            EntityType.SPRUCE_CHEST_BOAT -> Items.SPRUCE_CHEST_BOAT
            EntityType.TRIDENT -> Items.TRIDENT
            EntityType.WITHER_SKULL -> Items.WITHER_SKELETON_SKULL
            EntityType.FISHING_BOBBER -> Items.FISHING_ROD
            else -> SpawnEggItem.byId(entityType)
        }?.toCombine()
}
