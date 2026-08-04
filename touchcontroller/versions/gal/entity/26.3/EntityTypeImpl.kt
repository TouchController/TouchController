/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.gal.entity.v26_3

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.EntityTypes
import top.fifthlight.combine.backend.minecraft.identifier.v26_3.toCombine
import top.fifthlight.combine.backend.minecraft.text.v26_3.TextImpl
import top.fifthlight.combine.core.data.Text
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

    override val player = EntityTypeImpl(EntityTypes.PLAYER)
    override val minecart = EntityTypeImpl(EntityTypes.MINECART)
    override val pig = EntityTypeImpl(EntityTypes.PIG)
    override val llama = EntityTypeImpl(EntityTypes.LLAMA)
    override val strider = EntityTypeImpl(EntityTypes.STRIDER)

    override val boats: PersistentList<TouchControllerEntityType> by lazy {
        listOf(
            EntityTypes.OAK_BOAT,
            EntityTypes.SPRUCE_BOAT,
            EntityTypes.BIRCH_BOAT,
            EntityTypes.JUNGLE_BOAT,
            EntityTypes.ACACIA_BOAT,
            EntityTypes.CHERRY_BOAT,
            EntityTypes.DARK_OAK_BOAT,
            EntityTypes.PALE_OAK_BOAT,
            EntityTypes.MANGROVE_BOAT,
            EntityTypes.BAMBOO_RAFT,
        ).map { EntityTypeImpl(it) }.toPersistentList()
    }

    override val horses: PersistentList<TouchControllerEntityType> by lazy {
        listOf(
            EntityTypes.HORSE,
            EntityTypes.SKELETON_HORSE,
            EntityTypes.ZOMBIE_HORSE,
            EntityTypes.DONKEY,
            EntityTypes.MULE,
        ).map { EntityTypeImpl(it) }.toPersistentList()
    }

    override val camel: PersistentList<TouchControllerEntityType> by lazy {
        listOf(
            EntityTypes.CAMEL,
            EntityTypes.CAMEL_HUSK,
        ).map { EntityTypeImpl(it) }.toPersistentList()
    }
}
