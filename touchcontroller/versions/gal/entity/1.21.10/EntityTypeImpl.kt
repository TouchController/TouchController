/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.gal.entity.v1_21_10

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.entity.EntityType
import top.fifthlight.combine.backend.minecraft.text.v1_21_10.TextImpl
import top.fifthlight.combine.backend.minecraft.identifier.v1_21_10.toCombine
import top.fifthlight.combine.data.Text
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl
import top.fifthlight.touchcontroller.common.gal.entity.EntityTypeProvider
import top.fifthlight.touchcontroller.common.gal.entity.EntityType as TouchControllerEntityType

data class EntityTypeImpl(val inner: EntityType<*>) : TouchControllerEntityType() {
    override val identifier
        get() = BuiltInRegistries.ENTITY_TYPE.getKey(inner).toCombine()
    override val name: Text
        get() = TextImpl(inner.description)
}

@ActualImpl(EntityTypeProvider::class)
object EntityTypeProviderImpl : EntityTypeProvider {
    @JvmStatic
    @ActualConstructor
    fun of(): EntityTypeProvider = EntityTypeProviderImpl

    override val allTypes: PersistentList<TouchControllerEntityType> by lazy {
        BuiltInRegistries.ENTITY_TYPE.map { EntityTypeImpl(it) }.toPersistentList()
    }

    override val player = EntityTypeImpl(EntityType.PLAYER)
    override val minecart = EntityTypeImpl(EntityType.MINECART)
    override val pig = EntityTypeImpl(EntityType.PIG)
    override val llama = EntityTypeImpl(EntityType.LLAMA)
    override val strider = EntityTypeImpl(EntityType.STRIDER)

    override val boats: PersistentList<TouchControllerEntityType> by lazy {
        listOf(
            EntityType.OAK_BOAT,
            EntityType.SPRUCE_BOAT,
            EntityType.BIRCH_BOAT,
            EntityType.JUNGLE_BOAT,
            EntityType.ACACIA_BOAT,
            EntityType.CHERRY_BOAT,
            EntityType.DARK_OAK_BOAT,
            EntityType.PALE_OAK_BOAT,
            EntityType.MANGROVE_BOAT,
            EntityType.BAMBOO_RAFT,
        ).map { EntityTypeImpl(it) }.toPersistentList()
    }

    override val horses: PersistentList<TouchControllerEntityType> by lazy {
        listOf(
            EntityType.HORSE,
            EntityType.SKELETON_HORSE,
            EntityType.ZOMBIE_HORSE,
            EntityType.DONKEY,
            EntityType.MULE,
        ).map { EntityTypeImpl(it) }.toPersistentList()
    }

    override val camel: PersistentList<TouchControllerEntityType> by lazy {
        listOf(
            EntityType.CAMEL,
        ).map { EntityTypeImpl(it) }.toPersistentList()
    }
}
