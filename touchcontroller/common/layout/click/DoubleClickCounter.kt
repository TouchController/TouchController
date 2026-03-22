package top.fifthlight.touchcontroller.common.layout.click

import kotlin.collections.set
import kotlin.uuid.Uuid

class DoubleClickCounter(
    private val lastClickTimes: MutableMap<Uuid, CounterEntry> = kotlin.collections.mutableMapOf()
) {
    data class CounterEntry(
        val lastClickTick: Int = -1,
        val lastUpdateTick: Int,
    )

    fun update(tick: Int, uuid: Uuid) {
        val entry = lastClickTimes[uuid]
        if (entry == null) {
            lastClickTimes[uuid] = CounterEntry(lastUpdateTick = tick)
        } else {
            lastClickTimes[uuid] = entry.copy(lastUpdateTick = tick)
        }
    }

    fun click(tick: Int, uuid: Uuid, interval: Int): Boolean {
        val lastClick = lastClickTimes[uuid]
        if (lastClick == null) {
            return false
        }

        val lastClickTime = lastClick.lastClickTick
        val timeDelta = tick - lastClickTime
        val doubleClicked = timeDelta <= interval
        if (doubleClicked) {
            lastClickTimes[uuid] = lastClick.copy(lastClickTick = -1)
        } else {
            lastClickTimes[uuid] = lastClick.copy(lastClickTick = tick)
        }

        return doubleClicked
    }

    fun reset(uuid: Uuid) {
        val entry = lastClickTimes[uuid]
        if (entry != null) {
            lastClickTimes[uuid] = entry.copy(lastClickTick = -1)
        }
    }

    fun clean(tick: Int) {
        lastClickTimes.values.removeIf { it.lastUpdateTick < tick }
    }
}
