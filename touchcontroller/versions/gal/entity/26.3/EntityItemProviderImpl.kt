/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.gal.entity.v26_3

import net.minecraft.world.entity.EntityTypes
import net.minecraft.world.item.Items
import net.minecraft.world.item.SpawnEggItem
import top.fifthlight.combine.backend.minecraft.item.v26_3.toCombine
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl
import top.fifthlight.touchcontroller.common.gal.entity.EntityItemProvider
import kotlin.jvm.optionals.getOrNull
import top.fifthlight.touchcontroller.common.gal.entity.EntityType as TouchControllerEntityType

@ActualImpl(EntityItemProvider::class)
object EntityItemProviderImpl : EntityItemProvider {
    @JvmStatic
    @ActualConstructor
    fun of(): EntityItemProvider = this

    override fun getEntityIconItem(entity: TouchControllerEntityType) =
        when (val entityType = (entity as EntityTypeImpl).inner) {
            EntityTypes.PAINTING -> Items.PAINTING
            EntityTypes.ARMOR_STAND -> Items.ARMOR_STAND
            EntityTypes.MINECART -> Items.MINECART
            EntityTypes.TNT_MINECART -> Items.TNT_MINECART
            EntityTypes.CHEST_MINECART -> Items.CHEST_MINECART
            EntityTypes.HOPPER_MINECART -> Items.HOPPER_MINECART
            EntityTypes.FURNACE_MINECART -> Items.FURNACE_MINECART
            EntityTypes.COMMAND_BLOCK_MINECART -> Items.COMMAND_BLOCK_MINECART
            EntityTypes.AREA_EFFECT_CLOUD -> Items.LINGERING_POTION
            EntityTypes.ARROW -> Items.ARROW
            EntityTypes.ITEM_FRAME -> Items.ITEM_FRAME
            EntityTypes.PLAYER -> Items.PLAYER_HEAD
            EntityTypes.WIND_CHARGE -> Items.WIND_CHARGE
            EntityTypes.SNOWBALL -> Items.SNOWBALL
            EntityTypes.EGG -> Items.EGG
            EntityTypes.FIREBALL -> Items.FIRE_CHARGE
            EntityTypes.FIREWORK_ROCKET -> Items.FIREWORK_ROCKET
            EntityTypes.TNT -> Items.TNT
            EntityTypes.SMALL_FIREBALL -> Items.FIREWORK_ROCKET
            EntityTypes.ACACIA_BOAT -> Items.ACACIA_BOAT
            EntityTypes.ACACIA_CHEST_BOAT -> Items.ACACIA_CHEST_BOAT
            EntityTypes.BAMBOO_RAFT -> Items.BAMBOO_RAFT
            EntityTypes.BAMBOO_CHEST_RAFT -> Items.BAMBOO_CHEST_RAFT
            EntityTypes.BIRCH_BOAT -> Items.BIRCH_BOAT
            EntityTypes.BIRCH_CHEST_BOAT -> Items.BIRCH_CHEST_BOAT
            EntityTypes.SPECTRAL_ARROW -> Items.SPECTRAL_ARROW
            EntityTypes.CHERRY_BOAT -> Items.CHERRY_BOAT
            EntityTypes.CHERRY_CHEST_BOAT -> Items.CHERRY_CHEST_BOAT
            EntityTypes.DARK_OAK_BOAT -> Items.DARK_OAK_BOAT
            EntityTypes.DARK_OAK_CHEST_BOAT -> Items.DARK_OAK_CHEST_BOAT
            EntityTypes.DRAGON_FIREBALL -> Items.DRAGON_BREATH
            EntityTypes.ENDER_PEARL -> Items.ENDER_PEARL
            EntityTypes.EYE_OF_ENDER -> Items.ENDER_EYE
            EntityTypes.END_CRYSTAL -> Items.END_CRYSTAL
            EntityTypes.EXPERIENCE_BOTTLE -> Items.EXPERIENCE_BOTTLE
            EntityTypes.EXPERIENCE_ORB -> Items.EXPERIENCE_BOTTLE
            EntityTypes.FALLING_BLOCK -> Items.SAND
            EntityTypes.GIANT -> Items.ZOMBIE_HEAD
            EntityTypes.GLOW_ITEM_FRAME -> Items.GLOW_ITEM_FRAME
            EntityTypes.JUNGLE_BOAT -> Items.JUNGLE_BOAT
            EntityTypes.JUNGLE_CHEST_BOAT -> Items.JUNGLE_CHEST_BOAT
            EntityTypes.LEASH_KNOT -> Items.LEAD
            EntityTypes.LIGHTNING_BOLT -> Items.LIGHTNING_ROD.weathering().unaffected()
            EntityTypes.MANGROVE_BOAT -> Items.MANGROVE_BOAT
            EntityTypes.MANGROVE_CHEST_BOAT -> Items.MANGROVE_CHEST_BOAT
            EntityTypes.OAK_BOAT -> Items.OAK_BOAT
            EntityTypes.OAK_CHEST_BOAT -> Items.OAK_CHEST_BOAT
            EntityTypes.OMINOUS_ITEM_SPAWNER -> Items.TRIAL_SPAWNER
            EntityTypes.PALE_OAK_BOAT -> Items.PALE_OAK_BOAT
            EntityTypes.PALE_OAK_CHEST_BOAT -> Items.PALE_OAK_CHEST_BOAT
            EntityTypes.SPLASH_POTION -> Items.SPLASH_POTION
            EntityTypes.LINGERING_POTION -> Items.LINGERING_POTION
            EntityTypes.SHULKER_BULLET -> Items.SHULKER_SHELL
            EntityTypes.SPAWNER_MINECART -> Items.SPAWNER
            EntityTypes.SPRUCE_BOAT -> Items.SPRUCE_BOAT
            EntityTypes.SPRUCE_CHEST_BOAT -> Items.SPRUCE_CHEST_BOAT
            EntityTypes.TRIDENT -> Items.TRIDENT
            EntityTypes.WITHER_SKULL -> Items.WITHER_SKELETON_SKULL
            EntityTypes.FISHING_BOBBER -> Items.FISHING_ROD
            else -> SpawnEggItem.byId(entityType).getOrNull()?.value()
        }?.toCombine()
}
