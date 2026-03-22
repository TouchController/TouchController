package top.fifthlight.touchcontroller.common.config.preset

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import kotlin.collections.map
import kotlin.collections.toMap
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

data class PresetsContainer(
    val orderedEntries: PersistentList<Pair<Uuid, LayoutPreset>> = persistentListOf(),
) : PersistentMap<Uuid, LayoutPreset> by orderedEntries.toMap().toPersistentMap() {
    val order: ImmutableList<Uuid>
        get() = orderedEntries.map { it.first }.toPersistentList()
}

fun PresetsContainer(
    presets: ImmutableMap<Uuid, LayoutPreset>,
    order: ImmutableList<Uuid>,
): PresetsContainer {
    val orderedEntries = mutableListOf<Pair<Uuid, LayoutPreset>>()
    val entries = presets.toMutableMap()
    for (uuid in order) {
        val preset = entries.remove(uuid) ?: continue
        orderedEntries += Pair(uuid, preset)
    }
    for ((uuid, preset) in entries.entries.sortedBy { (id, _) -> id.toJavaUuid() }) {
        orderedEntries += uuid to preset
    }
    return PresetsContainer(orderedEntries.toPersistentList())
}
