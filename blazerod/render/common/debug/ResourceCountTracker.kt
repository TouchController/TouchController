package top.fifthlight.blazerod.render.common.debug

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class ResourceCountTracker {
    private val refCounts: ConcurrentMap<String, Int> = ConcurrentHashMap()

    fun increase(identifier: String) {
        refCounts.compute(identifier) { key, value -> (value ?: 0) + 1 }
    }

    fun decrease(identifier: String) {
        refCounts.compute(identifier) { key, value ->
            // Crash at here!
            check(value != null && value > 0) { "Decrease null or zero count: $key" }
            value - 1
        }
    }

    fun dumpData() = refCounts.toMap()

    companion object {
        @Volatile
        var instance: ResourceCountTracker? = null
            private set

        fun initialize() {
            synchronized(Companion) {
                if (instance != null) {
                    return
                }
                instance = ResourceCountTracker()
            }
        }
    }
}
