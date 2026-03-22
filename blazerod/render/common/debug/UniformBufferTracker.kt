package top.fifthlight.blazerod.render.common.debug

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class UniformBufferTracker {
    val bufferCapacity: ConcurrentMap<String, Int> = ConcurrentHashMap()

    fun set(name: String, newCapacity: Int) {
        bufferCapacity[name] = newCapacity
    }

    fun dumpData() = bufferCapacity.toMap()

    companion object {
        @Volatile
        var instance: UniformBufferTracker? = null
            private set

        fun initialize() {
            synchronized(Companion) {
                if (instance != null) {
                    return
                }
                instance = UniformBufferTracker()
            }
        }
    }
}
