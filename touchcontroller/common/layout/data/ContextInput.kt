/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.layout.data

import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf
import top.fifthlight.combine.item.data.Item
import top.fifthlight.touchcontroller.common.config.condition.input.BuiltinLayerCondition
import top.fifthlight.touchcontroller.common.config.condition.input.LayerConditionInput
import top.fifthlight.touchcontroller.common.gal.entity.EntityType
import top.fifthlight.touchcontroller.common.gal.player.PlayerHandle
import top.fifthlight.touchcontroller.common.gal.gamestate.CameraPerspective
import top.fifthlight.touchcontroller.common.gal.gamestate.CameraPerspectiveFactory
import top.fifthlight.touchcontroller.common.gal.view.CrosshairTarget
import kotlin.uuid.Uuid

data class ContextInput(
    val inGui: Boolean = false,
    override val builtinCondition: PersistentSet<BuiltinLayerCondition> = persistentSetOf(),
    override val customCondition: PersistentSet<Uuid> = persistentSetOf(),
    override val crosshairTarget: CrosshairTarget? = null,
    override val ridingEntity: EntityType? = null,
    val playerHandle: PlayerHandle? = null,
    val perspective: CameraPerspective = CameraPerspectiveFactory.firstPerson(),
) : LayerConditionInput {
    companion object {
        val EMPTY = ContextInput()
    }

    override fun holdingItem(item: Item): Boolean = playerHandle?.matchesItemOnHand(item) ?: false
}
