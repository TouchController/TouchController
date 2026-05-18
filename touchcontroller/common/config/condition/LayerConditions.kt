/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.config.condition

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import top.fifthlight.combine.core.data.Identifier
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.common.config.condition.input.LayerConditionInput
import top.fifthlight.touchcontroller.common.config.condition.serializer.LayerConditionsSerializer

@Serializable(with = LayerConditionsSerializer::class)
data class LayerConditions(
    val conditions: PersistentList<Item> = persistentListOf(),
) {
    @Serializable
    data class Item(
        val key: Key,
        val value: Value,
    )

    @Serializable
    sealed interface Key {
        fun isFulfilled(input: LayerConditionInput): Boolean
    }

    @Serializable
    enum class Value(val text: Identifier) {
        @SerialName("never")
        NEVER(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CONDITIONS_NEVER),

        @SerialName("want")
        WANT(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CONDITIONS_WANT),

        @SerialName("require")
        REQUIRE(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CONDITIONS_REQUIRE);
    }

    fun check(input: LayerConditionInput): Boolean {
        var haveWant = false
        var haveFulfilledWant = false

        for ((key, value) in conditions) {
            val current = key.isFulfilled(input)
            when (value) {
                Value.NEVER -> if (current) {
                    return false
                }

                Value.WANT -> {
                    haveWant = true
                    if (current) {
                        haveFulfilledWant = true
                    }
                }

                Value.REQUIRE -> if (!current) {
                    return false
                }
            }
        }

        return !(haveWant && !haveFulfilledWant)
    }
}

fun layerConditionsOf(vararg conditions: Pair<LayerConditions.Key, LayerConditions.Value>) = LayerConditions(
    conditions = conditions.map { (key, value) -> LayerConditions.Item(key, value) }.toPersistentList(),
)
