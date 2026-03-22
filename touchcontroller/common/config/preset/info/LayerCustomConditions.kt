package top.fifthlight.touchcontroller.common.config.preset.info

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable
import top.fifthlight.touchcontroller.common.config.preset.info.serializer.LayerCustomConditionsSerializer

@JvmInline
@Serializable(with = LayerCustomConditionsSerializer::class)
value class LayerCustomConditions(
    val conditions: PersistentList<CustomCondition> = persistentListOf(),
)
