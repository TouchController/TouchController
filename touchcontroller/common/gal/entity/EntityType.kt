/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.gal.entity

import kotlinx.collections.immutable.PersistentList
import top.fifthlight.combine.core.data.Identifier
import top.fifthlight.combine.core.data.Text
import top.fifthlight.mergetools.api.ExpectFactory

abstract class EntityType {
    abstract val identifier: Identifier
    abstract val name: Text
}

interface EntityTypeProvider {
    val allTypes: PersistentList<EntityType>

    val player: EntityType
    val minecart: EntityType?
    val pig: EntityType?
    val llama: EntityType?
    val strider: EntityType?

    val boats: PersistentList<EntityType>
    val horses: PersistentList<EntityType>
    val camel: PersistentList<EntityType>

    @ExpectFactory
    interface Factory {
        fun of(): EntityTypeProvider
    }

    companion object : EntityTypeProvider by EntityTypeProviderFactory.of()
}
